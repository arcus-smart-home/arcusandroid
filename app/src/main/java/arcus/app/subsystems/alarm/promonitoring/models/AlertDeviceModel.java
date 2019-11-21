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
package arcus.app.subsystems.alarm.promonitoring.models;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.common.collect.ComparisonChain;
import com.iris.client.model.DeviceModel;
import arcus.app.common.utils.CorneaUtils;

import java.util.Comparator;

public class AlertDeviceModel implements Parcelable, Comparable<AlertDeviceModel> {

    public String mainText;
    public String subText;
    public String abstractText;
    public DeviceModel deviceModel;
    public boolean hasChevron = true;

    public boolean isOnline, isInfoItem, isHeader, isPopular;
    public boolean hasSmoke = false;
    public boolean hasCO = false;
    public boolean hasSecurity = false;
    public boolean hasWaterLeak = false;
    public boolean waterShutoffEnabled = false;
    public boolean recordingSupported = false;
    public boolean shutOffFansOnSmoke = false;
    public boolean shutOffFansOnCO = false;
    public int id = -1;

    @SuppressWarnings("ConstantConditions") public AlertDeviceModel(String title) {
        mainText = title;
    }

    public static AlertDeviceModel headerModelType(@NonNull String titleText) {
        AlertDeviceModel m = new AlertDeviceModel(titleText);
        m.isHeader = true;

        return m;
    }

    public static AlertDeviceModel forOnlineDevice(@NonNull DeviceModel deviceModel) {
        AlertDeviceModel m = new AlertDeviceModel(deviceModel.getName());
        m.deviceModel = deviceModel;
        m.subText = CorneaUtils.getProductShortName(deviceModel.getProductId());
        m.isOnline = true;
        m.hasChevron = false;

        return m;
    }

    public static AlertDeviceModel forOfflineDevice(@NonNull  DeviceModel deviceModel) {
        AlertDeviceModel m = new AlertDeviceModel(deviceModel.getName());
        m.deviceModel = deviceModel;
        m.isOnline = false;
        m.hasChevron = false;

        return m;
    }

    public static AlertDeviceModel forSecurityDevice(@NonNull DeviceModel deviceModel, String subText, String abstractText, boolean isOnline) {
        AlertDeviceModel m = new AlertDeviceModel(deviceModel.getName());
        m.deviceModel = deviceModel;
        m.isOnline = isOnline;
        m.hasChevron = true;
        m.subText = subText;
        m.abstractText = abstractText;

        return m;
    }

    public int getViewType() {
        if (isHeader) {
            return 1;
        }

        return isInfoItem ? 2 : 3;
    }

    public void setRecordingSupported(boolean recordingSupported) {
        this.recordingSupported = recordingSupported;
    }

    public void setShutOffFansOnSmoke(boolean shutOffFansOnSmoke) {
        this.shutOffFansOnSmoke = shutOffFansOnSmoke;
    }

    public void setShutOffFansOnCO(boolean shutOffFansOnCO) {
        this.shutOffFansOnCO = shutOffFansOnCO;
    }

    @Override public int compareTo(@NonNull AlertDeviceModel another) {
        return ComparisonChain
                    .start() // Sort by view type fist, then by text content
                    .compare(getViewType(), another.getViewType())
                    .compare(String.valueOf(mainText), String.valueOf(another.mainText))
                    .compare(String.valueOf(subText), String.valueOf(another.subText))
                    .result();
    }

    @Override public String toString() {
        return "WeatherAlertModel{" +
              "mainText='" + mainText + '\'' +
              ", isOnline=" + isOnline +
              ", isBlankItem=" + isInfoItem +
              ", isHeader=" + isHeader +
              ", isPopular=" + isPopular +
              ", recordingSupported=" + recordingSupported +
              '}';
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mainText);
        dest.writeByte(this.isOnline ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isInfoItem ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isHeader ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isPopular ? (byte) 1 : (byte) 0);
        dest.writeByte(this.recordingSupported ? (byte) 1 : (byte) 0);
        dest.writeByte(this.shutOffFansOnSmoke ? (byte) 1 : (byte) 0);
        dest.writeByte(this.shutOffFansOnCO ? (byte) 1 : (byte) 0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AlertDeviceModel)) return false;

        AlertDeviceModel that = (AlertDeviceModel) o;

        if (isOnline != that.isOnline) return false;
        if (isInfoItem != that.isInfoItem) return false;
        if (isHeader != that.isHeader) return false;
        if (isPopular != that.isPopular) return false;
        if (hasSmoke != that.hasSmoke) return false;
        if (hasCO != that.hasCO) return false;
        if (hasSecurity != that.hasSecurity) return false;
        if (hasWaterLeak != that.hasWaterLeak) return false;
        if (waterShutoffEnabled != that.waterShutoffEnabled) return false;
        if (recordingSupported != that.recordingSupported) return false;
        if (shutOffFansOnSmoke != that.shutOffFansOnSmoke) return false;
        if (shutOffFansOnCO != that.shutOffFansOnCO) return false;
        if (id != that.id) return false;
        if (mainText != null ? !mainText.equals(that.mainText) : that.mainText != null)
            return false;
        if (subText != null ? !subText.equals(that.subText) : that.subText != null) return false;
        return abstractText != null ? abstractText.equals(that.abstractText) : that.abstractText == null;

    }

    @Override
    public int hashCode() {
        int result = mainText != null ? mainText.hashCode() : 0;
        result = 31 * result + (subText != null ? subText.hashCode() : 0);
        result = 31 * result + (abstractText != null ? abstractText.hashCode() : 0);
        result = 31 * result + (isOnline ? 1 : 0);
        result = 31 * result + (isInfoItem ? 1 : 0);
        result = 31 * result + (isHeader ? 1 : 0);
        result = 31 * result + (isPopular ? 1 : 0);
        result = 31 * result + (hasSmoke ? 1 : 0);
        result = 31 * result + (hasCO ? 1 : 0);
        result = 31 * result + (hasSecurity ? 1 : 0);
        result = 31 * result + (hasWaterLeak ? 1 : 0);
        result = 31 * result + (waterShutoffEnabled ? 1 : 0);
        result = 31 * result + (recordingSupported ? 1 : 0);
        result = 31 * result + (shutOffFansOnSmoke ? 1 : 0);
        result = 31 * result + (shutOffFansOnCO ? 1 : 0);
        result = 31 * result + id;
        return result;
    }

    protected AlertDeviceModel(Parcel in) {
        this.mainText = in.readString();
        this.isOnline = in.readByte() != 0;
        this.isInfoItem = in.readByte() != 0;
        this.isHeader = in.readByte() != 0;
        this.isPopular = in.readByte() != 0;
        this.recordingSupported = in.readByte() != 0;
        this.shutOffFansOnSmoke = in.readByte() != 0;
        this.shutOffFansOnCO = in.readByte() != 0;
    }

    public static Comparator<AlertDeviceModel> sortAlphaOrder = new Comparator<AlertDeviceModel>() {
        @Override
        public int compare(AlertDeviceModel lhs, AlertDeviceModel rhs) {
            if (lhs.mainText == null) {
                return 1;
            }
            else if (rhs.mainText == null) {
                return -1;
            }
            return lhs.mainText.compareToIgnoreCase(rhs.mainText);
        }
    };

    public static final Creator<AlertDeviceModel> CREATOR = new Creator<AlertDeviceModel>() {
        @Override public AlertDeviceModel createFromParcel(Parcel source) {
            return new AlertDeviceModel(source);
        }

        @Override public AlertDeviceModel[] newArray(int size) {
            return new AlertDeviceModel[size];
        }
    };
}
