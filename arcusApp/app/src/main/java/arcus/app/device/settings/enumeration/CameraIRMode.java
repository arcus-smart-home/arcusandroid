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

import android.content.Context;

import com.iris.client.capability.Camera;
import arcus.app.R;
import arcus.app.device.settings.core.Localizable;


public enum CameraIRMode implements Localizable {
    AUTO(R.string.setting_camera_led_mode_auto),
    ON(R.string.setting_camera_led_mode_on),
    OFF(R.string.setting_camera_led_mode_off);

    private final int stringResId;

    CameraIRMode (int stringResId) {
        this.stringResId = stringResId;
    }

    public static CameraIRMode fromCapabilityModeString (String mode) {
        if (mode == null) {
            return AUTO;
        }

        switch(mode) {
            case Camera.IRLEDMODE_OFF: return OFF;
            case Camera.IRLEDMODE_ON: return ON;
            default: return AUTO;
        }
    }

    public String getCapabilityModeString() {
        switch (this) {
            case AUTO: return Camera.IRLEDMODE_AUTO;
            case OFF: return Camera.IRLEDMODE_OFF;
            case ON: return Camera.IRLEDMODE_ON;

            default: throw new IllegalStateException("Bug! Unimplemented case: " + this);
        }
    }

    public String toString (Context context) {
        return context.getString(getStringResId());
    }

    @Override
    public int getStringResId() {
        return stringResId;
    }
}
