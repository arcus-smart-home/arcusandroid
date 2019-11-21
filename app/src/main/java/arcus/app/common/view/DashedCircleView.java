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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import arcus.app.R;
import arcus.app.subsystems.alarm.safety.SafetyAlarmFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DashedCircleView extends View implements SafetyAlarmFragment.DevicesCountUpdateListener {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    protected static final int MIN_GLOW_STROKE = 0;           // Min width of the ring that appears in ON/OFF mode

    private static final int ANIMATION_DURATION_MS = 500;   // Time in milliseconds animation should last

    protected static final float CIRCLE = 360f;

    protected static final float START_ANGLE = 270f;

    private int mGlowStroke = 15;

    protected int mShadowLayerStroke = 8;

    private int mWidth;

    private int mHeight;

    private int mCircleRadius = 0;

    protected int mCircleWidth = 4;

    private int mArcColor;

    protected int mDashes = 0;

    private Paint mArcPaint;

    protected Paint mGreyPaint;

    protected Paint mRedPaint;

    protected Paint mWhiteGlowPaint;

    protected float mGap = 4.0f;

    private int currentStrength;

    @NonNull
    protected RectF mArcRect = new RectF();

    protected boolean isGlowEnabled = false;

    protected boolean isGlowing = false;

    protected int mOffline =0;

    protected int mBypassed = 0;

    protected int mActive = 0;

    protected AlarmState alarmState = AlarmState.OFF;
    private AlarmType  alarmType  = AlarmType.SECURITY;

    @Override
    public void updateOffline(int offline) {

    }

    @Override
    public void updateBypassed(int bypassed) {

    }

    @Override
    public void updateActive(int active) {
        mActive = active;
    }


    public enum AlarmState {
        ON, OFF, ARMING, PARTIAL, ALERT
    }

    public enum AlarmType {
        SECURITY,
        SAFETY,
        SMOKE,
        CO,
        CARE
    }


    public DashedCircleView(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    public DashedCircleView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.dashedCircleStyle);
    }

    public DashedCircleView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public DashedCircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    protected void init(@NonNull Context context, @Nullable AttributeSet attrs, int defStyle) {
        final Resources res = getResources();
        float density = context.getResources().getDisplayMetrics().density;

        mArcColor = res.getColor(R.color.white);

        if(attrs!=null){
            final TypedArray a = context.obtainStyledAttributes(attrs,
                    R.styleable.DashedCircle, defStyle, 0);

            mCircleWidth = (int) a.getDimension(
                    R.styleable.DashedCircle_circleWidth, mCircleWidth);
            mArcColor = a.getColor(R.styleable.DashedCircle_circleColor, mArcColor);

            a.recycle();
        }

        mCircleWidth =  (int) (mCircleWidth * density);
        mGlowStroke = (int) (mGlowStroke * density);
        mShadowLayerStroke = (int) (mShadowLayerStroke * density);

        mArcPaint = new Paint();
        mArcPaint.setAntiAlias(true);
        mArcPaint.setColor(Color.WHITE);
        mArcPaint.setStyle(Paint.Style.STROKE);
        mArcPaint.setStrokeWidth(mCircleWidth);

        mGreyPaint = new Paint();
        mGreyPaint.setAntiAlias(true);
        mGreyPaint.setColor(getResources().getColor(R.color.white_with_10));
        mGreyPaint.setStyle(Paint.Style.STROKE);
        mGreyPaint.setStrokeWidth(mCircleWidth);

        mRedPaint = new Paint();
        mRedPaint.setAntiAlias(true);
        mRedPaint.setColor(getResources().getColor(R.color.pink_banner));
        mRedPaint.setStyle(Paint.Style.STROKE);
        mRedPaint.setStrokeWidth(mCircleWidth);

        mWhiteGlowPaint = new Paint();
        mWhiteGlowPaint.setAntiAlias(true);
        mWhiteGlowPaint.setColor(Color.WHITE);
        mWhiteGlowPaint.setStyle(Paint.Style.STROKE);
        mWhiteGlowPaint.setStrokeWidth(mCircleWidth);
        mWhiteGlowPaint.setShadowLayer(mShadowLayerStroke, 0, 0, Color.WHITE);
        setLayerType(LAYER_TYPE_SOFTWARE, mWhiteGlowPaint);

    }


    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        mDashes = mOffline + mBypassed + mActive;

        if(mDashes == 0){
            if(alarmState == AlarmState.ALERT){
                if(isGlowEnabled){
                    setGlowing(true);
                    isGlowEnabled = false;
                }
            }else {
                cleanCanvas(canvas); // In the event we're coming from "alerted state" to "no dashes"
                drawCircle(canvas, mGreyPaint);
            }
            return;
        }

        if(isGlowing) return;

        switch (alarmState){
            case OFF:
                cleanCanvas(canvas);
                drawCircle(canvas,mGreyPaint);
                break;
            case ON:
                cleanCanvas(canvas);
                drawArmed(canvas);
                break;
            case ARMING:
                cleanCanvas(canvas);
                drawArming(canvas);
                break;
            case PARTIAL:
                cleanCanvas(canvas);
                drawPartial(canvas);
                break;
            case ALERT:
                if(isGlowEnabled){
                    setGlowing(true);
                    isGlowEnabled = false;
                }
                break;
        }

    }

    protected void drawOneDevice(@NonNull Canvas canvas){
        if(mOffline ==1){
            drawCircle(canvas, mRedPaint);
        }else if(mBypassed ==1){
            drawCircle(canvas, mGreyPaint);
        }else if(mActive ==1){
            drawCircle(canvas, mWhiteGlowPaint);
        }
    }

    protected void cleanCanvas(@NonNull Canvas canvas){
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }


    /**
     * draw circle if alarm system is off, or if there is only one device
     * @param canvas canvas to draw
     * @param paint paint used by canvas
     */
    protected void drawCircle(@NonNull Canvas canvas, @NonNull Paint paint){
        canvas.drawCircle(mWidth / 2, mHeight / 2, mCircleRadius, paint);
    }

    /**
     * if alarm is armed, draw offline first, bypassed(if available) second, and lastly active
     * @param canvas canvas to draw
     */
    protected void drawArmed(@NonNull Canvas canvas){
        if(mDashes ==1){
            drawOneDevice(canvas);
            return;
        }

        final float sweep = CIRCLE/mDashes - mGap;
        int count =0;

        //draw offline dashes
        for(int i=0;i< mOffline;i++){
            canvas.drawArc(mArcRect, calAngle(START_ANGLE + (CIRCLE / mDashes) * count + mGap / 2f), sweep,false, mRedPaint);
            count ++;
        }

        //draw bypassed dashes
        for(int j=0;j<mBypassed;j++){
            canvas.drawArc(mArcRect, calAngle(START_ANGLE + (CIRCLE / mDashes) * count + mGap / 2f), sweep,false, mGreyPaint);
            count ++;
        }

        //draw active dashes
        for(int k =0; k<mActive;k++){
            canvas.drawArc(mArcRect, calAngle(START_ANGLE + (CIRCLE / mDashes) * count + mGap / 2f), sweep,false, mWhiteGlowPaint);
            count++;
        }
    }

    /**
     * draw grey arcs when alarm is arming
     * @param canvas canvas to draw
     */
    protected void drawArming(@NonNull Canvas canvas){
        drawArcs(canvas,mGreyPaint);
    }


    /**
     * draw white arcs when alarm is partial
     * @param canvas canvas to draw
     */
    protected void drawPartial(@NonNull Canvas canvas){
        if(mDashes ==1){
            drawOneDevice(canvas);
            return;
        }

        final float sweep = CIRCLE/mDashes - mGap;
        int count =0;

        //draw offline dashes
        for(int i=0;i< mOffline;i++){
            canvas.drawArc(mArcRect, calAngle(START_ANGLE + (CIRCLE / mDashes) * count + mGap / 2f), sweep,false, mRedPaint);
            count ++;
        }

        //draw bypassed dashes
        for(int j=0;j<mBypassed;j++){
            canvas.drawArc(mArcRect, calAngle(START_ANGLE + (CIRCLE / mDashes) * count + mGap / 2f), sweep,false, mGreyPaint);
            count ++;
        }

        //draw active dashes
        for(int k =0; k<mActive;k++){
            canvas.drawArc(mArcRect, calAngle(START_ANGLE + (CIRCLE / mDashes) * count + mGap / 2f), sweep,false, mWhiteGlowPaint);
            count++;
        }
    }

    /**
     * draw arcs with selected paint
     * @param canvas canvas to draw
     * @param paint paint used by canvas
     */
    private void drawArcs(@NonNull Canvas canvas, @NonNull Paint paint){
        if(mDashes ==1){
            drawCircle(canvas,paint);
            return;
        }

        final float sweep = CIRCLE/mDashes - mGap;
        for(int i=0;i<mDashes;i++){
            canvas.drawArc(mArcRect,calAngle(START_ANGLE + (CIRCLE / mDashes) * i + mGap / 2f), sweep,false, paint);
        }
    }

    /**
     * draw pink glowing
     * @param enabled enable glowing or not
     */
    public void setGlowing (boolean enabled){
        int targetStroke = enabled ? mGlowStroke : MIN_GLOW_STROKE;
        animateGlow(targetStroke);
    }

    /**
     * Incrementally change the current glow until it reaches the provided value. Duration of the
     * animation is specified by by ANIMATION_DURATION_MS.
     *
     * @param targetStrength The target width (in ON/OFF mode) or radius (in OPEN/CLOSE mode) of the
     *                     ring around the image.
     */
    protected void animateGlow(int targetStrength) {

        if (targetStrength < 0 || targetStrength > mGlowStroke) {
            throw new IllegalArgumentException("Target glow stroke must be within acceptable bounds: " + MIN_GLOW_STROKE + ".." + mGlowStroke);
        }

        ValueAnimator va = ValueAnimator.ofInt(currentStrength, targetStrength);
        va.setDuration(ANIMATION_DURATION_MS);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                drawGlow((Integer) animation.getAnimatedValue());
                isGlowing = true;
            }
        });

        va.addListener(new AnimatorListenerAdapter()
        {
            @Override
            public void onAnimationEnd(Animator animation) {
                logger.debug("Glowing animation is done: {}", animation);
                isGlowing = false;
            }
        });
        va.start();
    }

    private void drawGlow(int strength) {
        // Nothing to draw yet
        if (mWidth <= 0 || mHeight <= 0) return;

        // Calculate glow ring's center and radius
        int centerX = mWidth / 2;
        int centerY = mHeight/ 2;
        int radius = mCircleRadius + strength / 4;


        Bitmap output = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
        final Canvas c = new Canvas(output);
        c.drawARGB(0, 0, 0, 0);

        final Paint p = new Paint();
        p.setAntiAlias(true);
        p.setXfermode(null);
        p.setStyle(Paint.Style.STROKE);

        int color;
        if (AlarmType.CARE.equals(alarmType)) {
            color = getContext().getResources().getColor(R.color.care_alarm_purple);
        }
        else {
            color = getContext().getResources().getColor(R.color.pink_banner);
        }
        p.setColor(color);
        p.setShadowLayer(strength, 0, 0, color);
        p.setStrokeWidth(strength);

        c.drawCircle(centerX, centerY, radius, p);
        setBackground(new BitmapDrawable(getResources(), output));

        currentStrength = strength;
    }

    protected float calAngle(float angle){
        return angle>=CIRCLE ? angle-CIRCLE : angle;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mHeight = getDefaultSize(getHeight(),
                heightMeasureSpec);
        mWidth = getDefaultSize(getWidth(),
                widthMeasureSpec);
        final int min = Math.min(mWidth, mHeight);

        float center_x = mWidth/2;
        float center_y = mHeight/2;


        mCircleRadius = min/2 - mGlowStroke - getPaddingLeft();

        mArcRect.set(center_x - mCircleRadius, center_y - mCircleRadius, center_x + mCircleRadius, center_y + mCircleRadius);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void setAlarmType(@NonNull AlarmType type) {
        this.alarmType = type;
    }

    public int getCircleRadius() {
        return mCircleRadius;
    }

    public void setCircleRadius(int mCircleRadius) {
        this.mCircleRadius = mCircleRadius;
    }

    public int getCircleWidth() {
        return mCircleWidth;
    }

    public void setCircleWidth(int mCircleWidth) {
        this.mCircleWidth = mCircleWidth;
        mArcPaint.setStrokeWidth(mCircleWidth);
    }

    public int getArcColor() {
        return mArcColor;
    }

    public void setArcColor(int mArcColor) {
        this.mArcColor = mArcColor;
    }

    public int getDashes() {
        return mDashes;
    }

    public void setDashes(int mDashes) {
        this.mDashes = mDashes;
        invalidate();
    }

    public void setDevicesCount(int mOffline, int mBypassed, int mActive){
        this.mOffline = mOffline;
        this.mBypassed = mBypassed;
        this.mActive = mActive;
        invalidate();
    }

    public int getOffline() {
        return mOffline;
    }

    public int getBypassed() {
        return mBypassed;
    }
    public int getActive() {
        return mActive;
    }

    public AlarmState getAlarmState() {
        return alarmState;
    }

    public void setAlarmState(AlarmState alarmState) {
        this.alarmState = alarmState;
        isGlowEnabled = alarmState == AlarmState.ALERT;
        invalidate();
    }
}
