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
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.TemperatureUtils;
import com.iris.client.bean.Action;
import com.iris.client.bean.ActionSelector;
import com.iris.client.bean.ActionTemplate;
import com.iris.client.bean.ThermostatAction;
import com.iris.client.capability.Device;
import com.iris.client.capability.Thermostat;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.SceneModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.popups.MultiButtonPopup;
import arcus.app.common.popups.MultiModelPopup;
import arcus.app.common.popups.NumberPickerPopup;
import arcus.app.common.popups.TimePickerPopup;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.details.model.ThermostatOperatingMode;
import arcus.app.device.model.DeviceType;
import arcus.app.subsystems.scenes.editor.adapter.SceneDefaultSelectorAdapter;
import arcus.app.subsystems.scenes.editor.adapter.SceneSecuritySelectorAdapter;
import arcus.app.subsystems.scenes.editor.controller.SceneEditorSequenceController;
import arcus.app.subsystems.scenes.editor.model.ActionSelectorType;
import arcus.app.subsystems.scenes.editor.model.SceneListItemModel;
import arcus.app.subsystems.scenes.editor.model.SceneType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class SceneActionSelectionFragment extends SequencedFragment<SceneEditorSequenceController> {
    private static final Logger logger = LoggerFactory.getLogger(SceneActionSelectionFragment.class);
    private static final String SECONDS = "SEC";

    private Map<String, List<Map<String, Object>>> selectors;
    private Set<String> leftModels = new LinkedHashSet<>();
    private Set<String> rightModels = new LinkedHashSet<>();

    private boolean isListItemEditMode = false;
    private String selectorName = "";
    private AtomicBoolean leftSelected = new AtomicBoolean(true);
    private View topContainer;
    private Version1TextView leftActionText;
    private Version1TextView rightActionText;
    private Version1TextView deviceListHeading;
    private View divider;
    private ListView deviceListView;
    private Version1Button addButton;

    private SceneType sceneType;
    private String leftText;
    private String rightText;

    public static SceneActionSelectionFragment newInstance() {
        return new SceneActionSelectionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        topContainer = view.findViewById(R.id.topContainer);
        leftActionText = (Version1TextView) view.findViewById(R.id.left_action_text);
        rightActionText = (Version1TextView) view.findViewById(R.id.right_action_text);
        deviceListHeading = (Version1TextView) view.findViewById(R.id.action_device_list_heading);
        divider = view.findViewById(R.id.divider);
        deviceListView = (ListView) view.findViewById(R.id.scene_action_devices_selected_list);
        addButton = (Version1Button) view.findViewById(R.id.add_devices_button);
        return view;
    }

    @Override
    public void onResume () {
        super.onResume();
        ActionTemplate template = getController().getActionTemplate();
        if (template == null || Strings.isNullOrEmpty(template.getTypehint()) || getController().getSceneModel() == null) {
            goBack();
            return;
        }

        sceneType = SceneType.fromName(template.getTypehint().toLowerCase());
        if (SceneType.UNKNOWN.equals(sceneType)) {
            goBack();
            return;
        }

        selectors = getController().getActionTemplate().getSelectors();
        switch(sceneType) {
            case SECURITY:
                displaySecurityActions();
                addButton.setVisibility(View.GONE);
                break;
            case WATERHEATER:
                parseTemplateSelectors();
                parseExistingModelActions();
                addButton.setVisibility(View.GONE);
                break;
            default:
                parseTemplateSelectors();
                parseExistingModelActions();
                setAddButtonOnClickListener();
                break;
        }

        setTextResources();
        setColorScheme();
        getActivity().setTitle(String.valueOf(template.getName()).toUpperCase());
        getActivity().invalidateOptionsMenu();
    }

    private void setColorScheme() {
        if (addButton != null) {
            if (getController().isEditMode()) {
                addButton.setColorScheme(Version1ButtonColor.WHITE);
            }
            else {
                addButton.setColorScheme(Version1ButtonColor.BLACK);
            }
        }

        if (deviceListHeading != null && getController().isEditMode()) {
            deviceListHeading.setTextColor(Color.WHITE);
        }

        if (divider != null && getController().isEditMode()) {
            divider.setBackgroundColor(getResources().getColor(R.color.overlay_white_with_20));
        }
    }

    /**
     * Meant to setup list views for thermostats and others that, by default, populate a list instead of a user choosing it.
     * Should also get the L/R 'GROUP' text.
     */
    @SuppressWarnings({"unchecked"})
    private void parseTemplateSelectors() {
        Set<String> keySet = selectors.keySet();
        for (String key : keySet) {
            List<Map<String, Object>> selectorMap = selectors.get(key);
            if (selectorMap == null || selectorMap.isEmpty()) {
                continue;
            }

            ActionSelector selector = new ActionSelector(selectorMap.get(0));
            String selectorType = String.valueOf(selector.getType());
            selectorName = selector.getName();
            if (ActionSelectorType.GROUP.equals(selectorType)) {
                List<List<Object>> groupValue = (List<List<Object>>) selector.getValue();
                leftText = (String) groupValue.get(0).get(0);
                rightText = (String) groupValue.get(1).get(0);
            }
            return; // Only parse the first one.
        }
    }

    private void parseExistingModelActions() {
        Action modelActions = getActionsFromModel();
        if (modelActions == null) {
            return;
        }

        Map<String, Map<String, Object>> context = modelActions.getContext();
        if (context == null || context.isEmpty()) {
            return;
        }

        for (String key : context.keySet()) {
            if(String.valueOf(context.get(key).get(selectorName)).equalsIgnoreCase(rightText)) {
                rightModels.add(key);
            }
            else {
                leftModels.add(key);
            }
        }
        updateDefaultAdapter(false);
    }

    private void setTextResources() {
        if (leftText != null && rightText != null) {
            topContainer.setVisibility(View.VISIBLE);
            leftActionText.setText(leftText);
            rightActionText.setText(rightText);
            setNavButtonOnClickListeners();
        }

        if (leftText == null) { // No L/R selectors at the top (Eg. Security Alarm) Just use the unformatted text.
            deviceListHeading.setText(getString(sceneType.getHeadingText()));
        }
        else {
            deviceListHeading.setText(String.format(getString(sceneType.getHeadingText()), leftText.toLowerCase()));
        }
    }

    private void setAddButtonOnClickListener() {
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getController().setHasEdited(true);
                final Set<String> modelsAvailable = getController().getActionTemplate().getSelectors().keySet();
                final List<String> models = new ArrayList<>();
                if (!modelsAvailable.isEmpty()) {
                    models.addAll(modelsAvailable);
                }
                List<String> preSelectedModels = Lists.newArrayList(leftSelected.get() ? leftModels : rightModels);
                MultiModelPopup mmp = MultiModelPopup.newInstance(models, null, preSelectedModels, true);
                mmp.setCallback(new MultiModelPopup.Callback() {
                    @Override
                    public void itemSelectedAddress(ListItemModel itemModel) {
                        leftModels.remove(itemModel.getAddress());
                        rightModels.remove(itemModel.getAddress());

                        if (leftSelected.get() && itemModel.isChecked()) {
                            leftModels.add(itemModel.getAddress());
                        }
                        else if (itemModel.isChecked()) {
                            rightModels.add(itemModel.getAddress());
                        }

                        updateDefaultAdapter(true);
                    }
                });

                BackstackManager.getInstance().navigateToFloatingFragment(mmp, mmp.getClass().getSimpleName(), true);
            }
        });
    }

    @Override @Nullable
    public Integer getMenuId() {
        boolean showEdit = deviceListView.getAdapter() != null && deviceListView.getAdapter().getCount() > 0;
        return showEdit ? R.menu.menu_edit_done_toggle : null;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        isListItemEditMode = !isListItemEditMode;
        item.setTitle(isListItemEditMode ? getResources().getString(R.string.card_menu_done) : getResources().getString(R.string.card_menu_edit));
        attachEditModeListener();
        return true;
    }

    private void attachEditModeListener() {
        Adapter adapter = deviceListView.getAdapter();
        if (adapter == null || !(adapter instanceof SceneDefaultSelectorAdapter)) {
            return;
        }

        final SceneDefaultSelectorAdapter sceneAdapter = (SceneDefaultSelectorAdapter) adapter;
        sceneAdapter.isEditMode(isListItemEditMode);
        if (isListItemEditMode) {
            deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    SceneListItemModel m = sceneAdapter.getItem(position);
                    if (leftSelected.get()) {
                        leftModels.remove(m.getAddressAssociatedTo());
                    }
                    else {
                        rightModels.remove(m.getAddressAssociatedTo());
                    }
                    sceneAdapter.remove(position);
                    getController().removeAddressFromContext(m.getAddressAssociatedTo());
                }
            });
        }
        else {
            deviceListView.setOnItemClickListener(null);
            deviceListView.setAdapter(deviceListView.getAdapter());
        }
    }

    private void updateDefaultAdapter(boolean saveModels) {
        List<SceneListItemModel> sceneItemSelectors = updateList(leftSelected.get() ? leftModels : rightModels);

        //sort the list here
        Collections.sort(sceneItemSelectors, new Comparator<SceneListItemModel>() {
            @Override
            public int compare(SceneListItemModel lm1, SceneListItemModel lm2) {
                return lm1.getTitle().compareTo(lm2.getTitle());
            }
        });

        deviceListView.setAdapter(new SceneDefaultSelectorAdapter(getActivity(), sceneItemSelectors, getController().isEditMode()));
        getActivity().invalidateOptionsMenu();

        if (saveModels) {
            saveToPlatform();
        }
    }

    private void saveToPlatform() {
        final Map<String, Map<String, Object>> actionContext = new HashMap<>();
        List<SceneListItemModel> saveModels = updateList(leftModels);
        saveModels.addAll(updateList(rightModels));

        for (SceneListItemModel model : saveModels) {
            // Update everything at once.
            Map<String, Object> deviceContext = new HashMap<>();
            deviceContext.put(model.getSceneSelectorName(), model.getSceneSelectorValue());
            if (model.getSceneSelectorName() != null && model.getSceneSelectorName().toLowerCase().startsWith("thermo")) {
                actionContext.put(
                      model.getAddressAssociatedTo(),
                      ImmutableMap.<String, Object>of(selectorName, getController().getThermostatActions(model.getAddressAssociatedTo(), selectorName).toMap())
                );
            }
            else {
                if (!Strings.isNullOrEmpty(model.getRightSideSelectionName()) && model.getRightSideSelectionValue() != null) {
                    deviceContext.put(model.getRightSideSelectionName(), model.getRightSideSelectionValue());
                }
                actionContext.put(model.getAddressAssociatedTo(), deviceContext);
            }
        }
        getController().updateActionContext(actionContext);
    }

    @SuppressWarnings({"unchecked"})
    private List<SceneListItemModel> updateList(Set<String> models) {
        List<SceneListItemModel> sceneListItemModels = new ArrayList<>();

        for (final String item : models) {
            Model model = CorneaClientFactory.getModelCache().get(item);
            if (model == null || !model.getCaps().contains(Device.NAMESPACE)) {
                continue;
            }
            DeviceModel deviceModel = (DeviceModel) model;
            Map<String, List<Map<String, Object>>> selectors = getController().getActionTemplate().getSelectors();
            if (selectors == null) {
                continue;
            }

            List<Map<String, Object>> selectorItems = selectors.get(deviceModel.getAddress());
            if (selectorItems == null || selectorItems.isEmpty()) {
                continue;
            }

            final SceneListItemModel itemModel = new SceneListItemModel();
            itemModel.setAddressAssociatedTo(deviceModel.getAddress());
            itemModel.setTitle(deviceModel.getName());
            itemModel.setSubText(deviceModel.getVendor());
            itemModel.setSceneSelectorName(selectorName);
            itemModel.setSceneSelectorValue(leftModels.contains(deviceModel.getAddress()) ? leftText : rightText);
            for (Map<String, Object> contextMap : selectorItems) {
                ActionSelector actionSelector = new ActionSelector(contextMap);
                String type = String.valueOf(actionSelector.getType());
                switch(type) {
                    case ActionSelectorType.THERMOSTAT:
                        ThermostatAction thermostatAction = getController().getThermostatActions(itemModel.getAddressAssociatedTo(), selectorName);
                        DeviceType deviceType = DeviceType.fromHint(deviceModel.getDevtypehint());

                        // Special case: Provision default action for Nest and TCC thermostats
                        if (thermostatAction.getMode() == null && (deviceType == DeviceType.TCC_THERM || deviceType == DeviceType.NEST_THERMOSTAT))
                        {
                            thermostatAction.setMode(CorneaUtils.getCapability(deviceModel, Thermostat.class).getHvacmode());
                            thermostatAction.setCoolSetPoint(CorneaUtils.getCapability(deviceModel, Thermostat.class).getCoolsetpoint());
                            thermostatAction.setHeatSetPoint(CorneaUtils.getCapability(deviceModel, Thermostat.class).getHeatsetpoint());
                            thermostatAction.setScheduleEnabled(false);
                        }

                        else if (thermostatAction.getScheduleEnabled() == null) {
                            thermostatAction.setScheduleEnabled(true);
                        }

                        if (Boolean.valueOf(true).equals(thermostatAction.getScheduleEnabled())) {
                            itemModel.setRightText(getString(R.string.follow_schedule_title));
                        }
                        else {
                            itemModel.setRightText(getThermostatAbstract(DeviceType.fromHint(deviceModel.getDevtypehint()), thermostatAction));
                        }

                        itemModel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                getController().goThermostatActionEditor(
                                      getActivity(),
                                      SceneActionSelectionFragment.this,
                                      itemModel.getAddressAssociatedTo());
                            }
                        });
                        getController().updateSelectorForDevice(itemModel.getAddressAssociatedTo(), selectorName, thermostatAction.toMap());
                        break;
                    case ActionSelectorType.GROUP:
                        try {
                            int index = leftModels.contains(deviceModel.getAddress()) ? 0 : 1;
                            List<List<Object>> groupValue = (List<List<Object>>) actionSelector.getValue();
                            List<Map<String, Object>> innards = (List<Map<String, Object>>) groupValue.get(index).get(1);
                            if (innards != null && !innards.isEmpty()) {
                                ActionSelector innerSelector = new ActionSelector(innards.get(0));
                                String innerSelectorType = String.valueOf(innerSelector.getType());
                                parseGroupItem(itemModel, innerSelectorType, innerSelector);
                            }
                        }
                        catch (Exception ex) {
                            logger.debug("Big bang theory is on tonight. ->", ex);
                        }
                        break;
                    default:
                        parseGroupItem(itemModel, type, actionSelector);
                        break;
                }
            }
            sceneListItemModels.add(itemModel);
        }


        return sceneListItemModels;
    }

    @SuppressWarnings({"unchecked"})
    private void parseGroupItem(final SceneListItemModel itemModel, String innerSelectorType, ActionSelector innerSelector) {
        String itemAddress = itemModel.getAddressAssociatedTo();
        String innerSelectorName = innerSelector.getName();
        Object rightSideSelectionValue = getController().getSelectorForDevice(itemAddress, innerSelectorName);
        itemModel.setRightSideSelectionName(innerSelectorName);

        itemModel.setIsSeconds(SECONDS.equals(innerSelector.getUnit()));
        int max = 100;
        int min = 1;
        int step = 1;
        if (innerSelector.getMax() != null) {
            max = itemModel.isSeconds() ? innerSelector.getMax() / 60 : innerSelector.getMax();
        }
        if (innerSelector.getMin() != null) {
            int setMinTo = itemModel.isSeconds() ? innerSelector.getMin() / 60 : innerSelector.getMin();
            if (setMinTo > 0) {
                min = setMinTo;
            }
        }
        if (innerSelector.getStep() != null) {
            step = innerSelector.getStep();
        }

        if (rightSideSelectionValue == null) {
            itemModel.setRightSideSelectionValue(itemModel.isSeconds() ? min * 60 : max);
        }
        else {
            itemModel.setRightSideSelectionValue(rightSideSelectionValue);
        }

        switch (innerSelectorType) {
            case ActionSelectorType.DURATION:
                if (itemModel.isSeconds()) {
                    try {
                        Number timeValue = (Number) itemModel.getRightSideSelectionValue();
                        itemModel.setRightText(getDurationAbstract(timeValue.intValue()));
                    }
                    catch (Exception ex) {
                        itemModel.setRightText(itemModel.getRightSideSelectionValue().toString());
                    }
                }
                else {
                    itemModel.setRightText("" + itemModel.getRightSideSelectionValue());
                }

                navigateToTimePicker(itemModel, min, max, min);
                break;

            case ActionSelectorType.RANGE:
                navigateToNumberPicker(itemModel, NumberPickerPopup.NumberPickerType.MIN_MAX, min, max, max, step);
                break;

            case ActionSelectorType.PERCENT: // Percents always by 10; round first value to closest 10th percentage
                itemModel.setRightText(getPercentAbstract((Number) itemModel.getRightSideSelectionValue()));
                navigateToNumberPicker(itemModel, NumberPickerPopup.NumberPickerType.PERCENT, (min / 10) * 10, max, max, 10);
                break;

            case ActionSelectorType.TEMPERATURE:
                final double minTemp = TemperatureUtils.celsiusToFahrenheit(innerSelector.getMin());
                final double maxTemp = TemperatureUtils.celsiusToFahrenheit(innerSelector.getMax());
                if (rightSideSelectionValue == null) { // Set the default value, if not present.
                    rightSideSelectionValue = 23.88889; // 75 in fahrenheit
                    itemModel.setRightSideSelectionValue(rightSideSelectionValue);
                }
                int curTemp = TemperatureUtils.celsiusToFahrenheit(((Number) itemModel.getRightSideSelectionValue()).doubleValue()).intValue();
                String s = Integer.toString(curTemp)+ getResources().getString(R.string.degree_symbol);
                itemModel.setRightText(s);
                itemModel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int defValue;
                        try {
                            defValue = TemperatureUtils.celsiusToFahrenheit(((Number) itemModel.getRightSideSelectionValue()).doubleValue()).intValue();
                        } catch (Exception ex) {
                            defValue = 75;
                        }
                        NumberPickerPopup popup = NumberPickerPopup.newInstance(NumberPickerPopup.NumberPickerType.MIN_MAX,
                                (int)minTemp, (int)maxTemp, defValue);
                        popup.setOnValueChangedListener(new NumberPickerPopup.OnValueChangedListener() {
                            @Override
                            public void onValueChanged(int value) {
                                double newValue = TemperatureUtils.fahrenheitToCelsius((double)value);
                                getController().updateSelectorForDevice(itemModel.getAddressAssociatedTo(), itemModel.getRightSideSelectionName(), newValue);
                                itemModel.setRightSideSelectionValue(value);
                                updateDefaultAdapter(false);
                            }
                        });
                        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getName(), true);
                    }
                });

                break;

            case ActionSelectorType.LIST:
                try {
                    Object innerValue = innerSelector.getValue();
                    if (innerValue == null || !(innerValue instanceof List)) {
                        break;
                    }

                    final List<List<Object>> listValues = (List<List<Object>>) innerValue;
                    final ArrayList<String> buttonValues = new ArrayList<>();
                    if (rightSideSelectionValue == null) { // Set the default value, if not present.
                        rightSideSelectionValue = listValues.get(listValues.size() - 1).get(1);
                        itemModel.setRightSideSelectionValue(rightSideSelectionValue);
                    }
                    for (List<Object> item : listValues) {
                        buttonValues.add((String) item.get(0));
                        if (rightSideSelectionValue != null && rightSideSelectionValue.equals(item.get(1))) {
                            itemModel.setRightText((String) item.get(0));
                        }
                    }

                    itemModel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            MultiButtonPopup popup = MultiButtonPopup.newInstance("SELECT", buttonValues);
                            popup.setOnButtonClickedListener(new MultiButtonPopup.OnButtonClickedListener() {
                                @Override
                                public void onButtonClicked(String buttonValue) {
                                    int index = buttonValues.indexOf(buttonValue);
                                    if (index == -1) {
                                        return;
                                    }

                                    List<Object> platformValues = listValues.get(index);
                                    if (platformValues == null || platformValues.isEmpty()) {
                                        return;
                                    }

                                    String address = itemModel.getAddressAssociatedTo();
                                    String selector = itemModel.getRightSideSelectionName();
                                    getController().updateSelectorForDevice(address, selector, platformValues.get(1));
                                    updateDefaultAdapter(false);
                                }
                            });
                            BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getName(), true);
                        }
                    });
                }
                catch (Exception ex) {
                    // No - Op.
                }
                break;

            default:
                // No - Op
                break;
        }
    }

    private void setNavButtonOnClickListeners() {
        leftActionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftSelected.set(true);
                leftActionText.setTextColor(getResources().getColor(R.color.overlay_white_with_100));
                rightActionText.setTextColor(getResources().getColor(R.color.black_with_20));
                deviceListHeading.setText(String.format(getString(sceneType.getHeadingText()), leftText.toLowerCase()));
                updateDefaultAdapter(false);
            }
        });
        rightActionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leftSelected.set(false);
                leftActionText.setTextColor(getResources().getColor(R.color.black_with_20));
                rightActionText.setTextColor(getResources().getColor(R.color.overlay_white_with_100));
                deviceListHeading.setText(String.format(getString(sceneType.getHeadingText()), rightText.toLowerCase()));
                updateDefaultAdapter(false);
            }
        });
    }

    private void displaySecurityActions() {
        SceneListItemModel listItemModel = new SceneListItemModel();
        listItemModel.setTitle(getString(R.string.scene_security_alarm_not_participating));
        listItemModel.setSubText(getString(R.string.scene_security_alarm_not_participating_text));
        listItemModel.setIsChecked(true);

        List<SceneListItemModel> modelsToSend = new ArrayList<>();
        modelsToSend.add(listItemModel);
        for (SceneListItemModel item : createModelsForList()){
            if (item.isChecked()) {
                modelsToSend.get(0).setIsChecked(false);
            }
            modelsToSend.add(item);
        }

        final SceneSecuritySelectorAdapter adapter = new SceneSecuritySelectorAdapter(getActivity(), modelsToSend, getController().isEditMode());
        deviceListView.setAdapter(adapter);
        deviceListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                adapter.toggleItem(position);
                SceneListItemModel item = adapter.getItem(position);
                if (item.getAddressAssociatedTo() == null) {
                    getController().commitModel(updateActions(null));
                } else {
                    Map<String, Map<String, Object>> context = new HashMap<>();
                    Map<String, Object> contextItems = new HashMap<>();
                    contextItems.put(item.getSceneSelectorName(), item.getSceneSelectorValue());
                    context.put(item.getAddressAssociatedTo(), contextItems);
                    getController().commitModel(updateActions(context));
                }

            }
        });
    }

    private void navigateToNumberPicker(final SceneListItemModel itemModel,
                                        final NumberPickerPopup.NumberPickerType type,
                                        final int min, final int max, final int defaultValue, final int step) {
        itemModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int defValue;
                try {
                    defValue = ((Number) itemModel.getRightSideSelectionValue()).intValue();
                } catch (Exception ex) {
                    defValue = defaultValue;
                }
                NumberPickerPopup popup = NumberPickerPopup.newInstance(type, min, max, defValue, step);
                popup.setOnValueChangedListener(new NumberPickerPopup.OnValueChangedListener() {
                    @Override
                    public void onValueChanged(int value) {
                        if (itemModel.isSeconds()) {
                            value *= 60;
                        }
                        getController().updateSelectorForDevice(itemModel.getAddressAssociatedTo(), itemModel.getRightSideSelectionName(), value);
                        updateDefaultAdapter(false);
                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getName(), true);
            }
        });
    }

    private void navigateToTimePicker(final SceneListItemModel itemModel, final int min, final int max, final int defaultValue) {
        itemModel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int defValue;
                try {
                    defValue = ((Number) itemModel.getRightSideSelectionValue()).intValue();
                }
                catch (Exception ex) {
                    defValue = defaultValue;
                }

                // Convert Default Value to Left Right
                int seconds = defValue;

                long minutes = seconds / 60;
                seconds -= minutes * 60;

                TimePickerPopup popup = TimePickerPopup.newInstance("TIME", "MIN", "SEC", (int) minutes, min, max, seconds, 0, 59, min * 60, max * 60);
                popup.setOnTimeChangedListener(new TimePickerPopup.OnTimeChangedListener() {
                    @Override
                    public void onTimeChanged(int leftValue, int rightValue) {

                    }

                    @Override
                    public void onAccept(int leftValue, int rightValue) {

                    }

                    @Override
                    public void onExit(int leftValue, int rightValue) {
                        int seconds = leftValue * 60 + rightValue;
                        getController().updateSelectorForDevice(itemModel.getAddressAssociatedTo(), itemModel.getRightSideSelectionName(), seconds);
                        updateDefaultAdapter(false);
                    }
                });

                BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getName(), true);
            }
        });
    }

    private @Nullable SceneDefaultSelectorAdapter getSceneAdapter() {
        Adapter adapter = deviceListView.getAdapter();
        if (adapter == null || !(adapter instanceof SceneDefaultSelectorAdapter)) {
            return null;
        }

        return (SceneDefaultSelectorAdapter) adapter;
    }

    @Override @Nullable
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_scene_action_list_with_header;
    }

    /// Controller bound methods?
    @SuppressWarnings({"unchecked"})
    public List<SceneListItemModel> createModelsForList() {
        List<SceneListItemModel> modelsToSend = new ArrayList<>();

        Map<String, List<Map<String, Object>>> items = getController().getActionTemplate().getSelectors();
        for (Map.Entry<String, List<Map<String, Object>>> actionItem : items.entrySet()) {
            for (Map<String, Object> i : actionItem.getValue()) {
                ActionSelector s = new ActionSelector(i);
                if (ActionSelectorType.LIST.equals(s.getType())) {
                    List<List<String>> type = (List<List<String>>) s.getValue();
                    for (List<String> t : type) {
                        SceneListItemModel innerAction = new SceneListItemModel();
                        innerAction.setAddressAssociatedTo(actionItem.getKey());
                        innerAction.setTitle(t.get(0));
                        innerAction.setSceneSelectorValue(t.get(1));
                        innerAction.setSceneSelectorName(s.getName());
                        Object value = getController().getSelectorForDevice(actionItem.getKey(), s.getName());
                        innerAction.setIsChecked(value != null && value.equals(t.get(1)));
                        modelsToSend.add(innerAction);
                    }
                }
            }
        }

        return modelsToSend;
    }

    /**
     *
     * Updates, or adds, the provided context to the list of context variables for this template.
     * Returns a list of remaining settings to set on the scene model and commit to the platform.
     *
     * If the provided list is null, it will remove it from the context.
     *
     * @return
     */
    private List<Map<String, Object>> updateActions(@Nullable Map<String, Map<String, Object>> context) {
        SceneModel model = getController().getSceneModel();
        if (model == null) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> newValues = new LinkedList<>();
        if (context != null && !context.isEmpty()) { // Still have stuff to save?
            Action action = new Action();
            action.setTemplate(getController().getActionTemplate().getId());
            action.setName(getController().getActionTemplate().getName());
            action.setContext(context);
            newValues.add(action.toMap());
        }

        for (Map<String, Object> actionsModel : model.getActions()) { // Rebuild save list
            Action actionName = new Action(actionsModel);
            if (!getController().getActionTemplate().getId().equals(actionName.getTemplate())) {
                newValues.add(actionName.toMap());
            }
        }

        return newValues;
    }

    private @Nullable Action getActionsFromModel() {
        SceneModel model = getController().getSceneModel();
        if (model == null) {
            return null;
        }

        for (Map<String, Object> action : model.getActions()) {
            Action concreteAction = new Action(action);
            if (concreteAction.getTemplate().equals(getController().getActionTemplate().getId())) {
                return concreteAction;
            }
        }

        return null;
    }

    private String getPercentAbstract(Number value) {
        return String.format("%d %%", value.intValue());
    }

    private String getDurationAbstract(int seconds) {
        return StringUtils.getDurationString(seconds);
    }

    private String getThermostatAbstract(DeviceType type, ThermostatAction action) {
        ThermostatOperatingMode mode = ThermostatOperatingMode.fromPlatformValue(action.getMode());
        int coolSetpointF = action.getCoolSetPoint() == null ? 0 : TemperatureUtils.roundCelsiusToFahrenheit(action.getCoolSetPoint());
        int heatSetpointF = action.getHeatSetPoint() == null ? 0 : TemperatureUtils.roundCelsiusToFahrenheit(action.getHeatSetPoint());

        String coolSetpointString = getString(R.string.temperature_degrees, coolSetpointF);
        String heatSetpointString = getString(R.string.temperature_degrees, heatSetpointF);
        String modeString = getString(mode.getStringResId(type == DeviceType.NEST_THERMOSTAT));

        switch (mode) {
            case HEAT: return modeString + ": " + heatSetpointString;
            case COOL: return modeString + ": " + coolSetpointString;
            case AUTO: return modeString + ": " + heatSetpointString + "-" + coolSetpointString;
            default: return modeString;
        }

    }
}
