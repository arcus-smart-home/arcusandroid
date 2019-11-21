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
package arcus.cornea.subsystem.lawnandgarden.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class LawnAndGardenControllerZoneDetailModel
      implements Comparable<LawnAndGardenControllerZoneDetailModel>, Parcelable {
    private String internalZoneName;
    private String zoneName;
    private String deviceAddress;
    private String productModelID;
    private int zoneNumber;
    private int defaultWateringTime;

    public LawnAndGardenControllerZoneDetailModel() {
    }

    public String getInternalZoneName() {
        return internalZoneName;
    }

    public void setInternalZoneName(String internalZoneName) {
        this.internalZoneName = internalZoneName;
    }

    public @Nullable String getZoneName() {
        return zoneName;
    }

    public void setZoneName(String zoneName) {
        this.zoneName = zoneName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public int getZoneNumber() {
        return zoneNumber;
    }

    public void setZoneNumber(int zoneNumber) {
        this.zoneNumber = zoneNumber;
    }

    public int getDefaultWateringTime() {
        return defaultWateringTime;
    }

    public void setDefaultWateringTime(int defaultWateringTime) {
        this.defaultWateringTime = defaultWateringTime;
    }

    public String getProductModelID() {
        return productModelID;
    }

    public void setProductModelID(String productModelID) {
        this.productModelID = productModelID;
    }

    @Override public int compareTo(@NonNull LawnAndGardenControllerZoneDetailModel another) {
        return Integer.valueOf(another.zoneNumber).compareTo(this.zoneNumber);
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        LawnAndGardenControllerZoneDetailModel that = (LawnAndGardenControllerZoneDetailModel) o;

        if (zoneNumber != that.zoneNumber) {
            return false;
        }
        if (defaultWateringTime != that.defaultWateringTime) {
            return false;
        }
        if (internalZoneName != null ? !internalZoneName.equals(that.internalZoneName) : that.internalZoneName != null) {
            return false;
        }
        if (zoneName != null ? !zoneName.equals(that.zoneName) : that.zoneName != null) {
            return false;
        }
        if (deviceAddress != null ? !deviceAddress.equals(that.deviceAddress) : that.deviceAddress != null) {
            return false;
        }
        return !(productModelID != null ? !productModelID.equals(that.productModelID) : that.productModelID != null);

    }

    @Override public int hashCode() {
        int result = internalZoneName != null ? internalZoneName.hashCode() : 0;
        result = 31 * result + (zoneName != null ? zoneName.hashCode() : 0);
        result = 31 * result + (deviceAddress != null ? deviceAddress.hashCode() : 0);
        result = 31 * result + (productModelID != null ? productModelID.hashCode() : 0);
        result = 31 * result + zoneNumber;
        result = 31 * result + defaultWateringTime;
        return result;
    }

    @Override public String toString() {
        return "LawnAndGardenControllerZoneDetailModel{" +
              "internalZoneName='" + internalZoneName + '\'' +
              ", zoneName='" + zoneName + '\'' +
              ", deviceAddress='" + deviceAddress + '\'' +
              ", productModelID='" + productModelID + '\'' +
              ", zoneNumber=" + zoneNumber +
              ", defaultWateringTime=" + defaultWateringTime +
              '}';
    }


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.internalZoneName);
        dest.writeString(this.zoneName);
        dest.writeString(this.deviceAddress);
        dest.writeString(this.productModelID);
        dest.writeInt(this.zoneNumber);
        dest.writeInt(this.defaultWateringTime);
    }

    protected LawnAndGardenControllerZoneDetailModel(Parcel in) {
        this.internalZoneName = in.readString();
        this.zoneName = in.readString();
        this.deviceAddress = in.readString();
        this.productModelID = in.readString();
        this.zoneNumber = in.readInt();
        this.defaultWateringTime = in.readInt();
    }

    public static final Parcelable.Creator<LawnAndGardenControllerZoneDetailModel> CREATOR =
          new Parcelable.Creator<LawnAndGardenControllerZoneDetailModel>() {
              public LawnAndGardenControllerZoneDetailModel createFromParcel(Parcel source) {
                  return new LawnAndGardenControllerZoneDetailModel(source);
              }

              public LawnAndGardenControllerZoneDetailModel[] newArray(int size) {
                  return new LawnAndGardenControllerZoneDetailModel[size];
              }
          };
}
