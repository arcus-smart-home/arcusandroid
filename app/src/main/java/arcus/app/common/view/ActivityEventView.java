/*
 *  Copyright 2019 Arcus Project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package arcus.app.common.view;

import android.annotation.TargetApi;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

import arcus.cornea.subsystem.care.model.ActivityLine;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ActivityEventView extends ImageView {
    protected final List<ActivityLine> activityIntervals = Collections.synchronizedList(new ArrayList<ActivityLine>());

    protected static final long THIRTY_MIN_IN_MILLIS = TimeUnit.MINUTES.toMillis(30);
    protected long VIEWPORT_WIDTH = TimeUnit.HOURS.toMillis(2);
    protected static final int BEFORE_LEFT_BOUND = 0;
    protected static final int AFTER_RIGHT_BOUND = -1;
    protected static final boolean DEBUG = false;
    protected static final String AM = "AM";
    protected static final String PM = "PM";
    protected static final String STAR_TEXT = new String(new char[] { 0x00B0 });

    protected final Paint borderAndLinePaintBrush   = new Paint();
    protected final Paint eventPaintBrush           = new Paint();
    protected final TextPaint textPaintBrush        = new TextPaint(new Paint());
    protected final Paint alphaPaintBrush           = new Paint();

    protected final Rect normalTextBounds = new Rect();

    protected final Calendar calendarEndTime = Calendar.getInstance();
    protected final Calendar hoursCalendar = Calendar.getInstance();
    protected final int WHITE = Color.WHITE;
    protected final int BLACK = Color.BLACK;
    protected final int fullAlpha     = 255;
    protected final int eightyPercent = Math.round(255f *.8f);
    protected final int sixtyPercent  = Math.round(255f *.6f);
    protected final int fortyPercent  = Math.round(255f *.4f);
    protected final int twentyPercent = Math.round(255f *.2f);
    protected long maxRightBound;
    protected long minLeftBound;
    protected boolean useAxisAlpha;
    protected boolean useContentAlpha;
    protected float borderRadius;         // Border radius size
    protected boolean shouldDrawAxis;     // If axis divider line should be drawn
    protected boolean shouldDrawBorder;   // If a border should be drawn.
    protected int canvasWidth;            // pixels wide the canvas is.
    protected int canvasHeight;           // Height of the canvas in pixels
    protected float superScriptScaleFactor; // What % of the textSP height should superscript fonts be?
    protected float textSPHeight;         // Height of the X-Axis text labels
    protected int axisSizePX;             // X-Axis label area size
    protected float bucketSize;           // Width of the event lines
    protected long startTime;             // Start time of the graph
    protected long endTime;               // End time of the graph
    protected float halfSolidLineStroke;  // 1/2 of solid line stroke, used to inset border drawing (else it gets clipped).
    protected RectF borderRectangle;      // Used to draw a rounded border if one is desired.

    public ActivityEventView(Context context) {
        super(context);
        init(context);
    }

    public ActivityEventView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ActivityEventView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21)
    public ActivityEventView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    protected void init(Context context) {
        canvasHeight = 1;
        canvasWidth = 1;

        // TODO: Set font typeface
        textPaintBrush.setColor(WHITE);
        textPaintBrush.setAlpha(sixtyPercent);
        setTextHeightInSP(11);

        eventPaintBrush.setColor(WHITE);
        // If this has an opacity may need to change the +1 for width so these don't overlap
        eventPaintBrush.setStyle(Paint.Style.STROKE);

        borderAndLinePaintBrush.setStrokeWidth(2);
        borderAndLinePaintBrush.setColor(WHITE);
        borderAndLinePaintBrush.setDither(true);
        borderAndLinePaintBrush.setStyle(Paint.Style.STROKE);
        borderAndLinePaintBrush.setStrokeJoin(Paint.Join.ROUND);
        borderAndLinePaintBrush.setStrokeCap(Paint.Cap.ROUND);
        borderAndLinePaintBrush.setPathEffect(new CornerPathEffect(10));
        borderAndLinePaintBrush.setAntiAlias(true);
        borderRectangle = new RectF();
        halfSolidLineStroke = borderAndLinePaintBrush.getStrokeWidth() / 2;

        alphaPaintBrush.setColor(BLACK);
        alphaPaintBrush.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, getResources().getDisplayMetrics()));
        alphaPaintBrush.setStyle(Paint.Style.FILL);

        setSuperscriptScaleFactor(.65f);
        setAxisSizeDP(15);
        setBucketSize(5f);

        setEndTime(System.currentTimeMillis());
        setShouldDrawBorder(true);
        setShouldDrawAxis(false);
        setBorderRadius(5);

        setUseAxisAlpha(false);
        setUseContentAlpha(false);
    }

    /**
     * Sets the border line width to be stroked
     *
     * @param lineWidth the border line width to be stroked
     */
    public void setBorderLineWidth(@FloatRange(from = 1) float lineWidth) {
        borderAndLinePaintBrush.setStrokeWidth(lineWidth);
        halfSolidLineStroke = borderAndLinePaintBrush.getStrokeWidth() / 2;
    }

    /**
     * Set what % of the textSP height should superscript fonts be
     *
     * @param factor % of the textSP height should superscript fonts are
     */
    public void setSuperscriptScaleFactor(@FloatRange(from = .1, to = 1) float factor) {
        superScriptScaleFactor = factor;
    }

    /**
     *
     * Set the height of the X-Axis text in SP
     *
     * @param sp height of the X-Axis text
     */
    public void setTextHeightInSP(@IntRange(from = 0, to = 100) int sp) {
        textSPHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, getResources().getDisplayMetrics());
        textPaintBrush.setTextSize(textSPHeight);
    }

    /**
     * Set the lines to be drawn on the graph.
     * Clears any existing lines and uses this set by default.
     * Also, calls {@link #invalidate()} to force a re-draw
     *
     * @param events lines to be drawn
     */
    public void setEvents(@NonNull  List<ActivityLine> events, long atTime) {
        activityIntervals.clear();
        activityIntervals.addAll(events);
        if (activityIntervals.isEmpty()) {
            setMinMaxBounds(atTime);
            return;
        }

        setMinMaxBounds(atTime);
    }

    /**
     * Set the lines to be drawn on the graph.
     * Clears any existing lines and uses this set by default.
     * Also, calls {@link #invalidate()} to force a re-draw
     *
     * @param events lines to be drawn
     */
    public void setEvents(@NonNull  List<ActivityLine> events, Comparator<ActivityLine> comparator, long atTime) {
        setEvents(events, atTime);
        Collections.sort(activityIntervals, comparator);
    }

    /**
     * Set the start/end time of the graph
     *
     * @param endTime What is the far left of the graphs time
     */
    public void setEndTime(long endTime) {
        this.endTime = getEndTime(endTime);
        this.startTime = this.endTime - VIEWPORT_WIDTH;
        setMinMaxBounds(endTime);
    }

    protected void setMinMaxBounds(long fromThisTime) {
        Calendar boundTime = Calendar.getInstance();
        boundTime.setTimeInMillis(fromThisTime);
        boundTime.set(Calendar.HOUR_OF_DAY, 0);
        boundTime.set(Calendar.MINUTE, 0);
        boundTime.set(Calendar.SECOND, 0);
        minLeftBound = boundTime.getTimeInMillis();

        if (Calendar.getInstance().get(Calendar.DAY_OF_YEAR) == boundTime.get(Calendar.DAY_OF_YEAR)) {
            // Same day cap bound at now(ish)
            maxRightBound = getEndTime(System.currentTimeMillis());
        }
        else {
            // Else allow to scroll to midnight (if yesterday or before)
            boundTime.set(Calendar.HOUR_OF_DAY, 23);
            boundTime.set(Calendar.MINUTE, 59);
            boundTime.set(Calendar.SECOND, 59);
            maxRightBound = boundTime.getTimeInMillis();
        }
    }

    public void setBucketSize(@FloatRange(from = .5, to = 120) float size) {
        this.bucketSize = size;
    }

    /**
     * Sets the amount of space from the X-axis to the bottom of the canvas.
     *
     * @param axisSizeInPX amount of space from X-axis to the bottom of the canvas
     */
    public void setAxisSizeDP(@IntRange(from = 1) int axisSizeInPX) {
        this.axisSizePX = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, axisSizeInPX, getResources().getDisplayMetrics()));
    }

    /**
     * Should a border be drawn?
     *
     * This border will exclude the axis and only draw a border around the data set - not the axis.
     *
     * @param drawBorder Should a border be drawn?
     */
    public void setShouldDrawBorder(boolean drawBorder) {
        this.shouldDrawBorder = drawBorder;
    }

    /**
     * Should a border be drawn?
     *
     * This border will excude the axis and only draw a border around the data set - not the axis.
     *
     * @param drawAxis Should a border be drawn?
     */
    public void setShouldDrawAxis(boolean drawAxis) {
        this.shouldDrawAxis = drawAxis;
    }

    /**
     * Set the size of the border radius.
     *
     * @param borderRadiusSize size of the border radius
     */
    public void setBorderRadius(@FloatRange(from = 1, to = 100) float borderRadiusSize) {
        this.borderRadius = borderRadiusSize;
    }

    public void setUseAxisAlpha(boolean useAxisAlpha) {
        this.useAxisAlpha = useAxisAlpha;
    }

    public void setUseContentAlpha(boolean useContentAlpha) {
        this.useContentAlpha = useContentAlpha;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int borderSize = getBorderSize();
        canvasWidth = canvas.getWidth();
        canvasHeight = getHeight() == 0 ? canvas.getHeight() : getHeight();
        eventPaintBrush.setStrokeWidth(((canvasWidth / TimeUnit.MILLISECONDS.toMinutes(VIEWPORT_WIDTH)) * bucketSize) + 1);

        canvas.save();
        if (shouldDrawBorder) {
            borderRectangle.set(halfSolidLineStroke, halfSolidLineStroke, canvasWidth - halfSolidLineStroke, canvasHeight - axisSizePX);
            canvas.drawRoundRect(borderRectangle, borderRadius, borderRadius, borderAndLinePaintBrush);
        }
        else if (shouldDrawAxis) {
            canvas.drawLine(borderSize, canvasHeight - axisSizePX, canvasWidth - borderSize, canvasHeight - axisSizePX, borderAndLinePaintBrush);
        }

        if (useContentAlpha) {
            alphaPaintBrush.setAlpha(sixtyPercent);
            canvas.drawRect(0, 0, canvasWidth, canvasHeight - axisSizePX, alphaPaintBrush);
        }

        if (useAxisAlpha) {
            alphaPaintBrush.setAlpha(eightyPercent);
            canvas.drawRect(0, canvasHeight - axisSizePX, canvasWidth, canvasHeight, alphaPaintBrush);
        }
        canvas.restore();

        drawEvents(canvas);
        drawHours(canvas);
    }

    protected int getBorderSize() {
        return shouldDrawBorder ? Math.round(borderAndLinePaintBrush.getStrokeWidth()) : 0;
    }

    protected long getEndTime(long fromTime) {
        calendarEndTime.setTimeInMillis(fromTime);
        int minute = getMinutesInIntervalOf30(calendarEndTime.get(Calendar.MINUTE), true);
        if (minute == 60) {
            minute = 0;
            calendarEndTime.add(Calendar.HOUR, 1);
        }
        calendarEndTime.set(Calendar.MINUTE, minute);
        calendarEndTime.set(Calendar.SECOND, 0);
        return calendarEndTime.getTimeInMillis();
    }

    protected int getMinutesInIntervalOf30(@IntRange(from = 0, to = 59) int minute, boolean roundUp) {
        if (!roundUp && (minute == 30 || minute == 0)) { // Prevents errors in first pass rounding (panning)
            return minute;
        }

        if (minute > 30) {
            return roundUp ? 60 : 30;
        }

        return roundUp ? 30 : 0;
    }

    protected void drawEvents(Canvas canvas) {
        int borderSize = getBorderSize();
        int canvasEventLineHeight = canvasHeight - axisSizePX;
        final Rect clipBounds = canvas.getClipBounds();

        for (ActivityLine line : activityIntervals) {
            float startDraw = getEventNextAndSkew(line, clipBounds);
            if (startDraw == BEFORE_LEFT_BOUND) {
                continue;
            }
            else if (startDraw == AFTER_RIGHT_BOUND) {
                return;
            }

            canvas.drawLine(startDraw, borderSize, startDraw, canvasEventLineHeight, eventPaintBrush);
        }
    }

    protected float getEventNextAndSkew(ActivityLine line, Rect clipBounds) {
        long eventTime = line.getEventTime();
        float xStartDraw = getNextX(eventTime);

        float skew = textPaintBrush.measureText("10");
        if ((xStartDraw + skew) < clipBounds.left) {
            return BEFORE_LEFT_BOUND;
        }
        if (xStartDraw > clipBounds.right) {
            return AFTER_RIGHT_BOUND;
        }

        return xStartDraw + skew;
    }

    protected void drawHours(Canvas canvas) {
        hoursCalendar.setTimeInMillis(startTime);
        hoursCalendar.set(Calendar.MINUTE, getMinutesInIntervalOf30(hoursCalendar.get(Calendar.MINUTE), false));
        hoursCalendar.set(Calendar.SECOND, 0);
        hoursCalendar.set(Calendar.MILLISECOND, 0);

        int minute = hoursCalendar.get(Calendar.MINUTE);
        int hourOfDay = hoursCalendar.get(Calendar.HOUR_OF_DAY);
        long next = hoursCalendar.getTimeInMillis();
        long first = hoursCalendar.getTimeInMillis();
        final Rect clipBounds = canvas.getClipBounds();

        float starSkew = textPaintBrush.measureText(STAR_TEXT);
        // Ends up drawing an extra to the right but the last 'vanishing' axis label goes away.
        for (long i = next; i < (endTime + (THIRTY_MIN_IN_MILLIS - 1000)); i += THIRTY_MIN_IN_MILLIS) {
            float x = getNextX(i);
            int h24 = hourOfDay % 24;
            int h12 = hourOfDay % 12;
            if (h12 == 0) h12 = 12;

            String normal = Integer.toString(h12);
            String supScript;
            if (minute == 0) {
                supScript = h24 >= 12 ? PM : AM;
            }
            else {
                supScript = Integer.toString(minute);
            }

            textPaintBrush.getTextBounds(normal, 0, normal.length(), normalTextBounds);
            int halfLineHeight = (int) (normalTextBounds.height() * (1 - superScriptScaleFactor));
            int top = canvasHeight - (axisSizePX / 3) + (normalTextBounds.height() / 2);
            float skew = textPaintBrush.measureText(normal);

            if (i == first) {
                textPaintBrush.setAlpha(sixtyPercent);
                canvas.drawText(normal, x, canvasHeight, textPaintBrush);
                textPaintBrush.setTextSize(textSPHeight * superScriptScaleFactor);

                textPaintBrush.setAlpha(fortyPercent);
                canvas.drawText(supScript, x + skew, top, textPaintBrush);
                textPaintBrush.setTextSize(textSPHeight);
            }
            else if (i == (TimeUnit.HOURS.toMillis(2) + first)) {
                // Shift the last entry a touch more inside so it doesn't get cut off.
                // Measure the width of the hour + minute (or am pm text) and inset from right that much.
                float offsetLeft = clipBounds.right - textPaintBrush.measureText(normal);
                textPaintBrush.setTextSize(textSPHeight * superScriptScaleFactor);
                offsetLeft -= textPaintBrush.measureText(supScript);
                textPaintBrush.setTextSize(textSPHeight);

                textPaintBrush.setAlpha(sixtyPercent);
                canvas.drawText(normal, offsetLeft, canvasHeight, textPaintBrush);
                textPaintBrush.setTextSize(textSPHeight * superScriptScaleFactor);

                textPaintBrush.setAlpha(fortyPercent);
                canvas.drawText(supScript, offsetLeft + skew, top, textPaintBrush);
                textPaintBrush.setTextSize(textSPHeight);
            }
            else {
                canvas.drawText(STAR_TEXT, x + starSkew, canvasHeight, textPaintBrush);
            }

            minute += 30;
            if (minute == 60) {
                hourOfDay++;
                minute = 0;
            }
        }
    }

    protected float getNextX(long next) {
        return (float) (next - startTime) / VIEWPORT_WIDTH * (canvasWidth * .85f);
    }
}
