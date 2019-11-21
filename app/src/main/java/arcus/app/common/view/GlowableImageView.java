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

import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import arcus.app.R;

/**
 * An ImageView which can be decorated by a glowing ring to indicate either an on/off state or an
 * open/closed state.
 *
 * This class assumes--but does not require--that the image set in setImageDrawable() is a circle
 * with square outer dimensions (equal height and width).
 */
public class GlowableImageView extends CircularImageView {

    private static final int ANIMATION_DURATION_MS = 500;   // Time in milliseconds animation should last
    private static final float SCALING_FACTOR = 0.00625f; // 1 / 160 (which is mdpi_density) == this number
    private Bitmap mRecycledBitmap;
    private Canvas mCanvas;
    private Paint mPaint;
    private ValueAnimator mValAnimator;
    private int white60;
    private int maxStrokeWidth = 10; // Previous static value.

    public enum GlowMode {
        OFF,            // Draws nothing around the base image, regardless of setGlowing()
        ON_OFF,         // Draws a "glowing" ring around the base image
        OPEN_CLOSE      // Draws an expanding/contracting rubber band ring around the base image
    }

    private int currentStrength;                    // Strength of the effect; animates between getMinGlowStrength() and getMaxGlowStrength()
    private boolean glowing = false;                // State of the glow effect
    private GlowMode mode = GlowMode.ON_OFF;        // Mode of the glow effect

    public GlowableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GlowableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    @TargetApi(21)
    public GlowableImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context);
    }

    protected void init(Context context) {
        white60  = context.getResources().getColor(R.color.white_with_60);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setXfermode(null);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setColor(Color.WHITE);

        Resources resources = context.getResources();
        if (resources != null) {
            DisplayMetrics displayMetrics = resources.getDisplayMetrics();
            if (displayMetrics != null) {
                float scaleSize = displayMetrics.densityDpi * SCALING_FACTOR;
                Number scaledNumber = scaleSize * 2.29;
                maxStrokeWidth = Math.min(10, scaledNumber.intValue());
            }
        }
    }

    @Override
    public void setImageDrawable (@Nullable Drawable drawable) {
        // Special case: remove image altogether
        if (drawable == null) {
            super.setImageDrawable(null);
            return;
        }

        this.setImageBitmap(drawableToBitmap(drawable));
    }

    @Override
    public void setImageBitmap (@Nullable Bitmap image) {
        super.setImageBitmap(image);

        // Special case: remove image, but can't draw glow
        if (image == null) {
            return;
        }

        viewHeight = imageHeight + (getMaxGlowStrength() * 4);
        viewWidth = imageWidth + (getMaxGlowStrength() * 4);

        ViewGroup.LayoutParams lp = getLayoutParams();
        lp.height = viewHeight;
        lp.width = viewWidth;
        setLayoutParams(lp);

        setScaleType(ScaleType.CENTER);

        // Don't animate before setting sizes; doing so causes extra re-animations as view invalidates
        int targetStroke = this.glowing ? getMaxGlowStrength() : getMinGlowStrength();
        animateGlow(targetStroke);
    }

    public void setGlowing (boolean enabled){
        this.glowing = enabled;
        int targetStrength = enabled ? getMaxGlowStrength() : getMinGlowStrength();

        animateGlow(targetStrength);
    }

    public boolean isGlowing () {
        return currentStrength > 0;
    }

    public void setGlowMode (GlowMode mode) {
        this.mode = mode;

        // Never draw bevel for open/close (rubber band) mode
        setBevelVisible(mode == GlowMode.ON_OFF);
    }

    public GlowMode getGlowMode () {
        return this.mode;
    }

    private void drawGlow(int strength) {
        // Nothing to draw yet
        if (viewWidth <= 0 || viewHeight <= 0) return;

        // Calculate glow ring's center and radius
        int centerX = viewWidth / 2;
        int centerY = viewHeight / 2;
        int stroke = getGlowStrokeForMode(mode, strength);
        int radius = getGlowRadiusForMode(mode, strength);

        // Create a canvas to paint on if necessary
        if (mRecycledBitmap == null || mRecycledBitmap.getWidth() != viewWidth || mRecycledBitmap.getHeight() != viewHeight) {
            mRecycledBitmap = Bitmap.createBitmap(viewWidth, viewHeight, Bitmap.Config.ARGB_8888);
        }

        mCanvas = new Canvas(mRecycledBitmap);
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        // Provision the paint
        if (!mode.equals(GlowMode.OPEN_CLOSE)) {
            mPaint.setShadowLayer(stroke, 0, 0, white60);
        }

        mPaint.setStrokeWidth(stroke);

        // Draw the circle on the canvas; don't draw if stroke is zero. Some Android versions render
        // a faint line instead of nothing when stroke == 0
        if (stroke > 0) {
            mCanvas.drawCircle(centerX, centerY, radius, mPaint);
        }

        // Then apply the canvas as the view's background
        setBackground(new BitmapDrawable(getResources(), mRecycledBitmap));

        currentStrength = strength;
    }

    /**
     * Gets the width of the glow ring to draw for the given mode and strength.
     *
     * @param mode
     * @param strength
     * @return
     */
    private int getGlowStrokeForMode (GlowMode mode, int strength) {
        switch (mode) {
            case ON_OFF:
                return strength;
            case OFF:
                return 0;
            case OPEN_CLOSE:
                return Math.min(strength, maxStrokeWidth);
            default:
                throw new IllegalStateException("Bug! Unimplemented GlowMode");
        }
    }

    /**
     * Gets the radius of the glow circle for a given mode and strength.
     *
     * @param mode
     * @param strength
     * @return
     */
    private int getGlowRadiusForMode (GlowMode mode, int strength) {
        switch(mode) {
            case ON_OFF:
                return ((imageWidth + (strength / 2)) / 2);
            case OFF:
                return 0;
            case OPEN_CLOSE:
                return ((imageWidth + (strength * 2)) / 2) + getGlowStrokeForMode(mode, strength);

            default:
                throw new IllegalStateException("Bug! Unimplemented GlowMode");
        }
    }

    /**
     * Incrementally change the current glow until it reaches the provided value. Duration of the
     * animation is specified by by ANIMATION_DURATION_MS.
     *
     * @param targetStrength The target width (in ON/OFF mode) or radius (in OPEN/CLOSE mode) of the
     *                     ring around the image.
     */
    private void animateGlow(int targetStrength) {

        if (targetStrength < 0 || targetStrength > getMaxGlowStrength()) {
            throw new IllegalArgumentException("Target glow stroke must be within acceptable bounds: " + getMinGlowStrength() + ".." + getMaxGlowStrength());
        }

        mValAnimator = ValueAnimator.ofInt(currentStrength, targetStrength);
        mValAnimator.setDuration(ANIMATION_DURATION_MS);
        mValAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                Integer value = (Integer) animation.getAnimatedValue();
                GlowableImageView.this.drawGlow(value);
            }
        });
        mValAnimator.start();
    }

    /**
     * Gets the maximum glow strength relative to the current image's dimensions. This allows the
     * power and size of the glow effect to scale proportionally to the image it's being applied to.
     *
     * @return
     */
    private int getMaxGlowStrength() {
        // 10% of the smaller image dimension
        Double strength = Math.ceil(Math.min(imageHeight, imageWidth) * 0.10);
        return strength.intValue();
    }


    /**
     * Gets the minimum glow strength to be used. Always returns zero; exists for consistency only.
     *
     * @return
     */
    private int getMinGlowStrength() {
        return 0;
    }


    /**
     * Converts a drawable to a rasterized bitmap image.
     *
     * @param drawable
     * @return
     */
    private Bitmap drawableToBitmap (Drawable drawable) {
        Bitmap bitmap;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if(bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if(drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
}
