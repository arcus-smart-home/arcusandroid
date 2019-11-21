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
import androidx.annotation.Nullable;

import arcus.cornea.utils.DayOfWeek;
import com.iris.client.bean.TimeOfDayCommand;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.schedule.AbstractWeeklySchedulerFragment;
import arcus.app.common.schedule.adapter.ScheduleCommandAdapter;
import arcus.app.common.schedule.adapter.TimeOfDayCommandAdapter;
import arcus.app.common.schedule.model.ScheduleCommandModel;
import arcus.app.subsystems.scenes.editor.controller.SceneEditorSequenceController;
import arcus.app.subsystems.scenes.schedule.controller.WeeklySchedulerFragmentController;

import java.util.List;
import java.util.Set;


public class WeeklySchedulerFragment extends AbstractWeeklySchedulerFragment<SceneEditorSequenceController> implements WeeklySchedulerFragmentController.Callbacks {

    private final static String SCREEN_VARIANT = "SCREEN_VARIANT";

    public enum ScreenVariant {
        ADD, EDIT
    }
    public void onScheduledCommandsLoaded(DayOfWeek selectedDay, List<ScheduleCommandModel> scheduledEvents, Set<DayOfWeek> daysWithScheduledEvents) {
        hideProgressBar();
        setScheduledCommandsAdapter(new ScheduleCommandAdapter(getActivity(), scheduledEvents, isEditMode()));
        setScheduledDaysOfWeek(daysWithScheduledEvents);
    }

    public void onCorneaError(Throwable cause) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    public static WeeklySchedulerFragment newInstance (boolean isEditMode) {
        WeeklySchedulerFragment instance = new WeeklySchedulerFragment();
        Bundle args = new Bundle();
        args.putSerializable(SCREEN_VARIANT, isEditMode ? WeeklySchedulerFragment.ScreenVariant.EDIT : WeeklySchedulerFragment.ScreenVariant.ADD);
        instance.setArguments(args);

        return instance;
    }

    @Override
    public void onResume () {
        super.onResume();

        showProgressBar();
        WeeklySchedulerFragmentController.getInstance().setListener(this);
        WeeklySchedulerFragmentController.getInstance().loadScheduleCommandsForDay(getScheduledEntityAddress(), getSelectedDayOfWeek());
    }

    @Override
    public void onPause () {
        super.onPause();

        WeeklySchedulerFragmentController.getInstance().removeListener();
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.scene_schedule);
    }

    @Override
    public void onScheduledCommandsLoaded(List<TimeOfDayCommand> scheduledCommandsList, Set<DayOfWeek> daysWithScheduledEvents) {
        hideProgressBar();
        setScheduledCommandsAdapter(new TimeOfDayCommandAdapter(getActivity(), scheduledCommandsList, isEditMode()));
        setScheduledDaysOfWeek(daysWithScheduledEvents);
    }

    @Override
    public String getScheduledEntityAddress() {
        return getController().getSceneAddress();
    }

    @Override
    public void onDayOfWeekChanged(DayOfWeek selectedDayOfWeek) {
        showProgressBar();
        WeeklySchedulerFragmentController.getInstance().loadScheduleCommandsForDay(getScheduledEntityAddress(), getSelectedDayOfWeek());
    }

    @Override
    public void onAddCommand() {
        getController().goAddScheduleEvent(getActivity(), WeeklySchedulerFragment.this, getSelectedDayOfWeek().toString());
    }

    @Override
    public void onEditCommand(Object command) {
        TimeOfDayCommand selectedCommand = (TimeOfDayCommand) command;
        getController().goEditScheduleEvent(getActivity(), WeeklySchedulerFragment.this, getSelectedDayOfWeek().toString(), selectedCommand.getId(), selectedCommand.getTime(), selectedCommand.getDays());
    }

    @Override
    public String getNoCommandsTitleCopy() {
        return getString(R.string.scene_set_and_forget);
    }

    @Override
    public String getNoCommandsDescriptionCopy() {
        return getString(R.string.scene_set_and_forget_desc);
    }

    @Override
    public boolean isEditMode () {
        return getArguments().getSerializable(SCREEN_VARIANT) == ScreenVariant.EDIT;
    }
}
