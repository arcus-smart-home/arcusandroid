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
package arcus.app.subsystems.climate.schedule;

import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.capability.SpaceHeater;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.popups.ButtonListPopup;
import arcus.app.common.popups.NumberPickerPopup;
import arcus.app.common.schedule.AbstractScheduleCommandEditorFragment;
import arcus.app.common.schedule.controller.ScheduleCommandEditController;
import arcus.app.common.schedule.model.ScheduleCommandModel;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.style.OnClickActionSetting;
import arcus.app.subsystems.climate.schedule.model.SpaceHeaterCommand;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class SpaceHeaterScheduleCommandEditorFragment extends AbstractScheduleCommandEditorFragment implements ScheduleCommandEditController.Callbacks {

    private final static String DEVICE_NAME = "DEVICE_NAME";
    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TIME_OF_DAY_COMMAND_ID = "TIME_OF_DAY_COMMAND_ID";
    private final static String CURRENT_DAY_OF_WEEK = "CURRENT_DAY_OF_WEEK";

    private SpaceHeaterCommand twinStarCommand = new SpaceHeaterCommand();

    public static SpaceHeaterScheduleCommandEditorFragment newEditEventInstance (String deviceAddress, String deviceName, String timeOfDayCommandId, DayOfWeek currentDayOfWeek) {
        SpaceHeaterScheduleCommandEditorFragment instance = new SpaceHeaterScheduleCommandEditorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        arguments.putString(TIME_OF_DAY_COMMAND_ID, timeOfDayCommandId);
        arguments.putSerializable(CURRENT_DAY_OF_WEEK, currentDayOfWeek);
        instance.setArguments(arguments);

        return instance;
    }

    public static SpaceHeaterScheduleCommandEditorFragment newAddEventInstance (String deviceAddress, String deviceName, DayOfWeek currentDayOfWeek) {
        SpaceHeaterScheduleCommandEditorFragment instance = new SpaceHeaterScheduleCommandEditorFragment();
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

        Version1TextView title = (Version1TextView) view.findViewById(R.id.rule_schedule_top_text);
        title.setVisibility(View.VISIBLE);
        title.setText(getString(R.string.spaceheater_add_event_title));
        title.setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);

        twinStarCommand.setId(getTimeOfDayCommandId());
        twinStarCommand.setDays(EnumSet.of(getCurrentDayOfWeek()));
        twinStarCommand.setState(SpaceHeater.HEATSTATE_OFF);
        twinStarCommand.setSetPoint(75.0);
        setDeleteButtonVisibility(isEditMode() ? View.VISIBLE : View.GONE);

        return view;
    }


    @Override
    public void onResume () {
        super.onResume();

        ScheduleCommandEditController.getInstance().setListener(this);
        if (isEditMode()) {
            ScheduleCommandEditController.getInstance().loadCommand(getScheduledEntityAddress(), getTimeOfDayCommandId(), new SpaceHeaterCommand());
        }
        rebind(true, getActivity().getString(R.string.scene_start_time), null);
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
        return new ArrayList<>(twinStarCommand.getDays());
    }

    @Override
    public TimeOfDay getScheduledTimeOfDay() {
        return twinStarCommand.getTime();
    }

    @Override
    public boolean isEditMode() {
        return getTimeOfDayCommandId() != null;
    }

    @Override
    public SettingsList getEditableCommandAttributes() {
        SettingsList settings = new SettingsList();

        settings.add(buildSpaceHeaterOnOffSetting());

        if("ON".equals(twinStarCommand.getState())) {
            tapCopy.setText(getString(R.string.spaceheater_temp_schedule_repeat_text));
            settings.add(buildTemperatureSetting());
        }
        else {
            tapCopy.setText(getString(R.string.climate_schedule_repeat_text));
        }

        if (twinStarCommand.getDays().size() > 1) {
            settings.add(buildRepeatSetting());
        }

        return settings;
    }

    @Override
    public void onDeleteEvent() {
        ScheduleCommandEditController.getInstance().deleteCommand(getScheduledEntityAddress(), twinStarCommand);
    }

    @Override
    public String getScheduledEntityAddress() {
        return getArguments().getString(DEVICE_ADDRESS);
    }

    @Override
    public void onRepeatChanged(Set repeatDays) {
        twinStarCommand.setDays(repeatDays);
        setRepeatRegionVisibility(repeatDays.size() > 1 ? View.GONE : View.VISIBLE);

        rebind(true, getActivity().getString(R.string.scene_start_time), null);
    }

    @Override
    public void onSaveEvent(EnumSet selectedDays, TimeOfDay timeOfDay) {
        twinStarCommand.setDays(selectedDays);
        twinStarCommand.setTime(timeOfDay);

        if (isEditMode()) {
            ScheduleCommandEditController.getInstance().updateCommand(getScheduledEntityAddress(), twinStarCommand);
        } else {
            ScheduleCommandEditController.getInstance().addCommand(getScheduledEntityAddress(), twinStarCommand);
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
        this.twinStarCommand = (SpaceHeaterCommand) scheduleCommandModel;

        setRepeatRegionVisibility(twinStarCommand.getDays().size() > 1 ? View.GONE : View.VISIBLE);
        setSelectedDays(twinStarCommand.getDaysAsEnumSet());

        // Redraw the screen with the updated command values
        rebind(true, getActivity().getString(R.string.scene_start_time), null);
    }

    @Override
    public void onSuccess() {
        hideProgressBar();
        goBack();
    }

    private Setting buildRepeatSetting () {
        String repeatAbstract = StringUtils.getScheduleAbstract(getActivity(), twinStarCommand.getDays());
        OnClickActionSetting repeatSetting = new OnClickActionSetting(getString(R.string.doors_and_locks_repeat_on), null, repeatAbstract);
        repeatSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepeatPicker();
            }
        });

        return repeatSetting;
    }

    private Setting buildSpaceHeaterOnOffSetting () {
        String stateAbstract = twinStarCommand.getState();
        OnClickActionSetting stateSetting = new OnClickActionSetting(getString(R.string.setting_state), null, stateAbstract);
        stateSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptUserForStateSelection();
            }
        });

        return stateSetting;
    }

    private Setting buildTemperatureSetting () {
        String stateAbstract = Integer.toString((int)twinStarCommand.getSetPoint()) + getString(R.string.degree_symbol);
        OnClickActionSetting stateSetting = new OnClickActionSetting(getString(R.string.temperature), null, stateAbstract);
        stateSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptUserForTemperatureSelection();
            }
        });

        return stateSetting;
    }

    private void promptUserForStateSelection () {
        HashMap<String,String> editChoices = new HashMap<>();
        editChoices.put(SpaceHeater.HEATSTATE_OFF, SpaceHeater.HEATSTATE_OFF);
        editChoices.put(SpaceHeater.HEATSTATE_ON, SpaceHeater.HEATSTATE_ON);

        ButtonListPopup editStateSelectionPopup = ButtonListPopup.newInstance(editChoices, R.string.hvac_mode_selection, -1);

        editStateSelectionPopup.setCallback(new ButtonListPopup.Callback() {
            @Override
            public void buttonSelected(String buttonKeyValue) {
                BackstackManager.getInstance().navigateBack();
                twinStarCommand.setState(buttonKeyValue);
                rebind(true, getActivity().getString(R.string.scene_start_time), null);
            }
        });

        BackstackManager.getInstance().navigateToFloatingFragment(editStateSelectionPopup, editStateSelectionPopup.getClass().getSimpleName(), true);
    }

    private void promptUserForTemperatureSelection() {
        NumberPickerPopup popup = NumberPickerPopup.newInstance(NumberPickerPopup.NumberPickerType.MIN_MAX,
                twinStarCommand.getMinTemperature(), twinStarCommand.getMaxTemperature(), (int)twinStarCommand.getSetPoint());
        popup.setOnValueChangedListener(new NumberPickerPopup.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                twinStarCommand.setSetPoint(value);
                rebind(true, getActivity().getString(R.string.scene_start_time), null);
            }
        });
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    private String getDeviceName() {
        return getArguments().getString(DEVICE_NAME);
    }

    private String getTimeOfDayCommandId() {
        return getArguments().getString(TIME_OF_DAY_COMMAND_ID, null);
    }
}
