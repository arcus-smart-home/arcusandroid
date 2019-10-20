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
package arcus.cornea.device.waterheater;

import arcus.cornea.device.DeviceController;
import arcus.cornea.error.Errors;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.DebouncedRequest;
import arcus.cornea.utils.DebouncedRequestScheduler;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import arcus.cornea.utils.TemperatureUtils;
import com.iris.client.capability.AOSmithWaterHeaterController;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.WaterHeater;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;

/**
 * The set point methods are de-bounced, this means:
 * - First update will go through immediately
 * - Any following requests will be delayed until 500 ms after the last change
 * have been received
 * - No value changes will result in updates to the view until the final
 * pending change has been sent
 */
public class WaterHeaterController extends DeviceController<WaterHeaterProxyModel> {
    private static final int DEBOUNCE_REQUEST_DELAY_MS = 500;
    private static final String SET_POINT_TASK = "SET_POINT";
    private int minTemp = 60;
    private int maxTemp = 150;
    private final DebouncedRequestScheduler debouncedRequestScheduler;

    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            updateView();
            showError(throwable);
        }
    });

    public static WaterHeaterController newController(String deviceId, Callback callback) {
        ModelSource<DeviceModel> source = DeviceModelProvider.instance().getModel("DRIV:dev:" + deviceId);
        WaterHeaterController controller = new WaterHeaterController(source);
        controller.setCallback(callback);
        return controller;
    }

    WaterHeaterController(ModelSource<DeviceModel> source) {
        super(source);
        listenForProperties(
                Device.ATTR_NAME,
                DeviceConnection.ATTR_STATE,
                WaterHeater.ATTR_HEATINGSTATE,
                WaterHeater.ATTR_HOTWATERLEVEL,
                WaterHeater.ATTR_SETPOINT,
                AOSmithWaterHeaterController.ATTR_CONTROLMODE
        );
        debouncedRequestScheduler = new DebouncedRequestScheduler(DEBOUNCE_REQUEST_DELAY_MS);
    }

    @Override
    protected WaterHeaterProxyModel update(DeviceModel device) {
        WaterHeater waterHeater = (WaterHeater) device;
        WaterHeaterProxyModel model = new WaterHeaterProxyModel();
        model.setDeviceId(device.getId());
        model.setName(device.getName());
        model.setDeviceTypeHint(device.getDevtypehint());
        model.setOnline(DeviceConnection.STATE_ONLINE.equals(device.get(DeviceConnection.ATTR_STATE)));
        model.setHotWaterLevel(waterHeater.getHotwaterlevel());
        model.setHeatingState(waterHeater.getHeatingstate());
        model.setSetPoint(TemperatureUtils.celsiusToFahrenheit(waterHeater.getSetpoint()));
        if (device instanceof AOSmithWaterHeaterController) {
            AOSmithWaterHeaterController aoWaterHeater = (AOSmithWaterHeaterController) device;
            model.setControlMode(aoWaterHeater.getControlmode());
            model.setTemperatureScale(aoWaterHeater.getUnits());
            model.setMinTemp(minTemp);
            maxTemp = (int) TemperatureUtils.celsiusToFahrenheit(waterHeater.getMaxsetpoint()).doubleValue();
            model.setMaxTemp(maxTemp);
        }

        return model;
    }

    public void updateMode(String mode) {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        if (model instanceof AOSmithWaterHeaterController) {
            AOSmithWaterHeaterController waterHeater = (AOSmithWaterHeaterController) model;
            waterHeater.setControlmode(mode);
            model.commit().onFailure(onError);
        }

    }

    public void updateCurrentSetPoint(int setPoint) { // Debounce(500ms)
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        updateSetPoint(setPoint);
    }

    private void updateSetPoint(int setPointInF) {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        WaterHeater waterHeater = (WaterHeater) model;

        double setPoint = TemperatureUtils.fahrenheitToCelsius(setPointInF);
        waterHeater.setSetpoint(setPoint);
        DebouncedRequest request = new DebouncedRequest(model);
        request.setOnError(onError);
        debouncedRequestScheduler.schedule(SET_POINT_TASK, request);
        updateView();
    }


    private int getDisplayedTemp() {
        WaterHeater waterHeater = (WaterHeater) getDevice();
        return TemperatureUtils.roundCelsiusToFahrenheit(waterHeater.getSetpoint());
    }


    public void incActiveProgress() {
        if (getDisplayedTemp() < maxTemp) {
            if (getDisplayedTemp() == 60) {
                updateSetPointBy(20);
            } else {
                updateSetPointBy(1);
            }
        }
    }

    public void decActiveProgress() {
        if (getDisplayedTemp() > minTemp) {
            if (getDisplayedTemp() == 80) {
                updateSetPointBy(-20);
            } else {
                updateSetPointBy(-1);
            }
        }
    }


    private void updateSetPointBy(int byF) {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        WaterHeater waterHeater = (WaterHeater) getDevice();

        int curSetPoint = TemperatureUtils.roundCelsiusToFahrenheit(waterHeater.getSetpoint()) + byF;

        updateSetPoint(curSetPoint);
    }

    private void showError(Throwable throwable) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.onError(Errors.translate(throwable));
        }
    }
}
