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
package arcus.cornea.device.valve;

import arcus.cornea.device.DeviceController;

import arcus.cornea.error.Errors;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Valve;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;


public class ValveController extends DeviceController<ValveProxyModel> {
    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            showError(throwable);
        }
    });

    public static ValveController newController(String deviceId, Callback callback) {
        ModelSource<DeviceModel> source = DeviceModelProvider.instance().getModel("DRIV:dev:" + deviceId);
        ValveController controller = new ValveController(source);
        controller.setCallback(callback);
        return controller;
    }

    ValveController(ModelSource<DeviceModel> source) {
        super(source);
        listenForProperties(
            Device.ATTR_NAME,
            DeviceConnection.ATTR_STATE,
            Valve.ATTR_VALVESTATE
        );
    }

    @Override
    protected ValveProxyModel update(DeviceModel device) {
        ValveProxyModel model = new ValveProxyModel();
        model.setDeviceId(device.getId());
        model.setName(device.getName());
        model.setDeviceTypeHint(device.getDevtypehint());
        model.setOnline(DeviceConnection.STATE_ONLINE.equals(device.get(DeviceConnection.ATTR_STATE)));

        Valve valve = (Valve) device;
        if(valve.getValvestate() != null) {
            model.setValveState(valve.getValvestate());
        }
        return model;
    }


    public void setValveState(boolean open) {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }

        Valve valve = (Valve) model;
        if(open) {
            valve.setValvestate(Valve.VALVESTATE_OPEN);
        }
        else {
            valve.setValvestate(Valve.VALVESTATE_CLOSED);
        }

        model.commit().onFailure(onError);
        updateView();
    }

    private void showError(Throwable throwable) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.onError(Errors.translate(throwable));
        }
    }
}
