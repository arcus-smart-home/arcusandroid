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
package arcus.app.common.error;

import androidx.annotation.NonNull;

import arcus.app.common.error.base.Error;
import arcus.app.common.error.definition.GenericFatalError;
import arcus.app.common.error.type.BillingErrorType;
import arcus.app.common.error.type.HubErrorType;
import arcus.app.common.error.type.LoginErrorType;
import arcus.app.common.error.type.SignupErrorType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ErrorLocator {

    private static final Logger logger = LoggerFactory.getLogger(ErrorLocator.class);
    public static final GenericFatalError genericFatalError = new GenericFatalError();

    public static Error locate(@NonNull ErrorDuring condition, Throwable throwable) {
        switch (condition) {
            case HUB_PAIRING:
                return HubErrorType.fromThrowable(throwable);
            case ENTER_BILLING_INFO:
                return BillingErrorType.fromThrowable(throwable);
            case LOGIN:
                return LoginErrorType.fromThrowable(throwable);
            case SIGNUP:
                return SignupErrorType.fromThrowable(throwable);

            default:
                logger.debug("Unable to locate error for condition: [{}] using generic fatal error.", condition.name());
                return ErrorLocator.genericFatalError;
        }
    }
}
