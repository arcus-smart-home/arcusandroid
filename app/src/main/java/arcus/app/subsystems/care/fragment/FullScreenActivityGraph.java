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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.SessionController;
import arcus.cornea.subsystem.care.CareActivityController;
import arcus.cornea.subsystem.care.model.ActivityLine;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.PlaceModel;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.ViewBackgroundTarget;
import arcus.app.common.image.picasso.transformation.AlphaOverlayTransformation;
import arcus.app.common.image.picasso.transformation.AlphaPreset;
import arcus.app.common.image.picasso.transformation.BlurTransformation;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.popups.DayPickerPopup;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.PanningActivityEventView;
import arcus.app.subsystems.care.adapter.CareFilterDevicesAdapter;
import arcus.app.subsystems.care.util.CareUtilities;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FullScreenActivityGraph extends Fragment implements CareActivityController.Callback {
    public static final String TIMELINE_START = "TIMELINE_START";
    public static final String DEFAULT_DEVICES = "DEFAULT_DEVICES";
    protected static final int DAYS_CARE_CAN_GO_BACK = 14;
    protected View fragmentLL;
    protected ImageView careActivityZoomOut;
    protected TextView careActivityCurrentDay;
    protected RecyclerView careDevicesFilterRV;
    protected ViewBackgroundTarget viewBackgroundTarget;
    protected PanningActivityEventView panningActivityEventView;
    protected long currentTimeFilter = System.currentTimeMillis();
    private List<String> filteredToDevices = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("ccc MMM d", Locale.getDefault());
    private DayPickerPopup dayPickerPopup;
    private CareFilterDevicesAdapter adapter;
    boolean summaryNotSelected = false;
    List<String> allDevices = new ArrayList<>();

    private final DayPickerPopup.Callback dppCallback = new DayPickerPopup.Callback() {
        @Override
        public void selected(long time) {
            moveTimelineTo(time, summaryNotSelected);
        }
    };
    private CareFilterDevicesAdapter.RVItemClicked adapterItemClickListener =
          new CareFilterDevicesAdapter.RVItemClicked() {
              @Override public void itemClicked(@Nullable String address, int adapterPosition) {
                  // Check if we're clicking the same device we have already filtered to.
                  if (!filteredToDevices.isEmpty() && !TextUtils.isEmpty(address)) {
                      if (filteredToDevices.contains(address) && filteredToDevices.size() == 1) {
                          return;
                      }
                  }

                  // Otherwise just clear the list, add the address if it's not null/empty
                  filteredToDevices.clear();
                  summaryNotSelected = adapterPosition != 0;
                  if (summaryNotSelected && !TextUtils.isEmpty(address)) {
                      filteredToDevices.add(address);
                  }

                  if(!summaryNotSelected) {
                      if(allDevices != null && allDevices.size() > 0) {
                          filteredToDevices = new ArrayList<>(allDevices);
                      }
                  }

                  // Get new list of filtered devices
                  shouldUseDash3(summaryNotSelected);
                  CareActivityController.instance().loadActivitiesDuring(currentTimeFilter, filteredToDevices, false, summaryNotSelected);

                  // Remove this listener (will be added back in onError/onReceive)
                  adapter.setItemClickedListener(null);
              }
          };

    public static FullScreenActivityGraph newInstance() {
        return new FullScreenActivityGraph();
    }

    @Override public View onCreateView(
          @NonNull LayoutInflater inflater,
          ViewGroup container,
          Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_fullscreen_care_activity, container, false);
        if (view != null) {
            careActivityZoomOut = (ImageView) view.findViewById(R.id.care_activity_zoom);
            careActivityZoomOut.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    getActivity().finish();
                }
            });

            careActivityCurrentDay = (TextView) view.findViewById(R.id.care_activity_current_day);
            careActivityCurrentDay.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    dayPickerPopup = DayPickerPopup.newInstance(DAYS_CARE_CAN_GO_BACK);
                    dayPickerPopup.setCallback(dppCallback);
                    dayPickerPopup.setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            switch (v.getId()) {
                                case R.id.fragment_arcus_pop_up_close_btn:
                                    if (dayPickerPopup != null) {
                                        dayPickerPopup.doClose();
                                    }
                                    getActivity().getSupportFragmentManager().popBackStack();
                                    break;
                            }
                        }
                    });

                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    manager.beginTransaction()
                          .add(R.id.care_floating, dayPickerPopup)
                          .addToBackStack(dayPickerPopup.getClass().getCanonicalName())
                          .commit();
                }
            });

            careDevicesFilterRV = (RecyclerView) view.findViewById(R.id.care_activity_devices_filter);
            panningActivityEventView = (PanningActivityEventView) view.findViewById(R.id.care_full_activity_graph);
            panningActivityEventView.setViewportWidthInHours(4);
            panningActivityEventView.setShouldUseDash3Line(false);
            panningActivityEventView.setAxisSizeDP(62);
            panningActivityEventView.setBucketSize(5f);

            fragmentLL = view.findViewById(R.id.care_fullscreen_container);

            Bundle args = getArguments();
            if (args != null) {
                currentTimeFilter = args.getLong(TIMELINE_START, System.currentTimeMillis());
                if (!CareActivityController.instance().isToday(currentTimeFilter)) {
                    panningActivityEventView.setEndTime(currentTimeFilter + TimeUnit.DAYS.toMillis(1) - TimeUnit.SECONDS.toMillis(1));
                    getSelectedTimeText(currentTimeFilter);
                }

                allDevices = args.getStringArrayList(DEFAULT_DEVICES);
            }
        }

        return view;
    }

    protected void moveTimelineTo(long time, boolean summaryNotSelected) {
        if (panningActivityEventView == null) {
            return;
        }

        panningActivityEventView.setEndTime(getSelectedTimeText(time));
        currentTimeFilter = CareActivityController.instance().getBaselineTimeFrom(time);
        CareActivityController.instance().loadActivitiesDuring(time, filteredToDevices, false, summaryNotSelected);
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
        if (careActivityCurrentDay == null) {
            return;
        }

        careActivityCurrentDay.setText(text);
    }

    @Override public void onResume() {
        super.onResume();
        View root = getView();
        if (root == null) {
            return;
        }

        viewBackgroundTarget = new ViewBackgroundTarget(fragmentLL);
        setUserBackground();
        filteredToDevices.clear();
        CareActivityController.instance().setCallback(this);
        if(allDevices != null && allDevices.size() > 0) {
            filteredToDevices = new ArrayList<>(allDevices);
        }
        CareActivityController.instance().loadActivitiesDuring(currentTimeFilter, filteredToDevices, false);

        List<Model> models = getModelListFrom(CareActivityController.instance().getFilterableDevices());
        List<ListItemModel> listItemModels = new ArrayList<>(models.size() + 1);

        ListItemModel summaryIcon = new ListItemModel(getString(R.string.summary_generic_text));
        summaryIcon.setImageResId(R.drawable.summary_icon);
        summaryIcon.setChecked(true);
        listItemModels.add(summaryIcon);

        List<ListItemModel> toSort = getListItemModelsFrom(models);
        Collections.sort(toSort, CareUtilities.listItemModelComparatorByName(CareUtilities.Sort.DSC));
        listItemModels.addAll(toSort);

        careDevicesFilterRV.setLayoutManager(getLayoutManager());

        adapter = new CareFilterDevicesAdapter(getActivity(), listItemModels);
        careDevicesFilterRV.setAdapter(adapter);
    }

    protected RecyclerView.LayoutManager getLayoutManager() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        return linearLayoutManager;
    }

    protected List<Model> getModelListFrom(@NonNull List<String> addressList) {
        List<Model> models = new ArrayList<>(addressList.size());

        for (String device : addressList) {
            Model model = CorneaClientFactory.getModelCache().get(device);
            if (model != null) {
                models.add(model);
            }
        }

        return models;
    }

    protected List<ListItemModel> getListItemModelsFrom(@NonNull List<Model> models) {
        List<ListItemModel> data = new ArrayList<>(models.size());

        for (Model model : models) {
            if (!(model instanceof DeviceModel)) {
                continue;
            }

            ListItemModel item = new ListItemModel();

            item.setAddress(model.getAddress());
            item.setData(model);
            item.setText(((DeviceModel) model).getName());
            data.add(item);
        }

        return data;
    }

    protected void setUserBackground() {
        final PlaceModel placeModel = SessionController.instance().getPlace();
        if (fragmentLL == null || placeModel == null) {
            return;
        }

        ImageManager.with(getActivity())
                .putPlaceImage(placeModel.getId())
                .withTransform(new BlurTransformation(getActivity()))
                .withTransform(new AlphaOverlayTransformation(AlphaPreset.DARKEN))
                .into(viewBackgroundTarget)
                .execute();
        }

    protected void shouldUseDash3(boolean shouldUse) {
        if (panningActivityEventView != null) {
            panningActivityEventView.setShouldUseDash3Line(shouldUse);
        }
    }

    protected void addAdapterClickListener() {
        if (adapter != null) {
            adapter.setItemClickedListener(adapterItemClickListener);
        }
    }

    @Override public void onError(Throwable cause) {
        addAdapterClickListener();
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override public void activitiesLoaded(List<ActivityLine> activityLines) {
        addAdapterClickListener();

        if (panningActivityEventView != null) {
            panningActivityEventView.setEvents(activityLines, currentTimeFilter);
            panningActivityEventView.invalidate();
        }
    }

}
