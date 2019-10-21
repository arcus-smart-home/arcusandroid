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

import arcus.cornea.subsystem.care.model.ActivityLine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ActivityDataGenerator {
    private static List<Long> generateFakeActivityData() {
        long localStartTime = System.currentTimeMillis();
        List<Long> passIt = new ArrayList<>(111);
        List<Integer> used = new ArrayList<>(111);
        for (int i = 1; i <= 10; i++) {
            localStartTime -= 60 * 1000;
            passIt.add(localStartTime);
        }

        localStartTime -= TimeUnit.MINUTES.toMillis(15);
        Random random = new Random();
        FOR_LOOP: for (int i = 1; i <= 100; i++) {
            int nextEventTS = random.nextInt(86400 * 1000);
            int littleI = 0;
            while (used.contains(nextEventTS)) {
                nextEventTS = random.nextInt(86400 * 1000);
                if (++littleI > 100) {
                    break FOR_LOOP;
                }
            }
            passIt.add(localStartTime - nextEventTS);
            used.add(nextEventTS);
        }

        return passIt;
    }

    public static List<ActivityLine> generateFakeActivityIntervals() {
        List<Long> activityEvents = generateFakeActivityData();
        Collections.sort(activityEvents);

        List<ActivityLine> activityIntervals = new ArrayList<>(110);
        for (Long event : activityEvents) {
            // Normally parse the activity intervals to determine the type of device.
            ActivityLine line = new ActivityLine();
            line.setEventTime(event);

            activityIntervals.add(line);
        }

        return activityIntervals;
    }
}
