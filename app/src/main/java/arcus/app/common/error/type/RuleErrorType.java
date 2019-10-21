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
import arcus.app.common.error.definition.DisplayedError;
import arcus.app.common.error.definition.DisplayedPopupError;
import arcus.app.common.error.base.Error;


public enum RuleErrorType implements ErrorType {

    NOT_EDITED(new DisplayedPopupError(R.string.rules_err_validation, R.string.rules_err_not_edited)),
    NOT_DELETED(new DisplayedError(R.string.rules_err_default_title, R.string.rules_err_not_deleted)),
    NOT_UPDATED(new DisplayedError(R.string.rules_err_default_title, R.string.rules_err_not_updated)),
    NO_RULES(new DisplayedError(R.string.rules_err_default_title, R.string.rules_err_no_rules));

    private Error error;

    RuleErrorType (Error error) {
        this.error = error;
    }

    @Override
    public Error getError() {
        return error;
    }
}
