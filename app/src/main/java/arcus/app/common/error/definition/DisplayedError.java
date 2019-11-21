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
package arcus.app.common.error.definition;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import androidx.annotation.NonNull;

import arcus.app.R;
import arcus.app.common.error.base.AcceptableError;

public class DisplayedError extends ErrorMessage implements AcceptableError {
    private int acceptButtonText = R.string.error_generic_accept;

    public DisplayedError(int title, int text, Object... formatArgs) {
        super(title, text, formatArgs);
    }
    public DisplayedError(int title, int text) {
        super(title, text);
    }

    @NonNull
    @Override
    public String getAcceptButtonTitle(@NonNull Resources resources) {
        return resources.getString(R.string.error_generic_accept);
    }

    @Override
    public void onAccept(@NonNull DialogInterface dialog, Activity activity) {
        dialog.dismiss();
    }
}
