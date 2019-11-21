package arcus.app.common.utils;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

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
public class ColorPickerArc extends View {

    protected static final String TAG = ColorPickerArc.class.getSimpleName();
    protected static int INVALID_PROGRESS_VALUE = -1;
    public static final int THUMB_LOW = 0;

    SweepGradient gradient1;
    protected float[] colorHSV = new float[] { 0f, 0f, 1f };
    /**
     * The Drawable for the seek arc thumbnail
     */
    @Nullable
    protected Drawable mThumb;

    /**
     * The Maximum value that this SeekArc can be set to
     */
    protected int mMax = 360;

    /**
     * The visual min / max values for the picker
     */
    protected int mMinValue = 0;
    protected int mMaxValue = 100;

    /**
     * The Current value that the SeekArc is set to
     */
    protected int mProgress = 0;

    /**
     * The width of the progress line for this SeekArc
     */
    protected int mProgressWidth = 4;

    /**
     * The Width of the background arc for the SeekArc
     */
    protected int mArcWidth = 32;

    /**
     * The Angle to start drawing this Arc from
     */
    protected int mStartAngle = 0;

    /**
     * The Angle through which to draw the arc (Max is 360)
     */
    protected int mSweepAngle = 360;

    /**
     * The rotation of the SeekArc- 0 is twelve o'clock
     */
    protected int mRotation = 0;

    /**
     * Give the SeekArc rounded edges
     */
    protected boolean mRoundedEdges = true;
    private float mTouchIgnoreInnerRadius;

    // Internal variables
    protected int mArcRadius = 0;
    protected float mProgressSweep = 0;
    @NonNull
    protected RectF mArcRect = new RectF();
    protected Paint mArcPaint;
    protected int mTranslateX;
    protected int mTranslateY;
    protected int mThumbLowXPos;
    protected int mThumbLowYPos;
    protected OnSeekArcChangeListener mOnSeekArcChangeListener;
    protected boolean mIsDragging;
    protected float strokeWidth;

    public interface OnSeekArcChangeListener {

        /**
         * Notification that the progress level has changed. Clients can use the
         * fromUser parameter to distinguish user-initiated changes from those
         * that occurred programmatically.
         *
         * @param seekArc  The SeekArc whose progress has changed
         * @param progress The current progress level. This will be in the range
         *                 0..max where max was set by
         *                 ProgressArc#setMax(int). (The default value for
         *                 max is 100.)
         * @param fromUser True if the progress change was initiated by the user.
         */
        void onProgressChanged(ColorPickerArc seekArc, int progress, boolean fromUser);

        /**
         * Notification that the user has started a touch gesture. Clients may
         * want to use this to disable advancing the seekbar.
         *
         * @param seekArc The SeekArc in which the touch gesture began
         * @param thumb The thumb that the user is interacting with
         * @param progress The current progress level
         */
        void onStartTrackingTouch(ColorPickerArc seekArc, int thumb, int progress);

        /**
         * Notification that the user has finished a touch gesture. Clients may
         * want to use this to re-enable advancing the seekarc.
         *
         * @param seekArc The SeekArc in which the touch gesture began
         * @param thumb The thumb that the user is interacting with
         * @param progress The current progress level
         */
        void onStopTrackingTouch(ColorPickerArc seekArc, int thumb, int progress);
    }

    public ColorPickerArc(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    public ColorPickerArc(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.seekArcStyle);
    }

    public ColorPickerArc(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    protected void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {

        Log.d(TAG, "Initialising ColorPickerArc");
        float density = context.getResources().getDisplayMetrics().density;
        int thumbHalfheight;
        int thumbHalfWidth;
        //mThumb = ContextCompat.getDrawable(getContext(), R.drawable.color_picker_selector);
        mThumb = ContextCompat.getDrawable(getContext(), R.drawable.color_arch_selector);
        mThumb.clearColorFilter();
        // Convert progress width to pixels for current density
        mProgressWidth = (int) (mProgressWidth * density);

        if (attrs != null) {
            // Attribute initialization
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.SeekArc, defStyle, 0);

            Drawable thumb = a.getDrawable(R.styleable.SeekArc_thumb);
            if (thumb != null) {
                mThumb = thumb;
            }

            mProgress = a.getInteger(R.styleable.SeekArc_progress, mProgress);
            mProgressWidth = (int) a.getDimension(
                    R.styleable.SeekArc_progressWidth, mProgressWidth);
            mArcWidth = (int) a.getDimension(R.styleable.SeekArc_arcWidth,
                    mArcWidth);
            mStartAngle = a.getInt(R.styleable.SeekArc_startAngle, mStartAngle);
            mSweepAngle = a.getInt(R.styleable.SeekArc_sweepAngle, mSweepAngle);

            mRotation = a.getInt(R.styleable.SeekArc_rotation, mRotation);
            mRoundedEdges = a.getBoolean(R.styleable.SeekArc_roundEdges,
                    mRoundedEdges);

            thumbHalfheight = mArcWidth / 2;
            DisplayMetrics dm = getResources().getDisplayMetrics() ;
            int dispValue = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, thumbHalfheight, dm);
            mThumb.setBounds(-dispValue, -dispValue, dispValue,
                    dispValue);

            a.recycle();
        }

        mProgress = (mProgress > mMax) ? mMax : mProgress;
        mProgress = (mProgress < 0) ? 0 : mProgress;

        mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
        mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;

        mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
        mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;

        mMax = mStartAngle+mSweepAngle;

        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setStyle(Paint.Style.STROKE);
        DisplayMetrics dm = getResources().getDisplayMetrics() ;
        strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mArcWidth, dm);
        mArcPaint.setStrokeWidth(strokeWidth);

        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {

    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
                if (ignoreTouch) {
                    break;
                }
                onStartTrackingTouch();
                updateOnTouch(event);
                attemptClaimDrag();
                break;
            case MotionEvent.ACTION_MOVE:
                updateOnTouch(event);
                break;
            case MotionEvent.ACTION_UP:
                onStopTrackingTouch();
                setPressed(false);
                break;
            case MotionEvent.ACTION_CANCEL:
                onStopTrackingTouch();
                setPressed(false);

                break;
        }

        return true;
    }

    protected void onStartTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener.onStartTrackingTouch(this, THUMB_LOW, getProgress());
        }
        mIsDragging = true;
    }

    protected void onStopTrackingTouch() {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener.onStopTrackingTouch(this, THUMB_LOW, getProgress());
        }
        mIsDragging = false;
    }

    protected void updateOnTouch(@NonNull MotionEvent event) {
    }

    public int getColor() {
        return Color.HSVToColor(colorHSV);
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any ancestors from stealing events in the drag.
     */
    protected void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    protected double getTouchDegrees(float xPos, float yPos) {
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;
        // convert to arc Angle
        double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2)
                - Math.toRadians(mRotation));
        if (angle < 0) {
            angle = 360 + angle;
        }
        angle -= mStartAngle;
        return angle;
    }

    protected int getProgressForAngle(double angle) {
        int touchProgress = (int) Math.round(valuePerDegree() * angle);
        if(touchProgress > 360) {
            touchProgress = INVALID_PROGRESS_VALUE;
        }
        else if(touchProgress < 0) {
            touchProgress = INVALID_PROGRESS_VALUE;
        }
        else if(touchProgress > mMax) {
            touchProgress = INVALID_PROGRESS_VALUE;
        }
        return touchProgress;
    }

    protected float valuePerDegree() {
        return (float) mMax / mSweepAngle;
    }

    protected void onProgressRefresh(int progress, boolean fromUser) {
        updateProgress(progress, fromUser);
    }

    protected void updateThumbPosition() {
        int thumbLowAngle = (int) (mStartAngle + mProgressSweep + mRotation + 90);
        mThumbLowXPos = (int) (mArcRadius * Math.cos(Math.toRadians(thumbLowAngle)));
        mThumbLowYPos = (int) (mArcRadius * Math.sin(Math.toRadians(thumbLowAngle)));
    }

    protected void updateProgress(int progress, boolean fromUser) {
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener
                    .onProgressChanged(this, calculateNearestValue(progress), fromUser);
        }

        // MixMax Check
        progress = (progress > mMax) ? mMax : progress;
        progress = (progress < 0) ? 0 : progress;

        progress = (mProgress < 0) ? 0 : progress;
        mProgress = progress;
        mProgressSweep = (float) progress / mMax * mSweepAngle;

        updateThumbPosition();

        invalidate();
    }

    /**
     * Sets a listener to receive notifications of changes to the SeekArc's
     * progress level. Also provides notifications of when the user starts and
     * stops a touch gesture within the SeekArc.
     *
     * @param l The seek bar notification listener
     */
    public void setOnSeekArcChangeListener(OnSeekArcChangeListener l) {
        mOnSeekArcChangeListener = l;
    }

    // Calculated Progress
    public int getProgress() {
        return calculateNearestValue(mProgress);
    }

    // Convert 0-100 to Device Range
    protected int calculateNearestValue(int progress) {
        //NewValue = (((OldValue - OldMin) * (NewMax - NewMin)) / (OldMax - OldMin)) + NewMin
        return (int) remap(progress, 0f, 100f, mMinValue, mMaxValue);
    }

    protected float remap(float x, float oMin, float oMax, float nMin, float nMax ){

        //range check
        if( oMin == oMax) {
            return -1;
        }

        if( nMin == nMax) {
            return -1;
        }

        //check reversed input range
        boolean reverseInput = false;
        float oldMin = Math.min(oMin, oMax);
        float oldMax = Math.max(oMin, oMax);
        if (oldMin == oMin)
            reverseInput = true;

        //check reversed output range
        boolean reverseOutput = false;
        float newMin = Math.min(nMin, nMax);
        float newMax = Math.max(nMin, nMax);
        if (newMin == nMin)
            reverseOutput = true;

        float portion = (x-oldMin)*(newMax-newMin)/(oldMax-oldMin);
        if (reverseInput)
            portion = (oldMax-x)*(newMax-newMin)/(oldMax-oldMin);

        float result = portion + newMin;
        if (reverseOutput)
            result = newMax - portion;

        return result;
    }

    public void setTouchInSide() {
        int thumbHalfheight = mThumb.getBounds().height() / 2;
        int thumbHalfWidth = mThumb.getBounds().width() / 2;

        // Don't use the exact radius makes interaction too tricky
        mTouchIgnoreInnerRadius = mArcRadius
                - Math.min(thumbHalfWidth, thumbHalfheight);
    }

    protected boolean ignoreTouch(float xPos, float yPos) {
        boolean ignore = false;
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;
        float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
        
        if ((touchRadius < mArcRadius - (strokeWidth*2)
                || touchRadius > mArcRadius + (strokeWidth*2))) {

            ignore = true;
        }

        return ignore;
    }

    public void setPosition(float[] color) {
    }
    public void setPosition(float percentage) {
    }
}
