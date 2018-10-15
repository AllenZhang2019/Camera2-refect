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

package com.freeme.camera.feature.setting.volumecapture;

import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;

import com.freeme.camera.common.ICameraContext;
import com.freeme.camera.common.app.IApp;
import com.freeme.camera.common.debug.LogHelper;
import com.freeme.camera.common.debug.LogUtil;
import com.freeme.camera.common.setting.ISettingManager.SettingController;
import com.freeme.camera.common.setting.SettingBase;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * ZSD setting.
 */

public class VolumeCapture extends SettingBase implements VolumeCaptureSettingView.OnVolumeCaptureClickListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(VolumeCapture.class.getSimpleName());


    private VolumeCaptureSettingView mSettingView;
    private boolean mIsVolumeCaptureSupported = false;
    private boolean mIsThirdParty = false;
    private List<String> mSupportValues = new ArrayList<String>();

    @Override
    public void init(IApp app, ICameraContext cameraContext, SettingController settingController) {
        super.init(app, cameraContext, settingController);
        Intent intent = mActivity.getIntent();
        String action = intent.getAction();
        if (MediaStore.ACTION_IMAGE_CAPTURE.equals(action)
                || MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
            mIsThirdParty = true;
        }
        initSettingValue();
    }

    @Override
    public void unInit() {

    }

    @Override
    public void overrideValues(@Nonnull String headerKey, String currentValue,
                               List<String> supportValues) {
        if (!mIsVolumeCaptureSupported) {
            return;
        }
        super.overrideValues(headerKey, currentValue, supportValues);
    }


    @Override
    public void addViewEntry() {
        if (!mIsVolumeCaptureSupported) {
            return;
        }
        if (mSettingView == null) {
            mSettingView = new VolumeCaptureSettingView(getKey());
            mSettingView.setVolumeCaptureOnClickListener(this);
        }
        mAppUi.addSettingView(mSettingView);
    }

    @Override
    public void removeViewEntry() {
        mAppUi.removeSettingView(mSettingView);
    }

    @Override
    public void refreshViewEntry() {
        if (mSettingView != null) {
            String value = mDataStore.getValue(getKey(), DEFAULT_VOLUME_CAPTURE, getStoreScope());
            mSettingView.setChecked(VALUE_ON.equals(value));
            mSettingView.setEnabled(getEntryValues().size() > 1);
            //setValue(value);
        }
    }

    @Override
    public void postRestrictionAfterInitialized() {

    }

    @Override
    public SettingType getSettingType() {
        return SettingType.PHOTO_AND_VIDEO;
    }

    @Override
    public String getKey() {
        return KEY_VOLUME_CAPTURE;
    }

    @Override
    public IParametersConfigure getParametersConfigure() {
        return null;
    }

    @Override
    public ICaptureRequestConfigure getCaptureRequestConfigure() {
        return null;
    }

    @Override
    public void onVolumeCaptureClicked(boolean checked) {
        String value = checked ? VALUE_ON : VALUE_OFF;
        LogHelper.d(TAG, "[onZsdClicked], value:" + value);
        setValue(value);
        mDataStore.setValue(getKey(), value, getStoreScope(), false);
    }

    private void initSettingValue(){
        mIsVolumeCaptureSupported = true;
        mSupportValues.add(VALUE_ON);
        mSupportValues.add(VALUE_OFF);
        setSupportedPlatformValues(mSupportValues);
        setSupportedEntryValues(mSupportValues);
        setEntryValues(mSupportValues);
        String value = mDataStore.getValue(getKey(), DEFAULT_VOLUME_CAPTURE , getStoreScope());
        setValue(value);
    }
}
