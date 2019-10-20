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

public class SomfyBlindsSettingModel implements Parcelable {
    private String deviceAddress;
    private String deviceName;
    private boolean isInOTA;
    private boolean isOnline;
    private String type;
    private boolean reversed;
    private String currentstate;

    public SomfyBlindsSettingModel() {}

    public SomfyBlindsSettingModel(String deviceAddress, String deviceName) {
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

    public boolean isOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean isInOTA() {
        return isInOTA;
    }

    public void setIsInOTA(boolean isInOTA) {
        this.isInOTA = isInOTA;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isReversed() {
        return reversed;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

    public String getCurrentstate() {
        return currentstate;
    }

    public void setCurrentstate(String currentstate) {
        this.currentstate = currentstate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SomfyBlindsSettingModel that = (SomfyBlindsSettingModel) o;

        if (deviceAddress != null ? !deviceAddress.equals(that.deviceAddress) : that.deviceAddress != null) {
            return false;
        }
        if (deviceName != null ? !deviceName.equals(that.deviceName) : that.deviceName != null) {
            return false;
        }
        if (isInOTA != that.isInOTA) {
            return false;
        }
        if (isOnline != that.isOnline) {
            return false;
        }
        if(type != null ? !type.equals(that.type) : that.type != null) {
            return false;
        }

        if (currentstate != null ? !currentstate.equals(that.currentstate) : that.currentstate!= null) {
            return false;
        }

        return reversed  == that.reversed;

    }

    @Override
    public int hashCode() {
        int result = deviceAddress != null ? deviceAddress.hashCode() : 0;
        result = 31 * result + (deviceName != null ? deviceName.hashCode() : 0);
        result = 31 * result + (isInOTA ? 1 : 0);
        result = 31 * result + (isOnline ? 1 : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (reversed ? 1 : 0);
        result = 31 * result + (currentstate != null ? currentstate.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SomfyBlindsSettingModel{" +
                "deviceAddress='" + deviceAddress + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", isInOTA=" + isInOTA +
                ", isOnline=" + isOnline +
                ", type=" + type +
                ", reversed=" + reversed +
                ", currentstate='" + currentstate + '\'' +
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
        dest.writeByte(isInOTA ? (byte) 1 : (byte) 0);
        dest.writeByte(isOnline ? (byte) 1 : (byte) 0);
        dest.writeString(this.type);
        dest.writeByte(reversed ? (byte) 1 : (byte) 0);
        dest.writeString(this.currentstate);
    }

    protected SomfyBlindsSettingModel(Parcel in) {
        this.deviceAddress = in.readString();
        this.deviceName = in.readString();
        this.isInOTA = in.readByte() != 0;
        this.isOnline = in.readByte() != 0;
        this.type = in.readString();
        this.reversed = in.readByte() != 0;
        this.currentstate = in.readString();
    }

    public static final Creator<SomfyBlindsSettingModel> CREATOR = new Creator<SomfyBlindsSettingModel>() {
        public SomfyBlindsSettingModel createFromParcel(Parcel source) {
            return new SomfyBlindsSettingModel(source);
        }

        public SomfyBlindsSettingModel[] newArray(int size) {
            return new SomfyBlindsSettingModel[size];
        }
    };
}
