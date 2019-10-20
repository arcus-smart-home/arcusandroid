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


public class ArmingModel {
    String mode;
    int deviceCount;
    int countdownSec;

    public ArmingModel() {}

    public ArmingModel(String mode, int deviceCount, int countdownSec) {
        this.mode = mode;
        this.deviceCount = deviceCount;
        this.countdownSec = countdownSec;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public int getDeviceCount() {
        return deviceCount;
    }

    public void setDeviceCount(int deviceCount) {
        this.deviceCount = deviceCount;
    }

    public int getCountdownSec() {
        return countdownSec;
    }

    public void setCountdownSec(int countdownSec) {
        this.countdownSec = countdownSec;
    }

    @Override
    public String toString() {
        return "ArmingModel{" +
                "mode='" + mode + '\'' +
                ", deviceCount=" + deviceCount +
                ", countdownSec=" + countdownSec +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ArmingModel that = (ArmingModel) o;

        if (deviceCount != that.deviceCount) return false;
        if (countdownSec != that.countdownSec) return false;
        return !(mode != null ? !mode.equals(that.mode) : that.mode != null);

    }

    @Override
    public int hashCode() {
        int result = mode != null ? mode.hashCode() : 0;
        result = 31 * result + deviceCount;
        result = 31 * result + countdownSec;
        return result;
    }
}
