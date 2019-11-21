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
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.utils.FontUtils;


public class Version1TextView extends TextView {

    private static final String TAG = "TextView";
    private boolean bApplyKerning = false;

    public Version1TextView(Context context) {
        super(context);
        setFont(context,-1,false);
    }

    public Version1TextView(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        setCustomFont(context, attrs);
    }

    public Version1TextView(@NonNull Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setCustomFont(context, attrs);
    }

    private void setCustomFont(@NonNull Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.Version1TextView);
        int customFont = a.getInteger(R.styleable.Version1TextView_customFont, -1);
        boolean tracking = a.getBoolean(R.styleable.Version1TextView_tracking, false);
        setFont(ctx, customFont, tracking);
        a.recycle();
    }

    private void setFont(Context ctx, int customFont,boolean tracking) {
        if (isInEditMode()) {
            return;
        }

        Typeface typeface;
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
                bApplyKerning = true;
                break;
            default:
                typeface = FontUtils.getNormal();

        }
        setTypeface(typeface);
        setText(this.getText(), BufferType.NORMAL);
    }

    public void text100Opacity(boolean isLight) {
        setTextColor(getResources().getColor(isLight ? R.color.white : R.color.black));
    }

    public void text60Opacity(boolean isLight) {
        setTextColor(getResources().getColor(isLight ? R.color.white_with_60 : R.color.black_with_60));
    }

    //Attempt at doing our own kerning
    //applyKerning is roughly from Pedro Barros (pedrobarros.dev at gmail.com)
    //@ https://stackoverflow.com/questions/1640659/how-to-adjust-text-kerning-in-android-textview
    /*@Override
    public void setText(CharSequence text, BufferType type) {
        if(bApplyKerning) {
            applyKerning(text);
        } else {
            super.setText(text, type);
        }

    }

    private void applyKerning(CharSequence text) {
        if (text == null) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        for(int i = 0; i < text.length(); i++) {
            builder.append(text.charAt(i));
            if(i+1 < text.length()) {
                builder.append("\u00A0");
            }
        }
        SpannableString finalText = new SpannableString(builder.toString());
        if(builder.toString().length() > 1) {
            for(int i = 1; i < builder.toString().length(); i+=2) {
                finalText.setSpan(new ScaleXSpan(0.5f), i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        super.setText(finalText, BufferType.SPANNABLE);
    }*/
}
