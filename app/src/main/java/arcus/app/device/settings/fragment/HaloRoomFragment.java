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
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import arcus.app.common.fragments.BaseFragment;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.device.smokeandco.HaloController;
import arcus.cornea.provider.DeviceModelProvider;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.adapters.CheckableListAdapter;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;

import java.util.List;

public class HaloRoomFragment extends BaseFragment implements HaloController.Callback, CheckableListAdapter.AdapterCallback {

    private static final String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String EDIT_MODE = "EDIT_MODE";

    private ListView list;
    private Version1TextView listTitle;
    private Version1TextView listSubTitle;
    private Version1Button nextButton;
    HaloController haloController;
    HaloRoomFragment frag;
    CheckableListAdapter adapter;
    private String[] roomTypes;
    private String selection;
    private List<String> rooms;

    @NonNull
    public static HaloRoomFragment newInstance (String deviceAddress, boolean isEditMode) {
        HaloRoomFragment fragment = new HaloRoomFragment();

        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_ADDRESS, deviceAddress);
        bundle.putBoolean(EDIT_MODE, isEditMode);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        list = (ListView) view.findViewById(R.id.list);
        nextButton = (Version1Button) view.findViewById(R.id.next_button);
        nextButton.setVisibility(isEditMode() ? View.GONE : View.VISIBLE);

        listTitle = (Version1TextView) view.findViewById(R.id.list_title);
        listTitle.setText(getString(R.string.halo_room_title));

        listSubTitle = (Version1TextView) view.findViewById(R.id.list_subtitle);
        listSubTitle.setText(getString(R.string.halo_room_subtitle));
        listSubTitle.setVisibility(View.VISIBLE);

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();

        frag = this;

        nextButton.setColorScheme(isEditMode() ? Version1ButtonColor.WHITE : Version1ButtonColor.BLACK);
        listTitle.setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);
        listSubTitle.setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);
        nextButton.setText(isEditMode() ? getString(R.string.save_text) : getString(R.string.pairing_next));
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(adapter.getSelectedItem() == -1) {
                    AlertPopup alertPopup = AlertPopup.newInstance(
                            getString(R.string.halo_post_pairing_room_error_title),
                            getString(R.string.halo_post_pairing_room_error_body),
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

        String deviceAddress = getArguments().getString(DEVICE_ADDRESS);

        haloController = new HaloController(
                DeviceModelProvider.instance().getModel(deviceAddress == null ? "DRIV:dev:" : deviceAddress),
                CorneaClientFactory.getClient(),
                null
        );
        haloController.setCallback(this);

        Activity activity = getActivity();
        if (activity != null) {
            activity.setTitle(getTitle());
        }

    }

    @NonNull
    @Override
    public String getTitle() {
        return getString(R.string.halo_post_pairing_info_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_checkable_list;
    }

    private boolean isEditMode() {
        return getArguments().getBoolean(EDIT_MODE);
    }

    @Override
    public void onError(Throwable throwable) {

    }

    @Override
    public void onSuccess(DeviceModel deviceModel) {
        rooms = haloController.getRoomTypes();

        roomTypes = rooms.toArray(new String[rooms.size()]);

        for(int nRoom = 0; nRoom < roomTypes.length; nRoom++) {
            roomTypes[nRoom] = roomTypes[nRoom].toUpperCase();
        }

        int defaultSelection = -1;
        if(isEditMode()) {
            selection = haloController.getSelectedRoomType();
        }
        if(selection != null) {
            defaultSelection = rooms.indexOf(selection);
        }
        adapter = new CheckableListAdapter(getActivity(), R.layout.cell_checkable_item, roomTypes, defaultSelection, this);
        if(isEditMode()) {
            adapter.setLightColorScheme(false);
        }

        if(isEditMode()) {
            list.setDivider(new ColorDrawable(getResources().getColor(R.color.white_with_30)));
            list.setDividerHeight(1);
        }
        list.setAdapter(adapter);
    }

    @Override
    public void onSelectionChanged() {
        if(rooms != null) {
            selection = rooms.get(adapter.getSelectedItem());
            haloController.setRoomType(selection);
        }
    }
}
