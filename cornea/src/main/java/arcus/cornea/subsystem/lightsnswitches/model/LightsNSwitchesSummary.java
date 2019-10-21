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

public class LightsNSwitchesSummary implements Parcelable {
    private int switchesOn;
    private int lightsOn;
    private int dimmersOn;

    private boolean allOff;

    public LightsNSwitchesSummary() {
    }

    public int getSwitchesOn() {
        return switchesOn;
    }

    public void setSwitchesOn(int switchesOn) {
        this.switchesOn = switchesOn;
    }

    public int getLightsOn() {
        return lightsOn;
    }

    public void setLightsOn(int lightsOn) {
        this.lightsOn = lightsOn;
    }

    public int getDimmersOn() {
        return dimmersOn;
    }

    public void setDimmersOn(int dimmersOn) {
        this.dimmersOn = dimmersOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LightsNSwitchesSummary that = (LightsNSwitchesSummary) o;

        if (switchesOn != that.switchesOn) {
            return false;
        }
        if (lightsOn != that.lightsOn) {
            return false;
        }
        if (dimmersOn != that.dimmersOn) {
            return false;
        }
        return allOff == that.allOff;

    }

    @Override
    public int hashCode() {
        int result = switchesOn;
        result = 31 * result + lightsOn;
        result = 31 * result + dimmersOn;
        result = 31 * result + (allOff ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "LightsNSwitchesSummary{" +
              "switchesOn=" + switchesOn +
              ", lightsOn=" + lightsOn +
              ", dimmersOn=" + dimmersOn +
              ", allOff=" + allOff +
              '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.switchesOn);
        dest.writeInt(this.lightsOn);
        dest.writeInt(this.dimmersOn);
        dest.writeByte(allOff ? (byte) 1 : (byte) 0);
    }

    protected LightsNSwitchesSummary(Parcel in) {
        this.switchesOn = in.readInt();
        this.lightsOn = in.readInt();
        this.dimmersOn = in.readInt();
        this.allOff = in.readByte() != 0;
    }

    public static final Parcelable.Creator<LightsNSwitchesSummary> CREATOR = new Parcelable.Creator<LightsNSwitchesSummary>() {
        public LightsNSwitchesSummary createFromParcel(Parcel source) {
            return new LightsNSwitchesSummary(source);
        }

        public LightsNSwitchesSummary[] newArray(int size) {
            return new LightsNSwitchesSummary[size];
        }
    };
}
