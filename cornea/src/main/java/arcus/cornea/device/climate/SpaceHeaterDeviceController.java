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
package arcus.cornea.device.climate;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Function;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.SchedulerModelProvider;
import arcus.cornea.subsystem.climate.BaseClimateController;
import arcus.cornea.utils.DateUtils;
import arcus.cornea.utils.DebouncedRequest;
import arcus.cornea.utils.DebouncedRequestScheduler;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import arcus.cornea.utils.PropertyChangeMonitor;
import arcus.cornea.utils.SunriseSunset;
import arcus.cornea.utils.TemperatureUtils;
import com.iris.client.bean.TimeOfDayCommand;
import com.iris.client.capability.ClimateSubsystem;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.DeviceOta;
import com.iris.client.capability.Schedule;
import com.iris.client.capability.SpaceHeater;
import com.iris.client.capability.Temperature;
import com.iris.client.capability.TwinStar;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.SchedulerModel;

import java.util.Map;
import java.util.Set;


public class SpaceHeaterDeviceController extends BaseClimateController<SpaceHeaterDeviceController.Callback> implements DebouncedRequest.DebounceCallback {
    private static final int DEBOUNCE_REQUEST_DELAY_MS = 500;
    private final DebouncedRequestScheduler debouncedRequestScheduler;

    public interface Callback {
        void showDeviceControls(SpaceHeaterControllerDetailsModel model);
        void errorOccurred(Throwable throwable);
    }

    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            updateView();
        }
    });

    public static SpaceHeaterDeviceController newController(String deviceId) {
        ModelSource<DeviceModel> source = DeviceModelProvider.instance().getModel("DRIV:dev:" + deviceId);
        SpaceHeaterDeviceController controller = new SpaceHeaterDeviceController(source);
        controller.init();
        return controller;
    }

    private ModelSource<DeviceModel> source;
    private static final int TIMEOUT_MS = 30_000;
    private final Function<String, Void> onFailureFunction = new Function<String, Void>() {
        @Override public Void apply(@Nullable String input) {
            refreshModel();

            return null;
        }
    };
    private final PropertyChangeMonitor.Callback propertyChangeMonitorCB = new PropertyChangeMonitor.Callback() {
        @Override public void requestTimedOut(String address, String attribute) {
            refreshModel();
        }

        @Override public void requestSucceeded(String address, String attribute) {
            LooperExecutor.getMainExecutor().execute(new Runnable() {
                @Override public void run() {
                    updateView();
                }
            });
        }
    };

    SpaceHeaterDeviceController(
          ModelSource<DeviceModel> source
    ) {
        debouncedRequestScheduler = new DebouncedRequestScheduler(DEBOUNCE_REQUEST_DELAY_MS);
        this.source = source;
        this.source.addModelListener(Listeners.runOnUiThread(new Listener<ModelEvent>() {
            @Override public void onEvent(ModelEvent modelEvent) {
                if (!(modelEvent instanceof ModelChangedEvent)) {
                    return;
                }

                ModelChangedEvent mce = (ModelChangedEvent) modelEvent;
                Set<String> changes = mce.getChangedAttributes().keySet();
                if (changes.contains(Device.ATTR_NAME) ||
                    changes.contains(DeviceOta.ATTR_STATUS) ||
                    changes.contains(DeviceConnection.ATTR_STATE) ||
                    changes.contains(SpaceHeater.ATTR_HEATSTATE) ||
                    changes.contains(SpaceHeater.ATTR_SETPOINT) ||
                    changes.contains(TwinStar.ATTR_ECOMODE) ||
                    changes.contains(DeviceAdvanced.ATTR_ERRORS) ||
                    changes.contains(Schedule.ATTR_ENABLED) ||
                    changes.contains(Schedule.ATTR_NEXTFIRECOMMAND) ||
                    changes.contains(Schedule.ATTR_NEXTFIRETIME) ||
                    changes.contains(Temperature.ATTR_TEMPERATURE)
                      ) {
                    updateView();
                }
            }
        }));
        this.source.load();
        scheduler = SchedulerModelProvider.instance().getForTarget(getDeviceAddress());
        schedulerRegistration = scheduler.addModelListener(schedulerModelListener);
        scheduler.load();
    }

    @Override protected void onSubsystemChanged(ModelChangedEvent event) {
        super.onSubsystemChanged(event);
        Set<String> changes = event.getChangedAttributes().keySet();
        if (changes.contains(ClimateSubsystem.ATTR_CONTROLDEVICES) ||
              changes.contains(ClimateSubsystem.ATTR_PRIMARYTHERMOSTAT) ||
              changes.contains(ClimateSubsystem.ATTR_TEMPERATUREDEVICES)) {
            updateView();
        }
    }

    @Override protected void updateView(Callback callback) {
        if(!source.isLoaded()) {
            source.load();
            return;
        }
        callback.showDeviceControls(update(source.get()));
    }

    protected SpaceHeaterControllerDetailsModel update(DeviceModel device) {
        if(device == null || !(device instanceof SpaceHeater)) {
            return new SpaceHeaterControllerDetailsModel();
        }

        SpaceHeater deviceModel = (SpaceHeater) device;

        ClimateSubsystem subsystem = getClimateSubsystem();
        if (subsystem == null) {
            return new SpaceHeaterControllerDetailsModel();
        }

        SpaceHeaterControllerDetailsModel model = new SpaceHeaterControllerDetailsModel();
        model.setDeviceAddress(device.getAddress());
        model.setDeviceName(device.getName());

        model.setIsInOTA(DeviceOta.STATUS_INPROGRESS.equals(device.get(DeviceOta.ATTR_STATUS)));

        // Unless it's been explicitly marked offline, leave it online.
        model.setIsOnline(!DeviceConnection.STATE_OFFLINE.equals(device.get(DeviceConnection.ATTR_STATE)));
        model.setRightButtonEnabled(true);
        model.setLeftButtonEnabled(true);

        //schedule mode?
        model.setHasRequestInFlight(PropertyChangeMonitor.instance().hasAnyChangesFor(device.getAddress()));
        if(model.hasRequestInFlight() || model.isInOTA() || !model.isOnline()) {
            model.setLeftButtonEnabled(false);
            model.setRightButtonEnabled(false);
            model.setBottomButtonEnabled(false);
        }
        else {
            model.setBottomButtonEnabled(true);
            if (deviceModel.getSetpoint() <= deviceModel.getMinsetpoint()) {
                model.setLeftButtonEnabled(false);
            }
            if (deviceModel.getSetpoint() >= deviceModel.getMaxsetpoint()) {
                model.setRightButtonEnabled(false);
            }
        }

        if (device instanceof TwinStar) {
            if (TwinStar.ECOMODE_ENABLED.equals(((TwinStar) device).getEcomode())) {
                model.setDeviceEcoOn(true);
            }
            else {
                model.setDeviceEcoOn(false);
            }
            if (SpaceHeater.HEATSTATE_ON.equals(deviceModel.getHeatstate())) {
                model.setDeviceModeOn(true);
            }
            else {
                model.setDeviceModeOn(false);
            }
        }

        if (device.getCaps().contains(Temperature.NAMESPACE)) {
            Temperature temperature = (Temperature) device;
            model.setCurrentTemp(TemperatureUtils.roundCelsiusToFahrenheit(temperature.getTemperature()));
        }
        else {
            model.setCurrentTemp(0);
        }

        model.setSetPoint(TemperatureUtils.roundCelsiusToFahrenheit(deviceModel.getSetpoint()));

        if(scheduler.get() != null) {
            model.setNextEventDisplay(getNextScheduleEvent(scheduler.get()));
        }

        return model;
    }

    public void leftButtonEvent() {
        double minSetpoint = TemperatureUtils.roundCelsiusToFahrenheit(((SpaceHeater)getDeviceModel()).getMinsetpoint());
        double fahrenheit = TemperatureUtils.roundCelsiusToFahrenheit(((SpaceHeater)getDeviceModel()).getSetpoint());

        fahrenheit--;
        if (fahrenheit > minSetpoint) {
            updateSetPoint(fahrenheit);
        }
    }

    public void rightButtonEvent() {
        double maxSetpoint = TemperatureUtils.roundCelsiusToFahrenheit(((SpaceHeater)getDeviceModel()).getMaxsetpoint());
        double fahrenheit = TemperatureUtils.roundCelsiusToFahrenheit(((SpaceHeater)getDeviceModel()).getSetpoint());

        fahrenheit++;
        if (fahrenheit < maxSetpoint) {
            updateSetPoint(fahrenheit);
        }
    }

    private void updateSetPoint(double value) {
        ClimateSubsystem subsystem = getClimateSubsystem();
        final String address = getDeviceAddress();

        double newTemp = getClampedSetPoint(value);
        ((SpaceHeater)getDeviceModel()).setSetpoint(newTemp);

        if (subsystem != null && !TextUtils.isEmpty(address)) {
            //we want to throttle the setpoint calls
            DebouncedRequest request = new DebouncedRequest(getDeviceModel());
            request.setCallbackHandler(new DebouncedRequest.DebounceCallback() {
                @Override
                public void commitEvent() {
                    startMonitorFor(
                            address,
                            SpaceHeater.ATTR_SETPOINT,
                            null
                    );
                    updateViewOnMainThread();
                }
            });
            request.setOnError(onError);
            debouncedRequestScheduler.schedule(SpaceHeater.ATTR_SETPOINT, request);
        }
        else {
            updateError(new RuntimeException("Unable to send command. Controller was null."));
        }
        updateView();
    }

    private double getClampedSetPoint(double value) {
        SpaceHeater heater = (SpaceHeater)getDeviceModel();
        double newTemp = TemperatureUtils.fahrenheitToCelsius(value);
        if(newTemp < heater.getMinsetpoint()) {
            newTemp = heater.getMinsetpoint();
        }
        if(newTemp > heater.getMaxsetpoint()) {
            newTemp = heater.getMaxsetpoint();
        }
        return newTemp;
    }

    public void updateSpaceHeaterMode(String mode) {
        if(!(getDeviceModel() instanceof SpaceHeater)) {
            updateView();
        }
        ClimateSubsystem subsystem = getClimateSubsystem();
        String address = getDeviceAddress();

        if (subsystem != null && !TextUtils.isEmpty(address)) {
            startMonitorFor(
                    address,
                    SpaceHeater.ATTR_HEATSTATE,
                    null
            );

            ((SpaceHeater)getDeviceModel()).setHeatstate(mode);
            getDeviceModel().commit();
            updateView();
        }
        else {
            updateError(new RuntimeException("Unable to send command. Controller was null."));
        }
    }

    public void updateEcoMode(String mode) {
        ClimateSubsystem subsystem = getClimateSubsystem();
        String address = getDeviceAddress();

        if (subsystem != null && !TextUtils.isEmpty(address)) {
            startMonitorFor(
                    address,
                    TwinStar.ATTR_ECOMODE,
                    null
            );

            ((TwinStar)getDeviceModel()).setEcomode(mode);
            getDeviceModel().commit();
            updateView();
        }
        else {
            updateError(new RuntimeException("Unable to send command. Controller was null."));
        }
    }

    protected void startMonitorFor(String address, String attribute, Object shouldBe) {
        PropertyChangeMonitor.instance().removeAllFor(address);
        PropertyChangeMonitor.instance().startMonitorFor(
              address, attribute, TIMEOUT_MS, propertyChangeMonitorCB, shouldBe, onFailureFunction
        );
    }

    protected void updateError(Throwable throwable) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.errorOccurred(throwable);
        }
    }

    protected void refreshModel() {
        DeviceModel model = getDeviceModel();
        if (model != null) {
            model.refresh();
        }
    }

    protected @Nullable DeviceModel getDeviceModel() {
        if (source == null) {
            return null;
        }

        return source.get();
    }

    protected @Nullable String getDeviceAddress() {
        DeviceModel model = getDeviceModel();
        if (model == null) {
            return null;
        }

        return model.getAddress();
    }

    @Override
    public void commitEvent() {
        //no-op for now
    }

    private void updateViewOnMainThread() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            @Override
            public void run() {
                updateView();
            }
        });
    }














    private ModelSource<SchedulerModel> scheduler;
    private ListenerRegistration schedulerRegistration = Listeners.empty();

    private void onSchedulerChanged(ModelChangedEvent mce) {
        updateView();
    }

    private void onSchedulerAdded() {
        updateView();
    }

    private final Listener<ModelEvent> schedulerModelListener = Listeners.runOnUiThread(new Listener<ModelEvent>() {
        @Override
        public void onEvent(ModelEvent modelEvent) {
            if(modelEvent instanceof ModelAddedEvent) {
                onSchedulerAdded();
            } else if(modelEvent instanceof ModelChangedEvent) {
                onSchedulerChanged((ModelChangedEvent) modelEvent);
            }
        }
    });

    private boolean isEnabled(SchedulerModel model) {
        Boolean enabled = (Boolean) model.get("sched:enabled:CLIMATE");
        return enabled == null ? false : enabled;
    }

    private String getNextScheduleEvent(SchedulerModel model) {

        if (model.getCommands() == null) {
            return "";
        }

        if(!isEnabled(model)) {
            return "";
        }

        String nextEvent = String.valueOf(model.get("sched:nextFireCommand:" + model.getNextFireSchedule()));
        Map<String, Object> command = model.getCommands().get(nextEvent);
        if (command == null) {
            return "";
        }

        try {
            TimeOfDayCommand timeOfDayCommand = new TimeOfDayCommand(command);
            String nextStartTime = DateUtils.format(model.getNextFireTime());
            Double setPoint = (Double) timeOfDayCommand.getAttributes().get(SpaceHeater.ATTR_SETPOINT);
            String heatState = (String) timeOfDayCommand.getAttributes().get(SpaceHeater.ATTR_HEATSTATE);

            int setPointDisplay = -1;
            if (setPoint != null) {
                setPointDisplay = TemperatureUtils.roundCelsiusToFahrenheit(setPoint);
            }

            if (setPointDisplay != -1) {
                if(timeOfDayCommand.getOffsetMinutes() != null) {
                    nextStartTime = SunriseSunset.getNextEventForSunriseSunset(model.getNextFireTime(), timeOfDayCommand);
                }
                if(SpaceHeater.HEATSTATE_OFF.equals(heatState)) {
                    return nextStartTime + " " + heatState;
                }
                else {
                    return setPointDisplay + "º " + nextStartTime + " " + heatState;
                }
            }

        }
        catch (Exception ex) {
            //logger.debug("Was unable to parse next start time", ex);
            return "";
        }
        return "";
    }
}
