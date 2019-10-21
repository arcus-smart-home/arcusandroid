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
package arcus.cornea.subsystem.security.model;


public class SettingsModel {
    private int exitDelayOnSec;
    private int entranceDelayOnSec;
    private int exitDelayPartialSec;
    private int entranceDelayPartialSec;
    private int alarmSensitivity;
    private int onAlarmSensitivity;
    private int partialAlarmSensitivity;
    private boolean enableSounds;
    private boolean silentAlarm;
    private int totalOnDevices;
    private int totalPartialDevices;
    private int onMotionSensorsCount;
    private int partialMotionSensorsCount;

    public int getExitDelayOnSec() {
        return exitDelayOnSec;
    }

    public void setExitDelayOnSec(int exitDelayOnSec) {
        this.exitDelayOnSec = exitDelayOnSec;
    }

    public int getEntranceDelayOnSec() {
        return entranceDelayOnSec;
    }

    public void setEntranceDelayOnSec(int entranceDelayOnSec) {
        this.entranceDelayOnSec = entranceDelayOnSec;
    }

    public int getExitDelayPartialSec() {
        return exitDelayPartialSec;
    }

    public void setExitDelayPartialSec(int exitDelayPartialSec) {
        this.exitDelayPartialSec = exitDelayPartialSec;
    }

    public int getEntranceDelayPartialSec() {
        return entranceDelayPartialSec;
    }

    public void setEntranceDelayPartialSec(int entranceDelayPartialSec) {
        this.entranceDelayPartialSec = entranceDelayPartialSec;
    }

    public int getAlarmSensitivity() {
        return alarmSensitivity;
    }

    public void setAlarmSensitivity(int alarmSensitivity) {
        this.alarmSensitivity = alarmSensitivity;
    }

    public boolean isEnableSounds() {
        return enableSounds;
    }

    public void setEnableSounds(boolean enableSounds) {
        this.enableSounds = enableSounds;
    }

    public boolean isSilentAlarm() {
        return silentAlarm;
    }

    public void setSilentAlarm(boolean silentAlarm) {
        this.silentAlarm = silentAlarm;
    }


    public int getTotalOnDevices() {
        return totalOnDevices;
    }

    public void setTotalOnDevices(int totalOnDevices) {
        this.totalOnDevices = totalOnDevices;
    }

    public int getTotalPartialDevices() {
        return totalPartialDevices;
    }

    public void setTotalPartialDevices(int totalPartialDevices) {
        this.totalPartialDevices = totalPartialDevices;
    }

    public int getOnAlarmSensitivity() {
        return onAlarmSensitivity;
    }

    public void setOnAlarmSensitivity(int onAlarmSensitivity) {
        this.onAlarmSensitivity = onAlarmSensitivity;
    }

    public int getPartialAlarmSensitivity() {
        return partialAlarmSensitivity;
    }

    public void setPartialAlarmSensitivity(int partialAlarmSensitivity) {
        this.partialAlarmSensitivity = partialAlarmSensitivity;
    }

    public int getOnMotionSensorsCount() {
        return onMotionSensorsCount;
    }

    public void setOnMotionSensorsCount(int onMotionSensorsCount) {
        this.onMotionSensorsCount = onMotionSensorsCount;
    }

    public int getPartialMotionSensorsCount() {
        return partialMotionSensorsCount;
    }

    public void setPartialMotionSensorsCount(int partialMotionSensorsCount) {
        this.partialMotionSensorsCount = partialMotionSensorsCount;
    }
}
