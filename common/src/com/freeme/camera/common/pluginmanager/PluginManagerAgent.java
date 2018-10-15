package com.freeme.camera.common.pluginmanager;

import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.freeme.camera.IPluginModuleEntry;
import com.freeme.camera.common.ICameraContext;
import com.freeme.camera.common.device.CameraDeviceManagerFactory;
import com.freeme.camera.common.mode.ICameraMode;
import com.freeme.camera.common.setting.ISettingManager;
import com.freeme.camera.common.setting.SettingManagerFactory;
import com.freeme.camera.common.utils.Size;
import com.freeme.camera.data.PictureSizeInfo;
import com.freeme.pluginmanager.BasePlugin;
import com.freeme.pluginmanager.Plugin;
import com.freeme.pluginmanager.PluginListener;
import com.freeme.pluginmanager.PluginManager;
import com.freeme.pluginmanager.PluginUtil;
import com.freeme.camera.common.app.IApp;

import java.util.List;

public class PluginManagerAgent implements PluginListener {
    private static final String TAG = "PluginManagerAgent";

    private PluginManager mPluginManager;
    private ViewGroup mSceneModeCtrlLayout;
    private SparseArray<IPluginModuleEntry> mModules = new SparseArray<IPluginModuleEntry>();

    private static final String KEY_PICTURE_SIZE = "key_picture_size";
    private ISettingManager mISettingManager;
    protected CameraDeviceManagerFactory.CameraApi mCameraApi;
    private IApp mApp;

    public PluginManagerAgent(IApp app) {
        mApp = app;
        mPluginManager = new PluginManager(app.getActivity().getBaseContext(), PluginManagerAgent.this);
        //初始化的时候会去scanPlugins()，并且注册了应用安装卸载广播以便后续更新插件列表;并且注册了扫描插件结束后的回调this.onRefreshPlugins
        //Activity.getBaseContext() 获取的是个什么玩意?
    }


    @Override
    public void onRefreshPlugins(List<Plugin> list, List<Plugin> list1, List<Plugin> list2) {
        synchronized (mModules) {
            mModules.clear();
            if (list != null) {
                for (Plugin plugin : list) {//外部插件
                    if (PluginUtil.CAMERA_MODULE_PLUGIN.equals(plugin.getType())) {
                        IPluginModuleEntry module = (IPluginModuleEntry) plugin.getInstance();
                        mModules.put(module.getModuleID(), module);

                    }
                }
            }

        }
    }


    public SparseArray<IPluginModuleEntry> getModules() {
        return mModules;
    }

    public void showPlugin(int mode){
        if (mSceneModeCtrlLayout == null) {
            mSceneModeCtrlLayout = (ViewGroup) mApp.getAppUi().getModeRootView();
        }

        if(mModules.get(mode) != null){
            mModules.get(mode).showPanel(mSceneModeCtrlLayout);

        }
    }

    public void hidePlugin(int mode){
        if(mModules.get(mode) != null){
            mModules.get(mode).hidePanel();
        }
    }

    public void notifyOrientationChanged(int mode , int orientation){
        if(mModules.get(mode) != null){
            ((BasePlugin)mModules.get(mode)).notifyOrientationChanged(orientation);
        }
    }

    public void mediaSaved(Uri uri, int mode) {
        if (mModules != null && mModules.size() > 0) {
            IPluginModuleEntry plugin = mModules.get(mode, null);
            if (plugin != null) {
                plugin.mediaSaved(uri);
            }
        }
    }

    public PictureSizeInfo  getPictureSizeInfo(int mode) {
        IPluginModuleEntry plugin = mModules.get(mode, null);
        if (plugin != null) {
            return plugin.getPictureSizeInfo();
        } else {
            return null;
        }

    }

    public byte[] blendOutput(byte[] jpegData ,int mode) {
        if (mModules != null && mModules.size() > 0) {
            IPluginModuleEntry plugin = mModules.get(mode, null);
            if (plugin != null) {
                return plugin.blendOutput(jpegData);
            }
        }
        return null;

    }

}