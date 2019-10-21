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

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.subsystem.lawnandgarden.model.LawnAndGardenDashboardCardModel;
import arcus.cornea.utils.AddressableModelSource;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.CapabilityUtils;
import arcus.cornea.utils.Listeners;
import com.iris.capability.util.Addresses;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.IrrigationController;
import com.iris.client.capability.IrrigationZone;
import com.iris.client.capability.LawnNGardenSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public class LawnAndGardenDashboardCardController extends BaseLawnAndGardenController<LawnAndGardenDashboardCardController.Callback> {

    public interface Callback {

        void showUnsatisfiableCopy();

        void showNoActivityCopy();

        void showSummary(LawnAndGardenDashboardCardModel model);
    }

    private static final LawnAndGardenDashboardCardController instance;

    static {
        instance = new LawnAndGardenDashboardCardController();
    }

    public static LawnAndGardenDashboardCardController instance() {
        return instance;
    }

    private AddressableModelSource<DeviceModel> irrigationcontroller;

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
                    changed.contains(Device.ATTR_NAME) ||
                    changed.contains(IrrigationZone.ATTR_ZONESTATE) ||
                    changed.contains(DeviceConnection.ATTR_STATE) ||
                    changed.contains(IrrigationController.ATTR_NUMZONES) ||
                    changed.contains(IrrigationController.ATTR_CONTROLLERSTATE) ||
                    changed.contains(IrrigationController.ATTR_MAXDAILYTRANSITIONS) ||
                    changed.contains(IrrigationController.ATTR_MAXIRRIGATIONTIME) ||
                    changed.contains(IrrigationController.ATTR_RAINDELAY) ||
                    changed.contains(IrrigationController.ATTR_ZONESINFAULT) ||
                    changed.contains(IrrigationController.ATTR_BUDGET) ||
                    changed.contains(IrrigationController.ATTR_MAXTRANSITIONS)
                ) {
                  updateView();
                }
            }
            else {
                updateView();
            }

        }
    });

    LawnAndGardenDashboardCardController() {
        super();
        irrigationcontroller = CachedModelSource.newSource();
        init();
    }


    public void init() {
        super.init();
        this.irrigationcontroller.addModelListener(modelListeners);
    }

    //TODO: Russ - update to use the right subsystem and use actual information to populate model
    @Override
    protected void updateView(Callback callback, LawnNGardenSubsystem subsystem) {
        if(!Boolean.TRUE.equals(subsystem.getAvailable())) {
            callback.showUnsatisfiableCopy();
            return;
        }

        if(subsystem.getControllers().size() == 0) {
            callback.showNoActivityCopy();
            return;
        }

        LawnAndGardenDashboardCardModel model = new LawnAndGardenDashboardCardModel();
        int zones = 0;

        Map<String, Map<String, String>> zoneMapping = new HashMap<>();
        Map<String, Map<String, Object>> map = subsystem.getZonesWatering();
        for(Map.Entry<String, Map<String, Object>> controller : map.entrySet()) {
            String controllerAddress = controller.getKey();
            DeviceModel deviceModel = (DeviceModel)CorneaClientFactory.getModelCache().get(controllerAddress);

            if (deviceModel == null) {
                return;
            }

            CapabilityUtils capabilityUtils = new CapabilityUtils(deviceModel);
            if (capabilityUtils != null) {
                for (String instance : capabilityUtils.getInstanceNames()) {
                    String status = (String) capabilityUtils.getInstanceValue(instance, IrrigationZone.ATTR_ZONESTATE);
                    if(IrrigationZone.ZONESTATE_WATERING.equals(status)) {
                        zones += 1;
                    }
                }
            }
        }

        model.setDeviceId(Addresses.getId((String)subsystem.getControllers().toArray()[0]));
        model.setCurrentlyWateringZoneCount(zones);

        Set<String> controllerAddresses = subsystem.getControllers();
        for(String controllerAddress : controllerAddresses) {
            DeviceModel deviceModel = (DeviceModel)CorneaClientFactory.getModelCache().get(controllerAddress);

            if (deviceModel == null) {
                continue;
            }
            CapabilityUtils capabilityUtils = new CapabilityUtils(deviceModel);
            if (capabilityUtils != null) {
                for (String instance : capabilityUtils.getInstanceNames()) {
                    String name = (String) capabilityUtils.getInstanceValue(instance, IrrigationZone.ATTR_ZONENAME);
                    Double number = (Double) capabilityUtils.getInstanceValue(instance, IrrigationZone.ATTR_ZONENUM);
                    int zoneNum = 1;
                    if (number != null) {
                        zoneNum = number.intValue();
                    }
                    if(name == null) {
                        name = "Zone "+Integer.toString(zoneNum);
                    }
                    if(zoneMapping.get(controllerAddress) == null) {
                        zoneMapping.put(controllerAddress, new HashMap<String, String>());
                    }
                    Map<String, String> tempZone = zoneMapping.get(controllerAddress);
                    tempZone.put(instance, name);
                    zoneMapping.put(controllerAddress, tempZone);
                }
            }
        }
        Map<String, Object> nextEvent = subsystem.getNextEvent();
        model.setNextEventTime(0.0);
        if(nextEvent != null) { // Why not use IrrigationTransitionEvent Bean?
            if(nextEvent.get("zone") != null) {
                if(zoneMapping != null && nextEvent.get("controller") != null) {
                    //if we don't find the controller in the zone mapping, it may have been deleted.
                    if(zoneMapping.get((String)nextEvent.get("controller")) == null) {
                        model.setNextEventTitle("");
                    }
                    else {
                        model.setNextEventTitle(getZoneName(nextEvent.get("controller"), (String)nextEvent.get("zone")));
                    }
                }
                else {
                    model.setNextEventTitle(getZoneName(nextEvent.get("controller"), (String)nextEvent.get("zone")));
                }
            }
            if(nextEvent.get("startTime") != null) {
                model.setNextEventTime((double)nextEvent.get("startTime"));
            }
        }

        callback.showSummary(model);
    }

    protected String getZoneName(Object controllerAddress, String zone) {
        Model model = CorneaClientFactory.getModelCache().get(String.valueOf(controllerAddress));
        if (zone == null) {
            zone = "";
        }

        if (model == null) {
            return String.format("Zone %s", zone.replace("z", ""));
        }

        String name = (String) model.get(String.format("%s:%s", IrrigationZone.ATTR_ZONENAME, zone));
        if (name == null || name.trim().length() == 0) {
            Number zoneNum = (Number) model.get(String.format("%s:%s", IrrigationZone.ATTR_ZONENUM, zone));
            name = String.format("Zone %s", zoneNum == null ? "" : zoneNum.intValue());
        }
        return name;
    }

    @Override
    protected void onSubsystemLoaded(ModelAddedEvent event) {
        super.onSubsystemLoaded(event);
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> changes = event.getChangedAttributes().keySet();
        if(
                changes.contains(LawnNGardenSubsystem.ATTR_CONTROLLERS) ||
                changes.contains(LawnNGardenSubsystem.ATTR_ODDSCHEDULES) ||
                changes.contains(LawnNGardenSubsystem.ATTR_INTERVALSCHEDULES) ||
                changes.contains(LawnNGardenSubsystem.ATTR_WEEKLYSCHEDULES) ||
                changes.contains(LawnNGardenSubsystem.ATTR_EVENSCHEDULES) ||
                changes.contains(LawnNGardenSubsystem.ATTR_SCHEDULESTATUS) ||
                changes.contains(LawnNGardenSubsystem.ATTR_ZONESWATERING)
        ){
            updateView();
        }
        super.onSubsystemChanged(event);
    }

    private boolean isOnline(AddressableModelSource<DeviceModel> model) {
        if(!model.isLoaded()) {
            return false;
        }
        return DeviceConnection.STATE_ONLINE.equals(model.get().get(DeviceConnection.ATTR_STATE));
    }
}
