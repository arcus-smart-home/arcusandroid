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

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Hub;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.events.FirmwareEvent;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.popups.InfoTextPopup;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.NoSwipeViewPager;
import arcus.app.common.view.SlidingTabLayout;
import arcus.app.device.DeviceMoreFragment;
import arcus.app.device.adapter.DeviceDetailViewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import de.greenrobot.event.EventBus;


public class DeviceDetailParentFragment extends BaseFragment implements DeviceDetailFragment.DeviceDetailCallback, SlidingTabLayout.TabClickCallback {
    private static final String SELECTED_POSITION = "selected_position";
    private static final String DEVICE_TYPE = "DEVICE_TYPE";

    private int mCurrentSelectedPosition = 0;

    private NoSwipeViewPager viewPager;
    private DeviceDetailViewPagerAdapter adapter;
    private SlidingTabLayout slidingTabLayout;

    private int mSelectedPosition = 0;

    @NonNull
    public static DeviceDetailParentFragment newInstance(String deviceAddress) {
        int position = SessionModelManager.instance().indexOf(CorneaUtils.getIdFromAddress(deviceAddress), true);
        return newInstance(position < 0 ? 0 : position);
    }

    @NonNull
    public static DeviceDetailParentFragment newInstance(int position){
        DeviceDetailParentFragment fragment = new DeviceDetailParentFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(SELECTED_POSITION, position);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION);
        }

        final Bundle arguments = getArguments();
        if(arguments !=null){
            mSelectedPosition = arguments.getInt(SELECTED_POSITION,0);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);

        viewPager = (NoSwipeViewPager) view.findViewById(R.id.fragment_device_detail_parent_viewpager);
        slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.fragment_device_detail_parent_sliding_tabs);
        slidingTabLayout.setTabClickCallback(this);

        populate();

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

        viewPager.setOffscreenPageLimit(1);

        slidingTabLayout.setDistributeEvenly(false);

        LayerDrawable layers = (LayerDrawable) slidingTabLayout.getBackground();

        GradientDrawable overlay = (GradientDrawable) (layers.findDrawableByLayerId(R.id.overlay));
        overlay.setColor(getResources().getColor(R.color.overlay_white_with_20));

        GradientDrawable background = (GradientDrawable) (layers.findDrawableByLayerId(R.id.background));
        background.setColor(getResources().getColor(android.R.color.transparent));


        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(android.R.color.transparent));
        slidingTabLayout.setViewPager(viewPager);

        slidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentSelectedPosition = position;
                updatePageUI(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {


            }
        });

        viewPager.post(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(mCurrentSelectedPosition, false);
                if (mCurrentSelectedPosition == 0) {
                    updatePageUI(mCurrentSelectedPosition);
                }
            }
        });

        return view;
    }

    public void onEvent(FirmwareEvent event) {
        if (slidingTabLayout != null) {
            slidingTabLayout.enableMoreTab(!(event.isCamera() && event.isUpdating()));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        updateBackground(true);
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    private void populate(){
        if(adapter == null) {
            adapter = new DeviceDetailViewPagerAdapter(getActivity(), getChildFragmentManager(), getFragments());
        }
        adapter.notifyDataSetChanged();
        viewPager.setAdapter(adapter);
    }

    public void updatePageUI(int position){
        final BaseFragment fragment = (BaseFragment) adapter.getFragment(position);

        updatePageContents(fragment);

        if (fragment instanceof IShowedFragment) {
            ((IShowedFragment) fragment).onShowedFragment();
        }
    }

    private void updatePageContents(BaseFragment fragment) {
        int color = -1;
        if (fragment instanceof DeviceMoreFragment) {
            moveViewBelow(R.id.banner_placeholder);
            DeviceDetailFragment detailsFragment = (DeviceDetailFragment) adapter.getFragment(0);
            if (detailsFragment != null) {
                String device = detailsFragment.getCurrentDeviceId();
                ((DeviceMoreFragment) fragment).setDeviceId(device);
                DeviceModel model = detailsFragment.getCurrentDeviceModel();
                if(model != null) {
                    if(model instanceof HubModel) {
                        fragment.updateBackground(!Hub.STATE_DOWN.equals(model.get(Hub.ATTR_STATE)));
                    }
                    else {
                        DeviceConnection connection = (DeviceConnection) model;
                        fragment.updateBackground(!DeviceConnection.STATE_OFFLINE.equals(connection.getState()));
                    }

                }
            }
            if(fragment != null) {
                color = fragment.getColorFilterValue();
            }
        } else {
            moveViewBelow(0);
            if(fragment != null) {
                color = fragment.getColorFilterValue();
            }
        }

        LayerDrawable layers = (LayerDrawable) slidingTabLayout.getBackground();
        GradientDrawable background = (GradientDrawable) (layers.findDrawableByLayerId(R.id.background));
        if(color == -1) {
            if(getContext() != null) {
                background.setColor(ContextCompat.getColor(getContext(), android.R.color.transparent));
            }
        }
        else {
            background.setColor(color);
        }
    }

    private void moveViewBelow(int anchor) {
        RelativeLayout.LayoutParams lp;
        lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.addRule(RelativeLayout.BELOW, anchor);
        viewPager.setLayoutParams(lp);
    }

    @NonNull
    private List<Fragment> getFragments() {
        List<Fragment> fList = new ArrayList<>();
        fList.add(DeviceDetailFragment.newInstance(mSelectedPosition, this));
        fList.add(DeviceMoreFragment.newInstance());
        return fList;
    }

    public void displayFirmwareUpdatingPopup () {
        ArcusFloatingFragment popup;
        popup = InfoTextPopup.newInstance(R.string.ota_firmware_update_popup_text, R.string.ota_firmware_update_popup_title);
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_device_detail_parent;
    }

    @Override
    public Integer getMenuId() {
        return R.menu.menu_device_detail;
    }

    @Override
    public void update() {
        final BaseFragment fragment = (BaseFragment) adapter.getFragment(mCurrentSelectedPosition);
        updatePageContents(fragment);
    }

    @Override
    public boolean enableTabClick(int position) {
        DeviceDetailFragment detailsFragment = (DeviceDetailFragment) adapter.getFragment(0);
        DeviceModel deviceModel = detailsFragment.getCurrentDeviceModel();

        if (deviceModel != null && detailsFragment.isUpgradingFirmware() && position == 1) {
            displayFirmwareUpdatingPopup();
            return false;
        }
        else {
            return true;
        }

    }
}
