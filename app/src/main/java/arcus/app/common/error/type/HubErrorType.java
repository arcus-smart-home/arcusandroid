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
import arcus.app.common.error.definition.CallSupportError;
import arcus.app.common.error.definition.DisplayedError;
import arcus.app.common.error.base.Error;
import arcus.app.common.utils.Errors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum HubErrorType implements ErrorType {
    HUB_ALREADY_PAIRED(new DisplayedError(R.string.error_hub_already_paired, R.string.error_hub_already_paired)),
    HUB_ID_INVALID(new DisplayedError(R.string.hub_id_invalid_title, R.string.hub_id_invalid_text)),
    HUB_SEARCH_TIMEOUT(new CallSupportError(R.string.hub_search_timeout_title, R.string.hub_search_timeout_text, R.string.try_again_text)),
    HUB_FIRMWARE_UPDATE_ALARM_ON(new DisplayedError(R.string.hub_firmware_alarm_on_title, R.string.hub_firmware_alarm_on_text));

    private static final Logger logger = LoggerFactory.getLogger(HubErrorType.class);
    private Error error;

    HubErrorType(Error error) {
        this.error = error;
    }

    @Override
    public Error getError() {
        return this.error;
    }

    public static Error fromThrowable(Throwable throwable) {
        if (throwable instanceof ErrorResponseException) {
            return fromErrorResponseException((ErrorResponseException) throwable);
        }
        else {
            logger.debug("Unhandled Exception. Returning Generic Error Message. Throwable: [{}]", throwable.getClass().getSimpleName());
            return ErrorLocator.genericFatalError;
        }
    }

    private static Error fromErrorResponseException(@NonNull ErrorResponseException response) {
        switch (response.getCode()) {
            case Errors.Hub.ALREADY_REGISTERED:
                return HUB_ID_INVALID.getError();

            case Errors.Hub.NOT_FOUND:
                return  HUB_SEARCH_TIMEOUT.getError();

            case Errors.Hub.SERVER_ERROR:
            case Errors.Hub.MISSING_ARGUMENT:
            case Errors.Process.RETRIES_EXCEEDED:
            default:
                return ErrorLocator.genericFatalError;
        }
    }
}
