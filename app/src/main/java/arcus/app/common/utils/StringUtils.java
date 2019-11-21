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
package arcus.app.common.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;

import arcus.cornea.utils.DayOfWeek;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.device.settings.core.Abstractable;

import org.apache.commons.lang3.text.WordUtils;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;


public class StringUtils extends org.apache.commons.lang3.StringUtils {
    public static final String EMPTY_STRING = "";

    public static String defaultToString (@Nullable Object o, String defaultValue) {
        if (o == null || o.toString() == null) {
            return defaultValue;
        }

        return o.toString();
    }

    public static boolean isEmpty(@Nullable Object object) {
        return object == null || object.toString().equals("");
    }

    public static String sanitize(@NonNull String deviceType) {
        // Remove all non-alphanumeric characters and set to lowercase
        return deviceType.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    public static String getAbstract (Context context, Object object) {
        if (object instanceof Abstractable) {
            return ((Abstractable) object).getAbstract(context);
        }

        return object.toString();
    }

    public static Set<String> setToStringSet (Set set) {
        Set<String> stringSet = new HashSet<>();
        for (Object thisObject : set.toArray()) {
            stringSet.add(thisObject.toString());
        }

        return stringSet;
    }

    /**
     * helper method to get a superscripted string
     * @param normal string displayed in its normal position
     * @param sup string displayed on the top right
     * @return a SpannableString accepted by a textview
     */
    public static SpannableString getSuperscriptSpan(@NonNull final String normal, @NonNull final String sup){
        SpannableStringBuilder cs = new SpannableStringBuilder(normal + sup);
        cs.setSpan(new SuperscriptSpan(), normal.length(), normal.length() + sup.length(), 0);
        cs.setSpan(new RelativeSizeSpan(0.5f), normal.length(), normal.length() + sup.length(), 0);
        return SpannableString.valueOf(cs);
    }

    public static SpannableString getSuperscriptSpan(@NonNull final String normal, @NonNull final String sup, final int superScriptColor) {
        SpannableStringBuilder cs = new SpannableStringBuilder(normal + sup);
        cs.setSpan(new SuperscriptSpan(), normal.length(), normal.length() + sup.length(), 0);
        cs.setSpan(new RelativeSizeSpan(0.5f), normal.length(), normal.length() + sup.length(), 0);
        cs.setSpan(new ForegroundColorSpan(superScriptColor), normal.length(), normal.length() + sup.length(), 0);
        return SpannableString.valueOf(cs);
    }

    /**
     * helper method to get prefix superscripted
     * @param sup string displayed on the top left
     * @param normal string displayed in its normal position
     * @return a SpannableString accepted by a textview
     */
    public static SpannableString getPrefixSuperscriptSpan(@NonNull final String sup, @NonNull final String normal){
        SpannableStringBuilder cs = new SpannableStringBuilder(sup + normal);
        cs.setSpan(new SuperscriptSpan(), 0, sup.length(), 0);
        cs.setSpan(new RelativeSizeSpan(0.5f), 0, sup.length(), 0);
        return SpannableString.valueOf(cs);
    }

    public static SpannableString applyColorSpan(@NonNull String normal, @NonNull String applyTo, @ColorInt final int color) {
        SpannableStringBuilder s = new SpannableStringBuilder(normal + applyTo);
        s.setSpan(new ForegroundColorSpan(color), normal.length(), normal.length() + applyTo.length(), 0);
        return SpannableString.valueOf(s);
    }

    public static long howManyDaysBetween(@NonNull Date now, @NonNull Date date){
        return (now.getTime() - date.getTime())/1000/60/60/24;
    }

    public static boolean isDateYesterday (@NonNull Date date) {
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        Calendar then = Calendar.getInstance();
        then.setTime(date);

        return yesterday.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR) &&
                yesterday.get(Calendar.YEAR) == then.get(Calendar.YEAR);
    }

    public static boolean isDateToday (@NonNull Date date) {
        Calendar now = Calendar.getInstance();
        Calendar then = Calendar.getInstance();
        then.setTime(date);

        return now.get(Calendar.DAY_OF_YEAR) == then.get(Calendar.DAY_OF_YEAR) &&
                now.get(Calendar.YEAR) == then.get(Calendar.YEAR);
    }

    /**
     * Converts a byte count into a GB-relative, human readable format following the specifications
     * provided by Robbie Falls. This is not a "normal" conversion and intentionally produces
     * mathematically incorrect results.
     *
     * 0 returns "0 GB", all other values are converted to GB (using 1024 per K) and displayed
     * with one optional decimal place that is always rounded up. Thus, an input of 1 (byte) returns
     * "0.1 GB" but 1073741824 returns "1 GB"
     *
     * @param bytes The number of bytes
     * @return A human readable conversion of bytes to GB.
     */
    @SuppressLint("DefaultLocale")
    public static String getQuoteSizeString(long bytes) {
        double bytesPerGb = 1024 /*kb*/ * 1024 /*mb*/ * 1024;

        if (bytes == 0) {
            return "0 GB";
        }

        double gbs = (double) bytes / bytesPerGb;
        gbs = Math.ceil(gbs / .1) * .1;     // Round up to nearest tenth

        DecimalFormat df = new DecimalFormat("#.#");
        return df.format(gbs) + " GB";
    }

    /**
     * Converts the date into an standard, relative datestamp string. This is similar to
     * {@link #getTimestampString(Date)}, but never renders the time in the resultant String.
     *
     * If the date is null, returns "UNKNOWN"
     * If the date is today, returns "Today"
     * If the date is yesterday, returns "Yesterday"
     * If the date is in the past, returns the date in "Wed, Apr 23" format
     *
     * @param dateValue
     * @return
     */
    public static String getDatestampString (Date dateValue) {
        DateFormat dateFormat = new SimpleDateFormat("EEE, MMM d", Locale.US);

        if (dateValue == null) {
            return ArcusApplication.getContext().getString(R.string.unknown_time_value);
        }

        // Date is today; just show the time
        else if (StringUtils.isDateToday(dateValue)) {
            return ArcusApplication.getContext().getString(R.string.today);
        }

        // Date is yesterday; show "YESTERDAY"
        else if (StringUtils.isDateYesterday(dateValue)) {
            return ArcusApplication.getContext().getString(R.string.yesterday);
        }

        // Date is in the past; show date
        else {
            return dateFormat.format(dateValue);
        }
    }

    /**
     * Converts a date into an standard, relative timestamp string.
     *
     * If the date is null, returns "UNKNOWN"
     * If the date is today, returns the time in "11:30 am" format
     * If the date is yesterday, returns "Yesterday"
     * If the date is in the past, returns the date in "Apr 23, 2016" format
     *
     * @param dateValue
     * @return
     */
    public static String getTimestampString (Date dateValue) {
        DateFormat timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
        DateFormat dateTimeFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);

        if (dateValue == null) {
            return ArcusApplication.getContext().getString(R.string.unknown_time_value);
        }

        // Date is today; just show the time
        else if (StringUtils.isDateToday(dateValue)) {
            return timeFormat.format(dateValue);
        }

        // Date is yesterday; show "YESTERDAY"
        else if (StringUtils.isDateYesterday(dateValue)) {
            return ArcusApplication.getContext().getString(R.string.yesterday);
        }

        // Date is in the past; show date and time
        else {
            return dateTimeFormat.format(dateValue).toUpperCase();
        }
    }

    public static String getScheduleAbstract (Context context, Set<DayOfWeek> allDaysWithEvents) {

        StringBuilder builder = new StringBuilder();
        if (allDaysWithEvents.contains(DayOfWeek.MONDAY)) builder.append("Mo, ");
        if (allDaysWithEvents.contains(DayOfWeek.TUESDAY)) builder.append("Tu, ");
        if (allDaysWithEvents.contains(DayOfWeek.WEDNESDAY)) builder.append("We, ");
        if (allDaysWithEvents.contains(DayOfWeek.THURSDAY)) builder.append("Th, ");
        if (allDaysWithEvents.contains(DayOfWeek.FRIDAY)) builder.append("Fr, ");
        if (allDaysWithEvents.contains(DayOfWeek.SATURDAY)) builder.append("Sa, ");
        if (allDaysWithEvents.contains(DayOfWeek.SUNDAY)) builder.append("Su, ");

        String scheduledDays = builder.toString();
        if (scheduledDays.isEmpty()) return context.getString(R.string.scene_none);
        if (scheduledDays.equals("Mo, Tu, We, Th, Fr, ")) return context.getString(R.string.scene_weekdays);
        if (scheduledDays.equals("Sa, Su, ")) return context.getString(R.string.scene_weekends);
        if (scheduledDays.equals("Mo, Tu, We, Th, Fr, Sa, Su, ")) return context.getString(R.string.scene_everyday);

        return scheduledDays.substring(0, scheduledDays.length() - 2);
    }

    public static int[] parseVersionString (String versionString) {
        if (isEmpty(versionString)) {
            return new int[0];
        }

        String[] versionComponents = versionString.split("\\.");
        int[] parsed = new int[versionComponents.length];

        for (int index = 0; index < versionComponents.length; index++) {
            try {
                parsed[index] = Integer.parseInt(versionComponents[index]);
            } catch (NumberFormatException nfe) {
                parsed[index] = 0;
            }
        }

        return parsed;
    }

    public static SpannableString getDashboardDateString (Date dateValue) {
        DateFormat timeFormat = new SimpleDateFormat("h:mm", Locale.US);
        DateFormat ampmFormat = new SimpleDateFormat(" a", Locale.US);
        DateFormat dateFormat = new SimpleDateFormat("MMM d", Locale.US);

        if (dateValue == null) {
            return new SpannableString("");
        }

        // Date is today; just show the time
        else if (StringUtils.isDateToday(dateValue)) {

            return StringUtils.getSuperscriptSpan(timeFormat.format(dateValue), ampmFormat.format(dateValue));
        }

        // Date is yesterday; show "YESTERDAY"
        else if (StringUtils.isDateYesterday(dateValue)) {
            return new SpannableString(ArcusApplication.getContext().getString(R.string.yesterday));
        }

        // Date is in the past; show date
        else {
            return new SpannableString(dateFormat.format(dateValue));
        }
    }

    public static SpannableString getDateStringDayAndTime (Date dateValue) {
        DateFormat timeFormat = new SimpleDateFormat("  h:mm", Locale.US);
        DateFormat ampmFormat = new SimpleDateFormat(" a", Locale.US);
        DateFormat dateFormat = new SimpleDateFormat("EEE", Locale.US);
        DateFormat dateFormatWithTime = new SimpleDateFormat("    h:mm a", Locale.US);

        if (dateValue == null) {
            return new SpannableString(ArcusApplication.getContext().getString(R.string.unknown_time_value));
        }

        // Date is today; just show the time
        else if (StringUtils.isDateToday(dateValue)) {
            return StringUtils.getSuperscriptSpan(ArcusApplication.getContext().getString(R.string.today), dateFormatWithTime.format(dateValue));
        }

        // Date is yesterday; show "YESTERDAY"
        else if (StringUtils.isDateYesterday(dateValue)) {
            return new SpannableString(ArcusApplication.getContext().getString(R.string.yesterday));
        }

        // Date is in the past; show date
        else {
            return StringUtils.getSuperscriptSpan(WordUtils.capitalize(dateFormat.format(dateValue)), dateFormatWithTime.format(dateValue));
        }
    }

    public static String getDurationString(int duration) {
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;

        String h = hours > 0 ? hours + "h " : "";
        String m = minutes > 0 ? minutes + "m " : "";
        String s = seconds > 0 ? seconds + "s" : "";

        return String.format("%s%s%s", h, m, s).trim();
    }

    public static String getFirstUpperRestLowerCaseString(@NonNull String initialString) {
        return initialString.substring(0, 1).toUpperCase() + initialString.substring(1).toLowerCase();
    }

}
