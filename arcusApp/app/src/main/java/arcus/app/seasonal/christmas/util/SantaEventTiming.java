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
package arcus.app.seasonal.christmas.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class SantaEventTiming {

    private static final SantaEventTiming INSTANCE = new SantaEventTiming();

    public static SantaEventTiming instance() {
        return INSTANCE;
    }

    /**
     * @return Time (in milliseconds since the epoch) that Santa will visit this year.
     */
    private long getSantaVisitTimeMillis() {
        return new GregorianCalendar(getSantaSeasonYear(), GregorianCalendar.DECEMBER, 25, 0, 25, 25).getTimeInMillis();
    }

    /**
     * @return Time (in milliseconds since the epoch) that the Santa Tracker will become available to the user.
     */
    private long getSantaSeasonOpeningMillis() {
        return new GregorianCalendar(getSantaSeasonYear(), GregorianCalendar.NOVEMBER, 24,  0,  0,  0).getTimeInMillis();
    }

    /**
     * @return Time (in milliseconds since the epoch) that the Santa Tracker will disappear from the app.
     */
    private long getSantaSeasonClosingMillis() {
        return new GregorianCalendar(getSantaSeasonYear() + 1, GregorianCalendar.JANUARY, 7, 23, 59, 59).getTimeInMillis();
    }

    /**
     * @return Time at which the "remember to configure santa tracker" notification will be generated.
     */
    public GregorianCalendar getNotConfiguredNotificationTime() {
        return new GregorianCalendar(getSantaSeasonYear(), GregorianCalendar.DECEMBER, 24, 17, 0);
    }

    /**
     * @return Time at which the "check the app for a photo of santa" notification will be generated.
     */
    public GregorianCalendar getSantaArrivedNotificationTime() {
        return new GregorianCalendar(getSantaSeasonYear(), GregorianCalendar.DECEMBER, 25, 11, 0);
    }

    public boolean hasSantaVisited() {
        return System.currentTimeMillis() >= getSantaVisitTimeMillis();
    }

    public boolean isSantaSeason() {
        return (System.currentTimeMillis() >= getSantaSeasonOpeningMillis()) && (System.currentTimeMillis() <= getSantaSeasonClosingMillis());
    }

    /**
     *
     * Gets the current time if the event hasn't begun or the time of santa's potential arrival if the event has begun.
     *
     * @return
     */
    public long getCurrentOrEventTime() {
        return System.currentTimeMillis() >= getSantaVisitTimeMillis() ? getSantaVisitTimeMillis() : System.currentTimeMillis();
    }

    public Date getEventDate() {
        return new Date(getCurrentOrEventTime());
    }

    /**
     * Gets the calendar year of this Christmas season. Typically the current year as returned by Calendar,
     * but returns the prior year during January.
     * @return
     */
    @SuppressWarnings("WrongConstant")
    public int getSantaSeasonYear() {
        Calendar santaYear = Calendar.getInstance();

        if (santaYear.get(Calendar.MONTH) == Calendar.JANUARY) {
            santaYear.add(Calendar.YEAR, -1);
        }

        return santaYear.get(Calendar.YEAR);
    }

    public String getFormattedEventTime() {
        return new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault()).format(new Date(getCurrentOrEventTime()));
    }
}
