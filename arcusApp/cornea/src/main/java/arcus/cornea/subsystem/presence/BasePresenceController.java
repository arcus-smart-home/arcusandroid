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
package arcus.cornea.subsystem.presence;

import com.google.common.collect.Lists;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.PersonModelProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.presence.model.PresenceModel;
import arcus.cornea.subsystem.presence.model.PresenceState;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.Device;
import com.iris.client.capability.Person;
import com.iris.client.capability.Presence;
import com.iris.client.capability.PresenceSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.PersonModel;
import com.iris.client.model.SubsystemModel;

import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class BasePresenceController<C> extends BaseSubsystemController<C> {

    private static final Logger logger = LoggerFactory.getLogger(BasePresenceController.class);

    protected AddressableListSource<PersonModel> peopleAtHome = PersonModelProvider.instance().newModelList();
    protected AddressableListSource<PersonModel> peopleAway = PersonModelProvider.instance().newModelList();
    protected AddressableListSource<DeviceModel> allDevices = DeviceModelProvider.instance().newModelList();
    private boolean peopleAtHomeLoaded = false;
    private boolean peopleAwayLoaded = false;
    private boolean devicesLoaded = false;

    private final Listener<List<PersonModel>> peopleAtHomeListener = Listeners.runOnUiThread(new Listener<List<PersonModel>>() {
        @Override
        public void onEvent(List<PersonModel> personModels) {
            peopleAtHomeLoaded = true;
            updateView();
        }
    });

    private final Listener<List<PersonModel>> peopleAwayListener = Listeners.runOnUiThread(new Listener<List<PersonModel>>() {
        @Override
        public void onEvent(List<PersonModel> personModels) {
            peopleAwayLoaded = true;
            updateView();
        }
    });

    private final Listener<List<DeviceModel>> devicesListener = Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
        @Override
        public void onEvent(List<DeviceModel> deviceModels) {
            devicesLoaded = true;
            updateView();
        }
    });

    private final Listener<ModelChangedEvent> personChangeListener = Listeners.runOnUiThread(new Listener<ModelChangedEvent>() {
        @Override
        public void onEvent(ModelChangedEvent modelChangedEvent) {
            onPersonUpdate(modelChangedEvent);
        }
    });

    private final Listener<ModelChangedEvent> deviceChangeListener = Listeners.runOnUiThread(new Listener<ModelChangedEvent>() {
        @Override
        public void onEvent(ModelChangedEvent modelChangedEvent) {
            onDeviceUpdate(modelChangedEvent);
        }
    });

    protected final Comparator<String> nullSafeStringComparator = new Comparator<String>() {
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

    protected final Comparator<PresenceModel> presenceSorter = new Comparator<PresenceModel>() {
        @Override
        public int compare(PresenceModel pm1, PresenceModel pm2) {
            int stateCompare = pm1.getState().compareTo(pm2.getState());

            if(stateCompare == 0) {
                int assignCompare = Boolean.valueOf(pm2.isAssigned()).compareTo(pm1.isAssigned());
                if(assignCompare != 0) {
                    return assignCompare;
                }
                String cmp1 = pm1.isAssigned() ? pm1.getFirstName() : pm1.getDeviceName();
                String cmp2 = pm2.isAssigned() ? pm2.getFirstName() : pm2.getDeviceName();
                return nullSafeStringComparator.compare(cmp1, cmp2);
            }

            return stateCompare;
        }
    };

    protected BasePresenceController(
            ModelSource<SubsystemModel> subsystem,
            AddressableListSource<PersonModel> peopleAtHome,
            AddressableListSource<PersonModel> peopleAway,
            AddressableListSource<DeviceModel> allDevices) {

        super(subsystem);
        this.peopleAtHome = peopleAtHome;
        this.peopleAway = peopleAway;
        this.allDevices = allDevices;
    }

    @Nullable
    public PresenceSubsystem getPresenceSubsystem() {
        return (PresenceSubsystem) getModel();
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        PresenceSubsystem sub = getPresenceSubsystem();
        this.peopleAtHome.setAddresses(Lists.newArrayList(sub.getPeopleAtHome()));
        this.peopleAway.setAddresses(Lists.newArrayList(sub.getPeopleAway()));
        this.allDevices.setAddresses(Lists.newArrayList(sub.getAllDevices()));

        this.peopleAtHome.addListener(peopleAtHomeListener);
        this.peopleAtHome.addModelListener(personChangeListener, ModelChangedEvent.class);
        this.peopleAway.addListener(peopleAwayListener);
        this.peopleAway.addModelListener(personChangeListener, ModelChangedEvent.class);
        this.allDevices.addListener(devicesListener);
        this.allDevices.addModelListener(deviceChangeListener, ModelChangedEvent.class);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> changes = event.getChangedAttributes().keySet();
        PresenceSubsystem sub = getPresenceSubsystem();

        boolean updateViewNow = false;
        if(changes.contains(PresenceSubsystem.ATTR_PEOPLEATHOME)) { peopleAtHomeLoaded = false; }
        if(changes.contains(PresenceSubsystem.ATTR_PEOPLEAWAY)) { peopleAwayLoaded = false; }
        if(changes.contains(PresenceSubsystem.ATTR_ALLDEVICES)) { devicesLoaded = false; }
        if(changes.contains(PresenceSubsystem.ATTR_DEVICESAWAY) || changes.contains(PresenceSubsystem.ATTR_DEVICESATHOME)) {
            updateViewNow = true;
        }

        if(!peopleAtHomeLoaded) {
            peopleAtHome.setAddresses(Lists.newArrayList(sub.getPeopleAtHome()));
            peopleAtHomeLoaded = peopleAtHome.get().size() == 0 || peopleAtHome.isLoaded();
        }
        if(!peopleAwayLoaded) {
            peopleAway.setAddresses(Lists.newArrayList(sub.getPeopleAway()));
            peopleAwayLoaded = peopleAway.get().size() == 0 || peopleAway.isLoaded();
        }
        if(!devicesLoaded) {
            allDevices.setAddresses(Lists.newArrayList(sub.getAllDevices()));
            devicesLoaded = allDevices.get().size() == 0 || allDevices.isLoaded();
        }

        // we don't need to load any additional data, so go ahead and update the view
        if(isLoaded() && updateViewNow) {
            updateView();
        }
    }

    @Override
    protected boolean isLoaded() {
        return super.isLoaded() && peopleAtHomeLoaded && peopleAwayLoaded && devicesLoaded;
    }

    @Override
    protected void updateView(C callback) {
        if(!isLoaded()) {
            logger.debug("not updating view because data is not fully loaded");
            return;
        }

        PresenceSubsystem sub = getPresenceSubsystem();
        if(!isExpectedSize(peopleAtHome.get(), sub.getPeopleAtHome().size()) ||
           !isExpectedSize(peopleAway.get(), sub.getPeopleAway().size()) ||
           !isExpectedSize(allDevices.get(), sub.getAllDevices().size())) {

            logger.debug("not updating view because lists are not the expected size");
            return;
        }

        updateView(callback, sub);
    }

    protected void updateView(C callback, PresenceSubsystem subsystem) {
        // no-op
    }

    private void onPersonUpdate(ModelChangedEvent evt) {
        if(evt.getChangedAttributes().keySet().contains(Person.ATTR_FIRSTNAME)) {
            updateView();
        }
    }

    private void onDeviceUpdate(ModelChangedEvent evt) {
        if(evt.getChangedAttributes().keySet().contains(Device.ATTR_NAME)) {
            updateView();
        }
    }

    private boolean isExpectedSize(List<?> l, int expected) {
        return l != null && l.size() == expected;
    }

    protected void addPeople(List<PresenceModel> presenceModels, List<PersonModel> people, PresenceState state) {
        if(people == null) {
            return;
        }
        for(PersonModel m : people) {
            presenceModels.add(createPresenceModel(m, state));
        }
    }

    protected PresenceModel createPresenceModel(PersonModel person, PresenceState state) {
        DeviceModel dm = findDeviceAssignedTo(person.getAddress());

        PresenceModel pm = new PresenceModel();
        pm.setPersonId(person.getId());
        pm.setFirstName(person.getFirstName());
        pm.setLastName(person.getLastName());
        pm.setRelationship(person.getTags() == null || person.getTags().size() == 0 ? null : person.getTags().iterator().next());
        pm.setState(state);

        if(dm != null) {
            pm.setDeviceName(dm.getName());
            pm.setDeviceId(dm.getId());
        }

        return pm;
    }

    protected PresenceModel createPresenceModel(DeviceModel device, PresenceState state) {
        PresenceModel pm = new PresenceModel();
        pm.setState(state);
        pm.setDeviceId(device.getId());
        pm.setDeviceName(device.getName());
        return pm;
    }

    protected List<PresenceModel> devicesAtHome() {
        return filterDevices(PresenceState.HOME);
    }

    protected List<PresenceModel> devicesAway() {
        return filterDevices(PresenceState.AWAY);
    }

    private List<PresenceModel> filterDevices(PresenceState state) {
        Set<String> devsInState = state == PresenceState.HOME ? getPresenceSubsystem().getDevicesAtHome() : getPresenceSubsystem().getDevicesAway();
        List<DeviceModel> devs = allDevices.get();
        List<PresenceModel> models = new ArrayList<>();
        if(devs != null) {
            for(DeviceModel d : devs) {
                if(devsInState.contains(d.getAddress())) {
                    models.add(createPresenceModel(d, state));
                }
            }
        }
        return models;
    }

    private DeviceModel findDeviceAssignedTo(String personAddress) {
        List<DeviceModel> devs = allDevices.get();
        if(devs == null) {
            return null;
        }
        for(DeviceModel dm : devs) {
            Presence p = (Presence) dm;
            if(personAddress.equals(p.getPerson()) && p.getUsehint().equals(Presence.USEHINT_PERSON)) {
                return dm;
            }
        }
        return null;
    }
}
