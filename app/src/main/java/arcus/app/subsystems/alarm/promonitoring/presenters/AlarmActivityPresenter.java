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
package arcus.app.subsystems.alarm.promonitoring.presenters;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import com.google.common.collect.Sets;
import arcus.cornea.common.BasePresenter;
import arcus.cornea.dto.HistoryLogEntries;
import arcus.cornea.subsystem.safety.SafetyHistoryController;
import arcus.cornea.subsystem.security.SecurityHistoryController;
import arcus.cornea.subsystem.water.WaterHistoryController;
import com.iris.client.bean.HistoryLog;
import com.iris.client.capability.AlarmIncident;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.utils.Day;
import arcus.app.common.utils.StringUtils;
import arcus.app.subsystems.alarm.promonitoring.models.HistoryListItemModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;



public class AlarmActivityPresenter extends BasePresenter<AlarmActivityContract.AlarmActivityView> implements
        AlarmActivityContract.AlarmActivityPresenter,
        SafetyHistoryController.Callback,
        SecurityHistoryController.Callback,
        WaterHistoryController.Callback
{

    private HistoryLogEntries securityHistory;
    private HistoryLogEntries safetyHistory;
    private HistoryLogEntries waterHistory;

    private Set<AlarmActivityContract.AlarmActivityFilter> filterSpec;
    private String nextToken = "";

    private final String SmokeString = ArcusApplication.getContext().getString(R.string.alarm_type_smoke).toUpperCase();
    private final String CoString = ArcusApplication.getContext().getString(R.string.alarm_type_co).toUpperCase();
    private final String SecurityString = ArcusApplication.getContext().getString(R.string.alarm_type_security).toUpperCase();
    private final String PanicString = ArcusApplication.getContext().getString(R.string.alarm_type_panic).toUpperCase();
    private final String WaterString = ArcusApplication.getContext().getString(R.string.alarm_type_water).toUpperCase();

    ArrayList<HistoryListItemModel> cachedHistoryItems = new ArrayList<>();

    @Override
    public void requestUpdate() {
        requestUpdate(Sets.immutableEnumSet(
                AlarmActivityContract.AlarmActivityFilter.SECURITY,
                AlarmActivityContract.AlarmActivityFilter.SAFETY,
                AlarmActivityContract.AlarmActivityFilter.WATER)
        );
    }

    @Override
    public void requestUpdate(Set<AlarmActivityContract.AlarmActivityFilter> filterSpec) {
        this.filterSpec = filterSpec;

        String securityListenerId = SecurityHistoryController.class.getCanonicalName();
        String safetyListenerId = SafetyHistoryController.class.getCanonicalName();
        String waterListenerId = WaterHistoryController.class.getCanonicalName();

        nextToken = "";
        cachedHistoryItems.clear();

        if (filterSpec.contains(AlarmActivityContract.AlarmActivityFilter.SECURITY)) {
            addListener(securityListenerId, SecurityHistoryController.getInstance().setCallback(this));
        }

        if (filterSpec.contains(AlarmActivityContract.AlarmActivityFilter.SAFETY)) {
            addListener(safetyListenerId, SafetyHistoryController.instance().setCallback(this));
        }

        if (filterSpec.contains(AlarmActivityContract.AlarmActivityFilter.WATER)) {
            addListener(waterListenerId, WaterHistoryController.getInstance().setCallback(this));
        }
    }

    public void fetchNextSet(Set<AlarmActivityContract.AlarmActivityFilter> filterSpec) {
        this.filterSpec = filterSpec;

        if(nextToken == null) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    if(isPresenting()) {
                        getPresentedView().updateView(insertDateHeaders(cachedHistoryItems));
                    }
                }
            });
            return;
        }

        if (filterSpec.contains(AlarmActivityContract.AlarmActivityFilter.SECURITY)) {
            SecurityHistoryController.getInstance().fetchNextSet(nextToken);
        }

        if (filterSpec.contains(AlarmActivityContract.AlarmActivityFilter.SAFETY)) {
            SafetyHistoryController.instance().fetchNextSet(nextToken);
        }

        if (filterSpec.contains(AlarmActivityContract.AlarmActivityFilter.WATER)) {
            WaterHistoryController.getInstance().fetchNextSet(nextToken);
        }
    }

    private List<HistoryListItemModel> buildHistoryModels() {
        List<HistoryListItemModel> items = new ArrayList<>();

        for (HistoryLog thisEntry : getVisibleHistoryLogEntries()) {

            // Build history item with icon and chevron
            if (isIncident(thisEntry)) {
                items.add(HistoryListItemModel.Builder
                        .historyIncidentItem()
                        .withTitle(thisEntry.getSubjectName().toUpperCase())
                        .withSubtitle(thisEntry.getLongMessage())
                        .withAbstractIcon(getIncidentIcon(thisEntry))
                        .withTimestamp(thisEntry.getTimestamp())
                        .withIncidentAddress(thisEntry.getSubjectAddress())
                        .build());
            }

            // Build "simple" history log item with title and subtext
            else {
                items.add(HistoryListItemModel.Builder
                        .historyLogItem()
                        .withTitle(thisEntry.getSubjectName().toUpperCase())
                        .withSubtitle(thisEntry.getLongMessage())
                        .withTimestamp(thisEntry.getTimestamp())
                        .build());
            }
        }

        cachedHistoryItems.addAll(items);
        return insertDateHeaders(cachedHistoryItems);
    }

    @NonNull private Map<Day,List<HistoryListItemModel>> groupByDate(@NonNull List<HistoryListItemModel> items) {
        Map<Day,List<HistoryListItemModel>> map = new HashMap<>();

        for (HistoryListItemModel thisItem : items) {

            Day thisItemDay = Day.fromDate(thisItem.getTimestamp());
            List<HistoryListItemModel> itemsOnDate = map.get(thisItemDay);

            if (itemsOnDate == null) {
                itemsOnDate = new ArrayList<>();
            }

            itemsOnDate.add(thisItem);
            map.put(thisItemDay, itemsOnDate);
        }

        return map;
    }

    private List<HistoryListItemModel> insertDateHeaders(@NonNull List<HistoryListItemModel> items) {

        List<HistoryListItemModel> datedItems = new ArrayList<>();

        Map<Day,List<HistoryListItemModel>> grouped = groupByDate(items);
        Day thisDay = Day.fromDate(new Date());
        Day earliestDay = grouped.keySet().isEmpty() ? Day.add(-1, new Date()) : Collections.min(grouped.keySet());

        do {
            // Add a header for this date
            datedItems.add(HistoryListItemModel.Builder
                            .sectionHeader()
                            .withTitle(StringUtils.getDatestampString(thisDay))
                            .build());

            List<HistoryListItemModel> itemsOnDate = grouped.get(thisDay);

            // If this date has no items, add a "No activity..." row
            if (itemsOnDate == null || itemsOnDate.size() == 0) {
                datedItems.add(HistoryListItemModel.Builder.noActivity());
            }

            // Otherwise, add the history items from this date in reverse chronological order
            else {
                Collections.sort(itemsOnDate, Collections.reverseOrder());
                datedItems.addAll(itemsOnDate);
            }

            // Step back one day and start over!
            thisDay = Day.add(-1, thisDay);

        } while (earliestDay.before(thisDay) || earliestDay.equals(thisDay));

        return datedItems;
    }

    private boolean isIncident(@NonNull HistoryLog logEntry) {
        // TODO: Not sure if this is correct...
        return logEntry.getSubjectAddress().contains(AlarmIncident.NAMESPACE);
    }

    private Integer getIncidentIcon(@NonNull HistoryLog logEntry) {

        // TODO: Not sure if this technique for determining icon is generally correct
        String subject = logEntry.getSubjectName().toUpperCase();

        if (subject.contains(SmokeString)) {
            return R.drawable.promon_smoke_white;
        } else if (subject.contains(CoString)) {
            return R.drawable.promon_co_white;
        } else if (subject.contains(PanicString) || subject.contains(SecurityString)) {
            return R.drawable.promon_security_white;
        } else if (subject.contains(WaterString)) {
            return R.drawable.promon_leak_white;
        }

        throw new IllegalArgumentException("Bug! No icon for incident: " + subject);
    }

    private List<HistoryLog> getVisibleHistoryLogEntries() {
        List<HistoryLog> visibleLogs = new ArrayList<>();

        if (securityHistory != null && this.filterSpec.contains(AlarmActivityContract.AlarmActivityFilter.SECURITY)) {
            visibleLogs.addAll(securityHistory.getEntries());
        }

        if (safetyHistory != null && this.filterSpec.contains(AlarmActivityContract.AlarmActivityFilter.SAFETY)) {
            visibleLogs.addAll(safetyHistory.getEntries());
        }

        if (waterHistory != null && this.filterSpec.contains(AlarmActivityContract.AlarmActivityFilter.WATER)) {
            visibleLogs.addAll(waterHistory.getEntries());
        }

        return visibleLogs;
    }

    @Override
    public void onShowSecurityHistory(HistoryLogEntries historyLogEntries) {
        this.securityHistory = historyLogEntries;
        nextToken = historyLogEntries.getNextToken();

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(isPresenting()) {
                    getPresentedView().updateView(buildHistoryModels());
                }
            }
        });
    }

    @Override
    public void onShowSafetyHistory(HistoryLogEntries historyLogEntries) {
        this.safetyHistory = historyLogEntries;
        nextToken = historyLogEntries.getNextToken();

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(isPresenting()) {
                    getPresentedView().updateView(buildHistoryModels());
                }
            }
        });
    }

    @Override
    public void onShowWaterHistory(HistoryLogEntries historyLogEntries) {
        this.waterHistory = historyLogEntries;
        nextToken = historyLogEntries.getNextToken();

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if(isPresenting()) {
                    getPresentedView().updateView(buildHistoryModels());
                }
            }
        });
    }
}
