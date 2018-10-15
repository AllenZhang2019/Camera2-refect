package com.freeme.camera.common.pluginmanager;

import android.app.Activity;

public interface ILocalPluginEntry {

    public void init(Activity activity);

    public boolean isCanCapture();

    public void onOrientationChanged(int orientation);

    public void onCameraDeviceReady();

    public void onCancelClick();

    public void onDoneClick();

    public void onStateChange(int state);

}
