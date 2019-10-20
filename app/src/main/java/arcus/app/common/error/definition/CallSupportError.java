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
import android.content.Intent;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import arcus.app.R;
import arcus.app.common.error.base.RejectableError;
import arcus.app.common.utils.GlobalSetting;

public class CallSupportError extends ErrorMessage implements RejectableError {
    private int acceptButtonText = R.string.error_generic_accept;
    private int rejectButtonText = R.string.error_generic_reject;

    public CallSupportError(int title, int text) {
        super(title, text);
    }

    public CallSupportError(int title, int text, int acceptButtonText) {
        this(title, text);
        this.acceptButtonText = acceptButtonText;
    }

    public CallSupportError(int title, int text, int acceptButtonText, int rejectButtonText) {
        this(title, text, acceptButtonText);
        this.rejectButtonText = rejectButtonText;
    }

    @NonNull
    public String getAcceptButtonTitle (@NonNull Resources resources) {
        return resources.getString(acceptButtonText);
    }

    @NonNull
    public String getRejectButtonTitle (@NonNull Resources resources) {
        return resources.getString(rejectButtonText);
    }

    public void onAccept (@NonNull DialogInterface dialog, Activity activity) {
        dialog.dismiss();
    }

    public void onReject (DialogInterface dialog, @NonNull Activity activity) {
        Intent callSupportIntent = new Intent(Intent.ACTION_DIAL, GlobalSetting.SUPPORT_NUMBER_URI);
        activity.startActivity(callSupportIntent);
    }
}
