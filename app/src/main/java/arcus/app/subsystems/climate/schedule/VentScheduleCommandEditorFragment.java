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
import arcus.app.common.popups.NumberPickerPopup;
import arcus.app.common.schedule.AbstractScheduleCommandEditorFragment;
import arcus.app.common.schedule.controller.ScheduleCommandEditController;
import arcus.app.common.schedule.model.ScheduleCommandModel;
import arcus.app.common.utils.StringUtils;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.style.OnClickActionSetting;
import arcus.app.subsystems.climate.schedule.model.VentCommand;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;


public class VentScheduleCommandEditorFragment extends AbstractScheduleCommandEditorFragment implements ScheduleCommandEditController.Callbacks {

    private final static String DEVICE_NAME = "DEVICE_NAME";
    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TIME_OF_DAY_COMMAND_ID = "TIME_OF_DAY_COMMAND_ID";
    private final static String CURRENT_DAY_OF_WEEK = "CURRENT_DAY_OF_WEEK";

    private VentCommand ventCommand = new VentCommand();

    public static VentScheduleCommandEditorFragment newEditEventInstance (String deviceAddress, String deviceName, String timeOfDayCommandId, DayOfWeek currentDayOfWeek) {
        VentScheduleCommandEditorFragment instance = new VentScheduleCommandEditorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        arguments.putString(TIME_OF_DAY_COMMAND_ID, timeOfDayCommandId);
        arguments.putSerializable(CURRENT_DAY_OF_WEEK, currentDayOfWeek);
        instance.setArguments(arguments);

        return instance;
    }

    public static VentScheduleCommandEditorFragment newAddEventInstance (String deviceAddress, String deviceName, DayOfWeek currentDayOfWeek) {
        VentScheduleCommandEditorFragment instance = new VentScheduleCommandEditorFragment();
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

        ventCommand.setId(getTimeOfDayCommandId());
        ventCommand.setDays(EnumSet.of(getCurrentDayOfWeek()));
        ventCommand.setVentLevel(50.0);

        setDeleteButtonVisibility(isEditMode() ? View.VISIBLE : View.GONE);

        return view;
    }


    @Override
    public void onResume () {
        super.onResume();

        ScheduleCommandEditController.getInstance().setListener(this);
        if (isEditMode()) {
            ScheduleCommandEditController.getInstance().loadCommand(getScheduledEntityAddress(), getTimeOfDayCommandId(), new VentCommand());
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
        return new ArrayList<>(ventCommand.getDays());
    }

    @Override
    public TimeOfDay getScheduledTimeOfDay() {
        return ventCommand.getTime();
    }

    @Override
    public boolean isEditMode() {
        return getTimeOfDayCommandId() != null;
    }

    @Override
    public SettingsList getEditableCommandAttributes() {
        SettingsList settings = new SettingsList();

        settings.add(buildStateSetting());

        // ... optionally show the "REPEAT ON" option when a repeat schedule is defined
        if (ventCommand.getDays().size() > 1) {
            settings.add(buildRepeatSetting());
        }

        return settings;
    }

    @Override
    public void onDeleteEvent() {
        ScheduleCommandEditController.getInstance().deleteCommand(getScheduledEntityAddress(), ventCommand);
    }

    @Override
    public String getScheduledEntityAddress() {
        return getArguments().getString(DEVICE_ADDRESS);
    }

    @Override
    public void onRepeatChanged(Set repeatDays) {
        ventCommand.setDays(repeatDays);
        setRepeatRegionVisibility(repeatDays.size() > 1 ? View.GONE : View.VISIBLE);

        rebind();
    }

    @Override
    public void onSaveEvent(EnumSet selectedDays, TimeOfDay timeOfDay) {
        ventCommand.setDays(selectedDays);
        ventCommand.setTime(timeOfDay);

        if (isEditMode()) {
            ScheduleCommandEditController.getInstance().updateCommand(getScheduledEntityAddress(), ventCommand);
        } else {
            ScheduleCommandEditController.getInstance().addCommand(getScheduledEntityAddress(), ventCommand);
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
        this.ventCommand = (VentCommand) scheduleCommandModel;

        setRepeatRegionVisibility(ventCommand.getDays().size() > 1 ? View.GONE : View.VISIBLE);
        setSelectedDays(ventCommand.getDaysAsEnumSet());

        // Redraw the screen with the updated command values
        rebind();
    }

    @Override
    public void onSuccess() {
        hideProgressBar();
        goBack();
    }

    private Setting buildRepeatSetting () {
        String repeatAbstract = StringUtils.getScheduleAbstract(getActivity(), ventCommand.getDays());
        OnClickActionSetting repeatSetting = new OnClickActionSetting(getString(R.string.doors_and_locks_repeat_on), null, repeatAbstract);
        repeatSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepeatPicker();
            }
        });

        return repeatSetting;
    }

    private Setting buildStateSetting () {
        OnClickActionSetting ventSetting = new OnClickActionSetting(getString(R.string.setting_vent_open), null,
                getString(R.string.lightsnswitches_percentage, (int)ventCommand.getVentLevel()), new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                NumberPickerPopup percentPicker = NumberPickerPopup.newInstance(NumberPickerPopup.NumberPickerType.PERCENT, 0,
                        100, (int)ventCommand.getVentLevel(), 10);
                percentPicker.setOnValueChangedListener(new NumberPickerPopup.OnValueChangedListener() {
                    @Override
                    public void onValueChanged(int value) {
                        ventCommand.setVentLevel((double)value);
                        rebind();
                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(percentPicker, percentPicker.getClass().getSimpleName(), true);
            }
        });
        return ventSetting;
    }

    private String getDeviceName() {
        return getArguments().getString(DEVICE_NAME);
    }

    private String getTimeOfDayCommandId() {
        return getArguments().getString(TIME_OF_DAY_COMMAND_ID, null);
    }
}
