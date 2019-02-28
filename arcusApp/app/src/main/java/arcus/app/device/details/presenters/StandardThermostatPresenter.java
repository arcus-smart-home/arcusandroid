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
package arcus.app.device.details.presenters;

import arcus.app.common.utils.Range;


public class StandardThermostatPresenter extends BaseThermostatPresenter {

    @Override
    Range<Integer> getRestrictedSetpointRange() {
        // No concept of restricted setpoints on standard thermostats
        return new Range<>(null, null);
    }

    @Override
    boolean isLeafEnabled() {
        // No concept of leaf on standard thermostats
        return false;
    }

    @Override
    boolean isControlDisabled() {
        return !isDeviceConnected();
    }

    @Override
    boolean isCloudConnected() {
        return false;
    }
}
