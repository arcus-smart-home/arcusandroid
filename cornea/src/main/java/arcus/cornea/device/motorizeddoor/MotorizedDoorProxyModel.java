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
package arcus.cornea.device.motorizeddoor;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MotorizedDoorProxyModel {

    public enum State {
        CLOSED,CLOSING,OBSTRUCTION, OPENING, OPEN;

        public String label() {
            return StringUtils.capitalize(StringUtils.lowerCase(name()));
        }
    }

    private String deviceId;
    private String deviceTypeHint;
    private String name;
    private State state;
    private Date lastStateChange;
    private boolean online;
    private Map<String, String> errors = new HashMap<String, String>();

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceTypeHint() {
        return deviceTypeHint;
    }

    public void setDeviceTypeHint(String deviceTypeHint) {
        this.deviceTypeHint = deviceTypeHint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public Date getLastStateChange() {
        return lastStateChange;
    }

    public void setLastStateChange(Date lastStateChange) {
        this.lastStateChange = lastStateChange;
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MotorizedDoorProxyModel that = (MotorizedDoorProxyModel) o;

        if (online != that.online) return false;
        if (deviceId != null ? !deviceId.equals(that.deviceId) : that.deviceId != null)
            return false;
        if (deviceTypeHint != null ? !deviceTypeHint.equals(that.deviceTypeHint) : that.deviceTypeHint != null)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (state != that.state) return false;
        return !(lastStateChange != null ? !lastStateChange.equals(that.lastStateChange) : that.lastStateChange != null);

    }

    @Override
    public int hashCode() {
        int result = deviceId != null ? deviceId.hashCode() : 0;
        result = 31 * result + (deviceTypeHint != null ? deviceTypeHint.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (state != null ? state.hashCode() : 0);
        result = 31 * result + (lastStateChange != null ? lastStateChange.hashCode() : 0);
        result = 31 * result + (online ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MotorizedDoorProxyModel{" +
                "deviceId='" + deviceId + '\'' +
                ", deviceTypeHint='" + deviceTypeHint + '\'' +
                ", name='" + name + '\'' +
                ", state=" + state +
                ", lastStateChange=" + lastStateChange +
                ", online=" + online +
                '}';
    }
}
