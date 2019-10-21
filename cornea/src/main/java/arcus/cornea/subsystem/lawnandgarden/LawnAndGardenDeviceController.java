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
package arcus.cornea.subsystem.lawnandgarden;

import com.google.common.collect.ImmutableList;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.lawnandgarden.model.LawnAndGardenDeviceControlModel;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.LawnNGardenSubsystem;
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


public class LawnAndGardenDeviceController extends BaseLawnAndGardenController<LawnAndGardenDeviceController.Callback> {
    private static final LawnAndGardenDeviceController instance;

    static {
        instance = new LawnAndGardenDeviceController(
              DeviceModelProvider.instance().getModels(ImmutableList.<String>of())
        );
        instance.init();
    }

    public static LawnAndGardenDeviceController instance() {
        return instance;
    }

    private final AddressableListSource<DeviceModel> controlDevices;
    private Listener<Object> updateViewTask = Listeners.runOnUiThread(new Listener<Object>() {
        @Override
        public void onEvent(Object o) {
            updateView();
        }
    });

    LawnAndGardenDeviceController(
            AddressableListSource<DeviceModel> controlDevices
    ) {
        super();
        this.controlDevices = controlDevices;
        this.controlDevices.addListener(updateViewTask);
    }

    LawnAndGardenDeviceController(
            AddressableListSource<DeviceModel> controlDevices,
            ModelSource<SubsystemModel> subsystem
    ) {
        super(subsystem);
        this.controlDevices = controlDevices;
        this.controlDevices.addListener(updateViewTask);
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        super.onSubsystemLoaded(event);
        controlDevices.setAddresses(list(getLawnNGardenSubsystem().getControllers()));
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        super.onSubsystemChanged(event);
        if(event.getChangedAttributes().containsKey(LawnNGardenSubsystem.ATTR_CONTROLLERS)) {
            controlDevices.setAddresses(list(getLawnNGardenSubsystem().getControllers()), true);
            updateView();
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
        callback.showDeviceControls(createDeviceControls(controlDevices.get()));
    }

    protected List<LawnAndGardenDeviceControlModel> createDeviceControls(List<DeviceModel> devices) {
        if(devices == null || devices.isEmpty()) {
            return ImmutableList.of();
        }

        List<LawnAndGardenDeviceControlModel> models = new ArrayList<>(devices.size());
        for(DeviceModel device : devices) {
            LawnAndGardenDeviceControlModel model = new LawnAndGardenDeviceControlModel();
            model.setDeviceId(device.getId());
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
        void showDeviceControls(List<LawnAndGardenDeviceControlModel> controls);

    }

    private static final Comparator<LawnAndGardenDeviceControlModel> ORDER = new Comparator<LawnAndGardenDeviceControlModel>() {
        @Override
        public int compare(LawnAndGardenDeviceControlModel m1, LawnAndGardenDeviceControlModel m2) {
            return ObjectUtils.compare(m1.getName(), m2.getName());
        }
    };

}
