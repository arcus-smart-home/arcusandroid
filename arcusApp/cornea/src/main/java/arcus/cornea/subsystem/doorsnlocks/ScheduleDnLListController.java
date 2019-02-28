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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.ScheduleGenericStateModel;

import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.DoorsNLocksSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.SubsystemModel;
import com.google.common.collect.Sets;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;



public class ScheduleDnLListController extends BaseDnLController<ScheduleDnLListController.Callback> implements ScheduleDnLStateController.Callback {
    private static final ScheduleDnLListController instance = new ScheduleDnLListController();

    private WeakReference<ScheduleDnLStateController> mScheduleStateController;

    final static Set<String> attributesChanges;

 static {
     attributesChanges = ImmutableSet.of(
             DoorsNLocksSubsystem.ATTR_AUTOPETDOORS,
             DoorsNLocksSubsystem.ATTR_PETDOORDEVICES,
             DoorsNLocksSubsystem.ATTR_UNLOCKEDPETDOORS,
             DoorsNLocksSubsystem.ATTR_OFFLINEPETDOORS,
             DoorsNLocksSubsystem.ATTR_OFFLINEMOTORIZEDDOORS,
             DoorsNLocksSubsystem.ATTR_MOTORIZEDDOORDEVICES,
             DoorsNLocksSubsystem.ATTR_OPENMOTORIZEDDOORS);
 }

    public static ScheduleDnLListController instance() {
        return instance;
    }

    private AddressableListSource<DeviceModel> doorDevices;

    private String placeId;

    ScheduleDnLListController() {
        super();
        this.doorDevices = DeviceModelProvider.instance().newModelList();
        init();
    }

    ScheduleDnLListController(ModelSource<SubsystemModel> subsystem, AddressableListSource<DeviceModel> thermostats) {
        super(subsystem);
        this.doorDevices = thermostats;
        init();
    }

    public ListenerRegistration selectAll(String placeId, Callback callback) {
        this.placeId = placeId;
        return this.setCallback(callback);
    }

    public void init() {
        super.init();
        this.doorDevices.addListener(Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
            @Override
            public void onEvent(List<DeviceModel> deviceModels) {
                updateView();
            }
        }));

        if (mScheduleStateController == null) {
            this.mScheduleStateController = new WeakReference<>(ScheduleDnLStateController.instance());
        }
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        ArrayList<String> devices = new ArrayList<>(getDnLSubsystem().getPetDoorDevices());
        devices.addAll(new ArrayList<>(getDnLSubsystem().getMotorizedDoorDevices()));
        doorDevices.setAddresses(devices);
        doorDevices.load();
        super.onSubsystemLoaded(event);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {

        Set<String> currentChanges = event.getChangedAttributes().keySet();
        Set<String> intersection = Sets.intersection(attributesChanges, currentChanges);
        if (!intersection.isEmpty()) {
            List<String> listAddresses = new ArrayList<>();
            listAddresses.addAll(list(getDnLSubsystem().getPetDoorDevices()));
            listAddresses.addAll(list(getDnLSubsystem().getMotorizedDoorDevices()));
            doorDevices.setAddresses(listAddresses, true);
        }

        super.onSubsystemChanged(event);
    }

    @Override
    protected void onSubsystemCleared(ModelDeletedEvent event) {
        doorDevices.setAddresses(ImmutableList.<String>of());
        super.onSubsystemCleared(event);
    }

    @Override
    protected void updateView(Callback callback, DoorsNLocksSubsystem subsystem) {
        if(!doorDevices.isLoaded()) {
            return;
        }

        List<DeviceModel> doorsNLocksListModels = doorDevices.get();
        if(doorsNLocksListModels.isEmpty()) {
            callback.showNoSchedulableDevices();
        }
        else {
            mScheduleStateController.get().selectAll(this.placeId, doorsNLocksListModels, this);
        }
    }

    @Override
    public void showScheduleStates(List<ScheduleGenericStateModel> models) {
        if (getCallback() != null) {
            getCallback().showSchedules(models);
        }
    }



    @Override
    public void onError(ErrorModel error) {

    }


    public interface Callback {

        void showNoSchedulableDevices();

        void showSchedules(List<ScheduleGenericStateModel> devices);
    }
}