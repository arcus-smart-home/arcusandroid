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

public enum DayTime {
    DAYTIME,
    NIGHTTIME,
    SUNRISE,
    SUNSET;

    private static final TimeOfDay MORNING = new TimeOfDay(6);
    private static final TimeOfDay EVENING = new TimeOfDay(17,59,59);

    public static DayTime from(TimeOfDay timeOfDay) {
        switch (timeOfDay.getSunriseSunset()) {
            case SUNRISE:
                return DayTime.SUNRISE;
            case SUNSET:
                return DayTime.SUNSET;

            default:
            case ABSOLUTE:
                if(timeOfDay.isBefore(MORNING) || timeOfDay.isAfter(EVENING)) {
                    return NIGHTTIME;
                }
                else {
                    return DAYTIME;
                }
        }
    }

    public static DayTime fromHour(int hour) {
        return from(new TimeOfDay(hour));
    }
}
