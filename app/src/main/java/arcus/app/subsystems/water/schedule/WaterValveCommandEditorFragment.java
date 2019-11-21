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
package arcus.app.subsystems.water.schedule;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.popups.ButtonListPopup;
import arcus.app.common.schedule.AbstractScheduleCommandEditorFragment;
import arcus.app.common.schedule.controller.ScheduleCommandEditController;
import arcus.app.common.schedule.model.ScheduleCommandModel;
import arcus.app.common.utils.StringUtils;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.style.OnClickActionSetting;
import arcus.app.subsystems.water.schedule.model.WaterValveCommand;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class WaterValveCommandEditorFragment extends AbstractScheduleCommandEditorFragment implements ScheduleCommandEditController.Callbacks {

    private final static String DEVICE_NAME = "DEVICE_NAME";
    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TIME_OF_DAY_COMMAND_ID = "TIME_OF_DAY_COMMAND_ID";
    private final static String CURRENT_DAY_OF_WEEK = "CURRENT_DAY_OF_WEEK";

    private WaterValveCommand waterValveCommand = new WaterValveCommand();

    public static WaterValveCommandEditorFragment newEditEventInstance (String deviceAddress, String deviceName, String timeOfDayCommandId, DayOfWeek currentDayOfWeek) {
        WaterValveCommandEditorFragment instance = new WaterValveCommandEditorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        arguments.putString(TIME_OF_DAY_COMMAND_ID, timeOfDayCommandId);
        arguments.putSerializable(CURRENT_DAY_OF_WEEK, currentDayOfWeek);
        instance.setArguments(arguments);

        return instance;
    }

    public static WaterValveCommandEditorFragment newAddEventInstance (String deviceAddress, String deviceName, DayOfWeek currentDayOfWeek) {
        WaterValveCommandEditorFragment instance = new WaterValveCommandEditorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        arguments.putSerializable(CURRENT_DAY_OF_WEEK, currentDayOfWeek);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        waterValveCommand.setId(getTimeOfDayCommandId());
        waterValveCommand.setDays(EnumSet.of(getCurrentDayOfWeek()));
        waterValveCommand.setWaterValveState(true);
        setDeleteButtonVisibility(isEditMode() ? View.VISIBLE : View.GONE);

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();

        ScheduleCommandEditController.getInstance().setListener(this);
        if (isEditMode()) {
            ScheduleCommandEditController.getInstance().loadCommand(getScheduledEntityAddress(), getTimeOfDayCommandId(), new WaterValveCommand());
        }
    }

    @Override
    public String getTitle () {
        return getDeviceName();
    }

    @Override
    public DayOfWeek getCurrentDayOfWeek() {
        return (DayOfWeek) getArguments().getSerializable(CURRENT_DAY_OF_WEEK);
    }

    @Override
    public List<DayOfWeek> getScheduledDaysOfWeek() {
        return new ArrayList<>(waterValveCommand.getDays());
    }

    @Override
    public TimeOfDay getScheduledTimeOfDay() {
        return waterValveCommand.getTime();
    }

    @Override
    public boolean isEditMode() {
        return getTimeOfDayCommandId() != null;
    }

    @Override
    public SettingsList getEditableCommandAttributes() {
        SettingsList settings = new SettingsList();

         settings.add(buildFanSpeedSetting());

        if (waterValveCommand.getDays().size() > 1) {
            settings.add(buildRepeatSetting());
        }

        return settings;
    }

    @Override
    public void onDeleteEvent() {
        ScheduleCommandEditController.getInstance().deleteCommand(getScheduledEntityAddress(), waterValveCommand);
    }

    @Override
    public String getScheduledEntityAddress() {
        return getArguments().getString(DEVICE_ADDRESS);
    }

    @Override
    public void onRepeatChanged(Set repeatDays) {
        waterValveCommand.setDays(repeatDays);
        setRepeatRegionVisibility(repeatDays.size() > 1 ? View.GONE : View.VISIBLE);

        rebind();
    }

    @Override
    public void onSaveEvent(EnumSet selectedDays, TimeOfDay timeOfDay) {
        waterValveCommand.setDays(selectedDays);
        waterValveCommand.setTime(timeOfDay);

        if (isEditMode()) {
            ScheduleCommandEditController.getInstance().updateCommand(getScheduledEntityAddress(), waterValveCommand);
        } else {
            ScheduleCommandEditController.getInstance().addCommand(getScheduledEntityAddress(), waterValveCommand);
        }
    }

    @Override
    public void onSchedulerError(Throwable cause) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override
    public void onTimeOfDayCommandLoaded(ScheduleCommandModel scheduleCommandModel) {
        hideProgressBar();
        this.waterValveCommand = (WaterValveCommand) scheduleCommandModel;

        setRepeatRegionVisibility(waterValveCommand.getDays().size() > 1 ? View.GONE : View.VISIBLE);
        setSelectedDays(waterValveCommand.getDaysAsEnumSet());

        rebind();
    }



    private Setting buildFanSpeedSetting () {
        String stateAbstract = waterValveCommand.getWaterValveState();
        OnClickActionSetting stateSetting = new OnClickActionSetting(getString(R.string.setting_valve_state), null, stateAbstract);
        stateSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptUserForStateSelection();
            }
        });

        return stateSetting;
    }

    private void promptUserForStateSelection () {
        HashMap<String,String> editChoices = new HashMap<>();
        editChoices.put("OPEN", "OPEN");
        editChoices.put("CLOSED", "CLOSED");

        ButtonListPopup editWhichDayPopup = ButtonListPopup.newInstance(editChoices, R.string.setting_valve_state, -1);

        editWhichDayPopup.setCallback(new ButtonListPopup.Callback() {
            @Override
            public void buttonSelected(String buttonKeyValue) {
                BackstackManager.getInstance().navigateBack();
                waterValveCommand.setWaterValveState(buttonKeyValue.contains("OPEN") ? true : false);
                rebind();
            }
        });

        BackstackManager.getInstance().navigateToFloatingFragment(editWhichDayPopup, editWhichDayPopup.getClass().getSimpleName(), true);
    }

    @Override
    public void onSuccess() {
        hideProgressBar();
        goBack();
    }

    private Setting buildRepeatSetting () {
        String repeatAbstract = StringUtils.getScheduleAbstract(getActivity(), waterValveCommand.getDays());
        OnClickActionSetting repeatSetting = new OnClickActionSetting(getString(R.string.doors_and_locks_repeat_on), null, repeatAbstract);
        repeatSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepeatPicker();
            }
        });

        return repeatSetting;
    }

    private String getDeviceName() {
        return getArguments().getString(DEVICE_NAME);
    }

    private String getTimeOfDayCommandId() {
        return getArguments().getString(TIME_OF_DAY_COMMAND_ID, null);
    }
}
