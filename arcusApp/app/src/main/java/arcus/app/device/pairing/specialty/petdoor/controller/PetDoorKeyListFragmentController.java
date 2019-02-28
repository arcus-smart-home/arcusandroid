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
import com.iris.client.model.DeviceModel;
import arcus.app.common.controller.FragmentController;

import java.util.ArrayList;
import java.util.List;


public class PetDoorKeyListFragmentController extends FragmentController<PetDoorKeyListFragmentController.Callbacks> {

    private final static PetDoorKeyListFragmentController instance = new PetDoorKeyListFragmentController();

    public interface Callbacks {
        void onKeyListItemsLoaded (List<PetDoorSmartKey> items);
        void onCorneaError (Throwable cause);
    }

    public static class PetDoorSmartKey {
        public final String petName;
        public final int tokenId;

        public PetDoorSmartKey (String petName, int tokenId) {
            this.petName = petName;
            this.tokenId = tokenId;
        }
    }

    private PetDoorKeyListFragmentController () {}
    public static PetDoorKeyListFragmentController getInstance() { return instance; }

    public void loadKeyListItems (String petDoorAddress) {
        DeviceModelProvider.instance().getModel(petDoorAddress).load().onSuccess(Listeners.runOnUiThread(new Listener<DeviceModel>() {
            @Override
            public void onEvent(DeviceModel deviceModel) {
                loadSmartKeysForDoor(deviceModel);
            }
        })).onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorneaError(throwable);
            }
        }));
    }

    private void loadSmartKeysForDoor (DeviceModel petDoor) {
        ArrayList<PetDoorSmartKey> listItems = new ArrayList<>();

        CapabilityUtils capabilityUtils = new CapabilityUtils(petDoor);
        for (String thisInstance : capabilityUtils.getInstanceNames()) {
            if ((boolean) capabilityUtils.getInstanceValue(thisInstance, PetToken.ATTR_PAIRED)) {
                String petName = (String) capabilityUtils.getInstanceValue(thisInstance, PetToken.ATTR_PETNAME);
                int tokenId = ((Number) capabilityUtils.getInstanceValue(thisInstance, PetToken.ATTR_TOKENID)).intValue();

                listItems.add(new PetDoorSmartKey(petName, tokenId));
            }
        }

        fireOnKeyListItemsLoaded(listItems);
    }

    private void fireOnKeyListItemsLoaded(List<PetDoorSmartKey> listItems) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onKeyListItemsLoaded(listItems);
        }
    }

    private void fireOnCorneaError (Throwable cause) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onCorneaError(cause);
        }
    }

}
