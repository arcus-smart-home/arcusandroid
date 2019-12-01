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
package arcus.app.common.utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Strings;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.dto.HubDeviceModelDTO;
import arcus.cornea.provider.PersonModelProvider;
import arcus.cornea.provider.PlaceModelProvider;
import arcus.cornea.provider.ProductModelProvider;
import arcus.cornea.subsystem.safety.model.HistoryEvent;
import com.iris.capability.util.Addresses;
import com.iris.client.bean.HistoryLog;
import com.iris.client.capability.Account;
import com.iris.client.capability.BridgeChild;
import com.iris.client.capability.Camera;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.DeviceOta;
import com.iris.client.capability.HubAdvanced;
import com.iris.client.capability.Place;
import com.iris.client.capability.Thermostat;
import com.iris.client.model.AccountModel;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.Model;
import com.iris.client.model.PersonModel;
import com.iris.client.model.PlaceModel;
import com.iris.client.model.ProductModel;
import com.iris.client.service.SessionService;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.models.SessionModelManager;
import arcus.app.subsystems.alarm.promonitoring.models.ProMonitoringHistoryItem;
import arcus.app.subsystems.people.model.PersonRelationshipFamilyTag;
import arcus.app.subsystems.people.model.PersonRelationshipServiceTag;
import arcus.app.subsystems.people.model.PersonRelationshipTag;
import arcus.app.subsystems.people.model.PersonTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class CorneaUtils {

    private final static Logger logger = LoggerFactory.getLogger(CorneaUtils.class);

    public static <T extends Capability> boolean hasCapability (Model deviceModel, @NonNull Class<T> capability) {
        return getCapability(deviceModel, capability) != null;
    }

    public static <T extends Capability> T getCapability (Model deviceModel, @NonNull Class<T> capability) {
        try {
            return capability.cast(deviceModel);
        } catch (ClassCastException e) {
            return null;
        }
    }

    @SuppressWarnings("ConstantConditions")
    public static boolean isThermostatDevice(@Nullable DeviceModel deviceModel) {
        if (deviceModel == null) {
            return false;
        }

        Collection<String> caps = deviceModel.getCaps();
        return (caps != null && !caps.isEmpty()) &&
              (caps.contains(Thermostat.NAMESPACE));
    }

    @Nullable
    public static Comparator<DeviceModel> deviceModelComparator = new Comparator<DeviceModel>() {
        @Override
        public int compare(DeviceModel lhs, DeviceModel rhs) {
            if (lhs instanceof HubDeviceModelDTO) {
                return -1;
            }
            else if (rhs instanceof HubDeviceModelDTO) {
                return 1;
            }

            // Don't blow up on these, but push them to the bottom of the list.
            if (lhs.getName() == null) {
                return 1;
            }
            else if (rhs.getName() == null) {
                return -1;
            }
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    };

    /**
     * Determines the relationship of this person to the account holder.
     *
     * TODO: Assumes that a PersonModel has no more than one tag (representing the relationship).
     *
     * @param personModel
     * @return
     */
    public static String getPersonRelationship(@Nullable PersonModel personModel) {
        if (isPersonAccountHolder(personModel)) {
            return ArcusApplication.getContext().getString(R.string.people_account_holder);
        }
        else {
            if (personModel == null || personModel.getTags() == null || personModel.getTags().isEmpty()) {
                return ArcusApplication.getContext().getString(R.string.people_other);
            }

            String taggedWith = String.valueOf(personModel.getTags().iterator().next()).toUpperCase();
            taggedWith = taggedWith.replace("/PARTNER", ""); // iOS was sending Spouse/Partner for the tag, this removes that so we can identify that tag.
            taggedWith = taggedWith.replace("ROOMATE", "ROOMMATE"); // Previously had a misspelling

            taggedWith = taggedWith.replace("(", "");
            taggedWith = taggedWith.replace(")", "");
            taggedWith = taggedWith.replace(" ", "");

            boolean bFoundInEnums = false;
            String returnValue = "";
            try {
                returnValue = ArcusApplication.getContext().getString(PersonRelationshipTag.valueOf(taggedWith).getStringResId());
                bFoundInEnums = true;
            } catch (Exception ignored) {
            }

            try {
                returnValue = ArcusApplication.getContext().getString(PersonRelationshipFamilyTag.valueOf(taggedWith).getStringResId());
                bFoundInEnums = true;
            } catch (Exception ignored) {
            }

            try {
                returnValue = ArcusApplication.getContext().getString(PersonRelationshipServiceTag.valueOf(taggedWith).getStringResId());
                bFoundInEnums = true;
            } catch (Exception ignored) {
            }

            //Supporting old lookup
            try {
                returnValue = ArcusApplication.getContext().getString(PersonTag.valueOf(taggedWith).getStringResId());
                bFoundInEnums = true;
            } catch (Exception ignored) {
            }

            if(!bFoundInEnums) {
                returnValue = String.valueOf(personModel.getTags().iterator().next());
            }
            return returnValue;
        }
    }

    public static String getPersonRelationship(@NonNull String personIDorAddress) {
        return getPersonRelationship(
              PersonModelProvider.instance().getStore().get(Addresses.getId(personIDorAddress))
        );
    }

    /**
     * Determines if the given person is the account holder.
     *
     * @param personModel
     * @return
     */
    public static boolean isPersonAccountHolder(@Nullable PersonModel personModel) {
        PersonModel accountHolder = getAccountHolder();
        return personModel != null && accountHolder != null && personModel.getId().equalsIgnoreCase(accountHolder.getId());
    }

    public static boolean isHobbit (@NonNull PersonModel personModel) {
        return !personModel.getHasLogin();
    }

    public static PersonModel getAccountHolder() {
        PlaceModel placeModel = PlaceModelProvider.getCurrentPlace().get();
        if (placeModel == null) {
            logger.error("Can't get account holder because the current place couldn't be retrieved.");
            return null;
        }

        String accountAddress = Addresses.toObjectAddress(Account.NAMESPACE, placeModel.getAccount());
        Model accountModel =  CorneaClientFactory.getModelCache().get(accountAddress);
        if (accountModel == null || !(accountModel instanceof AccountModel)) {
            logger.error("Can't get account holder because the current account couldn't be retrieved.");
            return null;
        }

        String ownerID = Addresses.getId(String.valueOf(((AccountModel) accountModel).getOwner()));
        PersonModel accountHolder = PersonModelProvider.instance().getStore().get(ownerID);

        if (accountHolder == null) {
            logger.error("Can't get account holder because a person model for id doesn't exist: " + ownerID);
        }

        return accountHolder;
    }

    /**
     * Takes either an entity address or id and returns the ID portion. If the input contains an
     * address, the address portion will be removed and the ID will be returned. Any other input
     * will be returned without modification.
     *
     * @param identifier
     * @return
     */
    public static String getNormalizedId(@NonNull String identifier) {
        if (identifier == null || "".equals(identifier) || !isAddress(identifier)) {
            return identifier;
        }

        String[] identifierSplit = identifier.split(":");
        if (identifierSplit.length != 3) {
            return identifier;
        }

        if (identifier.endsWith("hub")) {
            return identifierSplit[1];
        }
        else {
            return identifierSplit[2];
        }
    }

    /**
     * Determines if the input matches the entity address format.
     * @param identifier
     * @return
     */
    public static boolean isAddress (String identifier) {
        return identifier != null && identifier.matches("^.{4}:.*:.*");
    }

    public static boolean isHubAddress (@NonNull String identifier) {
        return isAddress(identifier) && identifier.endsWith(":hub");
    }

    public static boolean isDeviceAddress (@NonNull String identifier) {
        return isAddress(identifier) && identifier.contains(":dev:");
    }

    public static boolean isPersonAddress (@NonNull String identifier) {
        return isAddress(identifier) && identifier.contains("SERV:person:");
    }

    public static boolean isSecuritySubsystemAddress (@NonNull String identifier) {
        return isAddress(identifier) && identifier.contains("SERV:subsecurity:");
    }

    public static boolean isDoorsNLocksSubsystemAddress (@NonNull String identifier) {
        return isAddress(identifier) && identifier.contains("SERV:subdoorsnlocks:");
    }

    public static boolean isClimateSubsystemAddress (@NonNull String identifier) {
        return isAddress(identifier) && identifier.contains("SERV:subclimate:");
    }

    public static boolean isLightsNSwitchesSubsystemAddress (@NonNull String identifier) {
        return isAddress(identifier) && identifier.contains("SERV:sublightsnswitches:");
    }

    public static boolean isSafetySubsystemAddress (@NonNull String identifier) {
        return isAddress(identifier) && identifier.contains("SERV:subsafety:");
    }

    public static boolean isHomeNFamilySubsystemAddress (@NonNull String identifier) {
        return isAddress(identifier) && identifier.contains("SERV:pres:");
    }

    public static boolean isCareSubsystemAddress (@NonNull String identifier) {
        return isAddress(identifier) && identifier.contains("SERV:subcare:");
    }

    public static boolean isWaterSubsystemAddress (@NonNull String identifier) {
        return isAddress(identifier) && identifier.contains("SERV:subwater:");
    }

    public static boolean isCameraSubsystemAddress (@NonNull String identifier) {
        return isAddress(identifier) && identifier.contains("SERV:subcameras:");
    }

    public static boolean isLawnNGardenSubsystemAddress (@NonNull String identifier) {
        return isAddress(identifier) && identifier.contains("SERV:sublawnngarden:");
    }

    public static String getIdFromAddress (@NonNull String address) {
        return !isAddress(address) ? null : address.split(":")[2];
    }

    public static final Comparator<HistoryLog> DESC_HISTORY_LOG_COMPARATOR = new Comparator<HistoryLog>() {
        @Override
        public int compare(@NonNull HistoryLog lhs, @NonNull HistoryLog rhs) {
            return rhs.getTimestamp().compareTo(lhs.getTimestamp());
        }
    };

    public static final Comparator<ProMonitoringHistoryItem> DESC_ALARM_HISTORY_LOG_COMPARATOR = new Comparator<ProMonitoringHistoryItem>() {
        @Override
        public int compare(@NonNull ProMonitoringHistoryItem lhs, @NonNull ProMonitoringHistoryItem rhs) {
            return rhs.getLog().getTimestamp().compareTo(lhs.getLog().getTimestamp());
        }
    };

    public static final Comparator<HistoryEvent> DESC_HISTORY_EVENT_COMPARATOR = new Comparator<HistoryEvent>() {
        @Override
        public int compare(@NonNull HistoryEvent lhs, @NonNull HistoryEvent rhs) {
            return rhs.getTriggeredAt().compareTo(lhs.getTriggeredAt());
        }
    };

    @NonNull
    public static String getServiceAddress(String service, String id) {
        return "SERV:" + service + ":" + id;
    }

    @NonNull
    public static String getDeviceAddress(String id) {
        return "DRIV:" + Device.NAMESPACE + ":" + id;
    }

    public static String getPlaceAddress(String id) {
        return "SERV:" + Place.NAMESPACE + ":" + id;
    }

    @NonNull
    public static List<PersonModel> sortPeopleByDisplayName (@NonNull List<PersonModel> people) {
        List<PersonModel> sortedPeople = new ArrayList<>(people);

        Collections.sort(sortedPeople, new Comparator<PersonModel>() {
            @Override
            public int compare(@NonNull PersonModel lhs, @NonNull PersonModel rhs) {
                return CorneaUtils.getPersonDisplayName(lhs).toUpperCase().compareTo(CorneaUtils.getPersonDisplayName(rhs).toUpperCase());
            }
        });

        return sortedPeople;
    }

    @NonNull
    public static String getPersonDisplayName (@NonNull PersonModel personModel) {
        return personModel.getFirstName() + " " + personModel.getLastName();
    }

    public static DeviceModel filterDeviceModelsByAddress(@NonNull List<DeviceModel> models, @NonNull String address) {
        for (DeviceModel model : models) {
            if (model == null)
                continue;

            if (address.equals(model.getAddress()))
                return model;
        }

        return null;
    }

    @NonNull
    public static List<DeviceModel> filterDeviceModelsByAddress(@NonNull List<DeviceModel> models, @NonNull List<String> addresses) {

        List<DeviceModel> filteredDevices = new ArrayList<>();

        for (DeviceModel model : models) {
            if (model == null)
                continue;

            if (addresses.contains(model.getAddress()))
                filteredDevices.add(model);
        }

        return filteredDevices;
    }

    @NonNull
    public static List<DeviceModel> filterBridgeChildDeviceModelsByParentAddress(@NonNull List<DeviceModel> models, @NonNull String parentProtocolAddress) {

        List<DeviceModel> filteredDevices = new ArrayList<>();

        for (DeviceModel model : models) {
            if (model == null || !(model.getCaps().contains(BridgeChild.NAMESPACE))) {
                continue;
            }

            BridgeChild child = BridgeChild.class.cast(model);

            String childParentAddress = child.getParentAddress();

            if (childParentAddress != null && childParentAddress.equals(parentProtocolAddress)) {
                filteredDevices.add(model);
            }
        }

        return filteredDevices;
    }

    public static boolean firmwareIsUpdating(@Nullable Model model) {
        return model != null && DeviceOta.STATUS_INPROGRESS.equals(model.get(DeviceOta.ATTR_STATUS));
    }

    public static boolean firmwareIsUpdating(String address) {
        return firmwareIsUpdating(getDeviceModelFromCache(address));
    }

    public static boolean isZWaveNetworkRebuildSupported() {
        HubModel hub = SessionModelManager.instance().getHubModel();

        if (hub != null && hasCapability(hub, HubAdvanced.class)) {
            int[] versionComponents = StringUtils.parseVersionString(getCapability(hub, HubAdvanced.class).getOsver());
            if (versionComponents.length == 4) {

                final int major = versionComponents[0], minor = versionComponents[1], maintenace = versionComponents[2], build = versionComponents[3];
                final int MIN_MAJOR_VER = 2, MIN_MINOR_VER = 0, MIN_MAINT_VER = 2, MIN_BUILD_VER = 6;

                return  major > MIN_MAJOR_VER ||
                        major == MIN_MAJOR_VER && minor > MIN_MINOR_VER ||
                        major == MIN_MAJOR_VER && minor == MIN_MINOR_VER && maintenace > MIN_MAINT_VER ||
                        major == MIN_MAJOR_VER && minor == MIN_MINOR_VER && maintenace == MIN_MAINT_VER && build >= MIN_BUILD_VER;
            }
        }

        return false;
    }

    public static boolean isCamera(String address) {
        Model model = getDeviceModelFromCache(address);
        return model != null && model.getCaps() != null && model.getCaps().contains(Camera.NAMESPACE);
    }

    public static @Nullable Model getDeviceModelFromCache(String address) {
        if (Strings.isNullOrEmpty(address)) {
            return null;
        }

        return CorneaClientFactory.getModelCache().get(address);
    }

    public static boolean isDeviceProtocol(String deviceAddress, String protocol) {
        DeviceModel model = SessionModelManager.instance().getDeviceWithId(CorneaUtils.getIdFromAddress(deviceAddress), false);
        return isDeviceProtocol(model, protocol);
    }

    public static boolean isDeviceProtocol(DeviceModel model, String protocol) {
        DeviceAdvanced deviceAdvanced = CorneaUtils.getCapability(model, DeviceAdvanced.class);
        return deviceAdvanced != null && protocol.equals(deviceAdvanced.getProtocol());
    }

    public static boolean isInstanceCapabilityUpdate(@NonNull PropertyChangeEvent event) {
        // Will be in the form of NAMESPACE:ATTRIBUTE:INSTANCE_NAME
        // Example; but:state:diamond = PRESSED
        // or irr:wateringRemainingTime:z1 = 15 etc...
        return event.getPropertyName().matches(".*:.*.:.*");
    }

    public static String getInstancePropertyUpdateInstanceName (@NonNull PropertyChangeEvent event) {
        if (isInstanceCapabilityUpdate(event)) {
            return event.getPropertyName().split(":", 3)[2];
        }

        throw new IllegalArgumentException("PropertyChangeEvent must belong to a multi-instance capability's attribute: " + event);
    }

    public static String getInstancePropertyUpdateFullyQualifiedPropertyName (@NonNull PropertyChangeEvent event) {
        if (isInstanceCapabilityUpdate(event)) {
            return event.getPropertyName().split(":", 3)[0] + ":" + event.getPropertyName().split(":", 3)[1];
        }

        throw new IllegalArgumentException("PropertyChangeEvent must belong to a multi-instance capability's attribute: " + event);
    }

    public static String getInstancePropertyUpdatePropertyName (@NonNull PropertyChangeEvent event) {
        if (isInstanceCapabilityUpdate(event)) {
            return event.getPropertyName().split(":", 3)[1];
        }

        throw new IllegalArgumentException("PropertyChangeEvent must belong to a multi-instance capability's attribute: " + event);
    }

    public static void logMissingProductCatalogID(String deviceAddress, String productID) {
        try {
            SessionService service = CorneaClientFactory.getService(SessionService.class);
            service.log("prodcat", "", String.format("Address: %s -- productID: %s", deviceAddress, productID));
        } catch (Exception ignored) {}
    }

    @Nullable public static String getProductShortName(@Nullable String forProductId) {
        if (StringUtils.isEmpty(forProductId)) {
            return null;
        }

        ProductModel productModel = ProductModelProvider.instance().getByProductIDOrNull(forProductId);
        if (productModel != null) {
            return productModel.getShortName();
        } else {
            return null;
        }
    }
}
