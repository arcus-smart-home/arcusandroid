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
package arcus.app.common.events;

import android.os.Parcel;
import android.os.Parcelable;

public class FirmwareEvent implements Parcelable {
    private String  modelAddress;
    private boolean isUpdating;
    private boolean isCamera;

    public FirmwareEvent() {
    }

    public FirmwareEvent(boolean isCamera, boolean isUpdating, String modelAddress) {
        this.isCamera = isCamera;
        this.modelAddress = modelAddress;
        this.isUpdating = isUpdating;
    }

    public boolean isCamera() {
        return isCamera;
    }

    public void setIsCamera(boolean isCamera) {
        this.isCamera = isCamera;
    }

    public String getModelAddress() {
        return modelAddress;
    }

    public void setModelAddress(String modelAddress) {
        this.modelAddress = modelAddress;
    }

    public boolean isUpdating() {
        return isUpdating;
    }

    public void setIsUpdating(boolean isUpdating) {
        this.isUpdating = isUpdating;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        FirmwareEvent that = (FirmwareEvent) o;

        if (isUpdating != that.isUpdating) {
            return false;
        }
        if (isCamera != that.isCamera) {
            return false;
        }
        return !(modelAddress != null ? !modelAddress.equals(that.modelAddress) : that.modelAddress != null);

    }

    @Override
    public int hashCode() {
        int result = modelAddress != null ? modelAddress.hashCode() : 0;
        result = 31 * result + (isUpdating ? 1 : 0);
        result = 31 * result + (isCamera ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "FirmwareEvent{" +
              "modelAddress='" + modelAddress + '\'' +
              ", isUpdating=" + isUpdating +
              ", isCamera=" + isCamera +
              '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.modelAddress);
        dest.writeByte(isUpdating ? (byte) 1 : (byte) 0);
        dest.writeByte(isCamera ? (byte) 1 : (byte) 0);
    }

    protected FirmwareEvent(Parcel in) {
        this.modelAddress = in.readString();
        this.isUpdating = in.readByte() != 0;
        this.isCamera = in.readByte() != 0;
    }

    public static final Creator<FirmwareEvent> CREATOR = new Creator<FirmwareEvent>() {
        public FirmwareEvent createFromParcel(Parcel source) {
            return new FirmwareEvent(source);
        }

        public FirmwareEvent[] newArray(int size) {
            return new FirmwareEvent[size];
        }
    };
}
