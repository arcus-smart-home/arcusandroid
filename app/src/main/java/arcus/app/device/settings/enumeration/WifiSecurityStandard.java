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
import androidx.annotation.NonNull;

import arcus.app.R;
import arcus.app.device.settings.core.Abstractable;
import arcus.app.device.settings.core.Localizable;


public enum WifiSecurityStandard implements Localizable, Abstractable {

    NONE(R.string.setting_wifi_security_none, R.string.setting_wifi_security_none_abstract),
    WEP(R.string.setting_wifi_security_wep, R.string.setting_wifi_security_wep_abstract),
    WPA_PSK(R.string.setting_wifi_security_wpa, R.string.setting_wifi_security_wpa_abstract),
    WPA2_PSK(R.string.setting_wifi_security_wpa2_personal, R.string.setting_wifi_security_wpa2_personal_abstract),
    WPA_ENTERPRISE(R.string.setting_wifi_security_wpa_enterprise, R.string.setting_wifi_security_wpa_enterprise_abstract),
    WPA2_ENTERPRISE(R.string.setting_wifi_security_wpa2_enterprise, R.string.setting_wifi_security_wpa2_enterprise_abstract);

    private final int stringResId;
    private final int abstractStringResId;

    WifiSecurityStandard(int stringResId, int abstractStringResId) {
        this.stringResId = stringResId;
        this.abstractStringResId = abstractStringResId;
    }

    @Override
    public int getStringResId() {
        return stringResId;
    }

    @Override
    public String getAbstract (@NonNull Context context) {
        return context.getString(abstractStringResId);
    }

    @NonNull
    public static WifiSecurityStandard fromSecurityString (String value) {
        for (WifiSecurityStandard thisStandard : values()) {
            if (thisStandard.toString().equals(value)) {
                return thisStandard;
            }
        }

        return NONE;
    }
}
