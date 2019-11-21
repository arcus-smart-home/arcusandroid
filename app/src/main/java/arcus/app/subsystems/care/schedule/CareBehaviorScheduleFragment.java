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
package arcus.app.subsystems.care.schedule;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.app.Activity;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;

import arcus.cornea.subsystem.care.CareBehaviorController;
import arcus.cornea.subsystem.care.model.CareBehaviorModel;
import arcus.cornea.subsystem.care.model.CareBehaviorTemplateModel;
import arcus.cornea.subsystem.care.model.TimeWindowModel;
import arcus.cornea.utils.DayOfWeek;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.Wallpaper;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.subsystems.care.adapter.CareBehaviorScheduleAdapter;
import arcus.app.subsystems.care.util.CareUtilities;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CareBehaviorScheduleFragment extends BaseFragment implements CareBehaviorController.Callback {
    private static final String ID = "ID";
    private static final String DAY = "DAY";
    private static final String IS_EDIT = "IS_EDIT";

    private Map<DayOfWeek, List<TimeWindowModel>> timeWindows;
    private LinearLayout noSchedulesCopyLayout;
    private Version1Button addEventButton;
    private RadioGroup dayOfWeekGroup;
    private ListView schedulesList;

    public static CareBehaviorScheduleFragment newInstance(
          @NonNull  String id,
          @NonNull  Boolean isEdit,
          @Nullable DayOfWeek selectedDay
    ) {
        CareBehaviorScheduleFragment fragment = new CareBehaviorScheduleFragment();

        Bundle args = new Bundle(3);

        args.putString(ID, id);
        args.putSerializable(DAY, selectedDay);
        args.putBoolean(IS_EDIT, isEdit);

        fragment.setArguments(args);

        return fragment;
    }

    @Override public View onCreateView(
          @NonNull LayoutInflater inflater,
          ViewGroup container,
          Bundle savedInstanceState
    ) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        noSchedulesCopyLayout = (LinearLayout) view.findViewById(R.id.no_schedules_copy);
        addEventButton = (Version1Button) view.findViewById(R.id.add_event_button);
        dayOfWeekGroup = (RadioGroup) view.findViewById(R.id.day_of_week_radio_group);
        schedulesList = (ListView) view.findViewById(R.id.schedules);

        dayOfWeekGroup.check(R.id.radio_monday);

        TextView noScheduleCopyTitle = (TextView) view.findViewById(R.id.no_schedules_copy_title);
        TextView noSchedulesCopyDesc = (TextView) view.findViewById(R.id.no_schedules_copy_desc);
        noScheduleCopyTitle.setText(getString(R.string.care_no_schedule_header_title));
        noSchedulesCopyDesc.setText(getString(R.string.care_no_schedule_header_desc));

        return view;
    }

    @Override public void onResume () {
        super.onResume();
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        Activity activity = getActivity();
        String title = getTitle();
        if (activity != null && !TextUtils.isEmpty(title)) {
            activity.setTitle(title);
            activity.invalidateOptionsMenu();
        }

        ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
        addEventButton.setColorScheme(Version1ButtonColor.WHITE);
        dayOfWeekGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                setScheduledDaysOfWeek();
                selectedDayChanged(getSelectedDayOfWeek());
            }
        });
        addEventButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle args = getArguments();
                if (args == null) {
                    return;
                }

                BackstackManager.getInstance().navigateToFragment(
                      CareTimeWindowAddEditFragment.newInstance(args.getString(ID, ""), getSelectedDayOfWeek()), true
                );
            }
        });

        // By Default, don't show this. Wait for confirmation that there are no schedule events before doing so.
        noSchedulesCopyLayout.setVisibility(View.GONE);
        CareBehaviorController.instance().setCallback(this);

        if (args.getBoolean(IS_EDIT, false)) {
            CareBehaviorController.instance().editExistingBehaviorByID(args.getString(ID, ""));
        }
        else {
            CareBehaviorController.instance().addBehaviorByTemplateID(args.getString(ID, ""));
        }
    }

    @Nullable @Override public String getTitle() {
        return getString(R.string.schedule_generic_text);
    }

    public void setScheduledDaysOfWeek() {
        Set<DayOfWeek> scheduled = timeWindows.keySet();
        for (DayOfWeek day : DayOfWeek.values()) {
            Drawable highlight  = null;
            if (scheduled.contains(day) || day == getSelectedDayOfWeek()) {
                highlight = ContextCompat.getDrawable(getActivity(), R.drawable.day_of_week_radio_drawable_white);
            }
            dayOfWeekGroup.findViewById(getDayOfWeekRadioButton(day)).setBackground(highlight);
        }
    }

    public DayOfWeek getSelectedDayOfWeek() {
        switch (dayOfWeekGroup.getCheckedRadioButtonId()) {
            case R.id.radio_tuesday:    return DayOfWeek.TUESDAY;
            case R.id.radio_wednesday:  return DayOfWeek.WEDNESDAY;
            case R.id.radio_thursday:   return DayOfWeek.THURSDAY;
            case R.id.radio_friday:     return DayOfWeek.FRIDAY;
            case R.id.radio_saturday:   return DayOfWeek.SATURDAY;
            case R.id.radio_sunday:     return DayOfWeek.SUNDAY;

            case R.id.radio_monday:
            default:
                return DayOfWeek.MONDAY;
        }
    }

    protected int getDayOfWeekRadioButton(@Nullable DayOfWeek dayOfWeek) {
        if (dayOfWeek == null) {
            return R.id.radio_monday;
        }

        switch (dayOfWeek) {
            case TUESDAY:   return R.id.radio_tuesday;
            case WEDNESDAY: return R.id.radio_wednesday;
            case THURSDAY:  return R.id.radio_thursday;
            case FRIDAY:    return R.id.radio_friday;
            case SATURDAY:  return R.id.radio_saturday;
            case SUNDAY:    return R.id.radio_sunday;

            case MONDAY:
            default:
                return R.id.radio_monday;
        }
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_weekly_scheduler;
    }

    @Override public void editTemplate(
          CareBehaviorModel editingModel,
          CareBehaviorTemplateModel templateModel
    ) {
        noSchedulesCopyLayout.setVisibility(View.VISIBLE);
        timeWindows = CareUtilities.getSchedulesFor(editingModel);
        setScheduledDaysOfWeek();
        selectedDayChanged(getSelectedDayOfWeek());
    }

    protected void selectedDayChanged(DayOfWeek dayOfWeek) {
        if (timeWindows == null) {
            return;
        }

        List<TimeWindowModel> timeWindowModels = timeWindows.get(dayOfWeek);
        CareBehaviorScheduleAdapter adapter;
        if (timeWindowModels == null || timeWindowModels.isEmpty()) {
            noSchedulesCopyLayout.setVisibility(View.VISIBLE);
            adapter = new CareBehaviorScheduleAdapter(getActivity(), Collections.<TimeWindowModel>emptyList());
        }
        else {
            noSchedulesCopyLayout.setVisibility(View.GONE);
            adapter = new CareBehaviorScheduleAdapter(getActivity(), timeWindowModels);
            adapter.setOnItemClickListener(new CareBehaviorScheduleAdapter.ItemClickListener() {
                @Override public void itemClicked(TimeWindowModel timeWindowModel) {
                    Bundle args = getArguments();
                    if (args == null) {
                        return;
                    }

                    BackstackManager.getInstance().navigateToFragment(
                          CareTimeWindowAddEditFragment.newInstance(args.getString(ID, ""), timeWindowModel), true
                    );
                }
            });
            schedulesList.setAdapter(adapter);
        }

        schedulesList.setAdapter(adapter);
    }

    @Override public void onError(Throwable error) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(error);
    }

    @Override public void unsupportedTemplate() {} /* no-op */
}
