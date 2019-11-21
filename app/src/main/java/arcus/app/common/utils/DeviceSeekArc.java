package arcus.app.common.utils;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import arcus.app.ArcusApplication;
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
public class DeviceSeekArc extends View {

    private static final String TAG = DeviceSeekArc.class.getSimpleName();
    private static int INVALID_PROGRESS_VALUE = -1;
    public static final int THUMB_NONE = -1;
    public static final int THUMB_LOW = 0;
    public static final int THUMB_HIGH = 1;
    private static final int DEFAULT_TEXT_SIZE_IN_DP = 14;
    private static final int DEFAULT_TEXT_DISTANCE_TO_BUTTON_IN_DP = 0;
    private static final int DEFAULT_TEXT_DISTANCE_TO_TOP_IN_DP = 0;

    private int mThumbBeingDragged = THUMB_NONE;

    /**
     * When true, user can click within the progress track to cause the thumb to jump to the
     * clicked location. Has no affect when mRangeSelection is true.
     */
    private boolean mIsClickToSeek = true;

    /**
     * "Progress" color to paint slider between the low and high thumb (when range is enabled)
     */
    private int mRangeColor = Color.TRANSPARENT;
    private Paint mRangePaint;

    /**
     * Provides a hook for consumers to convert custom label text values (i.e., add a superscript
     * degree symbol to a temperature value)
     */
    private LabelTextTransform mLabelTransformer;

    /**
     * Represents minimum and maximum selectable values within a larger range of rendered values.
     * When non-null, a dot is drawn at the given position and thumbs cannot be moved to a value
     * above or below these stops.
     */
    private Integer mMinStop = null;
    private Integer mMaxStop = null;
    private ArcElementLocation mMinStopLoc = new ArcElementLocation(0);
    private ArcElementLocation mMaxStopLoc = new ArcElementLocation(0);
    private Drawable mStop;

    /**
     * Represents a gradient hilite painted on the progress track of the seek arc. For example,
     * used to draw a blue-to-white hilight between the current temperature and cool setpoint when
     * the cooling is active.
     */
    private ArcHiliteGradient mHiliteGradient = null;

    /**
     * A non-movable indicator/marker drawn at some position on the slider (used, for example, to
     * display current temperature on thermostat controls.
     */
    private TextPaint mLabelPaint;
    private Integer mMarkerPosition;
    private Drawable mMarkerDrawable;
    private ArcElementLocation mMarkerLoc = new ArcElementLocation(0);


    // TODO: "Legacy" label mode; this flag and associated code should be deleted when conversion to Nest look-and-feel is complete.
    private boolean mDrawLabelsInsideThumbs = true;

    /**
     * When layout size is not WRAP_CONTENT or MATCH_PARENT, the view will be drawn at exactly
     * the size specified, irrespective of whether or not the requested size exceeds clipping
     * boundaries.
     */
    private boolean mUseFixedSize = false;

    /**
     * Enabled or Disabled
     */
    private boolean enabled = true;

    /**
     * Indicates whether thumbs should be drawn when widget is disabled
     */
    private boolean drawThumbsWhenDisabled = false;

    /**
     * Single or MultiThumb Range Selection
     */
    private boolean mRangeSelection = false;

    /**
     * Distance between range values relative to the mMinValue and mMaxValue
     */
    private int mMinRangeDistance = 0;

    /**
     * Text Enabled
     */
    private boolean mTextEnabled = false;
    private int mTextSize;
    private int mTextOffset;

    /**
     * The Drawable for the seek arc thumbnail
     */
    @Nullable
    private Drawable mThumbLow;
    @Nullable
    private Drawable mThumbHigh;
    @Nullable
    private Drawable mPressedThumb = null;
    @Nullable
    private Drawable mLastPressedThumb = null;

    /**
     * Resource for the selected and unselected thumb
     */
    @Nullable
    private Drawable mSelectedResource;
    @Nullable
    private Drawable mUnSelectedResource;

    /**
     * The Maximum value that this SeekArc can be set to
     */
    private int mMax = 100;

    /**
     * The visual min / max values for the picker
     */
    private int mMinValue = 0;
    private int mMaxValue = 100;

    /**
     * The Current value that the SeekArc is set to
     */
    private int mProgressLow = 0;
    private int mProgressHigh = 90;

    /**
     * The width of the progress line for this SeekArc
     */
    private int mProgressWidth = 4;

    /**
     * The Width of the background arc for the SeekArc
     */
    private int mArcWidth = 2;

    /**
     * The Angle to start drawing this Arc from
     */
    private int mStartAngle = 0;

    /**
     * The Angle through which to draw the arc (Max is 360)
     */
    private int mSweepAngle = 360;

    /**
     * The rotation of the SeekArc- 0 is twelve o'clock
     */
    private int mRotation = 0;

    /**
     * Give the SeekArc rounded edges
     */
    private boolean mRoundedEdges = false;

    /**
     * Enable touch inside the SeekArc
     */
    private boolean mTouchInside = true;

    /**
     * Will the progress increase clockwise or anti-clockwise
     */
    private boolean mClockwise = true;

    // Internal variables
    private int mArcRadius = 0;
    private float mProgressLowSweep = 0;
    private float mProgressHighSweep = 0;
    @NonNull
    private RectF mArcRect = new RectF();
    private Paint mArcPaint;
    private Paint mProgressLowPaint;
    private Paint mProgressHighPaint;
    private Paint mDotPaint;
    private Paint mTextPaint;
    private int mTranslateX;
    private int mTranslateY;
    private boolean mDrawPoints = false;
    private float mTouchIgnoreInnerRadius;
    private OnSeekArcChangeListener mOnSeekArcChangeListener;

    private int mLowArcColor = -1;
    private int mHighArcColor = -1;
    private String leftArcText;
    private String rightArcText;

    private ArcElementLocation mThumbLowLoc = new ArcElementLocation(0);
    private ArcElementLocation mThumbHighLoc = new ArcElementLocation(0);

    public interface OnSeekArcChangeListener {

        /**
         * Notification that the progress level has changed. Clients can use the
         * fromUser parameter to distinguish user-initiated changes from those
         * that occurred programmatically.
         *
         * @param seekArc  The SeekArc whose progress has changed
         * @param progress The current progress level. This will be in the range
         *                 0..max where max was set by
         *                 {@link #setMaxValue(int)}. (The default value for
         *                 max is 100.)
         * @param fromUser True if the progress change was initiated by the user.
         */
        void onProgressChanged(DeviceSeekArc seekArc, int thumb, int progress, boolean fromUser);

        /**
         * Notification that the user has started a touch gesture. Clients may
         * want to use this to disable advancing the seekbar.
         *
         * @param seekArc The SeekArc in which the touch gesture began
         * @param thumb The thumb that the user is interacting with
         * @param progress The current progress level
         */
        void onStartTrackingTouch(DeviceSeekArc seekArc, int thumb, int progress);

        /**
         * Notification that the user has finished a touch gesture. Clients may
         * want to use this to re-enable advancing the seekarc.
         *
         * @param seekArc The SeekArc in which the touch gesture began
         * @param thumb The thumb that the user is interacting with
         * @param progress The current progress level
         */
        void onStopTrackingTouch(DeviceSeekArc seekArc, int thumb, int progress);
    }

    public DeviceSeekArc(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    public DeviceSeekArc(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.seekArcStyle);
    }

    public DeviceSeekArc(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    @SuppressWarnings("deprecation")
    private void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {

        Log.d(TAG, "Initialising SeekArc");
        final Resources res = getResources();

        // Defaults, may need to link this into theme settings
        int arcColor = res.getColor(R.color.white_with_10);
        int progressLowColor = res.getColor(R.color.white_with_35);
        int progressHighColor = res.getColor(R.color.white_with_35);
        int thumbHalfheight;
        int thumbHalfWidth;

        mThumbLow = res.getDrawable(R.drawable.seek_arc_control_selector);
        mThumbHigh = res.getDrawable(R.drawable.seek_arc_control_selector);
        mStop = res.getDrawable(R.drawable.seek_arc_control_selector);
        mTextSize =  ImageUtils.dpToPx(DEFAULT_TEXT_SIZE_IN_DP);
        int mDistanceToTop = ImageUtils.dpToPx(DEFAULT_TEXT_DISTANCE_TO_TOP_IN_DP);
        mTextOffset = this.mTextSize + ImageUtils.dpToPx(DEFAULT_TEXT_DISTANCE_TO_BUTTON_IN_DP) + mDistanceToTop;
        mMarkerDrawable = res.getDrawable(R.drawable.shape_temp_indicator);

        mLabelPaint = new TextPaint();
        mLabelPaint.setColor(Color.WHITE);
        mLabelPaint.setAntiAlias(true);
        mLabelPaint.setTextSize(ImageUtils.dpToPx(14));
        mLabelPaint.setFakeBoldText(true);

        if (attrs != null) {
            // Attribute initialization
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.SeekArc, defStyle, 0);

            Drawable thumb = a.getDrawable(R.styleable.SeekArc_thumb);
            if (thumb != null) {
                mThumbLow = thumb;
            }
            thumb = a.getDrawable(R.styleable.SeekArc_thumb);
            if (thumb != null) {
                mThumbHigh = thumb;
            }

            mRangeColor = a.getColor(R.styleable.SeekArc_rangeColor, Color.TRANSPARENT);
            mMax = a.getInteger(R.styleable.SeekArc_max, mMax);
            mProgressLow = a.getInteger(R.styleable.SeekArc_progress, mProgressLow);
            mProgressHigh = a.getInteger(R.styleable.SeekArc_progress, mProgressHigh);
            mProgressWidth = (int) a.getDimension(R.styleable.SeekArc_progressWidth, mProgressWidth);
            mArcWidth = (int) a.getDimension(R.styleable.SeekArc_arcWidth, mArcWidth);

            mStartAngle = a.getInt(R.styleable.SeekArc_startAngle, mStartAngle);
            mSweepAngle = a.getInt(R.styleable.SeekArc_sweepAngle, mSweepAngle);
            mRotation = a.getInt(R.styleable.SeekArc_rotation, mRotation);
            mRoundedEdges = a.getBoolean(R.styleable.SeekArc_roundEdges,
                    mRoundedEdges);
            mTouchInside = a.getBoolean(R.styleable.SeekArc_touchInside,
                    mTouchInside);
            mClockwise = a.getBoolean(R.styleable.SeekArc_clockwise,
                    mClockwise);

            arcColor = a.getColor(R.styleable.SeekArc_arcColor, arcColor);
            progressLowColor = a.getColor(R.styleable.SeekArc_progressColor,
                    progressLowColor);
            progressHighColor = a.getColor(R.styleable.SeekArc_progressColor,
                    progressHighColor);

            mDrawPoints = a.getBoolean(R.styleable.SeekArc_drawPoints, mDrawPoints);

            thumbHalfheight = mProgressWidth / 2;
            thumbHalfWidth = mProgressWidth / 2;
            mThumbLow.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth, thumbHalfheight);
            mThumbHigh.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth, thumbHalfheight);

            a.recycle();
        }

        mProgressLow = (mProgressLow > mMax) ? mMax : mProgressLow;
        mProgressLow = (mProgressLow < 0) ? 0 : mProgressLow;

        mProgressHigh = (mProgressHigh > mMax) ? mMax : mProgressHigh;
        mProgressHigh = (mProgressHigh < 0) ? 0 : mProgressHigh;

        mSweepAngle = (mSweepAngle > 360) ? 360 : mSweepAngle;
        mSweepAngle = (mSweepAngle < 0) ? 0 : mSweepAngle;

        mStartAngle = (mStartAngle > 360) ? 0 : mStartAngle;
        mStartAngle = (mStartAngle < 0) ? 0 : mStartAngle;

        if(mLowArcColor != -1) {
            progressLowColor = mLowArcColor;
        }

        if(mHighArcColor != -1) {
            progressHighColor = mHighArcColor;
        }

        mArcPaint = new Paint();
        mArcPaint.setColor(arcColor);
        mArcPaint.setAntiAlias(true);
        mArcPaint.setFlags(0);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mArcWidth);

        mRangePaint = new Paint();
        mRangePaint.setColor(mRangeColor);
        mRangePaint.setAntiAlias(true);
        mRangePaint.setStyle(Paint.Style.STROKE);
        mRangePaint.setStrokeWidth(mProgressWidth);

        mProgressLowPaint = new Paint();
        mProgressLowPaint.setColor(progressLowColor);
        mProgressLowPaint.setAntiAlias(true);
        mProgressLowPaint.setStyle(Paint.Style.STROKE);
        mProgressLowPaint.setStrokeWidth(mProgressWidth);

        mProgressHighPaint = new Paint();
        mProgressHighPaint.setColor(progressHighColor);
        mProgressHighPaint.setAntiAlias(true);
        mProgressHighPaint.setStyle(Paint.Style.STROKE);
        mProgressHighPaint.setStrokeWidth(mProgressWidth);

        mDotPaint = new Paint();
        mDotPaint.setAntiAlias(true);
        mDotPaint.setColor(Color.WHITE);
        mDotPaint.setStyle(Paint.Style.FILL);

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.GRAY);
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(mTextSize);
        mTextPaint.setStyle(Paint.Style.FILL);

        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressLowPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressHighPaint.setStrokeCap(Paint.Cap.ROUND);
            mDotPaint.setStrokeCap(Paint.Cap.ROUND);
        }

        mLastPressedThumb = mThumbLow;
    }

    private void drawArc(@NonNull Canvas canvas) {
        // Draw the arcs
        int mAngleOffset = -90;
        final int arcStart = mStartAngle + mAngleOffset + mRotation;
        final int arcSweep = mSweepAngle;

        canvas.drawArc(mArcRect, arcStart, arcSweep, false, mArcPaint);
        if (mProgressLowSweep != 0) {
            canvas.drawArc(mArcRect, arcStart, mProgressLowSweep, false, mProgressLowPaint);
        }

        if (mProgressHighSweep != 0) {
            canvas.drawArc(mArcRect, arcStart, mProgressHighSweep, false, mProgressHighPaint);
        }

        if (mRangeSelection) {
            canvas.drawArc(mArcRect, arcStart + mProgressLowSweep, (mProgressHighSweep - mProgressLowSweep), false, mRangePaint);
        }
    }

    private void drawPoints(@NonNull Canvas canvas) {
        int mAngleOffset = -90;

        //Draw points
        if(mDrawPoints) {
            canvas.save();

            int startPointAngle = mStartAngle + mAngleOffset;
            for (int i = 0; i < 11; i++) {
                int x = (int) ( (mArcRadius + mArcWidth)  * Math.cos(Math.toRadians(startPointAngle + i * 30)));
                int y = (int) ( (mArcRadius + mArcWidth)  * Math.sin(Math.toRadians(startPointAngle + i * 30)));
                if (i == 0) {
                    mDotPaint.setColor(Color.argb(30, 0, 0, 0));
                } else {
                    mDotPaint.setColor(Color.argb(i * 25, 255, 255, 255));
                }
                float mPointRadius = ImageUtils.dpToPx(4);
                canvas.drawCircle(mTranslateX - x, mTranslateY - y, mPointRadius, mDotPaint);
            }

            int offX = (int) (mArcRadius *1.05 * Math.cos(Math.toRadians(startPointAngle-25)));
            int offY = (int) (mArcRadius *1.05 * Math.sin(Math.toRadians(startPointAngle-25)));

            int onX = (int) (mArcRadius *1.05 * Math.cos(Math.toRadians(startPointAngle+320)))-5;
            int onY = (int) (mArcRadius *1.05 * Math.sin(Math.toRadians(startPointAngle+320)))-4;

            // Remove this from the seekarc control or allow setting it somewhere?
            int color = mTextPaint.getColor();
            mTextPaint.setColor(Color.WHITE);
            mTextPaint.setTextAlign(Paint.Align.RIGHT);
            if (leftArcText != null) {
                canvas.drawText(leftArcText, mTranslateX - offX, mTranslateY - offY, mTextPaint);
            }
            else {
                canvas.drawText(getResources().getString(R.string.device_arc_default_left), mTranslateX - offX, mTranslateY - offY, mTextPaint);
            }
            mTextPaint.setTextAlign(Paint.Align.LEFT);
            if (rightArcText != null) {
                onX = (int) (mArcRadius *1.05 * Math.cos(Math.toRadians(startPointAngle+327)));
                onY = (int) (mArcRadius *1.05 * Math.sin(Math.toRadians(startPointAngle+327)));
                canvas.drawText(rightArcText, mTranslateX - onX,mTranslateY- onY,mTextPaint);
            }
            else {
                canvas.drawText(getResources().getString(R.string.device_arc_default_right), mTranslateX - onX,mTranslateY- onY,mTextPaint);
            }
            mTextPaint.setColor(color);

            canvas.restore();
        }

    }

    private void drawThumbs(@NonNull Canvas canvas) {
        // Don't draw thumbs if not enabled (and not overridden)
        if (enabled || drawThumbsWhenDisabled) {
            canvas.save();

            // Draw the thumb nail
            int xTranslate = mTranslateX - mThumbLowLoc.x;
            int yTranslate = mTranslateY - mThumbLowLoc.y;
            canvas.translate(xTranslate, yTranslate);
            long rotate = (((mProgressLow - 0) * (90 - 10)) / (100 - 0)) + 10;

            if (mSelectedResource != null) {
                canvas.rotate(-(-((rotate * 360 / 100) - 90) + 90));
            }

            mThumbLow.draw(canvas);

            if (mSelectedResource != null) {
                canvas.rotate(-((rotate * 360 / 100) - 90) + 90);
            }

            // If text is enabled draw the low thumb text
            if (mTextEnabled && mDrawLabelsInsideThumbs) {
                float offset = mTextOffset / 3;
                String lowValueText = String.valueOf(getProgress(THUMB_LOW));
                canvas.drawText(lowValueText, -(offset + (mTextSize / offset * lowValueText.length())), offset, mTextPaint);
            }

            // If range is enabled, draw two thumbs
            if (mRangeSelection) {
                // Undo Translate
                canvas.translate(-xTranslate, -yTranslate);
                xTranslate = mTranslateX - mThumbHighLoc.x;
                yTranslate = mTranslateY - mThumbHighLoc.y;
                canvas.translate(xTranslate, yTranslate);

                rotate = ((mProgressHigh * (90 - 10)) / 100) + 10;

                if (mSelectedResource != null) {
                    canvas.rotate(-(-((rotate * 360 / 100) - 90) + 90));
                }

                mThumbHigh.draw(canvas);

                if (mSelectedResource != null) {
                    canvas.rotate(-((rotate * 360 / 100) - 90) + 90);
                }

                // Draw the high thumb text
                if (mTextEnabled && mDrawLabelsInsideThumbs) {
                    float offset = mTextOffset / 4;
                    String highValueText = String.valueOf(getProgress(THUMB_HIGH));
                    canvas.drawText(highValueText, -(offset + (mTextSize / offset * highValueText.length())), offset, mTextPaint);
                }
            }

            canvas.restore();
        }
    }

    private void drawMarker(@NonNull Canvas canvas) {
        if (mMarkerPosition != null) {
            canvas.save();

            int xTranslate = mTranslateX - mMarkerLoc.x;
            int yTranslate = mTranslateY - mMarkerLoc.y;

            canvas.translate(xTranslate, yTranslate);
            canvas.rotate(mMarkerLoc.relativeAngle + 90);

            // Make marker overhand arc width a few pixels
            int halfMarkerWidth = (mArcWidth + ImageUtils.dpToPx(15)) / 2;
            mMarkerDrawable.setBounds(-halfMarkerWidth, -halfMarkerWidth, halfMarkerWidth, halfMarkerWidth);
            mMarkerDrawable.draw(canvas);

            canvas.restore();
        }
    }

    private void drawLabels(@NonNull Canvas canvas) {

        RectF markerLabelBounds = null;
        CharSequence markerText = null;

        if (mTextEnabled) {

            if (mMarkerPosition != null) {
                canvas.save();

                markerText = String.valueOf(calculateNearestValue(mMarkerPosition));
                if (mLabelTransformer != null) {
                    markerText = mLabelTransformer.getDisplayedLabel(markerText.toString());
                }

                Rect markerLabelLoc = getLocationForLabel(mMarkerLoc, 5, markerText.toString());

                int left = mTranslateX - markerLabelLoc.left;
                int top = mTranslateY - markerLabelLoc.top;

                StaticLayout staticLayout = new StaticLayout(markerText, mLabelPaint, (int) mLabelPaint.measureText(markerText.toString()), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
                markerLabelBounds = new RectF(left, top, left + staticLayout.getWidth(), top + staticLayout.getHeight());

                canvas.translate(left, top);
                staticLayout.draw(canvas);
                canvas.restore();
            }

            if (enabled && !mDrawLabelsInsideThumbs && mRangeSelection) {
                canvas.save();

                CharSequence  thumbHighText = String.valueOf(getProgress(THUMB_HIGH));
                if (mLabelTransformer != null) {
                    thumbHighText = mLabelTransformer.getDisplayedLabel(thumbHighText.toString());
                }

                // Don't draw thumb label if it's the same text as the marker
                if (markerText == null || !thumbHighText.equals(markerText)) {
                    drawLabel(canvas, mThumbHighLoc, thumbHighText, mProgressHigh, markerLabelBounds);
                }

                canvas.restore();
            }

            if (enabled && !mDrawLabelsInsideThumbs) {
                canvas.save();

                CharSequence thumbLowText = String.valueOf(getProgress(THUMB_LOW));
                if (mLabelTransformer != null) {
                    thumbLowText = mLabelTransformer.getDisplayedLabel(thumbLowText.toString());
                }

                if (markerText == null || !thumbLowText.equals(markerText)) {
                    drawLabel(canvas, mThumbLowLoc, thumbLowText, mProgressLow, markerLabelBounds);
                }

                canvas.restore();
            }
        }

    }

    private RectF drawLabel(@NonNull Canvas canvas, ArcElementLocation anchoredAt, CharSequence text, int forProgress, RectF keepOut) {

        int labelCollisionOffset = 0;
        RectF labelBounds;
        StaticLayout staticLayout;
        Rect labelLoc;

        do {
            ArcElementLocation anchor = new ArcElementLocation(anchoredAt.absoluteAngle + labelCollisionOffset);
            labelLoc = getLocationForLabel(anchor, 0, text.toString());

            int left = mTranslateX - labelLoc.left;
            int top = mTranslateY - labelLoc.top;

            staticLayout = new StaticLayout(text, mLabelPaint, (int) mLabelPaint.measureText(text.toString()), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0, false);
            labelBounds = new RectF(left, top, left + staticLayout.getWidth(), top + staticLayout.getHeight());

            if (mMarkerPosition != null && forProgress < mMarkerPosition) {
                labelCollisionOffset--;
            } else {
                labelCollisionOffset++;
            }

        } while (keepOut != null && labelBounds.intersect(keepOut));

        canvas.translate(mTranslateX - labelLoc.left, mTranslateY - labelLoc.top);
        staticLayout.draw(canvas);

        return labelBounds;
    }

    private Rect getLocationForLabel(ArcElementLocation anchor, int paddingDp, String labelText) {

        int textWidth = (int) mLabelPaint.measureText(labelText);
        int textHeight = (int) mLabelPaint.getTextSize();

        float xRadiusOffset = ImageUtils.dpToPx(paddingDp) + textWidth + (mProgressWidth / 2);
        float yRadiusOffset = mProgressWidth / 2 + textHeight;
        ArcElementLocation labelLocation = new ArcElementLocation(anchor.absoluteAngle, xRadiusOffset, yRadiusOffset);

        int x = labelLocation.x;
        int y = labelLocation.y + textHeight / 2;

        if (anchor.relativeAngle > 180) {
            x += textWidth / 2;
        }

        return new Rect(x, y, x + textWidth, y + textHeight);
    }

    private void drawGradient(@NonNull Canvas canvas) {
        if (mHiliteGradient != null && isEnabled()) {
            canvas.save();

            // Manual gradient positions
            float startPosition = mHiliteGradient.startPosition;
            float endPosition = mHiliteGradient.endPosition;

            // Dynamic gradient start position
            if (mHiliteGradient.startMarker) {
                startPosition = getDrawnMarkerPosition();
            } else if (mHiliteGradient.startThumb == THUMB_LOW) {
                startPosition = mProgressLow;
            } else if (mHiliteGradient.startThumb == THUMB_HIGH) {
                startPosition = mProgressHigh;
            }

            // Dynamic gradient end position
            if (mHiliteGradient.endMarker) {
                endPosition = getDrawnMarkerPosition();
            } else if (mHiliteGradient.endThumb == THUMB_LOW) {
                endPosition = mProgressLow;
            } else if (mHiliteGradient.endThumb == THUMB_HIGH) {
                endPosition = mProgressHigh;
            }

            // Fix inverted positions to prevent weird gradient renderings
            if (endPosition < startPosition) {
                float swap = endPosition;
                startPosition = endPosition;
                endPosition = swap;
            }

            // Calculate arc relativeAngle at which gradient starts and ends
            float startAngle = ((startPosition / mMax) * mSweepAngle) + mStartAngle;
            float endAngle = ((endPosition / mMax) * mSweepAngle) + mStartAngle;

            Paint hilitePaint = mHiliteGradient.getHiliteStrokePaint(startAngle, endAngle, mArcRect, mProgressWidth);
            canvas.drawArc(mArcRect, startAngle - 90 + mRotation, endAngle - startAngle, false, hilitePaint);

            canvas.restore();
        }
    }

    private void drawStops(@NonNull Canvas canvas) {

        int halfStopSize = ImageUtils.dpToPx(4);
        mStop.setBounds(-halfStopSize, -halfStopSize, halfStopSize, halfStopSize);

        if (mMinStop != null) {
            canvas.save();

            int xTranslate = mTranslateX - mMinStopLoc.x;
            int yTranslate = mTranslateY - mMinStopLoc.y;
            canvas.translate(xTranslate, yTranslate);

            mStop.draw(canvas);
            canvas.restore();
        }

        if (mMaxStop != null) {
            canvas.save();

            int xTranslate = mTranslateX - mMaxStopLoc.x;
            int yTranslate = mTranslateY - mMaxStopLoc.y;
            canvas.translate(xTranslate, yTranslate);

            mStop.draw(canvas);
            canvas.restore();
        }

    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        if (!mClockwise) {
            canvas.scale(-1, 1, mArcRect.centerX(), mArcRect.centerY());
        }

        drawArc(canvas);            // Circular seek track and progress sweep
        drawGradient(canvas);       // Hilite gradient
        drawMarker(canvas);         // Marker indicator (i.e., current temperature line)
        drawPoints(canvas);         // Dots on outside of track
        drawThumbs(canvas);         // Drag handle(s)
        drawLabels(canvas);         // Thumb and marker labels
        drawStops(canvas);          // Draw min/max stop dots
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if (mUseFixedSize && getLayoutParams().width > 0 && getLayoutParams().height > 0) {
            setMeasuredDimension(getLayoutParams().width|MeasureSpec.EXACTLY, getLayoutParams().height|MeasureSpec.EXACTLY);
        }

        final int height = getMeasuredHeight();
        final int width = getMeasuredWidth();
        final int min = Math.min(width, height);

        mTranslateX = width / 2;
        mTranslateY = height / 2;

        int arcDiameter = min - getPaddingLeft();
        mArcRadius = arcDiameter / 2;
        float top = height / 2 - (arcDiameter / 2);
        float left = width / 2 - (arcDiameter / 2);
        mArcRect.set(left, top, left + arcDiameter, top + arcDiameter);

        mThumbLowLoc.setLocation((int) mProgressLowSweep);
        mThumbHighLoc.setLocation((int) mProgressHighSweep);

        // Null marker position means marker is not drawn
        if (mMarkerPosition != null) {
            mMarkerLoc.setLocation((int) ((float) getDrawnMarkerPosition() / mMax * mSweepAngle));
        }

        if (mMinStop != null) {
            mMinStopLoc.setLocation((int) ((float) mMinStop / mMax * mSweepAngle));
        }

        if (mMaxStop != null) {
            mMaxStopLoc.setLocation((int) ((float) mMaxStop / mMax * mSweepAngle));
        }

        setTouchInSide(mTouchInside);
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        if (!enabled) return true;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                mPressedThumb = evalPressedThumb(event.getX(), event.getY());

                // Only handle thumb presses.
                if (mPressedThumb == null && (!mIsClickToSeek || mRangeSelection)) {
                    return super.onTouchEvent(event);
                }

                onStartTrackingTouch();
                updateOnTouch(event);
                attemptClaimDrag();
                break;
            case MotionEvent.ACTION_MOVE:
                updateOnTouch(event);
                break;
            case MotionEvent.ACTION_UP:

                mPressedThumb = null;

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

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (mThumbLow != null && mThumbLow.isStateful()) {
            int[] state = getDrawableState();
            mThumbLow.setState(state);
        }

        if (mThumbHigh != null && mThumbHigh.isStateful()) {
            int[] state = getDrawableState();
            mThumbHigh.setState(state);
        }
        invalidate();
    }

    private void onStartTrackingTouch() {
        mThumbBeingDragged = mLastPressedThumb == mThumbLow ? THUMB_LOW : THUMB_HIGH;
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener.onStartTrackingTouch(this, mLastPressedThumb == mThumbLow ? THUMB_LOW : THUMB_HIGH, getProgress(THUMB_LOW));
        }
    }

    private void onStopTrackingTouch() {
        mThumbBeingDragged = THUMB_NONE;
        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener.onStopTrackingTouch(this, mLastPressedThumb == mThumbLow ? THUMB_LOW : THUMB_HIGH, getProgress(THUMB_LOW));
        }
    }

    private void updateOnTouch(@NonNull MotionEvent event) {
        if (!mRangeSelection) {
            boolean ignoreTouch = ignoreTouch(event.getX(), event.getY());
            if (ignoreTouch) {
                return;
            }
        }
        setPressed(true);
        double mTouchAngle = getTouchDegrees(event.getX(), event.getY());

        int progress = getProgressForAngle(mTouchAngle);

        // Keep thumbs inside of stops (when defined)
        if (mMinStop != null && progress < mMinStop) {
            progress = mMinStop;
        } else if (mMaxStop != null && progress > mMaxStop) {
            progress = mMaxStop;
        }

        // Determine which progress has been effected?
        if (mPressedThumb == mThumbLow || !mRangeSelection) {
            // Set the drawable resource
            if (mSelectedResource != null) mThumbLow = mSelectedResource;
            if (mUnSelectedResource != null) mThumbHigh = mUnSelectedResource;

            mPressedThumb = mThumbLow;
            mLastPressedThumb = mPressedThumb;

            // Update progress
            mThumbBeingDragged = THUMB_LOW;
            onProgressRefresh(THUMB_LOW, progress, true);

        } else if (mPressedThumb.equals(mThumbHigh)) {
            // Set the drawable resource
            if (mSelectedResource != null) mThumbHigh = mSelectedResource;
            if (mUnSelectedResource != null) mThumbLow = mUnSelectedResource;

            mPressedThumb = mThumbHigh;
            mLastPressedThumb = mPressedThumb;

            // Update progress
            mThumbBeingDragged = THUMB_HIGH;
            onProgressRefresh(THUMB_HIGH, progress, true);
        }
    }

    // Update ignore to allow touches inside or outside of the arc
    private boolean ignoreTouch(float xPos, float yPos) {
        boolean ignore = false;
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        float touchRadius = (float) Math.sqrt(((x * x) + (y * y)));
        if (!mTouchInside
                && (touchRadius < mTouchIgnoreInnerRadius
                || touchRadius > mTouchIgnoreInnerRadius + mArcWidth)) { // If touch is inside of the circle
            ignore = true;
        }

        return ignore;
    }

    private Drawable evalPressedThumb(float xPos, float yPos) {
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;

        if (insideThumb(mThumbLow, mThumbLowLoc.x, mThumbLowLoc.y, x, y)) return mThumbLow;
        else if (mRangeSelection && insideThumb(mThumbHigh, mThumbHighLoc.x, mThumbHighLoc.y, x, y))
            return mThumbHigh;

        return null;
    }

    private boolean insideThumb(@NonNull Drawable thumb, int thumbXPos, int thumbYPos, float x, float y) {
        double FATFINGER_CONST = 1.0;

        int thumbWidth = thumb.getBounds().width();
        int thumbHeight = thumb.getBounds().height();

        int halfWidth = thumbWidth / 2;
        int halfHeight = thumbHeight / 2;

        boolean betweenX = -x >= (thumbXPos - halfWidth) - (thumbWidth * FATFINGER_CONST) && -x <= (thumbXPos - halfWidth) + (thumbWidth * FATFINGER_CONST);
        boolean betweenY = -y >= (thumbYPos - halfHeight) - (thumbHeight * FATFINGER_CONST) && -y <= (thumbYPos - halfHeight) + (thumbHeight * FATFINGER_CONST);

        return betweenX && betweenY;
    }

    /**
     * Tries to claim the user's drag motion, and requests disallowing any ancestors from stealing events in the drag.
     */
    private void attemptClaimDrag() {
        if (getParent() != null) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
    }

    private double getTouchDegrees(float xPos, float yPos) {
        float x = xPos - mTranslateX;
        float y = yPos - mTranslateY;
        //invert the x-coord if we are rotating anti-clockwise
        x = (mClockwise) ? x : -x;
        // convert to arc Angle
        double angle = Math.toDegrees(Math.atan2(y, x) + (Math.PI / 2)
                - Math.toRadians(mRotation));
        if (angle < 0) {
            angle = 360 + angle;
        }
        angle -= mStartAngle;
        return angle;
    }

    private int getProgressForAngle(double angle) {
        int touchProgress = (int) Math.round(valuePerDegree() * angle);

        touchProgress = (touchProgress < 0) ? INVALID_PROGRESS_VALUE
                : touchProgress;
        touchProgress = (touchProgress > mMax) ? INVALID_PROGRESS_VALUE
                : touchProgress;
        return touchProgress;
    }

    private float valuePerDegree() {
        return (float) mMax / mSweepAngle;
    }

    private void onProgressRefresh(int thumb, int progress, boolean fromUser) {
        updateProgress(thumb, progress, fromUser);
    }

    private void updateThumbPosition() {
        mThumbLowLoc.setLocation((int) mProgressLowSweep);

        if (mRangeSelection) {
            mThumbHighLoc.setLocation((int) mProgressHighSweep);
        }
    }

    private void updateProgress(int thumb, int progress, boolean fromUser) {

        if (progress == INVALID_PROGRESS_VALUE) {
            return;
        }

        if (mOnSeekArcChangeListener != null) {
            mOnSeekArcChangeListener
                    .onProgressChanged(this, thumb, calculateNearestValue(progress), fromUser);
        }

        // MixMax Check
        progress = (progress > mMax) ? mMax : progress;
        progress = (progress < 0) ? 0 : progress;

        // Range Distance
        if (mRangeSelection && fromUser) {
            if (thumb == THUMB_LOW) {
                progress = (progress > calculateRangeValue(calculateNearestValue(mProgressHigh) - mMinRangeDistance)) ? mProgressLow : progress;
            } else if (thumb == THUMB_HIGH) {
                progress = (progress < calculateRangeValue(calculateNearestValue(mProgressLow) + mMinRangeDistance)) ? mProgressHigh : progress;
            }
        }

        // Check stops
        if (mMinStop != null && fromUser) {
            progress = progress < mMinStop ? mMinStop : progress;
        }

        if (mMaxStop != null && fromUser) {
            progress = progress > mMaxStop ? mMaxStop : progress;
        }

        if (thumb == THUMB_LOW) {
            progress = (mProgressLow < 0) ? 0 : progress;
            mProgressLow = progress;
            mProgressLowSweep = getAngleForProgress(progress);
        } else if (mRangeSelection && thumb == THUMB_HIGH) {
            progress = (mProgressHigh < 0) ? 0 : progress;
            mProgressHigh = progress;
            mProgressHighSweep = getAngleForProgress(progress);
        }

        updateThumbPosition();

        invalidate();
    }

    private float getAngleForProgress(float progress) {
        return progress / mMax * mSweepAngle;
    }

    /**
     * Sets a listener to receive notifications of changes to the SeekArc's
     * progress level. Also provides notifications of when the user starts and
     * stops a touch gesture within the SeekArc.
     *
     * @param l The seek bar notification listener
     * @see DeviceSeekArc.OnSeekArcChangeListener
     */
    public void setOnSeekArcChangeListener(OnSeekArcChangeListener l) {
        mOnSeekArcChangeListener = l;
    }

    public void setProgress(int thumb, int progress) {
        setProgress(thumb, progress, false);
    }

    public void setProgress(int thumb, int progress, boolean fromUser) {

        // Don't jerk thumbs around if user is in process of dragging
        if (thumb == mThumbBeingDragged) {
            return;
        }

        // Don't update if value isn't changing
        if ((thumb == THUMB_LOW && calculateNearestValue(mProgressLow) == progress) || (thumb == THUMB_HIGH && calculateNearestValue(mProgressHigh) == progress)) {
            return;
        }

        updateProgress(thumb, calculateRangeValue(progress), fromUser);
    }

    // Uncalculated progress of the lowest value
    public int getProgress() {
        return mProgressLow;
    }

    // Calculated Progress
    public int getProgress(int thumb) {
        return calculateNearestValue((thumb == THUMB_HIGH) ? mProgressHigh : mProgressLow);
    }

    public void setActiveProgress(int thumb) {
        if (thumb == THUMB_LOW) {
            mLastPressedThumb = mThumbLow;
            if (mOnSeekArcChangeListener != null) {
                mOnSeekArcChangeListener
                        .onProgressChanged(this, thumb, getProgress(thumb), false);
            }
        } else if (thumb == THUMB_HIGH) {
            mLastPressedThumb = mThumbHigh;
            if (mOnSeekArcChangeListener != null) {
                mOnSeekArcChangeListener
                        .onProgressChanged(this, thumb, getProgress(thumb), false);
            }
        }
    }

    public int getSelectedProgress() {
        if(mThumbLow == mSelectedResource) {
            return THUMB_LOW;
        }
        else if(mThumbHigh == mSelectedResource) {
            return THUMB_HIGH;
        }
        return -1;
    }

    public int getActiveProgress() {
        return mLastPressedThumb == mThumbLow ? THUMB_LOW : THUMB_HIGH;
    }

    public void incrementActiveProgress() {
        if (mLastPressedThumb == mThumbLow)
            updateProgress(THUMB_LOW, calculateRangeValue(calculateNearestValue(mProgressLow) + 1), true);
        else if (mLastPressedThumb == mThumbHigh)
            updateProgress(THUMB_HIGH, calculateRangeValue(calculateNearestValue(mProgressHigh) + 1), true);
    }

    public void decrementActiveProgress() {
        if (mLastPressedThumb == mThumbLow)
            updateProgress(THUMB_LOW, calculateRangeValue(calculateNearestValue(mProgressLow) - 1), true);
        else if (mLastPressedThumb == mThumbHigh)
            updateProgress(THUMB_HIGH, calculateRangeValue(calculateNearestValue(mProgressHigh) - 1), true);
    }

    public int getProgressWidth() {
        return mProgressWidth;
    }

    public void setProgressWidth(int mProgressWidth) {
        this.mProgressWidth = mProgressWidth;
        mProgressLowPaint.setStrokeWidth(mProgressWidth);
        mProgressHighPaint.setStrokeWidth(mProgressWidth);
    }

    public int getArcWidth() {
        return mArcWidth;
    }

    public void setArcWidth(int mArcWidth) {
        this.mArcWidth = mArcWidth;
        mArcPaint.setStrokeWidth(mArcWidth);
    }

    public int getArcRotation() {
        return mRotation;
    }

    public void setArcRotation(int mRotation) {
        this.mRotation = mRotation;
        updateThumbPosition();
    }

    public int getStartAngle() {
        return mStartAngle;
    }

    public void setStartAngle(int mStartAngle) {
        this.mStartAngle = mStartAngle;
        updateThumbPosition();
    }

    public int getSweepAngle() {
        return mSweepAngle;
    }

    public void setSweepAngle(int mSweepAngle) {
        this.mSweepAngle = mSweepAngle;
        updateThumbPosition();
    }

    public void setRoundedEdges(boolean isEnabled) {
        mRoundedEdges = isEnabled;
        if (mRoundedEdges) {
            mArcPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressLowPaint.setStrokeCap(Paint.Cap.ROUND);
            mProgressHighPaint.setStrokeCap(Paint.Cap.ROUND);
        } else {
            mArcPaint.setStrokeCap(Paint.Cap.SQUARE);
            mProgressLowPaint.setStrokeCap(Paint.Cap.SQUARE);
            mProgressHighPaint.setStrokeCap(Paint.Cap.SQUARE);
        }
    }

    public void setTouchInSide(boolean isEnabled) {
        int thumbHalfheight = mThumbLow.getIntrinsicHeight() / 2;
        int thumbHalfWidth = mThumbLow.getIntrinsicWidth() / 2;
        mTouchInside = isEnabled;
        if (mTouchInside) {
            mTouchIgnoreInnerRadius = (float) mArcRadius / 4;
        } else {
            // Don't use the exact radius makes interaction too tricky
            mTouchIgnoreInnerRadius = mArcRadius - Math.min(thumbHalfWidth, thumbHalfheight);
        }
    }

    public void setClockwise(boolean isClockwise) {
        mClockwise = isClockwise;
    }

    public void setProgressColor(int thumb, int color) {
        if (thumb == THUMB_LOW) mProgressLowPaint.setColor(color);
        else if (thumb == THUMB_HIGH) mProgressHighPaint.setColor(color);
    }

    public void setRangeEnabled(boolean isEnabled) {
        mRangeSelection = isEnabled;
    }

    public boolean isRangeEnabled() {
        return mRangeSelection;
    }

    public void setTextEnabled(boolean isEnabled) {
        mTextEnabled = isEnabled;
    }


    public void setMinValue(int mMinValue) {
        this.mMinValue = mMinValue;
    }

    public void setMaxValue(int mMaxValue) {
        this.mMaxValue = mMaxValue;
    }

    // Convert 0-100 to Device Range
    private int calculateNearestValue(int progress) {
        //NewValue = (((OldValue - OldMin) * (NewMax - NewMin)) / (OldMax - OldMin)) + NewMin
        return (int) remap(progress, 0f, 100f, mMinValue, mMaxValue);
    }

    // Convert device range to 0-100
    private int calculateRangeValue(int progress) {
        //NewValue = (((OldValue - OldMin) * (NewMax - NewMin)) / (OldMax - OldMin)) + NewMin
        return (int)remap(progress, mMinValue, mMaxValue, 0f, 100f);
    }

    private float remap(float x, float oMin, float oMax, float nMin, float nMax ){

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

        return Math.round(result);
    }

    public void setSelectedResource(int res, @Nullable Rect bounds) {
        mSelectedResource = getResources().getDrawable(res);
        if (bounds != null) mSelectedResource.setBounds(bounds);
        else {
            int thumbHalfheight = mSelectedResource.getIntrinsicHeight() / 4;
            int thumbHalfWidth = mSelectedResource.getIntrinsicWidth() / 4;
            mSelectedResource.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth,
                    thumbHalfheight);
        }
        mThumbLow = mSelectedResource;
        mLastPressedThumb = mThumbLow;
        invalidate();
    }

    public void setUnSelectedResource(int res, @Nullable Rect bounds) {
        mUnSelectedResource = getResources().getDrawable(res);
        if (bounds != null) mSelectedResource.setBounds(bounds);
        {
            int thumbHalfheight = mUnSelectedResource.getIntrinsicHeight() / 4;
            int thumbHalfWidth = mUnSelectedResource.getIntrinsicWidth() / 4;
            mUnSelectedResource.setBounds(-thumbHalfWidth, -thumbHalfheight, thumbHalfWidth,
                    thumbHalfheight);
        }
        mThumbHigh = mUnSelectedResource;
        invalidate();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        invalidate();
    }

    public int getMinRangeDistance() {
        return mMinRangeDistance;
    }

    public void setMinRangeDistance(int distance) {
        mMinRangeDistance = distance;
    }

    public void setLeftArcText(String left) {
        leftArcText = left;
    }

    public void setRightArcText(String right) {
        rightArcText = right;
    }

    public void setDrawThumbsWhenDisabled (boolean drawThumbsWhenDisabled) {
        this.drawThumbsWhenDisabled = drawThumbsWhenDisabled;
    }

    public void setLowArcColor(int mLowArcColor) {
        this.mLowArcColor = mLowArcColor;
        if(mProgressLowPaint != null) {
            mProgressLowPaint.setColor(this.mLowArcColor);
            invalidate();
        }
    }

    public void setHighArcColor(int mHighArcColor) {
        this.mHighArcColor = mHighArcColor;
        if(mProgressHighPaint != null) {
            mProgressHighPaint.setColor(this.mHighArcColor);
            invalidate();
        }
    }

    public void setDrawPoints(boolean drawPoints) {
        mDrawPoints = drawPoints;
    }

    public void setUseFixedSize(boolean useFixedSize) {
        mUseFixedSize = useFixedSize;
    }

    public void setMarkerPosition(Integer position) {
        mMarkerPosition = position == null ? null : calculateRangeValue(position);
        invalidate();
    }

    public int getMarkerPosition() {
        return mMarkerPosition;
    }

    public int getDrawnMarkerPosition() {
        int drawnMarkPos = (mMarkerPosition > calculateRangeValue(mMaxValue)) ? calculateRangeValue(mMaxValue) : mMarkerPosition;
        drawnMarkPos = (drawnMarkPos < calculateRangeValue(mMinValue)) ? calculateRangeValue(mMinValue) : drawnMarkPos;

        return drawnMarkPos;
    }

    public void setArcHiliteGradient(ArcHiliteGradient hiliteGradient) {
        mHiliteGradient = hiliteGradient;
    }

    public ArcHiliteGradient getArcHiliteGradient() {
        return mHiliteGradient;
    }

    public void setMinimumStopValue(Integer minimumStopValue) {
        mMinStop = minimumStopValue == null ? null : calculateRangeValue(minimumStopValue);
        invalidate();
    }

    public Integer getMinimumStopValue() {
        return mMinStop;
    }

    public void setMaximumStopValue(Integer maximumStopValue) {
        mMaxStop = maximumStopValue == null ? null : calculateRangeValue(maximumStopValue);
        invalidate();
    }

    public Integer getMaximumStopValue() {
        return mMaxStop;
    }

    public void setDrawLabelsInsideThumbs(boolean drawLabelsInsideThumbs) {
        mDrawLabelsInsideThumbs = drawLabelsInsideThumbs;
    }

    public boolean isDrawLabelsInsideThumbs() {
        return mDrawLabelsInsideThumbs;
    }

    public void setLabelTextTransformer(LabelTextTransform transformer) {
        mLabelTransformer = transformer;
    }

    public LabelTextTransform getLabelTransformer() {
        return mLabelTransformer;
    }

    public interface LabelTextTransform {
        CharSequence getDisplayedLabel(String forValue);
    }

    public Integer getRangeColor() {
        return mRangeColor;
    }

    public void setRangeColor(int rangeColor) {
        this.mRangeColor = rangeColor;
    }

    public boolean isDragging() {
        return mThumbBeingDragged != THUMB_NONE;
    }

    public boolean isClickToSeek() {
        return mIsClickToSeek;
    }

    public void setClickToSeek(boolean clickToSeek) {
        this.mIsClickToSeek = clickToSeek;
    }

    public class ArcElementLocation {
        public int x;
        public int y;
        public int relativeAngle;
        public int absoluteAngle;

        public ArcElementLocation(int atAngle, float radiusOffsetX, float radiusOffsetY) {
            setLocation(atAngle, radiusOffsetX, radiusOffsetY);
        }

        public ArcElementLocation(int atAngle) {
            setLocation(atAngle, 0, 0);
        }

        public void setLocation(int atAngle) {
            setLocation(atAngle, 0, 0);
        }

        public void setLocation(int atAngle, float radiusOffsetX, float radiusOffsetY) {
            this.absoluteAngle = atAngle;
            this.relativeAngle = atAngle + mStartAngle + mRotation + 90;
            this.x = (int) ((radiusOffsetX + mArcRadius) * Math.cos(Math.toRadians(this.relativeAngle)));
            this.y = (int) ((radiusOffsetY + mArcRadius) * Math.sin(Math.toRadians(this.relativeAngle)));
        }
    }

    public static class ArcHiliteGradient {
        public final int startColor;                // Gradient start color
        public final int endColor;                  // Gradient end color

        protected int startPosition;                // Start gradient at specific position (i.e., percent)
        protected int endPosition;                  // End gradient at specific position
        protected int startThumb = THUMB_NONE;      // Start gradient at a thumb (THUMB_LOW or THUMB_HIGH)
        protected int endThumb = THUMB_NONE;        // End gradient at a thumb
        protected boolean startMarker;              // Start gradient at the marker
        protected boolean endMarker;                // End gradient at marker

        public ArcHiliteGradient(int startColor, int endColor) {
            this.startColor = startColor;
            this.endColor = endColor;
        }

        public static ArcHiliteGradient forNotCooling() {
            ArcHiliteGradient gradient = new ArcHiliteGradient(ArcusApplication.getContext().getResources().getColor(R.color.white_with_60), ArcusApplication.getContext().getResources().getColor(R.color.white_with_60));
            gradient.endThumb = THUMB_LOW;
            gradient.startMarker = true;
            return gradient;
        }

        public static ArcHiliteGradient forNotHeating() {
            ArcHiliteGradient gradient = new ArcHiliteGradient(ArcusApplication.getContext().getResources().getColor(R.color.white_with_60), ArcusApplication.getContext().getResources().getColor(R.color.white_with_60));
            gradient.startThumb = THUMB_LOW;
            gradient.endMarker = true;
            return gradient;
        }

        public static ArcHiliteGradient forHeating() {
            ArcHiliteGradient gradient = new ArcHiliteGradient(ArcusApplication.getContext().getResources().getColor(R.color.heat_gradient_orange), ArcusApplication.getContext().getResources().getColor(R.color.heat_gradient_red));
            gradient.startMarker = true;
            gradient.endThumb = THUMB_LOW;
            return gradient;
        }

        public static ArcHiliteGradient forCooling(boolean inAutoMode) {
            ArcHiliteGradient gradient = new ArcHiliteGradient(ArcusApplication.getContext().getResources().getColor(R.color.cool_gradient_blue), ArcusApplication.getContext().getResources().getColor(R.color.cool_gradient_white));
            gradient.startThumb = inAutoMode ? THUMB_HIGH : THUMB_LOW;
            gradient.endMarker = true;
            return gradient;
        }

        private Paint getHiliteStrokePaint(float startAngle, float endAngle, RectF seekArcBounds, int strokeWidth) {
            // Create the gradient shader
            int[] colors = {startColor, startColor, endColor, endColor};
            float[] positions = {0, startAngle / 360f, endAngle / 360f, 1};
            SweepGradient gradient = new SweepGradient(seekArcBounds.left + seekArcBounds.width() / 2, seekArcBounds.top + seekArcBounds.height() / 2, colors , positions);

            // Rotate to match the orientation of the seek arc
            Matrix theMatrix = new Matrix();
            theMatrix.setRotate(90, seekArcBounds.left + seekArcBounds.width() / 2, seekArcBounds.top + seekArcBounds.height() / 2);
            gradient.setLocalMatrix(theMatrix);

            // Create the paint
            Paint p = new Paint();
            p.setStrokeWidth(strokeWidth);
            p.setShader(gradient);
            p.setAntiAlias(true);
            p.setStyle(Paint.Style.STROKE);

            return p;
        }
    }
}
