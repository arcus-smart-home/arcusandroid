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
package arcus.cornea.subsystem.safety.model;

import com.iris.client.capability.SafetySubsystem;

public class DeviceCounts {
    private final int totalDevices;
    private final int offlineDevices;
    private final int activeDevices;

    public DeviceCounts(SafetySubsystem model) {
        this.totalDevices = model.getTotalDevices().size();
        this.offlineDevices = model.getTotalDevices().size() - model.getActiveDevices().size();
        this.activeDevices = model.getActiveDevices().size();
    }

    public int getTotalDevices() { return totalDevices; }
    public int getOfflineDevices() { return offlineDevices; }
    public int getActiveDevices() { return activeDevices; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceCounts that = (DeviceCounts) o;

        if (totalDevices != that.totalDevices) return false;
        if (offlineDevices != that.offlineDevices) return false;
        return activeDevices == that.activeDevices;

    }

    @Override
    public int hashCode() {
        int result = totalDevices;
        result = 31 * result + offlineDevices;
        result = 31 * result + activeDevices;
        return result;
    }

    @Override
    public String toString() {
        return "DeviceCounts{" +
                "totalDevices=" + totalDevices +
                ", offlineDevices=" + offlineDevices +
                ", activeDevices=" + activeDevices +
                '}';
    }
}
