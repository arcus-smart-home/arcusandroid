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
import androidx.annotation.Nullable;

import arcus.cornea.utils.DayOfWeek;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.schedule.AbstractWeeklySchedulerFragment;
import arcus.app.common.schedule.controller.ScheduleCommandListController;
import arcus.app.common.schedule.model.ScheduleCommandModel;
import arcus.app.subsystems.climate.schedule.model.SpaceHeaterCommandFactory;


public class SpaceHeaterWeeklyScheduleFragment extends AbstractWeeklySchedulerFragment implements ScheduleCommandListController.Callbacks {

    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String DEVICE_NAME = "DEVICE_NAME";

    public static SpaceHeaterWeeklyScheduleFragment newInstance (String deviceAddress, String deviceName) {
        SpaceHeaterWeeklyScheduleFragment instance = new SpaceHeaterWeeklyScheduleFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();

        ScheduleCommandListController.getInstance().setListener(this);
        ScheduleCommandListController.getInstance().loadScheduledCommandsForDayOfWeek(getDeviceAddress(), getSelectedDayOfWeek(), new SpaceHeaterCommandFactory());
    }

    @Override
    public void onDayOfWeekChanged(DayOfWeek selectedDayOfWeek) {
        ScheduleCommandListController.getInstance().loadScheduledCommandsForDayOfWeek(getDeviceAddress(), selectedDayOfWeek, new SpaceHeaterCommandFactory());
    }

    @Override
    public void onAddCommand() {
        BackstackManager.getInstance().navigateToFragment(SpaceHeaterScheduleCommandEditorFragment.newAddEventInstance(getDeviceAddress(), getDeviceName(), getSelectedDayOfWeek()), true);
    }

    @Override
    public void onEditCommand(Object command) {
        String timeOfDayCommandId = ((ScheduleCommandModel) command).getId();
        BackstackManager.getInstance().navigateToFragment(SpaceHeaterScheduleCommandEditorFragment.newEditEventInstance(getDeviceAddress(), getDeviceName(), timeOfDayCommandId, getSelectedDayOfWeek()), true);
    }

    @Override
    public String getNoCommandsTitleCopy() {
        return getString(R.string.doors_and_locks_set_and_forget);
    }

    @Override
    public String getNoCommandsDescriptionCopy() {
        return getString(R.string.doors_and_locks_set_and_forget_desc);
    }

    @Override
    public boolean isEditMode() {
        return true;     // No dark-text version of this scheduler screen
    }

    @Override
    public String getScheduledEntityAddress() {
        return getArguments().getString(DEVICE_ADDRESS);
    }

    @Nullable
    @Override
    public String getTitle() {
        return getDeviceName();
    }

    @Override
    public void onCorneaError(Throwable cause) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    private String getDeviceName () {
        return getArguments().getString(DEVICE_NAME);
    }

    private String getDeviceAddress () {
        return getArguments().getString(DEVICE_ADDRESS);
    }
}
