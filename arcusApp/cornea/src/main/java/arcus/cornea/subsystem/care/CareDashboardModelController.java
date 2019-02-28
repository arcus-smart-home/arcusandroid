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
package arcus.cornea.subsystem.care;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import arcus.cornea.provider.CareBehaviorsProvider;
import arcus.cornea.subsystem.care.model.ActivityLine;
import arcus.cornea.subsystem.care.model.AlarmState;
import arcus.cornea.subsystem.care.model.CareBehaviorModel;
import arcus.cornea.subsystem.model.CareHistoryModel;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import com.iris.client.IrisClient;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CareDashboardModelController extends BaseCareController<CareDashboardModelController.Callback> implements CareActivityController.HistoryCallback {
    @Override
    public void activityHistoryLoaded(List<CareHistoryModel> entries, String nextToken) {
        final Callback callback = getCallback();
        if (callback == null) {
            return;
        }


        Date timestamp = null;
        if(entries.size() > 0) {
            final CareHistoryModel model = entries.get(0);
            timestamp = new Date(model.getTimestamp());
        }

        final Date finalTimestamp = timestamp;
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callback.updateLastEvent(finalTimestamp);
            }
        });
    }

    public interface Callback {
        void showAlerting(AlarmState alarmState);
        void showSummary(AlarmState alarmState);
        void showLearnMore();
        void updateLastEvent(Date lastEvent);
    }

    private static final Logger logger = LoggerFactory.getLogger(CareDashboardModelController.class);
    private static final int BUCKET_SIZE = 300; // 5 Minutes
    private static final long BUCKET_IN_MILLIS = TimeUnit.SECONDS.toMillis(BUCKET_SIZE);
    private static final long QUERY_HOURS = TimeUnit.HOURS.toMillis(3);
    private static final CareDashboardModelController INSTANCE;
    private ListenerRegistration historyListener;

    AlarmState alarmState;
    static {
        INSTANCE = new CareDashboardModelController(CareSubsystem.NAMESPACE);
        INSTANCE.init();
    }

    public static CareDashboardModelController instance() {
        return INSTANCE;
    }

    private Reference<ClientFuture<CareSubsystem.ListActivityResponse>> activityRequestRef = new WeakReference<>(null);
    private final Set<String> CHANGES_TO_UPDATE_ON = ImmutableSet.of(
          CareSubsystem.ATTR_AVAILABLE,
          CareSubsystem.ATTR_ALARMSTATE,
          CareSubsystem.ATTR_ALARMMODE,
          CareSubsystem.ATTR_ACTIVEBEHAVIORS,
          CareSubsystem.ATTR_LASTALERTTIME,
          CareSubsystem.ATTR_TRIGGEREDDEVICES,
          CareSubsystem.ATTR_BEHAVIORS
    );

    CareDashboardModelController(String namespace) {
        super(namespace);
    }

    CareDashboardModelController(ModelSource<SubsystemModel> subsystem, IrisClient irisClient) {
        super(subsystem);
    }

    @Override protected void onSubsystemLoaded(ModelAddedEvent event) {
        super.onSubsystemLoaded(event);
        doRefreshActivityLine();
    }

    @Override public ListenerRegistration setCallback(Callback callback) {
        doRefreshActivityLine();
        historyListener = CareActivityController.instance().setHistoryCallback(this);
        return super.setCallback(callback);
    }

    protected ClientFuture<CareSubsystem.ListActivityResponse> doRefreshActivityLine() {
        CareSubsystem careSubsystem = (CareSubsystem) getModel();
        if (careSubsystem == null || Boolean.FALSE.equals(careSubsystem.getAvailable())) {
            logger.warn("Ignoring request to get timeline.");
            return null;
        }

        ClientFuture<CareSubsystem.ListActivityResponse> request = activityRequestRef.get();
        if (request != null && !request.isDone()) { // Not Empty, Not Done.
            logger.warn("Using existing request.");
            return request;
        }

        long end = System.currentTimeMillis();
        long start = end - QUERY_HOURS;

        return request;
    }

    @Override protected void onSubsystemChanged(ModelChangedEvent event) {
        super.onSubsystemChanged(event);
        Set<String> currentChanges = event.getChangedAttributes().keySet();
        Set<String> intersection = Sets.intersection(CHANGES_TO_UPDATE_ON, currentChanges);
        if (!intersection.isEmpty()) {
            updateView();
        }
    }

    @Override protected void onSubsystemCleared(ModelDeletedEvent event) {
        super.onSubsystemCleared(event);
        updateView();
    }

    protected void updateView(List<ActivityLine> intervals) {
        Callback callback = getCallback();
        if (callback == null) {
            return;
        }

        updateView(callback, intervals);
    }

    @Override protected void updateView(Callback callback) {
        updateView(callback, Collections.<ActivityLine>emptyList());
    }

    protected void updateView(Callback callback, List<ActivityLine> intervals) {
        CareSubsystem careSubsystem = (CareSubsystem) getModel();
        if (careSubsystem == null || Boolean.FALSE.equals(careSubsystem.getAvailable())) {
            callback.showLearnMore();
            return;
        }

        String alarmAlertReady = String.valueOf(careSubsystem.getAlarmState());
        alarmState = new AlarmState();
        switch(alarmAlertReady) {
            case CareSubsystem.ALARMSTATE_READY:
                boolean alarmIsOn = CareSubsystem.ALARMMODE_ON.equals(careSubsystem.getAlarmMode());
                alarmState.setAlarmMode(alarmIsOn ? "On" : "Off");
                alarmState.setTotalBehaviors(set(careSubsystem.getBehaviors()).size());
                alarmState.setActiveBehaviors(set(careSubsystem.getActiveBehaviors()).size());
                alarmState.setEvents(intervals);
                alarmState.setLastEvent(careSubsystem.getLastAlertTime());

                callback.showSummary(alarmState);
                break;
            case CareSubsystem.ALARMSTATE_ALERT:
                alarmState.setIsAlert(true);

                if (!behaviorsLoaded()) {
                    CareBehaviorsProvider.instance().reload().onSuccess(Listeners.runOnUiThread(new Listener<List<Map<String, Object>>>() {
                        @Override
                        public void onEvent(List<Map<String, Object>> maps) {
                            updateView();
                        }
                    }));
                }

                Map<String, Object> behavior = CareBehaviorsProvider.instance().getById(careSubsystem.getLastAlertCause());
                if (behavior != null) {
                    alarmState.setAlertActor(AlarmState.AlertActor.BEHAVIOR);
                    alarmState.setAlertCause(CareBehaviorModel.fromMap(behavior, "").getName());
                }
                else {
                    alarmState.setAlertActor(AlarmState.AlertActor.PANIC);
                    alarmState.setAlertCause("Panic Rule.");
                }

                callback.showAlerting(alarmState);
                break;
        }
        CareActivityController.instance().loadActivityHistory(1, null, null);
    }

    public void clearHistoryListener() {
        historyListener = Listeners.clear(historyListener);
    }

}
