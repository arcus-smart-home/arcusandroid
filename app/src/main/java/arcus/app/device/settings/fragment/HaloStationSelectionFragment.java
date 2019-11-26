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
package arcus.app.device.settings.fragment;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.common.collect.ImmutableSet;

import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.device.smokeandco.HaloController;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.WeatherRadio;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.adapters.WeatherStationListAdapter;
import arcus.app.common.models.StationScanResult;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HaloStationSelectionFragment extends BaseFragment implements HaloController.Callback, WeatherStationListAdapter.Callback {


    private static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String EDIT_MODE = "EDIT_MODE";
    private View nextButton;
    private View skipWeatherSetupButton;
    private Version1Button rescanButton;
    private ListView stationList;
    private View noStationsFoundButtons;
    private Version1TextView title;
    private Version1TextView subtitle;
    protected WeatherStationListAdapter weatherStationListAdapter;
    private HaloController haloController;
    private int selectedStation;
    ArrayList<StationScanResult> items = new ArrayList<StationScanResult>();
    List<Map<String, Object>> stations = null;
    private HaloStationSelectionFragment frag;
    private boolean bShowAll = false;
    private DeviceModel model;
    public static final Set<String> UPDATE_ON = ImmutableSet.of(
        WeatherRadio.ATTR_PLAYINGSTATE,
        WeatherRadio.ATTR_STATIONSELECTED
    );

    @NonNull
    public static HaloStationSelectionFragment newInstance (String deviceAddress, boolean isEditMode) {
        HaloStationSelectionFragment fragment = new HaloStationSelectionFragment();

        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_ADDRESS, deviceAddress);
        bundle.putBoolean(EDIT_MODE, isEditMode);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        frag = this;

        stationList = (ListView) view.findViewById(R.id.station_list);
        noStationsFoundButtons = view.findViewById(R.id.no_station_layout);

        nextButton = view.findViewById(R.id.next_button);
        nextButton.setVisibility(isEditMode() ? View.GONE : View.VISIBLE);

        skipWeatherSetupButton = view.findViewById(R.id.skip_setup);
        skipWeatherSetupButton.setVisibility(isEditMode() ? View.GONE : View.VISIBLE);

        rescanButton = (Version1Button) view.findViewById(R.id.rescan);
        rescanButton.setColorScheme(isEditMode() ? Version1ButtonColor.WHITE : Version1ButtonColor.BLACK);

        Version1TextView footerDescription = (Version1TextView)view.findViewById(R.id.footer_description);
        footerDescription.setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);
        footerDescription.setVisibility(isEditMode() ? View.GONE : View.VISIBLE);

        title = (Version1TextView)view.findViewById(R.id.title);
        title.setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);
        subtitle = (Version1TextView)view.findViewById(R.id.subtitle);
        subtitle.setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);
        subtitle.setLinkTextColor(isEditMode() ? Color.WHITE : Color.BLACK);
        setListViewEnabled(true);
        return view;
    }

    @Override
    public void onDestroyView() {
        hideProgressBar();
        super.onDestroyView();
    }

    @Override
    public void onResume () {
        super.onResume();

        String deviceAddress = getArguments().getString(DEVICE_ADDRESS);

        setListViewEnabled(true);
        if (nextButton != null) {
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    BackstackManager.getInstance().navigateBack();
                }
            });
        }

        if(rescanButton != null) {
            rescanButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showProgressBar();
                    haloController.getStationList(loadStationList, failureListener);
                }
            });
        }

        if(stations != null) {
            stations.clear();
            stations = null;
        }

        haloController = new HaloController(
                DeviceModelProvider.instance().getModel(deviceAddress == null ? "DRIV:dev:" : deviceAddress),
                CorneaClientFactory.getClient(),
                UPDATE_ON
        );
        haloController.setCallback(this);

        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getTitle());
        }

    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.halo_post_pairing_radio_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_halo_station_selection;
    }

    @Override
    public void onError(Throwable throwable) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                hideProgressBar();
                setListViewEnabled(true);
            }
        });
    }

    @Override
    public void onSuccess(DeviceModel deviceModel) {
        model = deviceModel;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setListViewEnabled(false);
                if(stations == null) {
                    showProgressBar();
                    haloController.getStationList(loadStationList, failureListener);
                }
                else {
                    updateStationList();
                }
            }
        });
    }

    private boolean isEditMode () {
        return getArguments().getBoolean(EDIT_MODE);
    }

    //listener for the response of getStationList
    private final Listener<WeatherRadio.ScanStationsResponse> loadStationList = Listeners.runOnUiThread(new  Listener<WeatherRadio.ScanStationsResponse>() {
        @Override
        public void onEvent(WeatherRadio.ScanStationsResponse clientEvent) {
            stations = clientEvent.getStations();
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateStationList();
                }
            });
        }
    });

    private void updateStationList() {
        items.clear();
        boolean noAvailableStations = true;
        ArrayList<StationScanResult> list = new ArrayList<StationScanResult>();
        if(model == null || stations == null || stations.size() == 0) {
            noAvailableStations = true;
        } else {
            for (Map<String, Object> mapItem : stations) {
                StationScanResult result = new StationScanResult();

                result.setFreq((String) mapItem.get("freq"));

                Number id = (Number) mapItem.get("id");
                if (id != null) {
                    result.setId(id.doubleValue());
                }

                Number rssi = (Number) mapItem.get("rssi");
                if (rssi != null) {
                    if (rssi.doubleValue() > 0) {
                        noAvailableStations = false;
                    }
                    result.setRssi(rssi.doubleValue());
                }
                list.add(result);
            }
        }
        if (noAvailableStations) {
            title.setText(R.string.weather_radio_no_results_title);
            subtitle.setText(Html.fromHtml(String.format(getString(R.string.weather_radio_no_results), GlobalSetting.NOAA_WEATHER_RADIO_COVERAGE_URI.toString())));
            subtitle.setMovementMethod(LinkMovementMethod.getInstance());

            noStationsFoundButtons.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.GONE);
        }
        else {
            WeatherRadio radio = (WeatherRadio) model;
            Number numStation = (Number)model.get(WeatherRadio.ATTR_STATIONSELECTED);
            if(numStation == null) {
                selectedStation = 0;
            }
            else {
                selectedStation = numStation.intValue();
            }

            title.setText(R.string.more_stations_title);
            subtitle.setText(R.string.more_stations_description);

            noStationsFoundButtons.setVisibility(View.GONE);
            nextButton.setVisibility(isEditMode() ? View.GONE : View.VISIBLE);

            //sort on rssi
            Collections.sort(list, comparator);

            //move selected station to top
            for (int nStation = 0; nStation < list.size(); nStation++) {
                StationScanResult result = list.get(nStation);
                StationScanResult item = new StationScanResult();

                item.setPlaying(false);
                if (result.getId() == selectedStation) {
                    if(WeatherRadio.PLAYINGSTATE_PLAYING.equals(radio.getPlayingstate())) {
                        item.setPlaying(true);
                    }
                    selectedStation = (int)result.getId();
                }

                item.setTitle("Station " + (int) result.getId());
                item.setSubTitle(result.getFreq());
                item.setFreq(result.getFreq());
                item.setId(result.getId());
                items.add(item);
            }

            if(selectedStation == 0) {
                selectedStation = (int)items.get(0).getId();
                haloController.setSelectedStation(selectedStation);
            }

            updateListData();
            weatherStationListAdapter.setCallback(frag);
            stationList.setAdapter(weatherStationListAdapter);
        }

        hideProgressBar();
        setListViewEnabled(true);
    }

    private Listener<Throwable> failureListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            hideProgressBar();
            setListViewEnabled(true);
            if(stations != null) {
                stations.clear();
                stations = null;
            }
            updateStationList();
        }
    });

    //listener for click event on play/stop in the adapter
    @Override
    public void stationPlaybackChange(double id) {
        setListViewEnabled(false);
        showProgressBar();
        StationScanResult item = null;
        for(int index = 0; index < items.size(); index++) {
            if(items.get(index).getId() == id) {
                item = items.get(index);
                break;
            }
        }

        if(null == item) {
            return;
        }

        if(item.isPlaying()) {
            haloController.stopWeatherStation();
        }
        else {
            haloController.playWeatherStation((int)id, 10);
        }
        updateListData();
    }

    @Override
    public void stationSelectionChange(double id) {
        setListViewEnabled(false);
        showProgressBar();
        haloController.setSelectedStation((int) id);
    }

    private void updateListData() {
        if(weatherStationListAdapter == null) {
            weatherStationListAdapter = new WeatherStationListAdapter(getActivity(), items, selectedStation, isEditMode());
        }
        else {
            weatherStationListAdapter.setNotifyOnChange(false);
            weatherStationListAdapter.clear();
            weatherStationListAdapter.setSelectedItem(selectedStation);
            weatherStationListAdapter.addAll(items);
            weatherStationListAdapter.notifyDataSetChanged();
        }
    }

    private void setListViewEnabled(boolean allowUpdates) {
        if(weatherStationListAdapter != null) {
            weatherStationListAdapter.setEnabled(allowUpdates);
        }
    }

    private static final Comparator<StationScanResult> comparator = new Comparator<StationScanResult>() {
        @Override
        public int compare(StationScanResult lhs, StationScanResult rhs) {
            return (lhs.getRssi() > rhs.getRssi() ? -1 : (lhs.getRssi() ==rhs.getRssi() ? 0 : 1));
        }
    };
}
