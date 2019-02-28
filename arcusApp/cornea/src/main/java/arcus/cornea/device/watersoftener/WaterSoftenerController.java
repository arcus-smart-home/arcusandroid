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
package arcus.cornea.device.watersoftener;

import arcus.cornea.device.DeviceController;
import arcus.cornea.error.Errors;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.ConversionUtils;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.WaterSoftener;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;


public class WaterSoftenerController extends DeviceController<WaterSoftenerProxyModel> {
    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            showError(throwable);
        }
    });

    public static WaterSoftenerController newController(String deviceId, Callback callback) {
        ModelSource<DeviceModel> source = DeviceModelProvider.instance().getModel("DRIV:dev:" + deviceId);
        WaterSoftenerController controller = new WaterSoftenerController(source);
        controller.setCallback(callback);
        return controller;
    }

    WaterSoftenerController(ModelSource<DeviceModel> source) {
        super(source);
        listenForProperties(
            Device.ATTR_NAME,
            DeviceConnection.ATTR_STATE,
            WaterSoftener.ATTR_CURRENTSALTLEVEL
        );
    }

    @Override
    protected WaterSoftenerProxyModel update(DeviceModel device) {
        WaterSoftenerProxyModel model = new WaterSoftenerProxyModel();
        model.setDeviceId(device.getId());
        model.setName(device.getName());
        model.setDeviceTypeHint(device.getDevtypehint());
        model.setOnline(DeviceConnection.STATE_ONLINE.equals(device.get(DeviceConnection.ATTR_STATE)));

        WaterSoftener softener = (WaterSoftener) device;
        model.setSaltLevel(String.valueOf(ConversionUtils.waterSoftenerSaltLevel(softener)));
        return model;
    }

    private void showError(Throwable throwable) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.onError(Errors.translate(throwable));
        }
    }
}
