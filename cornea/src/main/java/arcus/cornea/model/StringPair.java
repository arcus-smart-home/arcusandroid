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

import java.io.Serializable;

public class StringPair implements Parcelable, Serializable {
    private String key;
    private String value;

    public StringPair(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        StringPair that = (StringPair) o;

        if (key != null ? !key.equals(that.key) : that.key != null) {
            return false;
        }
        return !(value != null ? !value.equals(that.value) : that.value != null);

    }

    @Override
    public int hashCode() {
        int result = key != null ? key.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "StringPair{" +
              "key='" + key + '\'' +
              ", value='" + value + '\'' +
              '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.key);
        dest.writeString(this.value);
    }

    protected StringPair(Parcel in) {
        this.key = in.readString();
        this.value = in.readString();
    }

    public static final Creator<StringPair> CREATOR = new Creator<StringPair>() {
        public StringPair createFromParcel(Parcel source) {
            return new StringPair(source);
        }

        public StringPair[] newArray(int size) {
            return new StringPair[size];
        }
    };
}
