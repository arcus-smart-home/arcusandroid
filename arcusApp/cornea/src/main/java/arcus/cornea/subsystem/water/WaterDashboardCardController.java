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
package arcus.cornea.subsystem.water;

import arcus.cornea.subsystem.water.model.DashboardCardModel;
import arcus.cornea.subsystem.water.model.WaterBadge;
import arcus.cornea.subsystem.water.model.WaterBadgeType;
import arcus.cornea.utils.AddressableModelSource;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.TemperatureUtils;
import com.iris.client.capability.WaterSubsystem;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Valve;
import com.iris.client.capability.WaterSoftener;
import com.iris.client.capability.WaterHeater;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class WaterDashboardCardController extends BaseWaterController<WaterDashboardCardController.Callback> {

    public interface Callback {

        void showUnsatisfiableCopy();

        void showNoActivityCopy();

        void showSummary(DashboardCardModel model);
    }

    private static final WaterDashboardCardController instance;

    static {
        instance = new WaterDashboardCardController();
    }

    public static WaterDashboardCardController instance() {
        return instance;
    }

    private AddressableModelSource<DeviceModel> waterSoftener;
    private AddressableModelSource<DeviceModel> waterHeater;
    private AddressableModelSource<DeviceModel> waterShutoff;

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
                if(changed.contains(DeviceConnection.ATTR_STATE) ||
                    changed.contains(WaterHeater.ATTR_HOTWATERLEVEL) ||
                    changed.contains(WaterHeater.ATTR_SETPOINT) ||
                    changed.contains(WaterHeater.ATTR_HEATINGSTATE) ||
                    changed.contains(Valve.ATTR_VALVESTATE) ||
                    changed.contains(Valve.ATTR_VALVESTATECHANGED) ||
                    changed.contains(WaterSoftener.ATTR_CURRENTSALTLEVEL)) {
                  updateView();
                }
            }
            else {
                updateView();
            }

        }
    });

    WaterDashboardCardController() {
        super();
        waterHeater = CachedModelSource.newSource();
        waterSoftener = CachedModelSource.newSource();
        waterShutoff = CachedModelSource.newSource();
        init();
    }

    public void init() {
        super.init();
        this.waterHeater.addModelListener(modelListeners);
        this.waterSoftener.addModelListener(modelListeners);
        this.waterShutoff.addModelListener(modelListeners);
    }

    @Override
    protected void updateView(Callback callback, WaterSubsystem subsystem) {

        if(!Boolean.TRUE.equals(subsystem.getAvailable())) {
            callback.showUnsatisfiableCopy();
            return;
        }

        boolean hasPrimaryWaterHeater = !StringUtils.isEmpty(subsystem.getPrimaryWaterHeater());
        boolean hasPrimaryWaterSoftener = !StringUtils.isEmpty(subsystem.getPrimaryWaterSoftener());

        if((hasPrimaryWaterHeater && !waterHeater.isLoaded()) ||
           (hasPrimaryWaterSoftener&& !waterSoftener.isLoaded())) {
            // TODO this shouldn't be necessary
            waterHeater.setAddress(subsystem.getPrimaryWaterHeater());
            waterHeater.load();
            waterSoftener.setAddress(subsystem.getPrimaryWaterSoftener());
            waterSoftener.load();
            loadPrimaryShutoffValve(getWaterSubsystem());
            return;
        }

        DashboardCardModel model = new DashboardCardModel();
        List<WaterBadge> badges = new ArrayList<>(3);
        if(hasPrimaryWaterHeater) {
            if (isOnline(waterHeater)) {
                if(waterHeater.get() instanceof WaterHeater) {
                    WaterHeater waterHeaterInstance = (WaterHeater)waterHeater.get();

                    if(waterHeaterInstance.getSetpoint() !=null) {
                        model.setTemperature(TemperatureUtils.roundCelsiusToFahrenheit(waterHeaterInstance.getSetpoint()));
                        model.setPrimaryWaterHeaterOffline(false);
                    }
                    if(waterHeaterInstance.getHotwaterlevel() != null) {
                        model.setWaterHeaterWaterLevel(waterHeaterInstance.getHotwaterlevel());
                    }
                    if(waterHeaterInstance.getHeatingstate() != null) {
                        model.isHeating(waterHeaterInstance.getHeatingstate());
                    }
                    if(waterHeaterInstance.getAddress() != null) {
                        model.setWaterHeaterAddress(waterHeaterInstance.getId());
                    }
                }
            }
            else {
                model.setTemperature(0);
                model.setPrimaryWaterHeaterOffline(true);
                model.isHeating(false);
            }
        }
        if(hasPrimaryWaterSoftener && isOnline(waterSoftener)) {
            if(waterSoftener.get() instanceof WaterSoftener) {
                WaterSoftener waterSoftenerInstance = (WaterSoftener)waterSoftener.get();
                boolean saltLevelEnabled = false;
                if(waterSoftenerInstance.getSaltLevelEnabled() != null) {
                    saltLevelEnabled = waterSoftenerInstance.getSaltLevelEnabled();
                }
                if (!saltLevelEnabled) {
                    WaterBadge badge = new WaterBadge();
                    badge.setType(WaterBadgeType.WATER_SOFTENER);
                    badge.setLabel("");
                    badges.add(badge);
                } else {
                    WaterBadge badge = new WaterBadge();
                    badge.setType(WaterBadgeType.WATER_SOFTENER);
                    badge.setLabel(Integer.toString((waterSoftenerInstance.getCurrentSaltLevel() * 100) / waterSoftenerInstance.getMaxSaltLevel()));
                    badges.add(badge);
                }

                if (waterSoftenerInstance.getAddress() != null) {
                    model.setWaterSoftenerDeviceId(waterSoftenerInstance.getId());
                }
            }
        }

        if(waterShutoff != null) {
            if(waterShutoff.get() instanceof Valve) {
                Valve shutoffInstance = (Valve)waterShutoff.get();
                WaterBadge badge = new WaterBadge();
                badge.setType(WaterBadgeType.WATER_VALVE);
                if(shutoffInstance != null) {
                    if(shutoffInstance.getValvestate() != null) {
                        badge.setLabel(shutoffInstance.getValvestate());
                    }
                }
                badges.add(badge);

                if(shutoffInstance.getAddress() != null) {
                    model.setValveDeviceId(shutoffInstance.getId());
                }
            }
        }

        model.setBadges(badges);

        if(hasPrimaryWaterHeater || model.isBadgeAvailable()) {
            callback.showSummary(model);
        }
        else {
            callback.showNoActivityCopy();
        }
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        String waterHeaterAddress = getWaterSubsystem().getPrimaryWaterHeater();
        if(!StringUtils.isEmpty(waterHeaterAddress)) {
            waterHeater.setAddress(waterHeaterAddress);
            waterHeater.load().onSuccess(onModelLoaded);
        }
        String waterSoftenerAddress = getWaterSubsystem().getPrimaryWaterSoftener();
        if(!StringUtils.isEmpty(waterSoftenerAddress)) {
            waterSoftener.setAddress(waterSoftenerAddress);
            waterSoftener.load().onSuccess(onModelLoaded);
        }
        loadPrimaryShutoffValve(getWaterSubsystem());
        //String waterShutoffAddress = loadPrimaryShutoffValve(getWaterSubsystem());
        /*if(!StringUtils.isEmpty(waterShutoffAddress)) {
            waterShutoff.setAddress(waterShutoffAddress);
            waterShutoff.load();
        }*/
        super.onSubsystemLoaded(event);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> changes = event.getChangedAttributes().keySet();
        if(changes.contains(WaterSubsystem.ATTR_PRIMARYWATERHEATER)) {
            waterHeater.setAddress(getWaterSubsystem().getPrimaryWaterHeater());
            waterHeater.load();
        }
        if(changes.contains(WaterSubsystem.ATTR_PRIMARYWATERSOFTENER)) {
            waterSoftener.setAddress(getWaterSubsystem().getPrimaryWaterSoftener());
            waterSoftener.load();
        }
        if(changes.contains(WaterSubsystem.ATTR_CLOSEDWATERVALVES)) {
            loadPrimaryShutoffValve(getWaterSubsystem());
            //waterShutoff.setAddress(loadPrimaryShutoffValve(getWaterSubsystem()));
            //waterShutoff.load();
            updateView();
        }
        if(changes.contains(WaterSubsystem.ATTR_WATERDEVICES)) {
            //need to see if ours was removed or one was added, not just opened or closed

            loadPrimaryShutoffValve(getWaterSubsystem());
            //waterShutoff.setAddress(loadPrimaryShutoffValve(getWaterSubsystem()));
            //waterShutoff.load();
            updateView();
        }
        super.onSubsystemChanged(event);
    }

    private void loadPrimaryShutoffValve(WaterSubsystem subsystem) {
        for (String address : subsystem.getWaterDevices()) {
            if(address.equals(subsystem.getPrimaryWaterSoftener()) ||
               address.equals(subsystem.getPrimaryWaterHeater())) {
                continue;
            }

            waterShutoff.setAddress(address);
            waterShutoff.load();
            if(waterShutoff.get() instanceof Valve) {
                return;
            }
        }
        //if we have one, we need to remove it.
        if(!waterShutoff.getAddress().equals("")) {
            waterShutoff = CachedModelSource.newSource();
            waterShutoff.addModelListener(modelListeners);
        }

        //waterShutoff.setAddress("");
        //waterShutoff.load();
        return;
    }

    private boolean isOnline(AddressableModelSource<DeviceModel> model) {
        if(!model.isLoaded()) {
            return false;
        }
        return DeviceConnection.STATE_ONLINE.equals(model.get().get(DeviceConnection.ATTR_STATE));
    }
}
