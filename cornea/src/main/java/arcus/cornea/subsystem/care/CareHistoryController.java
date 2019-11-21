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

import arcus.cornea.dto.HistoryLogEntries;
import arcus.cornea.subsystem.model.CareHistoryModel;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import com.iris.client.bean.HistoryLog;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.capability.Subsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CareHistoryController extends BaseCareController<CareHistoryController.Callback> {
    public interface Callback {
        void onError(Throwable error);
        void historyLoaded(List<CareHistoryModel> entries, @Nullable String nextToken);
    }

    private static final int DEFAULT_QUERY_LIMIT_SIZE = 25;
    private static final int VISIBLE_DAYS_OF_HISTORY = 14;

    private static final Logger logger = LoggerFactory.getLogger(CareHistoryController.class);
    private static final CareHistoryController INSTANCE;
    private final Listener<Throwable> errorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onError(throwable);
        }
    });
    private final Listener<Subsystem.ListHistoryEntriesResponse> historyResponseListener =
          new Listener<Subsystem.ListHistoryEntriesResponse>() {
              @Override public void onEvent(Subsystem.ListHistoryEntriesResponse listHistoryEntriesResponse) {
                  parseHistoryResponse(listHistoryEntriesResponse);
              }
          };

    static {
        INSTANCE = new CareHistoryController(CareSubsystem.NAMESPACE);
        INSTANCE.init();
    }

    public static CareHistoryController instance() {
        return INSTANCE;
    }

    protected CareHistoryController(String namespace) {
        super(namespace);
    }

    protected CareHistoryController(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
    }

    public void loadHistory(@Nullable Integer limit, @Nullable String token) {
        CareSubsystem careSubsystem = getCareSubsystemModel();
        if (careSubsystem == null) {
            logger.error("Cannot load history subsystem not loaded.");
            return;
        }

        if (limit == null) {
            limit = DEFAULT_QUERY_LIMIT_SIZE;
        }

        careSubsystem.listHistoryEntries(limit, token, true)
              .onFailure(errorListener)
              .onSuccess(historyResponseListener);
    }

    protected void onError(Throwable throwable) {
        Callback callback = getCallback();
        if (callback == null) {
            return;
        }

        callback.onError(throwable);
    }

    protected void parseHistoryResponse(Subsystem.ListHistoryEntriesResponse response) {
        final Callback callback = getCallback();
        if (callback == null) {
            return;
        }

        Calendar visibleWindow = Calendar.getInstance();
        visibleWindow.add(Calendar.DATE, VISIBLE_DAYS_OF_HISTORY * -1);

        DateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());
        DateFormat shortDateFormat = new SimpleDateFormat("ccc MMM d", Locale.getDefault());
        HistoryLogEntries entries = new HistoryLogEntries(response);
        final String nextToken = entries.getNextToken();
        final List<CareHistoryModel> historyModels = new ArrayList<>();

        Calendar calendar = Calendar.getInstance();
        for (HistoryLog entry : entries.getEntries()) {

            // Ignore entries older than the allowable window
            if (entry.getTimestamp().before(visibleWindow.getTime())) {
                continue;
            }

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
                callback.historyLoaded(historyModels, nextToken);
            }
        });
    }
}
