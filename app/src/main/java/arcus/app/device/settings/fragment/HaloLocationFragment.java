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
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import arcus.app.common.fragments.BaseFragment;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.SessionController;
import arcus.cornea.device.smokeandco.HaloController;
import arcus.cornea.provider.DeviceModelProvider;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.PlaceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.popups.LocationPopup;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HaloLocationFragment extends BaseFragment implements HaloController.Callback, LocationPopup.Callback, HaloController.LocationCallback {

    private static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String EDIT_MODE = "EDIT_MODE";
    private static final String COUNTY = "COUNTY";
    private static final String STATE = "STATE";

    private Version1TextView listTitle;
    private Version1TextView countyName;
    private Version1TextView stateName;
    private Version1Button nextButton;
    private Version1Button skipSetupButton;
    private View state;
    private View county;
    private HaloLocationFragment frag;
    private HaloController haloController;
    private ArrayList<String> stateList = new ArrayList<>();
    private ArrayList<String> countyList = new ArrayList<>();
    private String sameCode = "";
    private String stateCode = "";
    private String countyCode = "";
    private String deviceAddress = "";


    @NonNull
    public static HaloLocationFragment newInstance (String deviceAddress, boolean isEditMode) {
        HaloLocationFragment fragment = new HaloLocationFragment();

        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_ADDRESS, deviceAddress);
        bundle.putBoolean(EDIT_MODE, isEditMode);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        frag = this;
        View view = super.onCreateView(inflater, container, savedInstanceState);

        nextButton = (Version1Button) view.findViewById(R.id.next_button);
        skipSetupButton = (Version1Button) view.findViewById(R.id.skip_setup);

        listTitle = (Version1TextView) view.findViewById(R.id.list_title);

        final PlaceModel placeModel = SessionController.instance().getPlace();
        if (placeModel != null) {
            listTitle.setText(getString(R.string.halo_location_title));
        }

        countyName = (Version1TextView) view.findViewById(R.id.county_name);
        stateName = (Version1TextView) view.findViewById(R.id.state_name);
        state = view.findViewById(R.id.state_container);
        state.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LocationPopup popup = LocationPopup.newInstance(stateList,
                        getString(R.string.setting_state),
                        STATE);
                popup.setCallback(frag);
                BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getCanonicalName(), true);
            }
        });
        county = view.findViewById(R.id.county_container);
        county.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(stateCode.equals("") || countyList.size() == 0) {
                    return;
                }
                LocationPopup popup = LocationPopup.newInstance(countyList,
                        getString(R.string.counties),
                        COUNTY);
                popup.setCallback(frag);
                BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getCanonicalName(), true);
            }
        });

        listTitle.setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);
        countyName.setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);
        stateName.setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);
        ((Version1TextView)view.findViewById(R.id.list_subtitle)).setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);
        ((Version1TextView)view.findViewById(R.id.state_label)).setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);
        ((Version1TextView)view.findViewById(R.id.county_label)).setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);

        int chevronImage = isEditMode() ? R.drawable.chevron_white: R.drawable.chevron;
        ((ImageView)view.findViewById(R.id.state_chevron)).setImageResource(chevronImage);
        ((ImageView)view.findViewById(R.id.county_chevron)).setImageResource(chevronImage);

        int dividerColor = isEditMode() ? R.color.white_with_10: R.color.black_with_10;
        view.findViewById(R.id.divider0).setBackgroundColor(ContextCompat.getColor(getActivity(), dividerColor));
        view.findViewById(R.id.divider1).setBackgroundColor(ContextCompat.getColor(getActivity(), dividerColor));
        view.findViewById(R.id.divider2).setBackgroundColor(ContextCompat.getColor(getActivity(), dividerColor));

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();

        nextButton.setVisibility(isEditMode() ? View.GONE : View.VISIBLE);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(countyName.getText().equals(R.string.county) || stateName.getText().equals(R.string.state) || sameCode.equals("")) {
                    AlertPopup alertPopup = AlertPopup.newInstance(
                            getString(R.string.weather_radio_location_selection_warning_title),
                            getString(R.string.weather_radio_location_selection_warning),
                            null,
                            null,
                            new AlertPopup.AlertButtonCallback() {
                                @Override public boolean topAlertButtonClicked() { return false; }
                                @Override public boolean bottomAlertButtonClicked() { return false; }
                                @Override public boolean errorButtonClicked() { return false; }
                                @Override public void close() {
                                    BackstackManager.getInstance().navigateBack();
                                }
                            }
                    );
                    alertPopup.setCloseButtonVisible(true);
                    BackstackManager.getInstance().navigateToFloatingFragment(alertPopup, alertPopup.getClass().getCanonicalName(), true);
                    return;
                }
            }
        });

        skipSetupButton.setVisibility(isEditMode() ? View.GONE : View.VISIBLE);
        deviceAddress = getArguments().getString(DEVICE_ADDRESS);

        haloController = new HaloController(
                DeviceModelProvider.instance().getModel(deviceAddress == null ? "DRIV:dev:" : deviceAddress),
                CorneaClientFactory.getClient(),
                null
        );
        haloController.setCallback(this);
        haloController.setLocationCallback(this);

        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getTitle());
        }
    }

    @Override
    public void onDestroyView() {
        hideProgressBar();
        super.onDestroyView();
    }

    @NonNull
    @Override
    public String getTitle() {
        if(isEditMode()) {
            return getString(R.string.location_information_setting).toUpperCase();
        }
        return getString(R.string.halo_post_pairing_location_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_halo_location;
    }

    private boolean isEditMode() {
        return getArguments().getBoolean(EDIT_MODE);
    }

    @Override
    public void selectedItem(String item, String type) {
        if(type.equals(STATE)) {
            stateName.setText(item);
            stateCode = item;
            showProgressBar();
            haloController.getCountyNames(stateCode);
        }
        else if (type.equals(COUNTY)) {
            countyCode = item;
            countyName.setText(item);
            showProgressBar();
            haloController.getSameCode(stateCode, item);
        }
    }

    @Override
    public void onError(Throwable throwable) {

    }
    @Override
    public void onSuccess(DeviceModel deviceModel) {
        hideProgressBar();
        if(stateList == null || stateList.size() == 0 || stateCode.equals("")) {
            haloController.getStateNames();
        }
        else {
            stateName.setText(stateCode);
            if(!countyCode.equals("")) {
                countyName.setText(countyCode);
            }
        }
    }

    @Override
    public void onStateListLoaded(List<Map<String, Object>> stateListWithCodes) {
        hideProgressBar();
        this.stateList.clear();
        //if the place has a state, select it and load counties
        PlaceModel placeModel = SessionController.instance().getPlace();
        String state = "";
        if (placeModel != null) {
            state = placeModel.getState();
        }
        for(Map<String, Object> item : stateListWithCodes) {
            stateList.add((String)item.get("stateCode"));
        }

        if(stateList.contains(state)) {
            stateName.setText(state);
            stateCode = state;
            haloController.getCountyNames(stateCode);
        }
    }

    @Override
    public void onCountyListLoaded(List<String> countyList) {
        hideProgressBar();
        this.countyList = new ArrayList<>(countyList);

        //only try to update the county when pairing
        if(!isEditMode()) {
            PlaceModel placeModel = SessionController.instance().getPlace();
            if (placeModel != null) {
                String county = placeModel.getAddrCounty();
                if(this.countyList.contains(county)) {
                    countyCode = county;
                    countyName.setText(county);
                    showProgressBar();
                    haloController.getSameCode(stateCode, county);
                }
            }
        }
    }

    @Override
    public void onSAMECodeLoaded(String sameCode) {
        hideProgressBar();
        this.sameCode = sameCode;
        haloController.setLocation(sameCode);
    }
}
