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

import android.os.Bundle;
import androidx.annotation.Nullable;

import arcus.cornea.utils.DayOfWeek;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.schedule.AbstractWeeklySchedulerFragment;
import arcus.app.common.schedule.controller.ScheduleCommandListController;
import arcus.app.common.schedule.model.ScheduleCommandModel;
import arcus.app.subsystems.rules.schedule.model.RulesCommand;
import arcus.app.subsystems.rules.schedule.model.RulesCommandFactory;


public class RulesWeeklyScheduleFragment extends AbstractWeeklySchedulerFragment implements ScheduleCommandListController.Callbacks {

    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String DEVICE_NAME = "DEVICE_NAME";

    public static RulesWeeklyScheduleFragment newInstance (String deviceAddress, String deviceName) {
        RulesWeeklyScheduleFragment instance = new RulesWeeklyScheduleFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        instance.setArguments(arguments);

        return instance;
    }

    public void onCorneaError(Throwable cause) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override
    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();

        ScheduleCommandListController.getInstance().setListener(this);
        ScheduleCommandListController.getInstance()
                .loadScheduledCommandsForDayOfWeek(getScheduledEntityAddress(), getSelectedDayOfWeek(), new RulesCommandFactory());
    }

    //overrides bound superclass method
    @Override
    public void onDayOfWeekChanged(DayOfWeek selectedDayOfWeek) {
        ScheduleCommandListController.getInstance()
                .loadScheduledCommandsForDayOfWeek(getScheduledEntityAddress(), selectedDayOfWeek, new RulesCommandFactory());
    }

    @Override
    public void onAddCommand() {

        BackstackManager.getInstance().navigateToFragment(
              RulesScheduleCommandEditorFragment.newAddEventInstance(
                    getScheduledEntityAddress(),
                    getSelectedDayOfWeek().name(), //Use the day of the week as the title of the add/edit screens.
                    getSelectedDayOfWeek(),
                    RulesCommand.State.ACTIVE),
              true
        );
    }

    @Override
    public void onEditCommand(Object command) {


        String timeOfDayCommandId = ((ScheduleCommandModel) command).getId();
        RulesCommand.State state = RulesCommand.State.ACTIVE;
        if (command instanceof  RulesCommand && ((RulesCommand) command).getRawState() != null){
            state=  ((RulesCommand) command).getRawState();
        }
        BackstackManager.getInstance().navigateToFragment(
              RulesScheduleCommandEditorFragment.newEditEventInstance(
                    getScheduledEntityAddress(),
                    getSelectedDayOfWeek().name(), //Use the day of the week as the title of the add/edit screens.
                    timeOfDayCommandId,
                    getSelectedDayOfWeek(), state),
              true
        );
    }

    @Override
    public String getNoCommandsTitleCopy() {
        return getString(R.string.rules_scheduling_title);
    }

    @Override
    public String getNoCommandsDescriptionCopy() {
        return getString(R.string.rules_scheduling_desc);
    }

    @Override
    public boolean isEditMode() {
        return true;     // No dark-text version of this scheduler screen
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.scenes_schedule_title); // Use "Schedule" as the title of the "weekly" page.
    }

    public String getScheduledEntityAddress() {
        return getArguments().getString(DEVICE_ADDRESS);
    }
}
