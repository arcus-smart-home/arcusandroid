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
package arcus.cornea.device.petdoor;

import arcus.cornea.device.DeviceController;
import arcus.cornea.error.Errors;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.DevicePower;
import com.iris.client.capability.DoorLock;
import com.iris.client.capability.PetDoor;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;

public class PetDoorController extends DeviceController<PetDoorProxyModel> {

    public static PetDoorController newController(String deviceId, DeviceController.Callback callback) {
        ModelSource<DeviceModel> source = DeviceModelProvider.instance().getModel("DRIV:dev:" + deviceId);
        PetDoorController controller = new PetDoorController(source);

        controller.setCallback(callback);
        return controller;
    }

    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            showError(throwable);
        }
    });

    PetDoorController(ModelSource<DeviceModel> source) {
        super(source);
        listenForProperties(
                PetDoor.ATTR_LOCKSTATE,
                PetDoor.ATTR_LASTLOCKSTATECHANGEDTIME,
                PetDoor.ATTR_DIRECTION,
                PetDoor.ATTR_LASTACCESSTIME,
                DeviceConnection.ATTR_STATE,
                DevicePower.ATTR_BATTERY,
                DevicePower.ATTR_SOURCE
        );
    }

    @Override
    protected PetDoorProxyModel update(DeviceModel device) {
        PetDoorProxyModel model = new PetDoorProxyModel();
        model.setDeviceModel(device);
        model.setDeviceId(device.getId());
        model.setName(device.getName());
        model.setDeviceTypeHint(device.getDevtypehint());

        PetDoor petDoor = (PetDoor) device;
        switch (petDoor.getLockstate()) {
            case PetDoor.LOCKSTATE_AUTO:
                model.setState(PetDoorProxyModel.State.AUTO);
                break;
            case PetDoor.LOCKSTATE_LOCKED:
                model.setState(PetDoorProxyModel.State.LOCKED);
                break;
            case PetDoor.LOCKSTATE_UNLOCKED:
                model.setState(PetDoorProxyModel.State.UNLOCKED);
                break;

        }
        model.setLastStateChange(petDoor.getLastlockstatechangedtime());
        model.setLastAccessTime(petDoor.getLastaccesstime());
        model.setDirection(petDoor.getDirection());
        model.setNumPetTokens(petDoor.getNumPetTokensSupported());
        model.setOnline(!DeviceConnection.STATE_OFFLINE.equals(device.get(DeviceConnection.ATTR_STATE)));

        return model;
    }

    public void lock() {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }
        DoorLock doorLock = (DoorLock) model;
        doorLock.setLockstate(PetDoorProxyModel.State.LOCKED.name());
        model.commit().onFailure(onError);
    }

    public void unlock() {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }
        DoorLock doorLock = (DoorLock) model;
        doorLock.setLockstate(PetDoorProxyModel.State.UNLOCKED.name());
        model.commit().onFailure(onError);
    }

    public void buzzIn() {
        DeviceModel model = getDevice();
        if (model == null) {
            return;
        }
        DoorLock doorLock = (DoorLock) model;
        doorLock.buzzIn().onFailure(onError);
    }

    private void showError(Throwable t) {
        Callback cb = getCallback();
        if (cb != null) {
            cb.onError(Errors.translate(t));
        }
    }
}
