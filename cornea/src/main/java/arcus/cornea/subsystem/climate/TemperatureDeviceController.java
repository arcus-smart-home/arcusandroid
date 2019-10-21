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

import com.google.common.collect.Lists;
import arcus.cornea.device.thermostat.ThermostatMode;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.climate.model.DeviceTemperatureModel;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import arcus.cornea.utils.TemperatureUtils;
import com.iris.client.capability.ClimateSubsystem;
import com.iris.client.capability.Device;
import com.iris.client.capability.RelativeHumidity;
import com.iris.client.capability.Temperature;
import com.iris.client.capability.Thermostat;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.SubsystemModel;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class TemperatureDeviceController extends BaseClimateController<TemperatureDeviceController.Callback> {
    public interface Callback {
        void showTemperatureDevices(List<DeviceTemperatureModel> devices);
        void updateTemperatureDevice(DeviceTemperatureModel device);
    }

    private static final Logger logger = LoggerFactory.getLogger(TemperatureDeviceController.class);
    private static final TemperatureDeviceController instance;

    static {
        instance = new TemperatureDeviceController();
        instance.init();
    }

    public static TemperatureDeviceController instance() {
        return instance;
    }

    private final Listener<List<DeviceModel>> onDeviceListChange = Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
        @Override
        public void onEvent(List<DeviceModel> deviceModels) {
            onDeviceListChange(deviceModels);
        }
    });

    private final Listener<ModelChangedEvent> onDeviceChange = Listeners.runOnUiThread(new Listener<ModelChangedEvent>() {
        @Override
        public void onEvent(ModelChangedEvent modelChangedEvent) {
            onDeviceChange(modelChangedEvent);
        }
    });

    private final Comparator<DeviceTemperatureModel> sorter = new Comparator<DeviceTemperatureModel>() {
        @Override
        public int compare(DeviceTemperatureModel m1, DeviceTemperatureModel m2) {
            boolean t1 = m1.isThermostat();
            boolean t2 = m2.isThermostat();

            if(t1 && !t2) {
                return -1;
            }
            if(t2 && !t1) {
                return 1;
            }

            return ObjectUtils.compare(m1.getName(), m2.getName());
        }
    };

    private AddressableListSource<DeviceModel> devices;

    TemperatureDeviceController() {
        this(SubsystemController.instance().getSubsystemModel(ClimateSubsystem.NAMESPACE),
             DeviceModelProvider.instance().newModelList());
    }

    TemperatureDeviceController(ModelSource<SubsystemModel> subsystemModel,
                                AddressableListSource<DeviceModel> devices) {
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
        return super.isLoaded() && devices.isLoaded();
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        ClimateSubsystem c = getClimateSubsystem();
        devices.setAddresses(Lists.newArrayList(c.getTemperatureDevices()));
        super.onSubsystemLoaded(event);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> changes = event.getChangedAttributes().keySet();
        if(changes.contains(ClimateSubsystem.ATTR_TEMPERATUREDEVICES)) {
            onTemperatureDeviceSetChange(getClimateSubsystem());
        } else {
            logger.debug("ignoring climate subsystem change that did include {}", ClimateSubsystem.ATTR_TEMPERATUREDEVICES);
        }
    }

    @Override
    protected void updateView(Callback callback) {
        if(!isLoaded()) {
            logger.debug("ignoring updating temperature devices view until the subsystem is fully loaded");
            return;
        }

        List<DeviceTemperatureModel> models = buildModels();
        callback.showTemperatureDevices(models);
    }

    private void onTemperatureDeviceSetChange(ClimateSubsystem c) {
        devices.setAddresses(Lists.newArrayList(c.getTemperatureDevices()));
    }

    private List<DeviceModel> getDevices() {
        return this.devices.get();
    }

    private void onDeviceListChange(List<DeviceModel> devices) {
        updateView();
    }

    private void onDeviceChange(ModelChangedEvent mce) {
        Set<String> changes = mce.getChangedAttributes().keySet();
        // rebuild the whole list to ensure sorting
        if(changes.contains(Device.ATTR_NAME)) {
            updateView();
        } else if(changes.contains(Temperature.ATTR_TEMPERATURE) ||
                  changes.contains(RelativeHumidity.ATTR_HUMIDITY)) {
            updateInstance((DeviceModel) mce.getModel());
        }
    }

    private void updateInstance(DeviceModel d) {
        Callback cb = getCallback();
        if(cb == null) {
            logger.debug("ignoring update to specific device {}, no callback set", d.getAddress());
            return;
        }

        if(!isLoaded()) {
            logger.debug("ignoring update to specific device until subsystem fully loaded");
            return;
        }

        DeviceTemperatureModel model = createModel(d);
        cb.updateTemperatureDevice(model);
    }

    private List<DeviceTemperatureModel> buildModels() {
        List<DeviceModel> devices = getDevices();
        List<DeviceTemperatureModel> models = new ArrayList<>(devices.size());
        for(DeviceModel d : devices) {
            DeviceTemperatureModel temperature = createModel(d);
            if(temperature != null) {
                models.add(temperature);
            }
        }
        Collections.sort(models, sorter);
        return models;
    }

    private DeviceTemperatureModel createModel(DeviceModel d) {
        DeviceTemperatureModel model = new DeviceTemperatureModel();
        model.setName(d.getName());
        model.setDeviceId(d.getId());

        Temperature temp = (Temperature) d;
        Double temperature = temp.getTemperature();
        if(temperature == null) {
            logger.warn("Skipping temperature device with no temperature set, name: [{}] address: [{}]", d.getAddress(), d.getName());
            return null;
        }
        model.setTemperature(TemperatureUtils.roundCelsiusToFahrenheit(temp.getTemperature()));

        if(d instanceof RelativeHumidity) {
            RelativeHumidity humidity = (RelativeHumidity) d;
            Double value = humidity.getHumidity();

            if (value != null) {
                model.setHumidity((int) Math.round(humidity.getHumidity()));
            }
        }

        if(d instanceof Thermostat) {
            Thermostat t = (Thermostat) d;
            Double cool = t.getCoolsetpoint();
            Double heat = t.getHeatsetpoint();
            if (cool != null) {
                model.setCoolSetPoint(TemperatureUtils.roundCelsiusToFahrenheit(cool));
            }
            if (heat != null) {
                model.setHeatSetPoint(TemperatureUtils.roundCelsiusToFahrenheit(heat));
            }
            if (t.getHvacmode() != null) {
                model.setThermostatMode(ThermostatMode.valueOf(t.getHvacmode()));
            }
            else {
                model.setThermostatMode(ThermostatMode.OFF);
            }
        }

        return model;
    }
}
