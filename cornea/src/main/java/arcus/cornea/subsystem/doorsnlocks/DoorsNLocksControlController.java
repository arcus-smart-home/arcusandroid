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
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.doorsnlocks.model.DoorsNLocksDevice;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.DoorLock;
import com.iris.client.capability.DoorsNLocksSubsystem;
import com.iris.client.capability.MotorizedDoor;
import com.iris.client.capability.PetDoor;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.SubsystemModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public final class DoorsNLocksControlController extends BaseSubsystemController<DoorsNLocksControlController.Callback> {

    public interface Callback {
        /**
         * Called to show the list of devices.  These will be sorted by type followed by name.
         * @param devices
         */
        void showDevices(List<DoorsNLocksDevice> devices);
    }

    private static final Set<String> UPDATE_ON = ImmutableSet.of(
            DoorsNLocksSubsystem.ATTR_CONTACTSENSORDEVICES,
            DoorsNLocksSubsystem.ATTR_LOCKDEVICES,
            DoorsNLocksSubsystem.ATTR_MOTORIZEDDOORDEVICES,
            DoorsNLocksSubsystem.ATTR_OPENMOTORIZEDDOORS,
            DoorsNLocksSubsystem.ATTR_OPENCONTACTSENSORS,
            DoorsNLocksSubsystem.ATTR_OFFLINECONTACTSENSORS,
            DoorsNLocksSubsystem.ATTR_OFFLINELOCKS,
            DoorsNLocksSubsystem.ATTR_OFFLINEMOTORIZEDDOORS,
            DoorsNLocksSubsystem.ATTR_PETDOORDEVICES



    );

    private static final DoorsNLocksControlController instance;

    static {
        instance = new DoorsNLocksControlController();
        instance.init();
    }

    public static DoorsNLocksControlController instance() {
        return instance;
    }

    private final Listener<List<DeviceModel>> onDeviceListChange = Listeners.runOnUiThread(
        new Listener<List<DeviceModel>>() {
            @Override
            public void onEvent(List<DeviceModel> deviceModels) {
                onDevicesChanged(deviceModels);
            }
        });

    private final Comparator<DeviceModel> deviceSorter = new Comparator<DeviceModel>() {
        @Override
        public int compare(DeviceModel d1, DeviceModel d2) {
            DoorsNLocksDevice.Type t1 = getType(d1);
            DoorsNLocksDevice.Type t2 = getType(d2);

            int typeCompare = t1.compareTo(t2);
            return typeCompare == 0 ? d1.getName().compareTo(d2.getName()) : typeCompare;
        }
    };

    private final Function<DeviceModel, DoorsNLocksDevice> transform = new Function<DeviceModel, DoorsNLocksDevice>() {
        @Override
        public DoorsNLocksDevice apply(DeviceModel deviceModel) {
            DoorsNLocksDevice d = new DoorsNLocksDevice();
            d.setId(deviceModel.getId());
            if(isLock(deviceModel)) {
                d.setType(DoorsNLocksDevice.Type.LOCK);
            } else if(isMotorizedDoor(deviceModel)) {
                d.setType(DoorsNLocksDevice.Type.GARAGE_DOOR);
            }
            else if(isPetDoor(deviceModel)) {
                d.setType(DoorsNLocksDevice.Type.PET_DOOR);
            }
            else {
                d.setType(DoorsNLocksDevice.Type.DOOR_SENSOR);
            }
            return d;
        }
    };

    private final AddressableListSource<DeviceModel> devices;

    DoorsNLocksControlController() {
        this(SubsystemController.instance().getSubsystemModel(DoorsNLocksSubsystem.NAMESPACE),
                DeviceModelProvider.instance().newModelList());
    }

    DoorsNLocksControlController(ModelSource<SubsystemModel> subsystemModel,
                                 AddressableListSource<DeviceModel> devices) {
        super(subsystemModel);
        this.devices = devices;
    }

    @Override
    public void init() {
        this.devices.addListener(onDeviceListChange);
        super.init();
    }

    @Override
    protected boolean isLoaded() {
        if(super.isLoaded()) {
            DoorsNLocksSubsystem dnl = (DoorsNLocksSubsystem) getModel();
            List<DeviceModel> devices = getDevices();
            return devices != null && devices.size() == getExpectedSize(dnl);
        }
        return false;
    }

    private int getExpectedSize(DoorsNLocksSubsystem dnl) {
        return dnl.getLockDevices().size() + dnl.getMotorizedDoorDevices().size() + dnl.getContactSensorDevices().size() + dnl.getPetDoorDevices().size();
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        DoorsNLocksSubsystem dnl = (DoorsNLocksSubsystem) getModel();
        devices.setAddresses(getAllDeviceAddresses(dnl));
        super.onSubsystemLoaded(event);
    }

    private List<String> getAllDeviceAddresses(DoorsNLocksSubsystem dnl) {
        List<String> devices = new ArrayList<>();
        devices.addAll(dnl.getMotorizedDoorDevices());
        devices.addAll(dnl.getContactSensorDevices());
        devices.addAll(dnl.getLockDevices());
        devices.addAll(dnl.getPetDoorDevices());
        return devices;
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> changes = event.getChangedAttributes().keySet();
        Set<String> intersection = Sets.intersection(changes, UPDATE_ON);
        if(intersection.isEmpty()) {
            return;
        }
        DoorsNLocksSubsystem dnl = (DoorsNLocksSubsystem) getModel();
        devices.setAddresses(getAllDeviceAddresses(dnl));
    }

    @Override
    protected void updateView(Callback callback) {
        if(!isLoaded()) {
            return;
        }

        List<DoorsNLocksDevice> devices = buildDevices((DoorsNLocksSubsystem) getModel());
        callback.showDevices(devices);
    }

    private List<DoorsNLocksDevice> buildDevices(DoorsNLocksSubsystem subsystem) {
        List<DeviceModel> copy = new ArrayList<>(getDevices());
        Collections.sort(copy, deviceSorter);
        return Lists.newArrayList(Iterables.transform(copy, transform));
    }

    private void onDevicesChanged(List<DeviceModel> deviceModels) {
        updateView();
    }

    public List<DeviceModel> getDevices() {
        return devices.get();
    }

    private boolean isLock(DeviceModel d) {
        return d.getCaps().contains(DoorLock.NAMESPACE);
    }

    private boolean isMotorizedDoor(DeviceModel d) {
        return d.getCaps().contains(MotorizedDoor.NAMESPACE);
    }

    private boolean isPetDoor(DeviceModel d) {
        return d.getCaps().contains(PetDoor.NAMESPACE);
    }

    private DoorsNLocksDevice.Type getType(DeviceModel d) {
        if(isLock(d)) {
            return DoorsNLocksDevice.Type.LOCK;
        }
        if(isMotorizedDoor(d)) {
            return DoorsNLocksDevice.Type.GARAGE_DOOR;
        }

        if(isPetDoor(d)) {
            return DoorsNLocksDevice.Type.PET_DOOR;
        }

        return DoorsNLocksDevice.Type.DOOR_SENSOR;
    }
}
