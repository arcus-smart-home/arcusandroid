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
package arcus.cornea.subsystem.climate;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import arcus.cornea.utils.DayOfWeek;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ScheduleUtils {
    public static String generateRepeatsText(Set<DayOfWeek> days) {
        if(DayOfWeek.ALLDAYS.equals(days)) {
            return "Every Day";
        } else if(DayOfWeek.WEEKDAYS.equals(days)) {
            return "Weekdays";
        } else if(DayOfWeek.WEEKENDS.equals(days)) {
            return "Weekends";
        } else if(days.size() <= 1) {
            return "Never";
        } else {
            return joinDays(days);
        }
    }

    public static String joinDays(Set<DayOfWeek> days) {
        List<DayOfWeek> toSort = new ArrayList<>(days);
        Collections.sort(toSort);
        List<String> formatted = Lists.newArrayList(Iterables.transform(toSort, new Function<DayOfWeek, String>() {
            @Override
            public String apply(DayOfWeek dayOfWeek) {
                return StringUtils.capitalize(StringUtils.lowerCase(dayOfWeek.name()));
            }
        }));
        return StringUtils.join(formatted, ",");
    }

}
