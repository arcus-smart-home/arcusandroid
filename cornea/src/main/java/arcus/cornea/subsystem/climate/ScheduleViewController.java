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

import arcus.cornea.device.thermostat.ThermostatMode;
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


public class ScheduleViewController extends BaseClimateController<ScheduleViewController.Callback> {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleViewController.class);
    private static final DateFormat parseFormat = new SimpleDateFormat("hh:mm:ss", Locale.getDefault());
    private static final DateFormat displayFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    private static final ScheduleViewController instance;

    static {
        instance = new ScheduleViewController();
        instance.init();
    }

    public static ScheduleViewController instance() {
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

    private Map<DayOfWeek,ScheduledDay> heatSchedule = new HashMap<>();
    private Map<DayOfWeek,ScheduledDay> coolSchedule = new HashMap<>();
    private Map<DayOfWeek,ScheduledDay> autoSchedule = new HashMap<>();

    @Override
    protected boolean isLoaded() {
        return super.isLoaded() && device.isLoaded() && scheduler.isLoaded();
    }

    public ListenerRegistration select(String deviceAddress, Callback callback, DayOfWeek dayOfWeek) {
        Listeners.clear(deviceRegistration);
        Listeners.clear(schedulerRegistration);
        selectedDay = dayOfWeek;
        heatSchedule.clear();
        coolSchedule.clear();
        autoSchedule.clear();

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

    public void selectMode(ThermostatMode mode) {
        if(!isLoaded()) {
            logger.debug("ignoring request to select the mode, the subsystem, device or schedule is not loaded");
            return;
        }

        DeviceModel d = getDevice();
        Thermostat t = (Thermostat) d;
        t.setHvacmode(mode.name());
        d.commit().onFailure(onError);
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
        if(changes.contains(Thermostat.ATTR_HVACMODE)) {
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
        if(heatSchedule.isEmpty() || coolSchedule.isEmpty() || autoSchedule.isEmpty()) {
            populateSchedule(getScheduler());
        }
        showScheduleModel(callback);
        showDaysWithSchedules(callback);
        selectDay(selectedDay);
    }

    private void showScheduleModel(Callback cb) {
        ScheduleModel sm =  createScheduleModel();
        if(allDisabled()) {
            cb.showScheduleDisabled(sm);
            return;
        }

        switch(sm.getMode()) {
            case OFF: cb.showScheduleOff(sm); break;
            case HEAT: cb.showSchedule(sm); break;
            case COOL: cb.showSchedule(sm); break;
            case AUTO: cb.showSchedule(sm); break;
        }
    }

    private void showDaysWithSchedules(Callback cb) {
        ScheduleModel sm =  createScheduleModel();
        switch (sm.getMode()) {
            case HEAT:
                cb.showIfDaysHaveSchedules(heatSchedule);
                break;
            case COOL:
                cb.showIfDaysHaveSchedules(coolSchedule);
                break;
            case AUTO:
                cb.showIfDaysHaveSchedules(autoSchedule);
                break;
            case OFF:
                cb.showScheduleOff(sm);
                break;
        }
    }

    private boolean allDisabled() {
        SchedulerModel scheduler = getScheduler();
        for(ThermostatMode mode : ThermostatMode.values()) {
            if(mode == ThermostatMode.OFF) { continue; }
            if(isEnabled(mode, scheduler)) {
                return false;
            }
        }
        return true;
    }

    private boolean isEnabled(ThermostatMode mode, SchedulerModel model) {
        Boolean enabled = (Boolean) model.get("sched:enabled:" + mode.name());
        return enabled == null ? false : enabled;
    }

    private ScheduleModel createScheduleModel() {
        SchedulerModel scheduler = getScheduler();

        DeviceModel d = getDevice();
        Thermostat t = (Thermostat) d;

        ThermostatMode mode = ThermostatMode.OFF; // Default to off for schedules if we don't know what mode it's in.
        if (t.getHvacmode() != null) {
            mode = ThermostatMode.valueOf(t.getHvacmode());
        }

        ScheduleModel sm = new ScheduleModel();
        sm.setMode(mode);
        sm.setDeviceId(d.getId());
        sm.setTitle(d.getName());
        sm.setSchedulerAddress(scheduler.getAddress());
        sm.setNextEvent(getNextScheduleEvent(scheduler));
        // TODO:  description
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
            Double heatSetpoint = (Double) timeOfDayCommand.getAttributes().get(Thermostat.ATTR_HEATSETPOINT);
            Double coolSetpoint = (Double) timeOfDayCommand.getAttributes().get(Thermostat.ATTR_COOLSETPOINT);

            int heatDisplay = -1;
            int coolDisplay = -1;
            if (heatSetpoint != null) {
                heatDisplay = TemperatureUtils.roundCelsiusToFahrenheit(heatSetpoint);
            }
            if (coolSetpoint != null) {
                coolDisplay = TemperatureUtils.roundCelsiusToFahrenheit(coolSetpoint);
            }

            switch (ThermostatMode.valueOf(timeOfDayCommand.getScheduleId())) {
                case AUTO:
                    if (heatDisplay != -1 && coolDisplay != -1) {
                        return heatDisplay + "º - " + coolDisplay + "º At " + nextStartTime;
                    }
                case COOL:
                    if (coolDisplay != -1) {
                        return coolDisplay + "º At " + nextStartTime;
                    }
                case HEAT:
                    if (heatDisplay != -1) {
                        return heatDisplay + "º At " + nextStartTime;
                    }

                case OFF:
                default:
                    return "";
            }
        }
        catch (Exception ex) {
            logger.debug("Was unable to parse next start time", ex);
            return "";
        }
    }

    private ScheduledDay getSelectedDaySchedule(DeviceModel d) {
        Thermostat t = (Thermostat) d;
        if (t.getHvacmode() != null) {
            ThermostatMode mode = ThermostatMode.valueOf(t.getHvacmode());
            switch (mode) {
                case COOL:
                    return coolSchedule.get(selectedDay);
                case HEAT:
                    return heatSchedule.get(selectedDay);
                case AUTO:
                    return autoSchedule.get(selectedDay);
            }
        }
        return null;
    }

    private void populateSchedule(SchedulerModel model) {
        heatSchedule = buildSchedule(ThermostatMode.HEAT, model);
        coolSchedule = buildSchedule(ThermostatMode.COOL, model);
        autoSchedule = buildSchedule(ThermostatMode.AUTO, model);
    }

    private Map<DayOfWeek,ScheduledDay> buildSchedule(ThermostatMode mode, SchedulerModel model) {
        Map<DayOfWeek,ScheduledDay> sched = new HashMap<>();
        for(DayOfWeek day : DayOfWeek.values()) {
            String key = "schedweek:" + abbreviateDayOfWeek(day) + ":" + mode.name();
            Collection<Map<String,Object>> daySchedule = (Collection<Map<String,Object>>) model.get(key);
            if (daySchedule != null) {
                sched.put(day, parseDaySchedule(day, mode, daySchedule));
            }
        }
        return sched;
    }

    private ScheduledDay parseDaySchedule(DayOfWeek day, ThermostatMode mode, Collection<Map<String,Object>> daySchedule) {
        ScheduledDay sd = new ScheduledDay();
        sd.setDayOfWeek(day);

        List<ScheduledSetPoint> sunriseSunset = new ArrayList<>();
        List<ScheduledSetPoint> absolute = new ArrayList<>();
        List<ScheduledSetPoint> events = new ArrayList<>();
        for(Map<String,Object> evt : daySchedule) {
            TimeOfDayCommand command = new TimeOfDayCommand(evt);

            ScheduledSetPoint sp = new ScheduledSetPoint();
            sp.setId(command.getId());
            sp.setMode(mode);
            sp.setTimeOfDay(
                  TimeOfDay.fromStringWithMode(
                        command.getTime(),
                        SunriseSunset.fromTimeOfDayCommand(command),
                        command.getOffsetMinutes()
                  )
            );
            sp.setRepeatsOn(parseRepeatsOn(command.getDays()));
            sp.setRepetitionText(ScheduleUtils.generateRepeatsText(sp.getRepeatsOn()));
            populateSetPoints(sp, mode, command.getAttributes());

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

    private void populateSetPoints(ScheduledSetPoint sch, ThermostatMode mode, Map<String,Object> attributes) {
        try {
            switch (mode) {
                case COOL:
                    sch.setCoolSetPoint(getCoolSetPoint(attributes));
                    break;
                case HEAT:
                    sch.setHeatSetPoint(getHeatSetPoint(attributes));
                    break;
                case AUTO:
                    sch.setCoolSetPoint(getCoolSetPoint(attributes));
                    sch.setHeatSetPoint(getHeatSetPoint(attributes));
                    break;
            }
        }
        catch (Exception ex) {
            logger.error("NPE in populate set points", ex);
        }
    }

    private int getCoolSetPoint(Map<String,Object> attributes) {
        return getTemp(attributes, Thermostat.ATTR_COOLSETPOINT);
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
        void showScheduleDisabled(ScheduleModel model);

        void showScheduleOff(ScheduleModel model);

        void showSchedule(ScheduleModel model);

        void showSelectedDay(ScheduledDay model);

        void showIfDaysHaveSchedules(Map<DayOfWeek,ScheduledDay> weekScheduledDayMap);

        void onError(ErrorModel error);
    }
}
