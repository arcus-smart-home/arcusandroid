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
package arcus.app.device.details.presenters;

import arcus.cornea.common.BasePresenter;
import arcus.cornea.common.PresentedView;
import arcus.cornea.device.blinds.ShadeDeviceController;
import arcus.cornea.device.blinds.model.ShadeClientModel;
import arcus.cornea.provider.DeviceModelProvider;
import com.iris.client.capability.DevicePower;
import com.iris.client.capability.Shade;
import com.iris.client.model.DeviceModel;


/**
 * ShadePresenter returns state information about the Shade device provided by the deviceAddress
 *  updates the requesting UI element via {@link PresentedView#updateView(Object)}.
 */

public class ShadePresenter extends BasePresenter<ShadeContract.ShadeView> implements ShadeContract.ShadePresenter,
        ShadeDeviceController.Callback {

    private final ShadeDeviceController controller;

    public ShadePresenter(String deviceAddress) {
        controller = new ShadeDeviceController(DeviceModelProvider.instance().getModel(deviceAddress == null ? "DRIV:dev:" : deviceAddress));
        controller.setCallback(this);
    }

    @Override
    public void requestUpdate() {
        if(isPresenting()) {
            getPresentedView().onPending(null);
            controller.requestUpdate();
        }
    }

    public void setLevel(int level) {
        controller.setLevel(level);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onSuccess(DeviceModel deviceModel) {
        if(isPresenting()) {
            ShadeClientModel model = new ShadeClientModel();
            Shade shade = (Shade)deviceModel;

            model.setDeviceName(deviceModel.getName());
            model.setDeviceAddress(deviceModel.getAddress());
            if(shade.getLevel() != null) {
                model.setLevel(shade.getLevel());
            }
            model.setBatteryLevel(numberOrNull(deviceModel.get(DevicePower.ATTR_BATTERY)));
            getPresentedView().updateView(model);
        }
    }
}
