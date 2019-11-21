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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import arcus.app.R;
import arcus.app.common.utils.FontUtils;
import arcus.app.common.utils.ImageUtils;

public class ScleraButton extends AppCompatButton implements View.OnTouchListener {

    // Constants for rendering button in AS Preview
    private final static int EDITMODE_CORNER_RADIUS_PX = 7;
    private final static int EDITMODE_OUTLINE_STROKE_PX = 4;

    // Button style constants
    private final static int CORNER_RADIUS_DP = 2;
    private final static int OUTLINE_STROKE_DP = 1;
    private final static int LABEL_TEXT_SIZE_SP = 18;
    private final static int INTRINSIC_BUTTON_HEIGHT_DP = 54;

    private ScleraButtonColor color;
    private Drawable normalState;
    private Drawable depressedState;
    private Integer normalTextState;
    private Integer depressedTextState;

    public ScleraButton(Context context) {
        super(context);
    }

    public ScleraButton(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        if (!isInEditMode()) {
            setTypeface(FontUtils.getDemi());
        }
        setAllCaps(true);
        setTextSize(LABEL_TEXT_SIZE_SP);
        setHeight(ImageUtils.dpToPx(context, INTRINSIC_BUTTON_HEIGHT_DP));
        init(context, attrs);
    }

    public ScleraButton(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setColorScheme(@NonNull ScleraButtonColor color) {
        this.color = color;
        applyColorScheme(color);
    }

    private void applyColorScheme(@NonNull ScleraButtonColor color) {
        switch (color) {
            case OUTLINE_PURPLE:
                normalState = getStrokedBackground(getResources().getColor(R.color.sclera_purple), getResources().getColor(R.color.sclera_text_color_light));
                depressedState = getStrokedBackground(getResources().getColor(R.color.sclera_purple), getResources().getColor(R.color.sclera_text_color_light));
                normalTextState = getResources().getColor(R.color.sclera_purple);
                depressedTextState = getResources().getColor(R.color.sclera_disabled_color);
                break;
            case OUTLINE_DISABLED:
                normalState = getStrokedBackground(getResources().getColor(R.color.sclera_disabled_color), getResources().getColor(R.color.sclera_text_color_light));
                depressedState = getStrokedBackground(getResources().getColor(R.color.sclera_disabled_color), getResources().getColor(R.color.sclera_text_color_light));
                normalTextState = getResources().getColor(R.color.sclera_disabled_color);
                depressedTextState = getResources().getColor(R.color.sclera_disabled_color);
                break;
            case OUTLINE_WHITE:
                normalState = getStrokedBackground(getResources().getColor(R.color.sclera_text_color_light), getResources().getColor(R.color.sclera_transparent));
                depressedState = getStrokedBackground(getResources().getColor(R.color.sclera_white_button_depressed), getResources().getColor(R.color.sclera_transparent));
                normalTextState = getResources().getColor(R.color.sclera_text_color_light);
                depressedTextState = getResources().getColor(R.color.sclera_text_color_light_depressed);
                break;
            case SOLID_DISABLED:
                normalState = getSolidBackground(getResources().getColor(R.color.sclera_disabled_color));
                depressedState = getSolidBackground(getResources().getColor(R.color.sclera_disabled_color));
                normalTextState = getResources().getColor(R.color.sclera_text_color_light);
                depressedTextState = getResources().getColor(R.color.sclera_text_color_light);
                break;
            case SOLID_WHITE_BLUE_TEXT:
                normalState = getSolidBackground(getResources().getColor(R.color.sclera_text_color_light));
                depressedState = getSolidBackground(getResources().getColor(R.color.sclera_white_button_depressed));
                normalTextState = getResources().getColor(R.color.sclera_light_blue);
                depressedTextState = getResources().getColor(R.color.sclera_light_blue_depressed);
                break;
            case SOLID_WHITE_RED_TEXT:
                normalState = getSolidBackground(getResources().getColor(R.color.sclera_text_color_light));
                depressedState = getSolidBackground(getResources().getColor(R.color.sclera_white_button_depressed));
                normalTextState = getResources().getColor(R.color.sclera_alert);
                depressedTextState = getResources().getColor(R.color.sclera_alert_depressed);
                break;
            case SOLID_PURPLE:
            default:
                normalState = getSolidBackground(getResources().getColor(R.color.sclera_purple));
                depressedState = getSolidBackground(getResources().getColor(R.color.sclera_alt_purple));
                normalTextState = getResources().getColor(R.color.sclera_text_color_light);
                depressedTextState = getResources().getColor(R.color.sclera_text_color_light);

        }

        setBackground(normalState);
        setTextColor(normalTextState);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (w > 0 && h > 0) {
            if (getBackground() != null) {
                getBackground().setBounds(0, 0, w, h);
            }
        }
    }

    private void init(@NonNull Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.ScleraButton);
        int buttonColor = a.getInteger(R.styleable.ScleraButton_scleraButtonColor, 0);
        this.color = ScleraButtonColor.values()[buttonColor];
        applyColorScheme(color);
        a.recycle();
        setOnTouchListener(this);
        setStateListAnimator(null);
    }

    private Drawable getStrokedBackground(int strokeColor, int fillColor) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(isInEditMode() ? EDITMODE_CORNER_RADIUS_PX : ImageUtils.dpToPx(CORNER_RADIUS_DP));
        shape.setStroke(isInEditMode() ? EDITMODE_OUTLINE_STROKE_PX : ImageUtils.dpToPx(OUTLINE_STROKE_DP), strokeColor);
        shape.setColor(fillColor);

        return shape;
    }

    private Drawable getSolidBackground(int color) {
        GradientDrawable shape = new GradientDrawable();
        shape.mutate();
        shape.setColor(color);
        shape.setCornerRadius(isInEditMode() ? EDITMODE_CORNER_RADIUS_PX : ImageUtils.dpToPx(CORNER_RADIUS_DP));

        return shape;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            setBackground(depressedState);
            setTextColor(depressedTextState);
        } else {
            setBackground(normalState);
            setTextColor(normalTextState);
        }

        return false;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        applyColorScheme(enabled ? this.color : ScleraButtonColor.SOLID_DISABLED);
    }
}
