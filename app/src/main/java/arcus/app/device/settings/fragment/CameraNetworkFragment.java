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

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import arcus.cornea.device.camera.CameraWifiController;
import arcus.cornea.device.camera.model.AvailableNetworkModel;
import arcus.cornea.device.camera.model.CameraConnectionSettingModel;
import arcus.cornea.device.camera.model.WiFiSecurityType;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.popups.TextPickerPopup;
import arcus.app.common.popups.WiFiPickerPopup;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.validation.NotEmptyValidator;
import arcus.app.common.validation.NotEqualValidator;
import arcus.app.common.view.Version1EditText;
import arcus.app.common.view.Version1TextView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class CameraNetworkFragment extends SequencedFragment implements CameraWifiController.Callback {

    private final static String CAMERA_ADDRESS = "CAMERA_ADDRESS";
    private final static Logger logger = LoggerFactory.getLogger(CameraNetworkFragment.class);

    private String deviceAddress;

    private Version1TextView connectionStatus;
    private Version1TextView connectionDescription;
    private ImageView wifiChevron;
    private ImageView securityChevron;
    private LinearLayout wifiLayout;
    private LinearLayout passwordLayout;
    private LinearLayout passwordEditLayout;
    private LinearLayout passwordRepeatLayout;
    private LinearLayout customNameLayout;
    private LinearLayout securityLayout;
    private Version1TextView ssid;
    private Version1TextView passwordText;
    private Version1TextView security;
    private Version1EditText customSSID;
    private Version1EditText password;
    private Version1EditText passwordRepeat;

    private CameraWifiController controller;
    private WiFiSecurityType selectedSecurityType;
    private static final String CUSTOM = "Custom";
    private boolean isEditMode = false;
    private boolean isPopupMode = false;
    private boolean isLoadingNetworks = true;
    private WiFiPickerPopup wifiPopup;

    // Initial State
    private CameraConnectionSettingModel initialSettings;

    @NonNull
    public static CameraNetworkFragment newInstance(String cameraId) {
        CameraNetworkFragment instance = new CameraNetworkFragment();

        Bundle arguments = new Bundle();
        arguments.putString(CAMERA_ADDRESS, cameraId);

        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        connectionStatus = (Version1TextView) view.findViewById(R.id.connection_text);
        connectionDescription = (Version1TextView) view.findViewById(R.id.wifi_description);
        wifiChevron = (ImageView) view.findViewById(R.id.wifi_chevron);
        securityChevron = (ImageView) view.findViewById(R.id.security_chevron);
        wifiLayout = (LinearLayout) view.findViewById(R.id.wifi_network_container);
        passwordLayout = (LinearLayout) view.findViewById(R.id.password_container);
        passwordEditLayout = (LinearLayout) view.findViewById(R.id.password_edit_container);
        passwordRepeatLayout = (LinearLayout) view.findViewById(R.id.password_repeat_container);
        customNameLayout = (LinearLayout) view.findViewById(R.id.wifi_network_name_container);
        securityLayout = (LinearLayout) view.findViewById(R.id.security_container);
        ssid = (Version1TextView) view.findViewById(R.id.ssid);
        passwordText = (Version1TextView) view.findViewById(R.id.password_text);
        security = (Version1TextView) view.findViewById(R.id.security);
        customSSID = (Version1EditText) view.findViewById(R.id.custom_name);

        if (customSSID != null)
            customSSID.useLightColorScheme(true);
        password = (Version1EditText) view.findViewById(R.id.password_edit);
        if (password != null)
            password.useLightColorScheme(true);
        passwordRepeat = (Version1EditText) view.findViewById(R.id.password_repeat);
        if (passwordRepeat != null)
            passwordRepeat.useLightColorScheme(true);

        this.deviceAddress = getArguments().getString(CAMERA_ADDRESS, null);

        wifiLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    ArrayList<AvailableNetworkModel> networks = new ArrayList<>(controller.getAvailableWifiNetworks());
                    networks.add(new AvailableNetworkModel(true));

                    wifiPopup = WiFiPickerPopup.newInstance(networks, isLoadingNetworks, false);
                    wifiPopup.setCallback(new WiFiPickerPopup.Callback() {
                        @Override
                        public void selectedItem(AvailableNetworkModel model) {
                            if (model.isCustom()) {
                                ssid.setText(CUSTOM);
                                // TODO Show Custom Input
                                securityChevron.setVisibility(View.VISIBLE);
                                customNameLayout.setVisibility(View.VISIBLE);
                            } else {
                                ssid.setText(model.getSSID());
                                security.setText(model.getSecurity().getName());
                                selectedSecurityType = model.getSecurity();
                                securityChevron.setVisibility(View.GONE);
                                customNameLayout.setVisibility(View.GONE);
                            }

                            isPopupMode = false;

                            BackstackManager.getInstance().navigateBack();
                            getActivity().invalidateOptionsMenu();
                        }

                        @Override
                        public void onEnterSsid() {
                            // Nothing to do; selection not available in this context
                        }

                        @Override
                        public void close() {
                            isPopupMode = false;

                            BackstackManager.getInstance().navigateBack();
                            getActivity().invalidateOptionsMenu();
                        }
                    });
                    showPopup(wifiPopup);

                }
            }
        });

        securityLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEditMode) {
                    // TODO Show Security Popup
                    List<String> securityTypes = controller.getAllAvailableSecurityTypes();

                    TextPickerPopup popup = new TextPickerPopup().newInstance(securityTypes, getString(R.string.security_setting));
                    popup.setCallback(new TextPickerPopup.Callback() {
                        @Override
                        public void selectedItem(String type) {
                            security.setText(type);
                            selectedSecurityType = WiFiSecurityType.fromSecurityString(type);
                            isPopupMode = false;

                            BackstackManager.getInstance().navigateBack();
                            getActivity().invalidateOptionsMenu();
                        }

                        @Override
                        public void close() {
                            isPopupMode = false;

                            BackstackManager.getInstance().navigateBack();
                            getActivity().invalidateOptionsMenu();
                        }
                    });
                    showPopup(popup);
                }
            }
        });

        setRetainInstance(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (controller == null) {
            controller = CameraWifiController.newController(this.deviceAddress, this);
        } else {
            controller.setCallback(this);
        }

        if (!isEditMode) {
            updateCameraSettings(controller.getCameraConfiguration());
        }

        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void onPause() {
        super.onPause();

        controller.clearCallback();

        getActivity().invalidateOptionsMenu();
    }

    @Nullable
    @Override
    public Integer getMenuId() {
        return R.menu.menu_edit_done_toggle;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if(menu.size() > 0){
            if (!isPopupMode) {
                menu.getItem(0).setTitle(isEditMode ? getString(R.string.card_menu_done) : getString(R.string.card_menu_edit));
            } else {
                menu.getItem(0).setTitle("");
            }
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // Save Condition
        if (isEditMode) {
            save();
        } else {
            isEditMode = true;
            setEditMode(true);
        }

        item.setTitle(isEditMode ? getString(R.string.card_menu_done) : getString(R.string.card_menu_edit));

        return true;
    }

    @Override
    public String getTitle() {
        return getString(R.string.setting_camera_net_and_wifi);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_camera_network;
    }

    @Override
    public boolean onBackPressed() {
        if (isEditMode) {
            isEditMode = false;
            setEditMode(false);
            getActivity().invalidateOptionsMenu();

            // Reset values to the initial state and reverse the edit values
            if (initialSettings != null && !initialSettings.getSSID().equals("")) {
                this.ssid.setText(initialSettings.getSSID());
                this.passwordText.setText(getString(R.string.wifi_default_password));
                this.security.setText(initialSettings.getSecurity().getName());
            } else if (initialSettings == null) {
                this.ssid.setText("");
                this.passwordText.setText("");
                this.security.setText("");
            }

            return true;
        } else {
            return super.onBackPressed();
        }
    }

    /***
     * Modify Layout
     */

    private void setEditMode(Boolean isEditMode) {
        if (isEditMode) {
            // Adjust Layout Edit
            wifiChevron.setVisibility(View.VISIBLE);
            securityChevron.setVisibility(View.VISIBLE);
            passwordLayout.setVisibility(View.GONE);
            passwordEditLayout.setVisibility(View.VISIBLE);
            passwordRepeatLayout.setVisibility(View.VISIBLE);

            // Clear Password Field
            password.setText("");
            passwordRepeat.setText("");
        } else {
            // Adjust Layout Normal
            wifiChevron.setVisibility(View.GONE);
            securityChevron.setVisibility(View.GONE);
            passwordLayout.setVisibility(View.VISIBLE);
            customNameLayout.setVisibility(View.GONE);
            passwordEditLayout.setVisibility(View.GONE);
            passwordRepeatLayout.setVisibility(View.GONE);
        }
    }

    /***
     * Wifi Actions
     */

    private void save() {
        if (isSettingsValid()) {
            connectToWifiNetwork();
        }
    }

    public <T extends ArcusFloatingFragment> void showPopup(@NonNull T popup) {
        isPopupMode = true;
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    private boolean isSettingsValid() {
        if (ssid.getText().equals("")) {
            showWifiNotSelectedError();
            return false;
        }

        if (ssid.getText().equals(CUSTOM) && !new NotEmptyValidator(getActivity(), customSSID, R.string.wifi_network_name_empty).isValid()) {
            return false;
        }

        if (selectedSecurityType != WiFiSecurityType.NONE
                && !new NotEmptyValidator(getActivity(), password).isValid()
                && !new NotEmptyValidator(getActivity(), passwordRepeat, R.string.wifi_password_retry_not_entered).isValid()) {

            return false;
        }

        if (!new NotEqualValidator(getActivity(), passwordRepeat, R.string.wifi_password_err_not_equal, password.getText().toString()).isValid()) {
            return false;
        }

        if (selectedSecurityType == null || security.getText().equals("")) {
            showSecurityNotSelectedError();

            return false;
        }

        return true;
    }

    private void showSecurityNotSelectedError() {
        AlertPopup popup = AlertPopup.newInstance(getString(R.string.security_setting),
                getString(R.string.wifi_security_setting_error_msg), null, null, new AlertPopup.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        return false;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        return false;
                    }

                    @Override
                    public boolean errorButtonClicked() {
                        return false;
                    }

                    @Override
                    public void close() {
                        isPopupMode = false;

                        BackstackManager.getInstance().navigateBack();
                        getActivity().invalidateOptionsMenu();
                    }
                });

        showPopup(popup);
    }

    private void showWifiNotSelectedError() {
        AlertPopup popup = AlertPopup.newInstance(getString(R.string.wifi_network),
                getString(R.string.wifi_network_error_msg), null, null, new AlertPopup.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        return false;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        return false;
                    }

                    @Override
                    public boolean errorButtonClicked() {
                        return false;
                    }

                    @Override
                    public void close() {
                        isPopupMode = false;

                        BackstackManager.getInstance().navigateBack();
                        getActivity().invalidateOptionsMenu();
                    }
                });

        showPopup(popup);
    }

    private void connectToWifiNetwork() {
        String ssidString = ssid.getText().toString();
        if (ssidString.equals(CUSTOM)) ssidString = customSSID.getText().toString().trim();
        WiFiSecurityType securityType = selectedSecurityType;
        String passwordString = password.getText().toString();

        if (controller != null) {
            controller.connectToWifiNetwork(ssidString, securityType, passwordString.toCharArray());
        }
    }

    /***
     * Camera Wifi Callback
     */


    @Override
    public void onError(Throwable error) {
        AlertPopup popup = AlertPopup.newInstance(getString(R.string.wifi_connection_error),
                getString(R.string.wifi_save_error),
                getString(R.string.wifi_support_number),
                new AlertPopup.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        return false;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        return false;
                    }

                    @Override
                    public boolean errorButtonClicked() {
                        Intent callSupportIntent = new Intent(Intent.ACTION_DIAL, GlobalSetting.SUPPORT_NUMBER_URI);
                        getActivity().startActivity(callSupportIntent);

                        return false;
                    }

                    @Override
                    public void close() {
                        isPopupMode = false;

                        BackstackManager.getInstance().navigateBack();
                        getActivity().invalidateOptionsMenu();
                    }
                });

        showPopup(popup);
    }

    @Override
    public void showScanResults(@NonNull List<AvailableNetworkModel> availableNetworkModels) {
        // Handle.
        isLoadingNetworks = false;

        if (wifiPopup != null) {
            ArrayList<AvailableNetworkModel> list = new ArrayList<>(availableNetworkModels);
            list.add(new AvailableNetworkModel(true));
            wifiPopup.showWifiNetworks(list);
        }
    }

    @Override
    public void showCameraConnection(CameraConnectionSettingModel settings) {
        updateCameraSettings(settings);
    }

    private void updateCameraSettings(CameraConnectionSettingModel settings) {
        if (!isEditMode) {
            Boolean isWifi = settings.isWifiConnected();

            if (settings.getSSID() != null && !settings.getSSID().equals("")) {
                this.ssid.setText(settings.getSSID());
                this.passwordText.setText(R.string.wifi_default_password);
                this.security.setText(settings.getSecurity().name());

                this.initialSettings = settings;
                this.selectedSecurityType = settings.getSecurity();
            }
            this.connectionStatus.setText(getString(R.string.wifi_connection_status) + (isWifi ? getString(R.string.wifi_wifi) : getString(R.string.wifi_ethernet)));

            if (isWifi) {
                this.connectionDescription.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void showSavingSuccess() {
        // We've saved so go out
        isEditMode = false;
        setEditMode(false);
        passwordText.setText(getString(R.string.wifi_default_password));
        this.connectionStatus.setText(getString(R.string.wifi_connection_status) + getString(R.string.wifi_wifi));
        this.connectionDescription.setVisibility(View.GONE);
        getActivity().invalidateOptionsMenu();
    }
}
