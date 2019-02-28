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
package arcus.cornea.subsystem.alarm.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.iris.client.capability.DeviceConnection;
import com.iris.client.model.DeviceModel;

import java.util.Comparator;

public class AlertDeviceStateModel implements Parcelable {

    public String name;
    public boolean isOnline = false;

    @SuppressWarnings("ConstantConditions") public AlertDeviceStateModel(DeviceModel model) {
        this.name = model.getName();
        this.isOnline = DeviceConnection.STATE_ONLINE.equals(model.get(DeviceConnection.ATTR_STATE));
    }

    @Override
    public String toString() {
        return "AlertDeviceStateModel{" +
              "name='" + name + '\'' +
              ", isOnline=" + isOnline +
              '}';
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.name);
        dest.writeByte(this.isOnline ? (byte) 1 : (byte) 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlertDeviceStateModel)) return false;

        AlertDeviceStateModel that = (AlertDeviceStateModel) o;

        if (isOnline != that.isOnline) return false;

        return name != null ? name.equals(that.name) : that.name == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (isOnline ? 1 : 0);
        return result;
    }

    protected AlertDeviceStateModel(Parcel in) {
        this.name = in.readString();
        this.isOnline = in.readByte() != 0;
    }

    public static Comparator<AlertDeviceStateModel> sortAlphaOrder = new Comparator<AlertDeviceStateModel>() {
        @Override
        public int compare(AlertDeviceStateModel lhs, AlertDeviceStateModel rhs) {
            if (lhs.name == null) {
                return 1;
            }
            else if (rhs.name == null) {
                return -1;
            }
            return lhs.name.compareToIgnoreCase(rhs.name);
        }
    };

    public static final Creator<AlertDeviceStateModel> CREATOR = new Creator<AlertDeviceStateModel>() {
        @Override public AlertDeviceStateModel createFromParcel(Parcel source) {
            return new AlertDeviceStateModel(source);
        }

        @Override public AlertDeviceStateModel[] newArray(int size) {
            return new AlertDeviceStateModel[size];
        }
    };
}
