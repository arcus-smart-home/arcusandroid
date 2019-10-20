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



public enum SchedulingError implements ErrorType {

    CANT_SCHEDULE_TCC(new DisplayedPopupError(R.string.schedule_unavailable, R.string.honeywell_schedule_unavailable)),
    CANT_SCHEDULE_NEST(new DisplayedPopupError(R.string.schedule_unavailable, R.string.nest_schedule_unavailable));

    private final Error error;

    SchedulingError(Error theError) {
        this.error = theError;
    }

    @Override
    public Error getError() {
        return error;
    }
}
