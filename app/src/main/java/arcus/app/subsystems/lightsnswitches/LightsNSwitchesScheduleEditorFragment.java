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

import android.os.Bundle;
import android.view.View;

import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.lightsnswitches.model.LightsNSwitchesScheduleDay;
import arcus.cornea.subsystem.lightsnswitches.schedule.LightsNSwitchesEditController;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.schedule.AbstractScheduleCommandEditorFragment;
import arcus.app.common.popups.ButtonListPopup;
import arcus.app.common.popups.DayOfTheWeekPopup;
import arcus.app.common.popups.NumberPickerPopup;
import arcus.app.common.utils.StringUtils;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.style.BinarySetting;
import arcus.app.device.settings.style.OnClickActionSetting;
import arcus.app.subsystems.alarm.AlertFloatingFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class LightsNSwitchesScheduleEditorFragment extends AbstractScheduleCommandEditorFragment implements LightsNSwitchesEditController.Callback {

    private final static Logger logger = LoggerFactory.getLogger(LightsNSwitchesScheduleEditorFragment.class);

    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String CURRENT_DAY_OF_WEEK = "CURRENT_DAY_OF_WEEK";
    private final static String SCHEDULE_EVENT = "SCHEDULE_EVENT";

    private static final String EDIT_SELECTED = "EDIT_SELECTED";
    private static final String EDIT_ALL = "EDIT_ALL";

    private final static int MIN_SETTABLE_PERCENTAGE = 10;
    private final static int MAX_PERCENTAGE = 100;
    private final static int STEP_PERCENTAGE = 10;

    private List<DayOfWeek> scheduledDaysOfWeek;
    private TimeOfDay scheduledTimeOfDay;
    private SettingsList editableEventProperties = new SettingsList();
    private LightsNSwitchesScheduleDay scheduledDay;

    public static LightsNSwitchesScheduleEditorFragment newAddEventInstance(String deviceAddress, DayOfWeek currentDayOfWeek) {
        LightsNSwitchesScheduleEditorFragment instance = new LightsNSwitchesScheduleEditorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(CURRENT_DAY_OF_WEEK, currentDayOfWeek.toString());

        instance.setArguments(arguments);
        return instance;
    }

    public static LightsNSwitchesScheduleEditorFragment newEditEventInstance(String deviceAddress, DayOfWeek editingDay, LightsNSwitchesScheduleDay event) {
        LightsNSwitchesScheduleEditorFragment instance = new LightsNSwitchesScheduleEditorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(CURRENT_DAY_OF_WEEK, editingDay.toString());
        arguments.putParcelable(SCHEDULE_EVENT, event);

        instance.setArguments(arguments);
        return instance;
    }

    public void onResume() {
        super.onResume();

        setDeleteButtonVisibility(isEditMode() ? View.VISIBLE : View.GONE);

        if (getScheduleEvent() != null) {
            setSelectedDays(EnumSet.copyOf(getScheduleEvent().getRepeatsOn()));
        }

        if (isEditMode()) {
            LightsNSwitchesEditController.instance().edit(getCurrentDayOfWeek(), getScheduleEvent(), getScheduledEntityAddress(), this);
        } else {
            LightsNSwitchesEditController.instance().add(getCurrentDayOfWeek(), getScheduledEntityAddress(), this);
        }
    }

    @Override
    public DayOfWeek getCurrentDayOfWeek() {
        return DayOfWeek.valueOf(getArguments().getString(CURRENT_DAY_OF_WEEK, DayOfWeek.SUNDAY.toString()));
    }

    @Override
    public List<DayOfWeek> getScheduledDaysOfWeek() {
        return scheduledDaysOfWeek;
    }

    @Override
    public TimeOfDay getScheduledTimeOfDay() {
        return scheduledTimeOfDay;
    }

    @Override
    public boolean isEditMode() {
        return getArguments().getParcelable(SCHEDULE_EVENT) != null;
    }

    @Override
    public SettingsList getEditableCommandAttributes() {

        editableEventProperties.clear();

        if (scheduledDay != null && scheduledDay.isSwitchable()) {
            editableEventProperties.add(buildSwitchStateSetting());
        }

        if (scheduledDay != null && scheduledDay.isDimmable() && scheduledDay.isOn()) {
            editableEventProperties.add(buildBrightnessSetting());
        }

        if (scheduledDay != null && scheduledDay.isRepeating()) {
            editableEventProperties.add(buildRepeatSetting());
        }

        return editableEventProperties;
    }

    @Override
    public void onDeleteEvent() {
        LightsNSwitchesEditController.instance().delete();
    }

    @Override
    public void onRepeatChanged (Set repeatDays) {
        LightsNSwitchesEditController.instance().setRepeatsOn(repeatDays);
    }

    @Override public void onSaveEvent(EnumSet selectedDays, TimeOfDay timeOfDay) {
        if (scheduledDay.isSwitchable()) {
            LightsNSwitchesEditController.instance().setSwitchStateOn(scheduledDay.isOn());
        }

        if (scheduledDay.isDimmable()) {
            LightsNSwitchesEditController.instance().setDimPercent(scheduledDay.getDimPercentage());
        }

        LightsNSwitchesEditController.instance().setRepeatsOn(selectedDays);
        LightsNSwitchesEditController.instance().setScheduleTime(timeOfDay);
        LightsNSwitchesEditController.instance().save();
    }

    @Override
    public void showAdd(DayOfWeek day, LightsNSwitchesScheduleDay scheduledDay) {
        showEdit(day, scheduledDay);
    }

    @Override
    public void showEdit(DayOfWeek day, final LightsNSwitchesScheduleDay scheduledDay) {
        this.scheduledDay = scheduledDay;
        scheduledDaysOfWeek = new ArrayList<>(scheduledDay.getRepeatsOn());
        scheduledTimeOfDay = scheduledDay.getTimeOfDay();

        setRepeatRegionVisibility(scheduledDay.isRepeating() ? View.GONE : View.VISIBLE);

        rebind();
    }

    @Override
    public void onError(ErrorModel error) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(new RuntimeException((error.getMessage())));
    }

    @Override
    public void makingRequest() {
        showProgressBar();
    }

    @Override
    public void promptEditWhichDay() {
        HashMap<String,String> editChoices = new HashMap<>();
        editChoices.put(getString(R.string.climate_edit_all_days), EDIT_ALL);
        editChoices.put(getString(R.string.climate_edit_selected_day), EDIT_SELECTED);

        ButtonListPopup editWhichDayPopup = ButtonListPopup.newInstance(
                editChoices,
                R.string.climate_edit_event_title,
                R.string.climate_edit_event_description);

        editWhichDayPopup.setCallback(new ButtonListPopup.Callback() {
            @Override
            public void buttonSelected(String buttonKeyValue) {
                if (EDIT_ALL.equals(buttonKeyValue)) {
                    LightsNSwitchesEditController.instance().saveAllDays();
                } else {
                    LightsNSwitchesEditController.instance().saveSelectedDay();
                }
                BackstackManager.getInstance().navigateBack();
            }
        });

        BackstackManager.getInstance().navigateToFloatingFragment(editWhichDayPopup, editWhichDayPopup.getClass().getSimpleName(), true);
    }

    @Override
    public void promptDeleteWhichDay() {

        AlertFloatingFragment deleteWhichDayPrompt = AlertFloatingFragment.newInstance(
                getString(R.string.climate_edit_event_error_title),
                getString(R.string.climate_edit_event_error_description),
                getString(R.string.climate_edit_selected_day),
                getString(R.string.climate_edit_all_days),
                new AlertFloatingFragment.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        LightsNSwitchesEditController.instance().deleteSelectedDay();
                        return true;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        LightsNSwitchesEditController.instance().deleteAllDays();
                        return true;
                    }
                }
        );

        BackstackManager.getInstance().navigateToFloatingFragment(deleteWhichDayPrompt, deleteWhichDayPrompt.getClass().getSimpleName(), true);
    }

    @Override
    public void onRequestComplete() {
        hideProgressBar();
        goBack();
    }

    private LightsNSwitchesScheduleDay getScheduleEvent () {
        return getArguments().getParcelable(SCHEDULE_EVENT);
    }

    @Override
    public String getScheduledEntityAddress() {
        return getArguments().getString(DEVICE_ADDRESS);
    }

    private Setting buildRepeatSetting () {
        OnClickActionSetting repeatSetting = new OnClickActionSetting(getString(R.string.lightsnswitches_repeat_on), null, StringUtils.getScheduleAbstract(getActivity(), scheduledDay.getRepeatsOn()), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DayOfTheWeekPopup picker = DayOfTheWeekPopup.newInstance(EnumSet.copyOf(scheduledDay.getRepeatsOn()));
                picker.setCallback(new DayOfTheWeekPopup.Callback() {
                    @Override
                    public void selectedItems(EnumSet<DayOfWeek> dayOfWeek) {
                        setSelectedDays(dayOfWeek);     // Let the parent fragment know about the selected days, otherwise when it calls onSaveEvent() it's gonna pass us bogus values
                        LightsNSwitchesEditController.instance().setRepeatsOn(dayOfWeek);
                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(picker, picker.getClass().getCanonicalName(), true);
            }
        });

        return repeatSetting;
    }

    private Setting buildBrightnessSetting () {
        if (scheduledDay.getDimPercentage() == 0) {
            scheduledDay.setDimPercentage(MIN_SETTABLE_PERCENTAGE);
        }

        OnClickActionSetting brightnessSetting = new OnClickActionSetting(getString(R.string.lightsnswitches_brightness), null, getString(R.string.lightsnswitches_percentage, scheduledDay.getDimPercentage()), new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                NumberPickerPopup percentPicker = NumberPickerPopup.newInstance(NumberPickerPopup.NumberPickerType.PERCENT, MIN_SETTABLE_PERCENTAGE, MAX_PERCENTAGE, scheduledDay.getDimPercentage(), STEP_PERCENTAGE);
                percentPicker.setOnValueChangedListener(new NumberPickerPopup.OnValueChangedListener() {
                    @Override
                    public void onValueChanged(int value) {
                        LightsNSwitchesEditController.instance().setDimPercent(value);
                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(percentPicker, percentPicker.getClass().getSimpleName(), true);
            }
        });

        return brightnessSetting;
    }

    private Setting buildSwitchStateSetting () {
        BinarySetting stateSetting = new BinarySetting(getString(R.string.lightsnswitches_state), null, scheduledDay.isOn());
        stateSetting.addListener(new SettingChangedParcelizedListener() {
            @Override
            public void onSettingChanged(Setting setting, Object newValue) {
                LightsNSwitchesEditController.instance().setSwitchStateOn((boolean) newValue);
            }
        });

        return stateSetting;
    }
}
