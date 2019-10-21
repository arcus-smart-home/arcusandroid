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
package arcus.cornea.device.blinds.model;

import android.os.Parcel;
import android.os.Parcelable;

public class ShadeClientModel implements Parcelable {

    private String deviceAddress;
    private String deviceName;
    private int level;
    private int batteryLevel;

    public ShadeClientModel() {}

    public ShadeClientModel(String deviceAddress, String deviceName) {
        this.deviceAddress = deviceAddress;
        this.deviceName = deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Number batteryLevel) {
        this.batteryLevel = batteryLevel == null ? 0 : batteryLevel.intValue();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ShadeClientModel that = (ShadeClientModel) o;

        if (deviceAddress != null ? !deviceAddress.equals(that.deviceAddress) : that.deviceAddress != null) {
            return false;
        }
        if (deviceName != null ? !deviceName.equals(that.deviceName) : that.deviceName != null) {
            return false;
        }

        return level  == that.level;

    }

    @Override
    public int hashCode() {
        int result = deviceAddress != null ? deviceAddress.hashCode() : 0;
        result = 31 * result + (deviceName != null ? deviceName.hashCode() : 0);
        result = 31 * result + level;
        return result;
    }

    @Override
    public String toString() {
        return "ShadeClientModel{" +
                "deviceAddress='" + deviceAddress + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", level=" + level +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceAddress);
        dest.writeString(this.deviceName);
        dest.writeInt(level);
    }

    protected ShadeClientModel(Parcel in) {
        this.deviceAddress = in.readString();
        this.deviceName = in.readString();
        this.level = in.readInt();
    }

    public static final Creator<ShadeClientModel> CREATOR = new Creator<ShadeClientModel>() {
        public ShadeClientModel createFromParcel(Parcel source) {
            return new ShadeClientModel(source);
        }

        public ShadeClientModel[] newArray(int size) {
            return new ShadeClientModel[size];
        }
    };
}
