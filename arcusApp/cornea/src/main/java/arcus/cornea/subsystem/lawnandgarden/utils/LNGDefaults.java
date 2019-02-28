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
package arcus.cornea.subsystem.lawnandgarden.utils;

import arcus.cornea.model.StringPair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class LNGDefaults {
    private LNGDefaults() {
        //no instance
    }

    // From PM Page: https://eyeris.atlassian.net/wiki/pages/viewpage.action?pageId=26149246
    // Parts referencing "dropdown value for minutes you can water per zone in v1"
    public static int[] getWateringTimes() {
        return new int[]{
                1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 20, 25, 30, 45, 60, 120, 180, 240
        };
    }

    public static List<StringPair> wateringTimeOptions() {
        List<StringPair> tupleValues = new ArrayList<>(24);
        for (int current : getWateringTimes()) {
            int hours = (int) TimeUnit.MINUTES.toHours(current);
            if (hours != 0) {
                String format = "%s hour" + (hours == 1 ? "" : "s");
                tupleValues.add(new StringPair(String.valueOf(current), String.format(format, hours)));
            }
            else {
                String format = "%s minute" + (current == 1 ? "" : "s");
                tupleValues.add(new StringPair(String.valueOf(current), String.format(format, current)));
            }
        }
        return tupleValues;
    }
}
