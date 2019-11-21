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
package arcus.app.common.popups;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.events.TimeSelectedEvent;
import arcus.cornea.utils.DayOfWeek;
import arcus.app.R;
import arcus.app.common.view.NumberPicker;

import de.greenrobot.event.EventBus;

public class AMPMTimePopupWithHeader extends HeaderContentPopup {
    public interface OnClosedCallback {
        void selection(DayOfWeek day, int hour, int minute, boolean isAM);
    }

    private final int DEFAULT_HOUR_SELECTION = 12;
    private final int DEFAULT_MIN_SELECTION = 0;

    private static final String TIME_ONLY = "TIME.ONLY";
    private static final String HOUR_ONLY = "HOUR.ONLY";
    private static final String SELECTED_HOUR = "SELECTED.HOUR";
    private static final String SELECTED_MIN = "SELECTED.MIN";
    private static final String SELECTED_DAY = "SELECTED.DAY";
    private static final String CUSTOM_TITLE = "CUSTOM_TITLE";
    private static final String WITH_DAY_SELECTION = "WITH_DAY_SELECTION";
    private static final String SHOW_DIVIDER = "SHOW_DIVIDER";

    private View timePickerView;
    private View allDayTextView;

    private View allDayTextHeader;
    private View startTextHeader;
    private View endTextHeader;

    private View amTimePickerSelection;
    private View pmTimePickerSelection;
    @NonNull
    private AMPMSelection ampmSelection = AMPMSelection.AM;

    private NumberPicker hoursPicker;
    private NumberPicker minutesPicker;
    private NumberPicker dayPicker;

    @NonNull
    private TimeSelectedEvent.TimePickerType selected = TimeSelectedEvent.TimePickerType.START;

    private Drawable blackOutline;
    private Drawable whiteOutline;

    private int hoursValue = 0;
    private int minutesValue = 0;
    private OnClosedCallback onClosedCallback;

    public enum AMPMSelection {
        AM,
        PM
    }

    @NonNull
    public static AMPMTimePopupWithHeader newInstanceWithStartEnd() {
        AMPMTimePopupWithHeader popup = new AMPMTimePopupWithHeader();
        Bundle bundle = new Bundle(1);
        bundle.putBoolean(TIME_ONLY, false);
        popup.setArguments(bundle);
        return popup;
    }

    @NonNull
    public static AMPMTimePopupWithHeader newInstanceAsTimeOnly() {
        AMPMTimePopupWithHeader popup = new AMPMTimePopupWithHeader();
        Bundle bundle = new Bundle(1);
        bundle.putBoolean(TIME_ONLY, true);
        popup.setArguments(bundle);
        return popup;
    }

    @NonNull
    public static AMPMTimePopupWithHeader newInstanceAsTimeOnly (int selectedHour, int selectedMinute) {
        AMPMTimePopupWithHeader popup = new AMPMTimePopupWithHeader();
        Bundle bundle = new Bundle(3);
        bundle.putBoolean(TIME_ONLY, true);
        bundle.putInt(SELECTED_HOUR, selectedHour);
        bundle.putInt(SELECTED_MIN, selectedMinute);
        popup.setArguments(bundle);
        return popup;
    }

    @NonNull
    public static AMPMTimePopupWithHeader newInstanceAsTimeOnly (int selectedHour, int selectedMinute,
                                                                 String customTitle, boolean showDivider) {
        AMPMTimePopupWithHeader popup = new AMPMTimePopupWithHeader();
        Bundle bundle = new Bundle(4);
        bundle.putBoolean(TIME_ONLY, true);
        bundle.putInt(SELECTED_HOUR, selectedHour);
        bundle.putInt(SELECTED_MIN, selectedMinute);
        bundle.putString(CUSTOM_TITLE, customTitle);
        bundle.putBoolean(SHOW_DIVIDER, showDivider);
        popup.setArguments(bundle);
        return popup;
    }

    @NonNull
    public static AMPMTimePopupWithHeader newInstanceAsHourOnly(){
        AMPMTimePopupWithHeader popup = new AMPMTimePopupWithHeader();
        Bundle bundle = new Bundle(1);
        bundle.putBoolean(HOUR_ONLY, true);
        popup.setArguments(bundle);
        return popup;
    }

    @NonNull public static AMPMTimePopupWithHeader newInstanceAsDayAndTimePicker(
          int startHour,
          int startMinute,
          @NonNull DayOfWeek startDay,
          @Nullable String customTitle
    ) {
        AMPMTimePopupWithHeader popup = new AMPMTimePopupWithHeader();
        Bundle bundle = new Bundle(2);
        bundle.putInt(SELECTED_HOUR, startHour);
        bundle.putInt(SELECTED_MIN, startMinute);
        bundle.putInt(SELECTED_DAY, startDay.ordinal());
        bundle.putBoolean(WITH_DAY_SELECTION, true);
        bundle.putBoolean(TIME_ONLY, true);
        bundle.putString(CUSTOM_TITLE, customTitle);
        popup.setArguments(bundle);
        return popup;
    }

    @Override
    public void setupHeaderSection(@NonNull View view) {
        initializeSelectionOutlines();
        allDayTextHeader = view.findViewById(R.id.all_day_header);
        startTextHeader = view.findViewById(R.id.start_text_header);
        endTextHeader = view.findViewById(R.id.end_text_header);

        allDayTextHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTextHeader.setBackground(whiteOutline);
                endTextHeader.setBackground(whiteOutline);

                allDayTextHeader.setBackground(blackOutline);
                allDayTextView.setVisibility(View.VISIBLE);
                timePickerView.setVisibility(View.GONE);
                selected = TimeSelectedEvent.TimePickerType.ALL_DAY;

                EventBus.getDefault().post(new TimeSelectedEvent(0, 0, TimeSelectedEvent.TimePickerType.ALL_DAY));
            }
        });

        startTextHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTextHeader.setBackground(whiteOutline);
                allDayTextHeader.setBackground(whiteOutline);

                startTextHeader.setBackground(blackOutline);
                allDayTextView.setVisibility(View.GONE);
                timePickerView.setVisibility(View.VISIBLE);
                selected = TimeSelectedEvent.TimePickerType.START;
            }
        });

        endTextHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTextHeader.setBackground(whiteOutline);
                allDayTextHeader.setBackground(whiteOutline);

                endTextHeader.setBackground(blackOutline);
                allDayTextView.setVisibility(View.GONE);
                timePickerView.setVisibility(View.VISIBLE);
                selected = TimeSelectedEvent.TimePickerType.END;
            }
        });
    }

    @Override
    public void setupDividerSection(View view) {}

    @Override
    public void setupSubContentSection(@NonNull View view) {
        initializeSelectionOutlines();
        allDayTextView = view.findViewById(R.id.floating_all_day_text);
        timePickerView = view.findViewById(R.id.floating_ampm_picker);

        if (startTextHeader != null) {
            startTextHeader.callOnClick();
        }
        else { // Processing a "Time only" picker.
            allDayTextView.setVisibility(View.GONE);
        }

        hoursPicker = (NumberPicker) timePickerView.findViewById(R.id.hours_picker);
        minutesPicker = (NumberPicker) timePickerView.findViewById(R.id.minutes_picker);
        amTimePickerSelection = timePickerView.findViewById(R.id.time_picker_AM_selection);
        pmTimePickerSelection = timePickerView.findViewById(R.id.time_picker_PM_selection);

        amTimePickerSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amTimePickerSelection.setBackground(blackOutline);
                pmTimePickerSelection.setBackground(whiteOutline);
                ampmSelection = AMPMSelection.AM;
                getPickerValuesAndPostEvent();
            }
        });
        pmTimePickerSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amTimePickerSelection.setBackground(whiteOutline);
                pmTimePickerSelection.setBackground(blackOutline);
                ampmSelection = AMPMSelection.PM;
                getPickerValuesAndPostEvent();
            }
        });

        amTimePickerSelection.setBackground(getDefaultAmPmSelection() == AMPMSelection.AM ? blackOutline : whiteOutline);
        pmTimePickerSelection.setBackground(getDefaultAmPmSelection() == AMPMSelection.AM ? whiteOutline : blackOutline);
        ampmSelection = getDefaultAmPmSelection();

        hoursPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        minutesPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        hoursPicker.setMinValue(1);
        hoursPicker.setMaxValue(12);
        hoursPicker.setValue(getDefaultHourSelection());
        hoursPicker.setWrapSelectorWheel(true);

        if(isHourOnlyPicker()){
            minutesPicker.setMinValue(0);
            minutesPicker.setMaxValue(0);
            String[] va = new String[1];
            va[0] = "00";
            minutesPicker.setDisplayedValues(va);
        }else {
            String[] values = new String[60];
            for (int i = 0; i < 60; i++) {
                values[i] = String.valueOf((i < 10) ? "0" + i : i);
            }

            minutesPicker.setMinValue(0);
            minutesPicker.setMaxValue(59);
            minutesPicker.setDisplayedValues(values);
            minutesPicker.setValue(getDefaultMinuteSelection());
        }
        minutesPicker.setWrapSelectorWheel(true);

        minutesPicker.setOnScrollListener(new android.widget.NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(android.widget.NumberPicker view, int scrollState) {
                if (scrollState == android.widget.NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                    getPickerValuesAndPostEvent();
                }
            }
        });
        hoursPicker.setOnScrollListener(new android.widget.NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(android.widget.NumberPicker view, int scrollState) {
                if (scrollState == android.widget.NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                    getPickerValuesAndPostEvent();
                }
            }
        });

        if (!isDayAndTimePicker()) {
            return;
        }

        View dayPickerContainer = view.findViewById(R.id.day_picker_container);
        dayPickerContainer.setVisibility(View.VISIBLE);
        dayPicker = (NumberPicker) timePickerView.findViewById(R.id.day_picker);
        String[] values = DayOfWeek.stringRepresentation();
        dayPicker.setMinValue(0);
        dayPicker.setMaxValue(values.length - 1);
        dayPicker.setDisplayedValues(values);

        int day = getNonNullArguments().getInt(SELECTED_DAY, -1);
        if (day != -1) {
            dayPicker.setValue(day);
        }
    }

    private void getPickerValuesAndPostEvent() {
        hoursValue = hoursPicker.getValue();
        minutesValue = minutesPicker.getValue();
        if (ampmSelection.equals(AMPMSelection.PM) && hoursValue < 12) {
            hoursValue += 12; // Convert to 24 hour.
        }
        else if (ampmSelection.equals(AMPMSelection.AM) && hoursValue == 12) {
            hoursValue = 0;
        }

        if (isTimeOnlyPicker()) {
            EventBus.getDefault().post(new TimeSelectedEvent(hoursValue, minutesValue, TimeSelectedEvent.TimePickerType.SINGLE_TIME));
        } else if(isHourOnlyPicker()){
            EventBus.getDefault().post(new TimeSelectedEvent(hoursValue,0, TimeSelectedEvent.TimePickerType.SINGLE_TIME));
        }
        else {
            EventBus.getDefault().post(new TimeSelectedEvent(hoursValue, minutesValue, selected));
        }
    }

    @Override public void doClose() {
        super.doClose();
        OnClosedCallback cb = onClosedCallback;
        if (cb == null) {
            return;
        }

        hoursValue = hoursPicker.getValue();
        minutesValue = minutesPicker.getValue();
        DayOfWeek[] days = DayOfWeek.values();

        cb.selection(days[dayPicker.getValue()], hoursValue, minutesValue, ampmSelection.equals(AMPMSelection.AM));
    }

    private void initializeSelectionOutlines() {
        blackOutline = ContextCompat.getDrawable(getActivity(), R.drawable.outline_button_style_black);
        whiteOutline = ContextCompat.getDrawable(getActivity(), R.drawable.outline_button_style);
    }

    @Override @Nullable @LayoutRes
    public Integer headerSectionLayout() {
        if (isTimeOnlyPicker() || isHourOnlyPicker()) {
            return null;
        }
        else {
            return R.layout.floating_start_end_all_day_content_header;
        }
    }

    @Nullable @LayoutRes
    public Integer contentDividerLayout() {
        if (isDayAndTimePicker()) {
            return R.layout.floating_divider;
        }

        if (isTimeOnlyPicker() || isHourOnlyPicker()) {
            if(getNonNullArguments().getBoolean(SHOW_DIVIDER, false)) {
                return R.layout.floating_divider;
            }
            return null;
        }
        else {
            return R.layout.floating_divider;
        }
    }

    @Override @Nullable @LayoutRes
    public Integer subContentSectionLayout() {
        return R.layout.floating_timepicker_start_end_all_day;
    }

    @Nullable
    @Override
    public String getTitle() {
        String customTitle = getNonNullArguments().getString(CUSTOM_TITLE);
        if (!TextUtils.isEmpty(customTitle)) {
            return customTitle;
        }

        if (isTimeOnlyPicker() || isHourOnlyPicker()) {
            return getString(R.string.picker_time_text);
        }
        else {
            return null;
        }
    }

    public int getDefaultHourSelection () {
        int selectedHour = getArguments().getInt(SELECTED_HOUR, DEFAULT_HOUR_SELECTION);
        return selectedHour > 12 ? selectedHour - 12 : selectedHour;
    }

    public int getDefaultMinuteSelection () {
        return getArguments().getInt(SELECTED_MIN, DEFAULT_MIN_SELECTION);
    }

    public AMPMSelection getDefaultAmPmSelection () {
        int selectedHour = getArguments().getInt(SELECTED_HOUR, DEFAULT_HOUR_SELECTION);
        return selectedHour > 12 ? AMPMSelection.PM : AMPMSelection.AM;
    }

    public boolean isTimeOnlyPicker() {
        return getNonNullArguments().getBoolean(TIME_ONLY, false);
    }

    public boolean isHourOnlyPicker() { return getNonNullArguments().getBoolean(HOUR_ONLY,false); }

    public boolean isDayAndTimePicker() { return getNonNullArguments().getBoolean(WITH_DAY_SELECTION, false); }

    protected Bundle getNonNullArguments() {
        Bundle bundle = getArguments();
        if (bundle == null) {
            return new Bundle();
        }

        return bundle;
    }

    public void setOnClosedCallback(OnClosedCallback callback) {
        this.onClosedCallback = callback;
    }
}
