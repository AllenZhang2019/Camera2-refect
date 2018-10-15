package com.freeme.camera.ui.shutter;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.freeme.camera.R;


class ShutterTitleView extends RelativeLayout {
    private String mMode;
    private String mModeName;
    private TextView mName;
    private OnShutterTextClicked mTextClickedListener;
    private String mType;

    public interface OnShutterTextClicked {
        void onShutterTextClicked(int i);
    }

    public ShutterTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setName(String name) {
        this.mName.setText(name);
    }

    public void setType(String type) {
        this.mType = type;
    }

    public void setOnShutterTextClickedListener(OnShutterTextClicked listener) {
        this.mTextClickedListener = listener;
    }

    public String getType() {
        return this.mType;
    }

    public void setMode(String mode) {
        this.mMode = mode;
    }

    public void setModeName(String modeName) {
        this.mModeName = modeName;
    }

    public String getModeName() {
        return this.mModeName;
    }

    public void setTextColor(int color) {
        if (this.mName != null) {
            this.mName.setTextColor(getContext().getResources().getColor(color));
        }
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mName = (TextView) findViewById(R.id.shutter_text);
        this.mName.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (ShutterTitleView.this.mTextClickedListener != null) {
                    ShutterTitleView.this.mTextClickedListener.onShutterTextClicked(((Integer) ShutterTitleView.this.getTag()).intValue());
                }
            }
        });
    }

    public void setEnabled(boolean enabled) {
        if (this.mName != null) {
            this.mName.setEnabled(enabled);
            this.mName.setClickable(enabled);
        }
    }
}
