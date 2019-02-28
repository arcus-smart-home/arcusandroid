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
package arcus.cornea.subsystem.lightsnswitches.model;

import android.os.Parcel;
import android.os.Parcelable;

public class LightsNSwitchesDevice implements Parcelable {
    public enum Type {
        DIMMER,
        LIGHT,
        SWITCH,
        HALO,
        UNKNOWN
    }

    private Type   deviceType;
    private String address;
    private String deviceId;
    private String deviceName;
    private int dimPercent;
    private int colorTemp;
    private int colorMinTemp;
    private int colorMaxTemp;
    private int colorHue;
    private int colorSaturation;
    private String colorMode;
    private boolean on;
    private boolean isOffline;
    private boolean isOnBattery;
    private boolean isCloudDevice;
    private boolean colorTempChangeable;
    private boolean colorChangeable;
    private boolean switchable;
    private boolean dimmable;
    private String errorType;
    private String errorText;

    public LightsNSwitchesDevice() {}

    public Type getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(Type deviceType) {
        this.deviceType = deviceType;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public int getDimPercent() {
        return dimPercent;
    }

    public void setDimPercent(int dimPercent) {
        this.dimPercent = dimPercent;
    }

    public int getColorTemp() {
        return colorTemp;
    }

    public void setColorTemp(int colorTemp) {
        this.colorTemp = colorTemp;
    }

    public int getColorMinTemp() {
        return colorMinTemp;
    }

    public void setColorMinTemp(int colorMinTemp) {
        this.colorMinTemp = colorMinTemp;
    }

    public int getColorMaxTemp() {
        return colorMaxTemp;
    }

    public void setColorMaxTemp(int colorMaxTemp) {
        this.colorMaxTemp = colorMaxTemp;
    }

    public int getColorHue() {
        return colorHue;
    }

    public void setColorHue(int colorHue) {
        this.colorHue = colorHue;
    }

    public int getColorSaturation() {
        return colorSaturation;
    }

    public void setColorSaturation(int colorSaturation) {
        this.colorSaturation = colorSaturation;
    }

    public String getColorMode() {
        return colorMode;
    }

    public void setColorMode(String colorMode) {
        this.colorMode = colorMode;
    }

    public boolean isOn() {
        return on;
    }

    public void setOn(boolean on) {
        this.on = on;
    }

    public boolean isColorTempChangeable() {
        return colorTempChangeable;
    }

    public void setColorTempChangeable(boolean colorTempChangeable) {
        this.colorTempChangeable = colorTempChangeable;
    }

    public boolean isColorChangeable() {
        return colorChangeable;
    }

    public void setColorChangeable(boolean colorChangeable) {
        this.colorChangeable = colorChangeable;
    }

    public boolean isSwitchable() {
        return switchable;
    }

    public void setSwitchable(boolean switchable) {
        this.switchable = switchable;
    }

    public boolean isDimmable() {
        return dimmable;
    }

    public void setDimmable(boolean dimmable) {
        this.dimmable = dimmable;
    }

    public boolean isOffline() {
        return isOffline;
    }

    public void setIsOffline(boolean isOffline) {
        this.isOffline = isOffline;
    }

    public boolean isOnBattery() {
        return isOnBattery;
    }

    public void setOnBattery(boolean onBattery) {
        isOnBattery = onBattery;
    }

    public boolean isCloudDevice() {
        return isCloudDevice;
    }

    public void setIsCloudDevice(boolean isCloud) {
        isCloudDevice = isCloud;
    }

    public String getErrorType() { return errorType; }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getErrorText() { return errorText; }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LightsNSwitchesDevice that = (LightsNSwitchesDevice) o;

        if (dimPercent != that.dimPercent) return false;
        if (colorTemp != that.colorTemp) return false;
        if (colorMinTemp != that.colorMinTemp) return false;
        if (colorMaxTemp != that.colorMaxTemp) return false;
        if (colorHue != that.colorHue) return false;
        if (colorSaturation != that.colorSaturation) return false;
        if (on != that.on) return false;
        if (isOffline != that.isOffline) return false;
        if (isOnBattery != that.isOnBattery) return false;
        if (colorTempChangeable != that.colorTempChangeable) return false;
        if (colorChangeable != that.colorChangeable) return false;
        if (switchable != that.switchable) return false;
        if (dimmable != that.dimmable) return false;
        if (deviceType != that.deviceType) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (deviceId != null ? !deviceId.equals(that.deviceId) : that.deviceId != null)
            return false;
        if (deviceName != null ? !deviceName.equals(that.deviceName) : that.deviceName != null)
            return false;
        if (colorMode != null ? !colorMode.equals(that.colorMode) : that.colorMode != null)
            return false;
        if (errorType != null ? errorType.equals(that.errorType) : that.errorType == null) return false;
        return errorText != null ? errorText.equals(that.errorText) : that.errorText == null;

    }

    @Override
    public int hashCode() {
        int result = deviceType != null ? deviceType.hashCode() : 0;
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (deviceId != null ? deviceId.hashCode() : 0);
        result = 31 * result + (deviceName != null ? deviceName.hashCode() : 0);
        result = 31 * result + dimPercent;
        result = 31 * result + colorTemp;
        result = 31 * result + colorMinTemp;
        result = 31 * result + colorMaxTemp;
        result = 31 * result + colorHue;
        result = 31 * result + colorSaturation;
        result = 31 * result + (colorMode != null ? colorMode.hashCode() : 0);
        result = 31 * result + (on ? 1 : 0);
        result = 31 * result + (isOffline ? 1 : 0);
        result = 31 * result + (isOnBattery ? 1 : 0);
        result = 31 * result + (isCloudDevice ? 1 : 0);
        result = 31 * result + (colorTempChangeable ? 1 : 0);
        result = 31 * result + (colorChangeable ? 1 : 0);
        result = 31 * result + (switchable ? 1 : 0);
        result = 31 * result + (dimmable ? 1 : 0);
        result = 31 * result + (errorType != null ? errorType.hashCode() : 0);
        result = 31 * result + (errorText != null ? errorText.hashCode() : 0);
        return result;
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.deviceType == null ? -1 : this.deviceType.ordinal());
        dest.writeString(this.address);
        dest.writeString(this.deviceId);
        dest.writeString(this.deviceName);
        dest.writeInt(this.dimPercent);
        dest.writeInt(this.colorTemp);
        dest.writeInt(this.colorMinTemp);
        dest.writeInt(this.colorMaxTemp);
        dest.writeInt(this.colorHue);
        dest.writeInt(this.colorSaturation);
        dest.writeString(this.colorMode);
        dest.writeByte(this.on ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isOffline ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isCloudDevice ? (byte) 1 : (byte) 0);
        dest.writeByte(this.isOnBattery ? (byte) 1 : (byte) 0);
        dest.writeByte(this.colorTempChangeable ? (byte) 1 : (byte) 0);
        dest.writeByte(this.colorChangeable ? (byte) 1 : (byte) 0);
        dest.writeByte(this.switchable ? (byte) 1 : (byte) 0);
        dest.writeByte(this.dimmable ? (byte) 1 : (byte) 0);
        dest.writeString(this.errorType);
        dest.writeString(this.errorText);
    }

    protected LightsNSwitchesDevice(Parcel in) {
        int tmpDeviceType = in.readInt();
        this.deviceType = tmpDeviceType == -1 ? null : Type.values()[tmpDeviceType];
        this.address = in.readString();
        this.deviceId = in.readString();
        this.deviceName = in.readString();
        this.dimPercent = in.readInt();
        this.colorTemp = in.readInt();
        this.colorMinTemp = in.readInt();
        this.colorMaxTemp = in.readInt();
        this.colorHue = in.readInt();
        this.colorSaturation = in.readInt();
        this.colorMode = in.readString();
        this.on = in.readByte() != 0;
        this.isOffline = in.readByte() != 0;
        this.isCloudDevice = in.readByte() != 0;
        this.isOnBattery = in.readByte() != 0;
        this.colorTempChangeable = in.readByte() != 0;
        this.colorChangeable = in.readByte() != 0;
        this.switchable = in.readByte() != 0;
        this.dimmable = in.readByte() != 0;
        this.errorType = in.readString();
        this.errorText = in.readString();
    }

    public static final Creator<LightsNSwitchesDevice> CREATOR = new Creator<LightsNSwitchesDevice>() {
        @Override public LightsNSwitchesDevice createFromParcel(Parcel source) {
            return new LightsNSwitchesDevice(source);
        }

        @Override public LightsNSwitchesDevice[] newArray(int size) {
            return new LightsNSwitchesDevice[size];
        }
    };
}
