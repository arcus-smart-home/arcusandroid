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
package arcus.cornea.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.lang3.text.WordUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class DateUtils {
    private static final String TIME = "%1$tl:%1$tM %1$Tp";
    public static final Map<Recency, String> DFLT_FMT_TABLE =
            ImmutableMap.of(
                    Recency.FUTURE, "%1$tb %1$te at " + TIME,
                    Recency.TODAY, "Today at " + TIME,
                    Recency.YESTERDAY, "Yesterday at " + TIME,
                    Recency.THIS_WEEK, "%1$ta at " + TIME,
                    Recency.PAST, "%1$tb %1$te at " + TIME
            );

    public enum Recency {
        FUTURE,
        TODAY,
        YESTERDAY,
        THIS_WEEK,
        PAST
    }

    public static Recency getRecency(Date date) {
        return getRecency(date, new Date());
    }

    public static Recency getRecency(Date then, Date now) {
        Calendar thenCal = Calendar.getInstance();
        thenCal.setTime(then);
        return getRecency(thenCal, now);
    }

    public static Recency getRecency(Calendar then, Date now) {
        Calendar copy = Calendar.getInstance();
        copy.setTimeZone(then.getTimeZone());
        copy.setTime(now);
        return doGetRecency(then, copy);
    }

    private static Recency doGetRecency(Calendar then, Calendar now) {
        int year = then.get(Calendar.YEAR);
        int day = then.get(Calendar.DAY_OF_YEAR);
        if(year > now.get(Calendar.YEAR)) {
            return Recency.FUTURE;
        }

        int nowDay = now.get(Calendar.DAY_OF_YEAR);
        if(year == now.get(Calendar.YEAR) && day == nowDay) {
            return Recency.TODAY;
        }

        // add so we don't have to worry about day roll-over and leap-years
        now.add(Calendar.DAY_OF_YEAR, -1);
        nowDay = now.get(Calendar.DAY_OF_YEAR);
        if(year == now.get(Calendar.YEAR) && day == nowDay) {
            return Recency.YESTERDAY;
        }

        // add so we don't have to worry about day roll-over and leap-years
        now.add(Calendar.DAY_OF_YEAR, -5);
        if(now.getTimeInMillis() < then.getTimeInMillis()) {
            return Recency.THIS_WEEK;
        }

        return Recency.PAST;
    }

    public static String format(Date date) {
        return format(date, new Date(), DFLT_FMT_TABLE);
    }

    public static String format(@NonNull TimeOfDay timeOfDay, boolean shortSunriseSetText) {
        return format(timeOfDay, null, shortSunriseSetText);
    }

    public static String format(@NonNull TimeOfDay timeOfDay, @Nullable String format, boolean shortSunriseSetText) {
        if (!SunriseSunset.ABSOLUTE.equals(timeOfDay.getSunriseSunset())) {
            return shortSunriseSetText ? formatSunriseSunsetShort(timeOfDay) : formatSunriseSunset(timeOfDay);
        }

        if (TextUtils.isEmpty(format)) {
            format = "h:mm aa";
        }

        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, timeOfDay.getHours());
        calendar.set(Calendar.MINUTE, timeOfDay.getMinutes());
        calendar.set(Calendar.SECOND, timeOfDay.getSeconds());
        calendar.set(Calendar.MILLISECOND, 0);

        return sdf.format(calendar.getTime());
    }

    public static String formatSunriseSunset(@NonNull TimeOfDay timeOfDay) {
        String riseSet = WordUtils.capitalize(timeOfDay.getSunriseSunset().name().toLowerCase());
        int offset = timeOfDay.getOffset();
        if (offset == 0) {
            return String.format("At %s", riseSet);
        }
        else {
            boolean before = offset < 0;
            return String.format("%s Min %s %s", Math.abs(offset), before ? "Before" : "After", riseSet);
        }
    }

    public static String formatSunriseSunsetShort(@NonNull TimeOfDay timeOfDay) {
        String riseSet = WordUtils.capitalize(timeOfDay.getSunriseSunset().name().toLowerCase());
        int offset = timeOfDay.getOffset();
        if (offset == 0) {
            return String.format("At %s", riseSet);
        }
        else {
            boolean before = offset < 0;
            return String.format("%s Min %s", Math.abs(offset), before ? "Before" : "After");
        }
    }

    public static String format(Date then, Date now) {
        return format(then, now, DFLT_FMT_TABLE);
    }

    public static String format(Date then, Date now, Map<Recency, String> formatTable) {
        Recency recency = getRecency(then, now);
        String format = formatTable.get(recency);
        if(format == null) {
            return "";
        }

        return String.format(format, then);
    }

}
