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
package arcus.cornea.device.camera.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;
import java.util.Map;

public class AvailableNetworkModel implements Parcelable {

    private String ssid;
    private WiFiSecurityType security;
    private Integer signal;
    private Boolean custom = false;
    private Integer frequency;

    public AvailableNetworkModel() {
    }

    @SuppressWarnings({"unchecked"})
    public AvailableNetworkModel(Map<String, Object> result) {
        if (result == null || result.isEmpty()) {
            ssid = "";
            signal = 0;
            security = WiFiSecurityType.NONE;
            frequency = 0;
        }
        else {
            ssid = String.valueOf(result.get("ssid"));
            Number signalStrength = (Number) result.get("signal");
            Number frequencyValue = (Number) result.get("frequency");
            if (signalStrength == null) {
                signal = 0;
            }
            else {
                signal = signalStrength.intValue();
            }
            if (frequency == null) {
                frequency = 0;
            }
            else {
                frequency = frequencyValue.intValue();
            }
            try {
                List<String> resultSecurity = (List<String>) result.get("security");
                String secType = String.valueOf(resultSecurity.get(0)).replace("-","_").toUpperCase();
                security = WiFiSecurityType.valueOf(secType);
            }
            catch (Exception ex) {
                ex.printStackTrace();
                security = WiFiSecurityType.NONE;
            }
        }
    }

    public AvailableNetworkModel(String ssid, WiFiSecurityType security, Integer signal, Integer frequency) {
        this.ssid = ssid;
        this.security = security;
        this.signal = signal;
        this.frequency = frequency;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public void setSignal(Integer signal) {
        this.signal = signal;
    }
    
    public void setFrequency(Integer frequency) { this.frequency = frequency; }

    public void setSecurity(WiFiSecurityType security) {
        this.security = security;
    }

    public AvailableNetworkModel(boolean isCustom) {
        this.custom = isCustom;
    }

    public String getSSID() {
        return ssid;
    }

    public WiFiSecurityType getSecurity() {
        return security;
    }

    public Integer getSignal() {
        return signal;
    }
   
    public Integer getfrequency() { return  frequency; }

    public Boolean isCustom() {
        return custom;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AvailableNetworkModel that = (AvailableNetworkModel) o;

        if (ssid != null ? !ssid.equals(that.ssid) : that.ssid != null) {
            return false;
        }
        if (security != that.security) {
            return false;
        }

        if (custom != null ? !custom.equals(this.custom) : that.custom != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = ssid != null ? ssid.hashCode() : 0;
        result = 31 * result + (security != null ? security.hashCode() : 0);
        result = 31 * result + (custom != null ? custom.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "AvailableNetworkModel{" +
              "ssid='" + ssid + '\'' +
              ", security=" + security +
              ", signal=" + signal +
              ", custom=" + custom +
              ", frequency=" + frequency +
              '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ssid);
        dest.writeInt(this.security == null ? -1 : this.security.ordinal());
        dest.writeValue(this.signal);
        dest.writeValue(this.custom);
    }

    protected AvailableNetworkModel(Parcel in) {
        this.ssid = in.readString();
        int tmpSecurity = in.readInt();
        this.security = tmpSecurity == -1 ? null : WiFiSecurityType.values()[tmpSecurity];
        this.signal = (Integer) in.readValue(Integer.class.getClassLoader());
        this.frequency = (Integer) in.readValue(Integer.class.getClassLoader()); 
        this.custom = (Boolean) in.readValue(Boolean.class.getClassLoader());
    }

    public static final Parcelable.Creator<AvailableNetworkModel> CREATOR = new Parcelable.Creator<AvailableNetworkModel>() {
        public AvailableNetworkModel createFromParcel(Parcel source) {
            return new AvailableNetworkModel(source);
        }

        public AvailableNetworkModel[] newArray(int size) {
            return new AvailableNetworkModel[size];
        }
    };
}
