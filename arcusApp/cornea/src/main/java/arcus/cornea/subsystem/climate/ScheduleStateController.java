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

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.error.Errors;
import arcus.cornea.subsystem.climate.model.DeviceControlType;
import arcus.cornea.subsystem.climate.model.ScheduleStateModel;
import arcus.cornea.utils.CapabilityUtils;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.Listeners;
import com.iris.capability.util.Addresses;
import com.iris.client.bean.ThermostatScheduleStatus;
import com.iris.client.capability.ClimateSubsystem;
import com.iris.client.capability.Device;
import com.iris.client.capability.Fan;
import com.iris.client.capability.Schedule;
import com.iris.client.capability.SpaceHeater;
import com.iris.client.capability.Thermostat;
import com.iris.client.capability.Vent;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.SchedulerModel;
import com.iris.client.service.SchedulerService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ScheduleStateController extends BaseClimateController<ScheduleStateController.Callback> {

    public static final String CLIMATE = "CLIMATE";
    private static final Logger logger = LoggerFactory.getLogger(ScheduleStateController.class);

    private static final ScheduleStateController instance;

    static {
        instance = new ScheduleStateController();
        instance.init();
    }

    public static ScheduleStateController instance() {
        return instance;
    }

    private final Listener<SchedulerService.ListSchedulersResponse> schedulersLoadedListener = Listeners.runOnUiThread(new Listener<SchedulerService.ListSchedulersResponse>() {
        @Override
        public void onEvent(SchedulerService.ListSchedulersResponse listSchedulersResponse) {
            Map<String, SchedulerModel> list = new HashMap<String, SchedulerModel>();
            for (Map<String,Object> item : listSchedulersResponse.getSchedulers()) {
                SchedulerModel model = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(item);

                list.put(model.getTarget(), model);
            }
            schedulers = list;
            updateView();
        }
    });

    private final Listener<SchedulerService.GetSchedulerResponse> schedulerLoadedListener = Listeners.runOnUiThread(new Listener<SchedulerService.GetSchedulerResponse>() {
        @Override
        public void onEvent(SchedulerService.GetSchedulerResponse getSchedulerResponse) {
            schedulers = new HashMap();
            SchedulerModel model = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(getSchedulerResponse.getScheduler());
            schedulers.put(model.getTarget(), model);
            updateView();
        }
    });

    private final Listener<Throwable> onError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onError(throwable);
        }
    });
    private final Listener<ClimateSubsystem.EnableSchedulerResponse> onEnableSuccess = Listeners.runOnUiThread(
          new Listener<ClimateSubsystem.EnableSchedulerResponse>() {
              @Override
              public void onEvent(ClimateSubsystem.EnableSchedulerResponse e) {
                  updateView();
              }
          }
    );
    private final Listener<ClimateSubsystem.DisableSchedulerResponse> onDisableSuccess = Listeners.runOnUiThread(
          new Listener<ClimateSubsystem.DisableSchedulerResponse>() {
              @Override
              public void onEvent(ClimateSubsystem.DisableSchedulerResponse e) {
                  updateView();
              }
          }
    );

    private List<DeviceModel> devices;
    private Map<String, SchedulerModel> schedulers; // deviceId, scheduler
    private ListenerRegistration deviceRegistration = Listeners.empty();
    private ListenerRegistration schedulerRegistration = Listeners.empty();

    @Override
    protected boolean isLoaded() {
        return super.isLoaded() && schedulers != null;
    }

    public ListenerRegistration select(String deviceId, Callback callback, DayOfWeek dayOfWeek) {
        Listeners.clear(deviceRegistration);
        Listeners.clear(schedulerRegistration);

        deviceId = "DRIV:dev:" + deviceId;

        devices = new ArrayList<>();

        devices.add((DeviceModel) CorneaClientFactory.getModelCache().get(deviceId));

        CorneaClientFactory
                .getService(SchedulerService.class)
                .getScheduler(deviceId)
                .onSuccess(schedulerLoadedListener)
                .onFailure(onError);

        return setCallback(callback);
    }

    public ListenerRegistration selectAll(String placeId, List<DeviceModel> devices, Callback callback) {
        Listeners.clear(schedulerRegistration);

        this.devices = new ArrayList<>();

        this.devices.addAll(devices);

        CorneaClientFactory
                .getService(SchedulerService.class)
                .listSchedulers(placeId, false)
                .onSuccess(schedulersLoadedListener)
                .onFailure(onError);

        return setCallback(callback);
    }

    private void onError(Throwable t) {
        Callback cb = getCallback();
        if(cb !=  null) {
            cb.onError(Errors.translate(t));
        }
    }

    @Override
    protected void updateView(Callback callback, ClimateSubsystem subsystem) {
        if(!isLoaded()) {
            logger.debug("skipping update view because the subsystem, device or schedule is not loaded");
            return;
        }
        List<ScheduleStateModel> models = new ArrayList<>();
        for (DeviceModel device : devices) {

            ScheduleStateModel stateModel = new ScheduleStateModel();
            stateModel.setDeviceId(device.getId());
            stateModel.setName(device.getName());

            if (device.getDevtypehint() != null && device.getDevtypehint().equals("NestThermostat")) {
                stateModel.setType(DeviceControlType.NESTTHERMOSTAT);
            } else if(device.getDevtypehint() != null && device.getDevtypehint().equals("TCCThermostat")) {
                stateModel.setType(DeviceControlType.HONEYWELLTCC);
            } else if (device.getCaps().contains(Thermostat.NAMESPACE)) {
                stateModel.setType(DeviceControlType.THERMOSTAT);
            } else if (device.getCaps().contains(Fan.NAMESPACE)) {
                stateModel.setType(DeviceControlType.FAN);
            } else if (device.getCaps().contains(Vent.NAMESPACE)) {
                stateModel.setType(DeviceControlType.VENT);
            } else if (device.getCaps().contains(SpaceHeater.NAMESPACE)) {
                stateModel.setType(DeviceControlType.SPACEHEATER);
            }

            SchedulerModel scheduler = schedulers.get(device.getAddress());
            if (DeviceControlType.HONEYWELLTCC.equals(stateModel.getType())) {
                stateModel.setSchedOn(true); // Allow Changes to these even though they don't have events in our system.
                stateModel.setChecked(isEnabled(subsystem, device.getAddress()));
            } else if (DeviceControlType.NESTTHERMOSTAT.equals(stateModel.getType())) {
                stateModel.setSchedOn(false);
                stateModel.setChecked(true);
            } else if (scheduler == null || scheduler.getCommands() == null || scheduler.getCommands().isEmpty() || stateModel.getType() == null) {
                stateModel.setChecked(false);
                stateModel.setSchedOn(false);
            } else {
                switch (stateModel.getType()) {
                    case FAN: // Fans / Vents Just check the Enabled Flag for CLIMATE
                    case VENT:
                    case SPACEHEATER:
                        Boolean climate = (Boolean) new CapabilityUtils(scheduler).getInstanceValue(CLIMATE, Schedule.ATTR_ENABLED);
                        stateModel.setChecked(Boolean.TRUE.equals(climate));
                        break;
                    default:
                        stateModel.setChecked(isEnabled(subsystem, device.getAddress()));
                        break;
                }
                stateModel.setSchedOn(scheduler.getCommands() != null && !scheduler.getCommands().isEmpty());
            }
            models.add(stateModel);
        }

        Callback cb = getCallback();
        if(cb != null) {
            cb.showScheduleStates(models);
        }
    }

    protected boolean isEnabled(ClimateSubsystem subsystem, String deviceAddress) {
        if (subsystem == null) {
            logger.warn("This shouldn't happen - Subsystem was null.");
            return false;
        }

        Map<String, Map<String, Object>> thermostatSchedules = subsystem.getThermostatSchedules();
        if (thermostatSchedules == null || thermostatSchedules.isEmpty()) {
            logger.warn("This shouldn't happen - Subsystem did not contain ANY schedules");
            return false;
        }

        Map<String, Object> thermostatInQuestion = thermostatSchedules.get(deviceAddress);
        if (thermostatInQuestion != null) {
            ThermostatScheduleStatus status = new ThermostatScheduleStatus(thermostatInQuestion);
            return Boolean.TRUE.equals(status.getEnabled());
        }

        return false;
    }

    public interface Callback {
        void showScheduleStates(List<ScheduleStateModel> models);
        void onError(ErrorModel error);
    }

    public void setScheduleEnabled(final ScheduleStateModel model, final boolean enabled) {
        ClimateSubsystem climateSubsystem = (ClimateSubsystem) getModel();
        if (climateSubsystem == null) {
            return;
        }

        String target = Addresses.toObjectAddress(Device.NAMESPACE, model.getDeviceId());
        switch (model.getType()) {
            case FAN:
            case VENT:
            case SPACEHEATER:
                SchedulerModel schedulerModel = schedulers.get(target);
                if (schedulerModel == null) {
                    return; // We should have a schedule already...
                }
                Map<String, Collection<String>> instances = schedulerModel.getInstances();
                if (instances == null || instances.isEmpty()) {
                    logger.error("Instances were null - was there no schedule created for this?");
                    return;
                }

                if (instances.keySet().contains(CLIMATE)) {
                    setEnabled(schedulerModel, CLIMATE, enabled);
                }
                break;
            case THERMOSTAT:
            case HONEYWELLTCC:
                if (enabled) {
                    climateSubsystem.enableScheduler(target)
                          .onSuccess(onEnableSuccess)
                          .onFailure(onError);
                }
                else {
                    climateSubsystem.disableScheduler(target)
                          .onSuccess(onDisableSuccess)
                          .onFailure(onError);
                }
                break;
            default:
                break;
        }
    }

    private void setEnabled (SchedulerModel scheduler, String instance, boolean isEnabled) {
        new CapabilityUtils(scheduler).setInstance(instance).attriubuteToValue(Schedule.ATTR_ENABLED, isEnabled).andSendChanges();
    }

}
