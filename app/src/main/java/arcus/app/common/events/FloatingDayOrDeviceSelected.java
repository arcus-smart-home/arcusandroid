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
package arcus.app.common.events;

import com.iris.client.model.DeviceModel;

public class FloatingDayOrDeviceSelected {
    private long selectedDay = -1;
    private DeviceModel deviceModel;

    private boolean isSelectedDay = false;
    private boolean isSelectedDevice = false;

    public FloatingDayOrDeviceSelected(long selectedDay) {
        this.selectedDay = selectedDay;
        this.isSelectedDay = true;
    }

    public FloatingDayOrDeviceSelected(DeviceModel selectedDeviceID) {
        this.deviceModel = selectedDeviceID;
        this.isSelectedDevice = true;
    }

    public boolean isSelectedDevice() {
        return isSelectedDevice;
    }

    public boolean isSelectedDay() {
        return isSelectedDay;
    }

    public DeviceModel getDeviceModel() {
        return this.deviceModel;
    }

    public long getSelectedDay() {
        return this.selectedDay;
    }
}
