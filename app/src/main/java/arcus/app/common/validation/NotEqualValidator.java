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
import androidx.annotation.NonNull;
import android.widget.EditText;

import arcus.app.R;


public class NotEqualValidator implements InputValidator {

    private final EditText field;
    @NonNull
    private final Context context;
    @NonNull
    private final String errorString;

    private String value;

    public NotEqualValidator(@NonNull Context context, EditText field, String value) {
        this(context, field, R.string.password_err_not_equal, value);
    }

    public NotEqualValidator(@NonNull Context context, EditText field, int errorStringResId, String value) {
        this.field = field;
        this.context = context;
        this.errorString = context.getString(errorStringResId);
        this.value = value;
    }

    public NotEqualValidator(@NonNull Context context, EditText field, int errorStringResId, String value, Object... formatArgs) {
        this.field = field;
        this.context = context;
        this.errorString = context.getString(errorStringResId, formatArgs);
        this.value = value;
    }

    @Override
    public boolean isValid() {

        if (!field.getText().toString().equals(value)) {
            field.setError(errorString);
            return false;
        }

        return true;
    }
}
