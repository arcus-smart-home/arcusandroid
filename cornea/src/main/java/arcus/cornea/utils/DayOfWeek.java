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

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.text.WordUtils;

import java.util.Calendar;
import java.util.EnumSet;
import java.util.Set;

/**
 * 
 */
public enum DayOfWeek {
   MONDAY,
   TUESDAY,
   WEDNESDAY,
   THURSDAY,
   FRIDAY,
   SATURDAY,
   SUNDAY;
   
   public static DayOfWeek from(Calendar calendar) {
      Preconditions.checkNotNull(calendar, "calendar may not be null");
      int dayId = calendar.get(Calendar.DAY_OF_WEEK);
      switch(dayId) {
      case Calendar.MONDAY:     return MONDAY;
      case Calendar.TUESDAY:    return TUESDAY;
      case Calendar.WEDNESDAY:  return WEDNESDAY;
      case Calendar.THURSDAY:   return THURSDAY;
      case Calendar.FRIDAY:     return FRIDAY;
      case Calendar.SATURDAY:   return SATURDAY;
      case Calendar.SUNDAY:     return SUNDAY;
      default: throw new IllegalArgumentException("Unrecognized day of week: " + dayId);
      }
   }

   public static DayOfWeek from(String abbrev) {
      for(DayOfWeek day : DayOfWeek.values()) {
         if(abbrev.equalsIgnoreCase(day.name().substring(0, 3))) {
            return day;
         }
      }
      throw new IllegalArgumentException("Unrecognized day of week: " + abbrev);
   }

   public static DayOfWeek fromFullName(String dayOfWeek) {
      for(DayOfWeek day : DayOfWeek.values()) {
         if (day.name().equalsIgnoreCase(dayOfWeek)) {
            return day;
         }
      }
      throw new IllegalArgumentException("Unrecognized day of week: " + dayOfWeek);
   }

   public static String[] stringRepresentation() {
      String[] stringValues = new String[values().length];
      for (DayOfWeek day : values()) {
         stringValues[day.ordinal()] = WordUtils.capitalize(day.name().substring(0,3).toLowerCase());
      }
      return stringValues;
   }

   public static DayOfWeek getNextDayFrom(DayOfWeek dayOfWeek) {
      switch (dayOfWeek) {
         case MONDAY: return TUESDAY;
         case TUESDAY: return WEDNESDAY;
         case WEDNESDAY: return THURSDAY;
         case THURSDAY: return FRIDAY;
         case FRIDAY: return SATURDAY;
         case SATURDAY: return SUNDAY;
         case SUNDAY: return MONDAY;

      }

      return MONDAY;
   }

   public static final Set<DayOfWeek> ALLDAYS = EnumSet.allOf(DayOfWeek.class);
   public static final Set<DayOfWeek> WEEKDAYS = EnumSet.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY);
   public static final Set<DayOfWeek> WEEKENDS = EnumSet.of(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);

}

