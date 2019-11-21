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
package arcus.cornea.subsystem.lawnandgarden.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LawnAndGardenControllerModel implements Comparable<LawnAndGardenControllerModel> {
    private String controllerName;
    private String deviceAddress;
    private List<LawnAndGardenControllerZoneDetailModel> zoneDetails;

    public LawnAndGardenControllerModel() {
        zoneDetails = new ArrayList<>();
    }

    public void setControllerName(String name) {
        this.controllerName = name;
    }

    public @Nullable String getControllerName() {
        return controllerName;
    }

    public int getZoneCount() {
        return zoneDetails.size();
    }

    public List<LawnAndGardenControllerZoneDetailModel> getZoneDetails() {
        return Collections.unmodifiableList(zoneDetails);
    }

    public void sortZoneDetailsAsc() {
        sortZoneDetails(true);
    }

    public void sortZoneDetailsDsc() {
        sortZoneDetails(false);
    }

    protected void sortZoneDetails(boolean ascending) {
        if (ascending) {
            Collections.sort(zoneDetails);
        }
        else {
            Collections.sort(zoneDetails, Collections.reverseOrder());
        }
    }

    public void addZoneDetail(LawnAndGardenControllerZoneDetailModel model) {
        zoneDetails.add(model);
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public void setZoneDetails(List<LawnAndGardenControllerZoneDetailModel> zoneDetails) {
        this.zoneDetails = zoneDetails;
    }

    @Override public int compareTo(@NonNull LawnAndGardenControllerModel another) {
        return String.valueOf(this.controllerName).compareTo(String.valueOf(another.getControllerName()));
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LawnAndGardenControllerModel that = (LawnAndGardenControllerModel) o;

        if (controllerName != null ? !controllerName.equals(that.controllerName) : that.controllerName != null) {
            return false;
        }
        if (deviceAddress != null ? !deviceAddress.equals(that.deviceAddress) : that.deviceAddress != null) {
            return false;
        }
        return !(zoneDetails != null ? !zoneDetails.equals(that.zoneDetails) : that.zoneDetails != null);

    }

    @Override public int hashCode() {
        int result = controllerName != null ? controllerName.hashCode() : 0;
        result = 31 * result + (deviceAddress != null ? deviceAddress.hashCode() : 0);
        result = 31 * result + (zoneDetails != null ? zoneDetails.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "LawnAndGardenControllerModel{" +
              "controllerName='" + controllerName + '\'' +
              ", deviceAddress='" + deviceAddress + '\'' +
              ", zoneDetails=" + zoneDetails +
              '}';
    }
}
