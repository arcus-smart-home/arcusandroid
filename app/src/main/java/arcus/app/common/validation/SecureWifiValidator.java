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
package arcus.app.common.validation;

import androidx.annotation.NonNull;
import android.widget.EditText;

import arcus.cornea.device.camera.model.WiFiSecurityType;
import arcus.app.ArcusApplication;
import arcus.app.R;

import java.util.regex.Pattern;


public class SecureWifiValidator implements InputValidator {

    // 8-64 printable ascii characters or exactly 64 hex chars
    private static final Pattern validWpaPassword = Pattern.compile("\\A[\\x20-\\x7e]{8,63}|[a-fA-F0-9]{64}\\Z");

    // Exactly 10 or 23 hex characters
    private static final Pattern validWepPassword = Pattern.compile("\\A[a-fA-F0-9]{10}|[a-fA-F0-9]{23}\\Z");

    private final EditText passwordField;
    private final WiFiSecurityType securityType;

    public SecureWifiValidator (@NonNull EditText passwordField, WiFiSecurityType securityType) {
        this.passwordField = passwordField;
        this.securityType = securityType;
    }

    @Override
    public boolean isValid() {

        // Assume null security type means unknown security type; can't validate
        if (securityType == null) {
            return true;
        }

        if (securityType == WiFiSecurityType.NONE) {
            return true;
        }

        switch (securityType) {
            case WPA2_PSK:
            case WPA2_ENTERPRISE:
            case WPA_ENTERPRISE:
            case WPA_PSK:
                if (!validWpaPassword.matcher(passwordField.getText()).matches()) {
                    passwordField.setError(ArcusApplication.getContext().getString(R.string.wifi_bad_wpa_password));
                    return false;
                } else {
                    return true;
                }

            case WEP:
                if (!validWepPassword.matcher(passwordField.getText()).matches()) {
                    passwordField.setError(ArcusApplication.getContext().getString(R.string.wifi_bad_wep_password));
                    return false;
                } else {
                    return true;
                }

            default:
                throw new IllegalArgumentException("Bug! Unimplemented security WiFiSecurityType. " + securityType);
        }

    }
}
