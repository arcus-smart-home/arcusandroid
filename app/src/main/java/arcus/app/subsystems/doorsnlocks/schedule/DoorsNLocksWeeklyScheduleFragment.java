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
package arcus.app.subsystems.doorsnlocks.schedule;

import android.os.Bundle;
import androidx.annotation.Nullable;

import arcus.cornea.utils.DayOfWeek;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.schedule.AbstractWeeklySchedulerFragment;
import arcus.app.common.schedule.controller.ScheduleCommandListController;
import arcus.app.common.schedule.model.ScheduleCommandModel;
import arcus.app.common.schedule.model.ScheduleCommandModelFactory;
import arcus.app.device.model.DeviceType;
import arcus.app.subsystems.doorsnlocks.schedule.model.GarageDoorCommandFactory;
import arcus.app.subsystems.doorsnlocks.schedule.model.PetDoorCommandFactory;


public class DoorsNLocksWeeklyScheduleFragment extends AbstractWeeklySchedulerFragment implements ScheduleCommandListController.Callbacks {

    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String DEVICE_NAME = "DEVICE_NAME";
    private final static String DEVICE_TYPE = "DEVICE_TYPE";

    public static DoorsNLocksWeeklyScheduleFragment newInstance (String deviceAddress, String deviceName, DeviceType deviceType) {
        DoorsNLocksWeeklyScheduleFragment instance = new DoorsNLocksWeeklyScheduleFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        arguments.putSerializable(DEVICE_TYPE, deviceType);
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
        ScheduleCommandListController.getInstance().loadScheduledCommandsForDayOfWeek(getScheduledEntityAddress(), getSelectedDayOfWeek(), getCommandFactory());
    }

    @Override
    public void onDayOfWeekChanged(DayOfWeek selectedDayOfWeek) {
        ScheduleCommandListController.getInstance().loadScheduledCommandsForDayOfWeek(getScheduledEntityAddress(), selectedDayOfWeek, getCommandFactory());
    }

    @Override
    public void onAddCommand() {
        BackstackManager.getInstance().navigateToFragment(DoorsNLocksScheduleCommandEditorFragment.newAddEventInstance(getScheduledEntityAddress(), getDeviceName(), getSelectedDayOfWeek(), getDeviceType().equals(DeviceType.PET_DOOR)), true);
    }

    @Override
    public void onEditCommand(Object command) {
        String timeOfDayCommandId = ((ScheduleCommandModel) command).getId();
        BackstackManager.getInstance().navigateToFragment(DoorsNLocksScheduleCommandEditorFragment.newEditEventInstance(getScheduledEntityAddress(), getDeviceName(), timeOfDayCommandId, getSelectedDayOfWeek(), getDeviceType().equals(DeviceType.PET_DOOR)), true);
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

    @Nullable
    @Override
    public String getTitle() {
        return getDeviceName();
    }

    private String getDeviceName () {
        return getArguments().getString(DEVICE_NAME);
    }

    public String getScheduledEntityAddress() {
        return getArguments().getString(DEVICE_ADDRESS);
    }

    private DeviceType getDeviceType() {
        return (DeviceType) getArguments().getSerializable(DEVICE_TYPE);
    }

    private ScheduleCommandModelFactory getCommandFactory () {
        switch (getDeviceType()) {
            case PET_DOOR:
                return new PetDoorCommandFactory();

            case GARAGE_DOOR:
                return new GarageDoorCommandFactory();

            default:
                throw new IllegalStateException("Bug! Device type " + getDeviceType() + " is not schedulable in this context.");
        }
    }
}
