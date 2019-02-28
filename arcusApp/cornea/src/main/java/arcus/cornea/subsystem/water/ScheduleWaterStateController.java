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

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.error.Errors;
import arcus.cornea.subsystem.ScheduleGenericStateModel;
//import arcus.cornea.subsystem.water.model.ScheduleStateModel;
import arcus.cornea.utils.CapabilityUtils;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.Device;
import com.iris.client.capability.Schedule;
import com.iris.client.capability.Valve;
import com.iris.client.capability.WaterHeater;
import com.iris.client.capability.WaterSoftener;
import com.iris.client.capability.WaterSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.SchedulerModel;
import com.iris.client.service.SchedulerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ScheduleWaterStateController extends BaseWaterController<ScheduleWaterStateController.Callback> {

    private static final Logger logger = LoggerFactory.getLogger(ScheduleWaterStateController.class);

    private static final ScheduleWaterStateController instance;
    public static final String SCHED_ENABLED_WATER = "sched:enabled:WATER";

    static {
        instance = new ScheduleWaterStateController();
        instance.init();
    }

    public static ScheduleWaterStateController instance() {
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
    protected void updateView(Callback callback, WaterSubsystem subsystem) {
        if(!isLoaded()) {
            logger.debug("skipping update view because the subsystem, device or schedule is not loaded");
            return;
        }

        List<ScheduleGenericStateModel> models = new ArrayList<>();

        for (DeviceModel device : devices) {

            ScheduleGenericStateModel stateModel = new ScheduleGenericStateModel();
            stateModel.setDeviceId(device.getId());
            stateModel.setName(device.getName());



            //just use the namespace name
            if (device.getCaps().contains(WaterHeater.NAMESPACE)) {
                stateModel.setType(WaterHeater.NAMESPACE);
            } else if (device.getCaps().contains(Valve.NAMESPACE)) {
                stateModel.setType(Valve.NAMESPACE);
            } else if (device.getCaps().contains(WaterSoftener.NAMESPACE)) {
                stateModel.setType(WaterSoftener.NAMESPACE);
            } else {
                // unknown control type, skip this device for now...
            }

            SchedulerModel scheduler = schedulers.get(device.getAddress());
            if (scheduler == null || scheduler.getCommands() == null)  {

                stateModel.setChecked(false);
                stateModel.setSchedOn(false);

            }

            //bugfix #121 from Fabric
            //deMorgan's (scheduler != null && scheduler.getCommands() != null)
            else
            {
                Object object = scheduler.get(SCHED_ENABLED_WATER);
                if (object != null && object instanceof  Boolean){
                    stateModel.setChecked(Boolean.TRUE.equals(object));
                } else {
                    stateModel.setChecked(false);
                }

                if (scheduler.getCommands().size() != 0){
                    stateModel.setSchedOn(true);
                } else {
                    stateModel.setSchedOn(false);
                    stateModel.setChecked(false);
                }

            }

            models.add(stateModel);
        }

        Callback cb = getCallback();
        if(cb != null) {
            cb.showScheduleStates(models);
        }
    }

    public interface Callback {
        void showScheduleStates(List<ScheduleGenericStateModel> models);
        void onError(ErrorModel error);
    }



    public void setScheduleEnabled(final ScheduleGenericStateModel model, final boolean enabled) {
        final String deviceId = "DRIV:" + Device.NAMESPACE + ":" + model.getDeviceId();
        CorneaClientFactory.getService(SchedulerService.class).getScheduler(deviceId).onSuccess(new Listener<SchedulerService.GetSchedulerResponse>() {
            @Override
            public void onEvent(SchedulerService.GetSchedulerResponse getSchedulerResponse) {
                SchedulerModel scheduler = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(getSchedulerResponse.getScheduler());
                Set<String> keys = scheduler.getInstances().keySet();
                for (String key : keys) {
                    setEnabled(scheduler, key, enabled);
                }
            }
        });
    }

    private void setEnabled (SchedulerModel scheduler, String instance, boolean isEnabled) {
        new CapabilityUtils(scheduler).setInstance(instance).attriubuteToValue(Schedule.ATTR_ENABLED, isEnabled).andSendChanges();

    }

}
