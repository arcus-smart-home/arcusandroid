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


public enum PersonErrorType implements ErrorType {

    NO_TAG_SELECTED(new DisplayedPopupError(R.string.rules_err_validation, R.string.people_make_a_selection)),
    CANT_NOTIFY_HOBBIT_WITHOUT_PHONE(new DisplayedPopupError(R.string.people_hobbit_no_phone_title, R.string.people_hobbit_no_phone_desc));

    private arcus.app.common.error.base.Error error;

    PersonErrorType (Error error) {
        this.error = error;
    }

    @Override
    public Error getError() {
        return error;
    }
}
