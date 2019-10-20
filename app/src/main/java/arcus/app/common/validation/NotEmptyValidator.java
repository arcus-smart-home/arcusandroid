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
import android.support.annotation.NonNull;
import android.widget.EditText;

import arcus.app.R;

import org.apache.commons.lang3.StringUtils;


public class NotEmptyValidator implements InputValidator {

    private final EditText field;
    @NonNull
    private final String errorString;

    public NotEmptyValidator (@NonNull Context context, EditText field) {
        this(context, field, R.string.people_err_please_enter);
    }

    public NotEmptyValidator (@NonNull Context context, EditText field, int errorStringResId) {
        this.field = field;
        this.errorString = context.getString(errorStringResId);
    }

    public NotEmptyValidator (@NonNull Context context, EditText field, int errorStringResId, Object... formatArgs) {
        this.field = field;
        this.errorString = context.getString(errorStringResId, formatArgs);
    }

    @Override
    public boolean isValid() {

        // Disallow input that is all whitespace
        if (StringUtils.isEmpty(field.getText().toString())) {
            field.setError(errorString);
            return false;
        }

        return true;
    }
}
