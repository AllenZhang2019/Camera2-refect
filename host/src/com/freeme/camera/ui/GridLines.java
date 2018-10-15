package com.freeme.camera.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.freeme.camera.R;
import com.freeme.camera.common.IAppUiListener;
import com.freeme.camera.common.utils.Size;

public class GridLines extends View
        implements IAppUiListener.OnPreviewAreaChangedListener  {

    private RectF mDrawBounds;
    Paint mPaint = new Paint();

    public GridLines(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint.setStrokeWidth(getResources().getDimensionPixelSize(R.dimen.grid_line_width));
        mPaint.setColor(getResources().getColor(R.color.grid_line));
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mDrawBounds != null) {
            float thirdWidth = mDrawBounds.width() / 3;
            float thirdHeight = mDrawBounds.height() / 3;
            for (int i = 1; i < 3; i++) {
                // Draw the vertical lines.
                final float x = thirdWidth * i;
                canvas.drawLine(mDrawBounds.left + x, mDrawBounds.top,
                        mDrawBounds.left + x, mDrawBounds.bottom, mPaint);
                // Draw the horizontal lines.
                final float y = thirdHeight * i;
                canvas.drawLine(mDrawBounds.left, mDrawBounds.top + y,
                        mDrawBounds.right, mDrawBounds.top + y, mPaint);
            }
        }
    }

    /*@Override
    public void onPreviewAreaChanged(final RectF previewArea) {
        setDrawBounds(previewArea);
    }*/

    public void setDrawBounds(final RectF previewArea) {
        mDrawBounds = new RectF(previewArea);
        invalidate();
    }

    @Override
    public void onPreviewAreaChanged(RectF newPreviewArea, Size previewSize) {
        setDrawBounds(newPreviewArea);

    }
}
