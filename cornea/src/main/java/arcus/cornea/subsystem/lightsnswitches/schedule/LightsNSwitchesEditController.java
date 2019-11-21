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

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.google.gson.internal.LinkedTreeMap;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.error.Errors;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.climate.ScheduleUtils;
import arcus.cornea.subsystem.lightsnswitches.model.LightsNSwitchesScheduleDay;
import arcus.cornea.utils.CapabilityUtils;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import arcus.cornea.utils.SunriseSunset;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.capability.ColorTemperature;
import com.iris.client.capability.Dimmer;
import com.iris.client.capability.LightsNSwitchesSubsystem;
import com.iris.client.capability.Schedule;
import com.iris.client.capability.Scheduler;
import com.iris.client.capability.Switch;
import com.iris.client.capability.WeeklySchedule;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.SchedulerModel;
import com.iris.client.model.SubsystemModel;
import com.iris.client.service.SchedulerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LightsNSwitchesEditController extends BaseSubsystemController<LightsNSwitchesEditController.Callback> {
    public interface Callback {
        void showAdd(DayOfWeek day, LightsNSwitchesScheduleDay scheduledDay);
        void showEdit(DayOfWeek day, LightsNSwitchesScheduleDay scheduledDay);
        void onError(ErrorModel error);
        void makingRequest();
        void promptEditWhichDay();
        void promptDeleteWhichDay();
        void onRequestComplete();
    }

    private static final Logger logger = LoggerFactory.getLogger(LightsNSwitchesEditController.class);
    private static final LightsNSwitchesEditController INSTANCE;
    static {
        INSTANCE = new LightsNSwitchesEditController(
              SubsystemController.instance().getSubsystemModel(LightsNSwitchesSubsystem.NAMESPACE)
        );
        INSTANCE.init();
    }
    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onError(throwable);
        }
    });
    private final Listener<ClientEvent> onRequestComplete = Listeners.runOnUiThread(new Listener<ClientEvent>() {
        @Override
        public void onEvent(ClientEvent clientEvent) {
            onRequestComplete();
        }
    });
    private Listener<SchedulerService.GetSchedulerResponse> schedulerLoadedFromNetwork =
          Listeners.runOnUiThread(new Listener<SchedulerService.GetSchedulerResponse>() {
              @Override
              public void onEvent(SchedulerService.GetSchedulerResponse r) {
                  schedulerModel = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(r.getScheduler());
                  updateView();
              }
          });
    private final Listener<DeviceModel> deviceModelLoaded = Listeners.runOnUiThread(new Listener<DeviceModel>() {
        @Override
        public void onEvent(DeviceModel deviceModel) {
            if (scheduledDay == null) {
                return;
            }

            Collection<String> caps = deviceModel.getCaps();
            if (caps == null || caps.isEmpty()) {
                return;
            }

            scheduledDay.setSwitchable(caps.contains(Switch.NAMESPACE));
            scheduledDay.setDimmable(caps.contains(Dimmer.NAMESPACE));
            scheduledDay.setColorTempChangeable(caps.contains(ColorTemperature.NAMESPACE));
            updateView();
        }
    });

    private SchedulerModel schedulerModel;
    private String deviceAddress;
    private DayOfWeek editingDay;
    private boolean addingDay;
    private LightsNSwitchesScheduleDay thisDayOnlyCopy;
    private LightsNSwitchesScheduleDay scheduledDay;

    public static LightsNSwitchesEditController instance() {
        return INSTANCE;
    }

    LightsNSwitchesEditController(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
    }

    public ListenerRegistration add(
          @NonNull DayOfWeek editingDay,
          @NonNull String deviceAddress,
          Callback callback
    ) {
        Preconditions.checkNotNull(editingDay);
        Preconditions.checkNotNull(deviceAddress);

        this.deviceAddress = deviceAddress;
        this.scheduledDay = new LightsNSwitchesScheduleDay();
        this.addingDay = true;
        this.editingDay = editingDay;
        this.scheduledDay.setRepeatsOn(new HashSet<>(EnumSet.of(editingDay)));
        this.scheduledDay.setRepetitionText(ScheduleUtils.generateRepeatsText(new HashSet<>(EnumSet.of(editingDay))));

        DeviceModelProvider.instance().getModel(this.deviceAddress).load().onSuccess(deviceModelLoaded);
        getScheduler();
        return setCallback(callback);
    }

    public ListenerRegistration edit(
          @NonNull DayOfWeek editingDay,
          @NonNull LightsNSwitchesScheduleDay scheduledDay,
          @NonNull String deviceAddress,
          @NonNull Callback callback
    ) {
        Preconditions.checkNotNull(editingDay);
        Preconditions.checkNotNull(scheduledDay);
        Preconditions.checkNotNull(deviceAddress);

        this.deviceAddress = deviceAddress;
        this.scheduledDay = scheduledDay;
        this.thisDayOnlyCopy = cloneDay(scheduledDay);
        this.addingDay = false;
        this.editingDay = editingDay;

        DeviceModelProvider.instance().getModel(this.deviceAddress).load().onSuccess(deviceModelLoaded);
        getScheduler();
        return setCallback(callback);
    }

    public void save() {
        if (scheduledDay.getRepeatsOn().isEmpty()) {
            LooperExecutor.getMainExecutor().execute(new Runnable() { // Ensure we're on the main thread.
                @Override public void run() {
                    onError(new RuntimeException("Cannot save an event with no days selected for the event to occur on."));
                }
            });
            return;
        }

        boolean userChangedDays = thisDayOnlyCopy != null && !scheduledDay.getRepeatsOn().equals(thisDayOnlyCopy.getRepeatsOn());
        // If we're adding an event, just save
        // OR if the user changed days and the schedule is repeating, just save
        // OR regardless if the user changed days - if it's just a single day - just save that day
        if (addingDay || (userChangedDays && scheduledDay.isRepeating()) || !scheduledDay.isRepeating()) {
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
        if(thisDayOnlyCopy != null && thisDayOnlyCopy.isRepeating()) { // In the event the user deselects all days
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

    public void setScheduleTime(@NonNull TimeOfDay day) {
        scheduledDay.setTimeOfDay(day);
        updateView();
    }

    public void setSwitchStateOn(boolean isOn) {
        scheduledDay.setOn(isOn);
        updateView();
    }

    public void setDimPercent(@IntRange(from = 0, to = 100) int dimPercent) {
        scheduledDay.setDimPercentage(dimPercent);
        updateView();
    }

    public void setColorTemp(@IntRange(from = 0) int colorTemp) {
        scheduledDay.setColorTemp(colorTemp);
        updateView();
    }

    public void setRepeatsOn(@NonNull Set<DayOfWeek> days) {
        HashSet<DayOfWeek> repeats = new HashSet<>();
        repeats.addAll(days);
        scheduledDay.setRepeatsOn(repeats);
        scheduledDay.setRepetitionText(ScheduleUtils.generateRepeatsText(repeats));

        updateView();
    }

    public void disableRepetitions() {
        setRepeatsOn(EnumSet.noneOf(DayOfWeek.class));
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
            scheduledDay.getRepeatsOn().remove(editingDay);
            update(scheduledDay).onSuccess(onRequestComplete);
        }
        cb.makingRequest();
    }

    protected void doSave(boolean onlySelectedDay) {
        Callback cb = getCallback();
        if(cb == null) {
            return;
        }

        if(addingDay) {
            add(scheduledDay).onSuccess(onRequestComplete);
        }
        else if(!onlySelectedDay) {
            // All days selected
            update(scheduledDay).onSuccess(onRequestComplete);
        }
        else {
            thisDayOnlyCopy.getRepeatsOn().remove(editingDay);
            scheduledDay.setRepeatsOn(new HashSet<>(EnumSet.of(editingDay)));

            // TODO What if thisDayOnlyCopy.remove(editingDay) makes size() == 0?
            // Should that be a delete?
            update(thisDayOnlyCopy).onSuccess(new Listener<ClientEvent>() {
                @Override
                public void onEvent(ClientEvent clientEvent) {
                    add(scheduledDay).onSuccess(onRequestComplete);
                }
            });
        }

        cb.makingRequest();
    }

    private void deleteCommand() {
        ClientRequest request = new ClientRequest();

        request.setAddress(schedulerModel.getAddress());
        request.setCommand(Schedule.DeleteCommandRequest.NAME + ":" + LightsNSwitchesScheduleViewController.LNS_GROUP_ID);
        request.setAttribute(Schedule.DeleteCommandRequest.ATTR_COMMANDID, scheduledDay.getCommandID());

        CorneaClientFactory.getClient().request(request)
              .onFailure(onError)
              .onSuccess(onRequestComplete);
    }

    private ClientFuture<ClientEvent> update(LightsNSwitchesScheduleDay modifyingDay) {
        ClientRequest request = new ClientRequest();

        request.setAddress(schedulerModel.getAddress());
        request.setCommand(WeeklySchedule.UpdateWeeklyCommandRequest.NAME + ":" + LightsNSwitchesScheduleViewController.LNS_GROUP_ID);
        request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_DAYS, serializeDays(modifyingDay));
        request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_MODE, modifyingDay.getTimeOfDay().getSunriseSunset().name());

        if (SunriseSunset.ABSOLUTE.equals(modifyingDay.getTimeOfDay().getSunriseSunset())) {
            request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_TIME, modifyingDay.getTimeOfDay().toString());
        }
        else {
            request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_OFFSETMINUTES, modifyingDay.getTimeOfDay().getOffset());
        }

        request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_ATTRIBUTES, serializeAttributes(modifyingDay));
        request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_COMMANDID, modifyingDay.getCommandID());

        return CorneaClientFactory.getClient()
              .request(request)
              .onFailure(onError);
    }

    private ClientFuture<ClientEvent> add(LightsNSwitchesScheduleDay modifyingDay) {
        ClientRequest request = new ClientRequest();

        request.setAddress(schedulerModel.getAddress());
        request.setCommand(WeeklySchedule.ScheduleWeeklyCommandRequest.NAME + ":" + LightsNSwitchesScheduleViewController.LNS_GROUP_ID);
        request.setAttribute(WeeklySchedule.ScheduleWeeklyCommandRequest.ATTR_DAYS, serializeDays(modifyingDay));
        request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_MODE, modifyingDay.getTimeOfDay().getSunriseSunset().name());

        if (SunriseSunset.ABSOLUTE.equals(modifyingDay.getTimeOfDay().getSunriseSunset())) {
            request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_TIME, modifyingDay.getTimeOfDay().toString());
        }
        else {
            request.setAttribute(WeeklySchedule.UpdateWeeklyCommandRequest.ATTR_OFFSETMINUTES, modifyingDay.getTimeOfDay().getOffset());
        }

        request.setAttribute(WeeklySchedule.ScheduleWeeklyCommandRequest.ATTR_ATTRIBUTES, serializeAttributes(modifyingDay));


        LinkedTreeMap map = (LinkedTreeMap)schedulerModel.get(Scheduler.ATTR_COMMANDS);
        if (map != null && map.size() == 0){
            setScheduleEnabled(schedulerModel.getTarget(), true);
        }

        return CorneaClientFactory.getClient()
              .request(request)
              .onFailure(onError);
    }

    private Map<String,Object> serializeAttributes(LightsNSwitchesScheduleDay modifyingDay) {
        Map<String,Object> attrs = new HashMap<>();

        attrs.put(Switch.ATTR_STATE, modifyingDay.isOn() ? Switch.STATE_ON : Switch.STATE_OFF);

        if (modifyingDay.getDimPercentage() != 0 && modifyingDay.isOn()) {
            attrs.put(Dimmer.ATTR_BRIGHTNESS, modifyingDay.getDimPercentage());
        }

        if (modifyingDay.getColorTemp() != 0) {
            attrs.put(ColorTemperature.ATTR_COLORTEMP, modifyingDay.getColorTemp());
        }

        return attrs;
    }

    private Set<String> serializeDays(LightsNSwitchesScheduleDay modifyingDay) {
        Set<String> days = new HashSet<>();
        for(DayOfWeek dow : modifyingDay.getRepeatsOn()) {
            days.add(dow.name().substring(0, 3).toUpperCase());
        }
        return days;
    }

    protected void updateView(Callback callback) {
        if (!isLoaded()) {
            logger.debug("LightsNSwitchesEditController   Not updating view, isLoaded == false");
            return;
        }

        if (addingDay) {
            callback.showAdd(editingDay, scheduledDay);
        }
        else {
            callback.showEdit(editingDay, scheduledDay);
        }
    }

    private void onError(Throwable t) {
        Callback cb = getCallback();
        if(cb !=  null) {
            cb.onError(Errors.translate(t));
        }
    }

    private void onRequestComplete() {
        Callback cb = getCallback();
        if(cb != null) {
            cb.onRequestComplete();
        }
    }

    private void getScheduler() {
        for (SchedulerModel model : CorneaClientFactory.getStore(SchedulerModel.class).values()) {
            if (deviceAddress.equals(model.getTarget())) {
                schedulerModel = model;
                return;
            }
        }

        // TODO: Add a getOrCreateForTarget(String address) method to the SchedulerModelProvider class?
        CorneaClientFactory
              .getService(SchedulerService.class)
              .getScheduler(deviceAddress)
              .onSuccess(schedulerLoadedFromNetwork)
              .onFailure(onError);
    }

    private @NonNull LightsNSwitchesScheduleDay cloneDay(LightsNSwitchesScheduleDay cloneThis) {
        LightsNSwitchesScheduleDay clone = new LightsNSwitchesScheduleDay();

        clone.setColorTemp(cloneThis.getColorTemp());
        clone.setDimPercentage(cloneThis.getDimPercentage());
        clone.setOn(cloneThis.isOn());
        clone.setCommandID(cloneThis.getCommandID());
        clone.setRepeatsOn(new HashSet<>(cloneThis.getRepeatsOn()));
        clone.setRepetitionText(cloneThis.getRepetitionText());
        clone.setTimeOfDay(cloneThis.getTimeOfDay());
        clone.setColorTempChangeable(cloneThis.isColorTempChangeable());
        clone.setDimmable(cloneThis.isDimmable());
        clone.setSwitchable(cloneThis.isSwitchable());

        return clone;
    }



    public void setScheduleEnabled(String deviceId, final boolean enabled) {
        CorneaClientFactory.getService(SchedulerService.class).getScheduler(deviceId).onSuccess(new Listener<SchedulerService.GetSchedulerResponse>() {
            @Override
            public void onEvent(SchedulerService.GetSchedulerResponse getSchedulerResponse) {
                SchedulerModel scheduler = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(getSchedulerResponse.getScheduler());
                Set<String> keys = scheduler.getInstances().keySet();
                for (String key : keys) {
                    setEnabled(scheduler, key, enabled);
                }
            }
        });
    }

    private void setEnabled (SchedulerModel scheduler, String instance, boolean isEnabled) {
        new CapabilityUtils(scheduler).setInstance(instance).attriubuteToValue(Schedule.ATTR_ENABLED, isEnabled).andSendChanges();

    }
}
