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
package arcus.cornea.subsystem.lightsnswitches.model;

import android.os.Parcel;
import android.os.Parcelable;

import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

public class LightsNSwitchesScheduleDay implements Parcelable {
    private String commandID;
    private TimeOfDay timeOfDay;
    private HashSet<DayOfWeek> repeatsOn;
    private String repetitionText;
    private boolean on;
    private int dimPercentage;
    private int colorTemp;

    private boolean colorTempChangeable;
    private boolean switchable;
    private boolean dimmable;

    public LightsNSwitchesScheduleDay() {}

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

    public boolean isRepeating() {
        return getRepeatsOn() != null && getRepeatsOn().size() > 1;
    }

    public void setRepeatsOn(HashSet<DayOfWeek> repeatsOn) {
        this.repeatsOn = repeatsOn;
    }

    public String getRepetitionText() {
        return repetitionText;
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

    public int getDimPercentage() {
        return dimPercentage;
    }

    public void setDimPercentage(int dimPercentage) {
        this.dimPercentage = dimPercentage;
    }

    public int getColorTemp() {
        return colorTemp;
    }

    public void setColorTemp(int colorTemp) {
        this.colorTemp = colorTemp;
    }

    public boolean isColorTempChangeable() {
        return colorTempChangeable;
    }

    public void setColorTempChangeable(boolean colorTempChangeable) {
        this.colorTempChangeable = colorTempChangeable;
    }

    public boolean isSwitchable() {
        return switchable;
    }

    public void setSwitchable(boolean switchable) {
        this.switchable = switchable;
    }

    public boolean isDimmable() {
        return dimmable;
    }

    public void setDimmable(boolean dimmable) {
        this.dimmable = dimmable;
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
    public String toString() {
        return "LightsNSwitchesScheduleDay{" +
              "commandID='" + commandID + '\'' +
              ", timeOfDay=" + timeOfDay +
              ", repeatsOn=" + repeatsOn +
              ", repetitionText='" + repetitionText + '\'' +
              ", on=" + on +
              ", dimPercentage=" + dimPercentage +
              ", colorTemp=" + colorTemp +
              ", colorTempChangeable=" + colorTempChangeable +
              ", switchable=" + switchable +
              ", dimmable=" + dimmable +
              '}';
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
        dest.writeInt(this.dimPercentage);
        dest.writeInt(this.colorTemp);
        dest.writeByte(colorTempChangeable ? (byte) 1 : (byte) 0);
        dest.writeByte(switchable ? (byte) 1 : (byte) 0);
        dest.writeByte(dimmable ? (byte) 1 : (byte) 0);
    }

    protected LightsNSwitchesScheduleDay(Parcel in) {
        this.commandID = in.readString();
        this.timeOfDay = (TimeOfDay) in.readSerializable();
        this.repeatsOn = (HashSet<DayOfWeek>) in.readSerializable();
        this.repetitionText = in.readString();
        this.on = in.readByte() != 0;
        this.dimPercentage = in.readInt();
        this.colorTemp = in.readInt();
        this.colorTempChangeable = in.readByte() != 0;
        this.switchable = in.readByte() != 0;
        this.dimmable = in.readByte() != 0;
    }

    public static final Creator<LightsNSwitchesScheduleDay> CREATOR = new Creator<LightsNSwitchesScheduleDay>() {
        public LightsNSwitchesScheduleDay createFromParcel(Parcel source) {
            return new LightsNSwitchesScheduleDay(source);
        }

        public LightsNSwitchesScheduleDay[] newArray(int size) {
            return new LightsNSwitchesScheduleDay[size];
        }
    };
}
