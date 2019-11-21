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
package arcus.app.subsystems.scenes.editor;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ToggleButton;

import com.google.common.base.Strings;
import arcus.cornea.device.thermostat.ThermostatMode;
import arcus.cornea.device.thermostat.ThermostatProxyModel;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.TemperatureUtils;
import com.iris.client.bean.Action;
import com.iris.client.bean.ActionSelector;
import com.iris.client.bean.ThermostatAction;
import com.iris.client.capability.Thermostat;
import com.iris.client.model.SceneModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.popups.MultiButtonPopup;
import arcus.app.common.popups.NumberPickerPopup;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.details.model.ThermostatOperatingMode;
import arcus.app.device.details.presenters.BaseThermostatPresenter;
import arcus.app.device.model.DeviceType;
import arcus.app.subsystems.scenes.editor.controller.SceneEditorSequenceController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ThermostatActionEditFragment extends SequencedFragment<SceneEditorSequenceController> implements View.OnClickListener {
    private static final String TSTAT_ID = "TSTAT_ID";
    private LinearLayout mSettingsContainer;
    private LinearLayout mHeatContainer;
    private LinearLayout mCoolContainer;
    private RelativeLayout mFollowScheduleGroup;

    private Version1TextView mModeText;
    private Version1TextView mHighTempText;
    private Version1TextView mLowTempText;
    private Version1TextView lowTempLabelText;
    private Version1TextView lowTempLabelSubText;
    private Version1TextView highTempLabelText;
    private Version1TextView highTempLabelSubtext;
    private Version1TextView modelLabelText;
    private Version1TextView scheduleLableText;
    private Version1TextView scheduleLableSubtext;

    private ImageView modeChevron;
    private ImageView heatChevron;
    private ImageView coolChevron;

    private ToggleButton mSettingsSwitch;
    private String thermostatID;
    private String actionSelectorName;

    private ThermostatProxyModel thermostatModel;
    private ThermostatMode thermostatDefaultMode = ThermostatMode.AUTO;
    private boolean scheduleEnabled = false;
    private int tstatDefCoolTemp = 78;
    private int tstatDefHeatTemp = 68;
    private int thermostatMin = 45;
    private int thermostatMax = 95;

    public static ThermostatActionEditFragment newInstance(String tStatID) {
        ThermostatActionEditFragment fragment = new ThermostatActionEditFragment();

        Bundle bundle = new Bundle(1);
        bundle.putString(TSTAT_ID, tStatID);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        
        mSettingsSwitch = (ToggleButton) view.findViewById(R.id.toggle);

        mFollowScheduleGroup = (RelativeLayout) view.findViewById(R.id.follow_schedule_group);
        mSettingsContainer = (LinearLayout) view.findViewById(R.id.settings_group);
        mHeatContainer = (LinearLayout) view.findViewById(R.id.high_temp_container);
        mCoolContainer = (LinearLayout) view.findViewById(R.id.cool_temp_container);
        lowTempLabelText = (Version1TextView) view.findViewById(R.id.low_temp_label_text);
        lowTempLabelSubText = (Version1TextView) view.findViewById(R.id.low_temp_label_sub_text);

        mModeText = (Version1TextView) view.findViewById(R.id.mode_text);
        mHighTempText = (Version1TextView) view.findViewById(R.id.high_temp_text);
        mLowTempText = (Version1TextView) view.findViewById(R.id.low_temp_text);
        highTempLabelSubtext = (Version1TextView) view.findViewById(R.id.high_setpoint_copy);
        highTempLabelText = (Version1TextView) view.findViewById(R.id.high_copy);
        modelLabelText = (Version1TextView) view.findViewById(R.id.toggle2);
        scheduleLableText = (Version1TextView) view.findViewById(R.id.title);
        scheduleLableSubtext = (Version1TextView) view.findViewById(R.id.description);
        modeChevron = (ImageView) view.findViewById(R.id.mode_chevron);
        coolChevron = (ImageView) view.findViewById(R.id.cool_chevron);
        heatChevron = (ImageView) view.findViewById(R.id.heat_chevron);

        mSettingsSwitch.setChecked(true);
        mSettingsSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    mSettingsContainer.setVisibility(View.GONE);
                    saveSettings(createThermostatAction());
                } else {
                    mSettingsContainer.setVisibility(View.VISIBLE);
                    if(thermostatModel!=null){
                        switchThermostatMode(thermostatModel.getMode());
                    }
                }
            }
        });

        LinearLayout mModeContainer = (LinearLayout) view.findViewById(R.id.mode_container);
        mModeContainer.setOnClickListener(this);
        mHeatContainer.setOnClickListener(this);
        mCoolContainer.setOnClickListener(this);

        thermostatID = getArguments().getString(TSTAT_ID, "");

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionSelector actionSelector = getActionSelector();
        if (actionSelector == null) {
            return;
        }

        actionSelectorName = actionSelector.getName();
        setupThermostatModel();
        setupColorScheme();
        logger.debug("Action selector ahhhh [{}]", actionSelector);
    }

    private void setupColorScheme() {
        boolean isEditMode = getController().isEditMode();

        mModeText.setTextColor(isEditMode ? getResources().getColor(R.color.white_with_35) : getResources().getColor(R.color.black_with_60));
        mHighTempText.setTextColor(isEditMode ? getResources().getColor(R.color.white_with_35) : getResources().getColor(R.color.black_with_60));
        mLowTempText.setTextColor(isEditMode ? getResources().getColor(R.color.white_with_35) : getResources().getColor(R.color.black_with_60));
        lowTempLabelText.setTextColor(isEditMode ? Color.WHITE : Color.BLACK);
        lowTempLabelSubText.setTextColor(isEditMode ? getResources().getColor(R.color.white_with_35) : getResources().getColor(R.color.black_with_60));
        highTempLabelSubtext.setTextColor(isEditMode ? getResources().getColor(R.color.white_with_35) : getResources().getColor(R.color.black_with_60));
        highTempLabelText.setTextColor(isEditMode ? Color.WHITE : Color.BLACK);
        modelLabelText.setTextColor(isEditMode ? Color.WHITE : Color.BLACK);
        scheduleLableText.setTextColor(isEditMode ? Color.WHITE : Color.BLACK);
        scheduleLableSubtext.setTextColor(isEditMode ? getResources().getColor(R.color.white_with_35) : getResources().getColor(R.color.black_with_60));
        modeChevron.setImageResource(isEditMode ? R.drawable.chevron_white : R.drawable.chevron);
        heatChevron.setImageResource(isEditMode ? R.drawable.chevron_white : R.drawable.chevron);
        coolChevron.setImageResource(isEditMode ? R.drawable.chevron_white : R.drawable.chevron);

        if (isEditMode) {
            ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
        } else {
            ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());
        }
    }

    @Nullable
    @Override
    public String getTitle() {
        return "THERMOSTAT";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_scene_thermostat_editor;
    }

    @Override
    public void onClick(View v) {
        NumberPickerPopup.NumberPickerType type = NumberPickerPopup.NumberPickerType.MIN_MAX;
        switch (v.getId()) {
            case R.id.mode_container:
                loadThermostatModePopup();
                break;
            case R.id.high_temp_container: // This actually sets the "Cool" set point
                if (ThermostatMode.AUTO.equals(thermostatModel.getMode())) {
                    type = NumberPickerPopup.NumberPickerType.HIGH;
                    loadNumberPicker(type, thermostatModel.getCoolSetPoint());
                }
                else {
                    loadNumberPicker(type, thermostatModel.getHeatSetPoint());
                }
                break;
            case R.id.cool_temp_container: // This actually sets the "Heat" set point
                if (ThermostatMode.AUTO.equals(thermostatModel.getMode())) {
                    type = NumberPickerPopup.NumberPickerType.LOW;
                    loadNumberPicker(type, thermostatModel.getHeatSetPoint());
                }
                else {
                    loadNumberPicker(type, thermostatModel.getCoolSetPoint());
                }
                break;
        }
    }

    private void updateTemps() {
        switch(thermostatModel.getMode()) {
            case AUTO:
                // Text shows "Your hvac will heat should the temp go below"
                mHighTempText.setText(String.format("%dº", thermostatModel.getCoolSetPoint()));

                // Text shows "Your hvac will cool should the temp go above"
                mLowTempText.setText(String.format("%dº", thermostatModel.getHeatSetPoint()));
                break;

            case HEAT:
                mHighTempText.setText(String.format("%dº", thermostatModel.getHeatSetPoint()));
                break;
            case COOL:
                mLowTempText.setText(String.format("%dº", thermostatModel.getCoolSetPoint()));
                break;
        }
        saveSettings(createThermostatAction());
    }

    private void setupThermostatModel() {
        ThermostatAction action = getController().getThermostatActions(thermostatID, actionSelectorName);
        if (thermostatModel == null) {
            thermostatModel = new ThermostatProxyModel();
        }

        if (action.getMode() == null) {
            thermostatModel.setMode(ThermostatMode.OFF);
        }
        else {
            thermostatModel.setMode(ThermostatMode.valueOf(action.getMode()));
        }

        // High temp
        if (action.getCoolSetPoint() == null) {
            thermostatModel.setCoolSetPoint(tstatDefCoolTemp);
        }
        else {
            thermostatModel.setCoolSetPoint(TemperatureUtils.roundCelsiusToFahrenheit(action.getCoolSetPoint()));
        }

        // Low temp
        if (action.getHeatSetPoint() == null) {
            thermostatModel.setHeatSetPoint(tstatDefHeatTemp);
        }
        else {
            thermostatModel.setHeatSetPoint(TemperatureUtils.roundCelsiusToFahrenheit(action.getHeatSetPoint()));
        }

        Thermostat thermostat = (Thermostat) DeviceModelProvider.instance().getStore().get(CorneaUtils.getIdFromAddress(thermostatID));
        thermostatModel.setSupportedModes(ThermostatOperatingMode.toThermostatModes(BaseThermostatPresenter.getSupportedOperatingModes(thermostat)));
        thermostatModel.setDeviceTypeHint(thermostat.getDevtypehint());

        // No "Follow schedule" option for Nest or TCC thermostats
        if (DeviceType.fromHint(thermostat.getDevtypehint()) == DeviceType.NEST_THERMOSTAT || DeviceType.fromHint(thermostat.getDevtypehint()) == DeviceType.TCC_THERM) {
            mFollowScheduleGroup.setVisibility(View.GONE);
            mSettingsSwitch.setChecked(false);
        } else {
            mSettingsSwitch.setChecked(Boolean.TRUE.equals(action.getScheduleEnabled()));
        }
    }

    private ThermostatAction createThermostatAction() {
        ThermostatAction action = new ThermostatAction();

        action.setScheduleEnabled(Boolean.TRUE.equals(mSettingsSwitch.isChecked()));
        if (!action.getScheduleEnabled()) {
            action.setMode(thermostatModel.getMode().name());
            switch (thermostatModel.getMode()) {
                case AUTO:
                    action.setCoolSetPoint(TemperatureUtils.fahrenheitToCelsius((double) thermostatModel.getCoolSetPoint()));
                    action.setHeatSetPoint(TemperatureUtils.fahrenheitToCelsius((double) thermostatModel.getHeatSetPoint()));
                    break;

                case COOL:
                    action.setCoolSetPoint(TemperatureUtils.fahrenheitToCelsius((double) thermostatModel.getCoolSetPoint()));
                    break;

                case HEAT:
                    action.setHeatSetPoint(TemperatureUtils.fahrenheitToCelsius((double) thermostatModel.getHeatSetPoint()));
                    break;

                default:
                    // No - Op
                    break;
            }
        }

        return action;
    }

    // Pass in null to delete from current context.
    private void saveSettings(@Nullable ThermostatAction thermostatAction) {
        SceneModel sceneModel = getController().getSceneModel();
        if (sceneModel == null) {
            return;
        }

        if(thermostatAction == null) {
            getController().removeSelectorForDevice(thermostatID, actionSelectorName);
        }
        else {
            getController().updateSelectorForDevice(thermostatID, actionSelectorName, thermostatAction.toMap());
        }
    }

    private @Nullable ActionSelector getActionSelector() {
        List<Map<String, List<Action>>> actions = new ArrayList<>();
        Map<String, List<Map<String, Object>>> selector = getController().getActionTemplate().getSelectors();
        if (selector == null || selector.isEmpty()) {
            ErrorManager.in(getActivity()).got(new RuntimeException("T-Stat Selector Map<String, List<Map<String, Object>>> was null/empty."));
            return null;
        }

        List<Map<String, Object>> selectorForTstat = selector.get(thermostatID);
        if (selectorForTstat == null || selectorForTstat.isEmpty()) {
            ErrorManager.in(getActivity()).got(new RuntimeException("T-Stat Selector List<Map<String, Object>> was null/empty."));
            return null;
        }

        // T-Stats should have only 1 selector? Not sure how to handle more anyhow.
        ActionSelector actionSelector = new ActionSelector(selectorForTstat.get(0));
        if (Strings.isNullOrEmpty(actionSelector.getName())) { // Won't know how to save otherwise.
            ErrorManager.in(getActivity()).got(new RuntimeException("T-Stat ActionSelector NAME was null/empty."));
            return null;
        }

        return actionSelector;
    }

    private void loadNumberPicker(final NumberPickerPopup.NumberPickerType type, int currentValue) {
        Thermostat deviceModel = (Thermostat) DeviceModelProvider.instance().getStore().get(CorneaUtils.getIdFromAddress(thermostatID));

        NumberPickerPopup popup = NumberPickerPopup.newInstance(type, BaseThermostatPresenter.getMinimumSetpointF(deviceModel), BaseThermostatPresenter.getMaximumSetpointF(deviceModel), currentValue);
        popup.setCoolSetPoint(thermostatModel.getCoolSetPoint());
        popup.setHeatSetPoint(thermostatModel.getHeatSetPoint());
        popup.setOnValueChangedListener(new NumberPickerPopup.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                switch (type) {
                    case HIGH:
                        thermostatModel.setCoolSetPoint(value);
                        break;
                    case LOW:
                        thermostatModel.setHeatSetPoint(value);
                        break;
                    default:
                        if (ThermostatMode.COOL.equals(thermostatModel.getMode())) {
                            thermostatModel.setCoolSetPoint(value);
                        }
                        else if (ThermostatMode.HEAT.equals(thermostatModel.getMode())) {
                            thermostatModel.setHeatSetPoint(value);
                        }
                }
                updateTemps();
            }
        });

        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    private void loadThermostatModePopup(){
        ArrayList<String> buttons = new ArrayList<>();
        boolean isNest = DeviceType.fromHint(thermostatModel.getDeviceTypeHint()) == DeviceType.NEST_THERMOSTAT;

        for (ThermostatMode thisMode : thermostatModel.getSupportedModes()) {
            int nameStringResId = ThermostatOperatingMode.fromThermostatMode(thisMode).getStringResId(isNest);
            buttons.add(getString(nameStringResId));
        }

        MultiButtonPopup popup = MultiButtonPopup.newInstance("CHOOSE A MODE", buttons);
        popup.setOnButtonClickedListener(new MultiButtonPopup.OnButtonClickedListener() {
            @Override
            public void onButtonClicked(String buttonValue) {
                ThermostatOperatingMode selectedMode = ThermostatOperatingMode.fromDisplayString(buttonValue);
                switchThermostatMode(selectedMode.thermostatMode());
            }
        });
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    private void switchThermostatMode(ThermostatMode thermostatMode) {
        boolean isNest = DeviceType.fromHint(thermostatModel.getDeviceTypeHint()) == DeviceType.NEST_THERMOSTAT;

        switch(thermostatMode) {
            case COOL:
                lowTempLabelText.setText(getString(R.string.climate_more_temperature_title));
                lowTempLabelSubText.setVisibility(View.GONE);
                mHeatContainer.setVisibility(View.GONE);
                mCoolContainer.setVisibility(View.VISIBLE);
                break;
            case HEAT:
                highTempLabelText.setText(getString(R.string.climate_more_temperature_title));
                highTempLabelSubtext.setVisibility(View.GONE);
                mHeatContainer.setVisibility(View.VISIBLE);
                mCoolContainer.setVisibility(View.GONE);
                break;
            case AUTO:
                if ((thermostatModel.getCoolSetPoint() - thermostatModel.getHeatSetPoint()) < 3) {
                    thermostatModel.setHeatSetPoint(tstatDefHeatTemp);
                    thermostatModel.setCoolSetPoint(tstatDefCoolTemp);
                }
                highTempLabelText.setText(getResources().getString(R.string.climate_cool_to));
                lowTempLabelText.setText(getResources().getString(R.string.climate_heat_to));
                highTempLabelSubtext.setVisibility(View.VISIBLE);
                lowTempLabelSubText.setVisibility(View.VISIBLE);

                mHeatContainer.setVisibility(View.VISIBLE);
                mCoolContainer.setVisibility(View.VISIBLE);
                break;
            case ECO:
            case OFF:
                mHeatContainer.setVisibility(View.GONE);
                mCoolContainer.setVisibility(View.GONE);
                break;
            default:
                break;
        }

        thermostatModel.setMode(thermostatMode);

        String modeName = getString(ThermostatOperatingMode.fromThermostatMode(thermostatMode).getStringResId(isNest));
        mModeText.setText(modeName);
        updateTemps();
    }


}
