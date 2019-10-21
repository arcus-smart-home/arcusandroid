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
package arcus.cornea.device.vent;

import arcus.cornea.device.DeviceController;
import arcus.cornea.error.Errors;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.DebouncedRequest;
import arcus.cornea.utils.DebouncedRequestScheduler;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Vent;
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
public class VentController extends DeviceController<VentModel> {
    private final DebouncedRequestScheduler scheduler;
    private static final int DEBOUCNCE_DELAY_MS = 500;

    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            showError(throwable);
        }
    });

    public static VentController newController(String deviceId, Callback callback) {
        ModelSource<DeviceModel> source = DeviceModelProvider.instance().getModel("DRIV:dev:" + deviceId);
        VentController controller = new VentController(source);
        controller.setCallback(callback);
        return controller;
    }

    VentController(ModelSource<DeviceModel> source) {
        super(source);
        listenForProperties(
                Device.ATTR_NAME,
                DeviceConnection.ATTR_STATE,
                Vent.ATTR_LEVEL
        );
        scheduler = new DebouncedRequestScheduler(DEBOUCNCE_DELAY_MS);
    }

    @Override
    protected VentModel update(DeviceModel device) {
        VentModel model = new VentModel();
        model.setDeviceId(device.getId());
        model.setName(device.getName());
        model.setDeviceTypeHint(device.getDevtypehint());
        model.setOnline(DeviceConnection.STATE_ONLINE.equals(device.get(DeviceConnection.ATTR_STATE)));

        Vent vent = (Vent) device;
        model.setClosed(vent.getLevel() == 0);
        model.setOpenPercent(vent.getLevel());
        return model;
    }

    public void openVent(int openPercent) {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }
        Vent vent = (Vent) model;
        vent.setLevel(openPercent);
        model.commit().onFailure(onError);
    }

    public void openVent() {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        Vent vent = (Vent) model;
        Integer current = 1000;
        if(vent.getLevel() != null) {
            current = vent.getLevel();
        }

        if (current < 100) {
            Integer updateValue = current % 10 != 0 ? 10 - (current % 10)  : 10;
            vent.setLevel(current + updateValue);
            updateDebouncedRequest(Vent.ATTR_LEVEL, model);
            updateView();
        }
    }

    public void closeVent() {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        Vent vent = (Vent) model;
        Integer current = vent.getLevel();

        if (current > 0) {
            Integer updateValue = current % 10 != 0 ? current % 10 : 10;
            vent.setLevel(current - updateValue);
            updateDebouncedRequest(Vent.ATTR_LEVEL, model);
            updateView();
        }
    }

    // Debounce(500ms)
    public void increaseOpenPercent() {
        final DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        final Vent vent = (Vent) model;
        if (vent.getLevel() == 100) {
            return;
        }
        vent.setLevel(vent.getLevel() + 1);
        updateDebouncedRequest(Vent.ATTR_LEVEL, model);
    }

    // Debounce(500ms)
    public void decreaseOpenPercent() {
        final DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        final Vent vent = (Vent) model;
        if (vent.getLevel() == 0) {
            return;
        }
        vent.setLevel(vent.getLevel() - 1);
        updateDebouncedRequest(Vent.ATTR_LEVEL, model);
    }



    public void setDebouncePercent(int nLevel) {
        final DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        final Vent vent = (Vent) model;
        if (vent.getLevel() == 0) {
            return;
        }
        vent.setLevel(nLevel);
        updateDebouncedRequest(Vent.ATTR_LEVEL, model);
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
