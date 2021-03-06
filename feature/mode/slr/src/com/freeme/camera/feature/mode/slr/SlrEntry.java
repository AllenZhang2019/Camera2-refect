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

package com.freeme.camera.feature.mode.slr;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.hardware.Camera.Parameters;

import com.freeme.camera.R;
import com.freeme.camera.common.IAppUi.ModeItem;
import com.freeme.camera.common.debug.LogHelper;
import com.freeme.camera.common.debug.LogUtil;
import com.freeme.camera.common.device.CameraDeviceManagerFactory.CameraApi;
import com.freeme.camera.common.loader.DeviceDescription;
import com.freeme.camera.common.loader.FeatureEntryBase;
import com.freeme.camera.common.mode.ICameraMode;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Panorama photo mode entry.
 */
public class SlrEntry extends FeatureEntryBase {
    private static final LogUtil.Tag TAG = new LogUtil.Tag(SlrEntry.class.getSimpleName());
    private static final String MODE_ITEM_TYPE = "Picture";

    private static final int MODE_ITEM_PRIORITY = 45;

    /**
     * create an entry.
     *
     * @param context   current activity.
     * @param resources current resources.
     */
    public SlrEntry(Context context, Resources resources) {
        super(context, resources);
    }

    @Override
    public boolean isSupport(CameraApi currentCameraApi, Activity activity) {
        boolean support = false;
        if (!isThirdPartyIntent(activity)) {
            support = true;
        }
        return support;
    }

    @Override
    public String getFeatureEntryName() {
        return SlrEntry.class.getName();
    }

    @Override
    public Class getType() {
        return ICameraMode.class;
    }

    @Override
    public Object createInstance() {
        return new SlrMode();
    }
    /**
     * Get mode item if the feature is a mode.
     *
     * @return the mode item info.
     */
    @Override
    public ModeItem getModeItem() {
        ModeItem modeItem = new ModeItem();
        modeItem.mModeUnselectedIcon = mContext.getResources().getDrawable(R.drawable
                .ic_slr_mode_selected);
        modeItem.mModeSelectedIcon = mContext.getResources().getDrawable(R.drawable
                .ic_slr_mode_selected);
        modeItem.mType = MODE_ITEM_TYPE;
        modeItem.mPriority = MODE_ITEM_PRIORITY;
        modeItem.mClassName = getFeatureEntryName();
        modeItem.mModeName = mContext.getString(R.string.slr_dialog_title);
        modeItem.mSupportedCameraIds = new String[]{"0"};
        modeItem.mMode = "SLR";
        modeItem.mTitle = this.mContext.getResources().getString(R.string.slr_dialog_title);
        return modeItem;
    }

    private boolean isPlatformSupport() {
        boolean isSupport = false;
        if (CameraApi.API1.equals(mDefaultCameraApi)) {
            ConcurrentHashMap<String, DeviceDescription> descriptionMap =
                    mDeviceSpec.getDeviceDescriptionMap();
            Parameters parameters = descriptionMap.get("0").getParameters();
            if (parameters != null && parameters.get("cap-mode-values") != null) {
                isSupport  = parameters.get("cap-mode-values").contains("autorama");
                LogHelper.d(TAG, "isSupport = " + isSupport);
            }
        }
        return isSupport;
    }
}
