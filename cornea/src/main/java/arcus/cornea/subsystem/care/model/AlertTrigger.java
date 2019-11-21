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
package arcus.cornea.subsystem.care.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import java.util.Date;

public class AlertTrigger implements Comparable<AlertTrigger> {
    private String triggerTitle;
    private String triggerDescription;
    private Date triggerTime;
    private TriggerType triggerType;
    private String triggerID;

    public enum TriggerType {
        PANIC,
        BEHAVIOR,
        RULE
    }

    public AlertTrigger() {}

    public AlertTrigger(AlertTrigger copy) {
        this.triggerTitle = copy.getTriggerTitle();
        this.triggerDescription = copy.getTriggerDescription();
        this.triggerTime = copy.getTriggerTime();
        this.triggerType = copy.getTriggerType();
        this.triggerID = copy.getTriggerID();
    }

    public @Nullable String getTriggerTitle() {
        return triggerTitle;
    }

    public void setTriggerTitle(String triggerTitle) {
        this.triggerTitle = triggerTitle;
    }

    public @NonNull String getTriggerDescription() {
        return TextUtils.isEmpty(triggerDescription) ? "" : triggerDescription;
    }

    public void setTriggerDescription(String triggerDescription) {
        this.triggerDescription = triggerDescription;
    }

    public @Nullable Date getTriggerTime() {
        return triggerTime;
    }

    public void setTriggerTime(Date triggerTime) {
        this.triggerTime = triggerTime;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public String getTriggerID() {
        return triggerID;
    }

    public void setTriggerID(String triggerID) {
        this.triggerID = triggerID;
    }

    @Override public int compareTo(@NonNull AlertTrigger another) {
        if(getTriggerTime() == null) {
            return another.getTriggerTime() == null ? 0 : -1;
        }
        if(another.getTriggerTime() == null) {
            return 1;
        }

        return another.getTriggerTime().compareTo(triggerTime);
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AlertTrigger that = (AlertTrigger) o;

        if (triggerTitle != null ? !triggerTitle.equals(that.triggerTitle) : that.triggerTitle != null) {
            return false;
        }
        if (triggerDescription != null ? !triggerDescription.equals(that.triggerDescription) : that.triggerDescription != null) {
            return false;
        }
        if (triggerTime != null ? !triggerTime.equals(that.triggerTime) : that.triggerTime != null) {
            return false;
        }
        if (triggerType != that.triggerType) {
            return false;
        }
        return !(triggerID != null ? !triggerID.equals(that.triggerID) : that.triggerID != null);

    }

    @Override public int hashCode() {
        int result = triggerTitle != null ? triggerTitle.hashCode() : 0;
        result = 31 * result + (triggerDescription != null ? triggerDescription.hashCode() : 0);
        result = 31 * result + (triggerTime != null ? triggerTime.hashCode() : 0);
        result = 31 * result + (triggerType != null ? triggerType.hashCode() : 0);
        result = 31 * result + (triggerID != null ? triggerID.hashCode() : 0);
        return result;
    }

    @Override public String toString() {
        return "AlertTrigger{" +
              "triggerTitle='" + triggerTitle + '\'' +
              ", triggerDescription='" + triggerDescription + '\'' +
              ", triggerTime=" + triggerTime +
              ", triggerType=" + triggerType +
              ", triggerID='" + triggerID + '\'' +
              '}';
    }
}
