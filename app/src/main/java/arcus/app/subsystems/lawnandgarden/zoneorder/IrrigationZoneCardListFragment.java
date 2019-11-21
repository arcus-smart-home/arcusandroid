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
package arcus.app.subsystems.lawnandgarden.zoneorder;

import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import arcus.cornea.utils.CapabilityUtils;
import com.iris.client.capability.IrrigationZone;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.lawnandgarden.models.IrrigationZoneInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IrrigationZoneCardListFragment extends BaseFragment {
    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String SELECTED_ZONES = "SELECTED_ZONES";
    private final static String SCHEDULE_EDITMODE = "SCHEDULE_EDIT_MODE";
    private boolean isEditMode = false;

    private IrrigationZoneCardListAdapter mAdapter;
    private IrrigationZoneCardListFragment.ZoneInfoCallback callback;
    private View titleArea;

    public interface ZoneInfoCallback {
        void setSelectedZones(List<IrrigationZoneListItemModel> items);
    }

    public void setCallback(IrrigationZoneCardListFragment.ZoneInfoCallback callback) {
        this.callback = callback;
    }
    @NonNull
    public static IrrigationZoneCardListFragment newInstance (String deviceAddress, LinkedHashMap<String, Integer> zones, boolean scheduleEditMode) {
        IrrigationZoneCardListFragment instance = new IrrigationZoneCardListFragment();
        Bundle arguments = new Bundle();

        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putSerializable(SELECTED_ZONES, zones);
        arguments.putBoolean(SCHEDULE_EDITMODE, scheduleEditMode);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        boolean scheduleEditMode = getArguments().getBoolean(SCHEDULE_EDITMODE);
        String deviceAddress = getArguments().getString(DEVICE_ADDRESS);
        LinkedHashMap<String, Integer> selectedZones = (LinkedHashMap<String, Integer>) getArguments().getSerializable(SELECTED_ZONES);
        String deviceID = CorneaUtils.getIdFromAddress(deviceAddress);
        DeviceModel deviceModel = getCorneaService().getStore(DeviceModel.class).get(deviceID);
        CapabilityUtils capabilityUtils = new CapabilityUtils(deviceModel);

        ArrayList<IrrigationZoneInfo> allZones = new ArrayList<>();
        ArrayList<IrrigationZoneInfo> zonesNotSelected = new ArrayList<>();
        ArrayList<IrrigationZoneInfo> zonesSelected = new ArrayList<>();
        if (capabilityUtils != null) {
            for (String instance : capabilityUtils.getInstanceNames()) {
                IrrigationZoneInfo zone = new IrrigationZoneInfo();
                try {
                    String name = (String) capabilityUtils.getInstanceValue(instance, IrrigationZone.ATTR_ZONENAME);
                    zone.setDisplayName(name);
                    Double number = (Double) capabilityUtils.getInstanceValue(instance, IrrigationZone.ATTR_ZONENUM);
                    int zoneNum = 1;
                    if (number != null) {
                        zoneNum = number.intValue();
                    }
                    zone.setZoneNumber(zoneNum);
                    double duration = (Double) capabilityUtils.getInstanceValue(instance, IrrigationZone.ATTR_DEFAULTDURATION);
                    if(duration < 1) {
                        duration = 1;
                    }
                    zone.setDuration((int) duration);
                } catch (Exception e) {
                    zone.setDuration(1);
                }
                zone.setZoneId(instance);
                zone.setZoneDisplay("Zone " + instance.substring(1, instance.length()));
                if(selectedZones.get(instance) != null) {
                    zone.setVisible(true);
                    zone.setDuration(selectedZones.get(instance));
                    zonesSelected.add(zone);
                }
                if(selectedZones.get(instance) == null) {
                    zone.setVisible(false);
                    zonesNotSelected.add(zone);
                }
            }
            Collections.sort(zonesNotSelected);
            for(Map.Entry<String, Integer> entry : selectedZones.entrySet()) {
                String key = entry.getKey();
                for (IrrigationZoneInfo zone : zonesSelected) {
                    if(zone.getZoneId().equals(key)) {
                        allZones.add(zone);
                    }
                }
            }
            allZones.addAll(zonesNotSelected);
        }

        RecyclerView mRecyclerView = (RecyclerView) getView().findViewById(R.id.service_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        titleArea = getView().findViewById(R.id.irrigation_zone_info);
        if(!scheduleEditMode) {
            ((Version1TextView) getView().findViewById(R.id.irrigation_title)).setTextColor(getResources().getColor(R.color.black));
            ((Version1TextView) getView().findViewById(R.id.irrigation_title_description)).setTextColor(getResources().getColor(R.color.black));
            getView().findViewById(R.id.divider_parent).setBackgroundColor(getResources().getColor(R.color.black_with_10));
        }

        RecyclerViewDragDropManager mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        mRecyclerViewDragDropManager.setDraggingItemShadowDrawable((NinePatchDrawable) ContextCompat.getDrawable(getActivity(), R.drawable.material_shadow_z3));

        mAdapter = new IrrigationZoneCardListAdapter(getActivity(), new IrrigationZoneListDataProvider(getActivity(), allZones), deviceAddress, scheduleEditMode);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mRecyclerViewDragDropManager.createWrappedAdapter(mAdapter));

        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        isEditMode = !isEditMode;
        mAdapter.setIsEditable(isEditMode);

        if (isEditMode) {
            item.setTitle(getString(R.string.card_menu_done));
            mAdapter.showSelectedItemsOnly(false);
            mAdapter.setVisibleItemsChecked();
            mAdapter.notifyDataSetChanged();
            titleArea.setVisibility(View.GONE);
        } else {
            item.setTitle(getString(R.string.card_menu_edit));
            mAdapter.showSelectedItemsOnly(true);
            mAdapter.notifyDataSetChanged();
            titleArea.setVisibility(View.VISIBLE);
        }

        return true;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if(menu.size()>0){
            if (isEditMode) {
                menu.getItem(0).setTitle(getString(R.string.card_menu_done));
            } else {
                menu.getItem(0).setTitle(getString(R.string.card_menu_edit));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public Integer getMenuId() {
        return R.menu.menu_edit_done_toggle;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getResources().getString(R.string.irrigation_zones);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_irrigation_zone_card_list;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if(callback != null) {
            List<IrrigationZoneListItemModel> selectedItems = mAdapter.getOrderedVisibleItems();
            callback.setSelectedZones(selectedItems);
        }
    }
}
