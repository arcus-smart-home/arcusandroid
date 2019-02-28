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
package arcus.cornea.device.fan;

import arcus.cornea.device.DeviceController;
import arcus.cornea.error.Errors;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.DebouncedRequest;
import arcus.cornea.utils.DebouncedRequestScheduler;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Fan;
import com.iris.client.capability.Switch;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;

/**
 * The set point methods are de-bounced, this means:
 *  - First update will go through immediately
 *  - Any following requests will be delayed until 500 ms after the last change
 *    have been received
 *  - No value changes will result in updates to the view until the final
 *    pending change has been sent
 */
public class FanController extends DeviceController<FanProxyModel> {
    private final DebouncedRequestScheduler scheduler;
    private static final int DEBOUCNCE_DELAY_MS = 500;

    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            showError(throwable);
        }
    });

    public static FanController newController(String deviceId, DeviceController.Callback callback) {
        ModelSource<DeviceModel> source = DeviceModelProvider.instance().getModel("DRIV:dev:" + deviceId);
        FanController controller = new FanController(source);
        controller.setCallback(callback);
        return controller;
    }

    FanController(ModelSource<DeviceModel> source) {
        super(source);
        listenForProperties(
            Device.ATTR_NAME,
            DeviceConnection.ATTR_STATE,
            Switch.ATTR_STATE,
            Fan.ATTR_DIRECTION,
            Fan.ATTR_SPEED,
            Fan.ATTR_MAXSPEED // can this change?  that would be weird
        );
        scheduler = new DebouncedRequestScheduler(DEBOUCNCE_DELAY_MS);
    }

    @Override
    protected FanProxyModel update(DeviceModel device) {
        FanProxyModel model = new FanProxyModel();
        model.setDeviceId(device.getId());
        model.setName(device.getName());
        model.setDeviceTypeHint(device.getDevtypehint());
        model.setOnline(DeviceConnection.STATE_ONLINE.equals(device.get(DeviceConnection.ATTR_STATE)));

        Fan fan = (Fan) device;
        model.setMaxFanSpeed(fan.getMaxSpeed());
        model.setFanSpeed(FanSpeed.valueOf(fan.getSpeed()));
        model.setOn(fan.getSpeed() > 0 && Switch.STATE_ON.equals(device.get(Switch.ATTR_STATE)));

        return model;
    }


    public void updateFanSpeed(FanSpeed fanspeed) {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        Fan fan = (Fan) model;
        if (fanspeed.getSpeed() > fan.getMaxSpeed()) {
            return;
        }

        if (fan.getSpeed() != fanspeed.getSpeed()) {
            fan.setSpeed(fanspeed.getSpeed());
            updateDebouncedRequest(Fan.ATTR_SPEED, model);
        }
    }

    public void turnFanOff() {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        Fan fan = (Fan) model;
        fan.setSpeed(0);

        try {
            Switch swit = Switch.class.cast(model);
            swit.setState(Switch.STATE_OFF);
        } catch (ClassCastException e) {
            // Nothing to do if device doesn't support switch capability
        }

        updateDebouncedRequest(Fan.ATTR_SPEED, model);
    }

    public void turnFanOn() {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        Fan fan = (Fan) model;
        fan.setSpeed(1);

        try {
            Switch swit = Switch.class.cast(model);
            swit.setState(Switch.STATE_ON);
        } catch (ClassCastException e) {
            // Nothing to do if device doesn't support switch capability
        }

        updateDebouncedRequest(Fan.ATTR_SPEED, model);
    }

    public void increaseSpeed() {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        Fan fan = (Fan) model;
        if (fan.getSpeed() < fan.getMaxSpeed()) {
            fan.setSpeed(fan.getSpeed() + 1);
            updateDebouncedRequest(Fan.ATTR_SPEED, model);
        }
    }

    public void decreaseSpeed() {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        Fan fan = (Fan) model;
        if (fan.getSpeed() > 0) {
            fan.setSpeed(fan.getSpeed() - 1);
            updateDebouncedRequest(Fan.ATTR_SPEED, model);
        }
    }

    private void showError(Throwable throwable) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.onError(Errors.translate(throwable));
        }
    }

    private void updateDebouncedRequest(String name, DeviceModel model) {
        DebouncedRequest newTask = new DebouncedRequest(model);
        newTask.setOnError(onError);
        scheduler.schedule(name, newTask);
    }
}
