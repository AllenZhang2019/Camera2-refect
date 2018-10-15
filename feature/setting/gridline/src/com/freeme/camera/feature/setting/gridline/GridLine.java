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

package com.freeme.camera.feature.setting.gridline;

import android.content.Intent;
import android.provider.MediaStore;
import android.util.Log;

import com.freeme.camera.R;
import com.freeme.camera.common.ICameraContext;
import com.freeme.camera.common.app.IApp;
import com.freeme.camera.common.debug.LogHelper;
import com.freeme.camera.common.debug.LogUtil;
import com.freeme.camera.common.setting.ISettingManager.SettingController;
import com.freeme.camera.common.setting.SettingBase;
import com.freeme.camera.common.widget.PreviewFrameLayout;
import com.freeme.camera.ui.GridLines;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

/**
 * ZSD setting.
 */

public class GridLine extends SettingBase implements GridLineSettingView.OnGridLineClickListener {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(GridLine.class.getSimpleName());


    private GridLineSettingView mSettingView;
    private boolean mIsGridLineSupported = false;
    private boolean mIsThirdParty = false;
    private PreviewFrameLayout mFeatureRootView;
    private GridLines mGridLines;
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


        String value = mDataStore.getValue(getKey(), VALUE_OFF, getStoreScope());
        if (VALUE_ON.equals(value)) {
            addGridLines();
        }
    }

    @Override
    public void unInit() {
        String value = mDataStore.getValue(getKey(), VALUE_OFF, getStoreScope());

        if (VALUE_ON.equals(value)) {
            removeGridLines();
        }

    }

    @Override
    public void overrideValues(@Nonnull String headerKey, String currentValue,
                               List<String> supportValues) {
        if (!mIsGridLineSupported) {
            return;
        }
        super.overrideValues(headerKey, currentValue, supportValues);
    }


    @Override
    public void addViewEntry() {
        if (!mIsGridLineSupported) {
            return;
        }
        if (mSettingView == null) {
            mSettingView = new GridLineSettingView(getKey());
            mSettingView.setGridLineOnClickListener(this);
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
            String value = mDataStore.getValue(getKey(), DEFAULT_GRID_LINE, getStoreScope());
            mSettingView.setChecked(VALUE_ON.equals(value));
            mSettingView.setEnabled(getEntryValues().size() > 1);
            //setValue(value);
            if (VALUE_OFF.equals(value)) {
                removeGridLines();
            }
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
        return KEY_GRID_LINE;
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
    public void onGridLineClicked(boolean checked) {
        String value = checked ? VALUE_ON : VALUE_OFF;
        LogHelper.d(TAG, "[onZsdClicked], value:" + value);
        setValue(value);
        mDataStore.setValue(getKey(), value, getStoreScope(), false);
        if (VALUE_ON.equals(value)) {
            addGridLines();
        } else {
            removeGridLines();
        }
    }

    private void addGridLines(){
        if(mFeatureRootView == null) {
            mFeatureRootView = mApp.getAppUi().getPreviewFrameLayout();
        }
        mGridLines = (GridLines) mFeatureRootView.findViewById(R.id.grid_view);
        if (mGridLines == null) {
            mGridLines = (GridLines) mActivity.getLayoutInflater().inflate(R.layout.grid_view, mFeatureRootView, false);
            mFeatureRootView.addView(mGridLines);

        }
        mApp.getAppUi().registerOnPreviewAreaChangedListener(mGridLines);
    }

    private void removeGridLines(){
        if(mFeatureRootView == null) {
            mFeatureRootView = mApp.getAppUi().getPreviewFrameLayout();
        }
        mApp.getAppUi().unregisterOnPreviewAreaChangedListener(mGridLines);
        mFeatureRootView.removeView(mGridLines);
    }

    private void initSettingValue(){
        mIsGridLineSupported = true;
        mSupportValues.add(VALUE_ON);
        mSupportValues.add(VALUE_OFF);
        setSupportedPlatformValues(mSupportValues);
        setSupportedEntryValues(mSupportValues);
        setEntryValues(mSupportValues);
        String value = mDataStore.getValue(getKey(), DEFAULT_GRID_LINE , getStoreScope());
        setValue(value);
    }
}
