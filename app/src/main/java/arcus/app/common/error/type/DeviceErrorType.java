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
import arcus.app.common.error.ErrorLocator;
import arcus.app.common.error.base.Error;
import arcus.app.common.error.definition.TryAgainOrCancelError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: Implement Device Specific Errors
public enum DeviceErrorType implements ErrorType {
    UNABLE_TO_SAVE_CHANGES(ErrorLocator.genericFatalError),
    PAIRING_MODE_TIMEOUT(new TryAgainOrCancelError(R.string.device_pairing_mode_timeout_title, R.string.device_pairing_mode_timeout_text));

    private static final Logger logger = LoggerFactory.getLogger(DeviceErrorType.class);
    private Error error;

    DeviceErrorType(Error error) {
        this.error = error;
    }

    @Override
    public Error getError() {
        return this.error;
    }

    public static Error fromThrowable(Throwable throwable) {
        return ErrorLocator.genericFatalError;
    }
}
