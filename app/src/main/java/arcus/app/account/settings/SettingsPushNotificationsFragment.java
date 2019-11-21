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
package arcus.app.account.settings;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.iris.client.model.MobileDeviceModel;
import arcus.app.R;
import arcus.app.account.settings.controller.SettingsPushNotificationsFragmentController;
import arcus.app.account.settings.adapter.MobileDeviceListAdapter;
import arcus.app.activities.FullscreenFragmentActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.OtherErrorTypes;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.popups.InfoTextPopup;
import arcus.app.common.view.Version1TextView;

import java.util.List;


public class SettingsPushNotificationsFragment extends BaseFragment implements SettingsPushNotificationsFragmentController.Callbacks, MobileDeviceListAdapter.OnDeleteListener {

    private ListView mobileDeviceList;
    private LinearLayout otherDevicesSection;
    private LinearLayout currentDeviceSection;
    private Version1TextView currentDeviceName;
    private Version1TextView currentDeviceType;
    View mobileInfoContainer;

    private boolean isEditMode = false;
    private boolean hasOtherDevices = false;
    private MobileDeviceListAdapter mobileDeviceListAdapter;

    public static SettingsPushNotificationsFragment newInstance () {
        return new SettingsPushNotificationsFragment();
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        mobileDeviceList = (ListView) view.findViewById(R.id.mobile_device_list);
        otherDevicesSection = (LinearLayout) view.findViewById(R.id.other_devices_section);
        currentDeviceSection = (LinearLayout) view.findViewById(R.id.current_device_section);
        currentDeviceName = (Version1TextView) view.findViewById(R.id.device_name);
        currentDeviceType = (Version1TextView) view.findViewById(R.id.device_type);
        mobileInfoContainer = view.findViewById(R.id.mobile_notifications_info_container);

        mobileDeviceListAdapter = new MobileDeviceListAdapter(getActivity());
        mobileDeviceListAdapter.setOnDeleteListener(this);

        return view;
    }

    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();

        mobileInfoContainer.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                InfoTextPopup popup = InfoTextPopup.newInstance(R.string.mobile_disable_push, R.string.more_info_text);
                BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
            }
        });

        mobileDeviceList.setAdapter(mobileDeviceListAdapter);

        showProgressBar();
        SettingsPushNotificationsFragmentController.getInstance().setListener(this);
        SettingsPushNotificationsFragmentController.getInstance().loadMobileDevices(getActivity());
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        isEditMode = !isEditMode;
        mobileDeviceListAdapter.setEditEnabled(isEditMode);

        if (isEditMode) {
            item.setTitle(getString(R.string.card_menu_done));
        } else {
            item.setTitle(getString(R.string.card_menu_edit));
        }

        return true;
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (isEditMode) {
            menu.getItem(0).setTitle(getString(R.string.card_menu_done));
        } else {
            menu.getItem(0).setTitle(getString(R.string.card_menu_edit));
        }
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.push_notifications);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_settings_push_notifications;
    }

    @Override
    public Integer getMenuId() {
        return hasOtherDevices ? R.menu.menu_edit_done_toggle : null;
    }

    @Override
    public void onMobileDevicesLoaded(MobileDeviceModel currentDevice, List<MobileDeviceModel> otherDevices) {
        hideProgressBar();

        hasOtherDevices = otherDevices != null && otherDevices.size() > 0;
        otherDevicesSection.setVisibility(hasOtherDevices ? View.VISIBLE : View.GONE);

        boolean hasCurrentDevice = currentDevice != null;
        currentDeviceSection.setVisibility(hasCurrentDevice ? View.VISIBLE : View.GONE);

        if (hasCurrentDevice) {
            currentDeviceName.setText(getDeviceName(currentDevice));
            currentDeviceType.setText(getString(R.string.device_type, String.valueOf(currentDevice.getDeviceModel())));
        }

        if (hasOtherDevices) {
            mobileDeviceListAdapter.clear();
            mobileDeviceListAdapter.addAll(otherDevices);
            mobileDeviceListAdapter.notifyDataSetInvalidated();
        }

        // Special case: User has nada devices receiving push notifications
        if (!hasCurrentDevice && !hasOtherDevices) {
            BackstackManager.getInstance().navigateBack();
            FullscreenFragmentActivity.launch(getActivity(), SettingsTurnOnNotificationsFragment.class);
        }

        // Update the edit/done menu visibility on the presence of other devices
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onDeviceRemoved(MobileDeviceModel removedDevice) {
        hideProgressBar();
        ErrorManager.in(getActivity()).show(OtherErrorTypes.PUSH_NOTIFICATION_HEADS_UP);
    }

    @Override
    public void onCorneaError(Throwable cause) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override
    public void onDelete(MobileDeviceModel mobileDeviceModel) {
        showProgressBar();
        SettingsPushNotificationsFragmentController.getInstance().removeMobileDevice(getActivity(), mobileDeviceModel);
    }

    /**
     * Attempts to parse the OS type and version from the strings presents in the MobileDeviceModel.
     * This is a bad idea and should be refactored. No assurances that these string formats won't
     * change in the future.
     *
     * @param deviceModel
     * @return
     */
    public String getDeviceName (MobileDeviceModel deviceModel) {
        if ("ios".equalsIgnoreCase(deviceModel.getOsType())) {

            // Assumes iOS version string looks like
            if (deviceModel.getOsVersion() != null && deviceModel.getOsVersion().split(" ").length == 4) {
                return getActivity().getString(R.string.device_name, deviceModel.getOsType().toUpperCase(), deviceModel.getOsVersion().split(" ")[1]);
            } else {
                return deviceModel.getOsType().toUpperCase();
            }
        } else if ("android".equalsIgnoreCase(deviceModel.getOsType())) {
            if (deviceModel.getOsVersion() != null && deviceModel.getOsVersion().split(" ").length == 3) {
                return getActivity().getString(R.string.device_name, deviceModel.getOsType().toUpperCase(), deviceModel.getOsVersion().split(" ")[2]);
            } else {
                return deviceModel.getOsType().toUpperCase();
            }
        }

        return getActivity().getString(R.string.device_name_unknown);
    }
}
