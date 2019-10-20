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
package arcus.cornea.subsystem.doorsnlocks;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.error.Errors;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.PersonModelProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.doorsnlocks.model.AccessState;
import arcus.cornea.subsystem.doorsnlocks.model.AccessSummary;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.capability.util.Addresses;
import com.iris.client.bean.LockAuthorizationOperation;
import com.iris.client.bean.LockAuthorizationState;
import com.iris.client.capability.Device;
import com.iris.client.capability.DoorLock;
import com.iris.client.capability.DoorsNLocksSubsystem;
import com.iris.client.capability.Person;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.PersonModel;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DoorsNLocksAccessController extends BaseSubsystemController<DoorsNLocksAccessController.Callback> {

    /**
     * Access summary list callback
     */
    public interface Callback {
        /**
         * Called to show copy when a place has no lock devices, but does have other doors & locks
         * subsystem devices.
         */
        void showNoLocksCopy();

        /**
         * Called to show the summary of access control per lock
         *
         * @param accessSummary
         */
        void showAccessSummary(List<AccessSummary> accessSummary);

        /**
         * Called when the number of people with access to the device changes.
         *
         * @param accessSummary
         */
        void updateAccessSummary(AccessSummary accessSummary);

    }

    /**
     * Callback for a selected device.
     */
    public interface SelectedDeviceCallback {
        /**
         * Invoked to show the active access list
         */
        void showActiveAccess(List<AccessState> access);

        /**
         * Invoked to show the editable access list
         */
        void showEditableAccess(int maxPeople, List<AccessState> access);

        /**
         * Invoked as the state for a person changes.
         *
         * @param access
         */
        void updateAccessState(AccessState access);

        /**
         * Invoked while the saving the access control list to the device
         * should disable the 'save' button
         */
        void showSaving();

        void showError(ErrorModel error);
    }

    private static final Logger logger = LoggerFactory.getLogger(DoorsNLocksAccessController.class);
    private static final DoorsNLocksAccessController instance;

    static {
        instance = new DoorsNLocksAccessController();
        instance.init();
    }

    public static DoorsNLocksAccessController instance() {
        return instance;
    }

    private final Listener<List<DeviceModel>> onDeviceListChange = Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
        @Override
        public void onEvent(List<DeviceModel> deviceModels) {
            onDevicesChanged(deviceModels);
        }
    });

    private final Listener<ModelChangedEvent> onDeviceChange = Listeners.runOnUiThread(new Listener<ModelChangedEvent>() {
        @Override
        public void onEvent(ModelChangedEvent modelChangedEvent) {
            onDeviceChanged(modelChangedEvent);
        }
    });

    private final Listener<List<PersonModel>> onPeopleListChange = Listeners.runOnUiThread(new Listener<List<PersonModel>>() {
        @Override
        public void onEvent(List<PersonModel> personModels) {
            onPeopleChanged(personModels);
        }
    });

    private final Listener<ModelChangedEvent> onPersonChange = Listeners.runOnUiThread(new Listener<ModelChangedEvent>() {
        @Override
        public void onEvent(ModelChangedEvent modelChangedEvent) {
            onPersonChanged(modelChangedEvent);
        }
    });

    private final Comparator<AccessSummary> nameSorter = new Comparator<AccessSummary>() {
        @Override
        public int compare(AccessSummary accessSummary, AccessSummary t1) {
            return accessSummary.getName().compareTo(t1.getName());
        }
    };

    private final Comparator<AccessState> personNameSorter = new Comparator<AccessState>() {
        @Override
        public int compare(AccessState accessState, AccessState t1) {
            int comparison = nullSafeStringComparator.compare(accessState.getFirstName(), t1.getFirstName());
            if(comparison == 0) {
                return nullSafeStringComparator.compare(accessState.getLastName(), t1.getLastName());
            }
            return comparison;
        }
    };

    private final Comparator<String> nullSafeStringComparator = new Comparator<String>() {
        @Override
        public int compare(String s, String t1) {
            if(s == null) {
                return t1 == null ? 0 : -1;
            }
            if(t1 == null) {
                return 1;
            }
            return s.compareTo(t1);
        }
    };

    private final Predicate<AccessState> filterAuthorized = new Predicate<AccessState>() {
        @Override
        public boolean apply(AccessState accessState) {
            return LockAuthorizationState.STATE_AUTHORIZED.equals(accessState.getAccessState());
        }
    };

    private final Predicate<AccessState> filterPending = new Predicate<AccessState>() {
        @Override
        public boolean apply(AccessState accessState) {
            return LockAuthorizationState.STATE_PENDING.equals(accessState.getAccessState());
        }
    };

    private final Function<AccessState, Map<String,Object>> toOperation = new Function<AccessState, Map<String,Object>>() {
        @Override
        public Map<String, Object> apply(AccessState accessState) {
            String operation = getOperation(accessState.getAccessState());
            Map<String,Object> asMap = new HashMap<>();
            asMap.put(LockAuthorizationOperation.ATTR_PERSON, "SERV:person:" + accessState.getPersonId());
            asMap.put(LockAuthorizationOperation.ATTR_OPERATION, operation);
            return asMap;
        }

        private String getOperation(String state) {
            if(LockAuthorizationState.STATE_AUTHORIZED.equals(state)) {
                return LockAuthorizationOperation.OPERATION_AUTHORIZE;
            }
            return LockAuthorizationOperation.OPERATION_DEAUTHORIZE;
        }
    };

    private final Set<String> devicesWithChangesPending = Collections.synchronizedSet(new HashSet<String>());
    private final AddressableListSource<DeviceModel> devices;
    private final AddressableListSource<PersonModel> people;
    private WeakReference<SelectedDeviceCallback> selectedDeviceCallback;
    private SelectedMode mode = SelectedMode.SUMMARY;
    private Set<AccessSummary> curSummary = new HashSet<>();
    private String selectedDeviceId = null;
    private Set<AccessState> selectedDeviceState = new HashSet<>();

    DoorsNLocksAccessController() {
        this(SubsystemController.instance().getSubsystemModel(DoorsNLocksSubsystem.NAMESPACE),
             DeviceModelProvider.instance().newModelList(),
             PersonModelProvider.instance().newModelList());
    }

    DoorsNLocksAccessController(ModelSource<SubsystemModel> subsystemModel,
                                AddressableListSource<DeviceModel> devices,
                                AddressableListSource<PersonModel> people) {
        super(subsystemModel);
        this.devices = devices;
        this.people = people;
        this.selectedDeviceCallback = new WeakReference<>(null);
    }

    @Override
    public void init() {
        this.devices.addListener(onDeviceListChange);
        this.devices.addModelListener(onDeviceChange, ModelChangedEvent.class);
        this.people.addListener(onPeopleListChange);
        this.people.addModelListener(onPersonChange, ModelChangedEvent.class);
        super.init();
    }

    @Override
    protected boolean isLoaded() {
        if(super.isLoaded()) {
            DoorsNLocksSubsystem dnl = (DoorsNLocksSubsystem) getModel();
            List<DeviceModel> devices = getDevices();
            List<PersonModel> people = getPeople();
            return devices != null && devices.size() == dnl.getLockDevices().size() &&
                   people != null && people.size() == dnl.getAllPeople().size();
        }
        return false;
    }

    public ListenerRegistration setSelectedDeviceCallback(String id, SelectedDeviceCallback callback) {
        if(selectedDeviceCallback.get() != null) {
            logger.warn("Replacing selected device callback");
        }
        selectedDeviceId = id;
        selectedDeviceCallback = new WeakReference<>(callback);
        mode = SelectedMode.VIEW;
        selectedDeviceState.clear();
        updateSelected(callback);
        return Listeners.wrap(selectedDeviceCallback);
    }

    @Override
    public ListenerRegistration setCallback(Callback callback) {
        mode = SelectedMode.SUMMARY;
        curSummary.clear();
        return super.setCallback(callback);
    }

    public void edit() {
        mode = SelectedMode.EDIT;
        updateSelected(selectedDeviceCallback.get());
    }

    public void save(List<AccessState> accessStates) {
        SelectedDeviceCallback callback = selectedDeviceCallback.get();

        if(callback == null) {
            logger.debug("ignoring save request, no callback set");
            return;
        }

        if(!isLoaded()) {
            logger.debug("cannot update model, not loaded yet");
            return;
        }

        DeviceModel selectedDevice = findDevice(selectedDeviceId);

        if(selectedDevice == null) {
            logger.debug("cannot authorize people for a non-selected device");
            return;
        }

        DoorsNLocksSubsystem dnl = (DoorsNLocksSubsystem) getModel();

        mode = SelectedMode.SAVE;

        List<AccessState> filtered = Lists.newArrayList(Iterables.filter(accessStates, Predicates.not(filterPending)));
        List<Map<String,Object>> operations = Lists.newArrayList(Iterables.transform(filtered, toOperation));

        Set<AccessState> desiredState = Sets.newHashSet(accessStates);
        Set<AccessState> diff = Sets.difference(desiredState, selectedDeviceState);
        if (diff.isEmpty()) {
            logger.debug("No changes were detected, not sending request - Access lists the same.");
            afterSave();
            return;
        }

        callback.showSaving();

        devicesWithChangesPending.add(selectedDevice.getAddress());
        dnl.authorizePeople(selectedDevice.getAddress(), operations)
              .onSuccess(Listeners.runOnUiThread(new AuthorizationUpdate(selectedDevice.getAddress())))
              .onFailure(Listeners.runOnUiThread(new AuthorizationUpdateFailure(selectedDevice.getAddress())));
    }

    private void onSaved(boolean pendingChanges) {
        if(!pendingChanges) {
            afterSave();
        }
    }

    private void onSaveFailed(Throwable cause) {
        SelectedDeviceCallback cb = selectedDeviceCallback.get();
        if(cb != null) {
            cb.showError(Errors.translate(cause));
        }
    }

    private void afterSave() {
        selectedDeviceState.clear();
        mode = SelectedMode.VIEW;
        updateSelected(selectedDeviceCallback.get());
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        super.onSubsystemLoaded(event);
        DoorsNLocksSubsystem dnl = (DoorsNLocksSubsystem) getModel();
        this.devices.setAddresses(new ArrayList<>(dnl.getLockDevices()));
        this.people.setAddresses(new ArrayList<>(dnl.getAllPeople()));
    }

    @Override
    protected void onSubsystemCleared(ModelDeletedEvent event) {
        devicesWithChangesPending.clear();
        super.onSubsystemCleared(event);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> changes = event.getChangedAttributes().keySet();
        DoorsNLocksSubsystem dnl = (DoorsNLocksSubsystem) getModel();
        boolean setChanged = false;
        if(changes.contains(DoorsNLocksSubsystem.ATTR_LOCKDEVICES)) {
            setChanged = true;
            curSummary.clear();
            devices.setAddresses(new ArrayList<>(dnl.getLockDevices()));
        }
        if(changes.contains(DoorsNLocksSubsystem.ATTR_ALLPEOPLE)) {
            setChanged = true;
            people.setAddresses(new ArrayList<>(dnl.getAllPeople()));
        }

        // if one of the sets has changed, the view will be updated when the set data is loaded
        if(setChanged) {
            return;
        }

        if(changes.contains(DoorsNLocksSubsystem.ATTR_AUTHORIZATIONBYLOCK)) {
            for (String device : devicesWithChangesPending) {
                if (!havePending(getDeviceLockState(device))) {
                    devicesWithChangesPending.remove(device);
                }
            }
            handleLockAuthorizationChange();
        }
    }

    @Override
    protected void updateView(Callback callback) {
        if(!isLoaded()) {
            logger.debug("not updating view because the subsystem is not loaded");
            return;
        }
        DoorsNLocksSubsystem dnl = (DoorsNLocksSubsystem) getModel();
        if(dnl.getLockDevices().isEmpty()) {
            callback.showNoLocksCopy();
            return;
        }

        Set<AccessSummary> newSummary = buildSummary(dnl);
        Set<AccessSummary> tmpSummary = curSummary;
        if(newSummary.size() != tmpSummary.size()) {
            List<AccessSummary> toSort = Lists.newArrayList(newSummary);
            Collections.sort(toSort, nameSorter);
            callback.showAccessSummary(toSort);
        } else {
            Set<AccessSummary> diff = Sets.difference(newSummary, tmpSummary);
            for(AccessSummary summary : diff) {
                callback.updateAccessSummary(summary);
            }
        }

        curSummary = newSummary;
    }

    private void updateSelected(SelectedDeviceCallback callback) {
        if(callback == null) {
            logger.debug("not updating selected device callback, it is null");
            return;
        }

        if(!isLoaded()) {
            logger.debug("not updating selected device callback because the subsystem is not fully loaded");
            return;
        }

        DeviceModel selectedDevice = findDevice(selectedDeviceId);

        if(selectedDevice == null) {
            logger.debug("not updating selected device callback because selected device is null");
            return;
        }

        Set<AccessState> newState = getDeviceLockState(selectedDevice.getAddress());
        final Set<AccessState> tmpState = selectedDeviceState;
        switch(mode) {
            case VIEW:
                updateActiveAccess(newState, callback);
                break;
            case EDIT:
                updateEditableCallList(newState, callback, selectedDevice);
                break;
            case SAVE:
                Set<AccessState> diff = Sets.difference(newState, tmpState);
                for(AccessState state : diff) {
                    callback.updateAccessState(state);
                }
                if(!havePending(newState)) {
                    afterSave();
                }
                break;
        }

        selectedDeviceState = newState;
    }

    private boolean havePending(Set<AccessState> states) {
        List<AccessState> pending = Lists.newArrayList(Iterables.filter(states, filterPending));
        return !pending.isEmpty();
    }

    private void updateSelectedPending(SelectedDeviceCallback callback) {
        if(callback == null) {
            logger.debug("not updating selected device callback, it is null");
            return;
        }

        if(!isLoaded()) {
            logger.debug("not updating selected device callback because the subsystem is not fully loaded");
            return;
        }
        DeviceModel selectedDevice = findDevice(selectedDeviceId);
        if(selectedDevice == null) {
            logger.debug("not updating selected device callback because selected device is null");
            return;
        }

        Set<AccessState> newState = getDeviceLockState(selectedDevice.getAddress());
        final Set<AccessState> tmpState = selectedDeviceState;
        Set<AccessState> diff = Sets.difference(newState, tmpState);
        Iterable<AccessState> pendingChanges = Iterables.filter(diff, filterPending);
        if(mode == SelectedMode.EDIT) {
            for(AccessState pending : pendingChanges) {
                callback.updateAccessState(pending);
            }
        } else {
            logger.debug("ignoring update to pending only in {} mode", mode);
        }

        selectedDeviceState = newState;
    }

    private void updateActiveAccess(Set<AccessState> newState, SelectedDeviceCallback callback) {
        Set<AccessState> filtered = Sets.newHashSet(Iterables.filter(newState, filterAuthorized));

        if(filtered.isEmpty()) {
            callback.showActiveAccess(Collections.<AccessState>emptyList());
            return;
        }

        Set<AccessState> existingFiltered = Sets.newHashSet(Iterables.filter(selectedDeviceState, filterAuthorized));
        if(!filtered.equals(existingFiltered)) {
            List<AccessState> active = Lists.newArrayList(filtered);
            Collections.sort(active, personNameSorter);
            callback.showActiveAccess(active);
        } else {
            logger.debug("ignoring update to authorization states for current device, no changes to people authorized");
        }
    }

    private void updateEditableCallList(Set<AccessState> newState, SelectedDeviceCallback callback, DeviceModel selectedDevice) {
        List<AccessState> toSort = Lists.newArrayList(newState);
        Collections.sort(toSort, personNameSorter);
        DoorLock dl = (DoorLock) selectedDevice;
        callback.showEditableAccess(dl.getNumPinsSupported(), toSort);
    }

    private Set<AccessSummary> buildSummary(DoorsNLocksSubsystem subsystem) {
        List<DeviceModel> devices = getDevices();
        Set<AccessSummary> summary = new HashSet<>();
        for(DeviceModel device : devices) {
            summary.add(createSummary(device, subsystem));
        }
        return summary;
    }

    private Set<AccessState> getDeviceLockState(String device) {
        if(!isLoaded()) {
            logger.warn("attempt to get lock state for currently selected device before subsystem has been loaded");
            return Collections.emptySet();
        }

        DoorsNLocksSubsystem dnl = (DoorsNLocksSubsystem) getModel();

        Set<AccessState> states = new HashSet<>();
        Map<String,Set<Map<String,Object>>> byLock = dnl.getAuthorizationByLock();
        Set<Map<String,Object>> forLock = byLock.get(device);
        if(forLock == null) {
            logger.warn("no authorization states exist for lock {}", device);
            return Collections.emptySet();
        }

        for(Map<String,Object> state : forLock) {
            String personAddress = (String) state.get(LockAuthorizationState.ATTR_PERSON);
            PersonModel p = findPerson(personAddress);
            if(p == null) {
                logger.warn("lock authorization state ignored for person that doesn't exist {}", personAddress);
                continue;
            }

            AccessState s = new AccessState();
            s.setAccessState((String) state.get(LockAuthorizationState.ATTR_STATE));
            s.setFirstName(p.getFirstName());
            s.setLastName(p.getLastName());
            s.setPersonId(p.getId());
            ArrayList<String> tags = (ArrayList<String>) p.getTags();
            if (tags != null && tags.size() > 0 ){
                s.setRelationship(tags.get(0));
            }
            states.add(s);
        }
        return states;
    }

    private AccessSummary createSummary(DeviceModel device, DoorsNLocksSubsystem dnl) {
        int count = countAccess(device.getAddress(), dnl);
        AccessSummary summary = new AccessSummary();
        summary.setAccessCount(count);
        summary.setName(device.getName());
        summary.setDeviceId(device.getId());
        return summary;
    }

    private int countAccess(String devAddr, DoorsNLocksSubsystem dnl) {
        Map<String,Set<Map<String,Object>>> authByLock = dnl.getAuthorizationByLock();
        int access = 0;
        Set<Map<String,Object>> forLock = authByLock.get(devAddr);
        if(forLock == null) {
            return access;
        }
        for(Map<String,Object> auth : forLock) {
            if(LockAuthorizationState.STATE_AUTHORIZED.equals(auth.get(LockAuthorizationState.ATTR_STATE))) {
                access++;
            }
        }
        return access;
    }

    private void onDevicesChanged(List<DeviceModel> deviceModels) {
        switch(mode) {
            case SUMMARY:
                updateView();
                break;
            case VIEW:
                updateSelected(selectedDeviceCallback.get());
                break;
            default:
                logger.debug("ignoring device change in {} mode", mode);

        }
    }

    private void onDeviceChanged(ModelChangedEvent event) {
        if(!isLoaded()) {
            logger.debug("ignoring device change event, subsystem is not fully loaded");
            return;
        }

        if(mode != SelectedMode.SUMMARY) {
            logger.debug("ignoring device change event when not in summary mode");
            return;
        }

        Set<String> changes = event.getChangedAttributes().keySet();
        if(changes.contains(Device.ATTR_NAME)) {
            // clear the summary so it gets resorted by name
            curSummary.clear();
            updateView();
        }
    }

    private void onPersonChanged(ModelChangedEvent event) {
        SelectedDeviceCallback callback = selectedDeviceCallback.get();
        if(callback == null) {
            logger.debug("ignoring person change event, no callback set");
            return;
        }

        if(!isLoaded()) {
            logger.debug("ignoring person change event, subsystem is not fully loaded");
            return;
        }

        if(mode != SelectedMode.VIEW) {
            logger.debug("ignoring person change event, not in view mode");
            return;
        }

        Set<String> changes = event.getChangedAttributes().keySet();
        if (changes.contains(Person.ATTR_FIRSTNAME) || changes.contains(Person.ATTR_LASTNAME)) {
            selectedDeviceState.clear();
            updateSelected(callback);
        } else {
            logger.debug("ignoring person change event, change did not include first or last name");
        }
    }

    private void onPeopleChanged(List<PersonModel> peopleModels) {
        switch(mode) {
            case SUMMARY:
                updateView();
                break;
            case VIEW:
                updateSelected(selectedDeviceCallback.get());
                break;
            default:
                logger.debug("ignoring people change event in {} mode", mode);
        }
    }

    private void handleLockAuthorizationChange() {
        switch(mode) {
            case SUMMARY:
                updateView();
                break;
            case VIEW:
                updateSelected(selectedDeviceCallback.get());
                break;
            case EDIT:
                updateSelectedPending(selectedDeviceCallback.get());
                break;
            case SAVE:
                updateSelected(selectedDeviceCallback.get());
        }
    }

    private List<DeviceModel> getDevices() {
        return devices.get();
    }

    private List<PersonModel> getPeople() {
        return people.get();
    }

    private PersonModel findPerson(String address) {
        List<PersonModel> people = getPeople();
        if(people == null) {
            return null;
        }
        for(PersonModel p : people) {
            if(p.getAddress().equals(address)) {
                return p;
            }
        }
        return null;
    }

    private DeviceModel findDevice(String id) {
        List<DeviceModel> devices = getDevices();
        if(devices == null) {
            return null;
        }
        for(DeviceModel d : devices) {
            if(d.getId().equals(id)) {
                return d;
            }
        }
        return null;
    }

    private enum SelectedMode {
        VIEW,
        EDIT,
        SAVE,
        SUMMARY
    }

    public boolean hasPendingChanges() {
        return devicesWithChangesPending.contains(currentDeviceAddress()) || havePending(selectedDeviceState);
    }

    private String currentDeviceAddress() {
        return Addresses.toObjectAddress(Device.NAMESPACE, Addresses.getId(selectedDeviceId));
    }

    private class AuthorizationUpdateFailure implements Listener<Throwable> {
        private final String address;

        public AuthorizationUpdateFailure(String forAddress) {
            this.address = forAddress;
        }

        @Override public void onEvent(Throwable cause) {
            devicesWithChangesPending.remove(address);
            onSaveFailed(cause);
        }
    }

    private class AuthorizationUpdate implements Listener<DoorsNLocksSubsystem.AuthorizePeopleResponse> {
        private final String address;

        public AuthorizationUpdate(String forAddress) {
            this.address = forAddress;
        }

        @Override public void onEvent(DoorsNLocksSubsystem.AuthorizePeopleResponse response) {
            if (Boolean.FALSE.equals(response.getChangesPending())) {
                devicesWithChangesPending.remove(address);
            }
            onSaved(response.getChangesPending());
        }
    }
}
