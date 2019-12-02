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
package arcus.cornea.controller;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.error.ErrorModel;
import arcus.cornea.error.Errors;
import arcus.cornea.utils.Listeners;
import com.iris.client.model.SchedulerModel;
import com.iris.client.service.SchedulerService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RulesScheduleStateController {
    private static final RulesScheduleStateController INSTANCE = new RulesScheduleStateController();
    private Map<String, Map<String, SchedulerModel>> schedulersMap = new ConcurrentHashMap<>();

    public static RulesScheduleStateController instance() {
        return INSTANCE;
    }

    public void fetchAllSchedules(String placeId, Callback callback) {
        if (placeId == null || placeId.length() == 0) {
            callback.onError(Errors.translate(new RuntimeException("Unknown place identifier.")));
            return;
        }

        Map<String, SchedulerModel> initialSchedules = schedulersMap.get(placeId);
        if (initialSchedules != null) {
            callback.showScheduleStates(initialSchedules);
        }

        CorneaClientFactory
                .getService(SchedulerService.class)
                .listSchedulers(placeId, false)
                .onSuccess(Listeners.runOnUiThread(listSchedulersResponse -> {
                    Map<String, SchedulerModel> schedulers = new HashMap<>();

                    for (Map<String,Object> item : listSchedulersResponse.getSchedulers()) {
                        SchedulerModel model = (SchedulerModel) CorneaClientFactory.getModelCache().addOrUpdate(item);
                        schedulers.put(model.getTarget(), model);
                    }

                    schedulersMap.put(placeId, schedulers);
                    callback.showScheduleStates(schedulers);
                }))
                .onFailure(Listeners.runOnUiThread(error -> {
                    callback.onError(Errors.translate(error));
                }));
    }

    public interface Callback {
        void showScheduleStates(Map<String, SchedulerModel> schedulers);
        void onError(ErrorModel error);
    }
}
