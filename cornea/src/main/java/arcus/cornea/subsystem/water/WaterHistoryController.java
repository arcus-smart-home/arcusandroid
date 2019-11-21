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

import androidx.annotation.Nullable;

import arcus.cornea.dto.HistoryLogEntries;
import arcus.cornea.utils.GlobalValues;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;



public class WaterHistoryController extends BaseWaterController<WaterHistoryController.Callback> {

    public interface Callback {
        void onShowWaterHistory(HistoryLogEntries historyLogEntries);
    }

    private final static WaterHistoryController instance = new WaterHistoryController();

    private WaterHistoryController() {}

    public static WaterHistoryController getInstance() {
        return instance;
    }

    @Override
    public ListenerRegistration setCallback(WaterHistoryController.Callback callback) {
        ListenerRegistration registration = super.setCallback(callback);
        requestHistory(GlobalValues.ALARM_ACTIVITY_PAGING_SIZE, null);
        return registration;
    }

    public void fetchNextSet(String token) {
        requestHistory(GlobalValues.ALARM_ACTIVITY_PAGING_SIZE, token);
    }

    public void requestHistory(@Nullable Integer limit, @Nullable String token) {
        HistoryLogEntries.forSubsytemModel(getModel(), limit, token).onSuccess(new Listener<HistoryLogEntries>() {
            @Override
            public void onEvent(HistoryLogEntries historyLogEntries) {
                getCallback().onShowWaterHistory(historyLogEntries);
            }
        });
    }

}
