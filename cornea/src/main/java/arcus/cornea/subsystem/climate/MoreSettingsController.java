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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.error.Errors;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.climate.model.DeviceSettingsModel;
import arcus.cornea.subsystem.climate.model.MoreSettingsModel;
import arcus.cornea.subsystem.climate.model.ThermostatSettingsModel;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.capability.util.Addresses;
import com.iris.client.bean.ThermostatScheduleStatus;
import com.iris.client.capability.ClimateSubsystem;
import com.iris.client.capability.Device;
import com.iris.client.capability.RelativeHumidity;
import com.iris.client.capability.Temperature;
import com.iris.client.capability.Thermostat;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.SubsystemModel;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MoreSettingsController extends BaseClimateController<MoreSettingsController.Callback> {

    private static final Logger logger = LoggerFactory.getLogger(MoreSettingsController.class);
    private static final MoreSettingsController instance;

    static {
        instance = new MoreSettingsController();
        instance.init();
    }

    public static MoreSettingsController instance() {
        return instance;
    }

    private final Listener<List<DeviceModel>> onDeviceListChange = Listeners.runOnUiThread(new Listener<List<DeviceModel>>() {
        @Override
        public void onEvent(List<DeviceModel> deviceModels) {
            onDeviceListChange();
        }
    });

    private final Listener<ModelChangedEvent> onDeviceChange = Listeners.runOnUiThread(new Listener<ModelChangedEvent>() {
        @Override
        public void onEvent(ModelChangedEvent modelChangedEvent) {
            onDeviceChange(modelChangedEvent);
        }
    });

    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onError(throwable);
        }
    });

    private final Function<DeviceModel, DeviceSettingsModel> deviceSettingsModelTransformer = new Function<DeviceModel, DeviceSettingsModel>() {
        @Override
        public DeviceSettingsModel apply(DeviceModel deviceModel) {
            DeviceSettingsModel m = new DeviceSettingsModel();
            m.setDeviceId(deviceModel.getId());
            m.setName(deviceModel.getName());
            return m;
        }
    };

    private final Comparator<ThermostatSettingsModel> thermoSettingSorter = new Comparator<ThermostatSettingsModel>() {
        @Override
        public int compare(ThermostatSettingsModel thermostatSettingsModel, ThermostatSettingsModel t1) {
            return thermostatSettingsModel.getName().compareTo(t1.getName());
        }
    };

    private final Comparator<DeviceSettingsModel> devSettingsSorter = new Comparator<DeviceSettingsModel>() {
        @Override
        public int compare(DeviceSettingsModel deviceSettingsModel, DeviceSettingsModel t1) {
            return deviceSettingsModel.getName().compareTo(t1.getName());
        }
    };

    private final Predicate<DeviceModel> isThermostat = new Predicate<DeviceModel>() {
        @Override
        public boolean apply(DeviceModel deviceModel) {
            return deviceModel instanceof Thermostat;
        }
    };

    private final Predicate<DeviceModel> isTemperatureDevice  = new Predicate<DeviceModel>() {
        @Override
        public boolean apply(DeviceModel deviceModel) {
            return deviceModel instanceof Temperature;
        }
    };

    private final Predicate<DeviceModel> isHumidityDevice = new Predicate<DeviceModel>() {
        @Override
        public boolean apply(DeviceModel deviceModel) {
            return deviceModel instanceof RelativeHumidity;
        }
    };

    private AddressableListSource<DeviceModel> allDevices;
    private List<ListenerRegistration> schedulerRegistrations = new LinkedList<>();

    MoreSettingsController() {
        this(SubsystemController.instance().getSubsystemModel(ClimateSubsystem.NAMESPACE),
                DeviceModelProvider.instance().newModelList());
    }

    MoreSettingsController(ModelSource<SubsystemModel> subsystem,
                           AddressableListSource<DeviceModel> allDevices) {
        super(subsystem);
        this.allDevices = allDevices;
    }

    @Override
    public void init() {
        this.allDevices.addListener(onDeviceListChange);
        this.allDevices.addModelListener(onDeviceChange, ModelChangedEvent.class);
        super.init();
    }

    @Override
    protected boolean isLoaded() {
        if(!super.isLoaded()) {
            return false;
        }
        return allDevices.isLoaded();
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        ClimateSubsystem c = getClimateSubsystem();
        allDevices.setAddresses(getAllDeviceAddresses(c.getThermostats(), c.getTemperatureDevices(), c.getHumidityDevices()));
        super.onSubsystemLoaded(event);
    }

    private List<String> getAllDeviceAddresses(Set<String> thermostats, Set<String> temperature, Set<String> humidity) {
        Set<String> set = new HashSet<>();
        set.addAll(thermostats);
        set.addAll(temperature);
        set.addAll(humidity);
        return new ArrayList<>(set);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> changes = event.getChangedAttributes().keySet();
        ClimateSubsystem c = getClimateSubsystem();

        if(changes.contains(ClimateSubsystem.ATTR_TEMPERATUREDEVICES) ||
           changes.contains(ClimateSubsystem.ATTR_HUMIDITYDEVICES) ||
           changes.contains(ClimateSubsystem.ATTR_THERMOSTATS)) {

           allDevices.setAddresses(getAllDeviceAddresses(c.getThermostats(), c.getTemperatureDevices(), c.getHumidityDevices()));
           return;
        }

        if(changes.contains(ClimateSubsystem.ATTR_PRIMARYHUMIDITYDEVICE) ||
           changes.contains(ClimateSubsystem.ATTR_PRIMARYTEMPERATUREDEVICE)) {
           updateView();
        }
    }

    @Override
    protected void updateView(Callback callback) {
        if(!isLoaded()) {
            logger.debug("ignoring update more settings view until the subsystem is fully loaded");
            return;
        }

        MoreSettingsModel model = createModel();
        callback.showSettings(model);
    }

    public void setThermostatEnabled(String deviceId, boolean enabled) {
        if(enabled) {
            getClimateSubsystem()
                    .enableScheduler(Addresses.toObjectAddress(Device.NAMESPACE, deviceId))
                    .onFailure(onError);
        }
        else {
            getClimateSubsystem()
                    .disableScheduler(Addresses.toObjectAddress(Device.NAMESPACE, deviceId))
                    .onFailure(onError);
        }
    }

    public void setDashboardTemperature(String deviceId) {
        if(!isLoaded()) {
            return;
        }
        ClimateSubsystem c = getClimateSubsystem();
        c.setPrimaryTemperatureDevice(Addresses.toObjectAddress(Device.NAMESPACE, deviceId));
        ((SubsystemModel) c).commit().onFailure(onError);
    }

    public void setDashboardHumidity(String deviceId) {
        if(!isLoaded()) {
            return;
        }
        ClimateSubsystem c = getClimateSubsystem();
        c.setPrimaryHumidityDevice(Addresses.toObjectAddress(Device.NAMESPACE, deviceId));
        ((SubsystemModel) c).commit().onFailure(onError);
    }

    public void selectDashboardTemperatureDevice() {
        Callback callback = getCallback();
        if(callback == null) {
            return;
        }
        if(!isLoaded()) {
            return;
        }
        callback.promptSelectTemperatureDevice(getTempDeviceSettings());
    }

    public void selectDashboardHumidityDevice() {
        Callback callback = getCallback();
        if(callback == null) {
            return;
        }
        if(!isLoaded()) {
            return;
        }

        callback.promptSelectHumidityDevice(getHumidityDeviceSettings());
    }

    private void onDeviceListChange() {
        updateView();
    }

    private void onDeviceChange(ModelChangedEvent mce) {
        Set<String> changes = mce.getChangedAttributes().keySet();
        if(changes.contains(Device.ATTR_NAME)) {
            updateView();
        } else {
            logger.debug("ignoring device change that didn't contain the name");
        }
    }

    private void onError(Throwable t) {
        Callback cb = getCallback();
        if(cb != null) {
            cb.promptError(Errors.translate(t));
        }
    }

    private List<DeviceSettingsModel> getHumidityDeviceSettings() {
       return getDeviceSettings(getHumidityDevices());
    }

    private List<DeviceSettingsModel> getTempDeviceSettings() {
        return getDeviceSettings(getTempDevices());
    }

    private List<DeviceSettingsModel> getDeviceSettings(List<DeviceModel> models) {
        if(models == null) {
            return ImmutableList.of();
        }
        List<DeviceSettingsModel> toSort = Lists.newArrayList(Iterables.transform(models, deviceSettingsModelTransformer));
        Collections.sort(toSort, devSettingsSorter);
        return toSort;
    }

    private MoreSettingsModel createModel() {
        ClimateSubsystem c = getClimateSubsystem();
        MoreSettingsModel msm = new MoreSettingsModel();
        msm.setThermostats(getThermostatSettings());
        msm.setDashboardHumidity(createDeviceSettingsModel(c.getPrimaryHumidityDevice()));
        msm.setDashboardTemperature(createDeviceSettingsModel(c.getPrimaryTemperatureDevice()));
        return msm;
    }

    private List<ThermostatSettingsModel> getThermostatSettings() {
        List<DeviceModel> thermostats = getThermostats();
        List<ThermostatSettingsModel> settings = new ArrayList<>(thermostats.size());
        for(DeviceModel m : thermostats) {
            ThermostatSettingsModel tsm = new ThermostatSettingsModel();
            tsm.setName(m.getName());
            tsm.setDeviceId(m.getId());
            tsm.setScheduled(isScheduleEnabled(m));
            settings.add(tsm);
        }
        Collections.sort(settings, thermoSettingSorter);
        return settings;
    }

    private boolean isScheduleEnabled(DeviceModel d) {
        Map<String, Object> schedule =
            getClimateSubsystem()
                    .getThermostatSchedules()
                    .get(d.getAddress());
        if(schedule == null) {
            return false;
        }
        return Boolean.TRUE.equals(schedule.get(ThermostatScheduleStatus.ATTR_ENABLED));
    }

    private DeviceSettingsModel createDeviceSettingsModel(String address) {
        if(StringUtils.isBlank(address)) {
            return null;
        }

        DeviceModel d = getDevice(address);
        if(d == null) {
            return null;
        }

        return deviceSettingsModelTransformer.apply(d);
    }

    private DeviceModel getDevice(String address) {
        List<DeviceModel> devices = allDevices.get();
        if(devices == null) {
            return null;
        }
        for(DeviceModel d : devices) {
            if(d.getAddress().equals(address)) {
                return d;
            }
        }
        return null;
    }

    private List<DeviceModel> getThermostats() {
        return filterDevices(isThermostat);
    }

    private List<DeviceModel> getTempDevices() {
        return filterDevices(isTemperatureDevice);
    }

    private List<DeviceModel> getHumidityDevices() {
        return filterDevices(isHumidityDevice);
    }

    private List<DeviceModel> filterDevices(Predicate<DeviceModel> p) {
        List<DeviceModel> m = allDevices.get();
        if(m == null) {
            return ImmutableList.of();
        }
        return Lists.newArrayList(Iterables.filter(m, p));
    }

    public interface Callback {

        void showSettings(MoreSettingsModel model);

        void promptSelectTemperatureDevice(List<DeviceSettingsModel> devices);

        void promptSelectHumidityDevice(List<DeviceSettingsModel> devices);

        void promptError(ErrorModel error);
    }

}
