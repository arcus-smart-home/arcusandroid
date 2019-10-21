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
package arcus.app.subsystems.care.util;

import android.text.TextUtils;

import arcus.cornea.subsystem.care.model.CareBehaviorModel;
import arcus.cornea.subsystem.care.model.TimeWindowModel;
import arcus.cornea.utils.DayOfWeek;
import arcus.app.common.models.ListItemModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CareUtilities {
    public enum Sort {
        ASC,
        DSC
    }

    public static Comparator<ListItemModel> listItemModelComparatorByName(Sort sort) {
        if (Sort.ASC.equals(sort)) {
            return new Comparator<ListItemModel>() {
                @Override
                public int compare(ListItemModel lhs, ListItemModel rhs) {
                    if (rhs == null || TextUtils.isEmpty(rhs.getText())) {
                        return lhs == null || TextUtils.isEmpty(lhs.getText()) ? 0 : -1;
                    }
                    if (lhs == null || TextUtils.isEmpty(lhs.getText())) {
                        return 1;
                    }
                    return rhs.getText().compareToIgnoreCase(lhs.getText());
                }
            };
        }
        else {
            return new Comparator<ListItemModel>() {
                @Override
                public int compare(ListItemModel lhs, ListItemModel rhs) {
                    if (lhs == null || TextUtils.isEmpty(lhs.getText())) {
                        return rhs == null || TextUtils.isEmpty(rhs.getText()) ? 0 : -1;
                    }
                    if (rhs == null || TextUtils.isEmpty(rhs.getText())) {
                        return 1;
                    }
                    return lhs.getText().compareToIgnoreCase(rhs.getText());
                }
            };
        }
    }

    public static Map<DayOfWeek, List<TimeWindowModel>> getSchedulesFor(CareBehaviorModel careBehaviorModel) {
        if (careBehaviorModel == null) {
            return Collections.emptyMap();
        }

        List<TimeWindowModel> windows = careBehaviorModel.getTimeWindows();
        if (windows == null || windows.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<DayOfWeek, List<TimeWindowModel>> daysMap = new HashMap<>(7);
        DayOfWeek[] selectableDays = DayOfWeek.values();
        for (TimeWindowModel timeWindow : windows) {
            DayOfWeek startDay = timeWindow.getDay();
            DayOfWeek endDay   = timeWindow.getEndDay();

            if (startDay.ordinal() < endDay.ordinal()) { // If start < end it is -> same week no wraparound.
                for (int i = startDay.ordinal(); i <= endDay.ordinal(); i++) {
                    List<TimeWindowModel> currentList = daysMap.get(selectableDays[i]);
                    if (currentList == null) {
                        currentList = new ArrayList<>();
                        daysMap.put(selectableDays[i], currentList);
                    }

                    currentList.add(timeWindow);
                }
            }
            else if (startDay.equals(endDay)) { // Same Day....
                List<TimeWindowModel> currentList = daysMap.get(startDay);
                if (currentList == null) {
                    currentList = new ArrayList<>();
                    daysMap.put(startDay, currentList);
                }

                currentList.add(timeWindow);
            }
            else { // we are going Thurs -> Mon, or Fri->Tues or Sunday -> Monday
                for (int i = startDay.ordinal(); i < selectableDays.length; i++) {
                    List<TimeWindowModel> currentList = daysMap.get(selectableDays[i]);
                    if (currentList == null) {
                        currentList = new ArrayList<>();
                        daysMap.put(selectableDays[i], currentList);
                    }

                    currentList.add(timeWindow);
                }
                for (int i = 0; i <= endDay.ordinal(); i++) {
                    List<TimeWindowModel> currentList = daysMap.get(selectableDays[i]);
                    if (currentList == null) {
                        currentList = new ArrayList<>();
                        daysMap.put(selectableDays[i], currentList);
                    }

                    currentList.add(timeWindow);
                }
            }
        }

        Comparator<TimeWindowModel> timeWindowModelComparator = new Comparator<TimeWindowModel>() {
            @Override
            public int compare(TimeWindowModel lhs, TimeWindowModel rhs) {
                return lhs.getStartValue() < rhs.getStartValue() ? -1 : (lhs.getStartValue() == rhs.getStartValue() ? 0 : 1);
            }
        };


        for (DayOfWeek day : DayOfWeek.values()) {
            List<TimeWindowModel> daySchedules = daysMap.get(day);
            if (daySchedules != null) {
                Collections.sort(daySchedules, timeWindowModelComparator);
            }
        }

        return daysMap;
    }

}
