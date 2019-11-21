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
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import arcus.app.R;
import arcus.app.subsystems.alarm.safety.SafetyAlarmFragment;


public class IncidentCircleView extends View implements SafetyAlarmFragment.DevicesCountUpdateListener {

    private int mGlowStroke = 15;
    protected int mShadowLayerStroke = 8;
    private int mWidth;
    private int mHeight;
    private int mCircleRadius = 0;
    protected int mCircleWidth = 8;
    private int mArcColor;
    private int primaryColor = R.color.unselected_circle_color;

    protected Paint mLinePaint;
    protected Paint mCirclePaint;
    protected Paint mWhiteGlowPaint;
    protected boolean bDrawRightGradient = true;
    protected boolean bDrawLeftGradient = true;

    @NonNull
    protected RectF mArcRect = new RectF();
    protected int mActive = 0;

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

    public IncidentCircleView(@NonNull Context context) {
        super(context);
        init(context, null, 0);
    }

    public IncidentCircleView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, R.attr.dashedCircleStyle);
    }

    public IncidentCircleView(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public IncidentCircleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
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

        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setColor(getResources().getColor(R.color.white));
        mCirclePaint.setStyle(Paint.Style.STROKE);
        mCirclePaint.setStrokeWidth(mCircleWidth);

        mLinePaint = new Paint();
        mLinePaint.setAntiAlias(true);
        mLinePaint.setColor(getContext().getResources().getColor(R.color.white));
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setStrokeWidth(mCircleWidth/2);

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
        cleanCanvas(canvas);
        int radius = mCircleRadius/2;
        int gradientColor = getSelectedColor();

        if(isSelected()) {
            mCirclePaint.setStrokeWidth(mCircleWidth);
            radius = mCircleRadius;
        } else {
            mCirclePaint.setStrokeWidth(mCircleWidth/2);
        }
        mCirclePaint.setColor(gradientColor);
        drawCircle(canvas, radius, gradientColor);

    }

    private int getSelectedColor() {
        if(!isSelected()) {
            return getContext().getResources().getColor(R.color.unselected_circle_color);
        }
        return primaryColor;
    }

    protected void cleanCanvas(@NonNull Canvas canvas){
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }


    /**
     * draw circle if alarm system is off, or if there is only one device
     * @param canvas canvas to draw
     */
    protected void drawCircle(@NonNull Canvas canvas, int radius, int gradientColorgradient){
        int midPoint = mWidth/2;
        if(!isSelected()) {
            gradientColorgradient = getResources().getColor(R.color.unselected_circle_color);
        }
        if(bDrawLeftGradient) {
            Shader shaderGreyToColor = new LinearGradient(0, 0, midPoint - radius, 0, getResources().getColor(R.color.unselected_circle_color), gradientColorgradient, Shader.TileMode.CLAMP);
            mLinePaint.setShader(shaderGreyToColor);
            canvas.drawLine(0, mHeight / 2, midPoint - radius, mHeight / 2, mLinePaint);
        }
        canvas.drawCircle(mWidth / 2, mHeight / 2, radius, mCirclePaint);
        if(bDrawRightGradient) {
            Shader shaderColorToGrey = new LinearGradient(midPoint + radius, 0, mWidth, 0, gradientColorgradient, getResources().getColor(R.color.unselected_circle_color), Shader.TileMode.CLAMP);
            mLinePaint.setShader(shaderColorToGrey);
            canvas.drawLine(mWidth, mHeight / 2, midPoint + radius, mHeight / 2, mLinePaint);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        recalcLayout(widthMeasureSpec, heightMeasureSpec);

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private void recalcLayout(int widthMeasureSpec, int heightMeasureSpec) {
        mHeight = getDefaultSize(getHeight(),
                heightMeasureSpec);
        mWidth = getDefaultSize(getWidth(),
                widthMeasureSpec);

        float center_x = mWidth/2;
        float center_y = mHeight/2;


        mCircleRadius = mHeight/4;

        mArcRect.set(center_x - mCircleRadius, center_y - mCircleRadius, center_x + mCircleRadius, center_y + mCircleRadius);
    }

    @Override public void setSelected(boolean selected) {
        super.setSelected(selected);
    }
    public void setAlarmColor(int primaryColor) {
        this.primaryColor = primaryColor;
        invalidate();
    }

    public void showLeftGradient(boolean bShow) {
        bDrawLeftGradient = bShow;
    }

    public void showRightGradient(boolean bShow) {
        bDrawRightGradient = bShow;
    }
}
