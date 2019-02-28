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

import android.content.Context;
import android.widget.EditText;

import arcus.app.R;
import arcus.app.common.utils.StringUtils;


public class PhoneNumberValidator implements InputValidator {

    private final static String[] acceptableFormats = new String[] {
            "^(\\d{1}\\s)?\\d{3}(-)?(\\d{3})(-)?(\\d{4})$",
            "^\\(\\d{3}\\)(\\s)?(\\d{3})(-)?(\\d{4})$"
    };

    private final EditText phoneNumber;
    private final Context context;

    public PhoneNumberValidator (Context context, EditText phoneNumber) {
        this.context = context;
        this.phoneNumber = phoneNumber;
    }

    @Override
    public boolean isValid() {

        String phoneNumberString = phoneNumber.getText().toString();

        if (StringUtils.isEmpty(phoneNumberString)) {
            phoneNumber.setError(context.getString(R.string.account_registration_phone_error_hint));
            return false;
        }

        for (String thisAcceptableFormat : acceptableFormats) {
            if (phoneNumberString.matches(thisAcceptableFormat)) {
                return true;
            }
        }

        phoneNumber.setError(context.getString(R.string.account_registration_phone_error_hint));
        return false;
    }
}
