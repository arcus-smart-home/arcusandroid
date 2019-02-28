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

import arcus.cornea.subsystem.climate.model.ClimateBadge;
import arcus.cornea.subsystem.climate.model.ClimateBadgeType;
import arcus.cornea.subsystem.climate.model.DashboardCardModel;
import arcus.cornea.utils.AddressableModelSource;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.TemperatureUtils;
import com.iris.client.capability.ClimateSubsystem;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.RelativeHumidity;
import com.iris.client.capability.SpaceHeater;
import com.iris.client.capability.Temperature;
import com.iris.client.capability.Thermostat;
import com.iris.client.capability.TwinStar;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;


import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class ClimateDashboardCardController extends BaseClimateController<ClimateDashboardCardController.Callback> {

    public interface Callback {

        void showUnsatisfiableCopy();

        void showNoActivityCopy();

        void showSummary(DashboardCardModel model);
    }

    private static final ClimateDashboardCardController instance;

    static {
        instance = new ClimateDashboardCardController();
    }

    public static ClimateDashboardCardController instance() {
        return instance;
    }

    private AddressableModelSource<DeviceModel> temperature;
    private AddressableModelSource<DeviceModel> humidity;

    private Listener<DeviceModel> onModelLoaded = Listeners.runOnUiThread(new Listener<DeviceModel>() {
        @Override
        public void onEvent(DeviceModel deviceModel) {
            updateView();
        }
    });
    private Listener<ModelEvent> modelListeners = Listeners.runOnUiThread(new Listener<ModelEvent>() {
        @Override
        public void onEvent(ModelEvent modelEvent) {
            if (modelEvent instanceof ModelChangedEvent) {
                Set<String> changed = ((ModelChangedEvent) modelEvent).getChangedAttributes().keySet();
                if(
                        changed.contains(DeviceConnection.ATTR_STATE) ||
                        changed.contains(Thermostat.ATTR_COOLSETPOINT) ||
                        changed.contains(Thermostat.ATTR_HEATSETPOINT) ||
                        changed.contains(Thermostat.ATTR_HVACMODE) ||
                        changed.contains(Temperature.ATTR_TEMPERATURE) ||
                        changed.contains(RelativeHumidity.ATTR_HUMIDITY) ||
                        changed.contains(SpaceHeater.ATTR_SETPOINT) ||
                        changed.contains(TwinStar.ATTR_ECOMODE)
                ) {
                  updateView();
                }
            }
            else {
                updateView();
            }

        }
    });

    ClimateDashboardCardController() {
        super();
        temperature = CachedModelSource.newSource();
        humidity = CachedModelSource.newSource();
        init();
    }

    // For test cases
//    ClimateDashboardCardController(
//            ModelSource<SubsystemModel> subsystem,
//            AddressableModelSource<DeviceModel> thermostat
//    ) {
//        super(subsystem);
//        this.thermostat = thermostat;
//        init();
//    }

    public void init() {
        super.init();
        this.humidity.addModelListener(modelListeners);
        this.temperature.addModelListener(modelListeners);
    }

    @Override
    protected void updateView(Callback callback, ClimateSubsystem subsystem) {
        if(!Boolean.TRUE.equals(subsystem.getAvailable())) {
            callback.showUnsatisfiableCopy();
            return;
        }

        boolean hasPrimaryTemperature = !StringUtils.isEmpty(subsystem.getPrimaryTemperatureDevice());
        boolean hasPrimaryHumidity = !StringUtils.isEmpty(subsystem.getPrimaryHumidityDevice());

        if(
                (hasPrimaryTemperature && !temperature.isLoaded()) ||
                (hasPrimaryHumidity && !humidity.isLoaded())
        ) {
            // TODO this shouldn't be necessary
            temperature.setAddress(subsystem.getPrimaryTemperatureDevice());
            temperature.load();
            humidity.setAddress(subsystem.getPrimaryHumidityDevice());
            humidity.load();
            return;
        }

        DashboardCardModel model = new DashboardCardModel();
        List<ClimateBadge> badges = new ArrayList<>(3);
        if(hasPrimaryTemperature) {
            if(temperature.get() != null) {
                model.setPrimaryTemperatureDeviceId(temperature.get().getId());
            }
            if (isOnline(temperature)) {
                if(subsystem.getTemperature() !=null) {
                    model.setTemperature(TemperatureUtils.roundCelsiusToFahrenheit(subsystem.getTemperature()));
                    model.setPrimaryTemperatureOffline(false);
                }
            }
            else {
                model.setTemperature(0);
                model.setPrimaryTemperatureOffline(true);
            }
            if(temperature.get() != null && temperature.get().getDevtypehint() != null && temperature.get().getDevtypehint().equals("TCCThermostat")) {
                model.setIsTemperatureCloudDevice(true);
            }
        }
        if (hasPrimaryHumidity && isOnline(humidity)) {
            if(subsystem.getHumidity() != null) {
                ClimateBadge badge = new ClimateBadge();
                badge.setType(ClimateBadgeType.HUMIDITY);
                badge.setLabel((String.format("%.0f",subsystem.getHumidity())));
                badges.add(badge);
            }
            else if (humidity.isLoaded()) {
                Number deviceHumidity = (Number) humidity.get().get(RelativeHumidity.ATTR_HUMIDITY);
                if (deviceHumidity != null) {
                    ClimateBadge badge = new ClimateBadge();
                    badge.setType(ClimateBadgeType.HUMIDITY);
                    badge.setLabel(String.format("%s%%", deviceHumidity.doubleValue()));
                    badges.add(badge);
                }
            }
        }
        int closedVents = count(subsystem.getClosedVents());
        if(closedVents > 0) {
            ClimateBadge badge = new ClimateBadge();
            badge.setType(ClimateBadgeType.VENT);
            badge.setLabel(closedVents + " Closed");
            badges.add(badge);
        }
        int activeFans = count(subsystem.getActiveFans());
        if(activeFans > 0) {
            ClimateBadge badge = new ClimateBadge();
            badge.setType(ClimateBadgeType.FAN);
            badge.setLabel(activeFans + " On");
            badges.add(badge);
        }

        int activeHeaters = count(subsystem.getActiveHeaters());
        if(activeHeaters > 0) {
            ClimateBadge badge = new ClimateBadge();
            badge.setType(ClimateBadgeType.HEATER);
            badge.setLabel(activeHeaters + " On");
            badges.add(badge);
        }
        model.setBadges(badges);

        if(hasPrimaryTemperature && temperature.get() != null && temperature.get().getCaps().contains(Thermostat.NAMESPACE)) {
            if(isOnline(temperature)) {
                Thermostat t = (Thermostat) temperature.get();
                String hvacMode = String.valueOf(t.getHvacmode());

                Integer heat = null;
                if (t.getHeatsetpoint() != null) {
                    heat = TemperatureUtils.roundCelsiusToFahrenheit(t.getHeatsetpoint());
                }

                Integer cool = null;
                if (t.getCoolsetpoint() != null) {
                    cool = TemperatureUtils.roundCelsiusToFahrenheit(t.getCoolsetpoint());
                }

                switch (hvacMode) {
                    case Thermostat.HVACMODE_AUTO:
                        if (heat != null && cool != null) {
                            model.setThermostatLabel(String.format("%d°-%d°", heat, cool));
                        }
                        break;

                    case Thermostat.HVACMODE_COOL:
                        if (cool != null) {
                            model.setThermostatLabel(String.format("%d°", cool));
                        }
                        break;

                    case Thermostat.HVACMODE_HEAT:
                        if (heat != null) {
                            model.setThermostatLabel(String.format("%d°", heat));
                        }
                        break;

                    case Thermostat.HVACMODE_OFF:
                        //model.setThermostatLabel("Off");
                        model.setThermostatLabel("");
                        break;
                }
            }
            else {
                //model.setThermostatLabel("Thermostat Offline");
                model.setThermostatLabel("");
            }
        }
        else if (hasPrimaryTemperature && temperature.get() != null && temperature.get().getCaps().contains(SpaceHeater.NAMESPACE)) {
            SpaceHeater heater = (SpaceHeater) temperature.get();
            Integer heat = null;
            if(temperature.get().getCaps().contains(Temperature.NAMESPACE)) {
                heat = TemperatureUtils.roundCelsiusToFahrenheit(heater.getSetpoint());
            }
            if (SpaceHeater.HEATSTATE_ON.equals(heater.getHeatstate())) {
                model.setThermostatLabel(String.format("%d°", heat));
            }
            else {
                model.setThermostatLabel("");
            }
        }
        else if (hasPrimaryTemperature && temperature.get() != null) {
            model.setThermostatLabel("");
        }

        if(hasPrimaryTemperature || model.isBadgeAvailable()) {
            callback.showSummary(model);
        }
        else {
            callback.showNoActivityCopy();
        }
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        String temperatureAddress = getClimateSubsystem().getPrimaryTemperatureDevice();
        if(!StringUtils.isEmpty(temperatureAddress)) {
            temperature.setAddress(temperatureAddress);
            temperature.load().onSuccess(onModelLoaded);
        }
        String humidityAddress = getClimateSubsystem().getPrimaryHumidityDevice();
        if(!StringUtils.isEmpty(humidityAddress)) {
            humidity.setAddress(humidityAddress);
            humidity.load().onSuccess(onModelLoaded);
        }
        super.onSubsystemLoaded(event);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> changes = event.getChangedAttributes().keySet();
        if(changes.contains(ClimateSubsystem.ATTR_PRIMARYTEMPERATUREDEVICE)) {
            temperature.setAddress(getClimateSubsystem().getPrimaryTemperatureDevice());
            temperature.load();
        }
        if(changes.contains(ClimateSubsystem.ATTR_PRIMARYHUMIDITYDEVICE)) {
            humidity.setAddress(getClimateSubsystem().getPrimaryHumidityDevice());
            humidity.load();
        }
        if(
            changes.contains(ClimateSubsystem.ATTR_ACTIVEFANS) ||
            changes.contains(ClimateSubsystem.ATTR_CLOSEDVENTS) ||
            changes.contains(ClimateSubsystem.ATTR_HUMIDITY) ||
            changes.contains(ClimateSubsystem.ATTR_ACTIVEHEATERS) ||
            changes.contains(ClimateSubsystem.ATTR_TEMPERATURE)
        ) {
            updateView();
        }
        super.onSubsystemChanged(event);
    }

    private boolean isOnline(AddressableModelSource<DeviceModel> model) {
        if(!model.isLoaded()) {
            return false;
        }
        return !DeviceConnection.STATE_OFFLINE.equals(model.get().get(DeviceConnection.ATTR_STATE));
    }
}
