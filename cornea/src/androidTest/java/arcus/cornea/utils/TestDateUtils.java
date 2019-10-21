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

import org.junit.Test;

import java.util.Calendar;
import java.util.Date;

import static junit.framework.Assert.assertEquals;


public class TestDateUtils {

    @Test
    public void testToday() {
        Date now = today(11);
        Date then = today(10);

        String format = DateUtils.format(then, now);
        assertEquals("Today at 10:00 AM", format);
    }

    @Test
    public void testYesterday() {
        Date now = today(11);
        Date then = yesterday(13);

        String format = DateUtils.format(then, now);
        assertEquals("Yesterday at 1:00 PM", format);
    }

    @Test
    public void testEarlierThisWeek() {
        Date now = today(11);
        Date then = day(-3, 14);

        String format = DateUtils.format(then, now);
        assertEquals("Mon at 2:00 PM", format);
    }

    @Test
    public void testOlderThanThisWeek() {
        Date now = today(11);
        Date then = day(-7, 8);

        String format = DateUtils.format(then, now);
        assertEquals("Aug 20 at 8:00 AM", format);
    }

    private Date today(int hour) {
        return day(0, hour);
    }

    private Date yesterday(int hour) {
        return day(-1, hour);
    }

    private Date day(int dayDelta, int hour) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(1440690272969L);
        c.set(Calendar.DAY_OF_YEAR, c.get(Calendar.DAY_OF_YEAR) + dayDelta);
        c.set(Calendar.HOUR_OF_DAY, hour);
        c.set(Calendar.MINUTE, 0);
        return c.getTime();
    }
}
