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
package arcus.app.subsystems.water.schedule;

import android.os.Parcel;
import android.os.Parcelable;

import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

public class WaterScheduleDay implements Parcelable {
    private String commandID;
    private TimeOfDay timeOfDay;
    private HashSet<DayOfWeek> repeatsOn;
    private String repetitionText;
    private boolean on;
    private double setPoint;


    public WaterScheduleDay() {}

    public String getCommandID() {
        return commandID;
    }

    public void setCommandID(String commandID) {
        this.commandID = commandID;
    }

    public TimeOfDay getTimeOfDay() {
        return timeOfDay;
    }

    public void setTimeOfDay(TimeOfDay timeOfDay) {
        this.timeOfDay = timeOfDay;
    }

    public Set<DayOfWeek> getRepeatsOn() {
        return repeatsOn;
    }


    public void setRepeatsOn(HashSet<DayOfWeek> repeatsOn) {
        this.repeatsOn = repeatsOn;
    }


    public void setRepetitionText(String repetitionText) {
        this.repetitionText = repetitionText;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }



    public double getSetPoint() {
        return setPoint;
    }

    public void setSetPoint(double setPoint) {
        this.setPoint = setPoint;
    }





    @Override
    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }



    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.commandID);
        dest.writeSerializable(this.timeOfDay);
        dest.writeSerializable(this.repeatsOn);
        dest.writeString(this.repetitionText);
        dest.writeByte(on ? (byte) 1 : (byte) 0);
        dest.writeDouble(this.setPoint);
    }

    protected WaterScheduleDay(Parcel in) {
        this.commandID = in.readString();
        this.timeOfDay = (TimeOfDay) in.readSerializable();
        this.repeatsOn = (HashSet<DayOfWeek>) in.readSerializable();
        this.repetitionText = in.readString();
        this.on = in.readByte() != 0;
        this.setPoint = in.readInt();
    }

    public static final Creator<WaterScheduleDay> CREATOR = new Creator<WaterScheduleDay>() {
        public WaterScheduleDay createFromParcel(Parcel source) {
            return new WaterScheduleDay(source);
        }

        public WaterScheduleDay[] newArray(int size) {
            return new WaterScheduleDay[size];
        }
    };
}
