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
package arcus.cornea.device.hub;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import arcus.cornea.subsystem.connection.model.CellBackupModel;
import com.iris.client.capability.Hub;
import com.iris.client.capability.HubNetwork;
import com.iris.client.capability.HubPower;

public class HubProxyModel implements Parcelable {
    boolean isOnline;
    long onlineDays = 0, onlineHours = 0, onlineMinutes = 0, lastChanged = -1;
    double batteryLevel = -1;
    CellBackupModel cellBackupModel;
    String id, imei, simID, esn, onlineTime, connectionType, batteryType, batteryLevelString, wifiNetwork, wifiConnectedState;
    private String hubModelNumber;
    private boolean hasWiFiCredentials;
    private int wifiSignal;

    public HubProxyModel(@NonNull String deviceID) {
        this.id = deviceID;
    }

    public @NonNull String getId() {
        return TextUtils.isEmpty(id) ? "" : id;
    }

    public @NonNull String getAddress() {
        return String.format("SERV:%s:%s", id, Hub.NAMESPACE);
    }

    public @Nullable String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public @Nullable String getSimID() {
        return simID;
    }

    public void setSimID(String simID) {
        this.simID = simID;
    }

    public String getEsn() {
        return esn;
    }

    public void setEsn(String esn) {
        this.esn = esn;
    }

    public String getWifiNetwork() { return wifiNetwork; }

    public void setWifiNetwork(String wifiNetwork) { this.wifiNetwork = wifiNetwork; }

    public String getWifiConnectedState() {
        return wifiConnectedState;
    }

    public void setWifiConnectedState(String wifiConnectedState) {
        this.wifiConnectedState = wifiConnectedState;
    }

    public boolean isBroadbandConnection() { // Default to "broadband"
        return connectionType == null || HubNetwork.TYPE_ETH.equals(connectionType);
    }

    public boolean isWifiConnection() {
        return HubNetwork.TYPE_WIFI.equals(connectionType);
    }

    public boolean isCellConnection() {
        return HubNetwork.TYPE_3G.equals(connectionType);
    }

    public boolean isBatteryConnection() {
        return HubPower.SOURCE_BATTERY.equals(batteryType);
    }

    public boolean isACConnection() {
        return batteryType == null || HubPower.SOURCE_MAINS.equals(batteryType);
    }

    public boolean isOnline() {
        return isOnline;
    }

    public @Nullable String getOnlineTime() {
        return onlineTime;
    }

    public void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public void setOnlineDays(long onlineDays) {
        if (onlineDays >= 0) {
            this.onlineDays = onlineDays;
        }
    }

    public void setOnlineHours(long onlineHours) {
        if (onlineHours >= 0) {
            this.onlineHours = onlineHours;
        }
    }

    public void setOnlineMinutes(long onlineMinutes) {
        if (onlineMinutes >= 0) {
            this.onlineMinutes = onlineMinutes;
        }
    }

    public void setBatteryType(String batteryType) {
        this.batteryType = batteryType;
    }

    public String getBatteryLevelString() {
        return batteryLevelString;
    }

    public void setBatteryLevelString(String batteryLevelString) {
        this.batteryLevelString = batteryLevelString;
    }

    public double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public long getOnlineDays() {
        return onlineDays;
    }

    public long getOnlineHours() {
        return onlineHours;
    }

    public long getOnlineMinutes() {
        return onlineMinutes;
    }

    public long getLastChangedOrNow() {
        return lastChanged == -1 ? System.currentTimeMillis() : lastChanged;
    }

    public void setLastChanged(long lastChanged) {
        this.lastChanged = lastChanged;
    }

    public @NonNull CellBackupModel getCellBackupModel() {
        return (cellBackupModel == null) ? CellBackupModel.empty() : cellBackupModel;
    }

    public void setCellBackupModel(CellBackupModel cellBackupModel) {
        this.cellBackupModel = cellBackupModel;
    }

    public boolean isV3Hub() {
        return "IH300".equalsIgnoreCase(hubModelNumber);
    }

    public void setHubModelNumber(String hubModelNumber) {
        this.hubModelNumber = hubModelNumber;
    }

    public int getWifiSignal() {
        return wifiSignal;
    }

    public void setWifiSignal(int wifiSignal) {
        this.wifiSignal = wifiSignal;
    }

    public boolean hasWiFiCredentials() {
        return hasWiFiCredentials;
    }

    public void setHasWiFiCredentials(boolean hasWiFiCredentials) {
        this.hasWiFiCredentials = hasWiFiCredentials;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HubProxyModel that = (HubProxyModel) o;

        return id != null ? id.equals(that.id) : that.id == null;

    }

    @Override public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override public String toString() {
        return "HubProxyModel{" +
              "isOnline=" + isOnline +
              ", onlineDays=" + onlineDays +
              ", onlineHours=" + onlineHours +
              ", onlineMinutes=" + onlineMinutes +
              ", lastChanged=" + lastChanged +
              ", batteryLevel=" + batteryLevel +
              ", cellBackupModel=" + cellBackupModel +
              ", id='" + id + '\'' +
              ", imei='" + imei + '\'' +
              ", simID='" + simID + '\'' +
              ", esn='" + esn + '\'' +
              ", onlineTime='" + onlineTime + '\'' +
              ", connectionType='" + connectionType + '\'' +
              ", batteryType='" + batteryType + '\'' +
              ", batteryLevelString='" + batteryLevelString + '\'' +
              ", wifiNetwork='" + wifiNetwork + '\'' +
              ", wifiConnectedState=" + wifiConnectedState + '\'' +
              ", hubModelNumber=" + hubModelNumber + '\'' +
              ", hasWiFiCredentials=" + hasWiFiCredentials +
              ", wifiSignal=" + wifiSignal +
              '}';
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(this.isOnline ? (byte) 1 : (byte) 0);
        dest.writeLong(this.onlineDays);
        dest.writeLong(this.onlineHours);
        dest.writeLong(this.onlineMinutes);
        dest.writeLong(this.lastChanged);
        dest.writeDouble(this.batteryLevel);
        dest.writeParcelable(this.cellBackupModel, flags);
        dest.writeString(this.id);
        dest.writeString(this.imei);
        dest.writeString(this.simID);
        dest.writeString(this.esn);
        dest.writeString(this.onlineTime);
        dest.writeString(this.connectionType);
        dest.writeString(this.batteryType);
        dest.writeString(this.batteryLevelString);
        dest.writeString(this.wifiNetwork);
        dest.writeString(this.wifiConnectedState);
        dest.writeString(this.hubModelNumber);
        dest.writeByte(this.hasWiFiCredentials ? (byte) 1 : (byte) 0);
        dest.writeInt(this.wifiSignal);
    }

    protected HubProxyModel(Parcel in) {
        this.isOnline = in.readByte() != 0;
        this.onlineDays = in.readLong();
        this.onlineHours = in.readLong();
        this.onlineMinutes = in.readLong();
        this.lastChanged = in.readLong();
        this.batteryLevel = in.readDouble();
        this.cellBackupModel = in.readParcelable(CellBackupModel.class.getClassLoader());
        this.id = in.readString();
        this.imei = in.readString();
        this.simID = in.readString();
        this.esn = in.readString();
        this.onlineTime = in.readString();
        this.connectionType = in.readString();
        this.batteryType = in.readString();
        this.batteryLevelString = in.readString();
        this.wifiNetwork = in.readString();
        this.wifiConnectedState = in.readString();
        this.hubModelNumber = in.readString();
        this.hasWiFiCredentials = in.readByte() != 0;
        this.wifiSignal = in.readInt();
    }

    public static final Creator<HubProxyModel> CREATOR = new Creator<HubProxyModel>() {
        @Override public HubProxyModel createFromParcel(Parcel source) {
            return new HubProxyModel(source);
        }

        @Override public HubProxyModel[] newArray(int size) {
            return new HubProxyModel[size];
        }
    };
}
