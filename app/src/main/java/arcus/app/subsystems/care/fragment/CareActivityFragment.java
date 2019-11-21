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
package arcus.app.subsystems.care.fragment;

import android.content.pm.ActivityInfo;
import androidx.annotation.Nullable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import arcus.cornea.subsystem.care.CareActivityController;
import arcus.cornea.subsystem.care.model.ActivityLine;
import arcus.cornea.subsystem.model.CareHistoryModel;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.activities.FullscreenFragmentActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.DayPickerPopup;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.ActivityEventView;
import arcus.app.subsystems.care.adapter.CareHistoryListAdapter;
import arcus.app.subsystems.care.view.CareMultiModelPopup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class CareActivityFragment
      extends BaseFragment
      implements View.OnClickListener,
      CareActivityController.Callback,
      CareActivityController.HistoryCallback
{
    private static final int DAYS_CARE_CAN_GO_BACK = 14;
    private final SimpleDateFormat sdf = new SimpleDateFormat("ccc MMM d", Locale.getDefault());
    protected View careFilterContainer;
    protected TextView careSelectedDayTV;
    protected TextView filterDetailText;
    protected View careZoomIV;
    protected ActivityEventView activityEventView;
    protected ListView historyView;
    protected CareHistoryListAdapter historyListAdapter;
    private ListenerRegistration listener;
    private ListenerRegistration historyListener;
    private int viewingDay = -1;
    protected List<String> filteredToDevices = new ArrayList<>();
    protected List<String> availableDeviceList = Collections.emptyList();
    protected long viewingTimelineTime = System.currentTimeMillis();
    private AtomicReference<String> nextToken = new AtomicReference<>(null);
    private AtomicBoolean loadingMore = new AtomicBoolean(false);
    public static final int VISIBLE_THRESHOLD = 8;

    private DayPickerPopup.Callback dppCallback = new DayPickerPopup.Callback() {
        @Override public void selected(long time) {
            moveTimelineTo(time);
        }
    };

    private final CareMultiModelPopup.Callback selectedDevicesCallback = new CareMultiModelPopup.Callback() {
        @Override
        public void itemSelectedAddress(Set<String> selected) {
            filteredToDevices = Lists.newArrayList(selected);
            enableButtonsAndShowProgress(false, true);
            CareActivityController.instance().loadActivitiesDuring(viewingTimelineTime, filteredToDevices, true);
            CareActivityController.instance().loadActivityHistory(null, startNewDay(), filteredToDevices);
        }
    };


    public static CareActivityFragment newInstance() {
        CareActivityFragment fragment = new CareActivityFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }

    @Nullable @Override public View onCreateView(
          LayoutInflater inflater,
          @Nullable ViewGroup container,
          @Nullable Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view == null) {
            return null;
        }

        filterDetailText = (TextView) view.findViewById(R.id.care_activity_filter_details);

        careFilterContainer = view.findViewById(R.id.care_filter_container);
        if (careFilterContainer != null) {
            careFilterContainer.setOnClickListener(this);
        }

        careSelectedDayTV = (TextView) view.findViewById(R.id.care_activity_current_day);
        if (careSelectedDayTV != null) {
            careSelectedDayTV.setOnClickListener(this);
        }

        careZoomIV = view.findViewById(R.id.care_activity_zoom);
        if (careZoomIV != null) {
            careZoomIV.setOnClickListener(this);
        }

        activityEventView = (ActivityEventView) view.findViewById(R.id.care_half_activity_graph);
        historyView = (ListView) view.findViewById(R.id.care_activity_history);
        return view;
    }

    @Override public void onResume() {
        super.onResume();
        CharSequence contents = careSelectedDayTV.getText();
        if (!TextUtils.isEmpty(contents)) {
            careSelectedDayTV.setText(contents.toString().toUpperCase());
        }

        filteredToDevices = CareActivityController.instance().getSelectedCareDevices();
        listener = CareActivityController.instance().setCallback(this);
        historyListener = CareActivityController.instance().setHistoryCallback(this);

        enableButtonsAndShowProgress(false, false);
        viewingTimelineTime = CareActivityController.instance().getBaselineTimeFrom(viewingTimelineTime);
        CareActivityController.instance().loadActivitiesDuring(viewingTimelineTime, filteredToDevices, false);
        CareActivityController.instance().loadActivityHistory(null, startNewDay(), filteredToDevices);
    }

    @Override public void onPause() {
        super.onPause();
        enableButtonsAndShowProgress(true, false);
        listener = Listeners.clear(listener);
        historyListener = Listeners.clear(historyListener);
    }

    protected void moveTimelineTo(long time) {
        if (activityEventView == null) {
            return;
        }

        activityEventView.setEndTime(getSelectedTimeText(time));
        viewingTimelineTime = CareActivityController.instance().getBaselineTimeFrom(time);
        enableButtonsAndShowProgress(false, true);
        CareActivityController.instance().loadActivitiesDuring(time, filteredToDevices, true);
        CareActivityController.instance().loadActivityHistory(null, startNewDay(), filteredToDevices);
    }

    protected long getSelectedTimeText(long time) {
        long setToTime = System.currentTimeMillis();
        if (StringUtils.isDateToday(new Date(time))) {
            setSelectedDayTVText(getString(R.string.today).toUpperCase());
        }
        else if (StringUtils.isDateYesterday(new Date(time))) {
            setToTime = time - TimeUnit.HOURS.toMillis(11);
            setSelectedDayTVText(getString(R.string.yesterday).toUpperCase());
        }
        else {
            setToTime = time - TimeUnit.HOURS.toMillis(11);
            setSelectedDayTVText(time);
        }

        return setToTime;
    }

    protected void setSelectedDayTVText(long time) {
        setSelectedDayTVText(sdf.format(time).toUpperCase());
    }

    protected void setSelectedDayTVText(String text) {
        if (careSelectedDayTV == null) {
            return;
        }

        careSelectedDayTV.setText(text);
    }

    @Nullable @Override public String getTitle() {
        return getString(R.string.card_care_title);
    }

    @Override public Integer getLayoutId() {
        return R.layout.care_activity_fragment;
    }

    @Override public void onClick(View v) {
        String stackTitle;
        int id = v.getId();
        switch (id) {
            case R.id.care_activity_current_day:
                DayPickerPopup dayPickerPopup = DayPickerPopup.newInstance(DAYS_CARE_CAN_GO_BACK);
                dayPickerPopup.setCallback(dppCallback);
                stackTitle = dayPickerPopup.getClass().getCanonicalName();
                BackstackManager.getInstance().navigateToFloatingFragment(dayPickerPopup, stackTitle, true);
                break;
            case R.id.care_filter_container:
                availableDeviceList = CareActivityController.instance().getFilterableDevices();
                CareMultiModelPopup devicePickerPopup = CareMultiModelPopup.newInstance(
                      availableDeviceList,
                      null,
                      filteredToDevices,
                      true
                );
                devicePickerPopup.setCallback(selectedDevicesCallback);
                stackTitle = devicePickerPopup.getClass().getCanonicalName();
                BackstackManager.getInstance().navigateToFloatingFragment(devicePickerPopup, stackTitle, true);
                break;
            case R.id.care_activity_zoom:
                Bundle bundle = new Bundle(1);
                bundle.putLong(FullScreenActivityGraph.TIMELINE_START, viewingTimelineTime);

                FullscreenFragmentActivity.launch(
                      getActivity(),
                      FullScreenActivityGraph.class,
                      ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                      bundle
                );
                break;
            default:
                break;
        }
    }

    @Override public void onError(Throwable cause) {
        enableButtonsAndShowProgress(true, false);
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override public void activitiesLoaded(List<ActivityLine> activityLines) {
        enableButtonsAndShowProgress(true, false);

        if (activityEventView != null) {
            activityEventView.setEvents(activityLines, viewingTimelineTime);
            activityEventView.invalidate();
        }
    }

    @Override public void activityHistoryLoaded(List<CareHistoryModel> entries, String nextQueryToken) {
        filteredToDevices = CareActivityController.instance().getSelectedCareDevices();
        availableDeviceList = CareActivityController.instance().getFilterableDevices();

        if (filterDetailText != null) {
            filterDetailText.setText(
                  String.format("%d of %d Devices", filteredToDevices.size(), availableDeviceList.size())
            );
        }

        List<CareHistoryModel> historyModels = new ArrayList<>(entries.size());
        for (int i = 0, listSize = entries.size(); i < listSize; i++) {
            if (entries.get(i).getCalendarDayOfYear() == viewingDay) {
                historyModels.add(entries.get(i));
            }
        }

        if (historyListAdapter != null) {
            historyListAdapter.addAll(historyModels);
        }
        else {
            try {
                historyListAdapter = new CareHistoryListAdapter(getActivity(), historyModels);
                historyView.setDivider(null);
                historyView.setAdapter(historyListAdapter);
            } catch (NullPointerException exception) {
                //nothing to do right now
            }
        }

        historyView.setOnScrollListener(null);
        nextToken.set(historyModels.isEmpty() ? null : nextQueryToken); // If we queried, but didn't get any results for today. so we don't requery.
        loadingMore.set(false);
        historyView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override public void onScrollStateChanged(AbsListView view, int scrollState) {}

            @Override public void onScroll(
                  AbsListView view,
                  int firstVisibleItem,
                  int visibleItemCount,
                  int totalItemCount
            ) {
                String token = nextToken.get();
                if (TextUtils.isEmpty(token)) {
                    loadingMore.set(false);
                    return;
                }

                boolean topOutOfView = (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD);
                if (!loadingMore.get() && topOutOfView) {
                    loadingMore.set(true);
                    CareActivityController.instance().loadActivityHistory(null, token, filteredToDevices);
                }
            }
        });
    }

    private Calendar getNewViewingTimelineCalendar() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(viewingTimelineTime);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 59);

        return calendar;
    }

    private String startNewDay() {
        nextToken.set(null);
        loadingMore.set(true);
        historyListAdapter = null;

        Calendar calendar = getNewViewingTimelineCalendar();
        String newQueryStartToken = String.format("%d", calendar.getTimeInMillis());
        viewingDay = calendar.get(Calendar.DAY_OF_YEAR);

        return newQueryStartToken;
    }

    private void enableButtonsAndShowProgress(boolean enable, boolean showProgress) {
        if (showProgress) {
            showProgressBar();
        }
        else {
            hideProgressBar();
        }

        if (careFilterContainer != null) {
            careFilterContainer.setEnabled(enable);
        }

        if (careSelectedDayTV != null) {
            careSelectedDayTV.setEnabled(enable);
        }

        if (careZoomIV != null) {
            careZoomIV.setEnabled(enable);
        }
    }

}
