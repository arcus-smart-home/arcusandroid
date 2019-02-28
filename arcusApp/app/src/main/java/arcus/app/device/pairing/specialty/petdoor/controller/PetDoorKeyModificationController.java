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
package arcus.app.device.pairing.specialty.petdoor.controller;

import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.CapabilityUtils;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientEvent;
import com.iris.client.capability.PetDoor;
import com.iris.client.capability.PetToken;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.common.controller.FragmentController;
import arcus.app.common.utils.CorneaUtils;


public class PetDoorKeyModificationController extends FragmentController<PetDoorKeyModificationController.Callbacks> {

    public interface Callbacks {
        void onPetNameLoaded(String petName);
        void onSuccess();
        void onCorneaError(Throwable cause);
    }

    private final static PetDoorKeyModificationController instance = new PetDoorKeyModificationController();
    private PetDoorKeyModificationController () {}

    public static PetDoorKeyModificationController getInstance () { return instance; }

    public void loadPetName (String petDoorAddress, final int tokenId) {
        DeviceModelProvider.instance().getModel(petDoorAddress).load().onSuccess(Listeners.runOnUiThread(new Listener<DeviceModel>() {
            @Override
            public void onEvent(DeviceModel deviceModel) {
                CapabilityUtils capabilityUtils = new CapabilityUtils(deviceModel);
                fireOnPetNameLoaded((String) capabilityUtils.getInstanceValue(getInstanceNameForToken(deviceModel, tokenId), PetToken.ATTR_PETNAME));
            }
        })).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    public void setPetName (String petDoorAddress, final int tokenId, final String newPetName) {
        DeviceModelProvider.instance().getModel(petDoorAddress).load().onSuccess(new Listener<DeviceModel>() {
            @Override
            public void onEvent(DeviceModel deviceModel) {
                setPetName(deviceModel, tokenId, newPetName);
            }
        }).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    public void removeSmartKey (String petDoorAddress, final int tokenId) {
        DeviceModelProvider.instance().getModel(petDoorAddress).load().onSuccess(new Listener<DeviceModel>() {
            @Override
            public void onEvent(DeviceModel deviceModel) {
                removeSmartKey(deviceModel, tokenId);
            }
        }).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    private void removeSmartKey (DeviceModel device, int tokenId) {
        PetDoor petDoor = CorneaUtils.getCapability(device, PetDoor.class);
        petDoor.removeToken(tokenId).onSuccess(Listeners.runOnUiThread(new Listener<PetDoor.RemoveTokenResponse>() {
            @Override
            public void onEvent(PetDoor.RemoveTokenResponse removeTokenResponse) {
                fireOnSuccess();
            }
        })).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    private void setPetName (DeviceModel device, int tokenId, String newPetName) {
        CapabilityUtils capabilityUtils = new CapabilityUtils(device);
        capabilityUtils
                .setInstance(getInstanceNameForToken(device, tokenId))
                .attriubuteToValue(PetToken.ATTR_PETNAME, newPetName)
                .andSendChanges()
                .onSuccess(Listeners.runOnUiThread(new Listener<ClientEvent>() {
                    @Override
                    public void onEvent(ClientEvent clientEvent) {
                        fireOnSuccess();
                    }
                })).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    private String getInstanceNameForToken (DeviceModel deviceModel, int tokenId) {
        CapabilityUtils capabilityUtils = new CapabilityUtils(deviceModel);

        for (String thisInstance : capabilityUtils.getInstanceNames()) {
            if (tokenId == ((Number) capabilityUtils.getInstanceValue(thisInstance, PetToken.ATTR_TOKENID)).intValue()) {
                return thisInstance;
            }
        }

        throw new IllegalArgumentException("Bug! No pet door smart key token found with id " + tokenId);
    }

    private void fireOnPetNameLoaded(String petName) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onPetNameLoaded(petName);
        }
    }

    private void fireOnSuccess () {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onSuccess();
        }
    }

    private void fireOnCorneaError (Throwable cause) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onCorneaError(cause);
        }
    }

}
