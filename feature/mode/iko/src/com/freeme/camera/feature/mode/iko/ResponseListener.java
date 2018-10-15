package com.freeme.camera.feature.mode.iko;

/**
 * Created by azmohan on 18-3-13.
 */

public interface ResponseListener {
    public void requestSuccess(Object result);

    public void requestFailure(int errorCode, String failureStr);
}
