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
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import androidx.annotation.NonNull;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;
import android.widget.Button;

import arcus.app.R;
import arcus.app.common.utils.FontUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Version1Button extends Button {

    private final static Logger logger = LoggerFactory.getLogger(Version1Button.class);

    private float spacing = Spacing.NORMAL;
    private Version1ButtonColor color = Version1ButtonColor.BLACK;

    public Version1Button(Context context) {
        super(context);
    }

    public Version1Button(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        setAllCaps(true);
        setTextSize(12);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Version1Button(@NonNull Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public Version1Button(@NonNull Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public void setColorScheme(@NonNull Version1ButtonColor color) {
        this.color = color;
        applyColorScheme(color);
    }

    private void applyColorScheme(@NonNull Version1ButtonColor color) {
        GradientDrawable shape = new GradientDrawable();
        shape.setCornerRadius(15);

        switch (color) {
            case PURPLE:
                shape.setColor(getResources().getColor(R.color.arcus_purple));
                setTextColor(Color.WHITE);
                break;

            case DISABLED:
                shape.setColor(getResources().getColor(R.color.white_with_10));
                setTextColor(getResources().getColor(R.color.black_with_20));
                break;

            case DISABLED_ALT:
                shape.setColor(Color.GRAY);
                setTextColor(Color.WHITE);
                break;

            case MAGENTA:
                shape.setColor(getResources().getColor(R.color.pink_banner));
                setTextColor(Color.WHITE);
                break;

            case BLACK:
                shape.setColor(getResources().getColor(R.color.black));
                setTextColor(Color.WHITE);
                break;

            case TRANSPARENT_WHITE_TEXT:
                shape.setColor(getResources().getColor(android.R.color.transparent));
                setTextColor(Color.WHITE);
                break;

            case WATER:
                shape.setColor(getResources().getColor(R.color.waterleak_color));
                setTextColor(Color.WHITE);
                break;

            case SECURITY:
                shape.setColor(getResources().getColor(R.color.security_color));
                setTextColor(Color.WHITE);
                break;

            case PANIC:
                shape.setColor(getResources().getColor(R.color.panic_color));
                setTextColor(Color.WHITE);
                break;

            case SAFETY:
                shape.setColor(getResources().getColor(R.color.safety_color));
                setTextColor(Color.WHITE);
                break;

            case BLUE:
                shape.setColor(getResources().getColor(android.R.color.holo_blue_dark));
                setTextColor(Color.WHITE);
                break;

            case WHITE:
            default:
                shape.setColor(getResources().getColor(R.color.white));
                setTextColor(Color.BLACK);
                break;
        }

        setBackground(shape);
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
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.Version1Button);
        int customFont = a.getInteger(R.styleable.Version1Button_buttonFont, -1);
        boolean tracking = a.getBoolean(R.styleable.Version1Button_buttonTracking, false);
        int buttonColor = a.getInteger(R.styleable.Version1Button_buttonColorScheme, -1);
        if (buttonColor != -1) {
            this.color = Version1ButtonColor.values()[buttonColor];
            applyColorScheme(color);
        } else {
            setColorScheme(Version1ButtonColor.BLACK);
        }
        setFont(customFont, tracking);
        a.recycle();
    }

    private void setFont(int customFont, boolean tracking) {
        if (isInEditMode()) {
            return;
        }

        Typeface typeface;

        if (customFont == -1) {
            typeface = FontUtils.getNormal();
        }

        switch (customFont) {
            case 0:
                typeface = FontUtils.getNormal();
                break;
            case 1:
                typeface = FontUtils.getLight();
                break;
            case 2:
                typeface = FontUtils.getLightItalic();
                break;
            case 3:
                typeface = FontUtils.getBold();
                break;
            case 4:
                typeface = FontUtils.getItalic();
                break;
            case 5:
                typeface = FontUtils.getDemi();
                break;
            default:
                typeface = FontUtils.getNormal();
                break;

        }

        if (tracking) {
            setSpacing(5);
        }
        setTypeface(typeface);
    }

    public float getSpacing() {
        return this.spacing;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        applyColorScheme(enabled ? this.color : Version1ButtonColor.DISABLED);
    }

    public void setEnabled(boolean enabled, Version1ButtonColor disabledButtonColor) {
        super.setEnabled(enabled);
        applyColorScheme(enabled ? this.color : disabledButtonColor);
    }

    private void applySpacing() {
        final CharSequence originalText = getText();
        if (originalText == null || originalText.equals("")) return;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < originalText.length(); i++) {
            builder.append(originalText.charAt(i));
            if (i + 1 < originalText.length()) {
                builder.append("\u00A0");
            }
        }
        SpannableString finalText = new SpannableString(builder.toString());
        if (builder.toString().length() > 1) {
            for (int i = 1; i < builder.toString().length(); i += 2) {
                finalText.setSpan(new ScaleXSpan((spacing + 1) / 10), i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        super.setText(finalText, BufferType.SPANNABLE);
    }

    public class Spacing {
        public final static float NORMAL = 0;
    }

}
