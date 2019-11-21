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
package arcus.cornea.subsystem.lightsnswitches.schedule;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.error.Errors;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.climate.ScheduleUtils;
import arcus.cornea.subsystem.lightsnswitches.model.LightsNSwitchesScheduleDay;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import arcus.cornea.utils.SunriseSunset;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.bean.TimeOfDayCommand;
import com.iris.client.capability.Capability;
import com.iris.client.capability.ColorTemperature;
import com.iris.client.capability.Dimmer;
import com.iris.client.capability.LightsNSwitchesSubsystem;
import com.iris.client.capability.Switch;
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

public class LightsNSwitchesScheduleViewController extends BaseSubsystemController<LightsNSwitchesScheduleViewController.Callback> {
    public interface Callback {
        void showSchedule(
              @NonNull DayOfWeek selectedDay,
              @NonNull Set<DayOfWeek> daysWithSchedule,
              @NonNull List<LightsNSwitchesScheduleDay> events);

        void onError(@NonNull ErrorModel error);
    }

    //We don't want to call updateView right off the bat and pass back an empty list for no reason
    @Override
    public ListenerRegistration setCallback(LightsNSwitchesScheduleViewController.Callback callback) {
        if(callbackRef.get() != null) {
            logger.warn("Replacing existing callback");
        }
        callbackRef = new WeakReference<>(callback);
        return Listeners.wrap(callbackRef);
    }

    public  static final String LNS_GROUP_ID = "LIGHT";
    private static final Logger logger = LoggerFactory.getLogger(LightsNSwitchesScheduleViewController.class);
    private static final LightsNSwitchesScheduleViewController INSTANCE;
    static {
        INSTANCE = new LightsNSwitchesScheduleViewController(
              SubsystemController.instance().getSubsystemModel(LightsNSwitchesSubsystem.NAMESPACE)
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
    private final Comparator<LightsNSwitchesScheduleDay> eventSorter = new Comparator<LightsNSwitchesScheduleDay>() {
        @Override
        public int compare(LightsNSwitchesScheduleDay firstDetail, LightsNSwitchesScheduleDay secondDetail) {
            return firstDetail.getTimeOfDay().compareTo(secondDetail.getTimeOfDay());
        }
    };
    private SchedulerModel schedulerModel;
    private DayOfWeek selectedDay = DayOfWeek.MONDAY;
    private ListenerRegistration schedulerRegistration = Listeners.empty();
    private Map<DayOfWeek,List<LightsNSwitchesScheduleDay>> deviceSchedule;
    private Set<DayOfWeek> daysScheduled;

    public static LightsNSwitchesScheduleViewController instance() {
        return INSTANCE;
    }

    LightsNSwitchesScheduleViewController(ModelSource<SubsystemModel> subsystem) {
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

    public void selectDay(@NonNull DayOfWeek day) {
        selectedDay = day;
        if(!isLoaded()) {
            logger.debug("ignoring request to select the day, the subsystem, device or schedule is not loaded");
            return;
        }
        Callback cb = getCallback();
        if(cb == null) {
            logger.debug("ignoring request to select the day, no callback is set");
            return;
        }

        updateView(cb);
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
        List<LightsNSwitchesScheduleDay> scheduledDay = deviceSchedule.get(selectedDay);
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

        List<LightsNSwitchesScheduleDay> eventsAsSunriseSunset = new ArrayList<>();
        List<LightsNSwitchesScheduleDay> eventsAsAbsolute = new ArrayList<>();
        List<LightsNSwitchesScheduleDay> events = new ArrayList<>();

        for(Map<String,Object> evt : daySchedule) {
            TimeOfDayCommand command = new TimeOfDayCommand(evt);
            LightsNSwitchesScheduleDay detail = new LightsNSwitchesScheduleDay();
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

            Map<String, Object> detailsMap = command.getAttributes();
            if (detailsMap != null) {
                detail.setOn(Switch.STATE_ON.equals(detailsMap.get(Switch.ATTR_STATE)));
                Number dimPercent = (Number) detailsMap.get(Dimmer.ATTR_BRIGHTNESS);
                Number colorTemp = (Number) detailsMap.get(ColorTemperature.ATTR_COLORTEMP);
                detail.setDimPercentage(dimPercent != null ? dimPercent.intValue() : 0);
                detail.setColorTemp(colorTemp != null ? colorTemp.intValue() : 0);
            }

            if (SunriseSunset.ABSOLUTE.equals(detail.getTimeOfDay().getSunriseSunset())) {
                eventsAsAbsolute.add(detail);
            }
            else {
                eventsAsSunriseSunset.add(detail);
            }
        }

        // Sort so "Sunrise/Sunset" events are at the top, others are below.
        Collections.sort(eventsAsSunriseSunset, eventSorter);
        Collections.sort(eventsAsAbsolute, eventSorter);
        events.addAll(eventsAsSunriseSunset);
        events.addAll(eventsAsAbsolute);

        if (!events.isEmpty()) {
            deviceSchedule.put(day, events);
        }
    }

    private HashSet<DayOfWeek> parseRepeatsOn(Collection<String> days) {
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
