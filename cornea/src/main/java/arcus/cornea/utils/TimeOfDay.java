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
/**
 * 
 */
package arcus.cornea.utils;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;

import java.io.Serializable;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeOfDay implements Comparable<TimeOfDay>, Serializable, Parcelable {
   public static final TimeOfDay MIDNIGHT = new TimeOfDay(0, 0, 0);

   private static final Pattern TOD_PATTERN = Pattern.compile("(\\d{1,2})\\:(\\d{2}):(\\d{2})");

   public static TimeOfDay fromString(String tod) {
      Matcher m = TOD_PATTERN.matcher(tod);
      Preconditions.checkArgument(m.matches(), "Invalid time of day [" + tod + "]");
      int hours = Integer.parseInt(m.group(1));
      int min = Integer.parseInt(m.group(2));
      int sec = Integer.parseInt(m.group(3));
      return new TimeOfDay(hours, min, sec);
   }

   public static TimeOfDay fromStringWithMode(String tod, SunriseSunset mode, Integer offset) {
      Matcher m = TOD_PATTERN.matcher(tod);
      Preconditions.checkArgument(m.matches(), "Invalid time of day [" + tod + "]");
      int hours = Integer.parseInt(m.group(1));
      int min = Integer.parseInt(m.group(2));
      int sec = Integer.parseInt(m.group(3));

      if (offset == null) {
         offset = 0;
      }
      return new TimeOfDay(hours, min, sec, mode, offset);
   }

   private final int hours;
   private final int minutes;
   private final int seconds;
   private final DayTime dayTime;

   private final SunriseSunset sunriseSunset;
   private final int offset;

   public TimeOfDay(Calendar calendar) {
      this(calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
   }
   
   public TimeOfDay(int hours) {
      this(hours, 0, 0);
   }
   
   public TimeOfDay(int hours, int minutes, int seconds) {
      this(hours, minutes, seconds, SunriseSunset.ABSOLUTE, 0);
   }

   public TimeOfDay(SunriseSunset sunriseSunset, int offset) {
      this(0, 0, 0, sunriseSunset, offset);
   }

   public TimeOfDay(int hours, int minutes, int seconds, SunriseSunset sunriseSunset, int offset) {
      Preconditions.checkArgument(hours >= 0 && hours <= 24, "hours must be between 0 and 24");
      Preconditions.checkArgument(minutes >= 0 && minutes <= 59, "minutes must be between 0 and 59");
      Preconditions.checkArgument(seconds >= 0 && seconds <= 59, "seconds must be between 0 and 59");
      this.hours = hours;
      this.minutes = minutes;
      this.seconds = seconds;
      this.sunriseSunset = sunriseSunset;
      this.offset = offset;
      this.dayTime = DayTime.from(this);
   }
   
   public int getHours() {
      return hours;
   }

   public int getMinutes() {
      return minutes;
   }

   public int getSeconds() {
      return seconds;
   }

   public DayTime getDayTime() {
      return dayTime;
   }

   public SunriseSunset getSunriseSunset() {
      return sunriseSunset;
   }

   public int getOffset() {
      return offset;
   }

   public boolean isBefore(TimeOfDay o) {
      return compareTo(o) < 0;
   }
   
   public boolean isAfter(TimeOfDay o) {
      return compareTo(o) > 0;
   }

   /**
    * Gets the next {@link Calendar} at which the
    * time of day will be equal to this object.
    * @param from
    * @return
    */
   public Calendar next(Calendar from) {
      Calendar next = on(from);
      if(!next.after(from)) {
         next.add(Calendar.DAY_OF_MONTH, 1);
      }
      return next;
   }

   /**
    * Gets an instance of the Calender where
    * the TimeOfDay is equal to {@code this}.
    * @param day
    * @return
    */
   public Calendar on(Calendar day) {
      Calendar on = (Calendar) day.clone();
      on.set(Calendar.HOUR_OF_DAY, hours);
      on.set(Calendar.MINUTE, minutes);
      on.set(Calendar.SECOND, seconds);
      on.set(Calendar.MILLISECOND, 0);
      return on;
   }

   @Override
   public int compareTo(@NonNull TimeOfDay o) {
      if(o == null) {
         return toSeconds() + 1;
      }
      return toSeconds() - o.toSeconds();
   }

   public String toString() {
      return hours + ":" + (minutes < 10 ? "0" : "") + minutes + ":" + (seconds < 10 ? "0" : "") + seconds;
   }
   
   public int toSeconds() {
      return hours * 3600 + minutes * 60 + seconds;
   }

   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + hours;
      result = prime * result + minutes;
      result = prime * result + seconds;
      return result;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) return true;
      if (obj == null) return false;
      if (getClass() != obj.getClass()) return false;
      TimeOfDay other = (TimeOfDay) obj;
      if (hours != other.hours) return false;
      if (minutes != other.minutes) return false;
      return seconds == other.seconds;
   }

   @Override public int describeContents() {
      return 0;
   }

   @Override public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(this.hours);
      dest.writeInt(this.minutes);
      dest.writeInt(this.seconds);
      dest.writeInt(this.dayTime == null ? -1 : this.dayTime.ordinal());
      dest.writeInt(this.sunriseSunset == null ? -1 : this.sunriseSunset.ordinal());
      dest.writeInt(this.offset);
   }

   protected TimeOfDay(Parcel in) {
      this.hours = in.readInt();
      this.minutes = in.readInt();
      this.seconds = in.readInt();
      int tmpDayTime = in.readInt();
      this.dayTime = tmpDayTime == -1 ? null : DayTime.values()[tmpDayTime];
      int tmpSunriseSunset = in.readInt();
      this.sunriseSunset = tmpSunriseSunset == -1 ? null : SunriseSunset.values()[tmpSunriseSunset];
      this.offset = in.readInt();
   }

   public static final Parcelable.Creator<TimeOfDay> CREATOR =
         new Parcelable.Creator<TimeOfDay>() {
            @Override
            public TimeOfDay createFromParcel(Parcel source) {
               return new TimeOfDay(source);
            }

            @Override
            public TimeOfDay[] newArray(int size) {
               return new TimeOfDay[size];
            }
         };
}
