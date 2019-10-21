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

import java.util.HashMap;


public class EventTimeoutController {
    //map of device ids and map of attributes and time
    HashMap<String, HashMap<String, ThrottledDelayedExecutor>> delayedExecutorMap = new HashMap<>();
    private static EventTimeoutController instance = null;
    private static HashMap<String, EventTimeoutCallback> callbacks = new HashMap<>();

    protected EventTimeoutController() {
        // Exists only to defeat instantiation.
    }
    public static EventTimeoutController getInstance() {
        if(instance == null) {
            instance = new EventTimeoutController();
        }
        return instance;
    }

    public void setTimer(String deviceId, String attributeForTimer, ThrottledDelayedExecutor timer) {
        if(!delayedExecutorMap.containsKey(deviceId) || delayedExecutorMap.get(deviceId) == null) {
            HashMap<String, ThrottledDelayedExecutor> tempMap = new HashMap<>();
            tempMap.put(attributeForTimer, timer);
            delayedExecutorMap.put(deviceId, tempMap);
        }
        else {
            HashMap<String, ThrottledDelayedExecutor> tempMap = delayedExecutorMap.get(deviceId);
            tempMap.put(attributeForTimer, timer);
            delayedExecutorMap.put(deviceId, tempMap);
        }
    }

    public void removeTimer(String deviceId, String attribute) {
        if(delayedExecutorMap != null && delayedExecutorMap.get(deviceId) != null) {
            delayedExecutorMap.get(deviceId).remove(attribute);
        }
    }

    public ThrottledDelayedExecutor getTimer(String deviceId, String attribute) {
        if(delayedExecutorMap == null || delayedExecutorMap.get(deviceId) == null) {
            return null;
        }
        return delayedExecutorMap.get(deviceId).get(attribute);
    }

    public void setCallback(String deviceId, EventTimeoutCallback callback) {
        this.callbacks.put(deviceId, callback);
    }

    public static void timedOut(String deviceId) {
        if(callbacks != null) {
            EventTimeoutCallback callback = callbacks.get(deviceId);
            if(callback != null) {
                callback.timeoutReached();
            }
        }
    }

    public interface EventTimeoutCallback {
        void timeoutReached();
    }
}
