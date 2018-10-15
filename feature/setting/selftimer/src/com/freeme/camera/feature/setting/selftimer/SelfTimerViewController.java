/*
 * Copyright Statement:
 *
 *   This software/firmware and related documentation ("MediaTek Software") are
 *   protected under relevant copyright laws. The information contained herein is
 *   confidential and proprietary to MediaTek Inc. and/or its licensors. Without
 *   the prior written permission of MediaTek inc. and/or its licensors, any
 *   reproduction, modification, use or disclosure of MediaTek Software, and
 *   information contained herein, in whole or in part, shall be strictly
 *   prohibited.
 *
 *   MediaTek Inc. (C) 2016. All rights reserved.
 *
 *   BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 *   THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 *   RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER
 *   ON AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL
 *   WARRANTIES, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED
 *   WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 *   NONINFRINGEMENT. NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH
 *   RESPECT TO THE SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY,
 *   INCORPORATED IN, OR SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES
 *   TO LOOK ONLY TO SUCH THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO.
 *   RECEIVER EXPRESSLY ACKNOWLEDGES THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO
 *   OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES CONTAINED IN MEDIATEK
 *   SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK SOFTWARE
 *   RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 *   STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S
 *   ENTIRE AND CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE
 *   RELEASED HEREUNDER WILL BE, AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE
 *   MEDIATEK SOFTWARE AT ISSUE, OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE
 *   CHARGE PAID BY RECEIVER TO MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 *   The following software/firmware and/or related documentation ("MediaTek
 *   Software") have been modified by MediaTek Inc. All revisions are subject to
 *   any receiver's applicable license agreements with MediaTek Inc.
 */
package com.freeme.camera.feature.setting.selftimer;

import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.freeme.camera.R;
import com.freeme.camera.common.IAppUiListener;
import com.freeme.camera.common.app.IApp;
import com.freeme.camera.common.debug.LogHelper;
import com.freeme.camera.common.debug.LogUtil;
import com.freeme.camera.common.widget.RotateImageView;


/**
 * This class manages the looks of the flash and flash mode choice view.
 */
public class SelfTimerViewController {
    private static final LogUtil.Tag TAG =
            new LogUtil.Tag(SelfTimerViewController.class.getSimpleName());

    private static final int SELFTIMER_ENTRY_LIST_SWITCH_SIZE = 2;
    private static final int SELFTIMER_ENTRY_LIST_INDEX_0 = 0;
    private static final int SELFTIMER_ENTRY_LIST_INDEX_1 = 1;
    private static final int SELFTIMER_PRIORITY = 40;
    private static final int SELFTIMER_SHUTTER_PRIORITY = 70;

    private static final String SELFTIMER_OFF_VALUE = "off";
    private static final String SELFTIMER_2_VALUE = "2";
    private static final String SELFTIMER_10_VALUE = "10";

    private static final int SELFTIMER_VIEW_INIT = 0;
    private static final int SELFTIMER_VIEW_ADD_QUICK_SWITCH = 1;
    private static final int SELFTIMER_VIEW_REMOVE_QUICK_SWITCH = 2;
    private static final int SELFTIMER_VIEW_HIDE_CHOICE_VIEW = 3;
    private static final int SELFTIMER_VIEW_UPDATE_QUICK_SWITCH_ICON = 4;

    private ImageView mSelfTimerEntryView;
    private ImageView mSelfTimerOffIcon;
    private ImageView mSelfTimer2Icon;
    private ImageView mSelfTimer10Icon;
    private View mSelfTimerChoiceView;
    private View mOptionLayout;
    private final SelfTimer mSelfTimer;
    private final IApp mApp;
    private MainHandler mMainHandler;

    /**
     * Constructor of flash view.
     * @param selfTimer Flash instance.
     * @param app   The application app level controller.
     */
    public SelfTimerViewController(SelfTimer selfTimer, IApp app) {
        mSelfTimer = selfTimer;
        mApp = app;
        mMainHandler = new MainHandler(app.getActivity().getMainLooper());
        mSelfTimerEntryView = initSelfTimerEntryView();
        mMainHandler.sendEmptyMessage(SELFTIMER_VIEW_INIT);
    }

    /**
     * add flash switch to quick switch.
     */
    public void addQuickSwitchIcon() {
        mApp.getAppUi().addToQuickSwitcher(mSelfTimerEntryView, SELFTIMER_PRIORITY);
        mMainHandler.sendEmptyMessage(SELFTIMER_VIEW_ADD_QUICK_SWITCH);
    }

    /**
     * remove qiuck switch icon.
     */
    public void removeQuickSwitchIcon() {
        mApp.getAppUi().removeFromQuickSwitcher(mSelfTimerEntryView);
        mMainHandler.sendEmptyMessage(SELFTIMER_VIEW_REMOVE_QUICK_SWITCH);
    }

    /**
     * for overrides value, for set visibility.
     * @param isShow true means show.
     */
    public void showQuickSwitchIcon(boolean isShow) {
        mMainHandler.obtainMessage(SELFTIMER_VIEW_UPDATE_QUICK_SWITCH_ICON, isShow).sendToTarget();
    }

    /**
     * close option menu.
     */
    public void hideSelfTimerChoiceView() {
        mMainHandler.sendEmptyMessage(SELFTIMER_VIEW_HIDE_CHOICE_VIEW);
    }

    /**
     * Handler let some task execute in main thread.
     */
    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            LogHelper.d(TAG, "view handleMessage: " + msg.what);
            switch (msg.what) {
                case SELFTIMER_VIEW_INIT:
                    break;

                case SELFTIMER_VIEW_ADD_QUICK_SWITCH:
                    updateSelfTimerEntryView(mSelfTimer.getValue());
                    mApp.getAppUi().registerOnShutterButtonListener(mShutterListener,
                            SELFTIMER_SHUTTER_PRIORITY);
                    break;

                case SELFTIMER_VIEW_REMOVE_QUICK_SWITCH:
                    //updateFlashIndicator(false);
                    mApp.getAppUi().unregisterOnShutterButtonListener(mShutterListener);
                    break;

                case SELFTIMER_VIEW_UPDATE_QUICK_SWITCH_ICON:
                    if ((boolean) msg.obj) {
                        mSelfTimerEntryView.setVisibility(View.VISIBLE);
                        updateSelfTimerEntryView(mSelfTimer.getValue());
                    } else {
                        mSelfTimerEntryView.setVisibility(View.GONE);
                    }
                    break;

                case SELFTIMER_VIEW_HIDE_CHOICE_VIEW:
                    if (mSelfTimerChoiceView != null && mSelfTimerChoiceView.isShown()) {
                        mApp.getAppUi().hideQuickSwitcherOption();
                        updateSelfTimerEntryView(mSelfTimer.getValue());
                        // Flash indicator no need to show now,would be enable later
                        // updateFlashIndicator(mFlash.getValue());
                    }
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * Update ui by the value.
     * @param value the value to change.
     *
     */
    private void updateSelfTimerEntryView(final String value) {
        LogHelper.d(TAG, "[updateFlashView] currentValue = " + mSelfTimer.getValue());
        if (SELFTIMER_10_VALUE.equals(value)) {
            mSelfTimerEntryView.setImageResource(R.drawable.ic_selftimer_indicator_10);
            mSelfTimerEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.self_timer_entry_10));
        } else if (SELFTIMER_2_VALUE.equals(value)) {
            mSelfTimerEntryView.setImageResource(R.drawable.ic_selftimer_indicator_2);
            mSelfTimerEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.self_timer_entry_2));
        } else {
            mSelfTimerEntryView.setImageResource(R.drawable.ic_selftimer_indicator_off);
            mSelfTimerEntryView.setContentDescription(mApp.getActivity().getResources().getString(
                    R.string.self_timer_entry_off));
        }
        // Flash indicator no need to show now,would be enable later
        //updateFlashIndicator(value);
    }

    /**
     * Initialize the flash view which will add to quick switcher.
     * @return the view add to quick switcher
     */
    private ImageView initSelfTimerEntryView() {
        Activity activity = mApp.getActivity();
        RotateImageView view = (RotateImageView) activity.getLayoutInflater().inflate(
                R.layout.self_timer_icon, null);
        view.setOnClickListener(mSelfTimerEntryListener);

        return view;
    }

    /**
     * This listener used to monitor the flash quick switch icon click item.
     */
    private final View.OnClickListener mSelfTimerEntryListener = new View.OnClickListener() {
        public void onClick(View view) {
            if (mSelfTimer.getEntryValues().size() <= 1) {
                return;
            }
            if (mSelfTimer.getEntryValues().size() > SELFTIMER_ENTRY_LIST_SWITCH_SIZE) {
                initializeSelfTimerChoiceView();
                updateChoiceView();
                mApp.getAppUi().showQuickSwitcherOption(mOptionLayout);
            } else {
                String value = mSelfTimer.getEntryValues().get(SELFTIMER_ENTRY_LIST_INDEX_0);
                if (value.equals(mSelfTimer.getValue())) {
                    value = mSelfTimer.getEntryValues().get(SELFTIMER_ENTRY_LIST_INDEX_1);
                }
                updateSelfTimerEntryView(value);
                // Flash indicator no need to show now,would be enable later
                // updateFlashIndicator(value);
                mSelfTimer.onSelfTimerValueChanged(value);
            }
        }
    };

    private View.OnClickListener mSelfTimerChoiceViewListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String value = "";
            if (mSelfTimerOffIcon == view) {
                value = SELFTIMER_OFF_VALUE;
            } else if (mSelfTimer2Icon == view) {
                value = SELFTIMER_2_VALUE;
            } else {
                value = SELFTIMER_10_VALUE;
            }
            mApp.getAppUi().hideQuickSwitcherOption();
            updateSelfTimerEntryView(value);
            // Flash indicator no need to show now,would be enable later
            // updateFlashIndicator(value);
            mSelfTimer.onSelfTimerValueChanged(value);
        }

    };


    /**
     * This function used to high light the current choice for.
     * flash if flash choice view is show.
     */
    private void updateChoiceView() {
        if (SELFTIMER_OFF_VALUE.equals(mSelfTimer.getValue())) {
            mSelfTimerOffIcon.setImageResource(R.drawable.ic_selftimer_indicator_off_selected);
            mSelfTimer2Icon.setImageResource(R.drawable.ic_selftimer_indicator_2);
            mSelfTimer10Icon.setImageResource(R.drawable.ic_selftimer_indicator_10);
        } else if (SELFTIMER_2_VALUE.equals(mSelfTimer.getValue())) {
            mSelfTimerOffIcon.setImageResource(R.drawable.ic_selftimer_indicator_off);
            mSelfTimer2Icon.setImageResource(R.drawable.ic_selftimer_indicator_2_selected);
            mSelfTimer10Icon.setImageResource(R.drawable.ic_selftimer_indicator_10);
        } else {
            mSelfTimerOffIcon.setImageResource(R.drawable.ic_selftimer_indicator_off);
            mSelfTimer2Icon.setImageResource(R.drawable.ic_selftimer_indicator_2);
            mSelfTimer10Icon.setImageResource(R.drawable.ic_selftimer_indicator_10_selected);
        }
    }

    private void initializeSelfTimerChoiceView() {
        if (mSelfTimerChoiceView == null || mOptionLayout == null) {
            ViewGroup viewGroup =  mApp.getAppUi().getModeRootView();
            mOptionLayout = mApp.getActivity().getLayoutInflater().inflate(
                    R.layout.self_timer_option, viewGroup, false);
            mSelfTimerChoiceView = mOptionLayout.findViewById(R.id.self_timer_choice);
            mSelfTimerOffIcon = (ImageView) mOptionLayout.findViewById(R.id.self_timer_off);
            mSelfTimer2Icon = (ImageView) mOptionLayout.findViewById(R.id.self_timer_2);
            mSelfTimer10Icon = (ImageView) mOptionLayout.findViewById(R.id.self_timer_10);

            mSelfTimerOffIcon.setOnClickListener(mSelfTimerChoiceViewListener);
            mSelfTimer2Icon.setOnClickListener(mSelfTimerChoiceViewListener);
            mSelfTimer10Icon.setOnClickListener(mSelfTimerChoiceViewListener);
        }
    }

    private final IAppUiListener.OnShutterButtonListener mShutterListener =
            new IAppUiListener.OnShutterButtonListener() {

                @Override
                public boolean onShutterButtonFocus(boolean pressed) {
                    if (pressed) {
                        hideSelfTimerChoiceView();
                    }
                    return false;
                }

                @Override
                public boolean onShutterButtonClick() {
                    return false;
                }

                @Override
                public boolean onShutterButtonLongPressed() {
                    return false;
                }
            };
}
