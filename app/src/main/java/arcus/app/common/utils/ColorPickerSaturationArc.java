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
public class ColorPickerSaturationArc extends ColorPickerArc {
    private float hue = 1f;
    private int mArcTranslateX = 0;

    public ColorPickerSaturationArc(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    public ColorPickerSaturationArc(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.seekArcStyle);
    }

    public ColorPickerSaturationArc(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        canvas.translate(-mArcTranslateX, 0);

        // Draw the arcs
        int mAngleOffset = -90;

        final int arcStart = mStartAngle + mAngleOffset + mRotation;
        final int arcSweep = mSweepAngle;

        if(gradient1 == null) {
            float[] positions = new float[] { 0.0f, 0.25f, 0.75f, 1f};
            float midSaturation[] = new float[] { hue, 0.5f, I2ColorUtils.hsvValue };
            float noSaturation[] = new float[] { hue, 0f, I2ColorUtils.hsvValue };
            float fullSaturation[] = new float[] { hue, 1f, I2ColorUtils.hsvValue };

            int colors[] = new int[] {Color.HSVToColor(midSaturation), Color.HSVToColor(fullSaturation),
                                Color.HSVToColor(noSaturation), Color.HSVToColor(midSaturation)};


            gradient1 = new SweepGradient(mArcRect.centerX(), mArcRect.centerY(), colors, positions);
            mArcPaint.setShader(gradient1);
        }

        canvas.drawArc(mArcRect, arcStart, arcSweep, false, mArcPaint);
        canvas.translate(mArcTranslateX, 0);
        int xTranslate = mTranslateX - mThumbLowXPos;
        int yTranslate = mTranslateY - mThumbLowYPos;
        canvas.translate(xTranslate, yTranslate);
        mThumb.draw(canvas);

        //canvas.rotate(-180, mArcRect.centerX(), mArcRect.centerY());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = getDefaultSize(getHeight(),
                heightMeasureSpec);
        int width = (int)((double)getDefaultSize(getWidth(),
                widthMeasureSpec))*2;
        final int min = Math.min(width, height);
        float top;
        float left;
        int arcDiameter;

        mArcTranslateX = (int) (width * 0.5f);
        mTranslateX = 0;
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
            colorHSV[0] = 1f;
            colorHSV[1] = 1f-((float)(mTouchAngle))/mSweepAngle;
            colorHSV[2] = I2ColorUtils.hsvValue;
            onProgressRefresh(progress, true);
        }
    }

    public float getSaturation() {
        return 1f-colorHSV[1];
    }

    public void setHue(float hue) {
        this.hue = hue;
        gradient1 = null;
        invalidate();
    }

    @Override
    public void setPosition(float[] color) {
        colorHSV[1] = 1f-color[1];
        double mTouchAngle = (color[1])*mSweepAngle;
        int progress = getProgressForAngle(mTouchAngle);
        onProgressRefresh(progress, true);
        invalidate();
    }
}
