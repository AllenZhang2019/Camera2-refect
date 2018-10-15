/*
 *   Copyright Statement:
 *
 *     This software/firmware and related documentation ("MediaTek Software") are
 *     protected under relevant copyright laws. The information contained herein is
 *     confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *     the prior written permission of MediaTek inc. and/or its licensors, any
 *     reproduction, modification, use or disclosure of MediaTek Software, and
 *     information contained herein, in whole or in part, shall be strictly
 *     prohibited.
 *
 *     MediaTek Inc. (C) 2016. All rights reserved.
 *
 *     BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *    THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *     RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *     ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *     WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *     WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *     NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *     RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *     INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *     TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *     RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *     OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *     SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *     RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *     STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *     ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *     RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *     MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *     CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *     The following software/firmware and/or related documentation ("MediaTek
 *     Software") have been modified by MediaTek Inc. All revisions are subject to
 *     any receiver's applicable license agreements with MediaTek Inc.
 */

package com.freeme.camera.feature.setting.shuttersound;

import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Log;

import com.freeme.camera.common.debug.LogUtil;
import com.freeme.camera.common.device.v1.CameraProxy;
import com.freeme.camera.common.setting.ICameraSetting;
import com.freeme.camera.common.setting.ISettingManager;
import com.freeme.camera.feature.setting.facedetection.IFaceConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * Zsd parameters configure in camera API1.
 */
public class ShutterSoundParametersConfig implements ICameraSetting.IParametersConfigure {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(
            ShutterSoundParametersConfig.class.getSimpleName());


    private static final String VALUE_ON = "on";
    private static final String VALUE_OFF = "off";
    private ISettingManager.SettingDeviceRequester mDeviceRequester;
    private ShutterSound mShutterSound;
    private String mValue;

    /**
     * Zsd parameters configure constructor.
     *
     * @param shutterSound The instance of {@link ShutterSound}.
     * @param deviceRequester The instance of {@link ISettingManager.SettingDeviceRequester}.
     */
    public ShutterSoundParametersConfig(ShutterSound shutterSound,
                                         ISettingManager.SettingDeviceRequester deviceRequester) {
        mShutterSound = shutterSound;
        mDeviceRequester = deviceRequester;
    }

    @Override
    public void setOriginalParameters(Camera.Parameters originalParameters) {
        ArrayList<String> subStrings = new ArrayList<>();
        subStrings.add(VALUE_ON);
        subStrings.add(VALUE_OFF);
        String defaultValue = VALUE_ON;
        mShutterSound.initializeValue(subStrings, defaultValue);
    }

    @Override
    public boolean configParameters(Camera.Parameters parameters) {
        if (mShutterSound.getValue() == null) {
            return false;
        }
        boolean changed = !(mShutterSound.getValue().equals(mValue));
        mValue = mShutterSound.getValue();
        return changed;
    }

    @Override
    public void configCommand(CameraProxy cameraProxy) {
        if (mShutterSound.getShutterSoundState()) {
            cameraProxy.enableShutterSound(true);
        } else {
            cameraProxy.enableShutterSound(false);
        }

    }

    @Override
    public void sendSettingChangeRequest() {
        mDeviceRequester.requestChangeSettingValue(mShutterSound.getKey());

        mDeviceRequester.requestChangeCommand(mShutterSound.getKey());
    }

}
