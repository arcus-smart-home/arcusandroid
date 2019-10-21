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
package arcus.cornea.device.lawnandgarden;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.concurrent.TimeUnit;

public class IrrigationControllerDetailsModel implements Parcelable {
    private String deviceAddress;
    private String deviceName;

    private String nextEventZone;
    private String nextEventTime;

    private String skipUntilTime;

    private String zoneNameWatering;
    private int wateringDuration;
    private long wateringStartedAt;

    private IrrigationScheduleMode scheduleMode;

    private IrrigationControllerState controllerState;

    private boolean isMultiZone;

    private boolean isInOTA;
    private boolean isOnline;

    private boolean hasRequestInFlight;

    public IrrigationControllerDetailsModel() {
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

    public String getNextEventText() {
        return String.format("NEXT EVENT %s", nextEventTime);
    }

    public String getSkipText() {
        return String.format("SKIP UNTIL %s", skipUntilTime);
    }

    public String getZoneNameWatering() {
        return TextUtils.isEmpty(zoneNameWatering) ? "" : zoneNameWatering;
    }

    public String getZoneNameWateringUpperCase() {
        return TextUtils.isEmpty(zoneNameWatering) ? "" : zoneNameWatering.toUpperCase();
    }

    public void setZoneNameWatering(String zoneNameWatering) {
        this.zoneNameWatering = zoneNameWatering;
    }

    public long getWateringStartedAt() {
        return wateringStartedAt;
    }

    public void setWateringStartedAt(long wateringStartedAt) {
        this.wateringStartedAt = wateringStartedAt;
    }

    public boolean isMultiZone() {
        return isMultiZone;
    }

    public void setIsMultiZone(boolean isMultiZone) {
        this.isMultiZone = isMultiZone;
    }

    public int getWateringDuration() {
        return wateringDuration;
    }

    public void setWateringDuration(int wateringDuration) {
        this.wateringDuration = wateringDuration;
    }

    public IrrigationControllerState getControllerState() {
        return controllerState;
    }

    public void setControllerState(IrrigationControllerState controllerState) {
        this.controllerState = controllerState;
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

    public boolean hasNextEvent() {
        return !TextUtils.isEmpty(nextEventZone);
    }

    public String getNextEventZone() {
        return nextEventZone;
    }

    public String getNextEventZoneUpperCase() {
        return TextUtils.isEmpty(nextEventZone) ? "" : nextEventZone.toUpperCase();
    }

    public void setNextEventZone(String nextEventZone) {
        this.nextEventZone = nextEventZone;
    }

    public String getNextEventTime() {
        return nextEventTime;
    }

    public String getNextEventTimeUpperCase() {
        return TextUtils.isEmpty(nextEventTime) ? "" : nextEventTime.toUpperCase();
    }

    public void setNextEventTime(String nextEventTime) {
        this.nextEventTime = nextEventTime;
    }

    public String getSkipUntilTime() {
        return skipUntilTime;
    }

    public String getSkipUntilTimeUpperCase() {
        return skipUntilTime.toUpperCase();
    }

    public void setSkipUntilTime(String skipUntilTime) {
        this.skipUntilTime = skipUntilTime;
    }

    public IrrigationScheduleMode getScheduleMode() {
        if (scheduleMode == null) {
            return IrrigationScheduleMode.MANUAL;
        }

        return scheduleMode;
    }

    public void setScheduleMode(IrrigationScheduleMode scheduleMode) {
        this.scheduleMode = scheduleMode;
    }

    public int getWateringSecondsRemaining() {
        if (wateringStartedAt == 0) {
            return 0;
        }

        long now = System.currentTimeMillis();
        long end = wateringStartedAt + TimeUnit.MINUTES.toMillis(wateringDuration);

        return (int) TimeUnit.MILLISECONDS.toSeconds(end - now);
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        IrrigationControllerDetailsModel that = (IrrigationControllerDetailsModel) o;

        if (wateringDuration != that.wateringDuration) {
            return false;
        }
        if (wateringStartedAt != that.wateringStartedAt) {
            return false;
        }
        if (isMultiZone != that.isMultiZone) {
            return false;
        }
        if (isInOTA != that.isInOTA) {
            return false;
        }
        if (isOnline != that.isOnline) {
            return false;
        }
        if (hasRequestInFlight != that.hasRequestInFlight) {
            return false;
        }
        if (deviceAddress != null ? !deviceAddress.equals(that.deviceAddress) : that.deviceAddress != null) {
            return false;
        }
        if (deviceName != null ? !deviceName.equals(that.deviceName) : that.deviceName != null) {
            return false;
        }
        if (nextEventZone != null ? !nextEventZone.equals(that.nextEventZone) : that.nextEventZone != null) {
            return false;
        }
        if (nextEventTime != null ? !nextEventTime.equals(that.nextEventTime) : that.nextEventTime != null) {
            return false;
        }
        if (skipUntilTime != null ? !skipUntilTime.equals(that.skipUntilTime) : that.skipUntilTime != null) {
            return false;
        }
        if (zoneNameWatering != null ? !zoneNameWatering.equals(that.zoneNameWatering) : that.zoneNameWatering != null) {
            return false;
        }
        if (scheduleMode != that.scheduleMode) {
            return false;
        }
        return controllerState == that.controllerState;

    }

    @Override public int hashCode() {
        int result = deviceAddress != null ? deviceAddress.hashCode() : 0;
        result = 31 * result + (deviceName != null ? deviceName.hashCode() : 0);
        result = 31 * result + (nextEventZone != null ? nextEventZone.hashCode() : 0);
        result = 31 * result + (nextEventTime != null ? nextEventTime.hashCode() : 0);
        result = 31 * result + (skipUntilTime != null ? skipUntilTime.hashCode() : 0);
        result = 31 * result + (zoneNameWatering != null ? zoneNameWatering.hashCode() : 0);
        result = 31 * result + wateringDuration;
        result = 31 * result + (int) (wateringStartedAt ^ (wateringStartedAt >>> 32));
        result = 31 * result + (scheduleMode != null ? scheduleMode.hashCode() : 0);
        result = 31 * result + (controllerState != null ? controllerState.hashCode() : 0);
        result = 31 * result + (isMultiZone ? 1 : 0);
        result = 31 * result + (isInOTA ? 1 : 0);
        result = 31 * result + (isOnline ? 1 : 0);
        result = 31 * result + (hasRequestInFlight ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "IrrigationControllerDetailsModel{" +
              "deviceAddress='" + deviceAddress + '\'' +
              ", deviceName='" + deviceName + '\'' +
              ", nextEventZone='" + nextEventZone + '\'' +
              ", nextEventTime='" + nextEventTime + '\'' +
              ", skipUntilTime='" + skipUntilTime + '\'' +
              ", zoneNameWatering='" + zoneNameWatering + '\'' +
              ", wateringDuration=" + wateringDuration +
              ", wateringStartedAt=" + wateringStartedAt +
              ", scheduleMode=" + scheduleMode +
              ", controllerState=" + controllerState +
              ", isMultiZone=" + isMultiZone +
              ", isInOTA=" + isInOTA +
              ", isOnline=" + isOnline +
              ", hasRequestInFlight=" + hasRequestInFlight +
              '}';
    }


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceAddress);
        dest.writeString(this.deviceName);
        dest.writeString(this.nextEventZone);
        dest.writeString(this.nextEventTime);
        dest.writeString(this.skipUntilTime);
        dest.writeString(this.zoneNameWatering);
        dest.writeInt(this.wateringDuration);
        dest.writeLong(this.wateringStartedAt);
        dest.writeInt(this.scheduleMode == null ? -1 : this.scheduleMode.ordinal());
        dest.writeInt(this.controllerState == null ? -1 : this.controllerState.ordinal());
        dest.writeByte(isMultiZone ? (byte) 1 : (byte) 0);
        dest.writeByte(isInOTA ? (byte) 1 : (byte) 0);
        dest.writeByte(isOnline ? (byte) 1 : (byte) 0);
        dest.writeByte(hasRequestInFlight ? (byte) 1 : (byte) 0);
    }

    protected IrrigationControllerDetailsModel(Parcel in) {
        this.deviceAddress = in.readString();
        this.deviceName = in.readString();
        this.nextEventZone = in.readString();
        this.nextEventTime = in.readString();
        this.skipUntilTime = in.readString();
        this.zoneNameWatering = in.readString();
        this.wateringDuration = in.readInt();
        this.wateringStartedAt = in.readLong();
        int tmpScheduleMode = in.readInt();
        this.scheduleMode = tmpScheduleMode == -1 ? null : IrrigationScheduleMode.values()[tmpScheduleMode];
        int tmpControllerState = in.readInt();
        this.controllerState = tmpControllerState == -1 ? null : IrrigationControllerState.values()[tmpControllerState];
        this.isMultiZone = in.readByte() != 0;
        this.isInOTA = in.readByte() != 0;
        this.isOnline = in.readByte() != 0;
        this.hasRequestInFlight = in.readByte() != 0;
    }

    public static final Parcelable.Creator<IrrigationControllerDetailsModel> CREATOR =
          new Parcelable.Creator<IrrigationControllerDetailsModel>() {
              public IrrigationControllerDetailsModel createFromParcel(Parcel source) {
                  return new IrrigationControllerDetailsModel(source);
              }

              public IrrigationControllerDetailsModel[] newArray(int size) {
                  return new IrrigationControllerDetailsModel[size];
              }
          };
}
