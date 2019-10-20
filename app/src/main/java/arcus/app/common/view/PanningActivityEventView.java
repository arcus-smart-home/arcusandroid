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
import android.content.Context;
import android.graphics.Color;
import android.graphics.RectF;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;

import arcus.cornea.subsystem.care.model.ActivityLine;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class PanningActivityEventView extends ActivityEventView {
    private static final String LOG_TAG = PanningActivityEventView.class.getSimpleName();

    private boolean shouldUseDash3Line  = false;
    private float fivePXInDP;
    private int panFingerID;            // Which finger is down.
    private float finger1sX;            // Location of PAN movement start/end
    private MovementMode mode;          // What type of movement, eg drag (pan), none, or (future?) zoom
    private RectF borderRectangle;      // Used to draw a rounded border if one is desired.

    private enum MovementMode {
        NONE,
        PAN_AND_DRAG
    }

    public PanningActivityEventView(Context context) {
        super(context);
        init(context);
    }

    public PanningActivityEventView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PanningActivityEventView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21) public PanningActivityEventView(
          Context context,
          AttributeSet attrs,
          int defStyleAttr,
          int defStyleRes
    ) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    public void setShouldUseDash3Line(boolean shouldUse) {
        shouldUseDash3Line = shouldUse;
    }

    @Override protected void init(Context context) {
        super.init(context);

        mode = MovementMode.NONE;
        panFingerID = 0;
        finger1sX = 0f;
        setTextHeightInSP(14);
        setAxisSizeDP(30);

        setSuperscriptScaleFactor(.65f);
        setBucketSize(5f);

        setEndTime(System.currentTimeMillis());
        setShouldDrawBorder(false);
        setShouldDrawAxis(false);

        setUseAxisAlpha(true);
        setUseContentAlpha(true);

        fivePXInDP = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());
    }

    public void setViewportWidthInHours(int hours) {
        VIEWPORT_WIDTH = TimeUnit.HOURS.toMillis(hours);
        init(getContext());
    }

    @Override public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mode        = MovementMode.PAN_AND_DRAG;
                finger1sX   = event.getX();
                panFingerID = event.getPointerId(event.getActionIndex());
                invalidate();
                break;

            case MotionEvent.ACTION_MOVE:
                if (MovementMode.NONE.equals(mode)) {
                    if (DEBUG) {
                        Log.w(LOG_TAG, "Entered Action_Move with Movement Mode == NONE");
                    }
                    return false;
                }

                float new1x;
                if (event.findPointerIndex(panFingerID) == -1) {
                    new1x = finger1sX;
                }
                else {
                    new1x = event.getX();
                }

                // how far to scroll in milliseconds to match the scroll input in pixels
                long drag = (long) ((finger1sX - new1x) * (endTime - startTime) / canvasWidth);
                boolean exceededRightBound = (endTime + drag) > maxRightBound;
                boolean exceededLeftBound = (startTime + drag) <= minLeftBound;
                if (exceededLeftBound || exceededRightBound || drag == 0) {
                    // Don't redraw the screen if we haven't moved or are exceeding a bound.
                    return false;
                }
                startTime += drag;
                endTime += drag;
                finger1sX = new1x;

                if (DEBUG) {
                    Log.d(LOG_TAG, String.format("Adding %s to start/End from %s", drag, finger1sX));
                }
                invalidate(); // Redraw w/new L&R
                break;

            case MotionEvent.ACTION_CANCEL: // Oops, Cancelled!
            case MotionEvent.ACTION_UP:
                mode = MovementMode.NONE;
                invalidate();
                break;

            // Ignores two finger gestures -> MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_POINTER_UP
            default:
                break;
        }

        return true;
    }

    @Override protected void drawEvents(Canvas canvas) {
        int borderSize = getBorderSize();
        int canvasEventLineHeight = canvasHeight - axisSizePX;
        final Rect clipBounds = canvas.getClipBounds();

        float lineSegmentSize;
        for (ActivityLine line : activityIntervals) {
            float startDraw = getEventNextAndSkew(line, clipBounds);
            if (startDraw == BEFORE_LEFT_BOUND) {
                continue;
            }
            else if (startDraw == AFTER_RIGHT_BOUND) {
                return;
            }


            canvas.drawLine(startDraw, borderSize, startDraw, canvasEventLineHeight, eventPaintBrush);
            if (shouldUseDash3Line && line.getIsContact()) {
                lineSegmentSize = canvasEventLineHeight / 3;

                eventPaintBrush.setColor(Color.BLACK);
                eventPaintBrush.setAlpha(twentyPercent);

                canvas.drawLine(startDraw, borderSize, startDraw, lineSegmentSize, eventPaintBrush);
                canvas.drawLine(startDraw, borderSize + (lineSegmentSize * 2), startDraw, canvasEventLineHeight, eventPaintBrush);

                eventPaintBrush.setColor(Color.WHITE);
                eventPaintBrush.setAlpha(fullAlpha);
            }
        }
    }

    @Override protected void drawHours(Canvas canvas) {
        Calendar minLeft = Calendar.getInstance();
        minLeft.setTimeInMillis(startTime);
        minLeft.set(Calendar.MINUTE, getMinutesInIntervalOf30(minLeft.get(Calendar.MINUTE), false));
        minLeft.set(Calendar.SECOND, 0);
        minLeft.set(Calendar.MILLISECOND, 0);

        int minute = minLeft.get(Calendar.MINUTE);
        int hourOfDay = minLeft.get(Calendar.HOUR_OF_DAY);
        long next = minLeft.getTimeInMillis();
        final Rect clipBounds = canvas.getClipBounds();
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

            float skew = textPaintBrush.measureText(normal);
            textPaintBrush.getTextBounds(normal, 0, normal.length(), normalTextBounds);
            int halfLineHeight = (int) (normalTextBounds.height() * (1 - superScriptScaleFactor));
            int top = canvasHeight - (axisSizePX / 2) + (normalTextBounds.height() / 2);
            float farRightOfIncomingBound = x + skew;

            // If the hour digit cannot be seen, don't draw just "PM" or "30"
            if (farRightOfIncomingBound < clipBounds.left || x > clipBounds.right) {
                minute += 30;
                if (minute == 60) {
                    hourOfDay++;
                    minute = 0;
                }
                continue;
            }
            textPaintBrush.setAlpha(sixtyPercent);
            canvas.drawText(normal, x, top, textPaintBrush);
            textPaintBrush.setTextSize(textSPHeight * superScriptScaleFactor);

            textPaintBrush.setAlpha(fortyPercent);
            canvas.drawText(supScript, x + skew, top - halfLineHeight, textPaintBrush);
            textPaintBrush.setTextSize(textSPHeight);

            minute += 30;
            if (minute == 60) {
                hourOfDay++;
                minute = 0;
            }
        }
    }

    @Override protected float getNextX(long next) {
        return super.getNextX(next) + fivePXInDP;
    }
}
