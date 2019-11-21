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
package arcus.app.common.popups;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import arcus.cornea.controller.SubscriptionController;
import arcus.cornea.model.StringPair;
import arcus.cornea.subsystem.cameras.CameraDeviceListController;
import arcus.cornea.subsystem.cameras.ClipListingController;
import arcus.cornea.subsystem.cameras.model.CameraModel;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CameraFiltersPopup extends BaseFragment implements CameraDeviceListController.Callback {
    private static final int ALL_DAYS = 0;
    private static final int TODAY = 1;
    private static final int YESTERDAY = 2;
    private static final int LAST_7_DAYS = 3;
    private static final int LAST_30_DAYS = 4;
    private static final int LAST_3_MONTHS = 5;
    private static final int OLDER_THAN_3_MONTHS = 6;
    private static final int OLDER_THAN_6_MONTHS = 7;
    private static final int OVER_A_YEAR = 8;

    private Callback callback;
    List<StringPair> deviceFilterList = new ArrayList<>();
    List<StringPair> rangeFilterList = new ArrayList<>();
    private CameraDeviceListController mController;
    private ListenerRegistration mCallbackListener;
    private Calendar startTime;
    private Calendar endTime;
    private String deviceAddress;
    private Version1TextView deviceFilterValue;
    private Version1TextView dateFilterValue;
    private String dateFilterDisplay;
    private boolean bFirstLoad = true;
    private int defaultTimeRange = 0;

    protected View contentView;

    public interface Callback {
        void filterApplied();
    }

    protected final TupleSelectorPopup.Callback deviceFilterCallback = new TupleSelectorPopup.Callback() {
        @Override public void selectedItem(StringPair selected) {
            deviceAddress = selected.getKey();
            String deviceFilterDisplay = selected.getValue();
            if(getString(R.string.all_cameras).equals(deviceAddress)) {
                deviceAddress = null;
            }
            deviceFilterValue.setText(deviceFilterDisplay);
            hideActionBar();
        }
    };

    protected final TupleSelectorPopup.Callback dateFilterCallback = new TupleSelectorPopup.Callback() {
        @Override public void selectedItem(StringPair selected) {
            int selection = Integer.valueOf(selected.getKey());
            startTime = Calendar.getInstance();
            clearTimeFields(startTime);
            endTime = Calendar.getInstance();
            dateFilterDisplay = selected.getValue();
            defaultTimeRange = Integer.valueOf(selected.getKey());
            switch(selection) {
                case ALL_DAYS:
                    endTime = null;
                    startTime = null;
                    break;
                case TODAY:
                    endTime = null;
                    break;
                case YESTERDAY:
                    startTime.add(Calendar.DAY_OF_MONTH, -1);
                    clearTimeFields(endTime);
                    break;
                case LAST_7_DAYS:
                    endTime = null;
                    startTime.add(Calendar.DAY_OF_MONTH, -6);
                    break;
                case LAST_30_DAYS:
                    endTime = null;
                    startTime.add(Calendar.DAY_OF_MONTH, -30);
                    break;
                case LAST_3_MONTHS:
                    endTime = null;
                    startTime.add(Calendar.MONTH, -3);
                    clearTimeFields(startTime);
                    break;
                case OLDER_THAN_3_MONTHS:
                    startTime = null;
                    endTime.add(Calendar.MONTH, -3);
                    clearTimeFields(endTime);
                    break;
                case OLDER_THAN_6_MONTHS:
                    startTime = null;
                    endTime.add(Calendar.MONTH, -6);
                    clearTimeFields(endTime);
                    break;
                case OVER_A_YEAR:
                    startTime = null;
                    endTime.add(Calendar.YEAR, -1);
                    clearTimeFields(endTime);
                    break;
            }
            dateFilterValue.setText(dateFilterDisplay);
            hideActionBar();
        }
    };

    @SuppressWarnings({"ConstantConditions"}) @NonNull public static CameraFiltersPopup newInstance() {
        return new CameraFiltersPopup();
    }

    @Override public Integer getLayoutId() {
        return R.layout.fullscreen_arcus_popup_fragment_nopadding;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View contentView = inflater.inflate(contentSectionLayout(), container, false);
        contentView.setOnClickListener(view -> {
            //do nothing, but don't allow the click events to hit the fragment behind it.
        });
        ImageView closeButton = contentView.findViewById(R.id.close_button);
        closeButton.setOnClickListener(view -> BackstackManager.getInstance().navigateBack());
        Version1Button applyFilter = contentView.findViewById(R.id.btnApplyFilter);
        applyFilter.setOnClickListener(view -> {
            ClipListingController.instance().setFilterByDevice(deviceAddress);
            ClipListingController.instance().setFilterByTimeValue(defaultTimeRange);
            ClipListingController.instance().setFilterByTime(startTime == null ? null : startTime.getTime(), endTime == null ? null : endTime.getTime());
            if(callback != null) {
                callback.filterApplied();
            }
            BackstackManager.getInstance().navigateBack();
        });
        Version1Button clearFilter;
        clearFilter = contentView.findViewById(R.id.btnClearAll);
        clearFilter.setOnClickListener(view -> {
            dateFilterValue.setText(getString(R.string.all_days));
            deviceFilterValue.setText(getString(R.string.all_cameras));
            startTime = null;
            endTime = null;
            deviceAddress = null;
            defaultTimeRange = 0;
            if(callback != null) {
                callback.filterApplied();
            }
        });
        View deviceFilter =  contentView.findViewById(R.id.camera_layout);
        deviceFilter.setOnClickListener(view -> {
            TupleSelectorPopup popup = TupleSelectorPopup.newInstance(
                    deviceFilterList,
                    R.string.choose_a_camera,
                    deviceAddress,
                    true
            );
            popup.setCallback(deviceFilterCallback);
            popup.showDone();
            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
        });

        initializeRanges();
        final View rangeFilter = contentView.findViewById(R.id.date_layout);
        if(SubscriptionController.isPremiumOrPro()) {
            rangeFilter.setOnClickListener(view -> {
                TupleSelectorPopup popup = TupleSelectorPopup.newInstance(
                        rangeFilterList,
                        R.string.show_clips_from,
                        Integer.toString(defaultTimeRange),
                        true
                );
                popup.setCallback(dateFilterCallback);
                popup.showDone();
                BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
            });
        } else {
            rangeFilter.setVisibility(View.GONE);
            contentView.findViewById(R.id.basic_date_layout).setVisibility(View.VISIBLE);
        }

        deviceFilterValue = contentView.findViewById(R.id.device_filter_value);
        dateFilterValue = contentView.findViewById(R.id.date_filter_value);

        //todo: we might want to limit height to half of the screen size
        return contentView;
    }


    public void clearTimeFields(Calendar cal) {
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    public Integer contentSectionLayout() {
        return R.layout.fragment_camera_filter;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(mController ==null){
            mController = CameraDeviceListController.instance();
        }
        mCallbackListener = mController.setCallback(this);

        if(bFirstLoad) {
            loadFilteredDate();
            loadFilteredDevice();
        } else {
            dateFilterValue.setText(dateFilterDisplay);
            if(deviceAddress != null) {
                Model model = CorneaUtils.getDeviceModelFromCache(CorneaUtils.getDeviceAddress(deviceAddress));
                if(model != null) {
                    deviceFilterValue.setText(((DeviceModel)model).getName());
                }
            } else {
                deviceFilterValue.setText(getString(R.string.all_cameras));
            }
        }
        hideActionBar();

        bFirstLoad = false;
    }

    private void loadFilteredDate() {
        if(ClipListingController.instance().getFilterStartTime() == null) {
            startTime = null;
        } else {
            startTime = Calendar.getInstance();
            startTime.setTime(ClipListingController.instance().getFilterStartTime());
        }

        if(ClipListingController.instance().getFilterEndTime() == null) {
            endTime = null;
        } else {
            endTime = Calendar.getInstance();
            endTime.setTime(ClipListingController.instance().getFilterEndTime());
        }
        dateFilterValue.setText(getFilterTimeDisplay());
    }

    private void loadFilteredDevice() {
        if(ClipListingController.instance().getFilterDeviceAddress() != null) {
            Model model = CorneaUtils.getDeviceModelFromCache(CorneaUtils.getDeviceAddress(ClipListingController.instance().getFilterDeviceAddress()));
            if(model != null) {
                deviceFilterValue.setText(((DeviceModel)model).getName());
                deviceAddress = ClipListingController.instance().getFilterDeviceAddress();
                return;
            }
        }
        deviceFilterValue.setText(getString(R.string.all_cameras));
    }

    private String getFilterTimeDisplay() {
        defaultTimeRange = ClipListingController.instance().getFilterByTimeValue();
        switch (defaultTimeRange) {
            case ALL_DAYS:
                return getString(R.string.all_days);
            case TODAY:
                return getString(R.string.today);
            case YESTERDAY:
                return getString(R.string.yesterday);
            case LAST_7_DAYS:
                return getString(R.string.last_seven_days);
            case LAST_30_DAYS:
                return getString(R.string.last_thirty_days);
            case LAST_3_MONTHS:
                return getString(R.string.last_three_months);
            case OLDER_THAN_3_MONTHS:
                return getString(R.string.three_months_and_older);
            case OLDER_THAN_6_MONTHS:
                return getString(R.string.six_months_and_older);
            case OVER_A_YEAR:
                return getString(R.string.older_than_one_year);
        }
        return getString(R.string.all_days);
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mCallbackListener ==null || !mCallbackListener.isRegistered()){
            return;
        }
        mCallbackListener.remove();
        showActionBar();
    }

    @Override @NonNull public String getTitle() {
        return "";
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void showDevices(List<CameraModel> cameraModels) {
        deviceFilterList.clear();
        deviceFilterList.add(new StringPair(getString(R.string.all_cameras), getString(R.string.all_cameras)));
        for (CameraModel model : cameraModels) {
            deviceFilterList.add(new StringPair(model.getCameraID(), model.getCameraName()));
        }
    }

    public void initializeRanges() {
        rangeFilterList.clear();
        rangeFilterList.add(new StringPair(Integer.toString(ALL_DAYS), getString(R.string.all_days)));
        rangeFilterList.add(new StringPair(Integer.toString(TODAY), getString(R.string.today)));
        rangeFilterList.add(new StringPair(Integer.toString(YESTERDAY), getString(R.string.yesterday)));
        rangeFilterList.add(new StringPair(Integer.toString(LAST_7_DAYS), getString(R.string.last_seven_days)));
        rangeFilterList.add(new StringPair(Integer.toString(LAST_30_DAYS), getString(R.string.last_thirty_days)));
        rangeFilterList.add(new StringPair(Integer.toString(LAST_3_MONTHS), getString(R.string.last_three_months)));
        rangeFilterList.add(new StringPair(Integer.toString(OLDER_THAN_3_MONTHS), getString(R.string.three_months_and_older)));
        rangeFilterList.add(new StringPair(Integer.toString(OLDER_THAN_6_MONTHS), getString(R.string.six_months_and_older)));
        rangeFilterList.add(new StringPair(Integer.toString(OVER_A_YEAR), getString(R.string.older_than_one_year)));
    }


    protected void showFullScreen(boolean fullscreen) {
        Activity activity = getActivity();
        if (activity == null || !(activity instanceof BaseActivity)) {
            return;
        }

        ActionBar actionBar = ((BaseActivity)activity).getSupportActionBar();
        if (actionBar != null) {
            if (fullscreen) {
                actionBar.hide();
            }
            else {
                actionBar.show();
            }
        }
    }
}
