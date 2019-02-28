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
package arcus.app.subsystems.homenfamily.controllers;

import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.presence.model.PresenceModel;
import arcus.cornea.subsystem.presence.model.PresenceState;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.Presence;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.controller.FragmentController;
import arcus.app.common.models.PicListItemModel;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.StringUtils;
import arcus.app.subsystems.people.model.PersonTag;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class HomeNFamilyFragmentController extends FragmentController<HomeNFamilyFragmentController.Callbacks> {

    public interface Callbacks {
        void onPicListItemModelsLoaded(List<PicListItemModel> picListItems, PresenceTag tag);

        void onCorneaError(Throwable cause);
    }

    public enum PresenceTag {
        HOME, AWAY, ALL
    }

    private final static Logger logger = LoggerFactory.getLogger(HomeNFamilyFragmentController.class);
    private final static HomeNFamilyFragmentController instance = new HomeNFamilyFragmentController();

    private HomeNFamilyFragmentController() {
    }

    public static HomeNFamilyFragmentController getInstance() {
        return instance;
    }

    public void getPicListItemsForPresence(final List<PresenceModel> presenceModels, final PresenceTag tag) {
        logger.debug("Building {} presence items for {} models: {}", tag, presenceModels.size(), presenceModels);

        DeviceModelProvider.instance().addStoreLoadListener(new Listener<List<DeviceModel>>() {
            @Override
            public void onEvent(List<DeviceModel> deviceModels) {
                fireOnPicListItemModelsLoaded(buildPicListItemModels(presenceModels, deviceModels), tag);
            }
        });

        DeviceModelProvider.instance().load().onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                fireOnCorenaError(throwable);
            }
        }));
    }

    private List<PicListItemModel> buildPicListItemModels(List<PresenceModel> models, List<DeviceModel> deviceModels) {

        List<PicListItemModel> picListItemModels = new ArrayList<>();

        for (PresenceModel thisPresence : models) {
            DeviceModel thisDevice = findDeviceForPresence(thisPresence, deviceModels);

            if (thisDevice != null) {
                String deviceName = thisDevice.getName();
                String firstName = thisPresence.getFirstName();
                String lastName = thisPresence.getLastName();
                String relationship = thisPresence.getRelationship();
                String assignedTo = thisPresence.getPersonId();
                PresenceState homeAwayState = thisPresence.getState();

                picListItemModels.add(new PicListItemModel(deviceName, firstName, lastName, relationship, assignedTo, thisDevice, homeAwayState));
            }
        }

        Collections.sort(picListItemModels, ORDER);
        return picListItemModels;
    }

    private DeviceModel findDeviceForPresence(PresenceModel presenceModel, List<DeviceModel> deviceModels) {
        if (!StringUtils.isEmpty(presenceModel.getDeviceId())) {
            return findDeviceWithId(presenceModel, deviceModels);
        } else if (!StringUtils.isEmpty(presenceModel.getPersonId())) {
            return findDeviceAssignedToPerson(presenceModel, deviceModels);
        } else {
            logger.error("Bug! Inconsistent data. Presence model refers to neither device nor person: {}", presenceModel);
            return null;
        }
    }

    private DeviceModel findDeviceAssignedToPerson(PresenceModel presenceModel, List<DeviceModel> deviceModels) {
        for (DeviceModel thisModel : deviceModels) {
            Presence presence = CorneaUtils.getCapability(thisModel, Presence.class);

            logger.trace("Looking for device assigned to person {} ({}) in: device={}; has presence={}; assigned person={}", getPersonDisplayName(presenceModel), presenceModel.getPersonId(), thisModel.getName(), presence != null, presence != null ? presence.getPerson() : "N/A");

            if (presence != null &&
                    presence.getPerson() != null &&
                    CorneaUtils.isAddress(presence.getPerson()) &&
                    CorneaUtils.getIdFromAddress(presence.getPerson()).equals(presenceModel.getPersonId())) {
                return thisModel;
            }
        }

        logger.error("Bug! Presence indicated for person {} ({}), but no device found assigned to that person (in {} loaded models).", getPersonDisplayName(presenceModel), presenceModel.getPersonId(), deviceModels.size());
        return null;
    }

    private DeviceModel findDeviceWithId(PresenceModel presenceModel, List<DeviceModel> deviceModels) {
        for (DeviceModel thisModel : deviceModels) {
            if (thisModel.getId().equals(presenceModel.getDeviceId())) {
                return thisModel;
            }
        }

        logger.error("Bug! Presence indicated for device id={}, but no device found with that id (in {} loaded models).", presenceModel.getDeviceId(), deviceModels.size());
        return null;
    }

    private String getPersonDisplayName(PresenceModel model) {
        return model.getFirstName() + " " + model.getLastName();
    }

    private String getRelationshipDisplayString(String relationshipTag) {

        // Empty or null tag means account holder
        if (StringUtils.isEmpty(relationshipTag)) {
            return ArcusApplication.getContext().getString(R.string.people_account_holder);
        }

        // Person is tagged; determine relationship string
        else {
            try {
                return ArcusApplication.getContext().getString(PersonTag.valueOf(relationshipTag.toUpperCase()).getStringResId());
            } catch (IllegalArgumentException e) {
                // TODO: What to do here? Tag value cannot be mapped to a known relationship tag... using "OTHER" for the time being
                return ArcusApplication.getContext().getString(R.string.people_other);
            }
        }
    }

    private void fireOnCorenaError(Throwable cause) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onCorneaError(cause);
        }
    }

    private void fireOnPicListItemModelsLoaded(List<PicListItemModel> picListItemModels, PresenceTag tag) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onPicListItemModelsLoaded(picListItemModels, tag);
        }
    }

    private static final Comparator<PicListItemModel> ORDER = new Comparator<PicListItemModel>() {
        @Override
        public int compare(PicListItemModel firstModel, PicListItemModel secondModel) {

            return ObjectUtils.compare(firstModel.getDeviceName(), secondModel.getDeviceName());
        }
    };
}
