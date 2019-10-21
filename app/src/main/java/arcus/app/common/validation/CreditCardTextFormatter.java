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

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;

public class CreditCardTextFormatter implements TextWatcher {
    private final String dash = "-";
    private boolean selfChange = false;

    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
    @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override public void afterTextChanged(Editable creditCardNumber) {
        if (selfChange) {
            return;
        }

        String replacement = replaceChars(creditCardNumber.toString());
        selfChange = true;
        creditCardNumber.replace(0, creditCardNumber.length(), replacement, 0, replacement.length());
        selfChange = false;
    }

    String replaceChars(String current) {
        if (TextUtils.isEmpty(current)) {
            return "";
        }

        current = current.replace(dash, "");
        if (current.startsWith("34") || current.startsWith("37")) {
            return formatAMEXCard(current);
        } else {
            return formatNonAMEXCard(current);
        }
    }

    String formatAMEXCard(String current) {
        int length = current.length();
        if (length < 5) {
            return current;
        }

        String formattedValue = current.substring(0, 4).concat(dash);
        if (length < 11) {
            return formattedValue.concat(current.substring(4, length));
        }

        return formattedValue.concat(current.substring(4, 10).concat(dash).concat(current.substring(10, length)));
    }

    String formatNonAMEXCard(String current) {
        int length = current.length();
        if (length < 5) {
            return current;
        }

        String formattedValue = current.substring(0, 4).concat(dash);
        if (length < 9) {
            return formattedValue.concat(current.substring(4, length));
        }

        formattedValue += current.substring(4, 8).concat(dash);
        if (length < 13) {
            return formattedValue.concat(current.substring(8, length));
        }

        return formattedValue.concat(current.substring(8, 12).concat(dash).concat(current.substring(12, length)));
    }
}
