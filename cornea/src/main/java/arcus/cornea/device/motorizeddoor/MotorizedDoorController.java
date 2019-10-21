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
package arcus.cornea.device.motorizeddoor;

import arcus.cornea.device.DeviceController;
import arcus.cornea.error.Errors;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.MotorizedDoor;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;

import java.util.HashMap;
import java.util.Map;

public class MotorizedDoorController extends DeviceController<MotorizedDoorProxyModel> {

    public static MotorizedDoorController newController(String deviceId, DeviceController.Callback callback) {
        ModelSource<DeviceModel> source = DeviceModelProvider.instance().getModel("DRIV:dev:" + deviceId);
        MotorizedDoorController controller = new MotorizedDoorController(source);
        controller.setCallback(callback);
        return controller;
    }

    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            showError(throwable);
        }
    });

    MotorizedDoorController(ModelSource<DeviceModel> source) {
        super(source);
        listenForProperties(
                MotorizedDoor.ATTR_DOORSTATE,
                DeviceAdvanced.ATTR_ERRORS,
                DeviceConnection.ATTR_STATE
        );
    }

    @Override
    protected MotorizedDoorProxyModel update(DeviceModel device) {
        MotorizedDoorProxyModel model = new MotorizedDoorProxyModel();
        model.setDeviceId(device.getId());
        model.setName(device.getName());
        model.setDeviceTypeHint(device.getDevtypehint());

        MotorizedDoor motorizedDoor = (MotorizedDoor) device;

        model.setErrors(getErrors(device));

        model.setLastStateChange(motorizedDoor.getDoorstatechanged());
        if(motorizedDoor.getDoorstate() != null) {
            model.setState(MotorizedDoorProxyModel.State.valueOf(motorizedDoor.getDoorstate()));
        }
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

    public void openDoor() {
        DeviceModel model = getDevice();
        if(model == null) {
            return;
        }
        MotorizedDoor motorizedDoor = (MotorizedDoor) model;
        motorizedDoor.setDoorstate(MotorizedDoorProxyModel.State.OPEN.name());
        model.commit().onFailure(onError);
    }

    public void closeDoor() {
        DeviceModel model = getDevice();
        if(model == null) {
            return;
        }
        MotorizedDoor motorizedDoor = (MotorizedDoor) model;
        motorizedDoor.setDoorstate(MotorizedDoorProxyModel.State.CLOSED.name());
        model.commit().onFailure(onError);
    }

    private void showError(Throwable t) {
        Callback cb = getCallback();
        if(cb != null) {
            cb.onError(Errors.translate(t));
        }
    }
}
