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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.model.StringPair;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HaloModel {
    private boolean isHaloPlus;
    private boolean isFavorite;
    private boolean isOnBattery;
    private boolean isLightOn;
    private boolean isFirmwareUpdating;
    private boolean isOnline;
    private boolean lastTestPassed;
    private boolean isRadioPlaying;
    private boolean isPreSmoke;

    private int dimmerPercent;
    private int batteryLevel;
    private int errors = 0;

    private float hue;
    private float saturation;

    private String humidity;
    private String atmosphericPressure;
    private String temperature;
    private String name = "";
    private String lastTested;
    private String lastTestResult;

    private final String deviceAddress;
    private final List<StringPair> errorList = new ArrayList<>();

    public static HaloModel empty() {
        return new HaloModel(null);
    }

    public HaloModel(String deviceAddress) {
        this.deviceAddress = deviceAddress == null ? "DRIV:dev:" : deviceAddress;
    }

    public boolean isHaloPlus() {
        return isHaloPlus;
    }

    public void setHaloPlus(boolean haloPlus) {
        isHaloPlus = haloPlus;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public int getDimmerPercent() {
        return dimmerPercent;
    }

    public void setDimmerPercent(@Nullable Number dimmerPercent) {
        this.dimmerPercent = dimmerPercent == null ? 0 : dimmerPercent.intValue();
    }

    @NonNull
    public String getHumidity() {
        return humidity;
    }

    public void setHumidity(@NonNull String humidity) {
        this.humidity = humidity;
    }

    @NonNull
    public String getAtmosphericPressure() {
        return atmosphericPressure;
    }

    public void setAtmosphericPressure(@NonNull String atmosphericPressure) {
        this.atmosphericPressure = atmosphericPressure;
    }

    @NonNull
    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(@NonNull String temperature) {
        this.temperature = temperature;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name == null ? "" : name;
    }

    public boolean isOnBattery() {
        return isOnBattery;
    }

    public void setOnBattery(boolean onBattery) {
        isOnBattery = onBattery;
    }

    public boolean isLightOn() {
        return isLightOn;
    }

    public void setLightOn(boolean lightOn) {
        isLightOn = lightOn;
    }

    public boolean isFirmwareUpdating() {
        return isFirmwareUpdating;
    }

    public void setFirmwareUpdating(boolean firmwareUpdating) {
        isFirmwareUpdating = firmwareUpdating;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public float getHue() {
        return hue;
    }

    public void setHue(@Nullable Number hue) {
        this.hue = hue == null ? 0 : hue.floatValue();
    }

    public float getSaturation() {
        return saturation;
    }

    public void setSaturation(Number saturation) {
        this.saturation = saturation == null ? 0 : saturation.floatValue();
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Number batteryLevel) {
        this.batteryLevel = batteryLevel == null ? 0 : batteryLevel.intValue();
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public boolean hasErrors() {
        return errors != 0;
    }

    public boolean hasMultipleErrors() {
        return errors > 1;
    }

    public boolean hasSingleError() {
        return errors == 1;
    }

    public void setErrors(Map<String, Object> errorMap) {
        if (errorMap == null || errorMap.isEmpty()) {
            return;
        }

        errorList.clear();
        for (Map.Entry<String, Object> item : errorMap.entrySet()) {
            errorList.add(new StringPair(item.getKey(), (String) item.getValue()));
        }

        errors = errorMap.size();
    }

    public List<StringPair> getErrors() {
        return Collections.unmodifiableList(errorList);
    }

    public void setLastTested(String lastTested) {
        this.lastTested = lastTested;
    }

    @Nullable
    public String getLastTested() {
        return lastTested;
    }

    @Nullable
    public String getLastTestResult() {
        return lastTestResult;
    }

    public void setLastTestResult(String lastTestResult) {
        this.lastTestResult = lastTestResult;
    }

    public boolean isLastTestPassed() {
        return lastTestPassed;
    }

    public void setLastTestPassed(boolean lastTestPassed) {
        this.lastTestPassed = lastTestPassed;
    }

    public boolean isRadioPlaying() {
        return isRadioPlaying;
    }

    /**
     * Should only be set to true if the radio is playing AND the radio is not in alert state
     *
     * @param radioPlaying
     */
    public void setRadioPlaying(boolean radioPlaying) {
        isRadioPlaying = radioPlaying;
    }

    public boolean isPreSmoke() {
        return isPreSmoke;
    }

    public HaloModel setPreSmoke(boolean preSmoke) {
        isPreSmoke = preSmoke;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        HaloModel haloModel = (HaloModel) o;

        return deviceAddress.equals(haloModel.deviceAddress);

    }

    @Override
    public int hashCode() {
        return deviceAddress.hashCode();
    }

    @Override
    public String toString() {
        return "HaloModel{" +
              "isHaloPlus=" + isHaloPlus +
              ", deviceAddress='" + deviceAddress + '\'' +
              ", name='" + name + '\'' +
              ", isFavorite=" + isFavorite +
              ", isOnBattery=" + isOnBattery +
              ", isLightOn=" + isLightOn +
              ", isFirmwareUpdating=" + isFirmwareUpdating +
              ", isOnline=" + isOnline +
              ", dimmerPercent=" + dimmerPercent +
              ", batteryLevel=" + batteryLevel +
              ", hue=" + hue +
              ", saturation=" + saturation +
              ", humidity='" + humidity + '\'' +
              ", atmosphericPressure='" + atmosphericPressure + '\'' +
              ", temperature='" + temperature + '\'' +
              ", lastTested=" + lastTested +
              ", lastTestResult=" + lastTestResult +
              ", lastTestPassed=" + lastTestPassed +
              ", errorList=" + errorList +
              ", errors=" + errors +
              ", isRadioPlaying=" + isRadioPlaying +
              ", isPreSmoke=" + isPreSmoke +
              '}';
    }
}
