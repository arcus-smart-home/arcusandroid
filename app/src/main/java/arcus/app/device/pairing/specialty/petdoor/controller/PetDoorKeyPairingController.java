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
import com.iris.client.capability.PetToken;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import arcus.app.common.controller.FragmentController;
import arcus.app.common.utils.CorneaUtils;

import java.beans.PropertyChangeEvent;


public class PetDoorKeyPairingController extends FragmentController<PetDoorKeyPairingController.Callbacks> {

    public interface Callbacks {
        void onSmartKeyPaired (String smartKeyInstanceName, int tokenId);
        void onCorneaError (Throwable cause);
    }

    private final static PetDoorKeyPairingController instance = new PetDoorKeyPairingController();

    private ListenerRegistration propertyChangeListener;

    private PetDoorKeyPairingController () {}
    public static PetDoorKeyPairingController getInstance () { return instance; }

    public void pairSmartKey (String petDoorAddress) {
        DeviceModelProvider.instance().getModel(petDoorAddress).load().onSuccess(new Listener<DeviceModel>() {
            @Override
            public void onEvent(DeviceModel deviceModel) {
                pairSmartKeysForDoor(deviceModel);
            }
        }).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    private void pairSmartKeysForDoor(final DeviceModel deviceModel) {

        if (propertyChangeListener != null) {
            propertyChangeListener.remove();
        }

        propertyChangeListener = deviceModel.addListener(Listeners.runOnUiThread(new Listener<PropertyChangeEvent>() {
            @Override
            public void onEvent(PropertyChangeEvent event) {
                // Look for a PetToken's "paired" attribute to become true
                if (CorneaUtils.isInstanceCapabilityUpdate(event) &&
                        PetToken.ATTR_PAIRED.equals(CorneaUtils.getInstancePropertyUpdateFullyQualifiedPropertyName(event)) &&
                        (boolean) event.getNewValue())
                {
                    String instanceName = CorneaUtils.getInstancePropertyUpdateInstanceName(event);
                    int tokenId = ((Number) new CapabilityUtils(deviceModel).getInstanceValue(instanceName, PetToken.ATTR_TOKENID)).intValue();

                    fireOnSmartKeyPaired(CorneaUtils.getInstancePropertyUpdateInstanceName(event), tokenId);
                }
            }
        }));
    }

    private void fireOnCorneaError (Throwable cause) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onCorneaError(cause);
        }
    }

    private void fireOnSmartKeyPaired (String smartKeyInstanceName, int tokenId) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onSmartKeyPaired(smartKeyInstanceName, tokenId);
        }
    }

}
