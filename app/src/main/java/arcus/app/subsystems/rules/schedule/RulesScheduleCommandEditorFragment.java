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
package arcus.app.subsystems.rules.schedule;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.popups.ButtonListPopup;
import arcus.app.common.popups.RuleChooseActivePopup;
import arcus.app.common.schedule.AbstractScheduleCommandEditorFragment;
import arcus.app.common.schedule.controller.ScheduleCommandEditController;
import arcus.app.common.schedule.model.ScheduleCommandModel;
import arcus.app.common.utils.StringUtils;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.style.OnClickActionSetting;
import arcus.app.subsystems.rules.schedule.model.RulesCommand;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;


public class RulesScheduleCommandEditorFragment extends AbstractScheduleCommandEditorFragment implements ScheduleCommandEditController.Callbacks, RuleChooseActivePopup.Callback {

    private final static String DEVICE_NAME = "DEVICE_NAME";
    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TIME_OF_DAY_COMMAND_ID = "TIME_OF_DAY_COMMAND_ID";
    private final static String CURRENT_DAY_OF_WEEK = "CURRENT_DAY_OF_WEEK";
    private final static String ACTIVE_STATE = "ACTIVE_STATE";

    private CountDownLatch countDownLatch;

    private RulesCommand rulesCommand = new RulesCommand();
    private OnClickActionSetting stateSetting;

    public static RulesScheduleCommandEditorFragment newEditEventInstance(String deviceAddress, String deviceName, String timeOfDayCommandId, DayOfWeek currentDayOfWeek, RulesCommand.State state) {
        RulesScheduleCommandEditorFragment instance = new RulesScheduleCommandEditorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        arguments.putString(TIME_OF_DAY_COMMAND_ID, timeOfDayCommandId);
        arguments.putSerializable(CURRENT_DAY_OF_WEEK, currentDayOfWeek);
        arguments.putSerializable(ACTIVE_STATE, state);
        instance.setArguments(arguments);

        return instance;
    }

    public static RulesScheduleCommandEditorFragment newAddEventInstance(String deviceAddress, String deviceName, DayOfWeek currentDayOfWeek, RulesCommand.State state) {
        RulesScheduleCommandEditorFragment instance = new RulesScheduleCommandEditorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        arguments.putSerializable(CURRENT_DAY_OF_WEEK, currentDayOfWeek);
        arguments.putSerializable(ACTIVE_STATE, state);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        countDownLatch = new CountDownLatch(1); // TODO: 3/18/16 Why aren't we just disabling the button while in save mode?
        rulesCommand.setId(getTimeOfDayCommandId());
        rulesCommand.setDays(EnumSet.of(getCurrentDayOfWeek()));
        rulesCommand.setRulesCommandState(getStateActiveInactive());
        if (view != null) {
            TextView ruleTV = (TextView) view.findViewById(R.id.rule_schedule_top_text);
            if (ruleTV != null) {
                ruleTV.setVisibility(View.VISIBLE);
                ruleTV.setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);
            }
        }

        setDeleteButtonVisibility(isEditMode() ? View.VISIBLE : View.GONE);

        return view;
    }


    @Override
    public void onResume() {
        super.onResume();

        ScheduleCommandEditController.getInstance().setListener(this);
        if (isEditMode()) {
            ScheduleCommandEditController.getInstance().loadCommand(getScheduledEntityAddress(), getTimeOfDayCommandId(), new RulesCommand());
        }
    }

    @Override
    public DayOfWeek getCurrentDayOfWeek() {
        return (DayOfWeek) getArguments().getSerializable(CURRENT_DAY_OF_WEEK);
    }

    @Override
    public List<DayOfWeek> getScheduledDaysOfWeek() {
        return new ArrayList<>(rulesCommand.getDays());
    }

    @Override
    public TimeOfDay getScheduledTimeOfDay() {
        return rulesCommand.getTime();
    }

    @Override
    public boolean isEditMode() {
        return getTimeOfDayCommandId() != null;
    }

    @Override
    public SettingsList getEditableCommandAttributes() {
        SettingsList settings = new SettingsList();

        // Always show the "STATE" option
        settings.add(buildStateSetting());

        // ... optionally show the "REPEAT ON" option when a repeat schedule is defined
        if (rulesCommand.getDays().size() > 1) {
            settings.add(buildRepeatSetting());
        }

        return settings;
    }

    @Override
    public void onDeleteEvent() {
        if (countDownLatch.getCount() > 0) {
            countDownLatch.countDown();
            ScheduleCommandEditController.getInstance().deleteCommand(getScheduledEntityAddress(), rulesCommand);
        }
    }

    @Override
    public void onRepeatChanged(Set repeatDays) {
        rulesCommand.setDays(repeatDays);
        setRepeatRegionVisibility(repeatDays.size() > 1 ? View.GONE : View.VISIBLE);

        rebind();
    }

    @Override
    public void onSaveEvent(EnumSet selectedDays, TimeOfDay timeOfDay) {
        if (countDownLatch.getCount() > 0) {
            countDownLatch.countDown();

            rulesCommand.setDays(selectedDays);
            rulesCommand.setTime(timeOfDay);

            activeInactiveSelected(getCurrentStateActiveInactive());

            if (isEditMode()) {
                ScheduleCommandEditController.getInstance().updateCommandMessageType(getScheduledEntityAddress(), rulesCommand);
            } else {
                ScheduleCommandEditController.getInstance().addCommand(getScheduledEntityAddress(), rulesCommand);
            }
        }

    }

    @Override
    public void onSchedulerError(Throwable cause) {
        countDownLatch = new CountDownLatch(1);
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override
    public void onTimeOfDayCommandLoaded(ScheduleCommandModel scheduleCommandModel) {
        hideProgressBar();
        this.rulesCommand = (RulesCommand) scheduleCommandModel;

        setRepeatRegionVisibility(rulesCommand.getDays().size() > 1 ? View.GONE : View.VISIBLE);
        setSelectedDays(rulesCommand.getDaysAsEnumSet());

        rebind();
    }

    @Override
    public void onSuccess() {
        countDownLatch = new CountDownLatch(1);
        hideProgressBar();
        goBack();
    }

    private Setting buildRepeatSetting() {
        String repeatAbstract = StringUtils.getScheduleAbstract(getActivity(), rulesCommand.getDays());
        OnClickActionSetting repeatSetting = new OnClickActionSetting(getString(R.string.doors_and_locks_repeat_on), null, repeatAbstract);
        repeatSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepeatPicker();
            }
        });

        return repeatSetting;
    }

    private Setting buildStateSetting() {
        stateSetting = new OnClickActionSetting(getString(R.string.doors_and_locks_state), null, rulesCommand.getCommandAbstract());
        stateSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideProgressBar();
                promptUserForStateSelection();
            }
        });

        return stateSetting;
    }

    private void promptUserForStateSelection() {
        RuleChooseActivePopup ruleChooseActivePopup = RuleChooseActivePopup.newInstance(rulesCommand.getRawState());
        ruleChooseActivePopup.setCallback(this);
        BackstackManager.getInstance().navigateToFloatingFragment(ruleChooseActivePopup, ruleChooseActivePopup.getClass().getSimpleName(), true);
    }

    public String getScheduledEntityAddress() {
        return getArguments().getString(DEVICE_ADDRESS);
    }

    private String getTimeOfDayCommandId() {
        return getArguments().getString(TIME_OF_DAY_COMMAND_ID, null);
    }


    private RulesCommand.State getStateActiveInactive() {
        Serializable serializable = getArguments().getSerializable(ACTIVE_STATE);
        if (serializable == null) {
            return RulesCommand.State.ACTIVE;
        }
        return (RulesCommand.State) serializable;
    }

    private RulesCommand.State getCurrentStateActiveInactive() {
        return rulesCommand.getRawState();
    }


    @Override
    public void activeInactiveSelected(RulesCommand.State state) {

        if (state == RulesCommand.State.ACTIVE) {

            rulesCommand.setRulesCommandState(RulesCommand.State.ACTIVE);
        } else {
            rulesCommand.setRulesCommandState(RulesCommand.State.INACTIVE);
        }
        rebind();

    }

    @Override public void onConfirmUpdateAllDays(final ScheduleCommandModel scheduleCommandModel) {
        hideProgressBar();

        final String EDIT_ALL = "EDIT_ALL";
        final String EDIT_SELECTED = "EDIT_SELECTED";

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
                    ScheduleCommandEditController.getInstance().updateCommandMessageTypeAllDays(getScheduledEntityAddress(), scheduleCommandModel);
                }
                else {
                    ScheduleCommandEditController.getInstance().updateCommandMessageTypeSingleDay(getScheduledEntityAddress(), scheduleCommandModel, getCurrentDayOfWeek());
                }
                BackstackManager.getInstance().navigateBack();
            }
        });

        BackstackManager.getInstance().navigateToFloatingFragment(editWhichDayPopup, editWhichDayPopup.getClass().getSimpleName(), true);
    }
}
