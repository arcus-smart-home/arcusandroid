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
package arcus.cornea.model;

import androidx.annotation.NonNull;

import arcus.cornea.events.TimeSelectedEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class RuleConversionUtils {
    private static final Logger logger = LoggerFactory.getLogger(RuleConversionUtils.class);
    private static final SimpleDateFormat parseFormat = new SimpleDateFormat("H:mm:ss", Locale.getDefault());
    private static final SimpleDateFormat displayFormat = new SimpleDateFormat("h:mm a" , Locale.getDefault());

    public String convertStringToDisplayTime(Object stringTime) {
        if (stringTime == null) {
            logger.debug("Supplied argument to convertStringToDisplayTime was null, returning null as well.");
            return null;
        }

        String value = String.valueOf(stringTime);
        if (value.equals("00:00:00 - 23:59:59")) {
            return "all day";
        }

        String[] parts = value.split(" - ");
        if (parts.length == 2) {
            try {
                parts[0] = displayFormat.format(parseFormat.parse(parts[0]));
                parts[1] = displayFormat.format(parseFormat.parse(parts[1]));
                value = parts[0] + " and " + parts[1];
            }
            catch (Exception ex) {
                logger.debug("Exception trying to parse time string [{}]", stringTime, ex);
            }
        }

        return value;
    }

    public String convertStringToContextSaveTime(TimeSelectedEvent event, Object startingValue, SelectorType type) {
        if (event.isAllDayEvent()) {
            return "00:00:00 - 23:59:59";
        }

        if (type.equals(SelectorType.TIME_RANGE)) {
            if (startingValue == null) {
                // No time was set, update with start and end being the same
                return event.getAsTime() + " - " + event.getAsTime();
            }

            String start;
            String end;
            String currentValue = String.valueOf(startingValue);
            // Convert time range to appropriate start/end values
            if (event.getPickerType().equals(TimeSelectedEvent.TimePickerType.START)) {
                start = event.getAsTime();
                end = currentValue.substring(currentValue.indexOf("-") + 1, currentValue.length()).trim();
            }
            else {
                start = currentValue.substring(0, currentValue.indexOf("-") - 1).trim();
                end = event.getAsTime();
            }

            try {
                Date startDate = parseFormat.parse(start);
                Date endDate = parseFormat.parse(end);

                if (endDate.before(startDate)) {
                    end = "23:59:59";
                }
            } catch (ParseException e) {
                throw new IllegalArgumentException("Bug! Failed to parse time range.");
            }

            return start + " - " + end;
        }
        else {
            return event.getAsTime();
        }
    }

    public String getDurationStringForDisplay(Object value) {
        try {
            Long time = Long.valueOf(String.valueOf(value));
            Long hours = TimeUnit.SECONDS.toHours(time);
            Long minutes = TimeUnit.SECONDS.toMinutes(time) % 60;
            return getDurationFromHourMinute(hours, minutes);
        }
        catch (Exception ex) {
            logger.debug("Unable to convert to user-displayable string Value -> [{}] Reason.", value, ex);
            return null;
        }
    }

    public String getDurationFromHourMinute(String hourString, String minuteString) {
        try {
            return getDurationFromHourMinute(Long.valueOf(hourString), Long.valueOf(minuteString));
        }
        catch (Exception ex) {
            logger.debug("Error converting fields from strings -> long. [{}], [{}]", hourString, minuteString, ex);
            return null;
        }
    }

    public String getDurationForInfinity(String time) {
        try {
            return DecimalFormatSymbols.getInstance().getInfinity();
        }
        catch (Exception ex) {
            logger.debug("Caught ex trying to return infinity symbol.");
            return time;
        }
    }

    private String getDurationFromHourMinute(Long hours, Long minutes) {
        StringBuilder builder = new StringBuilder("");
        if (hours != 0) {
            builder.append(String.format((hours == 1) ? "%d hr and " : "%d hrs and ", hours));
        }

        builder.append(String.format(((minutes == 1) ? "%d min" : "%d mins"), minutes));
        return builder.toString();
    }

    /**
     *
     * Convert an button={options=[[Mock Button, DRIV:dev:5d0c58d6-29dc-4037-8435-bb8b797557a8]]} into
     * a map string, string that can be used by the UI's.
     *
     * @param data options list
     * @return
     */
    @NonNull
    public LinkedHashMap<String, String> convertOptionsMap(List<List<String>> data) {
        LinkedHashMap<String, String> returnValue = new LinkedHashMap<>();

        for (List<String> fromData : data) {
            try {
                returnValue.put(fromData.get(1), fromData.get(0));
            }
            catch (Exception ex) {
                logger.debug("Skipping over [{}] due to exception.", fromData, ex);
            }
        }

        return returnValue;
    }
}
