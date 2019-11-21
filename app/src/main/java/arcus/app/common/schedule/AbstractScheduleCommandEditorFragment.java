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
package arcus.app.common.schedule;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;

import arcus.cornea.events.TimeSelectedEvent;
import arcus.cornea.utils.DateUtils;
import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.SunriseSunset;
import arcus.cornea.utils.TimeOfDay;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.popups.AMPMTimePopupWithHeader;
import arcus.app.common.popups.ButtonListPopup;
import arcus.app.common.popups.DayOfTheWeekPopup;
import arcus.app.common.popups.SunriseSunsetPicker;
import arcus.app.common.schedule.controller.ScheduleCommandEditController;
import arcus.app.common.schedule.model.ScheduleCommandModel;
import arcus.app.common.sequence.SequenceController;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.settings.adapter.SettingsListAdapter;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.style.OnClickActionSetting;
import arcus.app.subsystems.alarm.AlertFloatingFragment;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import de.greenrobot.event.EventBus;


public abstract class AbstractScheduleCommandEditorFragment<K extends SequenceController> extends SequencedFragment<K> {

    private final int DEFAULT_EVENT_HOUR = 18;
    private final int DEFAULT_EVENT_MINUTE = 0;

    private Integer selectedHour;
    private Integer selectedMinute;
    private SunriseSunset sunriseSunset;
    private Integer sunriseSunsetOffset;
    private EnumSet<DayOfWeek> selectedDays;

    private RelativeLayout repeatClickableRegion;
    private Version1Button saveButton;
    private Version1Button deleteButton;
    private Version1TextView repeatButton;
    private ListView editablePropertiesList;
    private SettingsListAdapter editablePropertiesAdapter;

    private Drawable whiteOutline;
    private Drawable blackOutline;

    private OnClickActionSetting runtimeSetting;

    private boolean useAMPMPicker = false;

    protected Version1TextView tapCopy;

    /**
     * The current day of the week in the context of editing/adding the schedule event; this should
     * be the day of the week selected in the radio group of the schedule editor fragment (not
     * the current calendar day-of-week as returned by the OS).
     *
     * @return The day of the week currently selected in the radio group.
     */
    public abstract DayOfWeek getCurrentDayOfWeek();

    /**
     * The days of the week that this event is currently scheduled to occur on. Used only when editing
     * an existing schedule event.
     *
     * @return The days of the week on which this command should repeat. When adding a new command,
     * should be a singleton set containing the day returned by {@link #getCurrentDayOfWeek()}.
     */
    public abstract List<DayOfWeek> getScheduledDaysOfWeek ();

    /**
     * Gets the time of day this event is scheduled to run on; either a default time (when adding
     * a new event) or the existing time (when editing an event).
     *
     * @return The time of day when this command should execute.
     */
    public abstract TimeOfDay getScheduledTimeOfDay ();

    /**
     * Determines whether we're editing an existing schedule element or creating a new one; affects
     * background and text colors.
     *
     * @return True when editing an existing command; false when adding a new one.
     */
    public abstract boolean isEditMode ();

    /**
     * Gets the set of editable properties that should be displayed in the editor. Note that a
     * runtime property will always be displayed; return a new SettingsList() for the default
     * properties.
     *
     * @return A SettingsList in which each element in the list represents a "menu item" displayed
     * on the fragment allowing the user to adjust command attributes (i.e., like the state of a
     * door, the brightness of a dimmer, etc.)
     */
    public abstract SettingsList getEditableCommandAttributes();

    /**
     * Invoked when the user chooses to save the current scheduler selections.
     *
     * @param selectedDays The days of the week on which the user requested the command be repeated.
     *                     Will be the same value last passed to the delegate through the
     *                     {@link #onRepeatChanged(Set)} method.
     * @param timeOfDay The time of day the event is scheduled for - or sunrise/sunset time.
     */
    public abstract void onSaveEvent(EnumSet<DayOfWeek> selectedDays, TimeOfDay timeOfDay);

    /**
     * Invoked when the user chooses to delete the current scheduler event.
     */
    public abstract void onDeleteEvent ();

    /**
     * Invoked when the user changes the days of the week on which this command repeats.
     *
     * @param repeatDays The set of days the user has chosen for the command to repeat.
     */
    public abstract void onRepeatChanged (Set<DayOfWeek> repeatDays);

    /**
     * Gets the address of the device (or addressable entity) that is being scheduled.
     * @return The device address being scheduled
     */
    public abstract String getScheduledEntityAddress();

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        editablePropertiesList = (ListView) view.findViewById(R.id.editable_properties_list);
        repeatClickableRegion = (RelativeLayout) view.findViewById(R.id.repeat_clickable_region);
        saveButton = (Version1Button) view.findViewById(R.id.save_button);
        deleteButton = (Version1Button) view.findViewById(R.id.delete_button);
        tapCopy = (Version1TextView) view.findViewById(R.id.tap_copy);
        repeatButton = (Version1TextView) view.findViewById(R.id.repeat_button);
        whiteOutline = ContextCompat.getDrawable(getActivity(), R.drawable.outline_button_style);
        blackOutline = ContextCompat.getDrawable(getActivity(), R.drawable.outline_button_style_black);

        return view;
    }


    @Override
    public void onResume () {
        super.onResume();
        getActivity().setTitle(getTitle());
        getActivity().invalidateOptionsMenu();

        rebind();
    }

    public void setUseAMPMPicker(boolean value) {
        useAMPMPicker = value;
    }

    protected int getStartTimePopupTitle() {
        return R.string.picker_time_text;
    }

    /**
     * Redraws the fragment, rebinding data provided through the abstract template methods to the
     * UI elements. Similar to notifyDataSetChanged() in a ListView. Invoke this method if you need
     * to refresh the data visible in the UI.
     */
    public void rebind () {

        SettingsList editProps = new SettingsList();
        runtimeSetting = new OnClickActionSetting(getString(R.string.scene_start_time), null, new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!useAMPMPicker) {
                    showSunriseSunsetPicker();
                }
                else {
                    showAMPMPicker();
                }
            }
        });

        editProps.setUseLightColorScheme(isEditMode());
        runtimeSetting.setSelectionAbstract(DateUtils.format(getTimeOfDay(), false));

        SettingsList moreSettings = getEditableCommandAttributes();
        //this block of code re-orders the elements in the scheduler for consistency so that the run time is always second to last when repeat is on.
        if (moreSettings != null){
            if (moreSettings.getSettings().size() == 2){
                editProps.add(moreSettings.getSettings().get(0));
                editProps.add(runtimeSetting);
                editProps.add(moreSettings.getSettings().get(1));
            }
             else if (moreSettings.getSettings().size() == 3){
                editProps.add(moreSettings.getSettings().get(0));
                editProps.add(moreSettings.getSettings().get(1));
                editProps.add(runtimeSetting);
                editProps.add(moreSettings.getSettings().get(2));
            }
            else {
                for (Setting  setting: moreSettings.getSettings()) {
                    editProps.add(setting);
                }
                editProps.add(runtimeSetting);
            }
        }
        setEditableProperties(editProps);

    }


    protected void showAMPMPicker() {
        AMPMTimePopupWithHeader picker = AMPMTimePopupWithHeader.newInstanceAsTimeOnly(
              getRuntimeHourSelection(),
              getRuntimeMinuteSelection(),
              getResources().getString(getStartTimePopupTitle()),
              true
        );
        BackstackManager.getInstance().navigateToFloatingFragment(picker, picker.getClass().getCanonicalName(), true);
    }

    protected void showSunriseSunsetPicker() {
        SunriseSunsetPicker picker = SunriseSunsetPicker.newInstance(getTimeOfDay());
        picker.setCallback(new SunriseSunsetPicker.Callback() {
            @Override public void selection(TimeOfDay selectedTimeOfDay) {
                selectedHour        = selectedTimeOfDay.getHours();
                selectedMinute      = selectedTimeOfDay.getMinutes();
                sunriseSunset       = selectedTimeOfDay.getSunriseSunset();
                sunriseSunsetOffset = selectedTimeOfDay.getOffset();

                runtimeSetting.setSelectionAbstract(DateUtils.format(getTimeOfDay(), false));
            }
        });
        BackstackManager.getInstance().navigateToFloatingFragment(picker, picker.getClass().getCanonicalName(), true);
    }

    public void rebind (boolean bTimeOrderedFirst, String strTimeHeader, String strTimeSub) {

        //these will have the temp in them.
        SettingsList editProps = new SettingsList();
        runtimeSetting = new OnClickActionSetting(strTimeHeader, strTimeSub, new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (!useAMPMPicker) {
                    showSunriseSunsetPicker();
                }
                else {
                    showAMPMPicker();
                }
            }
        });
        runtimeSetting.setSelectionAbstract(DateUtils.format(getTimeOfDay(), false));

        if (bTimeOrderedFirst){
            editProps.add(runtimeSetting);

            SettingsList moreSettings = getEditableCommandAttributes();
            if (moreSettings != null){
                for (Setting  setting: moreSettings.getSettings()) {
                    editProps.add(setting);
                }
           }
        } else {
            if (getEditableCommandAttributes() != null){
                editProps = getEditableCommandAttributes();
            }
            editProps.add(runtimeSetting);
        }

        editProps.setUseLightColorScheme(isEditMode());
        setEditableProperties(editProps);
    }

    private void setEditableProperties(SettingsList editProps) {
        editablePropertiesAdapter = new SettingsListAdapter(getActivity(), editProps);
        editablePropertiesList.setAdapter(editablePropertiesAdapter);

        repeatClickableRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRepeatPicker();
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveEvent(selectedDays, getTimeOfDay());
            }
        });

        deleteButton.setColorScheme(Version1ButtonColor.MAGENTA);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteEvent();
            }
        });

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        runtimeSetting.setSelectionAbstract(DateUtils.format(getTimeOfDay(), false));

        selectedDays = getInitialDays();

        saveButton.setColorScheme(isEditMode() ? Version1ButtonColor.WHITE : Version1ButtonColor.BLACK);
        tapCopy.setTextColor(isEditMode() ? getResources().getColor(R.color.white_with_35) : getResources().getColor(R.color.black_with_60));
        repeatButton.setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);
        repeatButton.setBackground(isEditMode() ? whiteOutline : blackOutline);

        if (isEditMode()) {
            ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
        } else {
            ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());
        }
    }

    @Override
    public void onPause () {
        super.onPause();

        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Nullable
    @Override
    public String getTitle() {
        return getCurrentDayOfWeek().toString().toUpperCase();
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_add_event;
    }

    public void showRepeatPicker () {
        DayOfTheWeekPopup picker = DayOfTheWeekPopup.newInstance(selectedDays);
        picker.setCallback(new DayOfTheWeekPopup.Callback() {
            @Override
            public void selectedItems(EnumSet<DayOfWeek> dayOfWeek) {
                selectedDays = dayOfWeek;
                onRepeatChanged(selectedDays);
            }
        });
        BackstackManager.getInstance().navigateToFloatingFragment(picker, picker.getClass().getCanonicalName(), true);
    }

    public void setRepeatRegionVisibility (int visibility) {
        tapCopy.setVisibility(visibility);
        repeatClickableRegion.setVisibility(visibility);
    }

    public void setDeleteButtonVisibility (int visibility) {
        deleteButton.setVisibility(visibility);
    }

    public void setSelectedDays (EnumSet<DayOfWeek> selectedDays) {
        this.selectedDays = selectedDays;
    }

    public void setInitialEventTime(int hour, int min) {
        selectedHour = hour;
        selectedMinute = min;
    }

    public final void onEvent(TimeSelectedEvent event) {
        selectedHour = event.getHourValue();
        selectedMinute = event.getMinuteValue();
        sunriseSunset = SunriseSunset.ABSOLUTE;

        runtimeSetting.setSelectionAbstract(DateUtils.format(getTimeOfDay(), false));
    }

    private Integer getRuntimeOffsetDuration() {
        if (sunriseSunsetOffset != null) {
            return sunriseSunsetOffset;
        }

        if (isEditMode() && getScheduledTimeOfDay() != null) {
            return getScheduledTimeOfDay().getOffset();
        }

        return null;
    }

    private SunriseSunset getRuntimeSunriseSunset() {
        if (sunriseSunset != null) {
            return sunriseSunset;
        }

        if (isEditMode() && getScheduledTimeOfDay() != null) {
            return getScheduledTimeOfDay().getSunriseSunset();
        }

        return SunriseSunset.ABSOLUTE;
    }

    private int getRuntimeHourSelection() {
        if (selectedHour != null) {
            return selectedHour;
        }
        else if (isEditMode() && getScheduledTimeOfDay() != null) {
            return getScheduledTimeOfDay().getHours();
        } else {
            return getDefaultHourSelection();
        }
    }

    private int getRuntimeMinuteSelection() {
        if (selectedMinute != null) {
            return selectedMinute;
        }
        else if (isEditMode() && getScheduledTimeOfDay() != null) {
            return getScheduledTimeOfDay().getMinutes();
        } else {
            return getDefaultMinuteSelection();
        }
    }

    protected int getDefaultHourSelection() {
        return DEFAULT_EVENT_HOUR;
    }

    protected int getDefaultMinuteSelection() {
        return DEFAULT_EVENT_MINUTE;
    }

    protected EnumSet<DayOfWeek> getInitialDays () {
        if (selectedDays != null) {
            return selectedDays;
        } else if (isEditMode() && getScheduledDaysOfWeek() != null ) {
            return EnumSet.copyOf(getScheduledDaysOfWeek());
        } else {
            return EnumSet.of(getCurrentDayOfWeek());
        }
    }

    protected EnumSet<DayOfWeek> getSelectedDays() {
        return selectedDays;
    }

    public void onConfirmDeleteAllDays(final ScheduleCommandModel scheduleCommandModel) {
        hideProgressBar();

        AlertFloatingFragment deleteWhichDayPrompt = AlertFloatingFragment.newInstance(
                getString(R.string.climate_edit_event_error_title),
                getString(R.string.climate_edit_event_error_description),
                getString(R.string.climate_edit_selected_day),
                getString(R.string.climate_edit_all_days),
                new AlertFloatingFragment.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        ScheduleCommandEditController.getInstance().deleteCommandSingleDay(getScheduledEntityAddress(), scheduleCommandModel, getCurrentDayOfWeek());
                        return true;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        ScheduleCommandEditController.getInstance().deleteCommandAllDays(getScheduledEntityAddress(), scheduleCommandModel);
                        return true;
                    }
                }
        );

        BackstackManager.getInstance().navigateToFloatingFragment(deleteWhichDayPrompt, deleteWhichDayPrompt.getClass().getSimpleName(), true);
    }

    public void onConfirmUpdateAllDays(final ScheduleCommandModel scheduleCommandModel) {
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
                    ScheduleCommandEditController.getInstance().updateCommandAllDays(getScheduledEntityAddress(), scheduleCommandModel);
                } else {
                    ScheduleCommandEditController.getInstance().updateCommandSingleDay(getScheduledEntityAddress(), scheduleCommandModel, getCurrentDayOfWeek());
                }
                BackstackManager.getInstance().navigateBack();
            }
        });

        BackstackManager.getInstance().navigateToFloatingFragment(editWhichDayPopup, editWhichDayPopup.getClass().getSimpleName(), true);
    }

    protected void enableSaveButton(boolean enabled) {
        if (saveButton == null) {
            return;
        }

        saveButton.setEnabled(enabled);
    }





    protected TimeOfDay getTimeOfDay() {
        SunriseSunset sunriseSet = getRuntimeSunriseSunset();
        Integer offset = getRuntimeOffsetDuration();
        if (SunriseSunset.ABSOLUTE.equals(sunriseSet) || offset == null) {
            return new TimeOfDay(getRuntimeHourSelection(), getRuntimeMinuteSelection(), 0);
        }
        else {
            return new TimeOfDay(getRuntimeHourSelection(), getRuntimeMinuteSelection(), 0, sunriseSet, offset);
        }
    }
}
