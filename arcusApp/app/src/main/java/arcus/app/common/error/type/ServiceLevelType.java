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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum  ServiceLevelType implements ErrorType {
    RULES_REQUIRE_PREMIUM(new DisplayedPopupError(R.string.rule_requires_premium_title, R.string.rule_requires_premium_text)),
    RULES_DIRECT_WHERE_TO_ADD(new DisplayedPopupError(R.string.rule_instruct_where_to_add_title, R.string.rule_instruct_where_to_add_text, false));

    private static final Logger logger = LoggerFactory.getLogger(ServiceLevelType.class);
    private Error error;

    ServiceLevelType(Error error) {
        this.error = error;
    }

    @Override
    public Error getError() {
        return this.error;
    }
}