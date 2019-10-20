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

import com.iris.client.exception.UnauthorizedException;
import arcus.app.R;
import arcus.app.common.error.ErrorLocator;
import arcus.app.common.error.base.Error;
import arcus.app.common.error.definition.DisplayedPopupError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum LoginErrorType implements ErrorType {
    INVALID_USERNAME_PASSWORD(new DisplayedPopupError(R.string.invalid_username_password_title, R.string.invalid_username_password_text)),
    PASSWORD_RESET_INVALID_CODE(new DisplayedPopupError(R.string.login_password_reset_title, R.string.login_password_reset_body)),
    INVALID_EMAIL(new DisplayedPopupError(R.string.invalid_email_title, R.string.invalid_email_body));

    private static final Logger logger = LoggerFactory.getLogger(LoginErrorType.class);
    private Error error;

    LoginErrorType(Error error) {
        this.error = error;
    }

    @Override
    public Error getError() {
        return this.error;
    }

    public static Error fromThrowable(Throwable throwable) {
        if (throwable instanceof UnauthorizedException) {
            return INVALID_USERNAME_PASSWORD.getError();
        }
        else {
            logger.debug("Unhandled Exception. Returning Generic Error Message. Throwable: [{}]", throwable.getClass().getSimpleName());
            return ErrorLocator.genericFatalError;
        }
    }
}
