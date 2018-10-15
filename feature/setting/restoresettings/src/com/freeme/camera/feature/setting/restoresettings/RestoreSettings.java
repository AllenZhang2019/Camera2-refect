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

package com.freeme.camera.feature.setting.restoresettings;

import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;

import com.freeme.camera.common.ICameraContext;
import com.freeme.camera.common.app.IApp;
import com.freeme.camera.common.debug.LogHelper;
import com.freeme.camera.common.debug.LogUtil;
import com.freeme.camera.common.relation.DataStore;
import com.freeme.camera.common.setting.ISettingManager.SettingController;
import com.freeme.camera.common.setting.SettingBase;
import com.freeme.camera.feature.setting.picturesize.PictureSize;

import java.util.List;

import javax.annotation.Nonnull;

/**
 * ZSD setting.
 */

public class RestoreSettings extends SettingBase implements RestoreSettingsSettingView.OnRestoreSettingsConfirmListener{
    private static final LogUtil.Tag TAG = new LogUtil.Tag(RestoreSettings.class.getSimpleName());


    private RestoreSettingsSettingView mSettingView;
    private boolean mIsRestoreSettingsSupported = true;
    private boolean mIsThirdParty = false;
    private IApp mApp;

    @Override
    public void init(IApp app, ICameraContext cameraContext, SettingController settingController) {
        super.init(app, cameraContext, settingController);
        Intent intent = mActivity.getIntent();
        String action = intent.getAction();
        if (MediaStore.ACTION_IMAGE_CAPTURE.equals(action)
                || MediaStore.ACTION_VIDEO_CAPTURE.equals(action)) {
            mIsThirdParty = true;
        }

        mApp = app;
    }

    @Override
    public void unInit() {

    }

    @Override
    public void overrideValues(@Nonnull String headerKey, String currentValue,
                               List<String> supportValues) {
        if (!mIsRestoreSettingsSupported) {
            return;
        }
        super.overrideValues(headerKey, currentValue, supportValues);
    }


    @Override
    public void addViewEntry() {
        if (!mIsRestoreSettingsSupported) {
            return;
        }
        if (mSettingView == null) {
            mSettingView = new RestoreSettingsSettingView(getKey() , mApp);
            mSettingView.setRestoreSettingsConfirmedListener(this);

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
            mSettingView.setEnabled(getEntryValues().size() > 1);
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
        return KEY_RESTORE_SETTINGS;
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
    public void onRestoreSettingsConfirmed() {
        String storeScope = getStoreScope();

        mDataStore.setValue(KEY_VOLUME_CAPTURE , DEFAULT_VOLUME_CAPTURE , storeScope ,false);
        mDataStore.setValue(KEY_GRID_LINE , DEFAULT_GRID_LINE , storeScope,false);
        mDataStore.setValue(KEY_TOUCH_CAPTURE , DEFAULT_TOUCH_CAPTURE , storeScope,false);
        mDataStore.setValue(KEY_ZSD , mDataStore.getValue(KEY_DEFAULT_KEY_ZSD ,null , storeScope)  , storeScope,false);
        mDataStore.setValue(KEY_SHUTTER_SOUND , DEFAULT_SHUTTER_SOUND , getStoreScope(),false);
        mDataStore.setValue(KEY_MIRROR_CAPTURE , DEFAULT_MIRROR_CAPTURE , getStoreScope(),false);
        mDataStore.setValue(KEY_AUTO_EXIT , DEFAULT_AUTO_EXIT , storeScope,false);
        mDataStore.setValue(KEY_FOCUS_SOUND , DEFAULT_FOCUS_SOUND , storeScope,false);
        mDataStore.setValue(KEY_PICTURE_SIZE , mDataStore.getValue(KEY_DEFAULT_PICTURE_SIZE,null , storeScope) , storeScope,false);
        mDataStore.setValue(KEY_FACE_DETECTION , DEFAULT_FACE_DETECTION , storeScope,false);
        mDataStore.setValue(KEY_VIDEO_QUALITY , mDataStore.getValue(KEY_DEFAULT_VIDEO_QUALITY,null , storeScope) , storeScope,false);
        

        mSettingController.refreshViewEntry();
        mAppUi.refreshSettingView();
    }
}
