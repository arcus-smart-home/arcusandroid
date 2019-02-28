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

import java.util.Calendar;

public class ActivityLine {
    private long eventTime;
    private int hour12;
    private int hour24;
    private boolean isContact;

    public ActivityLine() {
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(eventTime);

        hour24 = calendar.get(Calendar.HOUR_OF_DAY);
        hour12 = calendar.get(Calendar.HOUR);
        if (hour12 == 0) {
            hour12 = 12;
        }
    }

    public boolean getIsContact() {
        return isContact;
    }

    public void setIsContact(boolean isContact) {
        this.isContact = isContact;
    }

    public int getHour24() {
        return hour24;
    }

    public void setHour24(int hour24) {
        this.hour24 = hour24;
    }

    public int getHour12() {
        return hour12;
    }

    public void setHour12(int hour12) {
        this.hour12 = hour12;
    }

    @Override public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ActivityLine that = (ActivityLine) o;

        if (eventTime != that.eventTime) {
            return false;
        }
        if (hour12 != that.hour12) {
            return false;
        }
        if (hour24 != that.hour24) {
            return false;
        }
        return isContact == that.isContact;

    }

    @Override public int hashCode() {
        int result = (int) (eventTime ^ (eventTime >>> 32));
        result = 31 * result + hour12;
        result = 31 * result + hour24;
        result = 31 * result + (isContact ? 1 : 0);
        return result;
    }

    @Override public String toString() {
        return "ActivityLine{" +
              "eventTime=" + eventTime +
              ", hour12=" + hour12 +
              ", hour24=" + hour24 +
              ", isContact=" + isContact +
              '}';
    }
}
