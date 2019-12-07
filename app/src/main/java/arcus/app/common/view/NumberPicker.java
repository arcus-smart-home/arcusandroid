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
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

import androidx.core.content.res.ResourcesCompat;
import arcus.app.R;


public class NumberPicker extends android.widget.NumberPicker {
    private static final String TEXT_SIZE = "mTextSize";
    private static final String DIVIDER   = "mSelectionDivider";
    private static final int FONT_SP_SIZE = 40;

    public NumberPicker(Context context) {
        super(context);
        setupDefaults();
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setupDefaults();
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        setupDefaults();
    }

    @TargetApi(21)
    public NumberPicker(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setupDefaults();
    }

    protected void setupDefaults() {
        setDescendantFocusability(FOCUS_BLOCK_DESCENDANTS);
        setDividerColor(Color.TRANSPARENT);
    }

    @Override
    public void addView(View child) {
        super.addView(child);
        modifyAddedView(child);
    }

    @Override
    public void addView(View child, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, params);
        modifyAddedView(child);
    }

    @Override
    public void addView(View child, int index, android.view.ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        modifyAddedView(child);
    }

    protected void modifyAddedView(View view) {
        if(view instanceof EditText) {
            ((EditText) view).setTextSize(FONT_SP_SIZE);
            ((EditText) view).setTypeface(ResourcesCompat.getFont(getContext(), R.font.nunito_light));
        }
    }

    private void setDividerColor(int color) {
        boolean setDivider = false;
        boolean setTextSize = false;

        java.lang.reflect.Field[] pickerFields = android.widget.NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            try {
                if (pf.getName().equals(DIVIDER)) {
                    pf.setAccessible(true);
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(this, colorDrawable);

                    setDivider = true;
                }
                else if (pf.getName().equals(TEXT_SIZE)) {
                    // Not setting the text size (since we increased to a larger size) was causing the
                    // measurement of the view to be "off". When a scroll took place, the offsets
                    // were all out of sync as to how far to scroll to get to the next view.  This was causing
                    // the picker to look like it was shaking violently everytime it reached a min/max value and you
                    // tried to scroll away.
                    pf.setAccessible(true);
                    pf.set(this, FONT_SP_SIZE);

                    setTextSize = true;
                }

                if (setDivider && setTextSize) {
                    break;
                }
            }
            catch (Exception ex) {
                // No - Op.
            }
        }
    }
}
