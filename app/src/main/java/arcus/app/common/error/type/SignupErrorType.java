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

import androidx.annotation.NonNull;

import com.iris.client.exception.ErrorResponseException;
import arcus.app.R;
import arcus.app.common.error.ErrorLocator;
import arcus.app.common.error.base.Error;
import arcus.app.common.error.definition.DisplayedPopupError;

public enum  SignupErrorType implements ErrorType {
    EMAIL_IN_USE(new DisplayedPopupError(R.string.email_already_registered_title, R.string.email_already_registered_text, true));

    private Error error;

    SignupErrorType(Error error) {
        this.error = error;
    }

    @Override
    public Error getError() {
        return error;
    }

    public static Error fromThrowable(Throwable throwable) {
        if (throwable instanceof ErrorResponseException) {
            return fromErrorResponseException((ErrorResponseException) throwable);
        }
        else if (throwable.getCause() instanceof ErrorResponseException) {
            return fromErrorResponseException((ErrorResponseException) throwable.getCause());
        }

        return ErrorLocator.genericFatalError;
    }

    private static Error fromErrorResponseException(@NonNull ErrorResponseException ex) {
        switch (ex.getCode()) {
            case SignupErrorResponses.EMAIL_IN_USE_ERROR_CODE:
            case SignupErrorResponses.EMAIL_IN_USE_INVITATION_ERROR_CODE:
                return EMAIL_IN_USE.getError();

            case SignupErrorResponses.ARGUMENT_ERROR:
            default:
                return ErrorLocator.genericFatalError;
        }
    }

    interface SignupErrorResponses {
        String ARGUMENT_ERROR = "missing.required.argument";
        String EMAIL_IN_USE_ERROR_CODE = "error.signup.emailinuse";
        String EMAIL_IN_USE_INVITATION_ERROR_CODE = "EmailInUseException";
    }
}
