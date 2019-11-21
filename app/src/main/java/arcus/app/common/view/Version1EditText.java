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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import arcus.app.R;
import arcus.app.common.utils.FontUtils;
import com.rengwuxian.materialedittext.MaterialEditText;


public class Version1EditText extends MaterialEditText {
    private static final int DARK = 0;
    private static final int LIGHT = 1;

    private static final float DEFAULT_TEXT_SIZE = 18;

    private boolean disableEnterKey = true;
    private float spacing = Spacing.NORMAL;
    private Integer style;
    private Float textSize;
    private Boolean clearable;      // Indicates whether clear 'X' is visible in field
    private Boolean showable;       // Indicates whether hide/show password icon is visible

    @NonNull
    private CharSequence originalText = "";

    public Version1EditText(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        initArcusStyle(context, attrs);
    }

    public Version1EditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initArcusStyle(context, attrs);
    }

    private void initArcusStyle(Context context, AttributeSet attrs) {

        Float textSize = DEFAULT_TEXT_SIZE * getResources().getDisplayMetrics().scaledDensity;
        Integer textStyle = DARK;
        Boolean clearable = false;
        Boolean showable = false;

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.Version1EditText,
                0, 0);

        try {
            textSize = a.getDimension(R.styleable.Version1EditText_iet_textSize, textSize);
            textSize = textSize / getResources().getDisplayMetrics().scaledDensity;
            textStyle = a.getInteger(R.styleable.Version1EditText_iet_style, DARK); // 0 - dark / 1 - light
            clearable = a.getBoolean(R.styleable.Version1EditText_iet_clearable, clearable);
            showable = a.getBoolean(R.styleable.Version1EditText_iet_showable, showable);
        } finally {
            a.recycle();
        }

        init(textStyle, textSize, clearable, showable);
    }

    public Version1EditText(Context context) {
        super(context);
        init(DARK, DEFAULT_TEXT_SIZE, false, false);
    }

    @NonNull
    public Version1EditText useLightColorScheme(boolean useLightText) {
        setBaseColor(getResources().getColor(useLightText ? android.R.color.white : android.R.color.black));
        setPrimaryColor(getResources().getColor(useLightText ? android.R.color.white : android.R.color.black));
        setTextColor(getResources().getColor(useLightText ? android.R.color.white : android.R.color.black));

        return this;
    }

    @NonNull
    public Version1EditText useUppercaseLabels () {
        if (this.getHelperText() != null) {
            this.setHelperText(getHelperText().toUpperCase());
        }
        if (this.getFloatingLabelText() != null) {
            this.setFloatingLabelText(getFloatingLabelText().toString().toUpperCase());
        }
        if (this.getText() != null) {
            this.setText(getText().toString().toUpperCase());
        }
        if (this.getHint() != null) {
            this.setHint(getHint().toString().toUpperCase());
        }

        return this;
    }

    public void resetArcusStyle() {
        init(style, textSize, clearable, showable);
    }

    private void init(Integer style, Float textSize, Boolean clearable, Boolean showable) {

        this.style = style;
        this.textSize = textSize;
        this.clearable = clearable;
        this.showable = showable;

        if (style == DARK) {
            useLightColorScheme(false);
        } else if (style == LIGHT) {
            useLightColorScheme(true);
        }

        setFloatingLabel(FLOATING_LABEL_NORMAL);

        if (!isInEditMode()) {
            setTypeface(FontUtils.getNormal());
        }
        setTextSize(TypedValue.COMPLEX_UNIT_SP, textSize);
        setShowButtonVisible(showable);
    }

    public void setDisableEnterKey(boolean disableEnterKey) {
        this.disableEnterKey = disableEnterKey;
    }

    public float getSpacing() {
        return this.spacing;
    }

    public void setSpacing(float spacing) {
        this.spacing = spacing;
        // disabling for now
        //applySpacing();
    }

    @Override
    public void setText(@Nullable CharSequence text, BufferType type) {
        if (text != null) {
            super.setText(text.toString(), type);
            originalText = text.toString();
        }
    }

    @Override
    public Editable getText() {
        Editable e = super.getText();
        originalText = e.toString();
        return e;
    }

    private void applySpacing() {

        if (this == null || this.originalText == null) return;

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (disableEnterKey && keyCode == KeyEvent.KEYCODE_ENTER) {
            return true; // Ignore
        }

        // -1 restores default value
        setFloatingLabelTextColor(-1);

        // Let superclass handle keydown event (add key to edit text)
        boolean trapped = super.onKeyDown(keyCode, event);

        // Hide or show clear button based on whether edit field now contains any text
        setClearButtonVisible(getText().length() > 0);

        // Let Android know if we handled this event
        return trapped;
    }

    public class Spacing {
        public final static float NORMAL = 0;
    }

    @Override
    public void setError(CharSequence errorText) {
        super.setError(errorText);

        if (errorText != null) {
            setFloatingLabelTextColor(getErrorColor());
        }
    }

    private void setShowButtonVisible(boolean visible) {
        if (showable && visible) {
            int drawableToGet = (style == DARK) ? R.drawable.button_close_black_x : R.drawable.button_close_box_white;
            Drawable x = ContextCompat.getDrawable(getContext(), drawableToGet);
            x.setBounds(0, 0, x.getIntrinsicWidth(), x.getIntrinsicHeight());
            setCompoundDrawables(null, null, x, null);
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int DRAWABLE_RIGHT = 2;

                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (event.getRawX() >= (getRight() - getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            if (getInputType() == (InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD)) {
                                setInputType(InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            } else {
                                setInputType(InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                            }

                            resetArcusStyle();
                            return false;
                        }
                    }
                    return false;
                }
            });
        } else {
            setCompoundDrawables(null, null, null, null);
            setOnTouchListener(null);
        }
    }

    private void setClearButtonVisible(boolean visible) {
        if (clearable && visible) {
            int drawableToGet = (style == DARK) ? R.drawable.button_close_black_x : R.drawable.button_close_box_white;
            Drawable x = ContextCompat.getDrawable(getContext(), drawableToGet);
            x.setBounds(0, 0, x.getIntrinsicWidth(), x.getIntrinsicHeight());
            setCompoundDrawables(null, null, x, null);
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int DRAWABLE_RIGHT = 2;

                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (event.getRawX() >= (getRight() - getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            setText("");
                            setClearButtonVisible(false);
                            return false;
                        }
                    }
                    return false;
                }
            });
        } else if (clearable) {
            setCompoundDrawables(null, null, null, null);
            setOnTouchListener(null);
        }
    }

}
