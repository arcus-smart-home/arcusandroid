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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlacesWithRoles implements Serializable, Parcelable {
    final PlaceAndRoleModel primaryPlace;
    final List<PlaceAndRoleModel> ownedPlaces;
    final List<PlaceAndRoleModel> unownedPlaces;

    public PlacesWithRoles(
          @NonNull List<PlaceAndRoleModel> ownedPlaces,
          @NonNull List<PlaceAndRoleModel> unownedPlaces,
          @Nullable PlaceAndRoleModel primaryPlace
    ) {
        this.ownedPlaces = new ArrayList<>(ownedPlaces);
        this.unownedPlaces = new ArrayList<>(unownedPlaces);
        this.primaryPlace = primaryPlace;
    }

    public boolean ownsPlaces() {
        return !ownedPlaces.isEmpty();
    }

    public boolean hasGuestAccess() {
        return !unownedPlaces.isEmpty();
    }

    public PlaceAndRoleModel getPrimaryPlace() {
        return primaryPlace;
    }

    public List<PlaceAndRoleModel> getOwnedPlaces() {
        return Collections.unmodifiableList(ownedPlaces);
    }

    public List<PlaceAndRoleModel> getUnownedPlaces() {
        return Collections.unmodifiableList(unownedPlaces);
    }

    public List<PlaceAndRoleModel> getSortedOwnedPlaces() {
        List<PlaceAndRoleModel> sorted = new ArrayList<>(ownedPlaces);
        Collections.sort(sorted);
        return Collections.unmodifiableList(sorted);
    }

    public List<PlaceAndRoleModel> getSortedUnownedPlaces() {
        List<PlaceAndRoleModel> sorted = new ArrayList<>(unownedPlaces);
        Collections.sort(sorted);
        return Collections.unmodifiableList(sorted);
    }

    public boolean hasPrimaryPlace() {
        return primaryPlace != null;
    }

    public int getTotalPlaces() {
        return ownedPlaces.size() + unownedPlaces.size();
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PlacesWithRoles that = (PlacesWithRoles) o;

        if (primaryPlace != null ? !primaryPlace.equals(that.primaryPlace) : that.primaryPlace != null) {
            return false;
        }
        if (!ownedPlaces.equals(that.ownedPlaces)) {
            return false;
        }
        return unownedPlaces.equals(that.unownedPlaces);

    }

    @Override public int hashCode() {
        int result = primaryPlace != null ? primaryPlace.hashCode() : 0;
        result = 31 * result + ownedPlaces.hashCode();
        result = 31 * result + unownedPlaces.hashCode();
        return result;
    }

    @Override public String toString() {
        return "PlacesWithRoles{" +
              "primaryPlace=" + primaryPlace +
              ", ownedPlaces=" + ownedPlaces +
              ", unownedPlaces=" + unownedPlaces +
              '}';
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.primaryPlace, flags);
        dest.writeTypedList(this.ownedPlaces);
        dest.writeTypedList(this.unownedPlaces);
    }

    protected PlacesWithRoles(Parcel in) {
        this.primaryPlace = in.readParcelable(PlaceAndRoleModel.class.getClassLoader());
        this.ownedPlaces = in.createTypedArrayList(PlaceAndRoleModel.CREATOR);
        this.unownedPlaces = in.createTypedArrayList(PlaceAndRoleModel.CREATOR);
    }

    public static final Parcelable.Creator<PlacesWithRoles> CREATOR = new Parcelable.Creator<PlacesWithRoles>() {
        @Override public PlacesWithRoles createFromParcel(Parcel source) {
            return new PlacesWithRoles(source);
        }

        @Override public PlacesWithRoles[] newArray(int size) {
            return new PlacesWithRoles[size];
        }
    };
}
