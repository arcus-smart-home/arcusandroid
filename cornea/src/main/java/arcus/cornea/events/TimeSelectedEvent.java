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
package arcus.cornea.events;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class TimeSelectedEvent {
    private int hourValue;
    private int minuteValue;
    private TimePickerType pickerType;

    public enum TimePickerType {
        ALL_DAY,
        START,
        END,
        SINGLE_TIME
    }

    public TimeSelectedEvent(int hourValue, int minuteValue, TimePickerType type) {
        this.hourValue = hourValue;
        this.minuteValue = minuteValue;
        this.pickerType = type;
    }

    public int getHourValue() {
        return hourValue;
    }

    public int getMinuteValue() {
        return minuteValue;
    }

    public String getAsTime() {
        String hour = (hourValue < 10 ? "0" : "") + hourValue;
        String minute = (minuteValue < 10 ? "0" : "") + minuteValue;
        return hour + ":" + minute + ":00";
    }

    public String getAsTimeString (DateFormat format) {
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, hourValue);
        cal.set(Calendar.MINUTE, minuteValue);

        return format.format(cal.getTime());
    }

    public boolean isAllDayEvent() {
        return pickerType.equals(TimePickerType.ALL_DAY);
    }

    public TimePickerType getPickerType() {
        return pickerType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TimeSelectedEvent that = (TimeSelectedEvent) o;

        if (hourValue != that.hourValue) {
            return false;
        }
        if (minuteValue != that.minuteValue) {
            return false;
        }
        return pickerType == that.pickerType;

    }

    @Override
    public int hashCode() {
        int result = hourValue;
        result = 31 * result + minuteValue;
        result = 31 * result + (pickerType != null ? pickerType.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TimeSelectedEvent{" +
              "hourValue=" + hourValue +
              ", minuteValue=" + minuteValue +
              ", pickerType=" + pickerType +
              '}';
    }
}
