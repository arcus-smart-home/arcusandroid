package arcus.app.common.utils;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.SweepGradient;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

import arcus.app.R;

/**
 * ****************************************************************************
 * The MIT License (MIT)
 * <p/>
 * Copyright (c) 2013 Triggertrap Ltd
 * Author Neil Davies
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 * ****************************************************************************
 */

/**
 * SeekArc.java
 * <p/>
 * This is a class that functions much like a SeekBar but
 * follows a circle path instead of a straight line.
 *
 * @author Neil Davies
 */
public class ColorPickerHueArc extends ColorPickerArc {

    private float MAX_COLOR = 360f;
    private float MIN_COLOR = 0f;

    public ColorPickerHueArc(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    public ColorPickerHueArc(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.seekArcStyle);
    }

    public ColorPickerHueArc(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        // Draw the arcs
        int mAngleOffset = -90;

        final int arcStart = mStartAngle + mAngleOffset + mRotation;
        final int arcSweep = mSweepAngle;

        if(gradient1 == null) {
            float gradientStart = (mStartAngle + 90)/360f;
            int colorCount = 12;
            //to decrease the spectrum, multiply the sweep angle.  Also do this in updateOnTouch
            float increment = (mSweepAngle/360f)/(colorCount);
            int colorAngleStep = 360 / colorCount;
            int colors[] = new int[colorCount + 1];
            float[] positions = new float[colorCount + 1];
            float hsv[] = new float[] { 0f, 1f, I2ColorUtils.hsvValue };
            for (int i = 0; i < colors.length; i++) {
                float percentage = ((float)(i * colorAngleStep))/360f;
                hsv[0] = (percentage*(MAX_COLOR-MIN_COLOR))+MIN_COLOR;
                colors[i] = Color.HSVToColor(hsv);
                positions[i] = gradientStart+(increment*i);
            }
            colors[colorCount] = colors[0];
            positions[colorCount] = 1.0f;

            gradient1 = new SweepGradient(mArcRect.centerX(), mArcRect.centerY(), colors, positions);
            mArcPaint.setShader(gradient1);
        }

        canvas.drawArc(mArcRect, arcStart, arcSweep, false, mArcPaint);

        // Draw the thumb nail
        int xTranslate = mTranslateX - mThumbLowXPos;
        int yTranslate = mTranslateY - mThumbLowYPos;
        canvas.translate(xTranslate, yTranslate);

        mThumb.draw(canvas);

        canvas.rotate(-180, mArcRect.centerX(), mArcRect.centerY());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getHeight(),
                heightMeasureSpec);
        int width = getDefaultSize(getWidth(),
                widthMeasureSpec)*2;

        final int min = Math.min(width, height);
        float top;
        float left;
        int arcDiameter;

        mTranslateX = (int) (width * 0.5f);
        mTranslateY = (int) (height * 0.5f);

        arcDiameter = min - (getPaddingLeft()+90);
        mArcRadius = arcDiameter / 2;
        top = height / 2 - (arcDiameter / 2);
        left = width / 2 - (arcDiameter / 2);

        mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);

        int arcLowStart = (int) mProgressSweep + mStartAngle + mRotation + 90;
        mThumbLowXPos = (int) (mArcRadius * Math.cos(Math.toRadians(arcLowStart)));
        mThumbLowYPos = (int) (mArcRadius * Math.sin(Math.toRadians(arcLowStart)));

        setTouchInSide();
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void updateOnTouch(@NonNull MotionEvent event) {
        if(!mIsDragging) {
            return;
        }
        setPressed(true);
        double mTouchAngle = getTouchDegrees(event.getX(), event.getY());

        int progress = getProgressForAngle(mTouchAngle);
        if(progress != INVALID_PROGRESS_VALUE) {
            float percentage = ((float)(mTouchAngle))/(mSweepAngle);
            colorHSV[0] = (percentage*(MAX_COLOR-MIN_COLOR))+MIN_COLOR;
            colorHSV[1] = 1f;
            colorHSV[2] = I2ColorUtils.hsvValue;
            onProgressRefresh(progress, true);
        }
    }

    public float getHue() {
        return colorHSV[0];
    }

    @Override
    public void setPosition(float[] color) {
        colorHSV[0] = color[0];
        float percentage = (color[0]-MIN_COLOR)/(MAX_COLOR-MIN_COLOR);
        double mTouchAngle = (percentage)*mSweepAngle;
        if (mTouchAngle < 0) {
            mTouchAngle = 0;
        }

        int progress = getProgressForAngle(mTouchAngle);
        onProgressRefresh(progress, true);
        invalidate();
    }

    public void setMaxHueValue(float max) {
        MAX_COLOR = max;
    }

    public void setMinHueValue(float min) {
        MIN_COLOR = min;
    }
}
