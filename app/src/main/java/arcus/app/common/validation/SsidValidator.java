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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.EditText;

import arcus.app.ArcusApplication;
import arcus.app.R;


public class SsidValidator implements InputValidator {

    private final EditText ssidField;
    private final EditText confirmField;

    public SsidValidator (@NonNull EditText ssidField, @Nullable EditText confirmField) {
        this.ssidField = ssidField;
        this.confirmField = confirmField;
    }

    @Override
    public boolean isValid() {
        if (confirmField != null && !confirmField.getText().toString().equals(ssidField.getText().toString())) {
            confirmField.setError(ArcusApplication.getContext().getString(R.string.swann_ssid_mismatch));
            return false;
        }

        if (ssidField.getText().length() > 32) {
            confirmField.setError(ArcusApplication.getContext().getString(R.string.swann_ssid_too_long));
            return false;
        }

        return true;
    }
}
