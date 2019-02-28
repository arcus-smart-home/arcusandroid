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

/**
 *
 * Other errors relating to UI Flow or Errors that aren't well defined because of an exception we can parse
 * or don't fall into a particular bucket yet.
 *
 */
public enum OtherErrorTypes implements ErrorType {
    HUB_PAIRED_CANT_ADD(HubErrorType.HUB_ALREADY_PAIRED.getError()),
    PIN_NUMBER_MISMATCH(new DisplayedPopupError(R.string.pin_code_not_match_title, R.string.pin_code_not_match_text)),
    EMAIL_REQUIRED(new DisplayedPopupError(R.string.email_address_required_title, R.string.email_address_required_text)),
    EMAIL_ADDRESSES_DO_NOT_MATCH(new DisplayedPopupError(R.string.email_addresses_not_match_title, R.string.email_addresses_not_match_text)),
    PHONE_NUMBER_REQUIRED(new DisplayedPopupError(R.string.phone_number_required_title, R.string.phone_number_required_text)),
    PUSH_NOTIFICATION_HEADS_UP(new DisplayedPopupError(R.string.push_heads_up_title, R.string.push_heads_up_desc, false)),
    CANT_WRITE_CONTACTS(new DisplayedPopupError(R.string.cant_access_contacts, R.string.cant_access_contacts_subtitle));

    private Error error;

    OtherErrorTypes(Error error) {
        this.error = error;
    }

    @Override
    public Error getError() {
        return error;
    }
}
