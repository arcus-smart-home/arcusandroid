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

import androidx.annotation.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.care.model.ActivityLine;
import arcus.cornea.subsystem.care.util.ActivityIntervalProcessor;
import arcus.cornea.subsystem.model.CareHistoryModel;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import com.iris.client.bean.HistoryLog;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class CareActivityController extends BaseSubsystemController<CareActivityController.Callback> {
    public interface Callback {
        void onError(Throwable cause);
        void activitiesLoaded(List<ActivityLine> activityLines);
    }

    public interface HistoryCallback {
        void activityHistoryLoaded(List<CareHistoryModel> entries, String nextToken);
    }

    private static final Logger logger = LoggerFactory.getLogger(CareActivityController.class);
    private static final int BUCKET_SIZE = 300;
    private static final int DEFAULT_QUERY_LIMIT_SIZE = 15;
    private static final long BUCKET_IN_MILLIS = TimeUnit.SECONDS.toMillis(BUCKET_SIZE);
    private static final CareActivityController INSTANCE;
    static {
        INSTANCE = new CareActivityController(
              SubsystemController.instance().getSubsystemModel(CareSubsystem.NAMESPACE)
        );
        INSTANCE.init();
    }

    public static CareActivityController instance() {
        return INSTANCE;
    }



    private Reference<ClientFuture<CareSubsystem.ListActivityResponse>> activityRequestRef = new WeakReference<>(null);
    private Reference<HistoryCallback> historyCallbackRef = new WeakReference<>(null);

    private final Set<String> CHANGES_TO_UPDATE_ON = ImmutableSet.of(
          CareSubsystem.ATTR_CARECAPABLEDEVICES
    );

    private final Listener<CareSubsystem.ListActivityResponse> withoutFillInListener =
          new Listener<CareSubsystem.ListActivityResponse>() {
              @Override public void onEvent(CareSubsystem.ListActivityResponse response) {
                  parseActivityIntervalAndActivitiesNoFillIn(response);
              }
          };
    private final Listener<CareSubsystem.ListActivityResponse> withFillInListener =
          new Listener<CareSubsystem.ListActivityResponse>() {
              @Override public void onEvent(CareSubsystem.ListActivityResponse response) {
                  parseActivityIntervalAndActivitiesWithFillIn(response);
              }
          };
    private final Listener<CareSubsystem.ListDetailedActivityResponse> onDetailLoaded =
          new Listener<CareSubsystem.ListDetailedActivityResponse>() {
              @Override public void onEvent(CareSubsystem.ListDetailedActivityResponse response) {
                  parseDetailedActivity(response);
              }
          };
    private final Listener<Throwable> errorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable cause) {
            Callback callback = getCallback();
            if (callback != null) {
                callback.onError(cause);
            }
        }
    });

    protected CareActivityController(String namespace) {
        super(namespace);
    }

    protected CareActivityController(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
    }

    public long getBaselineTimeFrom(long timestamp) {
        Calendar instance = Calendar.getInstance();
        instance.setTimeInMillis(timestamp);
        instance.set(Calendar.HOUR_OF_DAY, 0);
        instance.set(Calendar.MINUTE, 0);
        instance.set(Calendar.SECOND, 0);
        instance.set(Calendar.MILLISECOND, 0);

        return instance.getTimeInMillis();
    }

    public boolean isToday(long time) {
        int today = Calendar.getInstance().get(Calendar.DAY_OF_YEAR);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);

        int selected = calendar.get(Calendar.DAY_OF_YEAR);

        return today == selected;
    }

    public List<String> getFilterableDevices() {
        CareSubsystem careSubsystem = (CareSubsystem) getModel();
        if (careSubsystem == null || Boolean.FALSE.equals(careSubsystem.getAvailable())) {
            return Collections.emptyList();
        }

        Set<String> availableDevices = careSubsystem.getCareCapableDevices();
        if (availableDevices == null || availableDevices.isEmpty()) {
            return Collections.emptyList();
        }

        return Lists.newArrayList(availableDevices);
    }

    public List<String> getSelectedCareDevices() {
        CareSubsystem careSubsystem = (CareSubsystem) getModel();
        if (careSubsystem == null || Boolean.FALSE.equals(careSubsystem.getAvailable())) {
            return Collections.emptyList();
        }

        Set<String> careDevices = careSubsystem.getCareDevices();
        if (careDevices == null || careDevices.isEmpty()) {
            return Collections.emptyList();
        }

        return Lists.newArrayList(careDevices);
    }

    public ListenerRegistration setHistoryCallback(HistoryCallback callback) {
        historyCallbackRef = new WeakReference<>(callback);

        return Listeners.wrap(historyCallbackRef);
    }

    public void loadActivityHistory(@Nullable Integer limit, @Nullable String token, @Nullable List<String> filteredTo) {
        CareSubsystem careSubsystem = (CareSubsystem) getModel();
        if (careSubsystem == null || Boolean.FALSE.equals(careSubsystem.getAvailable())) {
            logger.error("Cannot load History for care - Subsystem is not available.");
            return;
        }

        Set<String> onlyWithDevices = filteredTo != null ? Sets.newHashSet(filteredTo) : null;
        if (limit == null) {
            limit = DEFAULT_QUERY_LIMIT_SIZE;
        }
        careSubsystem
              .listDetailedActivity(limit, token, onlyWithDevices)
              .onFailure(errorListener)
              .onSuccess(onDetailLoaded);
    }

    // Default behavior of the dashboard is to now only draw a line at the start and end of an event.
    // If we are in Full Screen Mode and pick a contact sensor, we can draw a full line....
    public void loadActivitiesDuring(long theDayTimeStamp, @Nullable List<String> filterBy, boolean saveFilter) {
        loadActivitiesDuring(theDayTimeStamp, filterBy, saveFilter, false);
    }

    public void loadActivitiesDuring(long theDayTimeStamp, @Nullable List<String> filterBy, boolean saveFilter, boolean withFillIn) {

        if (!CorneaClientFactory.isConnected()) {
            logger.error("Client disconnected; activity will relaunch application. Standby...");
            return;
        }

        CareSubsystem careSubsystem = (CareSubsystem) getModel();
        if (careSubsystem == null || Boolean.FALSE.equals(careSubsystem.getAvailable())) {
            logger.error("Cannot load History for care - Subsystem is not available.");
            return;
        }

        ClientFuture<CareSubsystem.ListActivityResponse> request = activityRequestRef.get();
        if (request != null && !request.isDone()) { // Not Empty, Not Done.
            logger.warn("Using existing request. Did not process request for additional data.");
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(theDayTimeStamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long start = calendar.getTimeInMillis() - TimeUnit.MINUTES.toMillis(10); // Previous Day - 10 minutes until midnight
        long end = calendar.getTimeInMillis() + TimeUnit.DAYS.toMillis(1) - 1; // 11:59:59:59 of Current day in query


        Set<String> filterToThese = null;
        if (filterBy != null) {
            filterToThese = Sets.newHashSet(filterBy);
            if (saveFilter) {
                saveParticipatingCareDevices(filterToThese);
            }
        }

        ClientFuture<CareSubsystem.ListActivityResponse> response = careSubsystem.listActivity(new Date(start), new Date(end), BUCKET_SIZE, filterToThese);
        activityRequestRef = new WeakReference<>(response);

        response
              .onFailure(errorListener)
              .onSuccess(withFillIn ? withFillInListener : withoutFillInListener);
    }

    @Override protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> currentChanges = event.getChangedAttributes().keySet();
        Set<String> intersection = Sets.intersection(CHANGES_TO_UPDATE_ON, currentChanges);
        if (!intersection.isEmpty()) {
            updateView();
        }
    }

    @Override protected void onSubsystemCleared(ModelDeletedEvent event) {
        super.onSubsystemCleared(event);
        clearView();
    }

    protected void saveParticipatingCareDevices(Set<String> careDevices) {
        CareSubsystem careSubsystem = (CareSubsystem) getModel();
        if (careSubsystem == null) {
            logger.error("Cannot update care participating devices. Subsystem was null");
            return;
        }

        careSubsystem.setCareDevices(careDevices);
        ((SubsystemModel) careSubsystem).commit();
    }

    protected void clearView() {
        Callback callback = getCallback();
        if (callback == null) {
            return;
        }

        callback.activitiesLoaded(Collections.<ActivityLine>emptyList());
    }

    protected void updateView(List<ActivityLine> linesToDraw) {
        Callback callback = getCallback();
        if (callback == null) {
            return;
        }

        callback.activitiesLoaded(linesToDraw);
    }

    protected void parseDetailedActivity(CareSubsystem.ListDetailedActivityResponse response) {
        final HistoryCallback callback = historyCallbackRef.get();
        if (callback == null) {
            return;
        }

        DateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        DateFormat shortDateFormat = new SimpleDateFormat("ccc MMM d", Locale.getDefault());

        final String nextToken = response.getNextToken();
        final List<CareHistoryModel> historyModels = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        for (Map<String, Object> item : response.getResults()) {
            HistoryLog entry = new HistoryLog(item);
            CareHistoryModel model = new CareHistoryModel();
            calendar.setTime(entry.getTimestamp());

            model.setDate(dateFormat.format(entry.getTimestamp()));
            model.setShortDate(shortDateFormat.format(entry.getTimestamp()));
            model.setTimestamp(entry.getTimestamp().getTime());
            model.setIsHeaderRow(false);
            model.setTitle(entry.getSubjectName());
            model.setSubTitle(entry.getLongMessage());
            model.setAddress(entry.getSubjectAddress());
            model.setCalendarDayOfYear(calendar.get(Calendar.DAY_OF_YEAR));

            historyModels.add(model);
        }

        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override public void run() {
                callback.activityHistoryLoaded(historyModels, nextToken);
            }
        });
    }

    protected void parseActivityIntervalAndActivitiesNoFillIn(CareSubsystem.ListActivityResponse response) {
        parseActivityIntervalResponse(response, false);
    }

    protected void parseActivityIntervalAndActivitiesWithFillIn(CareSubsystem.ListActivityResponse response) {
        parseActivityIntervalResponse(response, true);
    }

    protected void parseActivityIntervalResponse(CareSubsystem.ListActivityResponse response, boolean withFillIn) {
        final Callback callback = getCallback();
        if (callback == null) {
            return;
        }

        final List<ActivityLine> linesToDraw = getActivityArrayFor(response, withFillIn);
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callback.activitiesLoaded(linesToDraw);
            }
        });
    }

    protected List<ActivityLine> getActivityArrayFor(CareSubsystem.ListActivityResponse response, boolean withFillIn) {
        return ActivityIntervalProcessor.instance().parseActivityIntervalResponse(response, BUCKET_IN_MILLIS, withFillIn);
    }
}
