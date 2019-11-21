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
package arcus.app.subsystems.lawnandgarden.schedule;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.ListView;

import com.google.gson.internal.LinkedTreeMap;
import arcus.cornea.subsystem.lawnandgarden.schedule.LawnAndGardenScheduleController;
import arcus.cornea.utils.CapabilityUtils;
import arcus.cornea.utils.DateUtils;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.Listeners;
import com.iris.client.bean.IrrigationSchedule;
import com.iris.client.bean.IrrigationTransitionEvent;
import com.iris.client.capability.IrrigationZone;
import com.iris.client.capability.LawnNGardenSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.popups.NumericDayPopup;
import arcus.app.common.schedule.AbstractWeeklySchedulerFragment;

import arcus.app.common.view.Version1Button;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.lawnandgarden.LawnAndGardenWeeklyScheduleEventAdapter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;


public class IrrigationWeeklyScheduleFragment extends AbstractWeeklySchedulerFragment implements LawnAndGardenScheduleController.Callback {

    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String DEVICE_NAME = "DEVICE_NAME";
    private final static String SCHEDULE_TYPE = "SCHEDULE_TYPE";
    private LawnAndGardenScheduleController controller;
    Map<String, ArrayList<String>> scheduleByDay = new HashMap<>();
    LawnAndGardenWeeklyScheduleEventAdapter adapter;
    private View topStatusView;
    private View weeklyScheduleView;
    private View noSchedulesCopyView;
    private View selectDaysDialog;
    private View selectDaysDialogDivider;
    private ListView schedulesListView;
    private Version1Button addEventButton;
    private Version1TextView nextEventDisplay;
    private Version1TextView daysText;
    private String title;
    private int timeInDays = 1;

    enum SchedulerType {
        WEEKLY,
        INTERVAL,
        ODD,
        EVEN,
        MANUAL
    }

    private final Listener<LawnNGardenSubsystem.ConfigureIntervalScheduleResponse> configureIntervalScheduleSuccessListener = Listeners.runOnUiThread(new  Listener<LawnNGardenSubsystem.ConfigureIntervalScheduleResponse>() {
        @Override
        public void onEvent(LawnNGardenSubsystem.ConfigureIntervalScheduleResponse clientEvent) {
            hideProgressBar();
            getActivity().invalidateOptionsMenu();
        }
    });

    private Listener<Throwable> failureListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            hideProgressBar();
            ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
        }
    });

    private SchedulerType schedulerType = null;

    public static IrrigationWeeklyScheduleFragment newInstance (String deviceAddress, String deviceName, String scheduleType) {
        IrrigationWeeklyScheduleFragment instance = new IrrigationWeeklyScheduleFragment();

        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        arguments.putString(SCHEDULE_TYPE, scheduleType);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();
        controller = LawnAndGardenScheduleController.instance();
        LawnAndGardenScheduleController.instance().setCallback(this);
        Activity a = getActivity();
        schedulesListView = (ListView) a.findViewById(R.id.schedules);
        addEventButton = (Version1Button) a.findViewById(R.id.add_event_button);
        selectDaysDialog = a.findViewById(R.id.select_days_dialog);
        selectDaysDialogDivider = a.findViewById(R.id.water_every_divider);
        daysText = (Version1TextView) a.findViewById(R.id.days_text);
        selectDaysDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                numericDayPopup();
            }
        });
        noSchedulesCopyView = a.findViewById(R.id.no_schedules_copy);
        weeklyScheduleView = a.findViewById(R.id.weekly_schedule_view);
        topStatusView = a.findViewById(R.id.top_status_view);
        nextEventDisplay = (Version1TextView) a.findViewById(R.id.bottom_status_text_left);
        //TODO: get number of events from the subsystem and send to display setup
        title = getArguments().getString(SCHEDULE_TYPE);
        int scheduleCount = 0;
        if(title == null )title = "";
        switch(title) {
            case IrrigationSchedule.TYPE_WEEKLY:
                scheduleCount = controller.getWeeklySchedule(getDeviceAddress()).size();
                break;
            case IrrigationSchedule.TYPE_INTERVAL:
                scheduleCount = controller.getIntervalSchedule(getDeviceAddress()).size();
                break;
            case IrrigationSchedule.TYPE_ODD:
                scheduleCount = controller.getOddSchedule(getDeviceAddress()).size();
                break;
            case IrrigationSchedule.TYPE_EVEN:
                scheduleCount = controller.getEvenSchedule(getDeviceAddress()).size();
                break;
        }
        displaySetup(scheduleCount, title);
        updateView();

    }

    private void numericDayPopup() {

        NumericDayPopup picker = NumericDayPopup.newInstance(getResources().getString(R.string.irrigation_water_every));
        picker.setCallback(new NumericDayPopup.Callback() {

            @Override
            public void selected(int time) {
                String updateBlock;
                timeInDays = time;
                if (time > 1) updateBlock = time + " Days";
                else updateBlock = time + " Day";
                daysText.setText(updateBlock);
                controller.configureIntervalSchedule(getDeviceAddress(), new Date(System.currentTimeMillis()), timeInDays, configureIntervalScheduleSuccessListener, failureListener);
            }

        });
        BackstackManager.getInstance().navigateToFloatingFragment(picker, picker.getClass().getCanonicalName(), true);
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_weekly_irrigation_scheduler;
    }


    @Override
    public void onDayOfWeekChanged(DayOfWeek selectedDayOfWeek) {
        updateView();
    }

    @Override
    public void onAddCommand() {
        switch(title) {
            case IrrigationSchedule.TYPE_WEEKLY:
                //TODO set different states for next fragment on Interval, ODD and EVEN.
                BackstackManager.getInstance()
                        .navigateToFragment(IrrigationScheduleCommandEditorFragment.newAddEventInstance(getDeviceAddress(), getDeviceName(), getSelectedDayOfWeek(), IrrigationSchedule.TYPE_WEEKLY,0), true);
                break;
            case IrrigationSchedule.TYPE_INTERVAL:
                BackstackManager.getInstance()
                        .navigateToFragment(IrrigationScheduleCommandEditorFragment.newAddEventInstance(getDeviceAddress(), getDeviceName(), getSelectedDayOfWeek(), IrrigationSchedule.TYPE_INTERVAL,timeInDays), true);
                break;
            case IrrigationSchedule.TYPE_ODD:
                BackstackManager.getInstance()
                        .navigateToFragment(IrrigationScheduleCommandEditorFragment.newAddEventInstance(getDeviceAddress(), getDeviceName(), getSelectedDayOfWeek(),IrrigationSchedule.TYPE_ODD,0), true);
                break;
            case IrrigationSchedule.TYPE_EVEN:
                BackstackManager.getInstance()
                        .navigateToFragment(IrrigationScheduleCommandEditorFragment.newAddEventInstance(getDeviceAddress(), getDeviceName(), getSelectedDayOfWeek(), IrrigationSchedule.TYPE_EVEN,0), true);
                break;
        }    }

    @Override
    public void onEditCommand(Object eventId) {
        //TODO set different states for next fragment on Interval, ODD and EVEN.
        switch(title) {
            case IrrigationSchedule.TYPE_WEEKLY:
                BackstackManager.getInstance().navigateToFragment(IrrigationScheduleCommandEditorFragment.newEditEventInstance(getDeviceAddress(), getDeviceName(), (String) eventId, getSelectedDayOfWeek(), IrrigationSchedule.TYPE_WEEKLY, 0), true);
                break;
            case IrrigationSchedule.TYPE_INTERVAL:
                BackstackManager.getInstance().navigateToFragment(IrrigationScheduleCommandEditorFragment.newEditEventInstance(getDeviceAddress(), getDeviceName(), (String) eventId, getSelectedDayOfWeek(), IrrigationSchedule.TYPE_INTERVAL, timeInDays), true);
                break;
            case IrrigationSchedule.TYPE_ODD:
                BackstackManager.getInstance().navigateToFragment(IrrigationScheduleCommandEditorFragment.newEditEventInstance(getDeviceAddress(), getDeviceName(), (String) eventId, getSelectedDayOfWeek(), IrrigationSchedule.TYPE_ODD, 0), true);
                break;
            case IrrigationSchedule.TYPE_EVEN:
                BackstackManager.getInstance().navigateToFragment(IrrigationScheduleCommandEditorFragment.newEditEventInstance(getDeviceAddress(), getDeviceName(), (String) eventId, getSelectedDayOfWeek(), IrrigationSchedule.TYPE_EVEN, 0), true);
                break;
        }
    }

    @Override
    public String getNoCommandsTitleCopy() {
        return "Create a Schedule";
    }

    @Override
    public String getNoCommandsDescriptionCopy() {
        return "Tap Add Event below to create\na schedule for this day.";
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
        return getArguments().getString(SCHEDULE_TYPE);
    }

    private String getDeviceName () {
        return getArguments().getString(DEVICE_NAME);
    }

    private String getDeviceAddress () {
        return getArguments().getString(DEVICE_ADDRESS);
    }

    @Override
    public void updateView() {
        updateHeader();
        ArrayList<LinkedTreeMap<String, Object>> events = null;

        if(title == null) title = "";

        switch(title) {
            case IrrigationSchedule.TYPE_WEEKLY:
                events = controller.getWeeklySchedule(getDeviceAddress());
                break;
            case IrrigationSchedule.TYPE_INTERVAL:
                Map<String, Object> deviceMap = controller.getUnfilteredIntervalSchedule(getDeviceAddress());
                if(deviceMap.get("events") == null) {
                    break;
                }
                events = controller.filterDeletedEvents(((ArrayList<LinkedTreeMap<String, Object>>)deviceMap.get("events")));
                if(deviceMap.get("days") != null) {
                    timeInDays = ((Number)deviceMap.get("days")).intValue();
                    String updateBlock;
                    if (timeInDays > 1) {
                        updateBlock = timeInDays + " Days";
                    }
                    else {
                        updateBlock = timeInDays + " Day";
                    }
                    daysText.setText(updateBlock);
                }

                if(events.size() > 0) {
                    selectDaysDialog.setVisibility(View.VISIBLE);
                    selectDaysDialogDivider.setVisibility(View.VISIBLE);
                }
                else {
                    selectDaysDialog.setVisibility(View.GONE);
                    selectDaysDialogDivider.setVisibility(View.GONE);
                }
                break;
            case IrrigationSchedule.TYPE_ODD:
                events = controller.getOddSchedule(getDeviceAddress());
                break;
            case IrrigationSchedule.TYPE_EVEN:
                events = controller.getEvenSchedule(getDeviceAddress());
                break;
        }

        ArrayList<LinkedTreeMap<String, Object>> eventsForCurrentDay = new ArrayList<>();
        ArrayList<ArrayList<String>> zoneListsForDay = new ArrayList<>();

        DeviceModel deviceModel = getCorneaService().getStore(DeviceModel.class).get(CorneaUtils.getIdFromAddress(getDeviceAddress()));

        CapabilityUtils capabilityUtils = new CapabilityUtils(deviceModel);
        Map<String, String> zoneMapping = new HashMap<>();
        if (capabilityUtils != null) {
            for (String instance : capabilityUtils.getInstanceNames()) {
                String name = (String) capabilityUtils.getInstanceValue(instance, IrrigationZone.ATTR_ZONENAME);
                Double number = (Double) capabilityUtils.getInstanceValue(instance, IrrigationZone.ATTR_ZONENUM);
                int zoneNum = 1;
                if (number != null) {
                    zoneNum = number.intValue();
                }
                name = name != null ? name : "Zone "+Integer.toString(zoneNum);
                zoneMapping.put(instance, name);
            }
        }

        Set<DayOfWeek> scheduleDays = new HashSet<>();
        for(LinkedTreeMap<String, Object> item : events) {
            //new schedule entry for each event item
            String eventId = (String)item.get("eventId");
            ArrayList<String> eventDays = (ArrayList<String>) item.get("days");
            if (eventDays != null) {

                for (String eventDay : eventDays) {
                    ArrayList<String> days = scheduleByDay.get(eventDay);
                    if (days == null) {
                        days = new ArrayList<>();
                        scheduleByDay.put(eventDay, days);
                    }
                    if (!days.contains(eventId)) {
                        days.add(eventId);
                    }
                }

                switch (getSelectedDayOfWeek()) {
                    case MONDAY:
                        if (eventDays.contains("MON")) {
                            eventsForCurrentDay.add(item);
                            zoneListsForDay.add(getZoneList((ArrayList<LinkedTreeMap<String, Object>>) item.get("events"), zoneMapping));
                            scheduleDays.add(DayOfWeek.MONDAY);
                        }
                        break;
                    case TUESDAY:
                        if (eventDays.contains("TUE")) {
                            eventsForCurrentDay.add(item);
                            zoneListsForDay.add(getZoneList((ArrayList<LinkedTreeMap<String, Object>>) item.get("events"), zoneMapping));
                            scheduleDays.add(DayOfWeek.TUESDAY);
                        }
                        break;
                    case WEDNESDAY:
                        if (eventDays.contains("WED")) {
                            eventsForCurrentDay.add(item);
                            zoneListsForDay.add(getZoneList((ArrayList<LinkedTreeMap<String, Object>>) item.get("events"), zoneMapping));
                            scheduleDays.add(DayOfWeek.WEDNESDAY);
                        }
                        break;
                    case THURSDAY:
                        if (eventDays.contains("THU")) {
                            eventsForCurrentDay.add(item);
                            zoneListsForDay.add(getZoneList((ArrayList<LinkedTreeMap<String, Object>>) item.get("events"), zoneMapping));
                            scheduleDays.add(DayOfWeek.THURSDAY);
                        }
                        break;
                    case FRIDAY:
                        if (eventDays.contains("FRI")) {
                            eventsForCurrentDay.add(item);
                            zoneListsForDay.add(getZoneList((ArrayList<LinkedTreeMap<String, Object>>) item.get("events"), zoneMapping));
                            scheduleDays.add(DayOfWeek.FRIDAY);
                        }
                        break;
                    case SATURDAY:
                        if (eventDays.contains("SAT")) {
                            eventsForCurrentDay.add(item);
                            zoneListsForDay.add(getZoneList((ArrayList<LinkedTreeMap<String, Object>>) item.get("events"), zoneMapping));
                            scheduleDays.add(DayOfWeek.SATURDAY);
                        }
                        break;
                    case SUNDAY:
                        if (eventDays.contains("SUN")) {
                            eventsForCurrentDay.add(item);
                            zoneListsForDay.add(getZoneList((ArrayList<LinkedTreeMap<String, Object>>) item.get("events"), zoneMapping));
                            scheduleDays.add(DayOfWeek.SUNDAY);
                        }
                        break;
                }


                if (eventDays.contains("MON")) {
                    scheduleDays.add(DayOfWeek.MONDAY);
                }
                if (eventDays.contains("TUE")) {
                    scheduleDays.add(DayOfWeek.TUESDAY);
                }
                if (eventDays.contains("WED")) {
                    scheduleDays.add(DayOfWeek.WEDNESDAY);
                }
                if (eventDays.contains("THU")) {
                    scheduleDays.add(DayOfWeek.THURSDAY);
                }
                if (eventDays.contains("FRI")) {
                    scheduleDays.add(DayOfWeek.FRIDAY);
                }
                if (eventDays.contains("SAT")) {
                    scheduleDays.add(DayOfWeek.SATURDAY);
                }
                if (eventDays.contains("SUN")) {
                    scheduleDays.add(DayOfWeek.SUNDAY);
                }
            } else{
                eventsForCurrentDay.add(item);
                zoneListsForDay.add(getZoneList((ArrayList<LinkedTreeMap<String, Object>>) item.get("events"), zoneMapping));
            }
        }

        adapter = new LawnAndGardenWeeklyScheduleEventAdapter(getActivity(), eventsForCurrentDay, zoneListsForDay);
        adapter.notifyDataSetChanged();

        setScheduledCommandsAdapter(adapter);
        setScheduledDaysOfWeek(scheduleDays);
    }

    @Override
    public void subsystemUpdate() {
        updateView();
    }

    private ArrayList<String> getZoneList(ArrayList<LinkedTreeMap<String, Object>> items, Map<String, String> zoneMapping) {
        ArrayList<String> zones = new ArrayList<>();
        for (LinkedTreeMap<String, Object> item : items) {
            zones.add(zoneMapping.get(item.get("zone")));
        }
        return zones;
    }

    private void displaySetup(int events, String title){
        resetViews();

            switch(title) {
                case IrrigationSchedule.TYPE_WEEKLY:
                    schedulerType = SchedulerType.WEEKLY;
                    weeklyScheduleView.setVisibility(View.VISIBLE);
                    if (events==0){
                        return;
                    }
                    weeklySetup();
                    return;
                case IrrigationSchedule.TYPE_INTERVAL:
                    schedulerType = SchedulerType.INTERVAL;
                    if (events==0){
                        return;
                    }
                    intervalSetup();
                    return;
                case IrrigationSchedule.TYPE_ODD:
                    schedulerType = SchedulerType.ODD;
                    if (events==0){
                        return;
                    }
                    oddEvenSetup();
                    return;
                case IrrigationSchedule.TYPE_EVEN:
                    schedulerType = SchedulerType.EVEN;
                    if (events==0){
                        return;
                    }
                    oddEvenSetup();
            }
    }

    private void weeklySetup(){
        noSchedulesCopyView.setVisibility(View.GONE);
        schedulesListView.setVisibility(View.VISIBLE);
        weeklyScheduleView.setVisibility(View.VISIBLE);
        selectDaysDialog.setVisibility(View.GONE);
        selectDaysDialogDivider.setVisibility(View.GONE);
        updateHeader();
    }

    private void intervalSetup(){
        noSchedulesCopyView.setVisibility(View.GONE);
        schedulesListView.setVisibility(View.VISIBLE);
        weeklyScheduleView.setVisibility(View.GONE);
        selectDaysDialog.setVisibility(View.VISIBLE);
        selectDaysDialogDivider.setVisibility(View.VISIBLE);
        updateHeader();
    }

    private void oddEvenSetup(){
        noSchedulesCopyView.setVisibility(View.GONE);
        schedulesListView.setVisibility(View.VISIBLE);
        weeklyScheduleView.setVisibility(View.GONE);
        selectDaysDialog.setVisibility(View.GONE);
        selectDaysDialogDivider.setVisibility(View.GONE);
        updateHeader();
    }

    private void resetViews(){
        //Sets the view to a default, null set.
        noSchedulesCopyView.setVisibility(View.VISIBLE);
        schedulesListView.setVisibility(View.GONE);
        weeklyScheduleView.setVisibility(View.GONE);
        selectDaysDialog.setVisibility(View.GONE);
        selectDaysDialogDivider.setVisibility(View.GONE);
        topStatusView.setVisibility(View.GONE);
    }

    private void updateHeader() {
        String deviceAddress = getDeviceAddress();
        IrrigationTransitionEvent event = controller.getNextEvent(deviceAddress);
        String mode = controller.getSelectedScheduleType(deviceAddress);
        if (event == null || event.getStartTime() == null || TextUtils.isEmpty(mode)) {
            topStatusView.setVisibility(View.GONE);
            return;
        }

        if (schedulerType == null || !schedulerType.name().equals(mode)) {
            topStatusView.setVisibility(View.GONE);
            return;
        }


        nextEventDisplay.setText(DateUtils.format(event.getStartTime()));
        topStatusView.setVisibility(View.VISIBLE);
    }
}
