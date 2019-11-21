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

import com.google.common.base.Preconditions;
import com.iris.client.bean.TimeOfDayCommand;

import java.util.Date;

public enum SunriseSunset {
    SUNRISE,
    SUNSET,
    ABSOLUTE;

    public static SunriseSunset fromString(@Nullable String mode) {
        if (TextUtils.isEmpty(mode)) {
            return ABSOLUTE;
        }

        try {
            return valueOf(mode);
        }
        catch (Exception ex) {
            return ABSOLUTE;
        }
    }

    public static SunriseSunset fromTimeOfDayCommand(@NonNull TimeOfDayCommand command) {
        Preconditions.checkNotNull(command);

        if (TimeOfDayCommand.MODE_SUNRISE.equals(command.getMode())) {
            return SunriseSunset.SUNRISE;
        }
        else if (TimeOfDayCommand.MODE_SUNSET.equals(command.getMode())) {
            return SunriseSunset.SUNSET;
        }

        return SunriseSunset.ABSOLUTE;
    }

    public static String getNextEventForSunriseSunset(Date nextFireTime, TimeOfDayCommand timeOfDayCommand) {
        String beforeAfter = "";
        String nextStartTime = "";

        String nextFireTimeStr = DateUtils.format(nextFireTime);
        String[] splitStr = nextFireTimeStr.split("\\s+");

        if(splitStr.length > 1) {
            nextStartTime = splitStr[0] + " " +splitStr[1] + " ";
        }
        if(timeOfDayCommand.getOffsetMinutes() < 0) {
            beforeAfter = Math.abs(timeOfDayCommand.getOffsetMinutes())+" min Before ";
        }
        else if(timeOfDayCommand.getOffsetMinutes() > 0) {
            beforeAfter = timeOfDayCommand.getOffsetMinutes()+" min After ";
        }
        if(TimeOfDayCommand.MODE_SUNRISE.equals(timeOfDayCommand.getMode())) {
            nextStartTime += beforeAfter+TimeOfDayCommand.MODE_SUNRISE;
        }
        else if(TimeOfDayCommand.MODE_SUNSET.equals(timeOfDayCommand.getMode())) {
            nextStartTime += beforeAfter+TimeOfDayCommand.MODE_SUNSET;
        }

        return nextStartTime;
    }
}
