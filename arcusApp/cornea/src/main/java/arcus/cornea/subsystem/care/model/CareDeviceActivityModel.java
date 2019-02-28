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
package arcus.cornea.subsystem.care.model;

import com.iris.client.model.DeviceModel;

import java.util.Date;

public class CareDeviceActivityModel {
    private String address;
    private Date time;
    private String title;
    private String description;
    private DeviceModel deviceModel;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDeviceModel(DeviceModel deviceModel) {
        this.deviceModel = deviceModel;
    }

    public DeviceModel getDeviceModel() {
        return deviceModel;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CareDeviceActivityModel that = (CareDeviceActivityModel) o;

        if (address != null ? !address.equals(that.address) : that.address != null) {
            return false;
        }
        if (time != null ? !time.equals(that.time) : that.time != null) {
            return false;
        }
        if (title != null ? !title.equals(that.title) : that.title != null) {
            return false;
        }
        if (description != null ? !description.equals(that.description) : that.description != null) {
            return false;
        }
        return !(deviceModel != null ? !deviceModel.equals(that.deviceModel) : that.deviceModel != null);

    }

    @Override public int hashCode() {
        int result = address != null ? address.hashCode() : 0;
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (deviceModel != null ? deviceModel.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "CareDeviceActivityModel{" +
              "address='" + address + '\'' +
              ", time=" + time +
              ", title='" + title + '\'' +
              ", description='" + description + '\'' +
              ", deviceModel=" + deviceModel +
              '}';
    }
}
