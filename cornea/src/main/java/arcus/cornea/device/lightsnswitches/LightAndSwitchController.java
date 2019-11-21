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
package arcus.cornea.device.lightsnswitches;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.device.DeviceController;
import arcus.cornea.error.Errors;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.DebouncedClientRequest;
import arcus.cornea.utils.DebouncedRequestScheduler;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.capability.util.Addresses;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.IrisClient;
import com.iris.client.capability.Capability;
import com.iris.client.capability.ColorTemperature;
import com.iris.client.capability.Device;
import com.iris.client.capability.Dimmer;
import com.iris.client.capability.Switch;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Collection;
import java.util.Collections;

public class LightAndSwitchController extends DeviceController<LightSwitchProxyModel> {
    private static final int DEFAULT_TIMEOUT = 30_000;
    private static final int DEBOUNCE_REQUEST_DELAY_MS = 500;
    private final IrisClient irisClient;
    private final DebouncedRequestScheduler debouncedRequestScheduler;
    private final Listener<Throwable> onErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onError(throwable);
        }
    });
    private final Listener<ClientEvent> onSuccessListener = Listeners.runOnUiThread(new Listener<ClientEvent>() {
        @Override
        public void onEvent(ClientEvent clientEvent) {
            updateView();
        }
    });

    public static LightAndSwitchController newController(String idOrAddress, DeviceController.Callback<LightSwitchProxyModel> callback) {
        String id = Addresses.getId(idOrAddress);
        String address = Addresses.toObjectAddress(Device.NAMESPACE, id);
        ModelSource<DeviceModel> source = DeviceModelProvider.instance().getModel(address);

        LightAndSwitchController controller = new LightAndSwitchController(CorneaClientFactory.getClient(), source);
        controller.setCallback(callback);

        return controller;
    }

    @StringDef({Switch.STATE_ON, Switch.STATE_OFF})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SwitchState {}

    protected LightAndSwitchController(IrisClient client, ModelSource<DeviceModel> source) {
        super(source);
        listenForProperties(
              Dimmer.ATTR_BRIGHTNESS,
              ColorTemperature.ATTR_COLORTEMP,
              Switch.ATTR_STATE
        );
        this.irisClient = client;
        this.debouncedRequestScheduler = new DebouncedRequestScheduler(DEBOUNCE_REQUEST_DELAY_MS);
    }

    @Override
    protected LightSwitchProxyModel update(DeviceModel device) {
        LightSwitchProxyModel model = new LightSwitchProxyModel();

        Collection<String> caps = device.getCaps() == null ? Collections.<String>emptySet() : device.getCaps();
        model.setSupportsDim(caps.contains(Dimmer.NAMESPACE));
        model.setSupportsColorTemp(caps.contains(ColorTemperature.NAMESPACE));
        model.setSupportsOnOff(caps.contains(Switch.NAMESPACE));

        Number dim = (Number) device.get(Dimmer.ATTR_BRIGHTNESS);
        Number colorTemp = (Number) device.get(ColorTemperature.ATTR_COLORTEMP);
        model.setDimPercent(dim != null ? dim.intValue() : 0);
        model.setColorTemp(colorTemp != null ? colorTemp.intValue() : 0);
        model.setSwitchOn(Switch.STATE_ON.equals(String.valueOf(device.get(Switch.ATTR_STATE))));

        return model;
    }

    protected @NonNull Collection<String> getCaps() {
        DeviceModel model = getDevice();
        if (model == null || model.getCaps() == null) {
            return Collections.emptySet();
        }

        return model.getCaps();
    }

    public void turnSwitchOff() {
        toggleSwitchOnOff(Switch.STATE_OFF);
    }

    public void turnSwitchOn() {
        toggleSwitchOnOff(Switch.STATE_ON);
    }

    public void setDimPercent(int dimPercent) {
        if (!getCaps().contains(Dimmer.NAMESPACE)) {
            onError(new RuntimeException("Unsupported Operation - Dim Capability not present."));
            return;
        }

        if (dimPercent < 0) {
            dimPercent = 0;
        }

        if (dimPercent > 100) {
            dimPercent = 100;
        }

        updateAttributeRequest(Dimmer.ATTR_BRIGHTNESS, dimPercent);
    }

    public void setColorTemp(int colorTemp) {
        if (!getCaps().contains(ColorTemperature.NAMESPACE)) {
            onError(new RuntimeException("Unsupported Operation - Temp Capability not present."));
            return;
        }

        DeviceModel model = getDevice();
        Number minTemp = (Number) model.get(ColorTemperature.ATTR_MINCOLORTEMP);
        Number maxTemp = (Number) model.get(ColorTemperature.ATTR_MAXCOLORTEMP);
        if (minTemp != null && colorTemp < minTemp.intValue()) {
            colorTemp = minTemp.intValue();
        }

        if (maxTemp != null && colorTemp > maxTemp.intValue()) {
            colorTemp = maxTemp.intValue();
        }
        updateAttributeRequest(ColorTemperature.ATTR_COLORTEMP, colorTemp);
    }

    protected void toggleSwitchOnOff(@SwitchState String setToState) {
        if (!getCaps().contains(Switch.NAMESPACE)) {
            onError(new RuntimeException("Unsupported Operation - Switch Capability not present."));
            return;
        }

        updateAttributeRequest(Switch.ATTR_STATE, setToState);
    }

    protected void updateAttributeRequest(String key, Object value) {
        DeviceModel model = getDevice();
        if (model == null) {
            onError(new RuntimeException("Model is MIA; Unable to fulfill request."));
            return;
        }

        ClientRequest request = new ClientRequest();
        request.setRestfulRequest(false);
        request.setTimeoutMs(DEFAULT_TIMEOUT);
        request.setAddress(model.getAddress());
        request.setCommand(Capability.CMD_SET_ATTRIBUTES);
        request.setAttribute(key, value);

        DebouncedClientRequest debouncedRequest = new DebouncedClientRequest(irisClient, request);
        debouncedRequest.setOnError(onErrorListener);
        debouncedRequest.setOnSuccess(onSuccessListener);
        debouncedRequestScheduler.schedule(model.getAddress(), debouncedRequest);
    }

    protected void onError(Throwable throwable) {
        Callback callback = getCallback();
        if (callback == null) {
            return;
        }

        callback.onError(Errors.translate(throwable));
    }
}
