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

import java.util.Date;

public class Alarm {
    private final String devId;
    private final String name;
    private final String message;
    private final Date time;

    public Alarm(String devId, String name, String message, Date time) {
        this.devId = devId;
        this.name = name;
        this.message = message;
        this.time = time;
    }

    public String getDevId() { return devId; }
    public String getName() { return name; }
    public String getMessage() { return message; }
    public Date getTime() { return time; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Alarm alarm = (Alarm) o;

        if (devId != null ? !devId.equals(alarm.devId) : alarm.devId != null) return false;
        if (name != null ? !name.equals(alarm.name) : alarm.name != null) return false;
        if (message != null ? !message.equals(alarm.message) : alarm.message != null) return false;
        return !(time != null ? !time.equals(alarm.time) : alarm.time != null);

    }

    @Override
    public int hashCode() {
        int result = devId != null ? devId.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (message != null ? message.hashCode() : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Alarm{" +
                "devId='" + devId + '\'' +
                ", name='" + name + '\'' +
                ", message='" + message + '\'' +
                ", time=" + time +
                '}';
    }
}
