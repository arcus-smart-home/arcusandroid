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
package arcus.cornea.subsystem.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.Map;

public class TimeZoneModel implements Parcelable, Serializable {
    public static final String NAME = "TimeZone";
    public static final String ATTR_ID = "id";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_OFFSET = "offset";
    public static final String ATTR_USESDST = "usesDST";

    private String id;
    private String name;
    private Number offset;
    private boolean usesDST;

    public TimeZoneModel(Map<String, Object> attributes) {
        this.id = String.valueOf(attributes.get(ATTR_ID));
        this.name = String.valueOf(attributes.get(ATTR_NAME));
        this.offset = (Number) attributes.get(ATTR_OFFSET);
        this.usesDST = Boolean.TRUE.equals(attributes.get(ATTR_USESDST));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getOffset() {
        return offset == null ? 0 : offset.doubleValue();
    }

    public boolean isUsesDST() {
        return usesDST;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TimeZoneModel that = (TimeZoneModel) o;

        if (usesDST != that.usesDST) {
            return false;
        }
        if (id != null ? !id.equals(that.id) : that.id != null) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        return offset != null ? offset.equals(that.offset) : that.offset == null;

    }

    @Override public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (offset != null ? offset.hashCode() : 0);
        result = 31 * result + (usesDST ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "TimeZoneModel{" +
              "id='" + id + '\'' +
              ", name='" + name + '\'' +
              ", offset=" + offset +
              ", usesDST=" + usesDST +
              '}';
    }


    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeSerializable(this.offset);
        dest.writeByte(usesDST ? (byte) 1 : (byte) 0);
    }

    protected TimeZoneModel(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.offset = (Number) in.readSerializable();
        this.usesDST = in.readByte() != 0;
    }

    public static final Parcelable.Creator<TimeZoneModel> CREATOR =
          new Parcelable.Creator<TimeZoneModel>() {
              @Override
              public TimeZoneModel createFromParcel(Parcel source) {
                  return new TimeZoneModel(source);
              }

              @Override
              public TimeZoneModel[] newArray(int size) {
                  return new TimeZoneModel[size];
              }
          };
}
