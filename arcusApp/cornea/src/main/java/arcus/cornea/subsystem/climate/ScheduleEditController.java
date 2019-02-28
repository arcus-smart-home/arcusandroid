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

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.device.thermostat.ThermostatMode;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.error.Errors;
import arcus.cornea.subsystem.climate.model.ScheduledSetPoint;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.SunriseSunset;
import arcus.cornea.utils.TemperatureUtils;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.capability.Schedule;
import com.iris.client.capability.Thermostat;
import com.iris.client.capability.WeeklySchedule;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Controller for editing schedules.  Expected usage pattern for a new schedule is:
 *
 * UX -> Controller.add
 * Controller -> callback.showAdd
 * User -> UX -> Controller.setStartTime
 * Controller -> callback.showAdd // to update the start time
 * User -> UX -> Controller.setSelectedSetPoint
 * Controller -> callback.showAdd
 * User -> UX -> Controller.setRepetitions
 * Controller -> callback.showAdd
 * User -> UX -> save
 * Controller -> callback.showSaving
 * Controller -> callback.done
 *
 * The flow for editing an existing schedule would look like:
 *
 * UX -> Controller.edit
 * Controller -> callback.showEdit
 * User -> UX -> Controller.setStartTime
 * Controller -> callback.showEdit // to update the start time
 * User -> UX -> Controller.setSelectedSetPoint
 * Controller -> callback.showAdd
 * User -> UX -> Controller.save
 * Controller -> callback.promptEditWhichDay
 * User -> UX -> Controller.saveAllDays
 * Controller -> callback.showSaving
 * Controller -> callback.done
 *
 */
public class ScheduleEditController extends BaseClimateController<ScheduleEditController.Callback> {
    private static final ScheduleEditController instance = new ScheduleEditController();

    public static ScheduleEditController instance() {
        return instance;
    }

    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onError(throwable);
        }
    });

    private final Listener<ClientEvent> onDone = Listeners.runOnUiThread(new Listener<ClientEvent>() {
        @Override
        public void onEvent(ClientEvent clientEvent) {
            onDone();
        }
    });

    private ScheduledSetPoint original;
    private ScheduledSetPoint setPoint;
    private String schedulerAddress;
    private boolean add;
    private DayOfWeek editingDay;

    public ListenerRegistration add(DayOfWeek editingDay, ThermostatMode mode, String schedulerAddress, Callback callback) {
        this.schedulerAddress = schedulerAddress;
        this.setPoint = new ScheduledSetPoint();
        this.setPoint.setRepeatsOn(EnumSet.of(editingDay));
        this.setPoint.setMode(mode);
        this.add = true;
        this.editingDay = editingDay;
        return setCallback(callback);
    }

    public ListenerRegistration edit(DayOfWeek editingDay, ScheduledSetPoint setPoint, String schedulerAddress, Callback callback) {
        this.schedulerAddress = schedulerAddress;
        this.setPoint = setPoint;
        this.original = clonePoint(setPoint);
        this.add = false;
        this.editingDay = editingDay;
        return setCallback(callback);
    }

    public void save() {
        if(!setPoint.isRepeating() || add) {
            doSave(false);
        }
        else {
            promptEditWhichDay();
        }
    }

    public void saveAllDays() {
        doSave(false);
    }

    public void saveSelectedDay() {
        doSave(true);
    }

    public void delete() {
        if(setPoint.isRepeating()) {
            promptDeleteWhichDay();
        }
        else {
            doDelete(false);
        }
    }

    public void deleteAllDays() {
        doDelete(false);
    }

    public void deleteSelectedDay() {
        doDelete(true);
    }

    public void setStartTime(TimeOfDay day) {
        setPoint.setTimeOfDay(day);
        updateView();
    }

    public void setCurrentSetPoint(int point) {
        setPoint.setCurrentSetPoint(point);
        updateView();
    }

    public void setCoolPoint(int coolPoint) {
        setPoint.setCoolSetPoint(coolPoint);
        // TODO defer
        updateView();
    }

    public void setHeatPoint(int heatPoint) {
        setPoint.setHeatSetPoint(heatPoint);
        updateView();
    }

    public void disableRepetitions() {
        enableRepetitions(EnumSet.noneOf(DayOfWeek.class));
    }

    // can't remove the day being edited
    public void enableRepetitions(Set<DayOfWeek> days) {
        EnumSet<DayOfWeek> repetitions = EnumSet.of(editingDay);
        repetitions.addAll(days);
        setPoint.setRepeatsOn(repetitions);
        setPoint.setRepetitionText(ScheduleUtils.generateRepeatsText(days));
    }

    protected void updateView(Callback callback) {
        if(add) {
            callback.showAdd(editingDay, setPoint);
        }
        else {
            callback.showEdit(editingDay, setPoint);
        }
    }

    protected void promptEditWhichDay() {
        Callback cb = getCallback();
        if(cb == null) {
            return;
        }
        cb.promptEditWhichDay();
    }

    protected void promptDeleteWhichDay() {
        Callback cb = getCallback();
        if(cb == null) {
            return;
        }
        cb.promptDeleteWhichDay();
    }

    protected void doDelete(boolean removeJustSelectedDay) {
        Callback cb = getCallback();
        if(cb == null) {
            return;
        }
        if(!removeJustSelectedDay) {
            deleteCommand();
        } else {
            setPoint.getRepeatsOn().remove(editingDay);
            update(setPoint).onSuccess(onDone);
        }
        cb.showSaving();
    }

    private void deleteCommand() {
        ClientRequest request = new ClientRequest();
        request.setAddress(schedulerAddress);
        request.setCommand(Schedule.DeleteCommandRequest.NAME + ":" + setPoint.getMode().name());
        request.setAttribute(Schedule.DeleteCommandRequest.ATTR_COMMANDID, setPoint.getId());
        CorneaClientFactory.getClient().request(request)
                .onFailure(onError)
                .onSuccess(onDone);
    }

    private ClientFuture<ClientEvent> update(ScheduledSetPoint setPoint) {
        ClientRequest request = new ClientRequest();
        request.setAddress(schedulerAddress);
        request.setCommand(WeeklySchedule.UpdateWeeklyCommandRequest.NAME + ":" + setPoint.getMode().name());
        request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_DAYS, serializeDays(setPoint));
        request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_MODE, setPoint.getTimeOfDay().getSunriseSunset().name());

        if (SunriseSunset.ABSOLUTE.equals(setPoint.getTimeOfDay().getSunriseSunset())) {
            request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_TIME, setPoint.getTimeOfDay().toString());
        }
        else {
            request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_OFFSETMINUTES, setPoint.getTimeOfDay().getOffset());
        }

        request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_ATTRIBUTES, serializeAttributes(setPoint));
        request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_COMMANDID, setPoint.getId());

        return CorneaClientFactory.getClient().request(request).onFailure(onError);
    }

    private ClientFuture<ClientEvent> add(ScheduledSetPoint setPoint) {
        ClientRequest request = new ClientRequest();
        request.setAddress(schedulerAddress);
        request.setCommand(WeeklySchedule.ScheduleWeeklyCommandRequest.NAME + ":" + setPoint.getMode().name());
        request.setAttribute(WeeklySchedule.ScheduleWeeklyCommandRequest.ATTR_DAYS, serializeDays(setPoint));
        request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_MODE, setPoint.getTimeOfDay().getSunriseSunset().name());

        if (SunriseSunset.ABSOLUTE.equals(setPoint.getTimeOfDay().getSunriseSunset())) {
            request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_TIME, setPoint.getTimeOfDay().toString());
        }
        else {
            request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_OFFSETMINUTES, setPoint.getTimeOfDay().getOffset());
        }

        request.setAttribute(WeeklySchedule.ScheduleWeeklyCommandRequest.ATTR_ATTRIBUTES, serializeAttributes(setPoint));

        return CorneaClientFactory.getClient().request(request).onFailure(onError);
    }

    private Map<String,Object> serializeAttributes(ScheduledSetPoint setPoint) {
        Map<String,Object> attrs = new HashMap<>();
        switch(setPoint.getMode()) {
            case COOL: attrs.put(Thermostat.ATTR_COOLSETPOINT, TemperatureUtils.fahrenheitToCelsius(setPoint.getCoolSetPoint())); break;
            case HEAT: attrs.put(Thermostat.ATTR_HEATSETPOINT, TemperatureUtils.fahrenheitToCelsius(setPoint.getHeatSetPoint())); break;
            case AUTO:
                attrs.put(Thermostat.ATTR_COOLSETPOINT, TemperatureUtils.fahrenheitToCelsius(setPoint.getCoolSetPoint()));
                attrs.put(Thermostat.ATTR_HEATSETPOINT, TemperatureUtils.fahrenheitToCelsius(setPoint.getHeatSetPoint()));
                break;
        }
        return attrs;
    }

    private Set<String> serializeDays(ScheduledSetPoint setPoint) {
        Set<String> days = new HashSet<>();
        for(DayOfWeek dow : setPoint.getRepeatsOn()) {
            days.add(dow.name().substring(0, 3));
        }
        return days;
    }

    private void onError(Throwable t) {
        Callback cb = getCallback();
        if(cb != null) {
            cb.showError(Errors.translate(t));
        }
    }

    private void onDone() {
        Callback cb = getCallback();
        if(cb != null) {
            cb.done();
        }
    }

    protected void doSave(boolean onlySelectedDay) {
        Callback cb = getCallback();
        if(cb == null) {
            return;
        }
        if(add) {
            add(setPoint).onSuccess(onDone);
        } else if(!onlySelectedDay) {
            update(setPoint).onSuccess(onDone);
        } else {
            original.getRepeatsOn().remove(editingDay);
            setPoint.setRepeatsOn(EnumSet.of(editingDay));
            update(original).onSuccess(new Listener<ClientEvent>() {
                @Override
                public void onEvent(ClientEvent clientEvent) {
                    add(setPoint).onSuccess(onDone);
                }
            });
        }

        cb.showSaving();
    }

    private ScheduledSetPoint clonePoint(ScheduledSetPoint point) {
        ScheduledSetPoint clone = new ScheduledSetPoint();
        clone.setMode(setPoint.getMode());
        clone.setId(setPoint.getId());
        clone.setCoolSetPoint(setPoint.getCoolSetPoint());
        clone.setHeatSetPoint(setPoint.getHeatSetPoint());
        clone.setTimeOfDay(setPoint.getTimeOfDay());
        clone.setRepeatsOn(EnumSet.copyOf(setPoint.getRepeatsOn()));
        clone.setRepetitionText(setPoint.getRepetitionText());
        return clone;
    }

    public interface Callback {

        /**
         * Show the Add dialog with the given day as the title.
         * @param day
         * @param setPoint
         */
        void showAdd(DayOfWeek day, ScheduledSetPoint setPoint);

        /**
         * Show the Edit dialog with the given day as the title.
         * @param day
         * @param setPoint
         */
        void showEdit(DayOfWeek day, ScheduledSetPoint setPoint);

        void showError(ErrorModel error);

        // probably a no-op for now
        void showSaving();

        /**
         * Called to prompt the user for editing
         * just *this* day or every day it occurs.
         *
         * Normal flow is:
         *   UX -> Controller.save();
         *   Controller -> Callback.promptEditWhichDay()
         *   User "All Days" -> UX -> Controller.saveAllDays()
         *   -or-
         *   User "This Day Only" UX -> Controller.saveSelectedDay()
         */
        void promptEditWhichDay();

        void promptDeleteWhichDay();

        void done();
    }
}
