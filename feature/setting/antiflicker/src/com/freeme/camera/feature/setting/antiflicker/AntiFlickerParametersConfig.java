package com.freeme.camera.feature.setting.antiflicker;

import android.hardware.Camera;

import com.freeme.camera.common.debug.LogHelper;
import com.freeme.camera.common.debug.LogUtil;
import com.freeme.camera.common.device.v1.CameraProxy;
import com.freeme.camera.common.setting.ICameraSetting;
import com.freeme.camera.common.setting.ISettingManager;

import java.util.List;

/**
 * Anti flicker parameters configure.
 */

public class AntiFlickerParametersConfig implements ICameraSetting.IParametersConfigure {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(AntiFlickerParametersConfig.class.getSimpleName());
    private static final String DEFAULT_VALUE = "auto";

    private AntiFlicker mAntiFlicker;
    private ISettingManager.SettingDeviceRequester mDeviceRequester;

    /**
     * Anti flicker parameters configure.
     *
     * @param antiFlicker The instance of {@link AntiFlicker}.
     * @param deviceRequester The instance of {@link ISettingManager.SettingDeviceRequester}.
     */
    public AntiFlickerParametersConfig(AntiFlicker antiFlicker,
                                       ISettingManager.SettingDeviceRequester deviceRequester) {
        mAntiFlicker = antiFlicker;
        mDeviceRequester = deviceRequester;
    }

    @Override
    public void setOriginalParameters(Camera.Parameters originalParameters) {
        List<String> supportedValues = originalParameters.getSupportedAntibanding();
        mAntiFlicker.initializeValue(supportedValues, DEFAULT_VALUE);
    }

    @Override
    public boolean configParameters(Camera.Parameters parameters) {
        String value = mAntiFlicker.getValue();
        LogHelper.d(TAG, "[configParameters], value:" + value);
        if (value != null) {
            parameters.setAntibanding(value);
        }
        return false;
    }

    @Override
    public void configCommand(CameraProxy cameraProxy) {

    }

    @Override
    public void sendSettingChangeRequest() {
        mDeviceRequester.requestChangeSettingValue(mAntiFlicker.getKey());
    }
}
