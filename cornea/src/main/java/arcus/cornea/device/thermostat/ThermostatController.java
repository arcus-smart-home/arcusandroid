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
package arcus.cornea.device.thermostat;

import arcus.cornea.device.DeviceController;
import arcus.cornea.error.Errors;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.climate.EventMessageMonitor;
import arcus.cornea.subsystem.climate.model.DeviceControlType;
import arcus.cornea.utils.DebouncedRequest;
import arcus.cornea.utils.DebouncedRequestScheduler;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import arcus.cornea.utils.TemperatureUtils;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.RelativeHumidity;
import com.iris.client.capability.Temperature;
import com.iris.client.capability.Thermostat;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * The set point methods are de-bounced, this means:
 *  - First update will go through immediately
 *  - Any following requests will be delayed until 500 ms after the last change
 *    have been received
 *  - No value changes will result in updates to the view until the final
 *    pending change has been sent
 */
public class ThermostatController extends DeviceController<ThermostatProxyModel> implements DebouncedRequest.DebounceCallback {

    private static final int MIN_SETPOINT_NOMINAL = 45;
    private static final int MAX_SETPOINT_NOMINAL = 95;

    private static final int DEBOUNCE_REQUEST_DELAY_MS = 500;
    private static final int DEBOUNCE_REQUEST_DELAY_CLOUD_MS = 3000;
    private static final String SET_POINT_TASK = "SET_POINT";
    private static final String SET_MODE_TASK = "SET_MODE";
    private final DebouncedRequestScheduler debouncedRequestScheduler;
    protected EventMessageMonitor eventMesssageMonitor;
    private CommandCommittedCallback callback;
    private static final String HTCC = "TCCThermostat";

    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            updateView();
            showError(throwable);
        }
    });

    public void setCommandCommimttedCallback(CommandCommittedCallback callback) {
        this.callback = callback;
    }

    public static ThermostatController newController(String deviceId, DeviceController.Callback callback) {
        ModelSource<DeviceModel> source = DeviceModelProvider.instance().getModel("DRIV:dev:" + deviceId);
        ThermostatController controller = new ThermostatController(source);
        controller.setCallback(callback);
        return controller;
    }

    ThermostatController(ModelSource<DeviceModel> source) {
        super(source);
        listenForProperties(
            Device.ATTR_NAME,
            DeviceConnection.ATTR_STATE,
            Thermostat.ATTR_ACTIVE,
            Thermostat.ATTR_FANMODE,
            Thermostat.ATTR_AUTOFANSPEED,
            Thermostat.ATTR_CONTROLMODE,
            Thermostat.ATTR_COOLSETPOINT,
            Thermostat.ATTR_HEATSETPOINT,
            Thermostat.ATTR_HVACMODE
        );
        if(source.get() != null && source.get().getDevtypehint().equals("TCCThermostat")) {
            debouncedRequestScheduler = new DebouncedRequestScheduler(DEBOUNCE_REQUEST_DELAY_CLOUD_MS);
        }
        else {
            debouncedRequestScheduler = new DebouncedRequestScheduler(DEBOUNCE_REQUEST_DELAY_MS);
        }
        eventMesssageMonitor = EventMessageMonitor.getInstance();
    }

    @Override
    protected ThermostatProxyModel update(DeviceModel device) {
        ThermostatProxyModel model = new ThermostatProxyModel();
        model.setDeviceId(device.getId());
        model.setName(device.getName());
        model.setDeviceTypeHint(device.getDevtypehint());
        model.setIsCloudDevice(device.getDevtypehint() != null && (device.getDevtypehint().equals("TCCThermostat") || (device.getDevtypehint().equals("NestThermostat"))));
        model.setOnline(!DeviceConnection.STATE_OFFLINE.equals(device.get(DeviceConnection.ATTR_STATE)));
        model.setSupportedModes(getSupportedModes((Thermostat) device));
        model.setHoneywellDevice(device.getDevtypehint() != null && (device.getDevtypehint().equals("TCCThermostat")));
        model.setMaximumSetpoint(getMaximumSetpointF(device));
        model.setMinimumSetpoint(getMinimumSetpointF(device));

        Thermostat thermostat = (Thermostat) device;
        if (thermostat.getHvacmode() != null) {
            switch (thermostat.getHvacmode()) {
                case Thermostat.HVACMODE_AUTO:
                    model.setMode(ThermostatMode.AUTO);
                    break;
                case Thermostat.HVACMODE_COOL:
                    model.setMode(ThermostatMode.COOL);
                    break;
                case Thermostat.HVACMODE_HEAT:
                    model.setMode(ThermostatMode.HEAT);
                    break;
                case Thermostat.HVACMODE_OFF:
                    model.setMode(ThermostatMode.OFF);
                    break;
                case Thermostat.HVACMODE_ECO:
                    model.setMode(ThermostatMode.ECO);
            }
        }

        if (device instanceof DeviceAdvanced && ((DeviceAdvanced)device).getErrors() != null) {
            model.setErrors(((DeviceAdvanced)device).getErrors().keySet());
        }

        model.setFanMode(getFanMode(thermostat));
        model.setIsRunning(isRunning(thermostat));
        model.setFanBlowing(isFanBlowing(thermostat));
        if (thermostat.getHeatsetpoint() != null) {
            model.setHeatSetPoint(TemperatureUtils.roundCelsiusToFahrenheit(thermostat.getHeatsetpoint()));
        }
        if (thermostat.getCoolsetpoint() != null) {
            model.setCoolSetPoint(TemperatureUtils.roundCelsiusToFahrenheit(thermostat.getCoolsetpoint()));
        }

        if (device.getCaps().contains(Temperature.NAMESPACE)) {
            Temperature temperature = (Temperature) device;
            if (temperature.getTemperature() != null) {
                model.setTemperature(TemperatureUtils.roundCelsiusToFahrenheit(temperature.getTemperature()));
            }
        }

        if (device.getCaps().contains(RelativeHumidity.NAMESPACE)) {
            RelativeHumidity humidity = (RelativeHumidity) device;
            if (humidity.getHumidity() != null) {
                model.setHumidity(humidity.getHumidity().intValue());
            }
        }
        return model;
    }

    private List<ThermostatMode> getSupportedModes(Thermostat model) {
        List<ThermostatMode> displayedModes = new ArrayList<>();

        Set<String> supportedModes = model.getSupportedmodes();
        Boolean supportsAuto = model.getSupportsAuto();

        if (supportedModes == null || supportedModes.isEmpty()) {
            displayedModes = getDefaultOperatingModes(model.getDevtypehint(), supportsAuto == null || supportsAuto);
        } else {
            for (String thisMode : supportedModes) {
                displayedModes.add(ThermostatMode.valueOf(thisMode));
            }
        }

        return displayedModes;
    }

    private List<ThermostatMode> getDefaultOperatingModes(String devTypeHint, boolean supportsAuto) {

        if (devTypeHint.equalsIgnoreCase("nestthermostat")) {
            return Arrays.asList(ThermostatMode.AUTO, ThermostatMode.COOL, ThermostatMode.HEAT, ThermostatMode.ECO, ThermostatMode.OFF);
        } else if (supportsAuto) {
            return Arrays.asList(ThermostatMode.AUTO, ThermostatMode.COOL, ThermostatMode.HEAT, ThermostatMode.OFF);
        } else {
            return Arrays.asList(ThermostatMode.COOL, ThermostatMode.HEAT, ThermostatMode.OFF);
        }

    }

    // Should this be moved to the model?
    private FanMode getFanMode(Thermostat thermostat) {
        // TODO this capability isn't quite right
        if (thermostat.getFanmode() == null) return FanMode.OFF;

        if(thermostat.getFanmode() == 0) {
            return FanMode.OFF;
        }
        else if(thermostat.getAutofanspeed().equals(thermostat.getFanmode())) {
            return FanMode.AUTO;
        }
        else {
            return FanMode.ON;
        }
    }

    private boolean isFanBlowing(Thermostat thermostat) {
        return thermostat.getFanmode() != null && thermostat.getFanmode() != 0;
    }

    private boolean isRunning(Thermostat thermostat) {
        return Thermostat.ACTIVE_RUNNING.equals(thermostat.getActive());
    }

    public void updateFanMode(FanMode mode) {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        Thermostat thermostat = (Thermostat) model;
        thermostat.setFanmode(mode.equals(FanMode.ON) ? 1 : 0);

        model
              .commit()
              .onFailure(onError);
    }

    public boolean supportsAutoMode() {
        DeviceModel model = getDevice();
        if (model == null) {
            return true;
        }

        Boolean supportsAuto = (Boolean) model.get(Thermostat.ATTR_SUPPORTSAUTO);
        if (supportsAuto != null) { // If the supports auto is set
            return supportsAuto;
        }
        else { // Return true for everything other than Honeywell dev type hints
            return !HTCC.equals(model.getDevtypehint());
        }
    }

    public void updateMode(ThermostatMode mode) {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        eventMesssageMonitor.scheduleEvent(model.getId(), Thermostat.ATTR_HVACMODE);
        Thermostat thermostat = (Thermostat) model;
        thermostat.setHvacmode(mode.name());

        if(model.getDevtypehint().equals(DeviceControlType.HONEYWELLTCC)) {
            DebouncedRequest request = new DebouncedRequest(model);
            request.setCallbackHandler(this);
            request.setOnError(onError);
            debouncedRequestScheduler.schedule(SET_MODE_TASK, request);
        }
        else {
            model
                    .commit()
                    .onFailure(onError);
        }
    }

    public void updateCurrentSetPoint(int setPoint) { // Debounce(500ms)
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        Thermostat thermostat = (Thermostat) getDevice();
        if (ThermostatMode.COOL.name().equals(thermostat.getHvacmode())) {
            updateCoolSetPoint(setPoint);
        }
        else if (ThermostatMode.HEAT.name().equals(thermostat.getHvacmode())) {
            updateHeatSetPoint(setPoint);
        }
    }

    public void updateCoolSetPoint(int setPointInF) { // Debounce(500ms)
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        Thermostat thermostat = (Thermostat) model;
        if (!isValidSetPoint(model, setPointInF)) {
            return;
        }

        double coolSetPoint = TemperatureUtils.fahrenheitToCelsius(setPointInF);
        thermostat.setCoolsetpoint(coolSetPoint);
        eventMesssageMonitor.scheduleEvent(model.getId(), Thermostat.ATTR_COOLSETPOINT);
        DebouncedRequest request = new DebouncedRequest(model);
        request.setCallbackHandler(this);
        request.setOnError(onError);
        debouncedRequestScheduler.schedule(SET_POINT_TASK, request);
        updateView();
    }

    // Debounce(500ms)
    public void updateHeatSetPoint(int setPointInF) {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        Thermostat thermostat = (Thermostat) model;
        if (!isValidSetPoint(model, setPointInF)) {
            return;
        }

        thermostat.setHeatsetpoint(TemperatureUtils.fahrenheitToCelsius(setPointInF));
        eventMesssageMonitor.scheduleEvent(model.getId(), Thermostat.ATTR_HEATSETPOINT);
        DebouncedRequest request = new DebouncedRequest(model);
        request.setCallbackHandler(this);
        request.setOnError(onError);
        debouncedRequestScheduler.schedule(SET_POINT_TASK, request);
        updateView();
    }

    public void incrementCurrentSetPoint() { // Debounce(500ms)
        updateSetPointBy(1);
    }

    public void decrementCurrentSetPoint() { // Debounce(500ms)
        updateSetPointBy(-1);
    }

    private void updateSetPointBy(int byF) {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        Thermostat thermostat = (Thermostat) getDevice();
        if (thermostat.getCoolsetpoint() == null || thermostat.getHeatsetpoint() == null) {
            return;
        }
        int coolSetPoint = TemperatureUtils.roundCelsiusToFahrenheit(thermostat.getCoolsetpoint()) + byF;
        int heatSetPoint = TemperatureUtils.roundCelsiusToFahrenheit(thermostat.getHeatsetpoint()) + byF;

        if (ThermostatMode.COOL.name().equals(thermostat.getHvacmode())) {
            updateCoolSetPoint(coolSetPoint);
        }
        else if (ThermostatMode.HEAT.name().equals(thermostat.getHvacmode())) {
            updateHeatSetPoint(heatSetPoint);
        }
        else if (ThermostatMode.AUTO.name().equals(thermostat.getHvacmode())) {
            if (isValidSetPoint(model, coolSetPoint) && isValidSetPoint(model, heatSetPoint)) {
                thermostat.setCoolsetpoint(TemperatureUtils.fahrenheitToCelsius(coolSetPoint));
                thermostat.setHeatsetpoint(TemperatureUtils.fahrenheitToCelsius(heatSetPoint));

                DebouncedRequest request = new DebouncedRequest(model);
                request.setCallbackHandler(this);
                request.setOnError(onError);
                debouncedRequestScheduler.schedule(SET_POINT_TASK, request);
            }
        }
    }

    private boolean isValidSetPoint(DeviceModel model, int setPointValueInF) {
        return setPointValueInF >= getMinimumSetpointF(model) && setPointValueInF <= getMaximumSetpointF(model);
    }

    private int getMinimumSetpointF(DeviceModel model) {
        return MIN_SETPOINT_NOMINAL;
    }

    private int getMaximumSetpointF(DeviceModel model) {
        return MAX_SETPOINT_NOMINAL;
    }

    private void showError(Throwable throwable) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.onError(Errors.translate(throwable));
        }
    }

    @Override
    public void commitEvent() {
        if(callback != null) {
            callback.commandCommitted();
        }
    }

    public interface CommandCommittedCallback {
        void commandCommitted();
    }
}
