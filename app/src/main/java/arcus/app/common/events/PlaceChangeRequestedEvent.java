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

import com.iris.capability.util.Addresses;

public class PlaceChangeRequestedEvent implements Parcelable {
    String placeIDRequested;

    public PlaceChangeRequestedEvent(String placeAddress) {
        placeIDRequested = placeAddress;
    }

    public String getPlaceIDRequested() {
        return Addresses.getId(placeIDRequested);
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PlaceChangeRequestedEvent that = (PlaceChangeRequestedEvent) o;

        return placeIDRequested != null ? placeIDRequested.equals(that.placeIDRequested) : that.placeIDRequested == null;

    }

    @Override public int hashCode() {
        return placeIDRequested != null ? placeIDRequested.hashCode() : 0;
    }

    @Override public String toString() {
        return "PlaceChangeRequestedEvent{" +
              "placeIDRequested='" + placeIDRequested + '\'' +
              '}';
    }


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.placeIDRequested);
    }

    protected PlaceChangeRequestedEvent(Parcel in) {
        this.placeIDRequested = in.readString();
    }

    public static final Parcelable.Creator<PlaceChangeRequestedEvent> CREATOR = new Parcelable.Creator<PlaceChangeRequestedEvent>() {
        @Override public PlaceChangeRequestedEvent createFromParcel(Parcel source) {
            return new PlaceChangeRequestedEvent(source);
        }

        @Override public PlaceChangeRequestedEvent[] newArray(int size) {
            return new PlaceChangeRequestedEvent[size];
        }
    };
}
