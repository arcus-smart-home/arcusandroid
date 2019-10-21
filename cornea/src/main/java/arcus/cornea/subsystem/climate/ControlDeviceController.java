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
package arcus.cornea.subsystem.climate;

import com.google.common.collect.ImmutableList;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.climate.model.DeviceControlModel;
import arcus.cornea.subsystem.climate.model.DeviceControlType;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.ClimateSubsystem;
import com.iris.client.capability.Fan;
import com.iris.client.capability.SpaceHeater;
import com.iris.client.capability.Thermostat;
import com.iris.client.capability.Vent;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.SubsystemModel;

import org.apache.commons.lang3.ObjectUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ControlDeviceController extends BaseClimateController<ControlDeviceController.Callback> {
    private static final ControlDeviceController instance;

    static {
        instance = new ControlDeviceController(
              DeviceModelProvider.instance().getModels(ImmutableList.<String>of())
        );
        instance.init();
    }

    public static ControlDeviceController instance() {
        return instance;
    }

    private AddressableListSource<DeviceModel> controlDevices;
    private Listener<Object> updateViewTask = Listeners.runOnUiThread(new Listener<Object>() {
        @Override
        public void onEvent(Object o) {
            updateView();
        }
    });

    ControlDeviceController(
            AddressableListSource<DeviceModel> controlDevices
    ) {
        super();
        this.controlDevices = controlDevices;
        this.controlDevices.addListener(updateViewTask);
    }

    ControlDeviceController(
            AddressableListSource<DeviceModel> controlDevices,
            ModelSource<SubsystemModel> subsystem
    ) {
        super(subsystem);
        this.controlDevices = controlDevices;
        // TODO clear this when the callback is cleared?
        this.controlDevices.addListener(updateViewTask);
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        super.onSubsystemLoaded(event);
        controlDevices.setAddresses(list(getClimateSubsystem().getControlDevices()));
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {

        super.onSubsystemChanged(event);

        if(event.getChangedAttributes().containsKey(ClimateSubsystem.ATTR_CONTROLDEVICES)) {

            controlDevices.setAddresses(list(getClimateSubsystem().getControlDevices()), true);
            controlDevices.load().onSuccess(Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
                @Override public void onEvent(List<DeviceModel> deviceModels) {
                    updateView();
                }
            }));

        }
    }

    @Override
    protected void onSubsystemCleared(ModelDeletedEvent event) {
        super.onSubsystemCleared(event);
        this.controlDevices.setAddresses(ImmutableList.<String>of());
    }

    @Override
    protected void updateView(Callback callback) {

        if(!controlDevices.isLoaded()) {
            controlDevices.load();
            return;
        }
        callback.showDeviceControls( createDeviceControls( controlDevices.get() ) );
    }

    protected List<DeviceControlModel> createDeviceControls(List<DeviceModel> devices) {
        if(devices == null || devices.isEmpty() ) {
            return ImmutableList.of();
        }

        List<DeviceControlModel> models = new ArrayList<>(devices.size());
        for(DeviceModel device: devices) {
            DeviceControlModel model = new DeviceControlModel();
            model.setDeviceId(device.getId());
            if(device.getCaps().contains(Thermostat.NAMESPACE)) {
                model.setType(DeviceControlType.THERMOSTAT);
            }
            else if(device.getCaps().contains(Fan.NAMESPACE)) {
                model.setType(DeviceControlType.FAN);
            }
            else if(device.getCaps().contains(Vent.NAMESPACE)) {
                model.setType(DeviceControlType.VENT);
            }
            else if(device.getCaps().contains(SpaceHeater.NAMESPACE)) {
                model.setType(DeviceControlType.SPACEHEATER);
            }
            else {
                // unknown control type, skip this device for now...
                continue;
            }
            model.setName(device.getName());
            models.add(model);
        }
        Collections.sort(models, ORDER);

        return models;
    }

    public interface Callback {

        /**
         * Called when the callback is initially registered, or
         * when the set of devices being controlled changes.
         * @param controls
         */
        void showDeviceControls(List<DeviceControlModel> controls);

    }

    private static final Comparator<DeviceControlModel> ORDER = new Comparator<DeviceControlModel>() {
        @Override
        public int compare(DeviceControlModel m1, DeviceControlModel m2) {
            boolean t1 = m1.getType() == DeviceControlType.THERMOSTAT;
            boolean t2 = m2.getType() == DeviceControlType.THERMOSTAT;

            if(t1 && !t2) {
                return -1;
            }
            if(t2 && !t1) {
                return 1;
            }

            return ObjectUtils.compare(m1.getName(), m2.getName());
        }
    };


}
