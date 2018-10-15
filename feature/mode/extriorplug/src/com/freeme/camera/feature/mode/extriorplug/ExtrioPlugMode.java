package com.freeme.camera.feature.mode.extriorplug;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.freeme.camera.CameraInterfaceManage;
import com.freeme.camera.ICameraExt;
import com.freeme.camera.IPluginModuleEntry;
import com.freeme.camera.R;
import com.freeme.camera.common.IAppUi;
import com.freeme.camera.common.IAppUi.AnimationData;
import com.freeme.camera.common.IAppUiListener;
import com.freeme.camera.common.IAppUiListener.ISurfaceStatusListener;
import com.freeme.camera.common.ICameraContext;
import com.freeme.camera.common.app.IApp;
import com.freeme.camera.common.debug.CameraSysTrace;
import com.freeme.camera.common.debug.LogHelper;
import com.freeme.camera.common.debug.LogUtil;
import com.freeme.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.freeme.camera.common.memory.IMemoryManager;
import com.freeme.camera.common.memory.MemoryManagerImpl;
import com.freeme.camera.common.mode.CameraModeBase;
import com.freeme.camera.common.mode.DeviceUsage;

import com.freeme.camera.common.relation.StatusMonitor;
import com.freeme.camera.common.relation.StatusMonitor.StatusChangeListener;
import com.freeme.camera.common.setting.ISettingManager;
import com.freeme.camera.common.setting.SettingManagerFactory;
import com.freeme.camera.common.storage.MediaSaver.MediaSaverListener;
import com.freeme.camera.common.utils.BitmapCreator;
import com.freeme.camera.common.utils.CameraUtil;
import com.freeme.camera.common.utils.Size;
import com.freeme.camera.common.pluginmanager.FreemeSceneModeData;
import com.freeme.camera.data.PictureSizeInfo;
import com.freeme.camera.feature.setting.focus.Focus;
import com.freeme.camera.ui.GridLines;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Normal photo mode that is used to take normal picture.
 */
public class ExtrioPlugMode extends CameraModeBase implements IExtrioPlugDeviceController.JpegCallback,
        IExtrioPlugDeviceController.DeviceCallback, IExtrioPlugDeviceController.PreviewSizeCallback, IMemoryManager.IMemoryListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(ExtrioPlugMode.class.getSimpleName());
    private static final String KEY_MATRIX_DISPLAY_SHOW = "key_matrix_display_show";
    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private static final String KEY_DNG = "key_dng";
    private static final String JPEG_CALLBACK = "jpeg callback";
    private static final String POST_VIEW_CALLBACK = "post view callback";

    private static final int POST_VIEW_FORMAT = ImageFormat.NV21;
    private static final long DNG_IMAGE_SIZE = 45 * 1024 * 1024;

    protected static final String PHOTO_CAPTURE_START = "start";
    protected static final String PHOTO_CAPTURE_STOP = "stop";
    protected static final String KEY_PHOTO_CAPTURE = "key_photo_capture";

    protected IExtrioPlugDeviceController mIDeviceController;
    protected ExtrioPlugModeHelper mPhotoModeHelper;
    protected int mCaptureWidth;
    // make sure the picture size ratio = mCaptureWidth / mCaptureHeight not to NAN.
    protected int mCaptureHeight = Integer.MAX_VALUE;
    //the reason is if surface is ready, let it to set to device controller, otherwise
    //if surface is ready but activity is not into resume ,will found the preview
    //can not start preview.
    protected volatile boolean mIsResumed = true;
    private String mCameraId;

    private ISurfaceStatusListener mISurfaceStatusListener = new SurfaceChangeListener();
    private ISettingManager mISettingManager;
    private MemoryManagerImpl mMemoryManager;
    private byte[] mPreviewData;
    private int mPreviewFormat;
    private int mPreviewWidth;
    private int mPreviewHeight;
    //make sure it is in capturing to show the saving UI.
    private int mCapturingNumber = 0;
    private boolean mIsMatrixDisplayShow = false;
    private Object mPreviewDataSync = new Object();
    private Object mCaptureNumberSync = new Object();
    private HandlerThread mAnimationHandlerThread;
    private Handler mAnimationHandler;
    private StatusChangeListener mStatusChangeListener = new MyStatusChangeListener();
    private IMemoryManager.MemoryAction mMemoryState = IMemoryManager.MemoryAction.NORMAL;
    protected StatusMonitor.StatusResponder mPhotoStatusResponder;



    private ViewGroup mParentViewGroup;
    private View mRootView;
    private GridLines mLargeView;
    private int scenecModeId;
    private  IApp mApp;

    public ExtrioPlugMode(int sModeId) {
        scenecModeId = sModeId;
    }

    private Camera.Size mLocalPictureSize;

    @Override
    public void init(@Nonnull IApp app, @Nonnull ICameraContext cameraContext,
                     boolean isFromLaunch) {
        LogHelper.d(TAG, "[init]+");
        super.init(app, cameraContext, isFromLaunch);
        mPhotoModeHelper = new ExtrioPlugModeHelper(cameraContext);
        createAnimationHandler();

        mCameraId = getCameraIdByFacing(mDataStore.getValue(
                KEY_CAMERA_SWITCHER, null, mDataStore.getGlobalScope()));

        // Device controller must be initialize before set preview size, because surfaceAvailable
        // may be called immediately when setPreviewSize.
        DeviceControllerFactory deviceControllerFactory = new DeviceControllerFactory();
        mIDeviceController = deviceControllerFactory.createDeviceController(app.getActivity(),
                mCameraApi, mICameraContext);
        initSettingManager(mCameraId);
        initStatusMonitor();

        app.getAppUi().registerOnPreviewAreaChangedListener(mPreviewAreaChangedListener);

        mMemoryManager = new MemoryManagerImpl(app.getActivity());
        prepareAndOpenCamera(false, mCameraId, isFromLaunch);
        LogHelper.d(TAG, "[init]- ");


        /*mParentViewGroup = mIApp.getAppUi().getModeRootView();
        View viewLayout = mIApp.getActivity().getLayoutInflater().inflate(R.layout.large_preview,
                mParentViewGroup, true);

        mRootView = viewLayout.findViewById(R.id.large_frame_layout);
        mLargeView = mRootView.findViewById(R.id.grid_lines);
        if (mRootView != null)
            mRootView.setVisibility(View.VISIBLE);
        mLargeView.setVisibility(View.VISIBLE);
        mApp = app;*/

    }

    private IAppUiListener.OnPreviewAreaChangedListener mPreviewAreaChangedListener
            = new IAppUiListener.OnPreviewAreaChangedListener() {
        @Override
        public void onPreviewAreaChanged(RectF newPreviewArea, Size previewSize) {
        }
    };


    private static final int MSG_SHOW_EXTRIO_PLUG = 1;
    private static final int MSG_SHOW_EXTRIO_PLUGDELAY_MS = 500;

    private Handler mHander = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch (what) {
                case MSG_SHOW_EXTRIO_PLUG:
                    showExtrioPlugin();
                    break;
                default:
                    break;
            }
        }

    };

    private Camera mCamera;

    @Override
    public void resume(@Nonnull DeviceUsage deviceUsage) {
        super.resume(deviceUsage);
        mIsResumed = true;
        initSettingManager(mCameraId);
        initStatusMonitor();
        mMemoryManager.addListener(this);
        mMemoryManager.initStateForCapture(
                mICameraContext.getStorageService().getCaptureStorageSpace());
        mMemoryState = IMemoryManager.MemoryAction.NORMAL;
        mIDeviceController.queryCameraDeviceManager();
        prepareAndOpenCamera(false, mCameraId, false);

        if (scenecModeId == FreemeSceneModeData.FREEME_SCENE_MODE_QRCODE_ID) {
            mHander.sendEmptyMessageDelayed(MSG_SHOW_EXTRIO_PLUG, MSG_SHOW_EXTRIO_PLUGDELAY_MS);
        } else {
            showExtrioPlugin();
        }
    }


    private void  showExtrioPlugin() {
        mCamera = mIDeviceController.getCamera();
        CameraExt mCameraExt = new CameraExt();
        CameraInterfaceManage.getInstance().setCameraExt(mCameraExt);
        mIApp.getmPluginManagerAgent().showPlugin(scenecModeId);
    }


    private class  CameraExt implements  ICameraExt{

        @Override
        public void setSwipingEnabled(boolean b) {
        }

        @Override
        public void runOnUiThread(Runnable runnable) {
        }

        @Override
        public int getOrientationCompensation() {
            return 0;
        }

        @Override
        public Object getGLRoot() {
            return getGLRoot();
        }

        @Override
        public Context getHostContext() {
            return mIApp.getActivity();
        }

        @Override
        public Camera getApi1Camera() {
            if (mCamera != null) {
                return mCamera;
            }
            return null;
        }



        @Override
        public com.freeme.camera.data.Size getPictureSize() {
            if (mCamera == null) {
                return null;
            }
            if (scenecModeId == FreemeSceneModeData.FREEME_SCENE_MODE_WATERMARK_ID){
                setLocalPictureSize(FreemeSceneModeData.FREEME_SCENE_MODE_WATERMARK_ID,mCamera.getParameters().getSupportedPictureSizes());
            }

            if (mLocalPictureSize != null) {
                return new com.freeme.camera.data.Size(mLocalPictureSize.width, mLocalPictureSize.height);
            }

            Camera.Size size = mCamera.getParameters().getPictureSize();
            return new com.freeme.camera.data.Size(size.width, size.height);
        }

        @Override
        public int getOrientation() {
            return mIApp.getGSensorOrientation();
        }


        @Override
        public void setTopBarVisible(int visible) {
        }

        @Override
        public void setBottomBarVisible(int visible) {
        }

        @Override
        public void onShutter() {
            onShutterButtonClick();
        }

        @Override
        public void setTopBarBackgroudcolor(int color) {

        }

        @Override
        public void sendCommand(int cmd) {


        }

        public AlertDialog.Builder getAlertDialog() {
            return new AlertDialog.Builder(mIApp.getActivity(),com.freeme.internal.R.style.Theme_Freeme_Light_Dialog_Alert);
        }

        private void setLocalPictureSize(int mode, List<Camera.Size> sizes) {
            PictureSizeInfo picsize = mIApp.getmPluginManagerAgent().getPictureSizeInfo(mode);

            if (sizes == null || sizes.size() <= 0 || picsize == null) {
                return;
            }
            double ratio = picsize.getExpectScreenRatio();

            Camera.Size find = null;
            long nearest = Long.MAX_VALUE;
            long minAbs;
            if (sizes != null) {
                for (Camera.Size size : sizes) {
                    double candidate = size.width / (double) size.height;
                    if (Math.abs(ratio - candidate) < 0.01) {
                        long value = size.width * size.height;
                        minAbs = Math.abs(picsize.getExpecttOutImageSize() - value);
                        if (minAbs < nearest) {
                            find = size;
                            nearest = minAbs;
                        }
                    }
                }
            }

            if (find != null) {
                LogHelper.e(TAG, "setCameraPictureSize(): find step 1: find.width = " + find.width
                        + ", find.height = " + find.height + ",expect size:" + picsize.toString());
            } else {
                // get max supported size but not over 6M
                for (int i = sizes.size() - 1; i >= 0; i--) {
                    Camera.Size size = sizes.get(i);
                    if (size.width * size.height <= picsize.getExpecttOutImageSize()) {
                        find = size;
                        LogHelper.i(TAG, "setCameraPictureSize(): find step 2: find.width = " + find.width
                                + ", find.height = " + find.height);
                        break;
                    }
                }
            }

            mLocalPictureSize = find;

        }

    }

    @Override
    public void pause(@Nullable DeviceUsage nextModeDeviceUsage) {
        /*if (mRootView != null)
            mRootView.setVisibility(View.GONE);
        mLargeView.setVisibility(View.GONE);*/

        mIApp.getmPluginManagerAgent().hidePlugin(scenecModeId);
        if (scenecModeId == FreemeSceneModeData.FREEME_SCENE_MODE_BM_ID || scenecModeId == FreemeSceneModeData.FREEME_SCENE_MODE_QRCODE_ID) {
            mIApp.getAppUi().applyAllUIVisibilityImmediately(View.VISIBLE);
        }

        if (scenecModeId == FreemeSceneModeData.FREEME_SCENE_MODE_QRCODE_ID) {
            mHander.removeMessages(MSG_SHOW_EXTRIO_PLUG);
        }

        LogHelper.i(TAG, "[pause]+");
        super.pause(nextModeDeviceUsage);
        mIsResumed = false;
        mMemoryManager.removeListener(this);
        //clear the surface listener
        mIApp.getAppUi().clearPreviewStatusListener(mISurfaceStatusListener);

        if (mNeedCloseCameraIds.size() > 0) {
            prePareAndCloseCamera(needCloseCameraSync(), mCameraId);
            recycleSettingManager(mCameraId);
        } else {
            //clear the all callbacks.
            clearAllCallbacks(mCameraId);
            //do stop preview
            mIDeviceController.stopPreview();
        }
        LogHelper.i(TAG, "[pause]-");
    }

    @Override
    public void unInit() {
        super.unInit();

        destroyAnimationHandler();
        mIDeviceController.destroyDeviceController();
    }

    @Override
    public boolean onCameraSelected(@Nonnull String newCameraId) {
        LogHelper.i(TAG, "[onCameraSelected] ,new id:" + newCameraId + ",current id:" + mCameraId);
        //first need check whether can switch camera or not.
        if (canSelectCamera(newCameraId)) {
            //trigger switch camera animation in here
            //must before mCamera = newCameraId, otherwise the animation's orientation and
            // whether need mirror is error.
            synchronized (mPreviewDataSync) {
                startSwitchCameraAnimation();
            }
            doCameraSelect(mCameraId, newCameraId);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onShutterButtonFocus(boolean pressed) {
        return true;
    }

    @Override
    public boolean onShutterButtonClick() {
        //Storage case
        boolean storageReady = mICameraContext.getStorageService().getCaptureStorageSpace() > 0;
        boolean isDeviceReady = mIDeviceController.isReadyForCapture();
        LogHelper.i(TAG, "onShutterButtonClick, is storage ready : " + storageReady + "," +
                "isDeviceReady = " + isDeviceReady);

        if (storageReady && isDeviceReady && mIsResumed
                && mMemoryState != IMemoryManager.MemoryAction.STOP) {
            //trigger capture animation
            startCaptureAnimation();
            mPhotoStatusResponder.statusChanged(KEY_PHOTO_CAPTURE, PHOTO_CAPTURE_START);
            updateModeDeviceState(MODE_DEVICE_STATE_CAPTURING);
            mIApp.getAppUi().applyAllUIEnabledImmediately(false);
            mIDeviceController.updateGSensorOrientation(mIApp.getGSensorOrientation());
            mIDeviceController.takePicture(this);
        }
        return true;
    }

    @Override
    public boolean onShutterButtonLongPressed() {
        return false;
    }

    @Override
    public void onDataReceived(IExtrioPlugDeviceController.DataCallbackInfo dataCallbackInfo) {
        //when mode receive the data, need save it.
        byte[] jpegData = dataCallbackInfo.data;
        byte[] blendData = null;
        boolean needUpdateThumbnail = dataCallbackInfo.needUpdateThumbnail;
        boolean needRestartPreview = dataCallbackInfo.needRestartPreview;
        LogHelper.d(TAG, "onDataReceived, data = " + jpegData + ",mIsResumed = " + mIsResumed +
                ",needUpdateThumbnail = " + needUpdateThumbnail + ",needRestartPreview = " +
                needRestartPreview);
        if (jpegData != null) {
            CameraSysTrace.onEventSystrace(JPEG_CALLBACK, true);
        }
        //save file first,because save file is in other thread, so will improve the shot to shot
        //performance.
        if (jpegData != null) {
            if (scenecModeId == FreemeSceneModeData.FREEME_SCENE_MODE_WATERMARK_ID) {
                blendData = mIApp.getmPluginManagerAgent().blendOutput(jpegData, FreemeSceneModeData.FREEME_SCENE_MODE_WATERMARK_ID);
                saveData(blendData);
            } else {
                saveData(jpegData);
            }


        }
        //if camera is paused, don't need do start preview and other device related actions.
        if (mIsResumed) {
            //first do start preview in API1.
            if (mCameraApi == CameraApi.API1) {
                if (needRestartPreview && !mIsMatrixDisplayShow) {
                    mIDeviceController.startPreview();
                }
            }
        }
        //update thumbnail
        if (jpegData != null && needUpdateThumbnail) {
            updateThumbnail(jpegData);
        }
        if (jpegData != null) {
            CameraSysTrace.onEventSystrace(JPEG_CALLBACK, false);
        }
    }

    @Override
    public void onPostViewCallback(byte[] data) {
        LogHelper.d(TAG, "[onPostViewCallback] data = " + data + ",mIsResumed = " + mIsResumed);
        CameraSysTrace.onEventSystrace(POST_VIEW_CALLBACK, true);
        if (data != null && mIsResumed) {
            //will update the thumbnail
            int rotation = CameraUtil.getJpegRotationFromDeviceSpec(Integer.parseInt(mCameraId),
                    mIApp.getGSensorOrientation(), mIApp.getActivity());
            Bitmap bitmap = BitmapCreator.createBitmapFromYuv(data, POST_VIEW_FORMAT,
                    mPreviewWidth, mPreviewHeight, mIApp.getAppUi().getThumbnailViewWidth(),
                    rotation);
            mIApp.getAppUi().updateThumbnail(bitmap);
        }
        CameraSysTrace.onEventSystrace(POST_VIEW_CALLBACK, false);
    }

    @Override
    protected ISettingManager getSettingManager() {
        return mISettingManager;
    }

    @Override
    public void onCameraOpened(String cameraId) {
        updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
    }

    @Override
    public void beforeCloseCamera() {
        updateModeDeviceState(MODE_DEVICE_STATE_CLOSED);
    }

    @Override
    public void afterStopPreview() {
        updateModeDeviceState(MODE_DEVICE_STATE_OPENED);
    }

    @Override
    public void onPreviewCallback(byte[] data, int format) {
        // Because we want use the one preview frame for doing switch camera animation
        // so will dismiss the later frames.
        // The switch camera data will be restore to null when camera close done.
        if (!mIsResumed) {
            return;
        }
        synchronized (mPreviewDataSync) {
            //Notify preview started.
            if (!mIsMatrixDisplayShow) {
                mIApp.getAppUi().applyAllUIEnabled(true);
            }
            mIApp.getAppUi().onPreviewStarted(mCameraId);
            if (mPreviewData == null) {
                stopAllAnimations();
            }
            updateModeDeviceState(MODE_DEVICE_STATE_PREVIEWING);

            mPreviewData = data;
            mPreviewFormat = format;
        }
    }


    @Override
    public void onPreviewSizeReady(Size previewSize) {
        LogHelper.d(TAG, "[onPreviewSizeReady] previewSize: " + previewSize.toString());
        updatePictureSizeAndPreviewSize(previewSize);
    }

    private void onPreviewSizeChanged(int width, int height) {
        //Need reset the preview data to null if the preview size is changed.
        synchronized (mPreviewDataSync) {
            mPreviewData = null;
        }
        mPreviewWidth = width;
        mPreviewHeight = height;
        mIApp.getAppUi().setPreviewSize(mPreviewWidth, mPreviewHeight, mISurfaceStatusListener);
    }

    private void prepareAndOpenCamera(boolean needOpenCameraSync, String cameraId,
                                      boolean needFastStartPreview) {
        mCameraId = cameraId;
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(mCameraId);
        statusMonitor.registerValueChangedListener(KEY_PICTURE_SIZE, mStatusChangeListener);
        statusMonitor.registerValueChangedListener(KEY_MATRIX_DISPLAY_SHOW, mStatusChangeListener);

        //before open camera, prepare the device callback and size changed callback.
        mIDeviceController.setDeviceCallback(this);
        mIDeviceController.setPreviewSizeReadyCallback(this);
        //prepare device info.
        DeviceInfo info = new DeviceInfo();
        info.setCameraId(mCameraId);
        info.setSettingManager(mISettingManager);
        mIDeviceController.openCamera(info);
    }

    private void prePareAndCloseCamera(boolean needSync, String cameraId) {
        clearAllCallbacks(cameraId);
        mIDeviceController.closeCamera(needSync);
        mIsMatrixDisplayShow = false;
        //reset the preview size and preview data.
        mPreviewData = null;
        mPreviewWidth = 0;
        mPreviewHeight = 0;
    }

    private void clearAllCallbacks(String cameraId) {
        mIDeviceController.setPreviewSizeReadyCallback(null);
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(cameraId);
        statusMonitor.unregisterValueChangedListener(KEY_PICTURE_SIZE, mStatusChangeListener);
        statusMonitor.unregisterValueChangedListener(
                KEY_MATRIX_DISPLAY_SHOW, mStatusChangeListener);
    }

    private void initSettingManager(String cameraId) {
        SettingManagerFactory smf = mICameraContext.getSettingManagerFactory();
        mISettingManager = smf.getInstance(
                cameraId,
                getModeKey(),
                ModeType.PHOTO,
                mCameraApi);

        mIApp.getAppUi().applyAllUIVisibilityImmediately(View.INVISIBLE);

        if(scenecModeId == FreemeSceneModeData.FREEME_SCENE_MODE_BM_ID) {
            mIApp.getAppUi().setUIVisibilityImmediately(IAppUi.MODE_SWITCHER, View.VISIBLE);
        } else if (scenecModeId == FreemeSceneModeData.FREEME_SCENE_MODE_QRCODE_ID ) {
        } else if (scenecModeId == FreemeSceneModeData.FREEME_SCENE_MODE_CHILD_ID ) {
            mIApp.getAppUi().applyAllUIVisibilityImmediately(View.VISIBLE);
        } else {

            mIApp.getAppUi().setUIVisibilityImmediately(IAppUi.SHUTTER_BUTTON, View.VISIBLE);
            mIApp.getAppUi().setUIVisibilityImmediately(IAppUi.MODE_SWITCHER, View.VISIBLE);
            mIApp.getAppUi().setUIVisibilityImmediately(IAppUi.THUMBNAIL, View.VISIBLE);
            mIApp.getAppUi().setUIVisibilityImmediately(IAppUi.QUICK_SWITCHER, View.VISIBLE);
        }



    }

    private void recycleSettingManager(String cameraId) {
        mICameraContext.getSettingManagerFactory().recycle(cameraId);
    }

    private void createAnimationHandler() {
        mAnimationHandlerThread = new HandlerThread("Animation_handler");
        mAnimationHandlerThread.start();
        mAnimationHandler = new Handler(mAnimationHandlerThread.getLooper());
    }

    private void destroyAnimationHandler() {
        if (mAnimationHandlerThread.isAlive()) {
            mAnimationHandlerThread.quit();
            mAnimationHandler = null;
        }
    }

    private boolean canSelectCamera(@Nonnull String newCameraId) {
        boolean value = true;

        if (newCameraId == null || mCameraId.equalsIgnoreCase(newCameraId)) {
            value = false;
        }
        LogHelper.d(TAG, "[canSelectCamera] +: " + value);
        return value;
    }

    private void doCameraSelect(String oldCamera, String newCamera) {
        mIApp.getAppUi().applyAllUIEnabledImmediately(false);
        mIApp.getAppUi().onCameraSelected(newCamera);
        prePareAndCloseCamera(true, oldCamera);
        recycleSettingManager(oldCamera);
        initSettingManager(newCamera);
        prepareAndOpenCamera(false, newCamera, false);
    }

    private MediaSaverListener mMediaSaverListener = new MediaSaverListener() {

        @Override
        public void onFileSaved(Uri uri) {
            mIApp.notifyNewMedia(uri, true);
            if (mIApp.getmPluginManagerAgent() != null && scenecModeId == FreemeSceneModeData.FREEME_SCENE_MODE_BM_ID) {
                mIApp.getmPluginManagerAgent().mediaSaved(uri, scenecModeId);
            }
            synchronized (mCaptureNumberSync) {
                mCapturingNumber--;
                if (mCapturingNumber == 0) {
                    mMemoryState = IMemoryManager.MemoryAction.NORMAL;
                    mIApp.getAppUi().hideSavingDialog();
                    if (scenecModeId != FreemeSceneModeData.FREEME_SCENE_MODE_BM_ID) {
                        mIApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
                    }
                }
            }
            LogHelper.d(TAG, "[onFileSaved] uri = " + uri + ", mCapturingNumber = "
                    + mCapturingNumber);
        }
    };

    private void stopAllAnimations() {
        LogHelper.d(TAG, "[stopAllAnimations]");
        if (mAnimationHandler == null) {
            return;
        }
        //clear the old one.
        mAnimationHandler.removeCallbacksAndMessages(null);
        mAnimationHandler.post(new Runnable() {
            @Override
            public void run() {
                LogHelper.d(TAG, "[stopAllAnimations] run");
                //means preview is started, so need notify switch camera animation need stop.
                stopSwitchCameraAnimation();
                //need notify change mode animation need stop if is doing change mode.
                stopChangeModeAnimation();
                //stop the capture animation if is doing capturing.
                stopCaptureAnimation();
            }
        });
    }

    private void startSwitchCameraAnimation() {
        // Prepare the animation data.
        AnimationData data = prepareAnimationData(mPreviewData, mPreviewWidth,
                mPreviewHeight, mPreviewFormat);
        // Trigger animation start.
        mIApp.getAppUi().animationStart(IAppUi.AnimationType.TYPE_SWITCH_CAMERA, data);
    }

    private void stopSwitchCameraAnimation() {
        mIApp.getAppUi().animationEnd(IAppUi.AnimationType.TYPE_SWITCH_CAMERA);
    }

    private void startChangeModeAnimation() {
        AnimationData data = prepareAnimationData(mPreviewData, mPreviewWidth,
                mPreviewHeight, mPreviewFormat);
        mIApp.getAppUi().animationStart(IAppUi.AnimationType.TYPE_SWITCH_MODE, data);
    }

    private void stopChangeModeAnimation() {
        mIApp.getAppUi().animationEnd(IAppUi.AnimationType.TYPE_SWITCH_MODE);
    }

    private void startCaptureAnimation() {
        mIApp.getAppUi().animationStart(IAppUi.AnimationType.TYPE_CAPTURE, null);
    }

    private void stopCaptureAnimation() {
        mIApp.getAppUi().animationEnd(IAppUi.AnimationType.TYPE_CAPTURE);
    }

    private AnimationData prepareAnimationData(byte[] data, int width, int height, int format) {
        // Prepare the animation data.
        AnimationData animationData = new AnimationData();
        animationData.mData = data;
        animationData.mWidth = width;
        animationData.mHeight = height;
        animationData.mFormat = format;
        animationData.mOrientation = mPhotoModeHelper.getCameraInfoOrientation(mCameraId);
        animationData.mIsMirror = mPhotoModeHelper.isMirror(mCameraId);
        return animationData;
    }

    private void updatePictureSizeAndPreviewSize(Size previewSize) {
        ISettingManager.SettingController controller = mISettingManager.getSettingController();


        controller.querySupportedPlatformValues(KEY_PICTURE_SIZE);
        String size = controller.queryValue(KEY_PICTURE_SIZE);
        if (size != null && mIsResumed) {
            String[] pictureSizes = size.split("x");
            mCaptureWidth = Integer.parseInt(pictureSizes[0]);
            mCaptureHeight = Integer.parseInt(pictureSizes[1]);
            mIDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
            int width = previewSize.getWidth();
            int height = previewSize.getHeight();
            if (width != mPreviewWidth || height != mPreviewHeight) {
                onPreviewSizeChanged(width, height);
            }
        }

    }

    private void initStatusMonitor() {
        StatusMonitor statusMonitor = mICameraContext.getStatusMonitor(mCameraId);
        mPhotoStatusResponder = statusMonitor.getStatusResponder(KEY_PHOTO_CAPTURE);
    }


    private void saveData(byte[] jpegData) {
        if (jpegData != null) {
            //check memory to decide whether it can take next picture.
            //if not, show saving
            ISettingManager.SettingController controller = mISettingManager.getSettingController();
            String dngState = controller.queryValue(KEY_DNG);
            long saveDataSize = jpegData.length;
            if (dngState != null && "on".equalsIgnoreCase(dngState)) {
                saveDataSize = saveDataSize + DNG_IMAGE_SIZE;
            }
            synchronized (mCaptureNumberSync) {
                mCapturingNumber ++;
                mMemoryManager.checkOneShotMemoryAction(saveDataSize);
            }
            String fileDirectory = mICameraContext.getStorageService().getFileDirectory();
            Size exifSize = CameraUtil.getSizeFromExif(jpegData);
            ContentValues contentValues = mPhotoModeHelper.createContentValues(jpegData,
                    fileDirectory, exifSize.getWidth(), exifSize.getHeight());
            mICameraContext.getMediaSaver().addSaveRequest(jpegData, contentValues, null,
                    mMediaSaverListener);
            //reset the switch camera to null
            synchronized (mPreviewDataSync) {
                mPreviewData = null;
            }
        }
    }

    private void updateThumbnail(byte[] jpegData) {
        Bitmap bitmap = BitmapCreator.createBitmapFromJpeg(jpegData, mIApp.getAppUi()
                .getThumbnailViewWidth());
        mIApp.getAppUi().updateThumbnail(bitmap);
    }

    @Override
    public void onMemoryStateChanged(IMemoryManager.MemoryAction state) {
        if (state == IMemoryManager.MemoryAction.STOP && mCapturingNumber != 0) {
            //show saving
            LogHelper.d(TAG, "memory low, show saving");
            mIApp.getAppUi().showSavingDialog(null, true);
            mIApp.getAppUi().applyAllUIVisibility(View.INVISIBLE);
        }
    }

    /**
     * surface changed listener.
     */
    private class SurfaceChangeListener implements ISurfaceStatusListener {

        @Override
        public void surfaceAvailable(Object surfaceObject, int width, int height) {
            LogHelper.d(TAG, "surfaceAvailable,device controller = " + mIDeviceController
                    + ",w = " + width + ",h = " + height);
            if (mIDeviceController != null && mIsResumed) {
                mIDeviceController.updatePreviewSurface(surfaceObject);
            }
        }

        @Override
        public void surfaceChanged(Object surfaceObject, int width, int height) {
            LogHelper.d(TAG, "surfaceChanged, device controller = " + mIDeviceController
                    + ",w = " + width + ",h = " + height);
            if (mIDeviceController != null && mIsResumed) {
                mIDeviceController.updatePreviewSurface(surfaceObject);
            }
        }

        @Override
        public void surfaceDestroyed(Object surfaceObject, int width, int height) {
            LogHelper.d(TAG, "surfaceDestroyed,device controller = " + mIDeviceController);
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    }

    /**
     * Status change listener implement.
     */
    private class MyStatusChangeListener implements StatusChangeListener {
        @Override
        public void onStatusChanged(String key, String value) {
            LogHelper.d(TAG, "[onStatusChanged] key = " + key + ",value = " + value);
            if (KEY_PICTURE_SIZE.equalsIgnoreCase(key)) {
                String[] sizes = value.split("x");
                mCaptureWidth = Integer.parseInt(sizes[0]);
                mCaptureHeight = Integer.parseInt(sizes[1]);
                mIDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
                Size previewSize = mIDeviceController.getPreviewSize((double) mCaptureWidth /
                        mCaptureHeight);
                int width = previewSize.getWidth();
                int height = previewSize.getHeight();
                if (width != mPreviewWidth || height != mPreviewHeight) {
                    onPreviewSizeChanged(width, height);
                }
            } else if (KEY_MATRIX_DISPLAY_SHOW.equals(key)) {
                mIsMatrixDisplayShow = "true".equals(value);
            }
        }
    }
    @Override
    public  void onOrientationChanged(int orientation){
        if (scenecModeId == FreemeSceneModeData.FREEME_SCENE_MODE_BM_ID || scenecModeId == FreemeSceneModeData.FREEME_SCENE_MODE_WATERMARK_ID) {
            mIApp.getmPluginManagerAgent().notifyOrientationChanged(scenecModeId, orientation);
        }
    }


}