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

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TemperatureUtils;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.capability.WaterHeater;
import com.iris.client.model.Model;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.popups.WaterHeaterPickerPopup;
import arcus.app.common.schedule.AbstractScheduleCommandEditorFragment;
import arcus.app.common.schedule.controller.ScheduleCommandEditController;
import arcus.app.common.schedule.model.ScheduleCommandModel;
import arcus.app.common.utils.StringUtils;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.style.OnClickActionSetting;
import arcus.app.subsystems.water.schedule.model.WaterHeaterCommand;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;


public class WaterHeaterCommandEditorFragment extends AbstractScheduleCommandEditorFragment implements ScheduleCommandEditController.Callbacks {

    private final static String DEVICE_NAME = "DEVICE_NAME";
    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TIME_OF_DAY_COMMAND_ID = "TIME_OF_DAY_COMMAND_ID";
    private final static String CURRENT_DAY_OF_WEEK = "CURRENT_DAY_OF_WEEK";
    private final static String TEMP = "TEMP";


    public static final int MIN_TEMP = 60;
    public static final int MAX_TEMP = 122;


    private WaterHeaterCommand mWaterHeaterCommand = new WaterHeaterCommand();

    public static WaterHeaterCommandEditorFragment newEditEventInstance (String deviceAddress, String deviceName, String timeOfDayCommandId, DayOfWeek currentDayOfWeek, double temp) {
        WaterHeaterCommandEditorFragment instance = new WaterHeaterCommandEditorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        arguments.putString(TIME_OF_DAY_COMMAND_ID, timeOfDayCommandId);
        arguments.putSerializable(CURRENT_DAY_OF_WEEK, currentDayOfWeek);
        arguments.putDouble(TEMP, temp);
        instance.setArguments(arguments);

        return instance;
    }

    public static WaterHeaterCommandEditorFragment newAddEventInstance (String deviceAddress, String deviceName, DayOfWeek currentDayOfWeek) {
        WaterHeaterCommandEditorFragment instance = new WaterHeaterCommandEditorFragment();
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

        mWaterHeaterCommand.setId(getTimeOfDayCommandId());
        mWaterHeaterCommand.setDays(EnumSet.of(getCurrentDayOfWeek()));

        mWaterHeaterCommand.setWaterHeaterTemp(getTemp());

        setDeleteButtonVisibility(isEditMode() ? View.VISIBLE : View.GONE);

        return view;
    }


    @Override
    public void onResume () {
        super.onResume();

        ScheduleCommandEditController.getInstance().setListener(this);
        if (isEditMode()) {
            ScheduleCommandEditController.getInstance().loadCommand(getScheduledEntityAddress(), getTimeOfDayCommandId(), new WaterHeaterCommand());

        }
        rebind(true, getActivity().getString(R.string.water_heater_sched_title), getActivity().getString(R.string.water_heater_sched_sub));
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
        return new ArrayList<>(mWaterHeaterCommand.getDays());
    }

    @Override
    public TimeOfDay getScheduledTimeOfDay() {
        return mWaterHeaterCommand.getTime();
    }

    @Override
    public boolean isEditMode() {
        return getTimeOfDayCommandId() != null;
    }

    @Override
    public SettingsList getEditableCommandAttributes() {
        SettingsList settings = new SettingsList();


        settings.add( buildStateSetting ());

        if (mWaterHeaterCommand.getDays().size() > 1) {
            settings.add(buildRepeatSetting());
        }

        return settings;
    }




    @Override
    public void onDeleteEvent() {
        ScheduleCommandEditController.getInstance().deleteCommand(getScheduledEntityAddress(), mWaterHeaterCommand);
    }

    @Override
    public String getScheduledEntityAddress() {
        return getArguments().getString(DEVICE_ADDRESS);
    }

    @Override
    public void onRepeatChanged(Set repeatDays) {
        mWaterHeaterCommand.setDays(repeatDays);
        setRepeatRegionVisibility(repeatDays.size() > 1 ? View.GONE : View.VISIBLE);


        rebind(true, getActivity().getString(R.string.water_heater_sched_title), getActivity().getString(R.string.water_heater_sched_sub));
    }

    @Override
    public void onSaveEvent(EnumSet selectedDays, TimeOfDay timeOfDay) {
        mWaterHeaterCommand.setDays(selectedDays);
        mWaterHeaterCommand.setTime(timeOfDay);

        if (isEditMode()) {
            ScheduleCommandEditController.getInstance().updateCommand(getScheduledEntityAddress(), mWaterHeaterCommand);
        } else {
            ScheduleCommandEditController.getInstance().addCommand(getScheduledEntityAddress(), mWaterHeaterCommand);
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
        this.mWaterHeaterCommand = (WaterHeaterCommand) scheduleCommandModel;

        setRepeatRegionVisibility(mWaterHeaterCommand.getDays().size() > 1 ? View.GONE : View.VISIBLE);
        setSelectedDays(mWaterHeaterCommand.getDaysAsEnumSet());

        // Redraw the screen with the updated command values
        rebind(true, getActivity().getString(R.string.water_heater_sched_title), getActivity().getString(R.string.water_heater_sched_sub));
    }



    private Setting buildStateSetting () {

      int temperature =  mWaterHeaterCommand.getWaterHeaterTemp();
        OnClickActionSetting ventSetting = new OnClickActionSetting("TEMPERATURE", null,
                getString(R.string.water_heater_degrees,temperature), new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                promptUserForTempSelection();
            }
        });
        return ventSetting;
    }

    private void promptUserForTempSelection() {


        //build map
        TreeMap<Integer,Integer> treeMap = new TreeMap<>();
        treeMap.put(0,60);
        int maxTemp=MAX_TEMP;
        Model device = CorneaClientFactory.getModelCache().get(getScheduledEntityAddress());
        if(device!=null){
            Number maxSetPoint = (Number) device.get(WaterHeater.ATTR_MAXSETPOINT);
            if(maxSetPoint!=null){
                maxTemp = TemperatureUtils.roundCelsiusToFahrenheit(maxSetPoint.doubleValue());
            }
        }

        int nCounter = 1;
        for (int nC = 80; nC <=maxTemp; nC++) {
            treeMap.put(nCounter++, nC);
        }

        WaterHeaterPickerPopup percentPicker = WaterHeaterPickerPopup.newInstance( mWaterHeaterCommand.getWaterHeaterTemp(), treeMap);

        percentPicker.setOnValueChangedListener(new WaterHeaterPickerPopup.OnValueChangedListener() {
            @Override
            public void onValueChanged(int value) {
                mWaterHeaterCommand.setWaterHeaterTemp(value);

                rebind(true, getActivity().getString(R.string.water_heater_sched_title), getActivity().getString(R.string.water_heater_sched_sub));
            }
        });
        BackstackManager.getInstance().navigateToFloatingFragment(percentPicker, percentPicker.getClass().getSimpleName(), true);
    }

    @Override
    public void onSuccess() {
        hideProgressBar();
        goBack();
    }

    private Setting buildRepeatSetting () {
        String repeatAbstract = StringUtils.getScheduleAbstract(getActivity(), mWaterHeaterCommand.getDays());
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

    private int getTemp() {
        return getArguments().getInt(TEMP, 100);
    }
}
