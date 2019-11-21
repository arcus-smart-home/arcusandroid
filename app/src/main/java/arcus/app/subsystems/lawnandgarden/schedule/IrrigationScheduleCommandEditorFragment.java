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

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Sets;
import com.google.gson.internal.LinkedTreeMap;
import arcus.cornea.subsystem.lawnandgarden.schedule.LawnAndGardenScheduleController;
import arcus.cornea.utils.CapabilityUtils;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.bean.IrrigationSchedule;
import com.iris.client.bean.IrrigationScheduleStatus;
import com.iris.client.capability.LawnNGardenSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.popups.ButtonListPopup;
import arcus.app.common.schedule.AbstractScheduleCommandEditorFragment;
import arcus.app.common.schedule.controller.ScheduleCommandEditController;
import arcus.app.common.schedule.model.ScheduleCommandModel;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.StringUtils;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.style.OnClickActionSetting;
import arcus.app.subsystems.alarm.AlertFloatingFragment;
import arcus.app.subsystems.lawnandgarden.models.IrrigationZoneInfo;
import arcus.app.subsystems.lawnandgarden.zoneorder.IrrigationZoneCardListFragment;
import arcus.app.subsystems.lawnandgarden.zoneorder.IrrigationZoneListItemModel;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class IrrigationScheduleCommandEditorFragment extends AbstractScheduleCommandEditorFragment implements LawnAndGardenScheduleController.Callback,
        ScheduleCommandEditController.Callbacks, IrrigationZoneCardListFragment.ZoneInfoCallback {

    private final int DEFAULT_EVENT_HOUR = 6;
    private final int DEFAULT_EVENT_MINUTE = 0;
    private final static String TEMP_FIX_FOR_LAST_EVENT_DELETED = "IllegalStateException";
    private final static String DEVICE_NAME = "DEVICE_NAME";
    private final static String DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private final static String TIME_OF_DAY_COMMAND_ID = "TIME_OF_DAY_COMMAND_ID";
    private final static String CURRENT_DAY_OF_WEEK = "CURRENT_DAY_OF_WEEK";
    private final static String TITLE = "TITLE";
    private final static String INTERVAL = "INTERVAL";
    private static final String CODE_SCHEDULE_OVERLAPS = "lawnngarden.scheduling.has_overlaps";
    private LawnAndGardenScheduleController controller;
    private IrrigationScheduleCommandEditorFragment schedFrag;
    private CapabilityUtils capabilityUtils;
    private ArrayList<IrrigationZoneInfo> zones;
    private boolean bAllDays = false;
    private String title;
    private int interval = 1;
    TimeOfDay tod;

    private Listener<Throwable> failureListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            hideProgressBar();
            String title = ((ErrorResponseException) throwable).getCode();
            String description = throwable.getMessage();

            if (TEMP_FIX_FOR_LAST_EVENT_DELETED.equals(title)) {
                BackstackManager.getInstance().navigateBack();
                return;
            }

            if(((ErrorResponseException) throwable).getCode().equals(CODE_SCHEDULE_OVERLAPS)) {
                title = getString(R.string.irrigation_overlapping_event_title);
                description = getString(R.string.irrigation_overlapping_event_description);
                AlertPopup popup = AlertPopup.newInstance(title, description,
                      null, null, new AlertPopup.AlertButtonCallback() {
                          @Override
                          public boolean topAlertButtonClicked() {
                              return false;
                          }

                          @Override
                          public boolean bottomAlertButtonClicked() {
                              return false;
                          }

                          @Override
                          public boolean errorButtonClicked() {
                              return false;
                          }

                          @Override
                          public void close() {
                              BackstackManager.getInstance().navigateBack();
                              getActivity().invalidateOptionsMenu();
                          }
                      });
                BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getName(), true);
            }
            else {
                ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
            }
        }
    });

    private final Listener<LawnNGardenSubsystem.CreateWeeklyEventResponse> createWeeklySuccessListener = Listeners.runOnUiThread(new  Listener<LawnNGardenSubsystem.CreateWeeklyEventResponse>() {
        @Override
        public void onEvent(LawnNGardenSubsystem.CreateWeeklyEventResponse clientEvent) {
            successful();
        }
    });

    private final Listener<LawnNGardenSubsystem.UpdateWeeklyEventResponse> updateWeeklySucccessListener = Listeners.runOnUiThread(new  Listener<LawnNGardenSubsystem.UpdateWeeklyEventResponse>() {
        @Override
        public void onEvent(LawnNGardenSubsystem.UpdateWeeklyEventResponse clientEvent) {
            successful();
        }
    });

    private final Listener<LawnNGardenSubsystem.RemoveWeeklyEventResponse> removeWeeklySucccessListener = Listeners.runOnUiThread(new  Listener<LawnNGardenSubsystem.RemoveWeeklyEventResponse>() {
        @Override
        public void onEvent(LawnNGardenSubsystem.RemoveWeeklyEventResponse clientEvent) {
            successful();
        }
    });

    private final Listener<LawnNGardenSubsystem.CreateScheduleEventResponse> createEventSuccessListener = Listeners.runOnUiThread(new  Listener<LawnNGardenSubsystem.CreateScheduleEventResponse>() {
        @Override
        public void onEvent(LawnNGardenSubsystem.CreateScheduleEventResponse clientEvent) {
            successful();
        }
    });

    private final Listener<LawnNGardenSubsystem.RemoveScheduleEventResponse> removeScheduleEventRequest = Listeners.runOnUiThread(new  Listener<LawnNGardenSubsystem.RemoveScheduleEventResponse>(){
        @Override
        public void onEvent(LawnNGardenSubsystem.RemoveScheduleEventResponse clientEvent) {
            successful();
        }

    });

    private final Listener<LawnNGardenSubsystem.UpdateScheduleEventResponse> updateEventSucccessListener = Listeners.runOnUiThread(new  Listener<LawnNGardenSubsystem.UpdateScheduleEventResponse>() {
        @Override
        public void onEvent(LawnNGardenSubsystem.UpdateScheduleEventResponse clientEvent) {
            successful();
        }
    });

    private final Listener<LawnNGardenSubsystem.RemoveScheduleEventResponse> removeEventSucccessListener = Listeners.runOnUiThread(new  Listener<LawnNGardenSubsystem.RemoveScheduleEventResponse>() {
        @Override
        public void onEvent(LawnNGardenSubsystem.RemoveScheduleEventResponse clientEvent) {
            successful();
        }
    });

    private final Listener<LawnNGardenSubsystem.UpdateScheduleEventResponse> updateSuccessListener = Listeners.runOnUiThread(
            new Listener<LawnNGardenSubsystem.UpdateScheduleEventResponse>() {
                @Override public void onEvent(LawnNGardenSubsystem.UpdateScheduleEventResponse updateScheduleEventResponse) {
                    successfulResponse();
                }
            }
    );
    private final Listener<LawnNGardenSubsystem.CreateScheduleEventResponse> createNonWeeklySuccessListener = Listeners.runOnUiThread(
            new Listener<LawnNGardenSubsystem.CreateScheduleEventResponse>() {
                @Override public void onEvent(LawnNGardenSubsystem.CreateScheduleEventResponse createScheduleEventResponse) {
                    successfulResponse();
                }
            }
    );

    private void successful() {
        hideProgressBar();
        BackstackManager.getInstance().navigateBack();
        getActivity().invalidateOptionsMenu();
    }

    public static IrrigationScheduleCommandEditorFragment newEditEventInstance (String deviceAddress, String deviceName, String timeOfDayCommandId, DayOfWeek currentDayOfWeek, String title, int interval) {
        IrrigationScheduleCommandEditorFragment instance = new IrrigationScheduleCommandEditorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        arguments.putString(TITLE, title);
        arguments.putString(TIME_OF_DAY_COMMAND_ID, timeOfDayCommandId);
        arguments.putSerializable(CURRENT_DAY_OF_WEEK, currentDayOfWeek);
        arguments.putInt(INTERVAL, interval);
        instance.setArguments(arguments);

        return instance;
    }

    public static IrrigationScheduleCommandEditorFragment newAddEventInstance (String deviceAddress, String deviceName, DayOfWeek currentDayOfWeek, String title, int interval) {
        IrrigationScheduleCommandEditorFragment instance = new IrrigationScheduleCommandEditorFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_ADDRESS, deviceAddress);
        arguments.putString(DEVICE_NAME, deviceName);
        arguments.putString(TITLE, title);
        arguments.putInt(INTERVAL, interval);
        arguments.putSerializable(CURRENT_DAY_OF_WEEK, currentDayOfWeek);
        instance.setArguments(arguments);

        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        schedFrag = this;
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (view != null) {
            View titleArea = view.findViewById(R.id.title_area);
            if (titleArea != null) {
                if(!isEditMode()) {
                    titleArea.setVisibility(View.VISIBLE);
                }
            }
        }

        if (isEditMode()) {

            ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
        }
        else {
            ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());
        }

        String deviceID = CorneaUtils.getIdFromAddress(getDeviceAddress());
        DeviceModel deviceModel = getCorneaService().getStore(DeviceModel.class).get(deviceID);

        if(controller == null) {
            controller = LawnAndGardenScheduleController.instance();
        }
        LawnAndGardenScheduleController.instance().setCallback(this);
        controller.setAddress(deviceModel.getAddress());

        capabilityUtils = new CapabilityUtils(deviceModel);

        setDeleteButtonVisibility(isEditMode() ? View.VISIBLE : View.GONE);
        if(isEditMode() && tod == null) {
            setStartTime();
        }
        return view;
    }

    private void setStartTime() {
        ArrayList<LinkedTreeMap<String, Object>> events = getScheduleEvents();
        for(LinkedTreeMap<String, Object> item : events) {
            String eventId = (String)item.get("eventId");
            if(eventId.equals(getTimeOfDayCommandId())) {
                String timeOfDay = (String) item.get("timeOfDay");
                tod = TimeOfDay.fromString(timeOfDay);
                setInitialEventTime(tod.getHours(), tod.getMinutes());
            }
        }
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_add_event_irrigation;
    }

    @Override
    public void onResume () {
        super.onResume();
        setUseAMPMPicker(true);
        interval = getArguments().getInt(INTERVAL);
        title = getArguments().getString(TITLE);
        controller = LawnAndGardenScheduleController.instance();
        updateView();
        enableSaveButton(zones != null && !zones.isEmpty());
    }

    @Override
    public String getTitle () {
        if(isEditMode()) {
            return getResources().getString(R.string.irrigation_event);
        }
        return getResources().getString(R.string.climate_add_event_title);
    }

    @Override
    protected int getStartTimePopupTitle() {
        return R.string.irrigation_start_time;
    }

    @Override
    public DayOfWeek getCurrentDayOfWeek() {
        return (DayOfWeek) getArguments().getSerializable(CURRENT_DAY_OF_WEEK);
    }

    @Override
    public List<DayOfWeek> getScheduledDaysOfWeek() {
        EnumSet initialDays = getSelectedDays();
        if(initialDays == null) {
            initialDays = EnumSet.of(getCurrentDayOfWeek());
        }

        ArrayList<DayOfWeek> scheduledDays = new ArrayList<>();

        if(initialDays.contains(DayOfWeek.MONDAY)) {
            scheduledDays.add(DayOfWeek.MONDAY);
        }
        if(initialDays.contains(DayOfWeek.TUESDAY)) {
            scheduledDays.add(DayOfWeek.TUESDAY);
        }
        if(initialDays.contains(DayOfWeek.WEDNESDAY)) {
            scheduledDays.add(DayOfWeek.WEDNESDAY);
        }
        if(initialDays.contains(DayOfWeek.THURSDAY)) {
            scheduledDays.add(DayOfWeek.THURSDAY);
        }
        if(initialDays.contains(DayOfWeek.FRIDAY)) {
            scheduledDays.add(DayOfWeek.FRIDAY);
        }
        if(initialDays.contains(DayOfWeek.SATURDAY)) {
            scheduledDays.add(DayOfWeek.SATURDAY);
        }
        if(initialDays.contains(DayOfWeek.SUNDAY)) {
            scheduledDays.add(DayOfWeek.SUNDAY);
        }
        return scheduledDays;
    }

    @Override
    public TimeOfDay getScheduledTimeOfDay() {
        if(isEditMode()) {
            return tod;
        }
        TimeOfDay timeOfDay = new TimeOfDay(Calendar.getInstance());
        return timeOfDay;
    }

    @Override
    protected int getDefaultHourSelection() {
        return DEFAULT_EVENT_HOUR;
    }

    @Override
    protected int getDefaultMinuteSelection() {
        return DEFAULT_EVENT_MINUTE;
    }

    @Override
    public boolean isEditMode() {
        return getTimeOfDayCommandId() != null;
    }

    @Override
    public void onDeleteEvent() {
        if(getInitialDays().size() > 1) {
            confirmDeleteAllDays();
        }
        else {
            deleteEvent(false);
        }
    }

    @Override
    public String getScheduledEntityAddress() {
        return getArguments().getString(DEVICE_ADDRESS);
    }

    @Override
    public void onRepeatChanged(Set repeatDays) {
        rebind(true, getString(R.string.irrigation_start_time), getString(R.string.irrigation_start_time_description));
    }

    @Override
    public void onSaveEvent(EnumSet selectedDays, TimeOfDay timeOfDay) {
        int selectedHour = timeOfDay.getHours();
        int selectedMinute = timeOfDay.getMinutes();

        if(selectedDays.size() > 1 && isEditMode()) {
            confirmUpdateAllDays(selectedDays, selectedHour, selectedMinute);
        }
        else {
            saveEvent(selectedDays, selectedHour, selectedMinute, false);
        }

    }

    private void saveEvent(EnumSet selectedDays, int selectedHour, int selectedMinute, boolean allDays) {
        DayOfWeek day = getCurrentDayOfWeek();

        final ArrayList<Map<String, Object>> updateZones = new ArrayList<Map<String, Object>>();
        for(int nZone = 0; nZone < zones.size(); nZone++) {
            if(zones.get(nZone).getDuration() > 0) {
                Map<String, Object> zoneinfo = new HashMap<String, Object>();
                zoneinfo.put("zone", zones.get(nZone).getZoneId());
                zoneinfo.put("duration", zones.get(nZone).getDuration());
                updateZones.add(zoneinfo);
            }
        }
        final String time = String.format("%d:%02d", selectedHour, selectedMinute);
        Set<String> days = new HashSet<String>();
        if(selectedDays.contains(DayOfWeek.MONDAY)) {
            days.add("MON");
        }
        if(selectedDays.contains(DayOfWeek.TUESDAY)) {
            days.add("TUE");
        }
        if(selectedDays.contains(DayOfWeek.WEDNESDAY)) {
            days.add("WED");
        }
        if(selectedDays.contains(DayOfWeek.THURSDAY)) {
            days.add("THU");
        }
        if(selectedDays.contains(DayOfWeek.FRIDAY)) {
            days.add("FRI");
        }
        if(selectedDays.contains(DayOfWeek.SATURDAY)) {
            days.add("SAT");
        }
        if(selectedDays.contains(DayOfWeek.SUNDAY)) {
            days.add("SUN");
        }

        showProgressBar();
        if(title==null){
            title="";
        }
        switch (title){
            case IrrigationScheduleStatus.MODE_WEEKLY:
                if (isEditMode()) {
                    if(allDays) {
                        controller.updateWeekEvent(getDeviceAddress(), getTimeOfDayCommandId(), days, time, updateZones, null, updateWeeklySucccessListener, failureListener);
                    }
                    else {
                        controller.updateWeekEvent(getDeviceAddress(), getTimeOfDayCommandId(), null, time, updateZones, getTodayFormatted(), updateWeeklySucccessListener, failureListener);
                    }

                } else {
                    controller.createWeekEvent(getDeviceAddress(), days, updateZones, time, createWeeklySuccessListener, failureListener);
                }
                return;
            case IrrigationScheduleStatus.MODE_INTERVAL:
                if (isEditMode()) {
                    updateScheduleEvent(getDeviceAddress(), IrrigationScheduleStatus.MODE_INTERVAL, getTimeOfDayCommandId(), time, updateZones);
                }
                else {
                    if(interval == 0) {
                        interval = 1;
                    }
                    controller.configureIntervalSchedule(getDeviceAddress(), new Date(System.currentTimeMillis()), interval, Listeners.runOnUiThread(new Listener<LawnNGardenSubsystem.ConfigureIntervalScheduleResponse>() {
                        @Override
                        public void onEvent(LawnNGardenSubsystem.ConfigureIntervalScheduleResponse response) {
                            createIntervalSchedule(updateZones, time);
                        }
                    }), failureListener);
                }
                return;
            case IrrigationScheduleStatus.MODE_ODD:
                if (isEditMode()) {
                    updateScheduleEvent(getDeviceAddress(), IrrigationScheduleStatus.MODE_ODD, getTimeOfDayCommandId(), time, updateZones);
                } else {
                    controller.createScheduleEvent(getDeviceAddress(), IrrigationScheduleStatus.MODE_ODD, updateZones, time, createNonWeeklySuccessListener, failureListener);
                }
                return;
            case IrrigationScheduleStatus.MODE_EVEN:
                if (isEditMode()) {
                    updateScheduleEvent(getDeviceAddress(), IrrigationScheduleStatus.MODE_EVEN, getTimeOfDayCommandId(), time, updateZones);
                } else {
                    controller.createScheduleEvent(getDeviceAddress(), IrrigationScheduleStatus.MODE_EVEN,updateZones,time, createNonWeeklySuccessListener, failureListener);
                }
        }
    }

    protected void updateScheduleEvent(String deviceAddress, String mode, String eventId, String timeOfDay, List<Map<String, Object>> zoneDurations) {
        controller.updateScheduleEvent(deviceAddress, mode, eventId, timeOfDay, zoneDurations, updateSuccessListener, failureListener);
    }

    protected void createIntervalSchedule(List<Map<String, Object>> updateZones, String time) {
        controller.createScheduleEvent(getDeviceAddress(), IrrigationScheduleStatus.MODE_INTERVAL, updateZones, time, createNonWeeklySuccessListener, failureListener);
    }

    protected void successfulResponse() {
        hideProgressBar();
        BackstackManager.getInstance().navigateBack();
        getActivity().invalidateOptionsMenu();
    }


    private void deleteEvent(boolean bAllDays) {
        showProgressBar();
        if(title==null)title="";
        switch (title){
            case IrrigationScheduleStatus.MODE_WEEKLY:
                if(bAllDays) {
                    controller.removeWeeklySchedule(getDeviceAddress(), getTimeOfDayCommandId(), null, removeWeeklySucccessListener, failureListener);
                }
                else {
                    controller.removeWeeklySchedule(getDeviceAddress(), getTimeOfDayCommandId(), getTodayFormatted(), removeWeeklySucccessListener, failureListener);
                }
                return;
            case IrrigationScheduleStatus.MODE_INTERVAL:
                    controller.removeScheduleEvent(getDeviceAddress(),IrrigationScheduleStatus.MODE_INTERVAL,getTimeOfDayCommandId(),removeScheduleEventRequest,failureListener);
                return;
            case IrrigationScheduleStatus.MODE_ODD:
                controller.removeScheduleEvent(getDeviceAddress(),IrrigationScheduleStatus.MODE_ODD,getTimeOfDayCommandId(),removeScheduleEventRequest,failureListener);
                return;
            case IrrigationScheduleStatus.MODE_EVEN:
                controller.removeScheduleEvent(getDeviceAddress(),IrrigationScheduleStatus.MODE_EVEN,getTimeOfDayCommandId(),removeScheduleEventRequest,failureListener);

        }

    }

    private String getTodayFormatted() {
        DayOfWeek currentDay = getCurrentDayOfWeek();
        String today = "";
        switch (currentDay) {
            case SUNDAY:
                today = "SUN";
                break;
            case MONDAY:
                today = "MON";
                break;
            case TUESDAY:
                today = "TUE";
                break;
            case WEDNESDAY:
                today = "WED";
                break;
            case THURSDAY:
                today = "THU";
                break;
            case FRIDAY:
                today = "FRI";
                break;
            case SATURDAY:
                today = "SAT";
                break;

        }
        return today;
    }

    public void confirmDeleteAllDays() {
        hideProgressBar();

        AlertFloatingFragment deleteWhichDayPrompt = AlertFloatingFragment.newInstance(
                getString(R.string.climate_edit_event_error_title),
                getString(R.string.climate_edit_event_error_description),
                getString(R.string.climate_edit_selected_day),
                getString(R.string.climate_edit_all_days),
                new AlertFloatingFragment.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        deleteEvent(false);
                        bAllDays = false;
                        return true;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        deleteEvent(true);
                        return true;
                    }
                }
        );

        BackstackManager.getInstance().navigateToFloatingFragment(deleteWhichDayPrompt, deleteWhichDayPrompt.getClass().getSimpleName(), true);
    }

    public void confirmUpdateAllDays(final EnumSet selectedDays, final int selectedHour, final int selectedMinute) {
        hideProgressBar();

        final String EDIT_ALL = "EDIT_ALL";
        final String EDIT_SELECTED = "EDIT_SELECTED";

        HashMap<String,String> editChoices = new HashMap<>();
        editChoices.put(getString(R.string.climate_edit_all_days), EDIT_ALL);
        editChoices.put(getString(R.string.climate_edit_selected_day), EDIT_SELECTED);

        ButtonListPopup editWhichDayPopup = ButtonListPopup.newInstance(
                editChoices,
                R.string.climate_edit_event_title,
                R.string.climate_edit_event_description);

        editWhichDayPopup.setCallback(new ButtonListPopup.Callback() {
            @Override
            public void buttonSelected(String buttonKeyValue) {
                if (EDIT_ALL.equals(buttonKeyValue)) {
                    saveEvent(selectedDays, selectedHour, selectedMinute, true);
                } else {
                    saveEvent(selectedDays, selectedHour, selectedMinute, false);
                }
                BackstackManager.getInstance().navigateBack();
            }
        });

        BackstackManager.getInstance().navigateToFloatingFragment(editWhichDayPopup, editWhichDayPopup.getClass().getSimpleName(), true);
    }

    private String getDeviceName() {
        return getArguments().getString(DEVICE_NAME);
    }

    private String getDeviceAddress() {
        return getArguments().getString(DEVICE_ADDRESS);
    }

    private String getTimeOfDayCommandId() {
        return getArguments().getString(TIME_OF_DAY_COMMAND_ID, null);
    }

    @Override
    public void updateView() {
        if(isEditMode()) {
            ArrayList<LinkedTreeMap<String, Object>> events = getScheduleEvents();
            for(LinkedTreeMap<String, Object> item : events) {
                String eventId = (String)item.get("eventId");
                if(eventId.equals(getTimeOfDayCommandId())) {
                    switch(title) {
                        case IrrigationSchedule.TYPE_WEEKLY:
                            parseEventDays(item);
                            break;
                        default:
                            break;
                    }
                }
            }
        }
        rebind(true, getString(R.string.irrigation_start_time), getString(R.string.irrigation_start_time_description));

    }

    private void parseEventDays(LinkedTreeMap<String, Object> item) {
        EnumSet<DayOfWeek> repetitions = EnumSet.of(DayOfWeek.FRIDAY);
        repetitions.remove(DayOfWeek.FRIDAY);
        ArrayList<String> eventDays = (ArrayList<String>) item.get("days");
        if(eventDays.contains("MON")) {
            repetitions.add(DayOfWeek.MONDAY);
        }
        if(eventDays.contains("TUE")) {
            repetitions.add(DayOfWeek.TUESDAY);
        }
        if(eventDays.contains("WED")) {
            repetitions.add(DayOfWeek.WEDNESDAY);
        }
        if(eventDays.contains("THU")) {
            repetitions.add(DayOfWeek.THURSDAY);
        }
        if(eventDays.contains("FRI")) {
            repetitions.add(DayOfWeek.FRIDAY);
        }
        if(eventDays.contains("SAT")) {
            repetitions.add(DayOfWeek.SATURDAY);
        }
        if(eventDays.contains("SUN")) {
            repetitions.add(DayOfWeek.SUNDAY);
        }
        setSelectedDays(repetitions);
    }

    @Override
    public void subsystemUpdate() {
        //TODO: Not used, needs to be optional override.
    }

    @Override
    public SettingsList getEditableCommandAttributes() {
        SettingsList settings = new SettingsList();
        //TODO: settings.add is adding start time without adding it to the fragment? Needs to be fixed to make custom view for Interval Odd/Even
        displaySetup();
        settings.add(buildZoneSetting());
        if(getScheduledDaysOfWeek().size() > 1) {
            settings.add(buildRepeatOnSetting());
        }
        return settings;
    }

    private Setting buildZoneSetting () {
        int totalDuration;
        if (zones == null && isEditMode()) {
            totalDuration = buildExistingZoneDurationInfo();
        }
        else {
            totalDuration = resetOrCreateZoneDurationInfo();
        }

        String duration = getInitialDurationStringAbstract(totalDuration);
        OnClickActionSetting zoneSetting = new OnClickActionSetting(getString(R.string.irrigation_zones), getString(R.string.irrigation_zones_description), duration);
            zoneSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LinkedHashMap<String, Integer> zoneInfo = new LinkedHashMap<>();
                    for (IrrigationZoneInfo zone : zones) {
                        zoneInfo.put(zone.getZoneId(), zone.getDuration());
                    }
                    IrrigationZoneCardListFragment frag = IrrigationZoneCardListFragment.newInstance(getDeviceAddress(), zoneInfo, isEditMode());
                    frag.setCallback(schedFrag);
                    BackstackManager.getInstance().navigateToFragment(frag, true);
                }
            });

        return zoneSetting;
    }

    protected int buildExistingZoneDurationInfo() {
        zones = new ArrayList<>();
        int totalDuration = 0;

        ArrayList<LinkedTreeMap<String, Object>> events = getScheduleEvents();
        for(LinkedTreeMap<String, Object> item : events) {
            String eventId = (String) item.get("eventId");
            if(eventId.equals(getTimeOfDayCommandId())) {
                ArrayList<LinkedTreeMap<String,Object>> zoneEvents = (ArrayList<LinkedTreeMap<String,Object>>)item.get("events");
                for(LinkedTreeMap<String, Object> zoneInfo : zoneEvents) {
                    IrrigationZoneInfo zone = new IrrigationZoneInfo();
                    zone.setDuration((int)(double)zoneInfo.get("duration"));
                    zone.setZoneId((String) zoneInfo.get("zone"));
                    this.zones.add(zone);
                    totalDuration += zone.getDuration();
                }
            }
        }

        return totalDuration;
    }

    // For future use if we start adding default zones + duration. Right now will always be 0;
    protected int resetOrCreateZoneDurationInfo() {
        int totalDuration = 0;
        if (zones == null) {
            zones = new ArrayList<>();
            return totalDuration;
        }

        for (IrrigationZoneInfo item : zones) {
            totalDuration += item.getDuration();
        }

        return totalDuration;
    }

    protected String getInitialDurationStringAbstract(int forDuration) {
        if (forDuration == 0) {
            return "";
        }

        return getResources().getQuantityString(R.plurals.care_minutes_plural, forDuration, forDuration);
    }

    private Setting buildRepeatOnSetting () {
        setRepeatRegionVisibility(View.GONE);
        String dayDisplay = "";
        List<DayOfWeek> days = getScheduledDaysOfWeek();
        if (days != null && !days.isEmpty()) {
            dayDisplay = StringUtils.getScheduleAbstract(getActivity(), Sets.newHashSet(days));
        }

        OnClickActionSetting repeatSetting = new OnClickActionSetting(getString(R.string.irrigation_repeat_on), null, dayDisplay);
        repeatSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepeatPicker();
            }
        });

        return repeatSetting;
    }


    @Override
    public void onSchedulerError(Throwable cause) {
        hideProgressBar();
    }

    @Override
    public void onTimeOfDayCommandLoaded(ScheduleCommandModel scheduleCommandModel) {
        hideProgressBar();
    }

    @Override
    public void onSuccess() {
        hideProgressBar();
    }

    @Override
    public void setSelectedZones(List<IrrigationZoneListItemModel> items) {
        zones.clear();
        for(IrrigationZoneListItemModel model : items) {
            IrrigationZoneInfo zone = new IrrigationZoneInfo();
            zone.setDuration(model.getIrrigationZoneCard().getDuration());
            zone.setZoneId(model.getIrrigationZoneCard().getZoneId());
            zones.add(zone);
        }
        rebind(true, getString(R.string.irrigation_start_time), getString(R.string.irrigation_start_time_description));
    }

    private ArrayList<LinkedTreeMap<String, Object>> getScheduleEvents() {
        if(title == null) {
            title = getArguments().getString(TITLE);
        }
        ArrayList<LinkedTreeMap<String, Object>> events = new ArrayList<>();
        switch(title) {
            case IrrigationScheduleStatus.MODE_WEEKLY:
                events = controller.getWeeklySchedule(getDeviceAddress());
                break;
            case IrrigationScheduleStatus.MODE_INTERVAL:
                events = controller.getIntervalSchedule(getDeviceAddress());
                break;
            case IrrigationScheduleStatus.MODE_ODD:
                events = controller.getOddSchedule(getDeviceAddress());
                break;
            case IrrigationScheduleStatus.MODE_EVEN:
                events = controller.getEvenSchedule(getDeviceAddress());
                break;
        }
        return events;
    }

  private void displaySetup(){
      if (TextUtils.isEmpty(title)) {
          return;
      }
      switch(title) {
            case IrrigationSchedule.TYPE_WEEKLY:
                setRepeatRegionVisibility(View.VISIBLE);
                return;
          default:
              setRepeatRegionVisibility(View.GONE);
      }

    }
}
