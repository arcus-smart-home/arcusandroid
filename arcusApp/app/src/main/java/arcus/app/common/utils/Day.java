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

import java.util.Calendar;
import java.util.Date;



public class Day extends Date {

    private final static int DAY_MS = 24 * 60 * 60 * 1000;

    private Day (Date timestamp) {
        super(timestamp.getTime());
    }

    public static Day fromDate(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        return new Day(cal.getTime());
    }

    public static Day add(int days, Date day) {
        Calendar initial = Calendar.getInstance();
        initial.setTimeInMillis(day.getTime());
        initial.add(Calendar.DATE, days);
        return Day.fromDate(new Date(initial.getTimeInMillis()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Day)) return false;

        Day day = (Day) o;

        return this != null ? this.getTime() == day.getTime() : day == null;
    }

    @Override
    public int hashCode() {
        return Long.valueOf(getTime()).hashCode();
    }
}
