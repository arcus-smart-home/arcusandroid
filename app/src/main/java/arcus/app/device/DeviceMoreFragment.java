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
package arcus.app.device;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import arcus.cornea.dto.HubDeviceModelDTO;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.ColorTemperature;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Hub;
import com.iris.client.capability.HubAdvanced;
import com.iris.client.capability.HubConnection;
import com.iris.client.capability.Light;
import com.iris.client.capability.Thermostat;
import com.iris.client.capability.WiFi;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.events.ArcusEvent;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.utils.I2ColorUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.model.DeviceType;
import arcus.app.device.more.HubConnectivityAndPowerFragment;
import arcus.app.device.more.HubFirmwareFragment;
import arcus.app.device.more.ProductInfoFragment;
import arcus.app.device.pairing.post.NameDeviceFragment;
import arcus.app.device.removal.controller.DeviceRemovalSequenceController;
import arcus.app.device.settings.SettingsManager;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingAbstractChangedListener;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.subsystems.alarm.AlertFloatingFragment;

import java.beans.PropertyChangeEvent;
import java.util.List;

import de.greenrobot.event.EventBus;


public class DeviceMoreFragment extends BaseFragment implements IShowedFragment, View.OnClickListener {

    @Nullable
    private ViewGroup parentGroup;
    private LinearLayout connectivityContainer;
    private LinearLayout firmwareContainer;
    private LinearLayout favoritesContainer;
    private LinearLayout wifiContainer;
    private LinearLayout settingsContainer;

    private ToggleButton favoriteCheckBox;
    private Version1TextView productPlace;
    private Version1TextView productNameInstructions;
    private TextView productName;

    private TextView tstatExtraText;
    private Version1Button rebootHubBtn;

    private String mDeviceId;
    private boolean canRebootHub;
    private DeviceModel mDeviceModel;
    @Nullable
    private HubModel mHubModel;

    private ListenerRegistration modelFavoriteListener;
    private ListenerRegistration hubStateListener;
    private final String FAVORITE_TAG = "FAVORITE";
    private final ImmutableSet<String> tagSet = ImmutableSet.of(FAVORITE_TAG);
    @NonNull
    private Listener<Throwable> failureListener = new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
        }
    };

    @NonNull
    public static DeviceMoreFragment newInstance() {
        return new DeviceMoreFragment();
    }

    //have an implemenation where you pass in the type.

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        parentGroup = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        LinearLayout productContainer = (LinearLayout) parentGroup.findViewById(R.id.fragment_device_more_device);
        LinearLayout productInfoContainer =
                (LinearLayout) parentGroup.findViewById(R.id.fragment_device_more_product_info);
        favoritesContainer = (LinearLayout) parentGroup.findViewById(R.id.favorites_checkbox_container);
        wifiContainer = (LinearLayout) parentGroup.findViewById(R.id.fragment_device_more_wifi);
        wifiContainer.setVisibility(View.GONE);
        productPlace = (Version1TextView) parentGroup.findViewById(R.id.product_place);

        favoriteCheckBox = (ToggleButton) parentGroup.findViewById(R.id.favorites_checkbox);
        connectivityContainer = (LinearLayout) parentGroup.findViewById(R.id.fragment_more_product_connectivity);
        firmwareContainer = (LinearLayout) parentGroup.findViewById(R.id.fragment_more_product_firmware);

        productContainer.setOnClickListener(this);
        productInfoContainer.setOnClickListener(this);
        favoritesContainer.setOnClickListener(this);
        connectivityContainer.setOnClickListener(this);
        firmwareContainer.setOnClickListener(this);

        productName = (TextView) parentGroup.findViewById(R.id.fragment_more_product_name);
        productNameInstructions = (Version1TextView) parentGroup.findViewById(R.id.device_more_product_name_instructions);
        tstatExtraText = (TextView) parentGroup.findViewById(R.id.fragment_more_product_tstat);

        settingsContainer = (LinearLayout) parentGroup.findViewById(R.id.settings);

        rebootHubBtn = (Version1Button) parentGroup.findViewById(R.id.fragment_device_more_hub_reboot);
        rebootHubBtn.setColorScheme(Version1ButtonColor.WHITE);
        rebootHubBtn.setOnClickListener(this);

        Version1Button removeDeviceBtn = (Version1Button) parentGroup.findViewById(R.id.fragment_device_more_remove);
        removeDeviceBtn.setColorScheme(Version1ButtonColor.WHITE);
        removeDeviceBtn.setOnClickListener(this);

        return parentGroup;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {

        return R.layout.fragment_device_more;

    }

    @Override
    public void onShowedFragment() {
        SettingsList settings;

        // Configure for hubs
        if (mHubModel != null) {
            favoritesContainer.setVisibility(View.GONE);
            connectivityContainer.setVisibility(View.VISIBLE);
            firmwareContainer.setVisibility(View.VISIBLE);
            wifiContainer.setVisibility(View.GONE);
            rebootHubBtn.setVisibility(View.VISIBLE);
            String name;
            if (Strings.isNullOrEmpty(mHubModel.getName())) {
                name = "";
            } else {
                name = mHubModel.getName().toUpperCase();
            }
            productName.setText(name);

            settings = SettingsManager.with(getActivity(), mHubModel).getSettings();

            if (HubConnection.STATE_ONLINE.equals(mHubModel.get(HubConnection.ATTR_STATE))) {
                canRebootHub = true;
            }
            updateRebootBtn();
        }

        // Configure for devices
        else {

            // Hide favorite option when subscriber doesn't have access to that feature
            // Hide favorite option device is a genie or somfy blinds controller
            if (DeviceType.fromHint(mDeviceModel.getDevtypehint()).equals(DeviceType.GENIE_GARAGE_DOOR_CONTROLLER) ||
                    DeviceType.fromHint(mDeviceModel.getDevtypehint()).equals(DeviceType.SOMFYV1BRIDGE)) {
                favoritesContainer.setVisibility(View.GONE);
            } else {
                favoritesContainer.setVisibility(View.VISIBLE);
            }

            if (DeviceType.fromHint(mDeviceModel.getDevtypehint()).equals(DeviceType.HALO)) {
                productNameInstructions.setText(getString(R.string.device_more_product_name_instr_halo));
            } else if (DeviceType.fromHint(mDeviceModel.getDevtypehint()).equals(DeviceType.KEYFOB)) {
                productNameInstructions.setText(getString(R.string.device_more_product_name_instr_keyfob));
            } else {
                productNameInstructions.setText(getString(R.string.device_more_product_name_instr));
            }

            //make these invisible.
            if (DeviceType.fromHint(mDeviceModel.getDevtypehint()).equals(DeviceType.WATER_HEATER)) {
                wifiContainer.setVisibility(View.VISIBLE);
                productPlace.setText(String.valueOf(mDeviceModel.get(WiFi.ATTR_SSID)));
                tstatExtraText.setVisibility(View.VISIBLE);
                tstatExtraText.setText(getActivity().getText(R.string.water_more_extra_text));
            } else {
                wifiContainer.setVisibility(View.GONE);
            }

            //water softener, too.
            if (DeviceType.fromHint(mDeviceModel.getDevtypehint()).equals(DeviceType.WATER_SOFTENER)) {
                wifiContainer.setVisibility(View.VISIBLE);
                productPlace.setText(String.valueOf(mDeviceModel.get(WiFi.ATTR_SSID)));
            } else {
                wifiContainer.setVisibility(View.GONE);
            }

            connectivityContainer.setVisibility(View.GONE);
            firmwareContainer.setVisibility(View.GONE);
            String name;
            if (Strings.isNullOrEmpty(mDeviceModel.getName())) {
                name = "";
            } else {
                name = mDeviceModel.getName().toUpperCase();
            }
            productName.setText(name);

            settings = SettingsManager.with(getActivity(), mDeviceModel).getSettings();
        }

        if (settings != null) {
            // Call method to create Settings Section.
            createSettingsSection(settings);
        }

        if (mDeviceModel != null) {
            if (mDeviceModel.getCaps() != null && mDeviceModel.getCaps().contains(Thermostat.NAMESPACE)) {
                tstatExtraText.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getColorFilterValue() {
        if (mDeviceModel == null) {
            return 0;
        }
        int colorOverlay = 0;
        String colorMode = "";
        if (mDeviceModel instanceof Light) {
            Light light = (Light) mDeviceModel;
            colorMode = light.getColormode();
        }

        if (DeviceType.fromHint(mDeviceModel.getDevtypehint()).equals(DeviceType.HUE_FALLBACK)) {
            float[] colorHSV = new float[]{0f, 0f, 0.5f};
            return Color.HSVToColor(colorHSV);
        }

        if (mDeviceModel instanceof com.iris.client.capability.Color || mDeviceModel instanceof ColorTemperature) {
            if (Light.COLORMODE_COLORTEMP.equals(colorMode)) {
                if (mDeviceModel instanceof ColorTemperature) {
                    ColorTemperature colorTemperature = (ColorTemperature) mDeviceModel;
                    int temp = colorTemperature.getColortemp();
                    int minTemp = colorTemperature.getMincolortemp();
                    int maxTemp = colorTemperature.getMaxcolortemp();
                    float percentage = ((float) (temp - minTemp) / (float) (maxTemp - minTemp));
                    float[] colorHSV = I2ColorUtils.getTemperatureColor3Point(percentage);
                    colorOverlay = Color.HSVToColor(25, colorHSV);
                }
            } else if (Light.COLORMODE_COLOR.equals(colorMode)) {
                com.iris.client.capability.Color color = (com.iris.client.capability.Color) mDeviceModel;
                float[] colorHSV = new float[]{color.getHue(), (color.getSaturation() / 100f), 1f};
                colorOverlay = Color.HSVToColor(25, colorHSV);
            } else if (Light.COLORMODE_NORMAL.equals(colorMode)) {
                return 0;
            }
        }
        return colorOverlay;
    }

    @Override
    protected ColorFilter getOnlineColorFilter() {
        View colorOverlayView = parentGroup.findViewById(R.id.color_overlay);
        if (colorOverlayView == null) {
            return null;
        }

        if (mDeviceModel == null) {
            colorOverlayView.setVisibility(View.GONE);
            return null;
        }

        if (DeviceConnection.STATE_OFFLINE.equals(mDeviceModel.get(DeviceConnection.ATTR_STATE))) {
            colorOverlayView.setVisibility(View.GONE);
            canRebootHub = false;
            return null;
        }

        int colorOverlay = getColorFilterValue();

        if (colorOverlay != 0) {
            colorOverlayView.setVisibility(View.VISIBLE);
            colorOverlayView.setBackgroundColor(colorOverlay);
        } else {
            colorOverlayView.setVisibility(View.GONE);
            return null;
        }

        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0f);
        return new ColorMatrixColorFilter(cm);
    }

    public void onEvent(@Nullable final ArcusEvent event) {
        if (event != null) {
            mDeviceId = (String) event.getEvent();
            mDeviceModel = getCorneaService().getStore(DeviceModel.class).get(mDeviceId);
            productName.setText(mDeviceModel.getName().toUpperCase());
        }
    }

    @Override
    public void onClick(@NonNull View v) {
        final int id = v.getId();
        switch (id) {
            case R.id.fragment_device_more_device:
                String deviceAddress = mDeviceModel != null ? mDeviceModel.getAddress() :
                        mHubModel != null ? mHubModel.getAddress() : null;
                String deviceName =
                        mDeviceModel != null ? mDeviceModel.getName() : mHubModel != null ? mHubModel.getName() : null;
                BackstackManager.getInstance().navigateToFragment(NameDeviceFragment
                        .newInstance(NameDeviceFragment.ScreenVariant.SETTINGS, deviceName, deviceAddress), true);
                break;
            case R.id.fragment_device_more_product_info:
                BackstackManager.getInstance().navigateToFragment(ProductInfoFragment.newInstance(mDeviceId), true);
                break;
            case R.id.favorites_checkbox_container:
                setFavoriteTag(!isFavorite());
                break;
            case R.id.fragment_more_product_connectivity:
                BackstackManager.getInstance()
                        .navigateToFragment(HubConnectivityAndPowerFragment.newInstance(mDeviceId), true);
                break;
            case R.id.fragment_more_product_firmware:
                BackstackManager.getInstance().navigateToFragment(HubFirmwareFragment.newInstance(), true);
                break;
            case R.id.fragment_device_more_remove:
                remove();
                break;
            case R.id.fragment_device_more_hub_reboot:
                rebootHub();
                rebootHubBtn.setEnabled(false);
                break;
        }
    }

    private void remove() {

        // Removing the hub
        if (mHubModel != null) {
            BackstackManager.getInstance().navigateToFragment(RemoveHubFragment.newInstance(), true);
        } else {
            String title = getString(R.string.device_remove_are_you_sure);
            String description = "";
            if (mDeviceModel != null) {
                // Hide favorite option device is a genie controller
                if (DeviceType.GENIE_GARAGE_DOOR_CONTROLLER.equals(DeviceType.fromHint(mDeviceModel.getDevtypehint())) ||
                        DeviceType.HUE_BRIDGE.equals(DeviceType.fromHint(mDeviceModel.getDevtypehint()))) {
                    title = getString(R.string.genie_remove_title);
                    description = getString(R.string.genie_remove_description);
                }
            }


            AlertFloatingFragment areYouSureFragment = AlertFloatingFragment
                    .newInstance(title, description, getString(R.string.device_remove_yes),
                            getString(R.string.device_remove_no), new AlertFloatingFragment.AlertButtonCallback() {
                                @Override
                                public boolean topAlertButtonClicked() {
                                    BackstackManager.getInstance().navigateBack();
                                    new DeviceRemovalSequenceController()
                                            .startSequence(getActivity(), null, mDeviceModel);

                                    return false;
                                }

                                @Override
                                public boolean bottomAlertButtonClicked() {
                                    // Nothing to do; user canceled "Are you sure?"
                                    return true;
                                }
                            });

            BackstackManager.getInstance()
                    .navigateToFloatingFragment(areYouSureFragment, areYouSureFragment.getClass().getSimpleName(),
                            true);
        }
    }

    private void rebootHub() {
        HubModel hubModel = SessionModelManager.instance().getHubModel();
        HubAdvanced hubAdvanced = (HubAdvanced) hubModel;

        try {
            if (hubAdvanced != null) hubAdvanced.reboot();
        } catch (Exception e) {
            showRebootErrorPopup();
        }
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public void setDeviceId(String deviceId) {
        mDeviceId = deviceId;
        mDeviceModel = getCorneaService().getStore(DeviceModel.class).get(mDeviceId);
        if (mDeviceModel != null) {
            mHubModel = null;
            removeListener();
            removeHubListener();
            registerListener();
        } else {
            HubModel hubModel = SessionModelManager.instance().getHubModel();
            if (hubModel != null) {
                mHubModel = new HubDeviceModelDTO(hubModel);
                removeListener();
                removeHubListener();
                registerHubListener();
            }
        }

        updateFavoriteState(isFavorite());
    }

    private void removeListener() {
        modelFavoriteListener = Listeners.clear(modelFavoriteListener);
    }

    private void registerListener() {
        if (mDeviceModel != null) {
            modelFavoriteListener = mDeviceModel.addPropertyChangeListener(this::favoriteUpdated);
        }
    }

    private void removeHubListener() {
        hubStateListener = Listeners.clear(hubStateListener);
    }

    private void registerHubListener() {
        if (mHubModel != null) {
            hubStateListener = mHubModel.addPropertyChangeListener(evt -> {
                if (evt.getPropertyName().equals(Hub.ATTR_STATE)) {
                    hubStateChanged(evt.getNewValue());
                }
            });
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListener();
        removeHubListener();
        EventBus.getDefault().unregister(this);
    }

    private void updateFavoriteState(Boolean state) {
        if (state)
            favoriteCheckBox.setChecked(true);
        else
            favoriteCheckBox.setChecked(false);
    }

    private Boolean isFavorite() {
        if (mDeviceModel != null) {
            return mDeviceModel.getTags() != null && mDeviceModel.getTags().contains(FAVORITE_TAG);
        }
        return false;
    }

    private void setFavoriteTag(Boolean value) {
        if (mDeviceModel == null) {
            logger.debug("Unable to access NULL model. Cannot change favorite state.");
            return;
        }

        if (value) {
            mDeviceModel.addTags(tagSet).onFailure(failureListener);
        } else {
            mDeviceModel.removeTags(tagSet).onFailure(failureListener);
        }
    }

    // Created new method to build the Settings Section with new listener. This is called whenever
    // the Settings Abstract value changes.
    private void createSettingsSection(final SettingsList settings) {
        settingsContainer.removeAllViews();
        List<Setting> settingList = settings.getSettings();
        for (Setting setting : settingList) {
            if (setting != null) {
                settingsContainer.addView(setting.getView(getActivity(), settingsContainer));
                setting.addListener(new SettingAbstractChangedListener() {
                    @Override
                    public void onSettingAbstractChanged() {
                        createSettingsSection(settings);
                    }
                });
            }
        }
    }

    // Couldn't use the default listener registered in the base fragment since this fragment is getting reused
    // for each product, just the details of what the UI shows changes (the fragment is not re-created with a new listener).
    public void favoriteUpdated(@NonNull final PropertyChangeEvent event) {
        switch (event.getPropertyName()) {
            case Device.ATTR_TAGS:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateFavoriteState(isFavorite());
                    }
                });
                break;
        }
    }

    private void hubStateChanged(Object state) {
        if (mHubModel == null) {
            canRebootHub = false;
            return;
        }
        if (Hub.STATE_DOWN.equals(state)) {
            canRebootHub = false;
            updateRebootBtn();
        } else if (Hub.STATE_NORMAL.equals(state)) {
            canRebootHub = true;
            updateRebootBtn();
        }
    }

    private void updateRebootBtn() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                rebootHubBtn.setEnabled(canRebootHub);
            }
        });
    }

    private void showRebootErrorPopup() {
        AlertPopup popup = AlertPopup.newInstance(
                getString(R.string.error_generic_title)
                , null
                , getString(R.string.hub_reboot_failed),
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
                        return false;
                    }

                    @Override
                    public void close() {
                        BackstackManager.getInstance().navigateBack();
                    }
                });

        popup.setCloseButtonVisible(true);
    }
}
