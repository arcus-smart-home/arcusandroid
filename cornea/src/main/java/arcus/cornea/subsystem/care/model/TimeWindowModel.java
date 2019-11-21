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

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.collect.ImmutableMap;
import arcus.cornea.utils.DayOfWeek;

import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeWindowModel implements Parcelable {
    private static final TimeWindowModel EMPTY = new TimeWindowModel();
    private DayOfWeek day;
    private String startTime; // "HH:MM:SS"
    private Integer durationSecs;

    protected TimeWindowModel() {
        day = DayOfWeek.MONDAY;
        startTime = "12:00:00";
        durationSecs  = 0;
    }

    public TimeWindowModel(DayOfWeek day, String startTime, Integer duration) {
        this.day = day;
        this.startTime = startTime;
        this.durationSecs = duration;
    }

    public static TimeWindowModel fromMap(@NonNull Map<String, Object> timeWindow) {
        try {
            Number timeDuration = (Number) timeWindow.get(CareKeys.ATTR_TIMEWINDOW_DURATIONSECS.attrName());
            return new TimeWindowModel(
                  DayOfWeek.fromFullName((String) timeWindow.get(CareKeys.ATTR_TIMEWINDOW_DAY.attrName())),
                  (String) timeWindow.get(CareKeys.ATTR_TIMEWINDOW_STARTTIME.attrName()),
                  timeDuration == null ? 0 : timeDuration.intValue()
            );
        }
        catch (Exception ex) {
            LoggerFactory.getLogger(TimeWindowModel.class).debug("Error creating time window.", ex);
            return EMPTY;
        }
    }

    public Map<String, Object> toMap() {
        return ImmutableMap.<String, Object>of(
              CareKeys.ATTR_TIMEWINDOW_DAY.attrName(), WordUtils.capitalize(day.name().toLowerCase()),
              CareKeys.ATTR_TIMEWINDOW_DURATIONSECS.attrName(), durationSecs,
              CareKeys.ATTR_TIMEWINDOW_STARTTIME.attrName(), startTime
        );
    }

    public void setDay(DayOfWeek day) {
        this.day = day;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setDurationSecs(Integer duration) {
        this.durationSecs = duration;
    }

    public DayOfWeek getDay() {
        return day;
    }

    public String getStartTime() {
        return startTime;
    }

    public Integer getDurationSecs() {
        return durationSecs;
    }

    public int getStartHour() {
        Integer time = getTimeAt(0);
        return time == null ? 0 : time;
    }

    public int getStartMinute() {
        Integer time = getTimeAt(1);
        return time == null ? 0 : time;
    }

    public int getStartSecond() {
        Integer time = getTimeAt(2);
        return time == null ? 0 : time;
    }

    public DayOfWeek getEndDay() {
        return DayOfWeek.from(getEndTimeCalendar());
    }

    public int getEndHour() {
        return getEndTimeCalendar().get(Calendar.HOUR_OF_DAY);
    }

    public int getEndMinute() {
        return getEndTimeCalendar().get(Calendar.MINUTE);
    }

    /**
     * Calculates the time in seconds from {@link #day} at {@link #startTime}
     * to {@code endDay} at {@code endHour}:{@code endMinute}
     *
     * @param endDay Ending day of the event
     * @param endHour Ending hour of the event (24  hour format)
     * @param endMinute Ending minute of the event
     */
    public void calculateAndSetDurationTo(DayOfWeek endDay, int endHour, int endMinute) {
        try {
            long startTime = TimeUnit.HOURS.toSeconds(getStartHour()) + TimeUnit.MINUTES.toSeconds(getStartMinute());
            long endTime =   TimeUnit.HOURS.toSeconds(endHour)   + TimeUnit.MINUTES.toSeconds(endMinute);

            if (day.ordinal() < endDay.ordinal()) { // If start < end it is -> same week no wraparound.
                endTime += TimeUnit.DAYS.toSeconds(endDay.ordinal() - day.ordinal());
            }
            else if (!day.equals(endDay)) { // we are going Thurs -> Mon, or Fri->Tues or Sunday -> Monday
                endTime += TimeUnit.DAYS.toSeconds(7 - day.ordinal() + endDay.ordinal());
            }
            // else {} we do nothing to modify the endTime since it starts and stops on the same day.

            durationSecs = (int) (endTime - startTime);
        }
        catch (Exception ignore) {
            durationSecs = 0;
        }
    }

    // Checks for a valid duration; ie duration of rule is > 0
    public boolean isValidWindow() {
        return durationSecs != null && durationSecs > 0;
    }

    // Used for sorting. This takes the base of the day and converts that to seconds.
    // Then adds in it's hour + minute value
    // Since we are duplicating the time window on each day of the week, this will help sort events (and keep them at the top)
    // that start on a previous day. IE:
    // Event goes 5PM Tuesday -> 10PM Friday
    // While viewing Friday's schedule, we don't want to accidentally put this BELOW events that start at say 4PM Friday
    // We want to keep this ABOVE that event (it starts on the previous day after all)
    public long getStartValue() {
        if (day == null || TextUtils.isEmpty(startTime)) {
            return 0;
        }

        return TimeUnit.DAYS.toSeconds(day.ordinal()) +
              TimeUnit.HOURS.toSeconds(getStartHour()) +
              TimeUnit.MINUTES.toSeconds(getStartMinute());
    }

    public String getStringRepresentation() {
        DateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        Calendar startCal = getStartTimeCalendar();
        Calendar endCal   = getEndTimeCalendar();

        String startDOW = getCalendarAbbrFor(startCal.get(Calendar.DAY_OF_WEEK));
        String startTime = sdf.format(startCal.getTime());

        String endDOW = getCalendarAbbrFor(endCal.get(Calendar.DAY_OF_WEEK));
        String endTime = sdf.format(endCal.getTime());

        return String.format("%s %s - %s %s", startDOW, startTime, endDOW, endTime);
    }

    protected int getCalendarDayFor(@NonNull DayOfWeek day) {
        switch (day) {
            case MONDAY: return Calendar.MONDAY;
            case TUESDAY: return Calendar.TUESDAY;
            case WEDNESDAY: return Calendar.WEDNESDAY;
            case THURSDAY: return Calendar.THURSDAY;
            case FRIDAY: return Calendar.FRIDAY;
            case SATURDAY: return Calendar.SATURDAY;

            default:
            case SUNDAY: return Calendar.SUNDAY;
        }
    }

    protected String getCalendarAbbrFor(int day) {
        switch (day) {
            case Calendar.MONDAY: return "Mo";
            case Calendar.TUESDAY: return "Tu";
            case Calendar.WEDNESDAY: return "We";
            case Calendar.THURSDAY: return "Th";
            case Calendar.FRIDAY: return "Fr";

            default:
            case Calendar.SUNDAY: return "Su";
        }
    }

    protected Calendar getStartTimeCalendar() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, getCalendarDayFor(day));
        cal.set(Calendar.HOUR_OF_DAY, getStartHour());
        cal.set(Calendar.MINUTE, getStartMinute());
        cal.set(Calendar.SECOND, getStartSecond());

        return cal;
    }

    protected Calendar getEndTimeCalendar() {
        Calendar cal = getStartTimeCalendar();
        cal.add(Calendar.SECOND, getDurationSecs());

        return cal;
    }

    private @Nullable Integer getTimeAt(int index) {
        if (TextUtils.isEmpty(startTime)) {
            return null;
        }

        String[] times = startTime.split(":");
        try {
            return Integer.valueOf(times[index]);
        }
        catch (Exception ex) {
            return null;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TimeWindowModel that = (TimeWindowModel) o;

        if (day != that.day) {
            return false;
        }
        if (startTime != null ? !startTime.equals(that.startTime) : that.startTime != null) {
            return false;
        }
        return !(durationSecs != null ? !durationSecs.equals(that.durationSecs) : that.durationSecs != null);

    }

    @Override
    public int hashCode() {
        int result = day != null ? day.hashCode() : 0;
        result = 31 * result + (startTime != null ? startTime.hashCode() : 0);
        result = 31 * result + (durationSecs != null ? durationSecs.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TimeWindowModel{" +
              "day=" + day +
              ", startTime='" + startTime + '\'' +
              ", durationSecs=" + durationSecs +
              '}';
    }

    @Override public int describeContents() {
        return 0;
    }

    @Override public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.day == null ? -1 : this.day.ordinal());
        dest.writeString(this.startTime);
        dest.writeValue(this.durationSecs);
    }

    protected TimeWindowModel(Parcel in) {
        int tmpDay = in.readInt();
        this.day = tmpDay == -1 ? null : DayOfWeek.values()[tmpDay];
        this.startTime = in.readString();
        this.durationSecs = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    public static final Parcelable.Creator<TimeWindowModel> CREATOR = new Parcelable.Creator<TimeWindowModel>() {
        public TimeWindowModel createFromParcel(Parcel source) {
            return new TimeWindowModel(source);
        }

        public TimeWindowModel[] newArray(int size) {
            return new TimeWindowModel[size];
        }
    };
}
