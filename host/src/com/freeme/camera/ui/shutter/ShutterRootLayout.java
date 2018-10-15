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
package com.freeme.camera.ui.shutter;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.DecelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.Scroller;

import com.freeme.camera.R;
import com.freeme.camera.common.IAppUiListener;

import com.freeme.camera.common.debug.LogHelper;
import com.freeme.camera.common.debug.LogUtil;
import android.os.Handler;

/**
 * Shutter button root layout, control the shutter ui layout and scroll animation.
 */
class ShutterRootLayout extends RelativeLayout implements ShutterTitleView.OnShutterTextClicked {

    private int mChangIndex = 0;
    private Context mContext;
    private int mCurrentIndex = 0;
    Runnable mDelayed = new Runnable() {
        public void run() {
            ShutterRootLayout.this.snapTOShutter(ShutterRootLayout.this.mChangIndex, 600);
        }
    };
    private boolean mDirection = false;
    private Handler mHandler = new Handler();
    private boolean mIsFirstInit = true;
    private OnShutterChangeListener mListener;
    Runnable mNoModeDelayed = new Runnable() {
        public void run() {
            ShutterRootLayout.this.mIsFirstInit = false;
            ShutterRootLayout.this.snapTOShutterNoMode(3, 600);
        }
    };
    private boolean mResumed = false;
    private int mScrollDistance = 0;
    private Scroller mScroller;
    ViewTreeObserver vto = null;

    /**
     * Shutter type change listener.
     */
    public interface OnShutterChangeListener {
        /**
         * When current valid shutter changed, invoke the listener to notify.
         * @param newShutterName The new valid shutter name.
         */
        void onShutterChangedStart(String newShutterName);

        void onShutterChangedStart(String str, String str2);

        /**
         * When shutter change animation finish, invoke the listener to notify.
         */
        void onShutterChangedEnd();
    }
    private static final LogUtil.Tag TAG = new LogUtil.Tag(
                                        ShutterRootLayout.class.getSimpleName());
    private static final int ANIM_DURATION_MS = 1000;

    private static final int MINI_SCROLL_LENGTH = 100;

    public void setOnShutterChangedListener(OnShutterChangeListener listener) {
        mListener = listener;
    }

    public IAppUiListener.OnGestureListener getGestureListener() {
        return new GestureListenerImpl();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }
    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        /*View child;
        for (int i = 0; i < getChildCount(); i++) {
            child = getChildAt(i);
            ((ShutterView) child).onScrolled(l, (getWidth() + 1) / 2, mScrollDistance);
        }*/
    }

    @Override
    public void computeScroll() {
        /*if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            postInvalidate();
            if (mScroller.isFinished() && mListener != null) {
                mListener.onShutterChangedEnd();
            }
        }*/
        if (this.mScroller.computeScrollOffset()) {
            scrollTo(this.mScroller.getCurrX(), this.mScroller.getCurrY());
            postInvalidate();
            if (this.mScroller.isFinished() && this.mListener != null) {
                this.mListener.onShutterChangedEnd();
            }
        }
        for (int i = 0; i < getChildCount(); i++) {
            ShutterTitleView shutter = (ShutterTitleView) getChildAt(i);
            if (i == this.mCurrentIndex) {
                shutter.setTextColor(R.color.shutter_title_select_color);
            } else {
                shutter.setTextColor(R.color.shutter_title_normal_color);
            }
        }
    }

    public ShutterRootLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mScroller = new Scroller(context, new DecelerateInterpolator());
        this.mContext = context;
        this.vto = getViewTreeObserver();
        this.vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                if (ShutterRootLayout.this.mIsFirstInit) {
                    ShutterRootLayout.this.mHandler.removeCallbacks(ShutterRootLayout.this.mNoModeDelayed);
                    ShutterRootLayout.this.mHandler.postDelayed(ShutterRootLayout.this.mNoModeDelayed, 200);
                }
            }
        });
    }


    @Override
    public void onShutterTextClicked(int index) {
        LogHelper.d(TAG, "onShutterTextClicked index = " + index);
        if (mScroller.isFinished() && isEnabled() && mResumed) {
            snapTOShutter(index, ANIM_DURATION_MS);
        }
    }

    public void updateCurrentShutterIndex(int shutterIndex) {
        //doShutterAnimation(shutterIndex, 0);
        snapTOShutterNoMode(0, 0);
        this.mChangIndex = shutterIndex;
        this.mHandler.removeCallbacks(this.mDelayed);
        this.mHandler.postDelayed(this.mDelayed, 100);
    }

    public void onResume() {
        mResumed = true;
    }

    public void onPause() {
        mResumed = false;
    }


    private void doShutterAnimation(int whichShutter, int animationDuration) {
        /*mCurrentIndex = whichShutter;
        if (whichShutter > getChildCount() - 1) {
            mCurrentIndex = getChildCount() - 1;
        }

        int dx = 0;
        if (mCurrentIndex == 0) {
            dx = -getScrollX();
        } else {
            dx = (getChildAt(0).getMeasuredWidth() + getChildAt(1).getMeasuredWidth() + 1) / 2
                    - getScrollX();
        }
        mScroller.startScroll(getScrollX(), 0, dx, 0, animationDuration);
        mScrollDistance = Math.abs(dx);
        invalidate();*/
        int step = Math.abs(whichShutter - this.mCurrentIndex);
        if (whichShutter <= getChildCount() - 1) {
            int dx = 0;
            int i;
            if (whichShutter == 0) {
                dx = -getScrollX();
            } else if (this.mDirection) {
                if (step > 1) {
                    for (i = 0; i < step; i++) {
                        dx += ((getChildAt(this.mCurrentIndex - i).getMeasuredWidth() + getChildAt((this.mCurrentIndex - i) - 1).getMeasuredWidth()) / 2) + this.mContext.getResources().getDimensionPixelOffset(R.dimen.camera_shutter_bar_title_gap);
                    }
                } else {
                    dx = (((getChildAt(this.mCurrentIndex).getMeasuredWidth() + getChildAt(this.mCurrentIndex - 1).getMeasuredWidth()) + 1) / 2) + this.mContext.getResources().getDimensionPixelOffset(R.dimen.camera_shutter_bar_title_gap);
                }
            } else if (step > 1) {
                for (i = 0; i < step; i++) {
                    dx += ((getChildAt(this.mCurrentIndex + i).getMeasuredWidth() + getChildAt((this.mCurrentIndex + i) + 1).getMeasuredWidth()) / 2) + this.mContext.getResources().getDimensionPixelOffset(R.dimen.camera_shutter_bar_title_gap);
                }
            } else {
                dx = (((getChildAt(this.mCurrentIndex).getMeasuredWidth() + getChildAt(this.mCurrentIndex + 1).getMeasuredWidth()) + 1) / 2) + this.mContext.getResources().getDimensionPixelOffset(R.dimen.camera_shutter_bar_title_gap);
            }
            if (this.mDirection && whichShutter != 0) {
                dx = -dx;
            }
            this.mScroller.startScroll(getScrollX(), 0, dx, 0, animationDuration);
            this.mScrollDistance = Math.abs(dx);
            Log.i("zhangxu","-----------SRL-----------------------mScrollDistance:"+mScrollDistance);
            this.mCurrentIndex = whichShutter;
            invalidate();
        }
    }

    private void snapTOShutter(int whichShutter, int animationDuration) {
        /*if (whichShutter == mCurrentIndex) {
            return;
        }
        doShutterAnimation(whichShutter, animationDuration);
        if (mListener != null) {
            ShutterView shutter = (ShutterView) getChildAt(mCurrentIndex);
            mListener.onShutterChangedStart(shutter.getType());
        }*/
        if (whichShutter != this.mCurrentIndex) {
            if (whichShutter > this.mCurrentIndex) {
                this.mDirection = false;
            } else {
                this.mDirection = true;
            }
            doShutterAnimation(whichShutter, animationDuration);
            if (this.mListener != null) {
                ShutterTitleView shutter = (ShutterTitleView) getChildAt(this.mCurrentIndex);
                this.mListener.onShutterChangedStart(shutter.getModeName(), shutter.getType());
            }
        }
    }

    private void snapTOShutterNoMode(int whichShutter, int animationDuration) {
        if (whichShutter != this.mCurrentIndex) {
            if (whichShutter > this.mCurrentIndex) {
                this.mDirection = false;
            } else {
                this.mDirection = true;
            }
            doShutterAnimation(whichShutter, animationDuration);
        }
    }

    /**
     * Gesture listener implementer.
     */
    private class GestureListenerImpl implements IAppUiListener.OnGestureListener {

        private float mTransitionX;
        private float mTransitionY;
        private boolean mIsScale;

        @Override
        public boolean onDown(MotionEvent event) {
            mTransitionX = 0;
            mTransitionY = 0;
            return false;
        }

        @Override
        public boolean onUp(MotionEvent event) {
            mTransitionX = 0;
            mTransitionY = 0;
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float dx, float dy) {
            if (e2.getPointerCount() > 1) {
                return false;
            }
            if (getChildCount() < 2) {
                return false;
            }
            if (mIsScale) {
                return false;
            }
            if (mScroller.isFinished() && isEnabled() && mResumed) {
                mTransitionX += dx;
                mTransitionY += dy;

                Configuration config = getResources().getConfiguration();
                if (config.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (Math.abs(mTransitionX) > MINI_SCROLL_LENGTH
                            && Math.abs(mTransitionY) < Math.abs(mTransitionX)) {
                        if (mTransitionX > 0 && mCurrentIndex < (getChildCount() - 1)) {
                            if (getVisibility() != VISIBLE) {
                                return false;
                            }
                            if (getChildAt(mCurrentIndex + 1).getVisibility() != VISIBLE) {
                                return false;
                            }
                            snapTOShutter(mCurrentIndex + 1, ANIM_DURATION_MS);
                        } else if (mTransitionX < 0 && mCurrentIndex > 0) {
                            if (getVisibility() != VISIBLE) {
                                return false;
                            }
                            if (getChildAt(mCurrentIndex - 1).getVisibility() != VISIBLE) {
                                return false;
                            }
                            snapTOShutter(mCurrentIndex - 1, ANIM_DURATION_MS);
                        }
                        return true;
                    }
                } else if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if (Math.abs(mTransitionY) > MINI_SCROLL_LENGTH
                            && Math.abs(mTransitionX) < Math.abs(mTransitionY)) {
                        if (mTransitionY < 0 && mCurrentIndex < (getChildCount() - 1)) {
                            if (getChildAt(mCurrentIndex + 1).getVisibility() != VISIBLE) {
                                return false;
                            }
                            snapTOShutter(mCurrentIndex + 1, ANIM_DURATION_MS);
                        } else if (mTransitionY > 0 && mCurrentIndex > 0) {
                            if (getChildAt(mCurrentIndex - 1).getVisibility() != VISIBLE) {
                                return false;
                            }
                            snapTOShutter(mCurrentIndex - 1, ANIM_DURATION_MS);
                        }
                    }
                }
                return false;
            } else {
                return true;
            }
        }

        @Override
        public boolean onSingleTapUp(float x, float y) {
            return false;
        }

        @Override
        public boolean onSingleTapConfirmed(float x, float y) {
            return false;
        }

        @Override
        public boolean onDoubleTap(float x, float y) {
            return false;
        }

        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            mIsScale = true;
            return false;
        }

        @Override
        public boolean onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
            mIsScale = false;
            return false;
        }

        @Override
        public boolean onLongPress(float x, float y) {
            return false;
        }
    }
}
