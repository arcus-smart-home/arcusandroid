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
public class ColorPickerTemperatureArc extends ColorPickerArc {

    float percentage = 0f;
    public ColorPickerTemperatureArc(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    public ColorPickerTemperatureArc(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.seekArcStyle);
    }

    public ColorPickerTemperatureArc(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        // Draw the arcs
        int mAngleOffset = 0;

        final int arcStart = mStartAngle + mAngleOffset + mRotation;
        final int arcSweep = mSweepAngle;

        if(gradient1 == null) {
            float gradientStart = 0f;
            float gradientEnd = 1f;
            float[] positions = new float[] { gradientStart, (gradientEnd-gradientStart)/2f, gradientEnd };
            int[] colors = new int[] {Color.HSVToColor(I2ColorUtils.startColor), Color.HSVToColor(I2ColorUtils.midColor), Color.HSVToColor(I2ColorUtils.endColor)};

            gradient1 = new SweepGradient(mArcRect.centerX(), mArcRect.centerY(), colors, positions);
            mArcPaint.setShader(gradient1);
        }

        canvas.rotate(90, mArcRect.centerX(), mArcRect.centerY());
        canvas.drawArc(mArcRect, arcStart, arcSweep, false, mArcPaint);
        canvas.rotate(90, mArcRect.centerX(), mArcRect.centerY());

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
                widthMeasureSpec);

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
        mTouchAngle = (mTouchAngle+180)%360;
        if(mTouchAngle > mSweepAngle) {
            return;
        }
        percentage = ((float)(mTouchAngle))/(mSweepAngle);
        colorHSV = I2ColorUtils.getTemperatureColor3Point(percentage);

        int progress = getProgressForAngle(mTouchAngle);
        onProgressRefresh(progress, true);

    }

    public float[] getValue() {
        return colorHSV;
    }

    public float getPercentage() {
        return percentage;
    }

    @Override
    public void setPosition(float[] color) {
        colorHSV[0] = color[0];
        percentage = color[0]/(float)mSweepAngle;
        double mTouchAngle = percentage*mSweepAngle;

        int progress = getProgressForAngle(mTouchAngle);
        onProgressRefresh(progress, true);
        invalidate();
    }

    @Override
    public void setPosition(float percent) {
        percentage = percent;
        colorHSV = I2ColorUtils.getTemperatureColor3Point(percentage);
        double mTouchAngle = percentage*mSweepAngle;
        int progress = getProgressForAngle(mTouchAngle);
        onProgressRefresh(progress, true);
        invalidate();
    }
}
