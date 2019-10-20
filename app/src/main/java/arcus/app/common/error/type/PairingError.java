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
package arcus.app.common.error.type;

import arcus.app.R;
import arcus.app.common.error.base.Error;
import arcus.app.common.error.definition.DisplayedPopupError;


public enum PairingError implements ErrorType {

    DEVICE_ALREADY_CLAIMED,
    DEVICE_NOT_FOUND,
    DEVICE_REQUIRES_RESET,
    DEVICE_INVALID_CREDENTIALS,
    DEVICE_SCHEDULE_REQUIRES_DAYS;

    private String erroredFieldName;

    @Override
    public Error getError() {
        switch (this) {
            case DEVICE_ALREADY_CLAIMED: return new DisplayedPopupError(R.string.pairing_device_claimed_title, R.string.pairing_device_claimed_desc, true, erroredFieldName);
            case DEVICE_NOT_FOUND: return new DisplayedPopupError(R.string.pairing_device_not_found_title, R.string.pairing_device_not_found_desc, true, erroredFieldName);
            case DEVICE_REQUIRES_RESET: return new DisplayedPopupError(R.string.swann_requires_reset, R.string.swann_requires_reset_desc, true, null);
            case DEVICE_INVALID_CREDENTIALS: return new DisplayedPopupError(R.string.swann_requires_reset, R.string.swann_invalid_credentials, true, null);
            case DEVICE_SCHEDULE_REQUIRES_DAYS: return new DisplayedPopupError(R.string.swann_schedule_no_days_selected, R.string.swann_schedule_no_days_selected_desc, false, null);
            default: throw new IllegalStateException("Bug! Unhandled error case: " + this);
        }
    }

    public void setErroredFieldName (String erroredFieldName) {
        this.erroredFieldName = erroredFieldName;
    }
}
