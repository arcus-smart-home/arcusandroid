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
package arcus.cornea.device.doorlock;

import arcus.cornea.device.DeviceController;
import arcus.cornea.error.Errors;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.DoorLock;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;

import java.util.HashMap;
import java.util.Map;

public class DoorLockController extends DeviceController<DoorLockProxyModel> {

    public static DoorLockController newController(String deviceId, DeviceController.Callback callback) {
        ModelSource<DeviceModel> source = DeviceModelProvider.instance().getModel("DRIV:dev:" + deviceId);
        DoorLockController controller = new DoorLockController(source);
        controller.setCallback(callback);
        return controller;
    }

    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            showError(throwable);
        }
    });

    DoorLockController(ModelSource<DeviceModel> source) {
        super(source);
        listenForProperties(
                DoorLock.ATTR_LOCKSTATE,
                DeviceConnection.ATTR_STATE,
                DeviceAdvanced.ATTR_ERRORS
        );
    }

    @Override
    protected DoorLockProxyModel update(DeviceModel device) {
        DoorLockProxyModel model = new DoorLockProxyModel();
        model.setDeviceId(device.getId());
        model.setName(device.getName());
        model.setDeviceTypeHint(device.getDevtypehint());
        model.setErrors(getErrors(device));

        DoorLock doorLock = (DoorLock) device;
        String type = (String) device.get(DoorLock.ATTR_TYPE);

        if(type != null) {
            model.setType(DoorLockProxyModel.Type.valueOf(type));
        }
        if(doorLock.getLockstate() != null) {
            model.setState(DoorLockProxyModel.State.valueOf(doorLock.getLockstate()));
        }
        model.setLastStateChange(doorLock.getLockstatechanged());
        model.setNumPinsSupported(doorLock.getNumPinsSupported());
        model.setSupportsBuzzIn(doorLock.getSupportsBuzzIn());
        model.setOnline(!DeviceConnection.STATE_OFFLINE.equals(device.get(DeviceConnection.ATTR_STATE)));

        return model;
    }

    private Map<String, String> getErrors(DeviceModel device) {
        Map<String, String> errorMap = new HashMap<>();

        if (device != null && ((DeviceAdvanced)device).getErrors() != null) {
            errorMap = ((DeviceAdvanced)device).getErrors();
        }

        return errorMap;
    }

    public void lock() {
        DeviceModel model = getDevice();
        if(model == null) {
            return;
        }
        DoorLock doorLock = (DoorLock) model;
        doorLock.setLockstate(DoorLockProxyModel.State.LOCKED.name());
        model.commit().onFailure(onError);
    }

    public void unlock() {
        DeviceModel model = getDevice();
        if(model == null) {
            return;
        }
        DoorLock doorLock = (DoorLock) model;
        doorLock.setLockstate(DoorLockProxyModel.State.UNLOCKED.name());
        model.commit().onFailure(onError);
    }

    public void buzzIn() {
        DeviceModel model = getDevice();
        if(model == null) {
            return;
        }
        DoorLock doorLock = (DoorLock) model;
        doorLock.buzzIn().onFailure(onError);
    }

    private void showError(Throwable t) {
        Callback cb = getCallback();
        if(cb != null) {
            cb.onError(Errors.translate(t));
        }
    }
}
