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
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.error.ErrorModel;
import arcus.cornea.utils.DayOfWeek;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.schedule.AbstractWeeklySchedulerFragment;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.water.WaterHeaterScheduleEventAdapter;

import java.util.List;
import java.util.Set;

public class WaterHeaterWeeklyScheduleFragment extends AbstractWeeklySchedulerFragment implements WaterHeaterScheduleViewController.Callback {

    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String DEVICE_NAME = "DEVICE_NAME";
    public static final String WEEKLY = "WEEKLY";



    private Version1TextView topStatus;

    public static WaterHeaterWeeklyScheduleFragment newInstance (String deviceAddress, String deviceName) {
        WaterHeaterWeeklyScheduleFragment instance = new WaterHeaterWeeklyScheduleFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        topStatus = (Version1TextView) view.findViewById(R.id.top_status_text);
        return view;
    }


    @Override
    public final Integer getLayoutId() {
        return R.layout.fragment_weekly_water_scheduler;
    }



    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();

        showProgressBar();

        getActivity().setTitle(getTitle());

        WaterHeaterScheduleViewController.instance().select(getScheduledEntityAddress(), this, getSelectedDayOfWeek());

        setStatus();


    }


    private void setStatus(){
        topStatus.setText(WEEKLY);
    }
    @Override
    public void onDayOfWeekChanged(DayOfWeek selectedDayOfWeek) {



        WaterHeaterScheduleViewController.instance().select(getScheduledEntityAddress(), this, getSelectedDayOfWeek());
    }

    @Override
    public void onAddCommand() {
        BackstackManager.getInstance()
                .navigateToFragment(WaterHeaterCommandEditorFragment
                        .newAddEventInstance(getDeviceAddress(), getDeviceName(), getSelectedDayOfWeek()), true);
    }

    @Override
    public void onEditCommand(Object command) {
        String timeOfDayCommandId = ((WaterScheduleDay) command).getCommandID();
        double temp = ((WaterScheduleDay) command).getSetPoint();
        BackstackManager.getInstance()
                .navigateToFragment(WaterHeaterCommandEditorFragment
                       .newEditEventInstance(getDeviceAddress(), getDeviceName(), timeOfDayCommandId, getSelectedDayOfWeek(), temp), true);
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


    private String getDeviceName () {
        return getArguments().getString(DEVICE_NAME);
    }

    private String getDeviceAddress () {
        return getArguments().getString(DEVICE_ADDRESS);
    }

    @Override
    public void showSchedule(@NonNull DayOfWeek selectedDay, @NonNull Set<DayOfWeek> daysWithSchedule, @NonNull List<WaterScheduleDay> events) {
        hideProgressBar();

        setScheduledDaysOfWeek(daysWithSchedule);

        // Populate the list of schedule events...
        setScheduledCommandsAdapter(new WaterHeaterScheduleEventAdapter(getActivity(), events));
    }

    @Override
    public void onError(@NonNull ErrorModel error) {
        hideProgressBar();
        onCorneaError(new RuntimeException(error.getMessage()));
    }

    public void onCorneaError(Throwable cause) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

}
