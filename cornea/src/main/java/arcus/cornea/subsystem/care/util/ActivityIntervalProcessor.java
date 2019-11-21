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
package arcus.cornea.subsystem.care.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.subsystem.care.model.ActivityLine;
import com.iris.client.bean.ActivityInterval;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.capability.Motion;
import com.iris.client.model.Model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActivityIntervalProcessor {
    private static final Logger logger = LoggerFactory.getLogger(ActivityIntervalProcessor.class);
    private final Map<String, Boolean> sensorMap = new HashMap<>();
    private final Set<String> activeDevices = new HashSet<>();

    private final Comparator<ActivityLine> standardComparison = new Comparator<ActivityLine>() {
        @Override public int compare(ActivityLine lhs, ActivityLine rhs) {
            return Long.valueOf(lhs.getEventTime()).compareTo(rhs.getEventTime());
        }
    };

    public static ActivityIntervalProcessor instance() {
        return new ActivityIntervalProcessor();
    }

    private ActivityIntervalProcessor() {
        //no instance
    }

    public @NonNull List<ActivityLine> parseActivityIntervalResponse(
          @NonNull CareSubsystem.ListActivityResponse response,
          long bucketSizeMillis,
          boolean withFillIn
    ) {
        List<ActivityLine> linesToDraw = new ArrayList<>();
        List<ActivityInterval> intervals = getIntervals(response);
        if (intervals.isEmpty()) {
            return linesToDraw;
        }

        long endTime = System.currentTimeMillis();
        boolean foundMotion;
        for (int i = 0, size = intervals.size(); i < size; i++) {
            // Parse through the list of devices and:
            // Look for contact sensors (these win because they have a different line style)
            // Update the current list of active/inactive devices.
            Map<String, String> devices = intervals.get(i).getDevices();
            long eventTime = intervals.get(i).getStart().getTime();
            foundMotion = hasActiveMotionSensor(devices, linesToDraw, eventTime);

            if (activeDevices.isEmpty()) {
                continue;
            }

            // Else, We need to draw at least one line.
            ActivityLine line = new ActivityLine();
            line.setIsContact(!foundMotion);
            line.setEventTime(eventTime);
            linesToDraw.add(line);

            // If all were deactivated in this bucket, we don't need to draw anything further
            // If we're not supposed to fill in and we haven't found motion, we don't need to draw anything further
            if (!withFillIn && !foundMotion) {
                continue;
            }

            // Now, determine how much we have to fill in.
            long endTimeOfFillIn = endTime;
            if ((i + 1) < size) {
                // If there is another event, we want to stop drawing when we get to that and not the max end time.
                endTimeOfFillIn = intervals.get(i + 1).getStart().getTime();
            }

            // Continue to add "lines we need to draw" until we've reached the next event
            // Or the end of the time range - whatever was set above.
            for (eventTime = eventTime + bucketSizeMillis; eventTime < endTimeOfFillIn; eventTime += bucketSizeMillis) {
                line = new ActivityLine();
                line.setIsContact(!foundMotion); // Let these take precedent so the dashed line gets drawn if desired.
                line.setEventTime(eventTime);
                linesToDraw.add(line);
            }
        }

        logger.debug("Processed {} intervals for a total of {} line(s)", intervals.size(), linesToDraw.size());
        Collections.sort(linesToDraw, standardComparison);

        return linesToDraw;
    }

    protected boolean isMotionDevice(String key) {
        Boolean isMotion = sensorMap.get(key);

        if (isMotion == null) {
            Model model  = CorneaClientFactory.getModelCache().get(key);
            isMotion = model != null && model.getCaps().contains(Motion.NAMESPACE);
            sensorMap.put(key, isMotion);
        }

        return isMotion;
    }

    /**
     * Parses the list of activity intervals returned from the platform and strips any empty blocks out.
     */
    protected List<ActivityInterval> getIntervals(@Nullable CareSubsystem.ListActivityResponse response) {
        if (response == null) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> intervals = response.getIntervals();
        if (intervals == null || intervals.isEmpty()) {
            return Collections.emptyList();
        }

        List<ActivityInterval> activityIntervals = new ArrayList<>(intervals.size());
        for (int i = 0, size = intervals.size(); i < size; i++) {
            ActivityInterval item = new ActivityInterval(intervals.get(i));
            if (item.getDevices() != null && !item.getDevices().isEmpty()) {
                activityIntervals.add(item);
            }
        }

        return activityIntervals; // Should these be sorted when the client gets?
    }

    /**
     * Searches the map of devices for any active devices, returns true if one of those are a motion sensor.
     * Keeps the instance Set&lt;String&gt; up-to date with a current list of "active devices" waiting to be "deactivated"
     */
    protected boolean hasActiveMotionSensor(
          Map<String, String> deviceMap,
          List<ActivityLine> linesToDraw,
          long eventTime
    ) {
        if (deviceMap == null || deviceMap.isEmpty()) {
            return false;
        }

        boolean deactivatedContact = false;
        boolean foundMotion = false;
        for (String key : deviceMap.keySet()) {
            if (ActivityInterval.DEVICES_ACTIVATED.equals(deviceMap.get(key))) {
                if (!foundMotion) {
                    foundMotion = isMotionDevice(key);
                }
                activeDevices.add(key);
            }
            else {
                if (!deactivatedContact) {
                    deactivatedContact = !isMotionDevice(key);
                }
                activeDevices.remove(key);
            }
        }

        // If we've found motion, we're going to continue to draw anyhow, so don't double draw
        if (!foundMotion && deactivatedContact) {
            ActivityLine line = new ActivityLine();
            line.setIsContact(true);
            line.setEventTime(eventTime);
            linesToDraw.add(line);
        }
        return foundMotion;
    }
}
