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

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import arcus.app.common.popups.AMPMTimePopupWithHeader;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CareTimeWindowAddEditFragment extends BaseFragment implements CareBehaviorController.Callback {
    private static final String ID = "ID";
    private static final String IS_EDIT = "IS_EDIT";
    private static final String DAY = "DAY";
    private static final String EXISTING_TIMEWINDOW = "EXISTING_TIMEWINDOW";
    private static final String TIME_STRING_FORMAT = "cccc H:mm:ss";
    private static final String TIME_DISPLAY_FORMAT = "cccc h:mm a";

    private Integer startHour = 20; // is "Default time" 8PM->6AM
    private Integer startMinute = 0;
    private DayOfWeek startDay;

    private Integer endHour = 6;
    private Integer endMinute = 0;
    private DayOfWeek endDay;

    private Bundle arguments;
    private TimeWindowModel originalTimeWindow;
    private TimeWindowModel editingTimeWindow;

    private View startTimeContainer, topDivider, middleDivider, bottomDivider, endTimeContainer;
    private TextView topText, startTimeText, startTimeAbstract, endTimeText, endTimeAbstract;
    private ImageView startTimeChevron, endTimeChevron;
    private Version1Button saveButton, deleteButton;

    public static CareTimeWindowAddEditFragment newInstance(
          String id,
          DayOfWeek dayOfWeek
    ) {
        CareTimeWindowAddEditFragment fragment = new CareTimeWindowAddEditFragment();

        Bundle args = new Bundle(3);
        args.putString(ID, id);
        args.putBoolean(IS_EDIT, false);
        args.putSerializable(DAY, dayOfWeek);

        fragment.setArguments(args);

        return fragment;
    }

    public static CareTimeWindowAddEditFragment newInstance(
          String id,
          TimeWindowModel existing
    ) {
        CareTimeWindowAddEditFragment fragment = new CareTimeWindowAddEditFragment();

        Bundle args = new Bundle(3);

        args.putString(ID, id);
        args.putBoolean(IS_EDIT, true);
        args.putParcelable(EXISTING_TIMEWINDOW, existing);

        fragment.setArguments(args);

        return fragment;
    }

    @Override public View onCreateView(
          @NonNull LayoutInflater inflater,
          ViewGroup container,
          Bundle savedInstanceState
    ) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        topText = (TextView) view.findViewById(R.id.care_schedule_top_text);
        topDivider = view.findViewById(R.id.top_divider);
        startTimeContainer = view.findViewById(R.id.start_time_container);
        startTimeText = (TextView) view.findViewById(R.id.care_start_time_text);
        startTimeAbstract = (TextView) view.findViewById(R.id.care_start_time_abstract_text);
        startTimeChevron = (ImageView)view.findViewById(R.id.care_start_time_chevron);
        middleDivider = view.findViewById(R.id.middle_divider);
        endTimeContainer = view.findViewById(R.id.end_time_container);
        endTimeText = (TextView) view.findViewById(R.id.care_end_time_text);
        endTimeAbstract = (TextView) view.findViewById(R.id.care_end_time_abstract_text);
        endTimeChevron = (ImageView) view.findViewById(R.id.care_end_time_chevron);
        bottomDivider = view.findViewById(R.id.bottom_divider);
        saveButton = (Version1Button) view.findViewById(R.id.save_button);
        deleteButton = (Version1Button) view.findViewById(R.id.delete_button);

        Bundle args = getArguments();
        if (args == null) {
            return view;
        }

        setColorSchemeAndVisibility(args.getBoolean(IS_EDIT, false));

        return view;
    }

    @Override public void onResume () {
        super.onResume();
        Activity activity = getActivity();
        String title = getTitle();
        if (activity != null && !TextUtils.isEmpty(title)) {
            activity.setTitle(title);
        }

        arguments = getArguments();
        if (arguments == null) {
            return;
        }

        startDay = getInitialStartDay();
        endDay = DayOfWeek.getNextDayFrom(startDay);

        CareBehaviorController.instance().setCallback(this);
        if (arguments.getBoolean(IS_EDIT, false)) {
            ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().darkened());
            CareBehaviorController.instance().editExistingBehaviorByID(arguments.getString(ID, ""));
        }
        else {
            ImageManager.with(getActivity()).setWallpaper(Wallpaper.ofCurrentPlace().lightend());
            CareBehaviorController.instance().addBehaviorByTemplateID(arguments.getString(ID, ""));
        }
    }

    @Nullable @Override public String getTitle() {
        return getString(R.string.schedule_adding_new_event_title);
    }

    @Override public Integer getLayoutId() {
        return R.layout.care_add_edit_time_window;
    }

    @Override public void editTemplate(
          final CareBehaviorModel editingModel,
          CareBehaviorTemplateModel templateModel
    ) {
        setInitialDatesAndTimes();
        startTimeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AMPMTimePopupWithHeader picker = AMPMTimePopupWithHeader.newInstanceAsDayAndTimePicker(
                      startHour, startMinute, startDay,
                      getString(R.string.care_time_window_start_time)
                );
                picker.setOnClosedCallback(new AMPMTimePopupWithHeader.OnClosedCallback() {
                    @Override
                    public void selection(DayOfWeek day, int hour, int minute, boolean isAM) {
                        if (isAM) {
                            startHour = (hour == 12) ? 0 : hour;
                        }
                        else {
                            startHour = (hour == 12) ? hour : hour + 12;
                        }
                        startDay = day;
                        startMinute = minute;
                        updateModelAndAbstracts();
                    }
                });
                BackstackManager.getInstance()
                      .navigateToFloatingFragment(picker, picker.getClass().getCanonicalName(), true);
            }
        });

        endTimeContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AMPMTimePopupWithHeader picker = AMPMTimePopupWithHeader.newInstanceAsDayAndTimePicker(
                      endHour, endMinute, endDay,
                      getString(R.string.care_time_window_end_time)
                );
                picker.setOnClosedCallback(new AMPMTimePopupWithHeader.OnClosedCallback() {
                    @Override
                    public void selection(DayOfWeek day, int hour, int minute, boolean isAM) {
                        if (isAM) {
                            endHour = (hour == 12) ? 0 : hour;
                        }
                        else {
                            endHour = (hour == 12) ? hour : hour + 12;
                        }
                        endDay = day;
                        endMinute = minute;
                        updateModelAndAbstracts();
                    }
                });
                BackstackManager.getInstance()
                      .navigateToFloatingFragment(picker, picker.getClass().getCanonicalName(), true);
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (originalTimeWindow != null) {
                    originalTimeWindow.setDay(editingTimeWindow.getDay());
                    originalTimeWindow.setStartTime(editingTimeWindow.getStartTime());
                    originalTimeWindow.setDurationSecs(editingTimeWindow.getDurationSecs());
                }
                else {
                    editingModel.addTimeWindow(editingTimeWindow);
                }

                BackstackManager.getInstance().navigateBack();
            }
        });

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                editingModel.getTimeWindows().remove(originalTimeWindow);
                BackstackManager.getInstance().navigateBack();
            }
        });
    }

    @Override public void onError(Throwable error) {
        hideProgressBar();
        ErrorManager.in(getActivity()).showGenericBecauseOf(error);
    }

    @Override public void unsupportedTemplate() {
        hideProgressBar();
    }

    protected void setColorSchemeAndVisibility(boolean editing) {
        if (editing) {
            topText.setTextColor(Color.WHITE);
            topDivider.setBackgroundColor(getResources().getColor(R.color.overlay_white_with_20));
            startTimeText.setTextColor(Color.WHITE);
            startTimeAbstract.setTextColor(getResources().getColor(R.color.overlay_white_with_60));
            startTimeChevron.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.chevron_white));
            middleDivider.setBackgroundColor(getResources().getColor(R.color.overlay_white_with_20));
            endTimeText.setTextColor(Color.WHITE);
            endTimeAbstract.setTextColor(getResources().getColor(R.color.overlay_white_with_60));
            endTimeChevron.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.chevron_white));
            bottomDivider.setBackgroundColor(getResources().getColor(R.color.overlay_white_with_20));

            saveButton.setColorScheme(Version1ButtonColor.WHITE);
            deleteButton.setColorScheme(Version1ButtonColor.MAGENTA);
            deleteButton.setVisibility(View.VISIBLE);
        }
        else {
            topText.setTextColor(Color.BLACK);
            topDivider.setBackgroundColor(getResources().getColor(R.color.black_with_20));
            startTimeText.setTextColor(Color.BLACK);
            startTimeAbstract.setTextColor(getResources().getColor(R.color.black_with_60));
            startTimeChevron.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.chevron));
            middleDivider.setBackgroundColor(getResources().getColor(R.color.black_with_20));
            endTimeText.setTextColor(Color.BLACK);
            endTimeAbstract.setTextColor(getResources().getColor(R.color.black_with_60));
            endTimeChevron.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.chevron));
            bottomDivider.setBackgroundColor(getResources().getColor(R.color.black_with_20));

            saveButton.setColorScheme(Version1ButtonColor.BLACK);
            deleteButton.setVisibility(View.GONE);
        }
    }

    protected void updateModelAndAbstracts() {
        if (editingTimeWindow == null) {
            editingTimeWindow = new TimeWindowModel(startDay, "", 0);
        }

        editingTimeWindow.setDay(startDay);
        editingTimeWindow.setStartTime(getStartTimeString());
        editingTimeWindow.calculateAndSetDurationTo(endDay, endHour, endMinute);
        startTimeAbstract.setText(getTimeStringAbstract(startDay, startHour, startMinute));
        endTimeAbstract.setText(getTimeStringAbstract(endDay, endHour, endMinute));
        saveButton.setEnabled(editingTimeWindow != null && editingTimeWindow.isValidWindow());
    }

    protected String getStartTimeString() {
        return String.format("%s:%s%s:00", startHour, startMinute == 0 ? "0" : "", startMinute);
    }

    protected String getTimeStringAbstract(DayOfWeek day, int hour, int minute) {
        String parseString = String.format("%s %s:%s:00", day.name(), hour, minute);

        try {
            return getDateDisplayFormat().format(getDateParseFormat().parse(parseString));
        }
        catch (Exception ex) {
            logger.debug("Could not parse time: [{}]", parseString, ex);
            return "";
        }
    }

    protected void setInitialDatesAndTimes() {
        if (arguments == null) {
            return;
        }

        originalTimeWindow = arguments.getParcelable(EXISTING_TIMEWINDOW);
        if (originalTimeWindow != null) {
            startDay = originalTimeWindow.getDay();
            startHour = originalTimeWindow.getStartHour();
            startMinute = originalTimeWindow.getStartMinute();

            endDay = originalTimeWindow.getEndDay();
            endHour = originalTimeWindow.getEndHour();
            endMinute = originalTimeWindow.getEndMinute();
        }

        updateModelAndAbstracts();
    }

    protected DayOfWeek getInitialStartDay() {
        if (arguments == null) {
            return DayOfWeek.MONDAY;
        }

        DayOfWeek selectedDay = (DayOfWeek) arguments.getSerializable(DAY);
        if (selectedDay == null) {
            selectedDay = DayOfWeek.MONDAY;
        }

        return selectedDay;
    }

    protected DateFormat getDateDisplayFormat() {
        return new SimpleDateFormat(TIME_DISPLAY_FORMAT, Locale.getDefault());
    }

    protected DateFormat getDateParseFormat() {
        return new SimpleDateFormat(TIME_STRING_FORMAT, Locale.getDefault());
    }

}
