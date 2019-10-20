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
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.error.Errors;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.doorsnlocks.model.ChimeConfig;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.bean.DoorChimeConfig;
import com.iris.client.capability.Device;
import com.iris.client.capability.DoorsNLocksSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DoorsNLocksMoreController extends BaseSubsystemController<DoorsNLocksMoreController.Callback> {

    public interface Callback {
        /**
         * Called when there are no contact sensor devices
         */
        void showNoDevices();

        /**
         * Called to show the list of chime configs
         * @param configs
         */
        void showConfigs(List<ChimeConfig> configs);

        /**
         * Called to update information regarding a specific config
         * @param config
         */
        void updateConfig(ChimeConfig config);

        void showError(ErrorModel error);
    }

    private static final Logger logger = LoggerFactory.getLogger(DoorsNLocksMoreController.class);

    private static final DoorsNLocksMoreController instance;

    static {
        instance = new DoorsNLocksMoreController();
        instance.init();
    }

    public static DoorsNLocksMoreController instance() {
        return instance;
    }

    private final Listener<List<DeviceModel>> onDeviceListChange = Listeners.runOnUiThread(
        new Listener<List<DeviceModel>>() {
            @Override
            public void onEvent(List<DeviceModel> deviceModels) {
                onDevicesChanged(deviceModels);
            }
        });

    private final Listener<ModelChangedEvent> onDeviceChange = Listeners.runOnUiThread(new Listener<ModelChangedEvent>() {
        @Override
        public void onEvent(ModelChangedEvent modelEvent) {
            onDeviceChanged(modelEvent);
        }
    });

    private final Function<Map<String,Object>, ChimeConfig> transform = new Function<Map<String,Object>, ChimeConfig>() {
        @Override
        public ChimeConfig apply(Map<String,Object> m) {
            ChimeConfig config = new ChimeConfig();
            config.setEnabled((Boolean) m.get(DoorChimeConfig.ATTR_ENABLED));

            String devAddr = (String) m.get(DoorChimeConfig.ATTR_DEVICE);
            String id = devAddr.split(":")[2];

            config.setDeviceId(id);
            return config;
        }
    };

    private final Function<ChimeConfig, Map<String,Object>> toPlatform = new Function<ChimeConfig, Map<String,Object>>() {
        @Override
        public Map<String, Object> apply(ChimeConfig chimeConfig) {
            Map<String,Object> map = new HashMap<>();
            map.put(DoorChimeConfig.ATTR_DEVICE, "DRIV:dev:" + chimeConfig.getDeviceId());
            map.put(DoorChimeConfig.ATTR_ENABLED, chimeConfig.isEnabled());
            return map;
        }
    };

    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            showError(throwable);
        }
    });

    private final Comparator<ChimeConfig> nameSorter = new Comparator<ChimeConfig>() {
        @Override
        public int compare(ChimeConfig chimeConfig, ChimeConfig t1) {
            return chimeConfig.getName().compareTo(t1.getName());
        }
    };

    private final AddressableListSource<DeviceModel> devices;
    private Set<ChimeConfig> currentConfig = new HashSet<>();

    DoorsNLocksMoreController() {
        this(SubsystemController.instance().getSubsystemModel(DoorsNLocksSubsystem.NAMESPACE),
                DeviceModelProvider.instance().newModelList());
    }

    DoorsNLocksMoreController(ModelSource<SubsystemModel> subsystemModel, AddressableListSource<DeviceModel> devices) {
        super(subsystemModel);
        this.devices = devices;
    }

    @Override
    public void init() {
        this.devices.addListener(onDeviceListChange);
        this.devices.addModelListener(onDeviceChange, ModelChangedEvent.class);
        super.init();
    }

    @Override
    protected boolean isLoaded() {
        if(super.isLoaded()) {
            List<DeviceModel> devices = getDevices();
            DoorsNLocksSubsystem dnl = (DoorsNLocksSubsystem) getModel();
            return devices != null && devices.size() == dnl.getContactSensorDevices().size();
        }
        return false;
    }

    @Override
    public ListenerRegistration setCallback(Callback callback) {
        currentConfig.clear();
        return super.setCallback(callback);
    }

    public void setConfig(ChimeConfig config) {
        if(!isLoaded()) {
            logger.debug("not setting config because the subsystem is not loaded");
            return;
        }

        if(config == null) {
            return;
        }

        SubsystemModel m = getModel();
        DoorsNLocksSubsystem dnl = (DoorsNLocksSubsystem) m;
        Set<Map<String,Object>> newConfig = new HashSet<>(currentConfig.size());
        for(ChimeConfig cfg : currentConfig) {
            Map<String,Object> asMap = toPlatform.apply(cfg);
            if(asMap == null) {
                continue;
            }
            if(cfg.getDeviceId().equals(config.getDeviceId())) {
                asMap.put(DoorChimeConfig.ATTR_ENABLED, config.isEnabled());
            }
            newConfig.add(asMap);
        }

        dnl.setChimeConfig(newConfig);
        m.commit().onFailure(onError);
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        DoorsNLocksSubsystem dnl = (DoorsNLocksSubsystem) getModel();
        this.devices.setAddresses(new ArrayList<>(dnl.getContactSensorDevices()));
        super.onSubsystemLoaded(event);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        super.onSubsystemChanged(event);
        Set<String> changes = event.getChangedAttributes().keySet();
        DoorsNLocksSubsystem dnl = (DoorsNLocksSubsystem) getModel();
        if(changes.contains(DoorsNLocksSubsystem.ATTR_CONTACTSENSORDEVICES)) {
            devices.setAddresses(new ArrayList<>(dnl.getContactSensorDevices()));
            return;
        }
        if(changes.contains(DoorsNLocksSubsystem.ATTR_CHIMECONFIG)) {
            onChimeConfigChange();
        }
    }

    private void onChimeConfigChange() {
        updateView();
    }

    @Override
    protected void updateView(Callback callback) {
        if(!isLoaded()) {
            logger.debug("Not updating view because the subsystem is not loaded");
            return;
        }

        DoorsNLocksSubsystem dnl = (DoorsNLocksSubsystem) getModel();

        Set<ChimeConfig> newCurrent = buildConfigs(dnl);

        if(newCurrent.isEmpty()) {
            callback.showNoDevices();
        } else if(newCurrent.size() != currentConfig.size()) {
            List<ChimeConfig> configs = Lists.newArrayList(newCurrent);
            Collections.sort(configs, nameSorter);
            callback.showConfigs(configs);
        } else {
            Set<ChimeConfig> diff = Sets.difference(newCurrent, currentConfig);
            for(ChimeConfig config : diff) {
                callback.updateConfig(config);
            }
        }
        currentConfig = newCurrent;
    }

    private Set<ChimeConfig> buildConfigs(DoorsNLocksSubsystem subsystem) {
        Set<Map<String,Object>> chimeConfig = subsystem.getChimeConfig();
        Iterable<ChimeConfig> transformed = Iterables.transform(chimeConfig, transform);
        Set<ChimeConfig> configs = new HashSet<>();
        for(ChimeConfig cfg : transformed) {
            DeviceModel d = getDevice(cfg.getDeviceId());
            if(d == null) {
                logger.debug("skipping chime config that has no device loaded");
                continue;
            }
            cfg.setName(d.getName());
            configs.add(cfg);
        }
        return configs;
    }

    private void onDevicesChanged(List<DeviceModel> deviceModels) {
        updateView();
    }

    private void onDeviceChanged(ModelChangedEvent mce) {
        Callback cb = getCallback();
        if(cb == null) {
            logger.debug("not updating on device model change because no call back is set");
        }

        if(!isLoaded()) {
            logger.debug("not updating on device model change because doors n locks is not loaded");
        }

        Set<String> changes = mce.getChangedAttributes().keySet();
        if(changes.contains(Device.ATTR_NAME)) {
            // clear the current config so the sort will be reapplied
            currentConfig.clear();
            updateView();
        } else {
            logger.debug("not updating on device model change because the change did not include the name");
        }
    }

    public List<DeviceModel> getDevices() {
        return devices.get();
    }

    private DeviceModel getDevice(String id) {
        List<DeviceModel> devices = getDevices();
        if(devices == null) {
            return null;
        }
        for(DeviceModel device : devices) {
            if(device.getId().equals(id)) {
                return device;
            }
        }
        return null;
    }

    private void showError(Throwable t) {
        Callback cb = getCallback();
        if(cb != null) {
            cb.showError(Errors.translate(t));
        }
    }
}
