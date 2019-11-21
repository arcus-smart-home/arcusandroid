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
package arcus.cornea.device.blinds;

import androidx.annotation.StringDef;

import arcus.cornea.device.DeviceController;
import arcus.cornea.device.blinds.model.SomfyBlindsSettingModel;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.DeviceOta;
import com.iris.client.capability.Somfyv1;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class SomfyBlindsDeviceController extends DeviceController<SomfyBlindsSettingModel>{
    SomfyBlindsDeviceController.Callback callback;

    private final Listener<Throwable> errorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            updateError(throwable);
        }
    });

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({Somfyv1.TYPE_BLIND, Somfyv1.TYPE_SHADE})
    public @interface SomfyBlindType {}

    public interface Callback {
        void errorOccurred(Throwable throwable);
        void updateView();
    }

    public static SomfyBlindsDeviceController newController(String deviceId, Callback callback, DeviceController.Callback devcallback) {
        ModelSource<DeviceModel> source = DeviceModelProvider.instance().getModel("DRIV:dev:" + deviceId);
        SomfyBlindsDeviceController controller = new SomfyBlindsDeviceController(source, callback);
        controller.setCallback(devcallback);
        return controller;
    }

    public void setCallback(SomfyBlindsDeviceController.Callback callback) {
        this.callback = callback;
    }

    public void removeCallback() {
        this.callback = null;
        clearCallback();
    }

    //Sending in callback in the contructor so we can do the inital update for the view.
    SomfyBlindsDeviceController(ModelSource<DeviceModel> source, Callback cb) { // We don't use the cb?
        super(source);
        listenForProperties(Device.ATTR_NAME,
              DeviceConnection.ATTR_STATE,
              Somfyv1.ATTR_TYPE,
              Somfyv1.ATTR_REVERSED);
        if(getCallback() != null) {
            getCallback().show(update(getDevice()));
        }
    }

    @Override
    protected SomfyBlindsSettingModel update(DeviceModel device) {
        if(!isValidDevice(device)) {
            return new SomfyBlindsSettingModel(); // This crashes the fragment if there are no settings... currentstate etc.
        }

        SomfyBlindsSettingModel model = new SomfyBlindsSettingModel();
        model.setDeviceAddress(device.getAddress());
        model.setDeviceName(device.getName());
        model.setIsInOTA(DeviceOta.STATUS_INPROGRESS.equals(device.get(DeviceOta.ATTR_STATUS)));
        model.setIsOnline(!DeviceConnection.STATE_OFFLINE.equals(device.get(DeviceConnection.ATTR_STATE)));
        model.setType(String.valueOf(device.get(Somfyv1.ATTR_TYPE)));
        model.setReversed(Somfyv1.REVERSED_REVERSED.equals(device.get(Somfyv1.ATTR_REVERSED)));
        model.setCurrentstate(String.valueOf(device.get(Somfyv1.ATTR_CURRENTSTATE)));

        return model;
    }

    public void leftButtonAction() {
        updateOpenValue(true);
    }

    public void middleButtonAction() {
        updateOpenValue(false);
    }

    private void updateOpenValue(boolean bOpen) {
        DeviceModel device = getDevice();
        if(!isValidDevice(device)) {
            return;
        }

        boolean reversed = Somfyv1.REVERSED_REVERSED.equals(device.get(Somfyv1.ATTR_REVERSED));
        Somfyv1 mode = (Somfyv1) device;

        if(reversed) {
            bOpen = !bOpen;
        }

        if(bOpen) {
            mode.goToOpen().onFailure(errorListener);
        }
        else {
            mode.goToClosed().onFailure(errorListener);
        }
    }

    public void rightButtonAction() {
        DeviceModel device = getDevice();
        if(!isValidDevice(device)) {
            return;
        }

        Somfyv1 mode = (Somfyv1) device;
        mode.goToFavorite().onFailure(errorListener);
    }

    public void setType(@SomfyBlindType String type) {
        DeviceModel device = getDevice();
        if(!isValidDevice(device)) {
            return;
        }

        device.set(Somfyv1.ATTR_TYPE, type);
        device.commit().onFailure(errorListener);
    }

    public void setReversed(boolean reversed) {
        DeviceModel device = getDevice();
        if(!isValidDevice(device)) {
            return;
        }

        device.set(Somfyv1.ATTR_REVERSED, reversed ? Somfyv1.REVERSED_REVERSED : Somfyv1.REVERSED_NORMAL);
        device.commit().onFailure(errorListener);
    }

    public double getChannel() {
        DeviceModel device = getDevice();
        if(!isValidDevice(device)) {
            return -1;
        }

        Double channel = (Double) device.get(Somfyv1.ATTR_CHANNEL);
        return channel == null ? -1 : channel;
    }

    protected void updateError(Throwable throwable) {
        if (callback != null) {
            callback.errorOccurred(throwable);
        }
    }

    protected boolean isValidDevice(DeviceModel model) {
        return model != null && model instanceof Somfyv1;
    }
}
