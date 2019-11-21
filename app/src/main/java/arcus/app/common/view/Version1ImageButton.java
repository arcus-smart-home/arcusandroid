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

import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import androidx.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.ImageButton;

import arcus.app.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class Version1ImageButton extends ImageButton {

    private final static Logger logger = LoggerFactory.getLogger(Version1Button.class);

    private Version1ButtonColor color = Version1ButtonColor.BLACK;

    public Version1ImageButton(Context context) {
        super(context);
    }

    public Version1ImageButton(@NonNull Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public Version1ImageButton(@NonNull Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
            case DISABLED:
                shape.setColor(getResources().getColor(R.color.white_with_10));
                break;

            case DISABLED_ALT:
                shape.setColor(Color.GRAY);
                break;

            case MAGENTA:
                shape.setColor(getResources().getColor(R.color.pink_banner));
                break;

            case BLACK:
                shape.setColor(getResources().getColor(R.color.black));
                break;

            case TRANSPARENT_WHITE_TEXT:
                shape.setColor(getResources().getColor(android.R.color.transparent));
                break;

            case WATER:
                shape.setColor(getResources().getColor(R.color.waterleak_color));
                break;

            case SECURITY:
                shape.setColor(getResources().getColor(R.color.security_color));
                break;

            case PANIC:
                shape.setColor(getResources().getColor(R.color.panic_color));
                break;

            case SAFETY:
                shape.setColor(getResources().getColor(R.color.safety_color));
                break;

            case BLUE:
                shape.setColor(getResources().getColor(android.R.color.holo_blue_dark));
                break;

            case ALARM_CONFIRM_BUTTON_ENABLED:
                shape.setColor(getResources().getColor(R.color.alarm_confirm_button_enabled_color));
                break;

            case ALARM_CANCEL_BUTTON_ENABLED:
                shape.setColor(getResources().getColor(R.color.alarm_cancel_button_enabled_color));
                break;

            case ALARM_BUTTON_DISABLED:
                shape.setColor(getResources().getColor(R.color.alarm_button_disabled_color));
                break;

            case WHITE:
            default:
                shape.setColor(getResources().getColor(R.color.white));
                break;
        }

        setBackground(shape);
    }

    private void init(@NonNull Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.Version1Button);
        int buttonColor = a.getInteger(R.styleable.Version1Button_buttonColorScheme, -1);
        if (buttonColor != -1) {
            this.color = Version1ButtonColor.values()[buttonColor];
            applyColorScheme(color);
        } else {
            setColorScheme(Version1ButtonColor.BLACK);
        }
        a.recycle();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        applyColorScheme(enabled ? this.color : Version1ButtonColor.ALARM_BUTTON_DISABLED);
    }

    public void setEnabled(boolean enabled, Version1ButtonColor disabledButtonColor) {
        super.setEnabled(enabled);

        applyColorScheme(enabled ? this.color : disabledButtonColor);
    }

    public void setEnabled(boolean enabled, Version1ButtonColor disabledButtonColor, int enabledDrawableID, int disabledDrawableID) {
        super.setEnabled(enabled);
        
        if(enabled) {
            applyColorScheme(this.color);
            setImageResource(enabledDrawableID);
        } else {
            applyColorScheme(disabledButtonColor);
            setImageResource(disabledDrawableID);
        }
    }

}
