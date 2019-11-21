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
package arcus.cornea.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.iris.capability.util.Addresses;
import com.iris.client.capability.Place;
import com.iris.client.session.SessionInfo;

import java.io.Serializable;
import java.util.Map;

public class PlaceAndRoleModel implements Serializable, Parcelable, Comparable<PlaceAndRoleModel> {
    private final String placeId;
    private final String state;
    private final String zipCode;
    private final String role;
    private final String city;
    private final String name;
    private final String streetAddress1;
    private final String streetAddress2;
    private final boolean primary;

    public PlaceAndRoleModel(@NonNull Map<String, Object> place) {
        this.placeId = (String) place.get("placeId");
        this.state = (String) place.get("state");
        this.zipCode = (String) place.get("zipCode");
        this.role = (String) place.get("role");
        this.city = (String) place.get("city");
        this.name = (String) place.get("name");
        this.streetAddress1 = (String) place.get("streetAddress1");
        this.streetAddress2 = (String) place.get("streetAddress2");
        this.primary = Boolean.TRUE.equals(place.get("primary"));
    }

    public @NonNull String getAddress() {
        return Addresses.toObjectAddress(Place.NAMESPACE, placeId);
    }

    public @Nullable String getPlaceId() {
        return placeId;
    }

    public @Nullable String getState() {
        return state;
    }

    public @Nullable String getZipCode() {
        return zipCode;
    }

    public @Nullable String getRole() {
        return role;
    }

    public @Nullable String getCity() {
        return city;
    }

    public @Nullable String getName() {
        return name;
    }

    public @Nullable String getStreetAddress1() {
        return streetAddress1;
    }

    public @Nullable String getStreetAddress2() {
        return streetAddress2;
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isOwner() {
        return SessionInfo.PlaceDescriptor.ROLE_OWNER.equals(role);
    }

    @Override public int compareTo(@NonNull PlaceAndRoleModel another) {
        return String.valueOf(name).compareToIgnoreCase(String.valueOf(another.getName()));
    }

    public String getCityStateZip() {
        String cityText = TextUtils.isEmpty(city) ? "" : city;
        String stateText = TextUtils.isEmpty(state) ? "" : ", " + state;
        String zipText = TextUtils.isEmpty(zipCode) ? "" : " " + zipCode;

        return String.format("%s%s%s", cityText, stateText, zipText);
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PlaceAndRoleModel that = (PlaceAndRoleModel) o;

        if (primary != that.primary) {
            return false;
        }
        if (placeId != null ? !placeId.equals(that.placeId) : that.placeId != null) {
            return false;
        }
        if (state != null ? !state.equals(that.state) : that.state != null) {
            return false;
        }
        if (zipCode != null ? !zipCode.equals(that.zipCode) : that.zipCode != null) {
            return false;
        }
        if (role != null ? !role.equals(that.role) : that.role != null) {
            return false;
        }
        if (city != null ? !city.equals(that.city) : that.city != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (streetAddress1 != null ? !streetAddress1.equals(that.streetAddress1) : that.streetAddress1 != null) {
            return false;
        }
        return streetAddress2 != null ? streetAddress2.equals(that.streetAddress2) : that.streetAddress2 == null;

    }

    @Override public int hashCode() {
        int result = placeId != null ? placeId.hashCode() : 0;
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (zipCode != null ? zipCode.hashCode() : 0);
        result = 31 * result + (role != null ? role.hashCode() : 0);
        result = 31 * result + (city != null ? city.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (streetAddress1 != null ? streetAddress1.hashCode() : 0);
        result = 31 * result + (streetAddress2 != null ? streetAddress2.hashCode() : 0);
        result = 31 * result + (primary ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "PlaceAndRoleModel{" +
              "placeId='" + placeId + '\'' +
              ", state='" + state + '\'' +
              ", zipCode='" + zipCode + '\'' +
              ", role='" + role + '\'' +
              ", city='" + city + '\'' +
              ", name='" + name + '\'' +
              ", streetAddress1='" + streetAddress1 + '\'' +
              ", streetAddress2='" + streetAddress2 + '\'' +
              ", primary=" + primary +
              '}';
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.placeId);
        dest.writeString(this.state);
        dest.writeString(this.zipCode);
        dest.writeString(this.role);
        dest.writeString(this.city);
        dest.writeString(this.name);
        dest.writeString(this.streetAddress1);
        dest.writeString(this.streetAddress2);
        dest.writeByte(primary ? (byte) 1 : (byte) 0);
    }

    protected PlaceAndRoleModel(Parcel in) {
        this.placeId = in.readString();
        this.state = in.readString();
        this.zipCode = in.readString();
        this.role = in.readString();
        this.city = in.readString();
        this.name = in.readString();
        this.streetAddress1 = in.readString();
        this.streetAddress2 = in.readString();
        this.primary = in.readByte() != 0;
    }

    public static final Parcelable.Creator<PlaceAndRoleModel> CREATOR = new Parcelable.Creator<PlaceAndRoleModel>() {
        @Override public PlaceAndRoleModel createFromParcel(Parcel source) {
            return new PlaceAndRoleModel(source);
        }

        @Override public PlaceAndRoleModel[] newArray(int size) {
            return new PlaceAndRoleModel[size];
        }
    };
}
