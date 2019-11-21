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
package arcus.app.subsystems.water.schedule;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.error.Errors;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.climate.ScheduleUtils;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import arcus.cornea.utils.SunriseSunset;
import arcus.cornea.utils.TemperatureUtils;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.bean.TimeOfDayCommand;
import com.iris.client.capability.Capability;
import com.iris.client.capability.WaterHeater;
import com.iris.client.capability.WaterSubsystem;
import com.iris.client.capability.WeeklySchedule;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.SchedulerModel;
import com.iris.client.model.SubsystemModel;
import com.iris.client.service.SchedulerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class WaterHeaterScheduleViewController extends BaseSubsystemController<WaterHeaterScheduleViewController.Callback> {

    public static final String ATTRIBUTES = "attributes";

    public interface Callback {
        void showSchedule(
                @NonNull DayOfWeek selectedDay,
                @NonNull Set<DayOfWeek> daysWithSchedule,
                @NonNull List<WaterScheduleDay> events);

        void onError(@NonNull ErrorModel error);
    }

    //We don't want to call updateView right off the bat and pass back an empty list for no reason
    @Override
    public ListenerRegistration setCallback(WaterHeaterScheduleViewController.Callback callback) {
        if(callbackRef.get() != null) {
            logger.warn("Replacing existing callback");
        }
        callbackRef = new WeakReference<>(callback);
        return Listeners.wrap(callbackRef);
    }

    public  static final String LNS_GROUP_ID = "WATER";
    private static final Logger logger = LoggerFactory.getLogger(WaterHeaterScheduleViewController.class);
    private static final WaterHeaterScheduleViewController INSTANCE;
    static {
        INSTANCE = new WaterHeaterScheduleViewController(
              SubsystemController.instance().getSubsystemModel(WaterSubsystem.NAMESPACE)
        );
        INSTANCE.init();
    }
    private final Listener<SchedulerService.GetSchedulerResponse> schedulerLoadedListener =
          Listeners.runOnUiThread(new Listener<SchedulerService.GetSchedulerResponse>() {
              @Override
              public void onEvent(SchedulerService.GetSchedulerResponse r) {
                  schedulerModel = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(r.getScheduler());
                  buildSchedule();
                  addSchedulerModelListener();
                  updateView();
              }
          });
    private final Listener<PropertyChangeEvent> schedulerUpdatedListener =
          Listeners.runOnUiThread(new Listener<PropertyChangeEvent>() {
              @Override
              public void onEvent(PropertyChangeEvent propertyChangeEvent) {
                  onSchedulerChanged(propertyChangeEvent);
              }
          });

    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onError(throwable);
        }
    });
    private final Comparator<WaterScheduleDay> eventSorter = new Comparator<WaterScheduleDay>() {
        @Override
        public int compare(WaterScheduleDay firstDetail, WaterScheduleDay secondDetail) {
            return firstDetail.getTimeOfDay().compareTo(secondDetail.getTimeOfDay());

        }
    };
    private SchedulerModel schedulerModel;
    private DayOfWeek selectedDay = DayOfWeek.MONDAY;
    private ListenerRegistration schedulerRegistration = Listeners.empty();
    private Map<DayOfWeek,List<WaterScheduleDay>> deviceSchedule;
    private Set<DayOfWeek> daysScheduled;

    public static WaterHeaterScheduleViewController instance() {
        return INSTANCE;
    }

    WaterHeaterScheduleViewController(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
    }

    public ListenerRegistration select(@NonNull String deviceAddress, @NonNull Callback callback, @Nullable DayOfWeek dayOfWeek) {
        reset();
        if (dayOfWeek != null) {
            selectedDay = dayOfWeek;
        }
        loadScheduler(deviceAddress);
        return setCallback(callback);
    }

    private void reset() {
        schedulerRegistration = Listeners.clear(schedulerRegistration);
        schedulerModel = null;
        daysScheduled = new HashSet<>();
        deviceSchedule = new HashMap<>();
        selectedDay = DayOfWeek.MONDAY;
    }

    @Override
    protected void updateView(Callback callback) {
        List<WaterScheduleDay> scheduledDay = deviceSchedule.get(selectedDay);
        if (scheduledDay == null) {
            scheduledDay = new ArrayList<>();
        }
        callback.showSchedule(selectedDay, daysScheduled, scheduledDay);
    }

    private void onError(Throwable t) {
        Callback cb = getCallback();
        if(cb !=  null) {
            cb.onError(Errors.translate(t));
        }
    }

    @Override
    protected boolean isLoaded() {
        return super.isLoaded() && schedulerModel != null;
    }

    @SuppressWarnings({"unchecked"})
    private void buildSchedule() {
        for(DayOfWeek day : DayOfWeek.values()) {
            String key = String.format("%s:%s:%s", WeeklySchedule.NAMESPACE, abbreviateDayOfWeek(day), LNS_GROUP_ID);
            Collection<Map<String,Object>> dayDetails = (Collection<Map<String,Object>>) schedulerModel.get(key);
            parseDaySchedule(day, dayDetails);
        }
        daysScheduled = deviceSchedule.keySet();
    }

    @SuppressWarnings({"unchecked"})
    private void parseDaySchedule(DayOfWeek day, Collection<Map<String,Object>> daySchedule) {
        if (daySchedule == null) {
            return;
        }

        List<WaterScheduleDay> sunriseSunset = new ArrayList<>();
        List<WaterScheduleDay> absolute = new ArrayList<>();
        List<WaterScheduleDay> events = new ArrayList<>();
        for(Map<String,Object> evt : daySchedule) {
            TimeOfDayCommand command = new TimeOfDayCommand(evt);
            String time = command.getTime();

            WaterScheduleDay detail = new WaterScheduleDay();
            detail.setCommandID(command.getId());
            detail.setTimeOfDay(
                  TimeOfDay.fromStringWithMode(
                        command.getTime(),
                        SunriseSunset.fromTimeOfDayCommand(command),
                        command.getOffsetMinutes()
                  )
            );
            detail.setRepeatsOn(parseRepeatsOn(command.getDays()));
            detail.setRepetitionText(ScheduleUtils.generateRepeatsText(detail.getRepeatsOn()));

            Map<String, Object> detailsMap = (Map<String, Object>) evt.get(ATTRIBUTES);
            if (detailsMap != null) {
                Double setPoint = (Double)  detailsMap.get(WaterHeater.ATTR_SETPOINT);
                if (setPoint != null ) {
                    detail.setSetPoint(TemperatureUtils.roundCelsiusToFahrenheit(setPoint));
                    if (SunriseSunset.ABSOLUTE.equals(detail.getTimeOfDay().getSunriseSunset())) {
                        absolute.add(detail);
                    }
                    else {
                        sunriseSunset.add(detail);
                    }
                }
            }
        }

        Collections.sort(sunriseSunset, eventSorter);
        Collections.sort(absolute, eventSorter);
        events.addAll(sunriseSunset);
        events.addAll(absolute);

        if (!events.isEmpty()) {
            deviceSchedule.put(day, events);
        }
    }



    protected HashSet<DayOfWeek> parseRepeatsOn(Collection<String> days) {
        HashSet<DayOfWeek> dayOfWeeks = new HashSet<>();
        if (days == null) {
            return dayOfWeeks;
        }

        for(String d : days) {
            dayOfWeeks.add(DayOfWeek.from(d));
        }
        return dayOfWeeks;
    }

    private String abbreviateDayOfWeek(DayOfWeek day) {
        return day.name().substring(0, 3).toLowerCase();
    }

    private void loadScheduler(String deviceAddress) {
        CorneaClientFactory
              .getService(SchedulerService.class)
              .getScheduler(deviceAddress)
              .onSuccess(schedulerLoadedListener)
              .onFailure(onError);
    }

    private void addSchedulerModelListener() {
        if (schedulerModel == null) {
            schedulerRegistration = Listeners.clear(schedulerRegistration);
            return;
        }
        schedulerRegistration = schedulerModel.addListener(schedulerUpdatedListener);
    }

    private void onSchedulerChanged(PropertyChangeEvent pce) {
        if (Capability.EVENT_DELETED.equals(pce.getPropertyName())) {
            updateView();
            return;
        }

        String handle = String.format("%s:%s:%s", WeeklySchedule.NAMESPACE, abbreviateDayOfWeek(selectedDay), LNS_GROUP_ID);
        if (!handle.equals(pce.getPropertyName())) {
            return;
        }

        buildSchedule();
        updateView();
    }
}
