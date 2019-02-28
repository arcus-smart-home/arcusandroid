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
package arcus.cornea;

import arcus.cornea.error.ErrorModel;
import arcus.cornea.error.Errors;
import arcus.cornea.subsystem.climate.BaseClimateController;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.ClimateSubsystem;
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

public class RulesScheduleStateController extends BaseClimateController<RulesScheduleStateController.Callback> {

    private static final Logger logger = LoggerFactory.getLogger(RulesScheduleStateController.class);

    private static final RulesScheduleStateController instance;

    static {
        instance = new RulesScheduleStateController();
        instance.init();
    }

    public static RulesScheduleStateController instance() {
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

    private List<DeviceModel> mDevices;
    private Map<String, SchedulerModel> schedulers; // deviceId, scheduler
    private ListenerRegistration deviceRegistration = Listeners.empty();
    private ListenerRegistration schedulerRegistration = Listeners.empty();

    @Override
    protected boolean isLoaded() {
        return super.isLoaded() && schedulers != null;
    }



    public ListenerRegistration fetchAllSchedules(String placeId, Callback callback) {
        Listeners.clear(schedulerRegistration);

        this.mDevices = new ArrayList<>();

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
    protected void updateView(Callback callback) {
        if(!isLoaded()) {
            logger.debug("skipping update view because the subsystem, device or schedule is not loaded");
            return;
        }

        Callback cb = getCallback();
        if(cb != null) {
            cb.showScheduleStates(schedulers);
        }
    }

    public interface Callback {
        void showScheduleStates(Map<String, SchedulerModel> schedulers);
        void onError(ErrorModel error);
    }

}
