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
package arcus.cornea.subsystem.climate.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;


public class ScheduleStateModel implements Parcelable {
    private String deviceId;
    private String name;
    private ScheduleState state;
    private DeviceControlType type;
    private boolean bAllowScheduleSelection = true;

    private boolean checked = false;
    private boolean schedOn = false;

    public ScheduleStateModel() {}

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public DeviceControlType getType() {
        return type;
    }

    public ScheduleState getState() {
        return state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(DeviceControlType type) {
        this.type = type;
    }

    public void setState(ScheduleState state) {
        this.state = state;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public boolean isSchedOn() {
        return schedOn;
    }

    public void setSchedOn(boolean schedOn) {
        this.schedOn = schedOn;
    }

    public boolean isAllowScheduleSelection() {
        return bAllowScheduleSelection;
    }

    public void setAllowScheduleSelection(boolean bAllowScheduleSelection) {
        this.bAllowScheduleSelection = bAllowScheduleSelection;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public String toString() {
        return "ScheduleStateModel{" +
                "deviceType=" + type.name() +
                ", deviceId='" + deviceId + '\'' +
                ", deviceName='" + name + '\'' +
                ", checked=" + checked +
                ", schedOn=" + schedOn +
                ", allowScheduleSelection=" + bAllowScheduleSelection +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.type == null ? -1 : this.type.ordinal());
        dest.writeString(this.deviceId);
        dest.writeString(this.name);
        dest.writeInt(this.state == null ? -1 : this.state.ordinal());
        dest.writeInt(checked ? 1 : 0);
        dest.writeInt(schedOn ? 1:0);
        dest.writeInt(bAllowScheduleSelection ? 1:0);
    }

    protected ScheduleStateModel(Parcel in) {
        int tmpDeviceType = in.readInt();
        this.type = tmpDeviceType == -1 ? null : DeviceControlType.values()[tmpDeviceType];
        this.deviceId = in.readString();
        this.name = in.readString();
        int tmpDeviceState = in.readInt();
        this.state = tmpDeviceState == -1 ? null : ScheduleState.values()[tmpDeviceState];
        this.checked=  in.readInt() ==0 ? false : true;
        this.schedOn=  in.readInt() ==0 ? false : true;
        this.bAllowScheduleSelection = in.readInt() == 0 ? false : true;
    }

    public static final Creator<ScheduleStateModel> CREATOR = new Creator<ScheduleStateModel>() {
        public ScheduleStateModel createFromParcel(Parcel source) {
            return new ScheduleStateModel(source);
        }

        public ScheduleStateModel[] newArray(int size) {
            return new ScheduleStateModel[size];
        }
    };

}
