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
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;

import arcus.app.R;
import arcus.app.common.utils.FontUtils;
import com.rengwuxian.materialedittext.MaterialEditText;

public class ScleraEditText extends MaterialEditText implements TextWatcher, View.OnFocusChangeListener {

    private static final int DEFAULT_GREY_COLOR_RES = R.color.sclera_text_color_dark;
    private static final float DEFAULT_TEXT_SIZE_SP = 20;

    // True when field receives focus but has not yet received any keypresses
    private boolean didJustReceiveFocus = true;

    public interface LostFocusListener {
        void onDidLoseFocus(ScleraEditText editText);
    }

    public interface TextChangeListener {
        void onTextChanged(ScleraEditText editText, CharSequence newValue);
    }

    private boolean clearable = false;      // Indicates whether clear 'X' is visible in field
    private boolean showable = false;       // Indicates whether hide/show password toggle is visible
    private boolean resettable = true;      // Indicates whether backspace deletes all text after re-focus

    private LostFocusListener lostFocusListener;
    private TextChangeListener textChangeListener;

    public ScleraEditText(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        initArcuStyle(context, attrs);
    }

    public ScleraEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initArcuStyle(context, attrs);
    }

    public ScleraEditText(Context context) {
        super(context);
        init(false, false, true);
    }

    private void initArcuStyle(Context context, AttributeSet attrs) {

        boolean clearable = false;
        boolean showable = false;
        boolean resettable = true;

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ScleraEditText, 0, 0);

        try {
            clearable = a.getBoolean(R.styleable.ScleraEditText_clearable, false);
            showable = a.getBoolean(R.styleable.ScleraEditText_showable, false);
            resettable = a.getBoolean(R.styleable.ScleraEditText_resettable, true);
        } finally {
            a.recycle();
        }

        init(clearable, showable, resettable);
    }

    public void resetArcusStyle() {
        init(clearable, showable, resettable);
    }

    private void init(final Boolean clearable, Boolean showable, Boolean resettable) {

        this.clearable = clearable;
        this.showable = showable;
        this.resettable = resettable;

        setFloatingLabel(FLOATING_LABEL_NORMAL);

        if (!isInEditMode()) {
            setTypeface(FontUtils.getNormal());
        }
        setTextSize(TypedValue.COMPLEX_UNIT_SP, DEFAULT_TEXT_SIZE_SP);

        setFloatingLabelTextColor(getResources().getColor(DEFAULT_GREY_COLOR_RES));
        setHintTextColor(getResources().getColor(DEFAULT_GREY_COLOR_RES));

        // Modifies the normal behavior of hint and floating text to match Robbie's requirements
        setOnFocusChangeListener(this);
        addTextChangedListener(this);
    }

    @Override
    public void setText(@Nullable CharSequence text, BufferType type) {
        if (text != null) {
            super.setText(text.toString(), type);
            fireTextChangeListener(text);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        setFloatingLabelAlwaysShown(hasFocus);
        setHint(hasFocus ? "" : getFloatingLabelText());

        if (hasFocus) {
            setClearButtonVisible(clearable && getText().length() > 0);
            ScleraEditText.this.didJustReceiveFocus = true;
        } else {
            setClearButtonVisible(false);
            fireFocusLostListener();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        if (resettable && didJustReceiveFocus && after == 0) {
            this.didJustReceiveFocus = false;
            setText("");
        } else {
            this.didJustReceiveFocus = false;
        }
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        // Clear errors on edit
        setError(null);

        // Hide or show clear button based on whether edit field now contains any text
        setClearButtonVisible(getText().length() > 0);
        setShowHideToggleVisible(getText().length() > 0);
    }

    @Override
    public void afterTextChanged(Editable s) {
        fireTextChangeListener(s);
    }

    @Override
    public void setError(CharSequence errorText) {
        super.setError(errorText);

        if (errorText != null) {
            setFloatingLabelTextColor(getErrorColor());
        } else {
            setFloatingLabelTextColor(getResources().getColor(DEFAULT_GREY_COLOR_RES));
        }
    }

    public void setLostFocusListener(LostFocusListener listener) {
        this.lostFocusListener = listener;
    }

    public void setTextChangeListener(TextChangeListener listener) {
        this.textChangeListener = listener;
    }

    private void setShowHideToggleVisible(final boolean visible) {
        if (showable && visible) {
            int drawableToGet = isPasswordObscured() ? R.drawable.show_icon_type_small : R.drawable.hide_icon_type_small;
            Drawable x = ContextCompat.getDrawable(getContext(), drawableToGet);
            x.setBounds(0, 0, x.getIntrinsicWidth(), x.getIntrinsicHeight());
            setCompoundDrawables(null, null, x, null);
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int DRAWABLE_RIGHT = 2;

                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (event.getRawX() >= (getRight() - getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                            int insertionPoint = getSelectionStart();

                            if (isPasswordObscured()) {
                                setInputType(InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            } else {
                                setInputType(InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
                            }

                            setShowHideToggleVisible(visible);
                            resetArcusStyle();
                            setSelection(insertionPoint);

                            return false;
                        }
                    }
                    return false;
                }
            });
        } else if (showable) {
            setCompoundDrawables(null, null, null, null);
            setOnTouchListener(null);
        }
    }

    private boolean isPasswordObscured() {
        return getInputType() == (InputType.TYPE_CLASS_TEXT | EditorInfo.TYPE_TEXT_VARIATION_PASSWORD);
    }

    private void setClearButtonVisible(boolean visible) {
        if (clearable && visible) {
            int drawableToGet = R.drawable.circlex_icon_type_small;
            Drawable x = ContextCompat.getDrawable(getContext(), drawableToGet);
            x.setBounds(0, 0, x.getIntrinsicWidth(), x.getIntrinsicHeight());
            setCompoundDrawables(null, null, x, null);
            setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    final int DRAWABLE_RIGHT = 2;
                    if (event.getAction() == MotionEvent.ACTION_UP &&
                            event.getRawX() >= (getRight() - getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()))
                    {
                        onClearButtonPressed();
                        return false;
                    }
                    return false;
                }
            });
        } else if (clearable) {
            setCompoundDrawables(null, null, null, null);
            setOnTouchListener(null);
        }
    }

    private void onClearButtonPressed() {
        // Clear error
        setError(null);

        // Clear input
        setText("");

        // Hide 'X' button
        setClearButtonVisible(false);
    }

    private void fireFocusLostListener() {
        LostFocusListener listener = lostFocusListener;
        if (listener != null) {
            listener.onDidLoseFocus(this);
        }
    }

    private void fireTextChangeListener(CharSequence newValue) {
        TextChangeListener listener = textChangeListener;
        if (listener != null) {
            listener.onTextChanged(this, newValue);
        }
    }
}
