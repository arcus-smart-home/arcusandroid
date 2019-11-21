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
package arcus.cornea.subsystem.safety;

import androidx.annotation.Nullable;

import arcus.cornea.dto.HistoryLogEntries;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.utils.GlobalValues;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.SafetySubsystem;
import com.iris.client.capability.Subsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;

// todo:  this is just a stub until the per subsystem history log is available on the platform
public class SafetyHistoryController {

    public interface Callback {
        void onShowSafetyHistory(HistoryLogEntries entries);
    }

    private static final SafetyHistoryController instance =
            new SafetyHistoryController(SubsystemController.instance().getSubsystemModel(SafetySubsystem.NAMESPACE));

    public static SafetyHistoryController instance() {
        return instance;
    }

    private static final Logger logger = LoggerFactory.getLogger(SafetyHistoryController.class);

    private WeakReference<Callback> callback = new WeakReference<Callback>(null);

    private ModelSource<SubsystemModel> subsystem;

    private Listener<Throwable> onRequestError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onRequestError(throwable);
        }
    });
    private Listener<Subsystem.ListHistoryEntriesResponse> historyLoadedListener = Listeners.runOnUiThread(new Listener<Subsystem.ListHistoryEntriesResponse>() {
        @Override
        public void onEvent(Subsystem.ListHistoryEntriesResponse listHistoryEntriesResponse) {
            onHistoryLoaded(new HistoryLogEntries(listHistoryEntriesResponse));
        }
    });

    protected SafetyHistoryController(ModelSource<SubsystemModel> subsystem) {
        this.subsystem = subsystem;
    }

    public ListenerRegistration setCallback(Callback callback) {
        if(this.callback.get() == null) {
            logger.warn("Replacing existing callback");
        }
        this.callback = new WeakReference<>(callback);
        loadHistory(GlobalValues.ALARM_ACTIVITY_PAGING_SIZE, null);
        return Listeners.wrap(this.callback);
    }

    public void fetchNextSet(String token) {
        loadHistory(GlobalValues.ALARM_ACTIVITY_PAGING_SIZE, token);
    }

    protected SafetySubsystem get() {
        subsystem.load();
        return (SafetySubsystem) subsystem.get();
    }

    private void loadHistory(@Nullable Integer limit, @Nullable String token) {
        SafetySubsystem safety = get();
        if (safety == null) {
            return; // TODO: Error;
        }

        if (limit == null || limit < 1) {
            limit = 20;
        }

        safety
                .listHistoryEntries(limit, token, true)
                .onFailure(onRequestError)
                .onSuccess(historyLoadedListener);
    }

    protected void onHistoryLoaded(HistoryLogEntries entries) {
        Callback callback = this.callback.get();
        if (callback != null) {
            callback.onShowSafetyHistory(entries);
        }
    }

    protected void onRequestError(Throwable cause) {
        // TODO show the user something?
        logger.warn("Unable to complete request", cause);
    }
}
