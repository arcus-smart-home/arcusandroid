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
package arcus.cornea.device.climate;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Map;

public class SpaceHeaterControllerDetailsModel implements Parcelable {
    private String deviceAddress;
    private String deviceName;

    private boolean isInOTA;
    private boolean isOnline;

    private SpaceHeaterScheduleMode scheduleMode;
    private boolean hasRequestInFlight = false;

    private boolean leftButtonEnabled = false;
    private boolean rightButtonEnabled = false;
    private boolean bottomButtonEnabled = false;

    private boolean deviceModeOn = false;
    private boolean deviceEcoOn = false;

    private int setPoint = 0;
    private int currentTemp = 0;
    private Map<String, String> errors;
    private String nextEventDisplay;


    public SpaceHeaterControllerDetailsModel() {
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

    public SpaceHeaterScheduleMode getScheduleMode() {
        return scheduleMode;
    }

    public void setScheduleMode(SpaceHeaterScheduleMode scheduleMode) {
        this.scheduleMode = scheduleMode;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public boolean hasRequestInFlight() {
        return hasRequestInFlight;
    }

    public void setHasRequestInFlight(boolean hasRequestInFlight) {
        this.hasRequestInFlight = hasRequestInFlight;
    }

    public boolean isInOTA() {
        return isInOTA;
    }

    public void setIsInOTA(boolean isInOTA) {
        this.isInOTA = isInOTA;
    }

    public boolean isLeftButtonEnabled() {
        return leftButtonEnabled;
    }

    public void setLeftButtonEnabled(boolean leftButtonEnabled) {
        this.leftButtonEnabled = leftButtonEnabled;
    }

    public boolean isRightButtonEnabled() {
        return rightButtonEnabled;
    }

    public void setRightButtonEnabled(boolean rightButtonEnabled) {
        this.rightButtonEnabled = rightButtonEnabled;
    }

    public boolean isBottomButtonEnabled() {
        return bottomButtonEnabled;
    }

    public void setBottomButtonEnabled(boolean bottomButtonEnabled) {
        this.bottomButtonEnabled = bottomButtonEnabled;
    }

    public int getSetPoint() {
        return setPoint;
    }

    public void setSetPoint(int setPoint) {
        this.setPoint = setPoint;
    }

    public boolean isDeviceModeOn() {
        return deviceModeOn;
    }

    public void setDeviceModeOn(boolean deviceModeOn) {
        this.deviceModeOn = deviceModeOn;
    }

    public boolean isDeviceEcoOn() {
        return deviceEcoOn;
    }

    public void setDeviceEcoOn(boolean deviceEcoOn) {
        this.deviceEcoOn = deviceEcoOn;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public void setCurrentTemp(int currentTemp) {
        this.currentTemp = currentTemp;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    public String getNextEventDisplay() {
        return nextEventDisplay;
    }

    public void setNextEventDisplay(String nextEventDisplay) {
        this.nextEventDisplay = nextEventDisplay;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SpaceHeaterControllerDetailsModel that = (SpaceHeaterControllerDetailsModel) o;

        if (isInOTA != that.isInOTA) {
            return false;
        }
        if (isOnline != that.isOnline) {
            return false;
        }
        if (deviceAddress != null ? !deviceAddress.equals(that.deviceAddress) : that.deviceAddress != null) {
            return false;
        }
        if (deviceName != null ? !deviceName.equals(that.deviceName) : that.deviceName != null) {
            return false;
        }
        if (scheduleMode != that.scheduleMode) {
            return false;
        }
        if (leftButtonEnabled != that.leftButtonEnabled) {
            return false;
        }
        if (rightButtonEnabled != that.rightButtonEnabled) {
            return false;
        }
        return hasRequestInFlight == that.hasRequestInFlight;

    }

    @Override public int hashCode() {
        int result = deviceAddress != null ? deviceAddress.hashCode() : 0;
        result = 31 * result + (deviceName != null ? deviceName.hashCode() : 0);
        result = 31 * result + (scheduleMode != null ? scheduleMode.hashCode() : 0);
        result = 31 * result + (isInOTA ? 1 : 0);
        result = 31 * result + (isOnline ? 1 : 0);
        result = 31 * result + (hasRequestInFlight ? 1 : 0);
        result = 31 * result + (leftButtonEnabled ? 1 : 0);
        result = 31 * result + (rightButtonEnabled ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "SpaceHeaterControllerDetailsModel{" +
              "deviceAddress='" + deviceAddress + '\'' +
              ", deviceName='" + deviceName + '\'' +
              ", scheduleMode=" + scheduleMode +
              ", isInOTA=" + isInOTA +
              ", isOnline=" + isOnline +
              ", hasRequestInFlight=" + hasRequestInFlight +
              ", leftButtonEnabled=" + leftButtonEnabled +
              ", rightButtonEnabled=" + rightButtonEnabled +
              '}';
    }


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceAddress);
        dest.writeString(this.deviceName);
        dest.writeInt(this.scheduleMode == null ? -1 : this.scheduleMode.ordinal());
        dest.writeByte(isInOTA ? (byte) 1 : (byte) 0);
        dest.writeByte(isOnline ? (byte) 1 : (byte) 0);
        dest.writeByte(hasRequestInFlight ? (byte) 1 : (byte) 0);
        dest.writeByte(leftButtonEnabled ? (byte) 1 : (byte) 0);
        dest.writeByte(rightButtonEnabled ? (byte) 1 : (byte) 0);
    }

    protected SpaceHeaterControllerDetailsModel(Parcel in) {
        this.deviceAddress = in.readString();
        this.deviceName = in.readString();
        int tmpScheduleMode = in.readInt();
        this.scheduleMode = tmpScheduleMode == -1 ? null : SpaceHeaterScheduleMode.values()[tmpScheduleMode];
        this.isInOTA = in.readByte() != 0;
        this.isOnline = in.readByte() != 0;
        this.hasRequestInFlight = in.readByte() != 0;
        this.leftButtonEnabled = in.readByte() != 0;
        this.rightButtonEnabled = in.readByte() != 0;
    }

    public static final Creator<SpaceHeaterControllerDetailsModel> CREATOR =
          new Creator<SpaceHeaterControllerDetailsModel>() {
              public SpaceHeaterControllerDetailsModel createFromParcel(Parcel source) {
                  return new SpaceHeaterControllerDetailsModel(source);
              }

              public SpaceHeaterControllerDetailsModel[] newArray(int size) {
                  return new SpaceHeaterControllerDetailsModel[size];
              }
          };
}
