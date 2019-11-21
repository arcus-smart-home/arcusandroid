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

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.NumberPicker;

import arcus.cornea.device.camera.model.AvailableNetworkModel;
import arcus.app.R;
import arcus.app.common.adapters.WiFiNetworkListItemAdapter;
import arcus.app.common.view.Version1TextView;

import java.util.ArrayList;
import java.util.List;

public class WiFiPickerPopup extends ArcusFloatingFragment {
    private static final String NETWORKS = "Networks";
    private static final String LOADING = "LOADING";
    private static final String MANUAL_SSID = "MANUAL_SSID";

    private int titleStringResId = R.string.setting_wifi_network_name;
    private ListView picker;
    private LinearLayout manualSsidRegion;
    private Version1TextView enterManualSsid;
    private WiFiNetworkListItemAdapter adapter;
    private Callback callback;
    private boolean isLoading = false;

    @NonNull
    public static WiFiPickerPopup newInstance() {
        return new WiFiPickerPopup();
    }

    @NonNull
    public static WiFiPickerPopup newInstance(ArrayList<AvailableNetworkModel> wifiNetworks, Boolean loading, Boolean showManualSsidEntry) {
        WiFiPickerPopup instance = new WiFiPickerPopup();
        Bundle bundle = new Bundle();
        bundle.putBoolean(LOADING, loading);
        bundle.putParcelableArrayList(NETWORKS, wifiNetworks);
        bundle.putBoolean(MANUAL_SSID, showManualSsidEntry);
        instance.setArguments(bundle);
        return instance;
    }

    public void setTitleStringResId(int titleStringResId) {
        this.titleStringResId = titleStringResId;
    }

    @Override
    public void setFloatingTitle() {
        title.setText(getResources().getString(titleStringResId));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        closeBtn.setOnClickListener(this);

        manualSsidRegion = (LinearLayout) view.findViewById(R.id.ssid_info_region);
        enterManualSsid = (Version1TextView) view.findViewById(R.id.enter_ssid_button);

        enterManualSsid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callback.onEnterSsid();
            }
        });

        if (getArguments().getBoolean(MANUAL_SSID, false)) {
            manualSsidRegion.setVisibility(View.VISIBLE);
        }

        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP) {
                    doClose();
                    return true;
                } else {
                    return false;
                }
            }
        });

        return view;
    }

    @Override
    public void doContentSection() {

        Bundle arguments = getArguments();
        ArrayList<AvailableNetworkModel> networks = new ArrayList<>();

        if (arguments != null) {
            this.isLoading = arguments.getBoolean(LOADING, false);

            if (arguments.getParcelableArrayList(NETWORKS) != null) {
                networks = arguments.getParcelableArrayList(NETWORKS);
            }
        }

        picker = (ListView) contentView.findViewById(R.id.floating_network_picker);
        picker.setVisibility(View.GONE);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        adapter = new WiFiNetworkListItemAdapter(getActivity(), networks, isLoading);

        picker.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (callback != null) {
                    if(adapter.getItem(position) != null) {
                        callback.selectedItem(adapter.getItem(position));
                    }
                }

            }
        });
        picker.setAdapter(adapter);
        picker.setVisibility(View.VISIBLE);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.floating_list_picker_fragment;
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_network_picker;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void onClick(@NonNull View v) {
        final int id = v.getId();
        switch (id){
            case R.id.fragment_arcus_pop_up_close_btn:
                doClose();
                break;
        }
    }

    @Override
    public void doClose() {
        if (callback != null) {
            callback.close();
        }
    }

    public void showWifiNetworks(@NonNull List<AvailableNetworkModel> availableNetworkModels) {
        adapter.setLoading(false);
        adapter.setNetworks(availableNetworkModels);
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public interface Callback {
        void selectedItem(AvailableNetworkModel model);
        void onEnterSsid();
        void close();
    }
}
