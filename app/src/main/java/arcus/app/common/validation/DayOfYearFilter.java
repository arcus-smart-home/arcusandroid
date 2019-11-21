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
package arcus.app.common.validation;

import androidx.annotation.NonNull;

import com.iris.client.bean.HistoryLog;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DayOfYearFilter implements Filter<HistoryLog> {
    private Calendar calendar = Calendar.getInstance();
    private int  dayNumber;

    public DayOfYearFilter(long timeStamp) {
        setDayNumber(timeStamp);
    }

    public DayOfYearFilter(int  day) {
        this.dayNumber = day;
    }

    public DayOfYearFilter(@NonNull Date date) {
        setDayNumber(date);
    }

    @Override
    public boolean apply(@NonNull List<HistoryLog> itemsTofilter) {
        // Because API < 19 doesn't have Objects.equals (used by HistoryLog bean)
        boolean modifiedEntries = false;

        for (int i = 0; i < itemsTofilter.size(); ) {
            if (!matches(itemsTofilter.get(i).getTimestamp())) {
                itemsTofilter.remove(i);
                modifiedEntries = true;
            }
            else {
                i++;
            }
        }

        return modifiedEntries;
    }

    public boolean matches(long timestamp) {
        return getDayNumber(timestamp) == dayNumber;
    }

    public boolean matches(@NonNull Date date) {
        return getDayNumber(date) == dayNumber;
    }


    private void setDayNumber(@NonNull Date date) {
        calendar.setTime(date);
        dayNumber = calendar.get(Calendar.DAY_OF_YEAR);
    }

    private void setDayNumber(long timeStamp) {
        calendar.setTime(new Date(timeStamp));
        dayNumber = calendar.get(Calendar.DAY_OF_YEAR);
    }

    private int getDayNumber(@NonNull Date date) {
        calendar.setTime(date);
        return calendar.get(Calendar.DAY_OF_YEAR);
    }

    private int getDayNumber(long timestamp) {
        calendar.setTime(new Date(timestamp));
        return calendar.get(Calendar.DAY_OF_YEAR);
    }
}
