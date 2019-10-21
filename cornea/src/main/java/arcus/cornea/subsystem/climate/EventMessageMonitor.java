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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class EventMessageMonitor {
    private static long DEVICE_TIMEOUT = 120000;
    //map of device ids and map of attributes and time
    HashMap<String, HashMap<String, Long>> inFlightRequests = new HashMap<>();
    private static EventMessageMonitor instance = null;
    protected EventMessageMonitor() {
        // Exists only to defeat instantiation.
    }
    public static EventMessageMonitor getInstance() {
        if(instance == null) {
            instance = new EventMessageMonitor();
        }
        return instance;
    }

    public void scheduleEvent(String deviceId, String attribute) {
        if(!inFlightRequests.containsKey(deviceId) || inFlightRequests.get(deviceId) == null) {
            HashMap<String, Long> tempMap = new HashMap<>();
            tempMap.put(attribute, System.currentTimeMillis());
            inFlightRequests.put(deviceId, tempMap);
        }
        else {
            HashMap<String, Long> tempMap = inFlightRequests.get(deviceId);
            tempMap.put(attribute, System.currentTimeMillis());
            inFlightRequests.put(deviceId, tempMap);
        }
    }

    public void removeScheduledEvent(String deviceId, String attribute) {
        if(inFlightRequests != null && inFlightRequests.get(deviceId) != null) {
            inFlightRequests.get(deviceId).remove(attribute);
        }
    }

    public void removeScheduledEvents(String deviceId) {
        if(inFlightRequests != null && inFlightRequests.get(deviceId) != null) {
            inFlightRequests.get(deviceId).clear();
        }
    }

    public HashMap<String, Long> getScheduleForDevice(String deviceId) {
        if(inFlightRequests == null || inFlightRequests.get(deviceId) == null) {
            return null;
        }
        clearStale(deviceId);
        return inFlightRequests.get(deviceId);
    }

    public void clearStale(String deviceId) {
        if(inFlightRequests == null || inFlightRequests.get(deviceId) == null) {
            return;
        }
        else {
            HashMap<String, Long> tempMap = inFlightRequests.get(deviceId);

            for(Iterator<Map.Entry<String, Long>> it = tempMap.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, Long> entry = it.next();
                if ((entry.getValue()+DEVICE_TIMEOUT) < System.currentTimeMillis()) {
                    it.remove();
                }
            }
        }
    }
}
