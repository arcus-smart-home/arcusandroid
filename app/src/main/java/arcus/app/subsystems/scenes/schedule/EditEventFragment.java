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
package arcus.app.subsystems.scenes.schedule;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.schedule.AbstractScheduleCommandEditorFragment;
import arcus.app.common.schedule.controller.ScheduleCommandEditController;
import arcus.app.common.schedule.model.ScheduleCommandModel;
import arcus.app.common.utils.StringUtils;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.style.OnClickActionSetting;
import arcus.app.subsystems.scenes.editor.controller.SceneEditorSequenceController;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;


public class EditEventFragment extends AbstractScheduleCommandEditorFragment<SceneEditorSequenceController> implements ScheduleCommandEditController.Callbacks {

    private final static String DAY_OF_WEEK = "DAY_OF_WEEK";
    private final static String UPDATING_COMMAND_ID = "COMMAND_ID";
    private final static String SELECTED_TIME = "SELECTED_TIME";
    private final static String SELECTED_DAYS = "SELECTED_DAYS";

    private SceneCommand mSceneCommand = new SceneCommand();

    public static EditEventFragment newInstance(String selectedDayOfWeek) {
        Bundle args = new Bundle();
        args.putString(DAY_OF_WEEK, selectedDayOfWeek);
        EditEventFragment fragment = new EditEventFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static EditEventFragment newInstance(String selectedDayOfWeek, String commandId, String time, ArrayList<String> selectedDays) {
        Bundle args = new Bundle();
        args.putString(DAY_OF_WEEK, selectedDayOfWeek);
        args.putString(UPDATING_COMMAND_ID, commandId);
        args.putString(SELECTED_TIME, time);
        args.putSerializable(SELECTED_DAYS, selectedDays);
        EditEventFragment fragment = new EditEventFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);

        mSceneCommand.setId(getCommandId());
        mSceneCommand.setDays(EnumSet.of(getCurrentDayOfWeek()));
        setDeleteButtonVisibility(isEditMode() ? View.VISIBLE : View.GONE);

        return view;
    }

    @Override
    public void onResume () {
        super.onResume();
        ScheduleCommandEditController.getInstance().setListener(this);
        if (isEditMode()) {
            ScheduleCommandEditController.getInstance().loadCommand(getScheduledEntityAddress(), getCommandId(), mSceneCommand);
        }
    }

    @Override
    public void onPause () {
        super.onPause();
        ScheduleCommandEditController.getInstance().removeListener();
    }

    @Override
    public DayOfWeek getCurrentDayOfWeek() {
        return DayOfWeek.valueOf(getArguments().getString(DAY_OF_WEEK));
    }

    @Override
    public List<DayOfWeek> getScheduledDaysOfWeek() {
        return new ArrayList<>(mSceneCommand.getDays());
    }

    @Override
    public TimeOfDay getScheduledTimeOfDay() {
        return mSceneCommand.getTime();
    }

    @Override
    public boolean isEditMode () {
        return getCommandId() != null;
    }

    @Override
    public SettingsList getEditableCommandAttributes() {
        SettingsList settings = new SettingsList();

        if (mSceneCommand.getDays().size() > 1) {
            settings.add(buildRepeatSetting());
        }

        return settings;
    }

    private Setting buildRepeatSetting () {
        String repeatAbstract = StringUtils.getScheduleAbstract(getActivity(), mSceneCommand.getDays());
        OnClickActionSetting repeatSetting = new OnClickActionSetting(getString(R.string.doors_and_locks_repeat_on), null, repeatAbstract);
        repeatSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepeatPicker();
            }
        });

        return repeatSetting;
    }

    @Override
    public void onDeleteEvent() {
        ScheduleCommandEditController.getInstance().deleteCommand(getScheduledEntityAddress(), mSceneCommand);
    }

    @Override
    public void onRepeatChanged(Set repeatDays) {
        mSceneCommand.setDays(repeatDays);
        setRepeatRegionVisibility(repeatDays.size() > 1 ? View.GONE : View.VISIBLE);

        rebind();
    }

    @Override
    public void onSaveEvent(EnumSet selectedDays, TimeOfDay timeOfDay) {
        mSceneCommand.setDays(selectedDays);
        mSceneCommand.setTime(timeOfDay);

        if (isEditMode()) {
            ScheduleCommandEditController.getInstance().updateCommand(getScheduledEntityAddress(), mSceneCommand);
        } else {
            ScheduleCommandEditController.getInstance().addCommand(getScheduledEntityAddress(), mSceneCommand);
        }
    }

    private String getCommandId () {
        return getArguments().getString(UPDATING_COMMAND_ID);
    }

    @Override
    public void onSchedulerError(Throwable cause) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override
    public void onTimeOfDayCommandLoaded(ScheduleCommandModel scheduleCommandModel) {
        hideProgressBar();
        this.mSceneCommand = (SceneCommand) scheduleCommandModel;

        setRepeatRegionVisibility(mSceneCommand.getDays().size() > 1 ? View.GONE : View.VISIBLE);
        setSelectedDays(mSceneCommand.getDaysAsEnumSet());

        // Redraw the screen with the updated command values
        rebind();
    }

    @Override
    public void onSuccess() {
        hideProgressBar();
        goBack();
    }

    @Override
    public String getScheduledEntityAddress() {
        return getController().getSceneAddress();
    }
}
