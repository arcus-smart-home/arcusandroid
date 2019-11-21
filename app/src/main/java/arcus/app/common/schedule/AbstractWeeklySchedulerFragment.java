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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import arcus.cornea.utils.DayOfWeek;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.schedule.adapter.ScheduleCommandAdapter;
import arcus.app.common.schedule.model.ScheduleCommandModel;
import arcus.app.common.sequence.SequenceController;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;


public abstract class AbstractWeeklySchedulerFragment<T extends SequenceController> extends SequencedFragment<T> {

    private LinearLayout noSchedulesCopyLayout;
    private TextView noSchedulesCopyTitle;
    private TextView noSchedulesCopyDesc;
    private Version1Button addEventButton;
    private RadioGroup dayOfWeekGroup;
    protected ListView schedulesList;

    /**
     * Called to indicate the user has selected a new day-of-week radio button. Typically, the
     * implementer should then determine the set of scheduled events for this day and update the
     * view by calling {@link #setScheduledCommandsAdapter(ListAdapter)} with a new list adapter.
     *
     * @param selectedDayOfWeek Day of the week selected by the user
     */
    public abstract void onDayOfWeekChanged(DayOfWeek selectedDayOfWeek) ;


    /**
     * Called to indicate the user has requested to add a new event. Typically, the implementer will
     * transition to the new event editor fragment, an implementation of
     * {@link AbstractScheduleCommandEditorFragment}.
     */
    public abstract void onAddCommand();

    /**
     * Called to indicate the user has requested to edit an existing event. Typically, the implementer
     * will transition to the event editor fragment, an implementation of
     * {@link AbstractScheduleCommandEditorFragment}.
     *
     * @param command The selected shchedule event object as returned from the list adapter's {@link ListAdapter#getItem(int)} method.
     */
    public abstract void onEditCommand(Object command);

    /**
     * Called to retrieve the "no events available" title copy (shown in bold), typically some string like
     * "Set it and forget it".
     *
     * @return The title copy string to display
     */
    public abstract String getNoCommandsTitleCopy();

    /**
     * Called to retrieve the "no events available" descriptive copy, typically some string like
     * "Tap Add Event below to create a schedule for this day."
     *
     * @return The descriptive copy string to display
     */
    public abstract String getNoCommandsDescriptionCopy();

    /**
     * Called (initially, on resume) to determine if the screen should be rendered in add or edit
     * mode; edit mode uses a darkened background with white text, add mode a lightened background
     * with black text.
     *
     * @return True to indicate edit mode; false to indicate add mode
     */
    public abstract boolean isEditMode ();

    /**
     * Gets the address of the device (or addressable entity) that is being scheduled.
     * @return The device address being scheduled
     */
    public abstract String getScheduledEntityAddress();



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        noSchedulesCopyLayout = (LinearLayout) view.findViewById(R.id.no_schedules_copy);
        noSchedulesCopyTitle = (TextView) view.findViewById(R.id.no_schedules_copy_title);
        noSchedulesCopyDesc = (TextView) view.findViewById(R.id.no_schedules_copy_desc);
        addEventButton = (Version1Button) view.findViewById(R.id.add_event_button);
        dayOfWeekGroup = (RadioGroup) view.findViewById(R.id.day_of_week_radio_group);
        schedulesList = (ListView) view.findViewById(R.id.schedules);
        schedulesList.setDivider(null);

        dayOfWeekGroup.check(R.id.radio_monday);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(getTitle());

        // Initialize color scheme per edit mode
        noSchedulesCopyTitle.setTextColor(isEditMode() ? Color.WHITE : Color.BLACK);
        noSchedulesCopyDesc.setTextColor(isEditMode() ? getResources().getColor(R.color.white_with_35) : getResources().getColor(R.color.black_with_60));
        addEventButton.setColorScheme(isEditMode() ? Version1ButtonColor.WHITE : Version1ButtonColor.BLACK);
        for (int buttonIndex = 0; buttonIndex < dayOfWeekGroup.getChildCount(); buttonIndex++) {
            RadioButton thisButton = (RadioButton) dayOfWeekGroup.getChildAt(buttonIndex);
            thisButton.setTextColor(isEditMode() ? getResources().getColorStateList(R.color.radio_text_white) : getResources().getColorStateList(R.color.radio_text_black));
            thisButton.setBackgroundResource(isEditMode() ? R.drawable.day_of_week_radio_drawable_white : R.drawable.day_of_week_radio_drawable_black);
        }

        if (isEditMode()) {
            ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
        } else {
            ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());
        }

        addEventButton.setColorScheme(Version1ButtonColor.WHITE);

        dayOfWeekGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                onDayOfWeekChanged(getSelectedDayOfWeek());
            }
        });

        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAddCommand();
            }
        });

        schedulesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                onEditCommand(parent.getAdapter().getItem(position));
            }
        });
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_weekly_scheduler;
    }

    /**
     * Returns the currently selected day of week as indicated by the highlighted radio button
     * at the top of the fragment.
     *
     * @return Currently selected day of the week
     */
    public DayOfWeek getSelectedDayOfWeek () {
        switch (dayOfWeekGroup.getCheckedRadioButtonId()) {
            case R.id.radio_monday: return DayOfWeek.MONDAY;
            case R.id.radio_tuesday: return DayOfWeek.TUESDAY;
            case R.id.radio_wednesday: return DayOfWeek.WEDNESDAY;
            case R.id.radio_thursday: return DayOfWeek.THURSDAY;
            case R.id.radio_friday: return DayOfWeek.FRIDAY;
            case R.id.radio_saturday: return DayOfWeek.SATURDAY;
            case R.id.radio_sunday: return DayOfWeek.SUNDAY;

            default:
                return DayOfWeek.MONDAY;
        }
    }

    /**
     * Sets the title of the "add event" button that appears on the fragment. Useful for screen
     * variants that use a different button name.
     *
     * @param title The name of the add event button.
     */
    public void setAddCommandButtonTitle(String title) {
        addEventButton.setText(title);
    }

    /**
     * Sets the days of the week that are known to have scheduled commands executing on them. Draws
     * an empty circle around those day-of-week radio buttons as visual indication to the user that
     * commands exist.
     *
     * @param daysOfWeek The set of days which have active schedule commands executing.
     */
    public void setScheduledDaysOfWeek (Set<DayOfWeek> daysOfWeek) {
        // Draw rings around day-of-week radio buttons which schedules; always ring the selected day
        for (DayOfWeek thisDay : EnumSet.allOf(DayOfWeek.class)) {
            boolean hasSchedules = daysOfWeek.contains(thisDay) || thisDay == getSelectedDayOfWeek();

            Drawable highlight = !hasSchedules ? null : isEditMode() ? getResources().getDrawable(R.drawable.day_of_week_radio_drawable_white) : getResources().getDrawable(R.drawable.day_of_week_radio_drawable_black);
            dayOfWeekGroup.findViewById(getDayOfWeekRadioButton(thisDay)).setBackground(highlight);
        }
    }

    /**
     * Sets the {@link ListAdapter} used to render the list of schedule commands on the fragment.
     * Typically an instance of {@link arcus.app.common.schedule.adapter.ScheduleCommandAdapter}
     * should be provided, but any adapter is acceptable.
     *
     * @param adapter The adapter used to render the list of schedule commands.
     */
    public void setScheduledCommandsAdapter(ListAdapter adapter) {
        schedulesList.setAdapter(adapter);
        setNoScheduledCommandsCopyVisibility(adapter == null || adapter.getCount() == 0);
    }

    public void onScheduledCommandsLoaded(DayOfWeek selectedDay, List<ScheduleCommandModel> scheduledEvents, Set<DayOfWeek> daysWithScheduledEvents) {
        hideProgressBar();
        setScheduledCommandsAdapter(new ScheduleCommandAdapter(getActivity(), scheduledEvents, isEditMode()));
        setScheduledDaysOfWeek(daysWithScheduledEvents);
    }


    private void setNoScheduledCommandsCopyVisibility(boolean noSchedulesCopyVisible) {
        schedulesList.setVisibility(noSchedulesCopyVisible ? View.GONE : View.VISIBLE);
        noSchedulesCopyLayout.setVisibility(noSchedulesCopyVisible ? View.VISIBLE : View.GONE);

        if (noSchedulesCopyVisible) {
            String noSchedulesTitle = getNoCommandsTitleCopy();
            String noSchedulesDesc = getNoCommandsDescriptionCopy();

            // Hide copy if none specified by implementation
            if (noSchedulesTitle != null) {
                noSchedulesCopyTitle.setText(noSchedulesTitle);
            } else {
                noSchedulesCopyTitle.setVisibility(View.GONE);
            }

            if (noSchedulesDesc != null) {
                noSchedulesCopyDesc.setText(noSchedulesDesc);
            } else {
                noSchedulesCopyDesc.setVisibility(View.GONE);
            }
        }
    }

    private int getDayOfWeekRadioButton (DayOfWeek dayOfWeek) {
        switch (dayOfWeek) {
            case SUNDAY: return R.id.radio_sunday;
            case MONDAY: return R.id.radio_monday;
            case TUESDAY: return R.id.radio_tuesday;
            case WEDNESDAY: return R.id.radio_wednesday;
            case THURSDAY: return R.id.radio_thursday;
            case FRIDAY: return R.id.radio_friday;
            case SATURDAY: return R.id.radio_saturday;

            default:
                throw new IllegalArgumentException("Bug! Not implemented. Shouldn't be possible. Did we add an eighth day to the week?");
        }
    }

}
