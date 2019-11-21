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
package arcus.cornea.device.smokeandco;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.common.collect.ComparisonChain;

public class WeatherAlertModel implements Parcelable, Comparable<WeatherAlertModel> {
    public String mainText;
    public final String itemCode;
    public boolean isChecked, isInfoItem, isHeader, isPopular;

    @SuppressWarnings("ConstantConditions") public WeatherAlertModel(@NonNull String itemCode) {
        this.itemCode = itemCode == null ? "" + System.nanoTime() : itemCode;
    }

    public static WeatherAlertModel headerModelType(@NonNull String titleText) {
        WeatherAlertModel m = new WeatherAlertModel(titleText);
        m.mainText = titleText;
        m.isHeader = true;

        return m;
    }

    public static WeatherAlertModel infoModelType(@NonNull String titleText) {
        WeatherAlertModel m = new WeatherAlertModel(titleText);
        m.mainText = titleText;
        m.isInfoItem = true;

        return m;
    }

    public int getViewType() {
        if (isHeader) {
            return 1;
        }

        return isInfoItem ? 2 : 3;
    }

    @Override public int compareTo(@NonNull WeatherAlertModel another) {
        return ComparisonChain
                    .start() // Sort by view type fist, then by text content
                    .compare(getViewType(), another.getViewType())
                    .compare(String.valueOf(mainText.toUpperCase()), String.valueOf(another.mainText.toUpperCase()))
                    .result();
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WeatherAlertModel that = (WeatherAlertModel) o;

        return itemCode.equals(that.itemCode);

    }

    @Override public int hashCode() {
        return itemCode.hashCode();
    }

    @Override public String toString() {
        return "WeatherAlertModel{" +
              "mainText='" + mainText + '\'' +
              ", itemCode='" + itemCode + '\'' +
              ", isChecked=" + isChecked +
              ", isBlankItem=" + isInfoItem +
              ", isHeader=" + isHeader +
              ", isPopular=" + isPopular +
              '}';
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mainText);
        dest.writeString(this.itemCode);
        dest.writeByte(this.isChecked ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isInfoItem ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isHeader ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isPopular ? (byte) 1 : (byte) 0);
    }

    protected WeatherAlertModel(Parcel in) {
        this.mainText = in.readString();
        this.itemCode = in.readString();
        this.isChecked = in.readByte() != 0;
        this.isInfoItem = in.readByte() != 0;
        this.isHeader = in.readByte() != 0;
        this.isPopular = in.readByte() != 0;
    }

    public static final Creator<WeatherAlertModel> CREATOR = new Creator<WeatherAlertModel>() {
        @Override public WeatherAlertModel createFromParcel(Parcel source) {
            return new WeatherAlertModel(source);
        }

        @Override public WeatherAlertModel[] newArray(int size) {
            return new WeatherAlertModel[size];
        }
    };
}
