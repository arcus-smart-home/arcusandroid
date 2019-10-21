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

import arcus.cornea.error.ErrorModel;
import arcus.cornea.error.Errors;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.SchedulerModelProvider;
import arcus.cornea.subsystem.climate.model.ScheduleModel;
import arcus.cornea.subsystem.climate.model.ScheduledDay;
import arcus.cornea.subsystem.climate.model.ScheduledSetPoint;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import arcus.cornea.utils.SunriseSunset;
import arcus.cornea.utils.TemperatureUtils;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.bean.TimeOfDayCommand;
import com.iris.client.capability.ClimateSubsystem;
import com.iris.client.capability.SpaceHeater;
import com.iris.client.capability.Thermostat;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.SchedulerModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


public class SpaceHeaterScheduleViewController extends BaseClimateController<SpaceHeaterScheduleViewController.Callback> {

    private static final Logger logger = LoggerFactory.getLogger(SpaceHeaterScheduleViewController.class);
    private static final DateFormat parseFormat = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
    private static final DateFormat displayFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    private static final SpaceHeaterScheduleViewController instance;
    private Map<DayOfWeek,ScheduledDay> schedule = new HashMap<>();

    static {
        instance = new SpaceHeaterScheduleViewController();
        instance.init();
    }

    public static SpaceHeaterScheduleViewController instance() {
        return instance;
    }

    private final Listener<ModelEvent> deviceModelListener = Listeners.runOnUiThread(new Listener<ModelEvent>() {
        @Override
        public void onEvent(ModelEvent modelEvent) {
            if(modelEvent instanceof ModelAddedEvent) {
                onDeviceAdded();
            } else if(modelEvent instanceof ModelChangedEvent) {
                onDeviceChanged((ModelChangedEvent) modelEvent);
            }
        }
    });

    private final Listener<ModelEvent> schedulerModelListener = Listeners.runOnUiThread(new Listener<ModelEvent>() {
        @Override
        public void onEvent(ModelEvent modelEvent) {
            if(modelEvent instanceof ModelAddedEvent) {
                onSchedulerAdded();
            } else if(modelEvent instanceof ModelChangedEvent) {
                populateSchedule(getScheduler());
                onSchedulerChanged((ModelChangedEvent) modelEvent);
            }
        }
    });

    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onError(throwable);
        }
    });

    private final Comparator<ScheduledSetPoint> eventSorter = new Comparator<ScheduledSetPoint>() {
        @Override
        public int compare(ScheduledSetPoint scheduledSetPoint, ScheduledSetPoint t1) {
            return scheduledSetPoint.getTimeOfDay().compareTo(t1.getTimeOfDay());
        }
    };

    private ModelSource<DeviceModel> device;
    private ModelSource<SchedulerModel> scheduler;
    private ListenerRegistration deviceRegistration = Listeners.empty();
    private ListenerRegistration schedulerRegistration = Listeners.empty();
    private DayOfWeek selectedDay = DayOfWeek.MONDAY;

    @Override
    protected boolean isLoaded() {
        return super.isLoaded() && device.isLoaded() && scheduler.isLoaded();
    }

    public ListenerRegistration select(String deviceAddress, Callback callback, DayOfWeek dayOfWeek) {
        Listeners.clear(deviceRegistration);
        Listeners.clear(schedulerRegistration);
        selectedDay = dayOfWeek;

        String addr = deviceAddress;

        device = DeviceModelProvider.instance().getModel(addr);
        deviceRegistration = device.addModelListener(deviceModelListener);
        device.load();

        scheduler = SchedulerModelProvider.instance().getForTarget(addr);
        schedulerRegistration = scheduler.addModelListener(schedulerModelListener);
        scheduler.load();

        return setCallback(callback);
    }

    public void selectDay(DayOfWeek day) {
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

        ScheduledDay sch = getSelectedDaySchedule(getDevice());
        if(sch != null) {
            cb.showSelectedDay(sch);
        }
    }

    private void onDeviceAdded() {
        updateView();
    }

    private void onDeviceChanged(ModelChangedEvent mce) {
        if(!isLoaded()) {
            logger.debug("ignoring device change because the subsystem, device or schedule is not loaded");
            return;
        }
        Callback cb = getCallback();
        if(cb == null) {
            logger.debug("ignoring device change because no callback is set");
            return;
        }

        Set<String> changes = mce.getChangedAttributes().keySet();
        if(changes.contains(SpaceHeater.ATTR_HEATSTATE)) {
            updateView();
        }
    }

    private void onSchedulerChanged(ModelChangedEvent mce) {
        updateView();
    }

    private void onSchedulerAdded() {
        updateView();
    }

    private void onError(Throwable t) {
        Callback cb = getCallback();
        if(cb !=  null) {
            cb.onError(Errors.translate(t));
        }
    }

    @Override
    protected void updateView(Callback callback, ClimateSubsystem subsystem) {
        if(!isLoaded()) {
            logger.debug("skipping update view because the subsystem, device or schedule is not loaded");
            return;
        }
        populateSchedule(getScheduler());
        showScheduleModel(callback);
        selectDay(selectedDay);
    }

    private void showScheduleModel(Callback cb) {
        ScheduleModel sm =  createScheduleModel();
        cb.showSchedule(sm);
    }

    private ScheduleModel createScheduleModel() {
        SchedulerModel scheduler = getScheduler();

        DeviceModel d = getDevice();

        ScheduleModel sm = new ScheduleModel();
        sm.setDeviceId(d.getId());
        sm.setTitle(d.getName());
        sm.setSchedulerAddress(scheduler.getAddress());
        sm.setNextEvent(getNextScheduleEvent(scheduler));
        return sm;
    }

    private String getNextScheduleEvent(SchedulerModel model) {
        if (model.getCommands() == null) {
            return "";
        }

        String nextEvent = String.valueOf(model.get("sched:nextFireCommand:" + model.getNextFireSchedule()));
        Map<String, Object> command = model.getCommands().get(nextEvent);
        if (command == null) {
            return "";
        }

        try {
            TimeOfDayCommand timeOfDayCommand = new TimeOfDayCommand(command);
            String nextStartTime = displayFormat.format(parseFormat.parse(timeOfDayCommand.getTime()));
            Double setpoint = (Double) timeOfDayCommand.getAttributes().get(SpaceHeater.ATTR_SETPOINT);

            int display = -1;
            if (setpoint != null) {
                display = TemperatureUtils.roundCelsiusToFahrenheit(setpoint);
            }
            return display + "º At " + nextStartTime;
        }
        catch (Exception ex) {
            logger.debug("Was unable to parse next start time", ex);
            return "";
        }
    }

    private ScheduledDay getSelectedDaySchedule(DeviceModel d) {
        return schedule.get(selectedDay);
    }

    private void populateSchedule(SchedulerModel model) {
        schedule = buildSchedule(model);
    }

    private Map<DayOfWeek,ScheduledDay> buildSchedule(SchedulerModel model) {
        Map<DayOfWeek,ScheduledDay> sched = new HashMap<>();
        for(DayOfWeek day : DayOfWeek.values()) {
            String key = "schedweek:" + abbreviateDayOfWeek(day);
            Collection<Map<String,Object>> daySchedule = (Collection<Map<String,Object>>) model.get(key);
            if (daySchedule != null) {
                sched.put(day, parseDaySchedule(day, daySchedule));
            }
        }
        return sched;
    }

    private ScheduledDay parseDaySchedule(DayOfWeek day, Collection<Map<String,Object>> daySchedule) {
        ScheduledDay sd = new ScheduledDay();
        sd.setDayOfWeek(day);

        List<ScheduledSetPoint> sunriseSunset = new ArrayList<>();
        List<ScheduledSetPoint> absolute = new ArrayList<>();
        List<ScheduledSetPoint> events = new ArrayList<>();
        for(Map<String,Object> evt : daySchedule) {
            TimeOfDayCommand command = new TimeOfDayCommand(evt);

            ScheduledSetPoint sp = new ScheduledSetPoint();
            sp.setId(command.getId());
            sp.setTimeOfDay(
                  TimeOfDay.fromStringWithMode(
                        command.getTime(),
                        SunriseSunset.fromTimeOfDayCommand(command),
                        command.getOffsetMinutes()
                  )
            );
            sp.setRepeatsOn(parseRepeatsOn(command.getDays()));
            sp.setRepetitionText(ScheduleUtils.generateRepeatsText(sp.getRepeatsOn()));
            populateSetPoints(sp, command.getAttributes());

            if (SunriseSunset.ABSOLUTE.equals(sp.getTimeOfDay().getSunriseSunset())) {
                absolute.add(sp);
            }
            else {
                sunriseSunset.add(sp);
            }
        }

        Collections.sort(sunriseSunset, eventSorter);
        Collections.sort(absolute, eventSorter);
        events.addAll(sunriseSunset);
        events.addAll(absolute);

        sd.setSetPoints(events);
        return sd;
    }

    private HashSet<DayOfWeek> parseRepeatsOn(Collection<String> days) {
        HashSet<DayOfWeek> dayOfWeeks = new HashSet<>();
        for(String d : days) {
            dayOfWeeks.add(DayOfWeek.from(d));
        }
        return dayOfWeeks;
    }

    private void populateSetPoints(ScheduledSetPoint sch, Map<String,Object> attributes) {
        try {
            sch.setHeatSetPoint(getHeatSetPoint(attributes));
        }
        catch (Exception ex) {
            logger.error("NPE in populate set points", ex);
        }
    }

    private int getHeatSetPoint(Map<String,Object> attributes) {
        return getTemp(attributes, Thermostat.ATTR_HEATSETPOINT);
    }

    private int getTemp(Map<String,Object> attributes, String key) {
        try {
            return TemperatureUtils.roundCelsiusToFahrenheit((Double) attributes.get(key));
        }
        catch (Exception ex) {
            logger.debug("Could not convert [{}] (Key: [{}])", attributes.get(key), key);
            return 0;
        }
    }

    private String abbreviateDayOfWeek(DayOfWeek day) {
        return day.name().substring(0, 3).toLowerCase();
    }

    private DeviceModel getDevice() {
        return device.get();
    }

    private SchedulerModel getScheduler() {
        return scheduler.get();
    }

    public interface Callback {

        void showSchedule(ScheduleModel model);

        void showSelectedDay(ScheduledDay model);

        void onError(ErrorModel error);
    }
}
