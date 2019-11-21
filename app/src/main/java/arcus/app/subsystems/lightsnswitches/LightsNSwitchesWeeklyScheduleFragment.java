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
package arcus.app.subsystems.lightsnswitches;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.error.ErrorModel;
import arcus.cornea.subsystem.lightsnswitches.model.LightsNSwitchesScheduleDay;
import arcus.cornea.subsystem.lightsnswitches.schedule.LightsNSwitchesScheduleViewController;
import arcus.cornea.utils.DayOfWeek;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.schedule.AbstractWeeklySchedulerFragment;
import arcus.app.subsystems.lightsnswitches.adapter.LightsNSwitchesScheduleEventAdapter;

import java.util.List;
import java.util.Set;


public class LightsNSwitchesWeeklyScheduleFragment extends AbstractWeeklySchedulerFragment implements LightsNSwitchesScheduleViewController.Callback {

    private final static String DEVICE_NAME = "DEVICE_NAME";
    private final static String DEVICE_ADDR = "DEVICE_ADDR";

    public static LightsNSwitchesWeeklyScheduleFragment newInstance(String deviceAddress, String deviceName) {
        LightsNSwitchesWeeklyScheduleFragment instance = new LightsNSwitchesWeeklyScheduleFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_NAME, deviceName);
        arguments.putString(DEVICE_ADDR, deviceAddress);
        instance.setArguments(arguments);

        return instance;
    }

    public void onCorneaError(Throwable cause) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());

        showProgressBar();
        LightsNSwitchesScheduleViewController.instance().select(getScheduledEntityAddress(), this, getSelectedDayOfWeek());
    }


    @Override
    public void showSchedule(@NonNull DayOfWeek selectedDay, @NonNull Set<DayOfWeek> daysWithSchedule, @NonNull List<LightsNSwitchesScheduleDay> events) {
        hideProgressBar();

        setScheduledDaysOfWeek(daysWithSchedule);

        // Populate the list of schedule events...
        setScheduledCommandsAdapter(new LightsNSwitchesScheduleEventAdapter(getActivity(), events));
    }

    @Override
    public void onError(@NonNull ErrorModel error) {
        hideProgressBar();
        onCorneaError(new RuntimeException(error.getMessage()));
    }

    @Nullable
    @Override
    public String getTitle() {
        return getDeviceName();
    }

    @Override
    public void onDayOfWeekChanged(DayOfWeek selectedDayOfWeek) {
        showProgressBar();
        LightsNSwitchesScheduleViewController.instance().select(getScheduledEntityAddress(), LightsNSwitchesWeeklyScheduleFragment.this, selectedDayOfWeek);
    }

    @Override
    public void onAddCommand() {
        BackstackManager.getInstance().navigateToFragment(LightsNSwitchesScheduleEditorFragment.newAddEventInstance(getScheduledEntityAddress(), getSelectedDayOfWeek()), true);
    }

    @Override
    public void onEditCommand(Object command) {
        BackstackManager.getInstance().navigateToFragment(LightsNSwitchesScheduleEditorFragment.newEditEventInstance(getScheduledEntityAddress(), getSelectedDayOfWeek(), (LightsNSwitchesScheduleDay) command), true);
    }

    @Override
    public String getNoCommandsTitleCopy() {
        return getString(R.string.lightsnswitches_set_and_forget);
    }

    @Override
    public String getNoCommandsDescriptionCopy() {
        return getString(R.string.lightsnswitches_schedule_desc);
    }

    @Override
    public boolean isEditMode() {
        return true;    // L&S schedules are always "edited"
    }

    private String getDeviceName () {
        return getArguments().getString(DEVICE_NAME);
    }
    public String getScheduledEntityAddress() {
        return getArguments().getString(DEVICE_ADDR);
    }
}
