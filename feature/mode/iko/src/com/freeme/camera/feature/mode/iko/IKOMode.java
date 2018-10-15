package com.freeme.camera.feature.mode.iko;

import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.HandlerThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.freeme.camera.common.IAppUi;
import com.freeme.camera.common.IAppUi.AnimationData;
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
import com.freeme.camera.R;
import com.freeme.camera.common.widget.RotateImageView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Normal photo mode that is used to take normal picture.
 */
public class IKOMode extends CameraModeBase implements IIKODeviceController.JpegCallback,
        IIKODeviceController.DeviceCallback, IIKODeviceController.PreviewSizeCallback, IMemoryManager.IMemoryListener, View.OnClickListener  {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(IKOMode.class.getSimpleName());
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

    protected IIKODeviceController mIDeviceController;
    protected IKOModeHelper mPhotoModeHelper;
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

    private RelativeLayout mIkoFrameLayout;
    private ImageView mIntentReviewImageView;
    private ImageView mHandleView;
    private AnimationDrawable mCaptureanimDrawable;
    private TextView mSearchView;
    private AsyncTask<String, String, ImageResponseInfo> mAsyncTask;
    private byte[] mJpegData;



    private ViewGroup mParentViewGroup;
    private View mRootView;
    private View mLargeView;

    @Override
    public void init(@Nonnull IApp app, @Nonnull ICameraContext cameraContext,
                     boolean isFromLaunch) {
        LogHelper.d(TAG, "[init]+");
        super.init(app, cameraContext, isFromLaunch);
        mPhotoModeHelper = new IKOModeHelper(cameraContext);
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
        mMemoryManager = new MemoryManagerImpl(app.getActivity());
        prepareAndOpenCamera(false, mCameraId, isFromLaunch);
        LogHelper.d(TAG, "[init]- ");


        /*mParentViewGroup = mIApp.getAppUi().getModeRootView();
        View viewLayout = mIApp.getActivity().getLayoutInflater().inflate(R.layout.large_preview,
                mParentViewGroup, true);

        mRootView = viewLayout.findViewById(R.id.large_frame_layout);
        mLargeView = mRootView.findViewById(R.id.large_view);
        if (mRootView != null)
            mRootView.setVisibility(View.VISIBLE);
        mLargeView.setVisibility(View.VISIBLE);*/


        mIkoFrameLayout = (RelativeLayout) mIApp.getActivity().getLayoutInflater().inflate(R.layout.freeme_iko_preview_after_layout,/*mIApp.getAppUi().getModeRootView()*/mIApp.getAppUi().getPreviewFrameLayout(),false);
        RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        lp2.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        lp2.addRule(RelativeLayout.CENTER_HORIZONTAL);
        mIntentReviewImageView = (ImageView) mIkoFrameLayout.findViewById(R.id.intent_review_imageview);
        mHandleView = (ImageView) mIkoFrameLayout.findViewById(R.id.handle_progressing);
        mCaptureanimDrawable = (AnimationDrawable) mHandleView.getBackground();
        mSearchView = (TextView) mIkoFrameLayout.findViewById(R.id.iko_search);
        mSearchView.setOnClickListener(this);
        //mIApp.getAppUi().getModeRootView().addView(mIkoFrameLayout,lp2);
        mIApp.getAppUi().getPreviewFrameLayout().addView(mIkoFrameLayout);

    }


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

        hideIntentReviewImageView();
        mIApp.getAppUi().applyAllUIVisibilityImmediately(View.VISIBLE);
        mIApp.getAppUi().setUIVisibilityImmediately(IAppUi.MODE_SWITCHER, View.VISIBLE);
    }

    @Override
    public void pause(@Nullable DeviceUsage nextModeDeviceUsage) {

        /*if (mRootView != null)
            mRootView.setVisibility(View.GONE);
        mLargeView.setVisibility(View.GONE);*/


        LogHelper.i(TAG, "[pause]+");
        super.pause(nextModeDeviceUsage);
        mIsResumed = false;
        mMemoryManager.removeListener(this);
        //clear the surface listener
        mIApp.getAppUi().clearPreviewStatusListener(mISurfaceStatusListener);
        //camera animation.
        // this animation is no need right now so just mark it
        // if some day need can open it.
//        synchronized (mPreviewDataSync) {
//            if (mNeedCloseCameraIds != null && mPreviewData != null) {
//                startChangeModeAnimation();
//            }
//        }
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
    public void onDataReceived(IIKODeviceController.DataCallbackInfo dataCallbackInfo) {
        //when mode receive the data, need save it.
        byte[] jpegData = dataCallbackInfo.data;
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
            saveData(jpegData);
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

        //mIApp.getAppUi().applyAllUIVisibilityImmediately(View.GONE);
        //mIApp.getAppUi().setUIVisibilityImmediately(IAppUi.MODE_SWITCHER, View.VISIBLE);



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
            synchronized (mCaptureNumberSync) {
                mCapturingNumber--;
                if (mCapturingNumber == 0) {
                    mMemoryState = IMemoryManager.MemoryAction.NORMAL;
                    mIApp.getAppUi().hideSavingDialog();
                    mIApp.getAppUi().applyAllUIVisibility(View.VISIBLE);
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
        String size = controller.queryValue(KEY_PICTURE_SIZE);
        if (size != null && mIsResumed) {
            String[] pictureSizes = size.split("x");
            mCaptureWidth = Integer.parseInt(pictureSizes[0]);
            mCaptureHeight = Integer.parseInt(pictureSizes[1]);
            mIDeviceController.setPictureSize(new Size(mCaptureWidth, mCaptureHeight));
            int width = previewSize.getWidth();
            int height = previewSize.getHeight();
            LogHelper.d(TAG, "[updatePictureSizeAndPreviewSize] picture size: " + mCaptureWidth +
                    " X" + mCaptureHeight + ",current preview size:" + mPreviewWidth + " X " +
                    mPreviewHeight + ",new value :" + width + " X " + height);
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
            mJpegData = jpegData;
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
            //mICameraContext.getMediaSaver().addSaveRequest(jpegData, contentValues, null,
            //        mMediaSaverListener);
            mIDeviceController.stopPreview();
            mIApp.getAppUi().applyAllUIVisibilityImmediately(View.INVISIBLE);
            mIApp.getAppUi().setUIVisibilityImmediately(IAppUi.MODE_SWITCHER, View.INVISIBLE);
            showIntentReviewImageView();
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iko_search) {
            handleSearch();
        }
    }

    private void handleSearch() {
        if (IKOSearchUtil.getNetWorkStatus(mIApp.getActivity())) {
            mHandleView.setVisibility(View.VISIBLE);
            if (!mCaptureanimDrawable.isRunning()) {
                mCaptureanimDrawable.start();
            }
            final String tokenStr = IKOSearchUtil.buildTokenUrlStr();

            mAsyncTask = new AsyncTask<String, String, ImageResponseInfo>() {
                @Override
                protected ImageResponseInfo doInBackground(String[] objects) {
                    String str = objects[0];
                    final ImageResponseInfo imageInfo;
                    try {
                        String result = IKOSearchUtil.accessNetworkByPost(str);//2018.09.26 为什么是空的？
                        if (isCancelled()) {
                            return null;
                        }
                        TokenInfo info = (TokenInfo) ResponseParse.getInstance().parseJsonData(result, TokenInfo.class);
                        String parseStr = HttpUtil.post(IKOSearchUtil.PARSE_URL, info.access_token, IKOSearchUtil.buildParseUrlStr(tokenStr, mJpegData));
                        imageInfo = (ImageResponseInfo) ResponseParse.getInstance().parseJsonData(parseStr, ImageResponseInfo.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                    return imageInfo;
                }

                @Override
                protected void onPostExecute(ImageResponseInfo imageResponseInfo) {
                    super.onPostExecute(imageResponseInfo);
                    if (mCaptureanimDrawable.isRunning()) {
                        mCaptureanimDrawable.stop();
                    }
                    mHandleView.setVisibility(View.GONE);
                    if (imageResponseInfo == null || imageResponseInfo.result == null
                            || TextUtils.isEmpty(imageResponseInfo.result.url)) {
                        Toast.makeText(mIApp.getActivity(), R.string.freeme_iko_error, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Uri uri = Uri.parse(imageResponseInfo.result.url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.setPackage("com.ume.browser");
//                    if (CameraCustomXmlParser.sCameraGenerParamMap.containsKey(CameraCustomXmlParser.IKO_USE_SPECIFIED_BROWSER_PKG)) {
//                        String value = CameraCustomXmlParser.sCameraGenerParamMap.get(CameraCustomXmlParser.IKO_USE_SPECIFIED_BROWSER_PKG);
//                        if (!TextUtils.isEmpty(value)) {
//                            intent.setPackage(value);
//                        }
//                    }

                    try {
                        mIApp.getActivity().startActivity(intent);

                    } catch (SecurityException e) {
                        Toast.makeText(mIApp.getActivity(), R.string.activity_not_found, Toast.LENGTH_SHORT).show();
                        LogHelper.e(TAG, "Launcher does not have the permission to launch " + intent
                                + ". Make sure to create a MAIN intent-filter for the corresponding activity "
                                + "or use the exported attribute for this activity. " + "tag=" + "Browser"
                                + " intent=" + intent, e);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(mIApp.getActivity(), R.string.activity_not_found, Toast.LENGTH_SHORT).show();
                        LogHelper.e(TAG, "Unable to launch. tag=" + "Browser" + " intent=" + intent, e);
                    }
                }
            };
            String[] str = {
                    tokenStr};
            mAsyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, str);
        } else {
            Toast.makeText(mIApp.getActivity(), R.string.freeme_iko_no_network, Toast.LENGTH_SHORT).show();
            if (mCaptureanimDrawable.isRunning()) {
                mCaptureanimDrawable.stop();
            }
            mHandleView.setVisibility(View.GONE);
        }
    }
    //*/

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
    public boolean onBackPressed() {//remove view等UI操作 并且开始重新预览;需要判断模式选择是否打开
        if(mSearchView.getVisibility() == View.VISIBLE){
            hideIntentReviewImageView();
            mIApp.getAppUi().applyAllUIVisibilityImmediately(View.VISIBLE);
            mIApp.getAppUi().setUIVisibilityImmediately(IAppUi.MODE_SWITCHER, View.VISIBLE);
            mIDeviceController.startPreview();

            mHandleView.setVisibility(View.GONE);
            mCaptureanimDrawable.stop();
            return true;
        }
        return false;
    }

    public void showIntentReviewImageView() {
        if (mIntentReviewImageView != null) {
            mIApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSearchView.setVisibility(View.VISIBLE);
                    mIApp.getAppUi().getPreviewFrameLayout().setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public void hideIntentReviewImageView() {
        if (mIntentReviewImageView != null) {
            mIApp.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mSearchView.setVisibility(View.GONE);
                }
            });
        }
    }
}
