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
package arcus.app.subsystems.history;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import arcus.cornea.controller.SubscriptionController;
import arcus.cornea.dto.HistoryLogEntries;
import arcus.cornea.provider.PlaceModelProvider;
import arcus.cornea.subsystem.care.CareActivityController;
import arcus.cornea.subsystem.care.model.ActivityLine;
import arcus.cornea.subsystem.model.CareHistoryModel;
import arcus.cornea.utils.Listeners;
import com.iris.client.bean.HistoryLog;
import com.iris.client.capability.Place;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.PlaceModel;
import com.iris.client.util.Result;
import arcus.app.R;
import arcus.app.activities.FullscreenFragmentActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.popups.DayPickerPopup;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.validation.DayOfYearFilter;
import arcus.app.common.view.ActivityEventView;
import arcus.app.common.view.Version1TextView;
import arcus.app.dashboard.popups.responsibilities.history.HistoryServicePopupManager;
import arcus.app.subsystems.care.fragment.FullScreenActivityGraph;
import arcus.app.subsystems.history.adapters.HistoryFragmentListAdapter;
import arcus.app.subsystems.history.view.HistoryDeviceSelectionPopup;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class HistoryFragment extends BaseFragment implements CareActivityController.Callback,
        CareActivityController.HistoryCallback {
    private int VISIBLE_THRESHOLD = 8;
    private int DEFAULT_QUERY_LIMIT = 20;
    private static final int DAYS_CARE_CAN_GO_BACK = 14;
    @Nullable
    private String nextQueryToken;
    private String selectedDeviceID = "";
    private String selectedDeviceName = "";

    private DayOfYearFilter dayOfYearFilter = new DayOfYearFilter(new Date().getTime());
    private ListenerRegistration listener;
    private HistoryFragmentListAdapter historyListEntriesAdapter;
    private ListView mHistoryListView;
    protected TextView careSelectedDayTV;
    protected ActivityEventView activityEventView;
    private View graphLayout;
    private Version1TextView activityInvalidDevice;
    private final SimpleDateFormat sdf = new SimpleDateFormat("ccc MMM d", Locale.getDefault());
    private PlaceModel placeModel;
    protected View careZoomIV;
    protected List<String> filteredToDevices = new ArrayList<>();
    protected List<String> availableDeviceList = Collections.emptyList();
    private boolean loadingMore = false;
    protected long viewingTimelineTime = System.currentTimeMillis();
    protected long filteredDay = new Date().getTime();
    private Version1TextView emptyList;
    private DayPickerPopup.Callback dppCallback = new DayPickerPopup.Callback() {
        @Override public void selected(long time) {
            filteredDay = time;
            if(historyListEntriesAdapter == null) {
                return;
            }
            int position = historyListEntriesAdapter.getDayOfYearIndex(time);
            dayOfYearFilter = new DayOfYearFilter(time);

            Calendar date1 = Calendar.getInstance();
            date1.setTimeInMillis(System.currentTimeMillis());
            date1.set(Calendar.HOUR_OF_DAY, 23);
            date1.set(Calendar.MINUTE, 59);
            date1.set(Calendar.SECOND, 59);
            Calendar date2 = Calendar.getInstance();
            date2.setTimeInMillis(time);

            long diff = date1.getTimeInMillis() - date2.getTimeInMillis();
            int differential = (int) TimeUnit.DAYS.convert(diff, TimeUnit.MILLISECONDS);
            moveTimelineTo(time);

            if (position != -1) {
                nextQueryToken = historyListEntriesAdapter.getNextTokenForFilter(time);
                historyListEntriesAdapter.filterExistingToDay(dayOfYearFilter);
                mHistoryListView.setAdapter(historyListEntriesAdapter);
                mHistoryListView.setSelection(0);
            }

            final List<DeviceModel> models = SessionModelManager.instance().getDevices();
            List<String> modelAddresses = new ArrayList<>();
            String selectedAddress = "";
            for(DeviceModel model : models) {
                modelAddresses.add(model.getAddress());
                if(model.getId().equals(selectedDeviceID)) {
                    selectedAddress = model.getAddress();
                }
            }
            deviceSelected(selectedAddress);
        }
    };


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        placeModel = PlaceModelProvider.getCurrentPlace().get();

        activityEventView = (ActivityEventView) view.findViewById(R.id.care_half_activity_graph);
        activityInvalidDevice = (Version1TextView) view.findViewById(R.id.care_activity_invalid_device);
        graphLayout = view.findViewById(R.id.graph_layout);

        careSelectedDayTV = (TextView) view.findViewById(R.id.care_activity_current_day);
        careSelectedDayTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDay();
            }
        });

        mHistoryListView = (ListView) view.findViewById(R.id.history_list);
        View menu = view.findViewById(R.id.show_all_menu);
        menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickDevice();
            }
        });

        careZoomIV = view.findViewById(R.id.care_activity_zoom);
        careZoomIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle bundle = new Bundle(1);
                bundle.putLong(FullScreenActivityGraph.TIMELINE_START, viewingTimelineTime);
                if(filteredToDevices == null || filteredToDevices.size() == 0) {
                    bundle.putStringArrayList(FullScreenActivityGraph.DEFAULT_DEVICES, new ArrayList<>(availableDeviceList));
                } else {
                    bundle.putStringArrayList(FullScreenActivityGraph.DEFAULT_DEVICES, new ArrayList<>(filteredToDevices));
                }

                FullscreenFragmentActivity.launch(
                        getActivity(),
                        FullScreenActivityGraph.class,
                        ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
                        bundle
                );
            }
        });
        availableDeviceList = CareActivityController.instance().getFilterableDevices();
        filteredToDevices = availableDeviceList;
        loadDefaultAdapter();

        viewingTimelineTime = CareActivityController.instance().getBaselineTimeFrom(viewingTimelineTime);
        CareActivityController.instance().loadActivitiesDuring(viewingTimelineTime, availableDeviceList, false);

        showCareActivityScreen(null);
        emptyList = (Version1TextView) view.findViewById(R.id.empty_list);

        return (view);
    }

    @NonNull
    @Override
    public String getTitle() { return getString(R.string.history_title).toUpperCase(); }

    @Override
    public Integer getLayoutId() { return R.layout.fragment_history_list; }

    @Override
    public void onResume() {
        super.onResume();
        listener = CareActivityController.instance().setCallback(this);
        getActivity().setTitle(getTitle());
    }

    @Override
    public void onPause() {
        super.onPause();
        listener = Listeners.clear(listener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }



    private void setupDefaultEndlessScroll() {
        mHistoryListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    trimList();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (nextQueryToken != null && mHistoryListView.getAdapter() instanceof HistoryFragmentListAdapter) {
                    if (!loadingMore && (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD)) {
                        endlessScroll();
                    }
                }
            }

            private void endlessScroll() {
                if (placeModel == null) return;

                loadingMore = true;

                HistoryLogEntries.forPlaceModel(placeModel, DEFAULT_QUERY_LIMIT, nextQueryToken)
                      .onCompletion(new Listener<Result<HistoryLogEntries>>() {
                          @Override
                          public void onEvent(@NonNull final Result<HistoryLogEntries> result) {
                              getActivity().runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      loadingMore = false;

                                      if (result.isValue()) {
                                          List<HistoryLog> entries = filterEntries(result);
                                          appendHistoryFragmentListAdapter(entries);
                                      }
                                  }
                              });
                          }
                      });
            }
        });
    }

    private void setupDeviceEndlessScroll(@NonNull final DeviceModel deviceModel) {
        mHistoryListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    trimList();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (nextQueryToken != null && mHistoryListView.getAdapter() instanceof HistoryFragmentListAdapter) {
                    if (!loadingMore && (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD)) {
                        endlessScroll();
                    }
                }
            }

            private void endlessScroll() {
                loadingMore = true;
                HistoryLogEntries.forDeviceModel(deviceModel, DEFAULT_QUERY_LIMIT, nextQueryToken)
                      .onCompletion(new Listener<Result<HistoryLogEntries>>() {
                          @Override
                          public void onEvent(@NonNull final Result<HistoryLogEntries> result) {
                              getActivity().runOnUiThread(new Runnable() {
                                  @Override
                                  public void run() {
                                      loadingMore = false;

                                      if (result.isValue()) {
                                          List<HistoryLog> entries = filterEntries(result);
                                          appendHistoryFragmentListAdapter(entries);
                                      }
                                  }
                              });
                          }
                      });
            }
        });
    }

    private void trimList() {
        // Don't load any new entries while we are trimming.
        loadingMore = true;
        try {
            int LIST_SIZE_THRESHOLD = 2_500;
            historyListEntriesAdapter.trimListToThreshold(LIST_SIZE_THRESHOLD);
        }
        catch (Exception ex) {
            logger.debug("Caught ex trying to trim list:", ex);
        }
        loadingMore = false;
    }

    private void loadDefaultAdapter() {
        showProgressBar();
        resetTokenAndDeviceID();

        setupDefaultEndlessScroll();

        String stringTimestamp = String.valueOf(filteredDay);

        if (placeModel == null) return;

        HistoryLogEntries.forPlaceModel(placeModel, DEFAULT_QUERY_LIMIT, stringTimestamp).onCompletion(new Listener<Result<HistoryLogEntries>>() {
            @Override
            public void onEvent(@NonNull final Result<HistoryLogEntries> result) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideProgressBar();
                        if (result.isValue()) {
                            List<HistoryLog> entries = filterEntries(result);
                            setNewAdapter(new HistoryFragmentListAdapter(getActivity(), entries, false));
                        }
                    }
                });
            }
        });
    }

    private void loadDeviceHistoryLogs(@NonNull final DeviceModel selectedDeviceModel) {
        selectedDeviceID = selectedDeviceModel.getId();
        logger.debug("Loading events for device ID:[{}] (Name: {})", selectedDeviceID, selectedDeviceModel.getName());

        String stringTimestamp = String.valueOf(filteredDay);

        loadingMore = true;
        HistoryLogEntries.forDeviceModel(selectedDeviceModel, DEFAULT_QUERY_LIMIT, stringTimestamp).onCompletion(new Listener<Result<HistoryLogEntries>>() {
            @Override
            public void onEvent(@NonNull final Result<HistoryLogEntries> result) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(result.isValue()) {
                            List<HistoryLog> entries = filterEntries(result);
                            if (SubscriptionController.isPremiumOrPro()) {
                                //don't update because the scrollListener is going to do it again.
                                //nextQueryToken = result.getValue().getNextToken();
                            } else {
                                DayOfYearFilter filter = new DayOfYearFilter(System.currentTimeMillis());
                                filter.apply(entries);
                                nextQueryToken = null;
                            }
                            setNewAdapter(new HistoryFragmentListAdapter(getActivity(), entries, false));
                            setupDeviceEndlessScroll(selectedDeviceModel);
                            loadingMore = false;
                        }
                    }
                });
            }
        });
    }

    @NonNull
    private List<HistoryLog> filterEntries(@NonNull Result<HistoryLogEntries> result) {
        List<HistoryLog> entries = new ArrayList<>(result.getValue().getEntries());

        if (dayOfYearFilter != null) {
            // If we have to trim back some of the response because we went past our previous day, null the token
            // so we don't continue to scroll, else, allow scrolling (if token isn't null anyhow - which
            // should only happen if these were the last history entries the server had for this record type)
            nextQueryToken = dayOfYearFilter.apply(entries) ? null : result.getValue().getNextToken();
        }
        else {
            nextQueryToken = result.getValue().getNextToken();
        }

        if(entries.size() == 0) {
            emptyList.setVisibility(View.VISIBLE);
            mHistoryListView.setVisibility(View.GONE);
            if(selectedDeviceID.equals("")) {
                emptyList.setText(getString(R.string.no_history));
            } else {
                emptyList.setText(getString(R.string.no_history_for_device, selectedDeviceName));
            }
        } else {
            emptyList.setVisibility(View.GONE);
            mHistoryListView.setVisibility(View.VISIBLE);
        }
        return entries;
    }

    /**
     * Logs
     * history.filter.day	User lands on history → choose filter → day
     */
    private void pickDay() {
        DayPickerPopup picker;
        if(placeModel != null && Place.SERVICELEVEL_BASIC.equals(placeModel.getServiceLevel())) {
            picker = DayPickerPopup.newInstance(2);
        } else {
            picker = DayPickerPopup.newInstance();
        }

        picker.setCallback(dppCallback);
        BackstackManager.getInstance().navigateToFloatingFragment(picker, picker.getClass().getCanonicalName(), true);
    }

    private void pickDevice() {
        final List<DeviceModel> models = SessionModelManager.instance().getDevices();
        List<String> modelAddresses = new ArrayList<>();
        String selectedAddress = "";
        for(DeviceModel model : models) {
            modelAddresses.add(model.getAddress());
            if(model.getId().equals(selectedDeviceID)) {
                selectedAddress = model.getAddress();
            }
        }
        HistoryDeviceSelectionPopup devicePickerPopup = HistoryDeviceSelectionPopup.newInstance(
                modelAddresses,
                null,
                selectedAddress
        );
        devicePickerPopup.setCallback(new HistoryDeviceSelectionPopup.Callback() {
            @Override
            public void itemSelectedAddress(String addressesSelected) {
                resetTokenAndDeviceID();
                deviceSelected(addressesSelected);

            }
        });
        BackstackManager.getInstance().navigateToFloatingFragment(devicePickerPopup, devicePickerPopup.getClass().getCanonicalName(), true);
    }

    private void deviceSelected(String addressesSelected) {
        final List<DeviceModel> models = SessionModelManager.instance().getDevices();

        DeviceModel selectedModel = null;
        for(DeviceModel model : models) {
            if(model.getAddress().equals(addressesSelected)) {
                selectedModel = model;
            }
        }

        if(selectedModel != null) {
            selectedDeviceID = selectedModel.getId();
            selectedDeviceName = selectedModel.getName();
            filteredToDevices = Lists.newArrayList(selectedModel.getAddress());
            loadDeviceHistoryLogs(selectedModel);
        } else {
            selectedDeviceID = "";
            filteredToDevices = availableDeviceList;
            loadDefaultAdapter();
        }


        CareActivityController.instance().loadActivitiesDuring(viewingTimelineTime, filteredToDevices, false);

        showCareActivityScreen(selectedModel);
    }

    private void showCareActivityScreen(DeviceModel selectedModel) {
        if (SubscriptionController.isPremiumOrPro()) {
            if(selectedModel == null || availableDeviceList.contains(selectedModel.getAddress())) {
                activityEventView.setVisibility(View.VISIBLE);
                activityInvalidDevice.setVisibility(View.GONE);
                careZoomIV.setVisibility(View.VISIBLE);
            } else {
                activityEventView.setVisibility(View.GONE);
                activityInvalidDevice.setVisibility(View.VISIBLE);
                careZoomIV.setVisibility(View.GONE);
            }
        } else {
            activityEventView.setVisibility(View.GONE);
            activityInvalidDevice.setVisibility(View.GONE);
            careZoomIV.setVisibility(View.GONE);
        }
    }

    private void setNewAdapter(final HistoryFragmentListAdapter fragmentAdapter) {
        if (fragmentAdapter != null && fragmentAdapter.getCount() > 0) {
            HistoryServicePopupManager.getInstance().triggerPopups();
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                historyListEntriesAdapter = fragmentAdapter;
                mHistoryListView.setAdapter(historyListEntriesAdapter);
            }
        });
    }

    private void appendHistoryFragmentListAdapter(@NonNull final List<HistoryLog> entries) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (historyListEntriesAdapter != null) {
                    historyListEntriesAdapter.appendEntries(entries);
                }
                else {
                    historyListEntriesAdapter = new HistoryFragmentListAdapter(getActivity(), entries, false);
                    mHistoryListView.setAdapter(historyListEntriesAdapter);
                }
            }
        });
    }

    private void resetTokenAndDeviceID() {
        nextQueryToken = null;
    }

    protected void moveTimelineTo(long time) {
        if (activityEventView == null) {
            return;
        }

        activityEventView.setEndTime(getSelectedTimeText(time));
        viewingTimelineTime = CareActivityController.instance().getBaselineTimeFrom(time);
        CareActivityController.instance().loadActivitiesDuring(time, filteredToDevices, false);
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

    private String startNewDay() {
        return "";
    }


    @Override
    public void onError(Throwable cause) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override
    public void activitiesLoaded(List<ActivityLine> activityLines) {
        if (activityEventView != null) {
            activityEventView.setEvents(activityLines, viewingTimelineTime);
            activityEventView.invalidate();
        }
    }

    @Override
    public void activityHistoryLoaded(List<CareHistoryModel> entries, String nextToken) {

    }
}
