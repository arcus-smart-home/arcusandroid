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
package arcus.app.device.details;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.utils.Listeners;
import com.iris.capability.util.Addresses;
import com.iris.client.capability.Device;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.ModelEvent;
import arcus.app.R;
import arcus.app.common.events.FirmwareEvent;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.adapter.DeviceDetailPagerAdapter;
import arcus.app.common.events.ArcusEvent;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.dashboard.HomeFragment;
import arcus.app.common.models.SessionModelManager;

import java.util.List;

import de.greenrobot.event.EventBus;


public class DeviceDetailFragment extends BaseFragment implements SessionModelManager.SessionModelChangeListener, IShowedFragment, BaseFragment.BaseFragmentInterface  {

    private static final String SELECTED_POSITION = "selected_position";

    private int mCurrentSelectedPosition = 0;
    private int mLastSelectedPosition = 0;

    private ViewPager viewPager;
    private DeviceDetailCallback callback;
    @Nullable
    private DeviceDetailPagerAdapter adapter;

    private ListenerRegistration modelListener;
    private List<DeviceModel> devices;
    private final ViewPager.OnPageChangeListener onPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageSelected(int position) {
            EventBus.getDefault().postSticky(new FirmwareEvent(isCamera(), isUpgradingFirmware(), getDeviceAddress()));
            if(callback != null) {
                callback.update();
            }
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    @Override
    public void backgroundUpdated() {
        if(callback != null) {
            callback.update();
        }
    }

    public interface DeviceDetailCallback {
        void update();
    }

    public void setCallback(DeviceDetailCallback callback) {
        this.callback = callback;
    }

    @NonNull
    public static DeviceDetailFragment newInstance(int position, DeviceDetailCallback callback) {
        DeviceDetailFragment fragment = new DeviceDetailFragment();
        fragment.setCallback(callback);
        Bundle bundle = new Bundle();
        bundle.putInt(SELECTED_POSITION, position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            mCurrentSelectedPosition = arguments.getInt(SELECTED_POSITION, 0);
        }

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION);
        }

        mLastSelectedPosition = mCurrentSelectedPosition;
    }

    @Override
    public void onResume() {
        super.onResume();
        addModelListener();
        if (SessionModelManager.instance().isListenerRegistered(this))
            addListener();

        viewPager.setCurrentItem(mCurrentSelectedPosition, false);
        viewPager.post(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(mCurrentSelectedPosition, false);
                adapter.updatePageUI(mCurrentSelectedPosition, mLastSelectedPosition);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Listeners.clear(modelListener);
        if (SessionModelManager.instance().isListenerRegistered(this))
            removeListener();
        // We need to make sure the listener gets deregistered, not sure this will cover all scenarios.
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(viewPager!=null){
            outState.putInt(SELECTED_POSITION, viewPager.getCurrentItem());
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);
        viewPager = (ViewPager) (view != null ? view.findViewById(R.id.fragment_device_detail_child_view_pager) : null);
        if (viewPager != null) {
            viewPager.setOffscreenPageLimit(1);
        }
        populate();

        viewPager.removeOnPageChangeListener(onPageChangeListener);
        viewPager.addOnPageChangeListener(onPageChangeListener);
        return view;
    }


    @Override
    public void onPause() {
        mLastSelectedPosition = mCurrentSelectedPosition;
        mCurrentSelectedPosition = viewPager.getCurrentItem();
        super.onPause();
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_device_detail;
    }


    private void populate() {
        if (adapter == null) {
            devices = SessionModelManager.instance().getDevicesWithHub();
            adapter = new DeviceDetailPagerAdapter(this, getChildFragmentManager(), getActivity(), devices);
        }
        adapter.setUpPageListener(viewPager);
        viewPager.setAdapter(adapter);
    }

    /*
     * Get current device model for the selected pager fragment
     */
    public String getCurrentDeviceId() {
        if (devices != null && viewPager.getCurrentItem() < devices.size()) {
            return devices.get(viewPager.getCurrentItem()).getId();
        }

        return "";
    }

    public DeviceModel getCurrentDeviceModel() {
        if (devices != null) {
            return devices.get(viewPager.getCurrentItem());
        }

        return null;
    }

    public boolean isUpgradingFirmware() {
        try {
            return CorneaUtils.firmwareIsUpdating(getDeviceAddress());
        } catch (Exception ex) {
            return false;
        }
    }

    public boolean isCamera() {
        try {
            return CorneaUtils.isCamera(getDeviceAddress());
        } catch (Exception ex) {
            return false;
        }
    }

    private String getDeviceAddress() {
        return Addresses.toObjectAddress(Device.NAMESPACE, getCurrentDeviceId());
    }

    private void addModelListener() {
        if (modelListener == null || !modelListener.isRegistered()) {

            modelListener = getCorneaService().getStore(DeviceModel.class).addListener(new Listener<ModelEvent>() {
                @Override
                public void onEvent(@NonNull final ModelEvent modelEvent) {
                    if (modelEvent instanceof ModelDeletedEvent) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                DeviceModel modelNeedToDelete = (DeviceModel) modelEvent.getModel();
                                if(adapter != null){
                                    adapter.removeDevice(modelNeedToDelete);
                                    adapter.updatePageUI(viewPager.getCurrentItem(), mLastSelectedPosition);
                                }
                                if (SessionModelManager.instance().deviceCount(true) < 1) {
                                    BackstackManager.getInstance().navigateBackToFragment(HomeFragment.class);
                                } else {
                                    List<DeviceModel> devicesWithHub = SessionModelManager.instance().getDevicesWithHub();
                                    if (devicesWithHub != null && adapter != null && devicesWithHub.size() < viewPager.getCurrentItem()) {
                                        DeviceModel deviceModel = devicesWithHub.get(viewPager.getCurrentItem() >= devicesWithHub.size() ? devicesWithHub.size() -1 : viewPager.getCurrentItem());
                                        String deviceID = deviceModel.getId();
                                        EventBus.getDefault().post(new ArcusEvent(deviceID));
                                    }
                                }
                            }
                        });
                    }
                }
            });
        }
    }

    // SessionModelManager Listeners
    private void addListener() {
        SessionModelManager.instance().addSessionModelChangeListener(this);
    }

    private void removeListener() {
        SessionModelManager.instance().removeSessionModelChangeListener(this);
    }

    @Override
    public void onSessionModelChangeEvent(SessionModelManager.SessionModelChangedEvent event) {
        assert adapter != null;
        adapter.notifyDataSetChanged();
        adapter.updatePageUI(viewPager.getCurrentItem(), mLastSelectedPosition);
    }

    @Override
    public void onShowedFragment() {
        assert adapter != null;
        Fragment fragment = adapter.getFragment(adapter.getCurrentSelectedPosition());
        if (fragment instanceof IShowedFragment) {
            ((IShowedFragment) fragment).onShowedFragment();
            if(callback != null) {
                callback.update();
            }
        }
    }

    public int getColorFilterValue() {
        Fragment fragment = adapter.getFragment(adapter.getCurrentSelectedPosition());
        if (fragment instanceof BaseFragment) {
            return ((BaseFragment) fragment).getColorFilterValue();
        }
        return -1;
    }
}
