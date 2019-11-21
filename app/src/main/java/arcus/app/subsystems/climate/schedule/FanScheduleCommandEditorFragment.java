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
import arcus.app.common.popups.ButtonListPopup;
import arcus.app.common.schedule.AbstractScheduleCommandEditorFragment;
import arcus.app.common.schedule.controller.ScheduleCommandEditController;
import arcus.app.common.schedule.model.ScheduleCommandModel;
import arcus.app.common.utils.StringUtils;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.style.BinarySetting;
import arcus.app.device.settings.style.OnClickActionSetting;
import arcus.app.subsystems.climate.schedule.model.FanCommand;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;


public class FanScheduleCommandEditorFragment extends AbstractScheduleCommandEditorFragment implements ScheduleCommandEditController.Callbacks {

    private final static String DEVICE_NAME = "DEVICE_NAME";
    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TIME_OF_DAY_COMMAND_ID = "TIME_OF_DAY_COMMAND_ID";
    private final static String CURRENT_DAY_OF_WEEK = "CURRENT_DAY_OF_WEEK";

    private FanCommand fanCommand = new FanCommand();

    public static FanScheduleCommandEditorFragment newEditEventInstance (String deviceAddress, String deviceName, String timeOfDayCommandId, DayOfWeek currentDayOfWeek) {
        FanScheduleCommandEditorFragment instance = new FanScheduleCommandEditorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        arguments.putString(TIME_OF_DAY_COMMAND_ID, timeOfDayCommandId);
        arguments.putSerializable(CURRENT_DAY_OF_WEEK, currentDayOfWeek);
        instance.setArguments(arguments);

        return instance;
    }

    public static FanScheduleCommandEditorFragment newAddEventInstance (String deviceAddress, String deviceName, DayOfWeek currentDayOfWeek) {
        FanScheduleCommandEditorFragment instance = new FanScheduleCommandEditorFragment();
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

        fanCommand.setId(getTimeOfDayCommandId());
        fanCommand.setDays(EnumSet.of(getCurrentDayOfWeek()));
        fanCommand.setFanState(true);
        fanCommand.setFanSpeed("LOW");
        setDeleteButtonVisibility(isEditMode() ? View.VISIBLE : View.GONE);

        return view;
    }


    @Override
    public void onResume () {
        super.onResume();

        ScheduleCommandEditController.getInstance().setListener(this);
        if (isEditMode()) {
            ScheduleCommandEditController.getInstance().loadCommand(getScheduledEntityAddress(), getTimeOfDayCommandId(), new FanCommand());
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
        return new ArrayList<>(fanCommand.getDays());
    }

    @Override
    public TimeOfDay getScheduledTimeOfDay() {
        return fanCommand.getTime();
    }

    @Override
    public boolean isEditMode() {
        return getTimeOfDayCommandId() != null;
    }

    @Override
    public SettingsList getEditableCommandAttributes() {
        SettingsList settings = new SettingsList();

        if(fanCommand.hasSwitch()) {
            settings.add(buildFanOnOffSetting());
        }
        if (fanCommand.getFanState()) {
            settings.add(buildFanSpeedSetting());
        }

        if (fanCommand.getDays().size() > 1) {
            settings.add(buildRepeatSetting());
        }

        return settings;
    }

    @Override
    public void onDeleteEvent() {
        ScheduleCommandEditController.getInstance().deleteCommand(getScheduledEntityAddress(), fanCommand);
    }

    @Override
    public String getScheduledEntityAddress() {
        return getArguments().getString(DEVICE_ADDRESS);
    }

    @Override
    public void onRepeatChanged(Set repeatDays) {
        fanCommand.setDays(repeatDays);
        setRepeatRegionVisibility(repeatDays.size() > 1 ? View.GONE : View.VISIBLE);

        rebind();
    }

    @Override
    public void onSaveEvent(EnumSet selectedDays, TimeOfDay timeOfDay) {
        fanCommand.setDays(selectedDays);
        fanCommand.setTime(timeOfDay);

        if (isEditMode()) {
            ScheduleCommandEditController.getInstance().updateCommand(getScheduledEntityAddress(), fanCommand);
        } else {
            ScheduleCommandEditController.getInstance().addCommand(getScheduledEntityAddress(), fanCommand);
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
        this.fanCommand = (FanCommand) scheduleCommandModel;

        setRepeatRegionVisibility(fanCommand.getDays().size() > 1 ? View.GONE : View.VISIBLE);
        setSelectedDays(fanCommand.getDaysAsEnumSet());

        // Redraw the screen with the updated command values
        rebind();
    }

    @Override
    public void onSuccess() {
        hideProgressBar();
        goBack();
    }

    private Setting buildRepeatSetting () {
        String repeatAbstract = StringUtils.getScheduleAbstract(getActivity(), fanCommand.getDays());
        OnClickActionSetting repeatSetting = new OnClickActionSetting(getString(R.string.doors_and_locks_repeat_on), null, repeatAbstract);
        repeatSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepeatPicker();
            }
        });

        return repeatSetting;
    }

    private Setting buildFanOnOffSetting () {
        BinarySetting stateAbstract = new BinarySetting(getActivity().getResources().getString(R.string.setting_state), null, fanCommand.getFanState());
        stateAbstract.addListener(new SettingChangedParcelizedListener() {
            @Override
            public void onSettingChanged(Setting setting, Object newValue) {
                fanCommand.setFanState((Boolean) newValue);
                rebind();
            }
        });

        return stateAbstract;
    }

    private Setting buildFanSpeedSetting () {
        String stateAbstract = fanCommand.getFanSpeed();
        OnClickActionSetting stateSetting = new OnClickActionSetting(getString(R.string.setting_fan_speed), null, stateAbstract);
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
        editChoices.put(getString(R.string.setting_low), "LOW");
        editChoices.put(getString(R.string.setting_medium), "MEDIUM");
        editChoices.put(getString(R.string.setting_high), "HIGH");

        ButtonListPopup editWhichDayPopup = ButtonListPopup.newInstance(editChoices, R.string.setting_fan_speed, -1);

        editWhichDayPopup.setCallback(new ButtonListPopup.Callback() {
            @Override
            public void buttonSelected(String buttonKeyValue) {
                BackstackManager.getInstance().navigateBack();
                //fanCommand.setFanState(OPEN.equals(buttonKeyValue));
                fanCommand.setFanSpeed(buttonKeyValue);
                rebind();
            }
        });

        BackstackManager.getInstance().navigateToFloatingFragment(editWhichDayPopup, editWhichDayPopup.getClass().getSimpleName(), true);
    }

    private String getDeviceName() {
        return getArguments().getString(DEVICE_NAME);
    }

    private String getTimeOfDayCommandId() {
        return getArguments().getString(TIME_OF_DAY_COMMAND_ID, null);
    }
}
