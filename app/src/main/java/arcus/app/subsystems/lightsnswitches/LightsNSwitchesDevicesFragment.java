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
package arcus.app.subsystems.lightsnswitches;

import android.app.Activity;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import arcus.cornea.subsystem.lightsnswitches.LightsNSwitchesDevListController;
import arcus.cornea.subsystem.lightsnswitches.model.LightsNSwitchesDevice;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.subsystems.lightsnswitches.adapter.LightsNSwitchesDeviceControlAdapter;
import arcus.app.subsystems.lightsnswitches.adapter.LightsNSwitchesPreferenceDelegate;
import arcus.app.subsystems.lightsnswitches.model.EditModeChangeListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class LightsNSwitchesDevicesFragment extends BaseFragment implements LightsNSwitchesDevListController.Callback, EditModeChangeListener, IShowedFragment {

    private static Logger logger = LoggerFactory.getLogger(LightsNSwitchesDevicesFragment.class);

    private RecyclerView deviceList;
    private LightsNSwitchesDeviceControlAdapter deviceListAdapter;
    private RecyclerView.Adapter wrappedDeviceListAdapter;
    private LightsNSwitchesParentFragment parentFragment;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;

    public static LightsNSwitchesDevicesFragment newInstance() {
        return new LightsNSwitchesDevicesFragment();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        deviceList = (RecyclerView) view.findViewById(R.id.device_list);
        parentFragment = (LightsNSwitchesParentFragment) BackstackManager.getInstance().getFragmentOnStack(LightsNSwitchesParentFragment.class);
    }

    @Override
    public void onResume () {
        super.onResume();

        logger.debug("Requesting list of lights & switches devices...");
        LightsNSwitchesDevListController.instance().setCallback(this);
    }

    @Override
    public void onPause () {
        super.onPause();

        if (mRecyclerViewDragDropManager != null) {
            mRecyclerViewDragDropManager.cancelDrag();
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }

        if (deviceList != null) {
            deviceList.setItemAnimator(null);
            deviceList.setAdapter(null);
        }

        if (wrappedDeviceListAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedDeviceListAdapter);
            wrappedDeviceListAdapter = null;
        }

        deviceListAdapter = null;
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.lightsnswitches_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_lightsnswitches_devices;
    }

    @Override
    public void showDevices(List<LightsNSwitchesDevice> devices) {
        logger.debug("Received {} lights & switches devices.", devices.size());
        List<LightsNSwitchesDevice> userOrderedDeviceList = LightsNSwitchesPreferenceDelegate.loadLightsAndSwitchesDeviceOrder(devices);

        Activity activity = getActivity();
        if(activity == null){
            return;
        }

        deviceListAdapter = new LightsNSwitchesDeviceControlAdapter(activity, userOrderedDeviceList);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(activity);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();

        NinePatchDrawable ninePatchDrawable = (NinePatchDrawable) ContextCompat.getDrawable(activity, R.drawable.material_shadow_z3);
        if(ninePatchDrawable != null){
            mRecyclerViewDragDropManager.setDraggingItemShadowDrawable(ninePatchDrawable);
        }
        wrappedDeviceListAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(deviceListAdapter);

        deviceList.setLayoutManager(linearLayoutManager);
        deviceList.setAdapter(wrappedDeviceListAdapter);
        deviceList.setItemAnimator(new RefactoredDefaultItemAnimator());

        mRecyclerViewDragDropManager.attachRecyclerView(deviceList);
    }

    @Override
    public void onEditModeChanged(boolean isEditMode) {
        parentFragment.setSlidingTabLayoutVisibility(isEditMode ? View.GONE : View.VISIBLE);
        if(deviceListAdapter!=null){
            deviceListAdapter.setEditMode(isEditMode);
        }
    }

    @Override
    public void onShowedFragment() {
        parentFragment.setEditMenuVisible(true);
        parentFragment.setEditModeChangeListener(this);
    }

    @Override
    public void updateDevices(List<LightsNSwitchesDevice> devices) {
        for (LightsNSwitchesDevice thisDevice : devices) {
            int thisDevicePosition = getPositionOfDevice(thisDevice);

            if (thisDevicePosition >= 0) {
                deviceListAdapter.refreshItem(thisDevicePosition, thisDevice);
            } else {
                logger.error("Bug! Controller indicates state change to device not already in list.");
            }
        }
    }

    private int getPositionOfDevice (LightsNSwitchesDevice device) {
        if(deviceListAdapter == null) {
            return -1;
        }
        for (int position = 0; position < deviceListAdapter.getItemCount(); position++) {
            LightsNSwitchesDevice thisDevice = deviceListAdapter.getItem(position);

            if (thisDevice.getDeviceId().equals(device.getDeviceId())) {
                return position;
            }
        }

        return -1;
    }
}
