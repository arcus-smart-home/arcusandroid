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

import android.os.Parcel;
import android.os.Parcelable;

public class Settings implements Parcelable {
    private boolean silentAlarm;

    public Settings() {
    }

    public Settings(boolean silentAlarm) {
        this.silentAlarm = silentAlarm;
    }

    public boolean isSilentAlarm() {
        return silentAlarm;
    }

    public void setSilentAlarm(boolean silentAlarm) {
        this.silentAlarm = silentAlarm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Settings settings = (Settings) o;

        return silentAlarm == settings.silentAlarm;

    }

    @Override
    public int hashCode() {
        return (silentAlarm ? 1 : 0);
    }

    @Override
    public String toString() {
        return "Settings{" +
              "silentAlarm=" + silentAlarm +
              '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(silentAlarm ? (byte) 1 : (byte) 0);
    }

    protected Settings(Parcel in) {
        this.silentAlarm = in.readByte() != 0;
    }

    public static final Parcelable.Creator<Settings> CREATOR = new Parcelable.Creator<Settings>() {
        public Settings createFromParcel(Parcel source) {
            return new Settings(source);
        }

        public Settings[] newArray(int size) {
            return new Settings[size];
        }
    };
}
