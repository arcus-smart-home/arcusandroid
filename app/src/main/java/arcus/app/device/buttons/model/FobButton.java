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
package arcus.app.device.buttons.model;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.device.settings.core.Abstractable;


public class FobButton implements Button, Abstractable {
    private static final String SECURITY_ON = "SET_SECURITY_ALARM_TO_ON";
    private static final String SECURITY_OFF = "SET_SECURITY_ALARM_TO_OFF";
    private static final String SECURITY_PARTIAL = "SET_SECURITY_ALARM_TO_PARTIAL";
    private static final String PLAY_CHIME = "PLAY_CHIME";
    private static final String ACTIVATE_RULE = "ACTIVATE_A_RULE";

    private final String name;
    private final int stringResId;
    private final int imageResId;
    private String abstractText;
    private String buttonAction;

    public static FobButton[] constructTwoButtonFob() {

        return new FobButton[]{
                new FobButton("HOME", R.string.care_pendant_home, R.drawable.icon_keyfob_home),
                new FobButton("AWAY", R.string.care_pendant_away, R.drawable.icon_keyfob_away)
        };
    }

    public static FobButton[] constructFourButtonFob() {

        return new FobButton[]{
                new FobButton("CIRCLE", R.string.setting_button_circle, R.drawable.icon_keyfob_circle),
                new FobButton("DIAMOND", R.string.setting_button_diamond, R.drawable.icon_keyfob_diamond),
                new FobButton("SQUARE", R.string.setting_button_square, R.drawable.icon_keyfob_square),
                new FobButton("HEXAGON", R.string.setting_button_hexagon, R.drawable.icon_keyfob_hex)
        };
    }

    public static FobButton[] constructGen3FourButtonFob() {

        return new FobButton[]{
                new FobButton("AWAY", R.string.setting_button_away, R.drawable.icon_keyfob_away_white),
                new FobButton("HOME", R.string.setting_button_home, R.drawable.icon_keyfob_home_white),
                new FobButton("A", R.string.setting_button_a, R.drawable.icon_keyfob_a_white),
                new FobButton("B", R.string.setting_button_b, R.drawable.icon_keyfob_b_white)
        };
    }

    @Override
    public String toString() {
        return name;
    }

    private FobButton(String name, int stringResId, int imageResId) {
        this.stringResId = stringResId;
        this.imageResId = imageResId;
        this.name = name;
    }

    @Override
    public int getStringResId() {
        return stringResId;
    }

    public int getImageResId() {
        return imageResId;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Nullable
    public String getButtonName() {
        return name;
    }

    @NonNull
    @Override
    public String getAbstract(Context context) {
        return abstractText;
    }

    public String getAbstractText() {
        return abstractText;
    }

    public String getButtonAction() {
        return buttonAction;
    }

    public void setButtonAction(String buttonAction) {
        this.buttonAction = buttonAction;

        System.err.println("   ******   Setting Button Action: " + buttonAction + "   ******   ");

        switch (buttonAction) {
            case SECURITY_ON:
                this.abstractText = ArcusApplication.getContext().getResources().getString(R.string.fob_button_alarm_on);
                break;
            case SECURITY_OFF:
                this.abstractText = ArcusApplication.getContext().getResources().getString(R.string.fob_button_alarm_off);
                break;
            case SECURITY_PARTIAL:
                this.abstractText = ArcusApplication.getContext().getResources().getString(R.string.fob_button_alarm_partial);
                break;
            case PLAY_CHIME:
                this.abstractText = ArcusApplication.getContext().getResources().getString(R.string.fob_button_play_chime);
                break;
            case ACTIVATE_RULE:
                this.abstractText = ArcusApplication.getContext().getResources().getString(R.string.fob_button_activate_rule);
            break;
        }
    }
}
