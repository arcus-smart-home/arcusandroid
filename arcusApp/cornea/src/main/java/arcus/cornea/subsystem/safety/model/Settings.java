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
package arcus.cornea.subsystem.safety.model;

import com.iris.client.capability.SafetySubsystem;

public class Settings {

    public static class Builder {
        private boolean waterShutoffAvailable;
        private boolean waterShutoffEnabled;
        private boolean silentAlarm;

        Builder() {
        }

        public Builder withSilentAlarm(boolean enable) {
            silentAlarm = enable;
            return this;
        }

        public Builder withWaterShutoffEnabled(boolean enabled) {
            this.waterShutoffEnabled = enabled;
            return this;
        }

        public Builder from(Settings settings) {
            silentAlarm = settings.isSilentAlarm();
            return this;
        }

        public Builder from(SafetySubsystem safety) {
            silentAlarm = safety.getSilentAlarm();
            waterShutoffEnabled = safety.getWaterShutOff();
            waterShutoffAvailable = safety.getWaterShutoffValves() != null && !safety.getWaterShutoffValves().isEmpty();
            return this;
        }

        public Settings build() {
            return new Settings(waterShutoffAvailable, waterShutoffEnabled, silentAlarm);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final boolean waterShutoffAvailable;
    private final boolean waterShutoffEnabled;
    private final boolean silentAlarm;

    Settings(boolean waterShutoffAvailable, boolean waterShutoffEnabled, boolean silentAlarm) {
        this.waterShutoffAvailable = waterShutoffAvailable;
        this.waterShutoffEnabled = waterShutoffEnabled;
        this.silentAlarm = silentAlarm;
    }

    public boolean isWaterShutoffAvailable() {
        return waterShutoffAvailable;
    }

    public boolean isWaterShutoffEnabled() {
        return waterShutoffEnabled;
    }

    public boolean isSilentAlarm() {
        return silentAlarm;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Settings settings = (Settings) o;

        if (waterShutoffAvailable != settings.waterShutoffAvailable) return false;
        if (waterShutoffEnabled != settings.waterShutoffEnabled) return false;
        return silentAlarm == settings.silentAlarm;

    }

    @Override
    public int hashCode() {
        int result = (waterShutoffAvailable ? 1 : 0);
        result = 31 * result + (waterShutoffEnabled ? 1 : 0);
        result = 31 * result + (silentAlarm ? 1 : 0);
        return result;
    }


    @Override
    public String toString() {
        return "Settings{" +
                "waterShutoffAvailable=" + waterShutoffAvailable +
                ", waterShutoffEnabled=" + waterShutoffEnabled +
                ", silentAlarm=" + silentAlarm +
                '}';
    }
}
