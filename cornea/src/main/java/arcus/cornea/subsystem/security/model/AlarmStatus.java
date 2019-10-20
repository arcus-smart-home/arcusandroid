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

import com.iris.client.capability.SecuritySubsystem;

import java.util.Date;


public class AlarmStatus {
    public final static String STATE_DISARMED = "Disarmed";
    public final static String STATE_ARMING = "Arming";
    public final static String STATE_ARMED = "Armed";

    public final static String MODE_OFF = SecuritySubsystem.ALARMMODE_OFF;
    public final static String MODE_ON = SecuritySubsystem.ALARMMODE_ON;
    public final static String MODE_PARTIAL = SecuritySubsystem.ALARMMODE_PARTIAL;

    private String state;
    private String mode;
    private Date date;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }
}
