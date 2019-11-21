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
package arcus.app.device.settings.enumeration;

import androidx.annotation.NonNull;

import arcus.app.R;
import arcus.app.device.settings.core.Localizable;

/**
 * An enumeration of LED inversion setting states.
 */
public enum InvertedLedState implements Localizable {
    ON_WHEN_SWITCH_IS_ON(R.string.setting_led_status_normal),
    ON_WHEN_SWITCH_IS_OFF(R.string.setting_led_status_inverted);

    private final int displayedValueId;

    InvertedLedState(int displayedValueResId) {
        this.displayedValueId = displayedValueResId;
    }

    @NonNull
    public static InvertedLedState fromInverted(boolean isInverted) {
        return isInverted ? ON_WHEN_SWITCH_IS_OFF : ON_WHEN_SWITCH_IS_ON;
    }

    public int getStringResId () {
        return this.displayedValueId;
    }
}
