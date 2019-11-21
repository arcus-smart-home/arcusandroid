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
package arcus.cornea.subsystem;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class ScheduleGenericStateModel implements Parcelable, Comparable<ScheduleGenericStateModel> {
    private String deviceId;
    private String name;
    private String stype;
    private boolean checked;
    private boolean schedOn;


    public ScheduleGenericStateModel() {}

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getType() {
        return stype;
    }

    public void setType(String stype) {
        this.stype = stype;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override public int compareTo(@NonNull ScheduleGenericStateModel another) {
        String lhs = String.valueOf(another.getName());
        String rhs = String.valueOf(getName());

        return rhs.compareToIgnoreCase(lhs);
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
                "deviceId='" + deviceId + '\'' +
                ", name='" + name + '\'' +
                ", type=" + stype +
                ", checked=" + checked +
                ", schedOn=" + schedOn +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.deviceId);
        dest.writeString(this.stype);
        dest.writeString(this.name);
        dest.writeInt(checked ? 1:0);
        dest.writeInt(schedOn ? 1:0);
    }

    protected ScheduleGenericStateModel(Parcel in) {
        int tmpDeviceType = in.readInt();
        this.deviceId = in.readString();
        this.stype = in.readString();
        this.name = in.readString();
        int tmpDeviceState = in.readInt();
        this.checked=  in.readInt() ==0 ? false : true;
        this.schedOn=  in.readInt() ==0 ? false : true;

    }

    public static final Creator<ScheduleGenericStateModel> CREATOR = new Creator<ScheduleGenericStateModel>() {
        public ScheduleGenericStateModel createFromParcel(Parcel source) {
            return new ScheduleGenericStateModel(source);
        }

        public ScheduleGenericStateModel[] newArray(int size) {
            return new ScheduleGenericStateModel[size];
        }
    };

}
