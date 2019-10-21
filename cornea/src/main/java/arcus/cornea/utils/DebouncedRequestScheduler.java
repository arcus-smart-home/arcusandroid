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

import com.google.common.base.Strings;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

public class DebouncedRequestScheduler {
    private final long debounceDelayMilliSeconds;
    private final Timer timer;
    private final ConcurrentHashMap<String, TimerTask> taskMap;

    public DebouncedRequestScheduler(long debounceDelayMilliSeconds) {
        this(debounceDelayMilliSeconds, null);
    }

    public DebouncedRequestScheduler(long debounceDelayMilliSeconds, String timerName) {
        if (Strings.isNullOrEmpty(timerName)) {
            timer = new Timer();
        }
        else {
            timer = new Timer(timerName);
        }
        taskMap = new ConcurrentHashMap<>();
        this.debounceDelayMilliSeconds = debounceDelayMilliSeconds;
    }

    public void schedule(String taskIdentifier, TimerTask task) {
        TimerTask existingTask = taskMap.get(taskIdentifier);

        taskMap.put(taskIdentifier, task);
        if (existingTask != null) {
            existingTask.cancel();
            timer.purge(); // Clear existing cancelled requests.
        }

        timer.schedule(task, debounceDelayMilliSeconds);
    }
}
