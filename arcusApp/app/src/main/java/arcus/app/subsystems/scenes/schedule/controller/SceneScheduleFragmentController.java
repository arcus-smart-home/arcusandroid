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
package arcus.app.subsystems.scenes.schedule.controller;

import android.content.Context;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.CapabilityUtils;
import arcus.cornea.utils.Listeners;
import com.iris.client.bean.TimeOfDayCommand;
import com.iris.client.capability.Schedule;
import com.iris.client.event.Listener;
import com.iris.client.model.SchedulerModel;
import com.iris.client.service.SchedulerService;
import arcus.app.R;
import arcus.app.common.controller.FragmentController;
import arcus.app.common.utils.GlobalSetting;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class SceneScheduleFragmentController extends FragmentController<SceneScheduleFragmentController.Callbacks> {

    private final static SceneScheduleFragmentController instance = new SceneScheduleFragmentController();

    private String currentAbstract;

    public interface Callbacks {
        void onCorneaError (Throwable cause);
        void onScheduleAbstractLoaded (boolean hasSchedule, boolean isScheduleEnabled, String scheduleAbstract, String sceneAddress);
    }

    private SceneScheduleFragmentController() {}
    public static SceneScheduleFragmentController getInstance () { return instance; }

    public void loadScheduleAbstract (final Context context, final String sceneAddress) {

        CorneaClientFactory.getService(SchedulerService.class).getScheduler(sceneAddress).onSuccess(Listeners.runOnUiThread(new Listener<SchedulerService.GetSchedulerResponse>() {
            @Override
            public void onEvent(SchedulerService.GetSchedulerResponse getSchedulerResponse) {

                SchedulerModel scheduler = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(getSchedulerResponse.getScheduler());
                currentAbstract = getScheduleAbstract(context, scheduler);

                if (scheduler.getCommands() != null && scheduler.getCommands().size() > 0) {
                    fireOnScheduleAbstractLoaded(true, isEnabled(scheduler), currentAbstract , sceneAddress);
                } else {
                    fireOnScheduleAbstractLoaded(false, false, context.getString(R.string.scene_none), sceneAddress);
                }
            }
        })).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    public void setScheduleEnabled (final String sceneAddress, final boolean enabled) {
        CorneaClientFactory.getService(SchedulerService.class).getScheduler(sceneAddress).onSuccess(new Listener<SchedulerService.GetSchedulerResponse>() {
            @Override
            public void onEvent(SchedulerService.GetSchedulerResponse getSchedulerResponse) {
                SchedulerModel scheduler = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(getSchedulerResponse.getScheduler());
                setEnabled(scheduler, enabled);
            }
        }).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    private String getScheduleAbstract (Context context, SchedulerModel scheduler) {

        List<String> allDaysWithEvents = new ArrayList<>();
        for (Map<String, Object> thisCommandData : scheduler.getCommands().values()) {
            TimeOfDayCommand thisCommand = new TimeOfDayCommand(thisCommandData);
            for(String day : thisCommand.getDays()) {
                allDaysWithEvents.add(String.valueOf(day).toUpperCase());
            }
        }

        StringBuilder builder = new StringBuilder();
        if (allDaysWithEvents.contains("MON")) builder.append("M, ");
        if (allDaysWithEvents.contains("TUE")) builder.append("T, ");
        if (allDaysWithEvents.contains("WED")) builder.append("W, ");
        if (allDaysWithEvents.contains("THU")) builder.append("Th, ");
        if (allDaysWithEvents.contains("FRI")) builder.append("F, ");
        if (allDaysWithEvents.contains("SAT")) builder.append("Sat, ");
        if (allDaysWithEvents.contains("SUN")) builder.append("Sun, ");

        String scheduledDays = builder.toString();
        if (scheduledDays.isEmpty()) return context.getString(R.string.scene_none);
        if (scheduledDays.equals("M, T, W, Th, F,")) return context.getString(R.string.scene_weekdays);
        if (scheduledDays.equals("Sat, Sun, ")) return context.getString(R.string.scene_weekends);
        if (scheduledDays.equals("M, T, W, Th, F, Sat, Sun, ")) return context.getString(R.string.scene_everyday);

        return scheduledDays.substring(0, scheduledDays.length() - 2);
    }

    private boolean isEnabled (SchedulerModel scheduler) {
        Object value = new CapabilityUtils(scheduler).getInstanceValue(GlobalSetting.SCENE_SCHEDULER_NAME, Schedule.ATTR_ENABLED);
        return value != null && (Boolean) value;
    }

    private void setEnabled (SchedulerModel scheduler, final boolean isEnabled) {
        new CapabilityUtils(scheduler).setInstance(GlobalSetting.SCENE_SCHEDULER_NAME).attriubuteToValue(Schedule.ATTR_ENABLED, isEnabled).andSendChanges()
                .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                    fireOnCorneaError(throwable);
            }
        }));
    }

    private void fireOnCorneaError (Throwable cause) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onCorneaError(cause);
        }
    }

    private void fireOnScheduleAbstractLoaded (boolean hasSchedule, boolean scheduleEnabled, String scheduleAbstract, String sceneAddress) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onScheduleAbstractLoaded(hasSchedule, scheduleEnabled, scheduleAbstract, sceneAddress);
        }
    }
}

