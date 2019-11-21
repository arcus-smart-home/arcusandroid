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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import arcus.cornea.provider.PlaceModelProvider;
import arcus.cornea.utils.ModelSource;
import arcus.cornea.utils.SunriseSunset;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.model.PlaceModel;
import arcus.app.R;
import arcus.app.common.view.NumberPicker;

import java.util.Locale;

public class SunriseSunsetPicker extends HeaderContentPopup {
    private static final String TIME_OBJECT = "TIME_OBJECT";
    private static final String NONE = "NONE";

    private int sunriseOffset   = 0;
    private int sunsetOffset    = 0;
    private boolean isBeforeSunrise = true;
    private boolean isBeforeSunset = true;

    private int hour            = 18;
    private int minute          = 0;
    private boolean isAM        = false;

    private Selection selection = Selection.TIME;
    private Callback callback = null;

    private boolean canSetSunriseSunset = true;

    protected View timeHeader;
    protected View sunriseHeader;
    protected View sunsetHeader;
    protected View sunriseSunsetLayout;
    protected View sunriseSunsetLayoutUnavailable;
    protected View timePickerLayout;
    protected NumberPicker hoursPicker;
    protected NumberPicker minutesPicker;
    protected TextView amSelection;
    protected TextView pmSelection;

    protected NumberPicker sunsetSunrisePicker;
    protected TextView beforeSunriseSunsetText;
    protected TextView afterSunriseSunsetText;

    private Drawable blackOutline;
    private Drawable whiteOutline;

    private enum Selection {
        TIME,
        SUNRISE,
        SUNSET
    }

    public interface Callback {
        void selection(TimeOfDay selected);
    }

    public static SunriseSunsetPicker newInstance(TimeOfDay timeOfDay) {
        SunriseSunsetPicker fragment = new SunriseSunsetPicker();
        Bundle args = new Bundle(1);

        args.putParcelable(TIME_OBJECT, timeOfDay);

        fragment.setArguments(args);
        return fragment;
    }

    public void setCallback(Callback popupCallback) {
        callback = popupCallback;
    }

    @Override public void setupHeaderSection(View view) {
        initializeSelectionOutlines();
        if (view == null) {
            return;
        }

        // The Time, Sunrise, Sunset fields.
        timeHeader = view.findViewById(R.id.time_header);
        sunriseHeader = view.findViewById(R.id.sunrise_header);
        sunsetHeader = view.findViewById(R.id.sunset_header);

        timeHeader.setOnClickListener(this);
        sunriseHeader.setOnClickListener(this);
        sunsetHeader.setOnClickListener(this);
    }

    @Override public void setupDividerSection(View view) {}

    @Override public void setupSubContentSection(View view) {
        if (view == null) {
            return;
        }

        canSetSunriseSunset = checkIfCanSetSunriseSunset();

        // The values of the time/sunrise/sunset
        sunriseSunsetLayout = view.findViewById(R.id.sunrise_sunset_layout);
        timePickerLayout = view.findViewById(R.id.ampm_picker_layout);
        sunriseSunsetLayoutUnavailable = view.findViewById(R.id.sunrise_sunset_layout_unavailable);

        hoursPicker = (NumberPicker) view.findViewById(R.id.hours_picker);
        hoursPicker.setOnValueChangedListener(new android.widget.NumberPicker.OnValueChangeListener() {
            @Override public void onValueChange(android.widget.NumberPicker picker, int oldVal, int newVal) {
                hour = newVal;
            }
        });
        minutesPicker = (NumberPicker) view.findViewById(R.id.minutes_picker);
        minutesPicker.setOnValueChangedListener(new android.widget.NumberPicker.OnValueChangeListener() {
            @Override public void onValueChange(android.widget.NumberPicker picker, int oldVal, int newVal) {
                minute = newVal;
            }
        });

        amSelection = (TextView) view.findViewById(R.id.time_picker_AM_selection);
        pmSelection = (TextView) view.findViewById(R.id.time_picker_PM_selection);

        sunsetSunrisePicker = (NumberPicker) view.findViewById(R.id.sunrise_sunset_picker);
        beforeSunriseSunsetText = (TextView) view.findViewById(R.id.before_sunrise_sunset_text);
        afterSunriseSunsetText = (TextView) view.findViewById(R.id.after_sunrise_sunset_text);

        hoursPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        minutesPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        sunsetSunrisePicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);

        sunsetSunrisePicker.setOnValueChangedListener(new android.widget.NumberPicker.OnValueChangeListener() {
            @Override public void onValueChange(android.widget.NumberPicker picker, int oldVal, int newVal) {
                if (Selection.SUNSET.equals(selection)) {
                    sunsetOffset = newVal;
                }
                else {
                    sunriseOffset = newVal;
                }
            }
        });

        amSelection.setOnClickListener(this);
        pmSelection.setOnClickListener(this);
        beforeSunriseSunsetText.setOnClickListener(this);
        afterSunriseSunsetText.setOnClickListener(this);

        initializeTimePicker();
        initializeSunriseSetPicker();

        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        TimeOfDay timeOfDay = args.getParcelable(TIME_OBJECT);
        if (timeOfDay == null) {
            setTimePickerValues(); // Use defaults.
            circleAm(isAM);
            return;
        }

        switch (timeOfDay.getSunriseSunset()) {
            case ABSOLUTE:
                hour = timeOfDay.getHours();
                circleAm(hour < 12);
                hour %= 12;
                minute = timeOfDay.getMinutes();
                setTimePickerValues();
                break;

            case SUNRISE:
                sunriseOffset = Math.abs(timeOfDay.getOffset());
                isBeforeSunrise = timeOfDay.getOffset() < 0;
                onClick(sunriseHeader);
                break;

            case SUNSET:
                sunsetOffset = Math.abs(timeOfDay.getOffset());
                isBeforeSunset = timeOfDay.getOffset() < 0;
                onClick(sunsetHeader);
                break;

            default: // No-Op
        }
    }

    @Override public void onClick(@NonNull View v) {
        super.onClick(v);

        switch (v.getId()) {
            case R.id.time_header:
                circleTimeSunriseSunsetHeaders(true, false, false); // Header
                setTimePickerValues(); // Content
                showTimeAndSunset(true, false, false); // Always show time.
                circleAm(isAM);
                break;

            case R.id.sunrise_header:
                circleTimeSunriseSunsetHeaders(false, true, false); // Header
                if (canSetSunriseSunset) {
                    showTimeAndSunset(false, true, false); // Show it
                    setSunriseValue();
                    circleBeforeTextValue(isBeforeSunrise);
                }
                else {
                    showTimeAndSunset(false, false, true); // Show "Unavailable"
                }
                break;

            case R.id.sunset_header:
                circleTimeSunriseSunsetHeaders(false, false, true); // Header
                if (canSetSunriseSunset) {
                    showTimeAndSunset(false, true, false); // Show it
                    setSunsetValue();
                    circleBeforeTextValue(isBeforeSunset);
                }
                else {
                    showTimeAndSunset(false, false, true); // Show "Unavailable"
                }
                break;

            case R.id.before_sunrise_sunset_text:
                circleBeforeTextValue(true);
                break;

            case R.id.after_sunrise_sunset_text:
                circleBeforeTextValue(false);
                break;

            case R.id.time_picker_AM_selection:
                circleAm(true);
                break;

            case R.id.time_picker_PM_selection:
                circleAm(false);
                break;

            default: // No-Op
        }
    }

    @Override public void doClose() {
        if (callback != null) {
            if (!Selection.TIME.equals(selection) && !canSetSunriseSunset) {
                return; // Can't set sunrise/sunset and they were on that view when closing.
            }

            int offset;

            switch (selection) {
                case TIME:
                    int calculatedHour;
                    calculatedHour = hour % 12;
                    if (!isAM) {
                        calculatedHour += 12;
                    }

                    String time = String.format(Locale.US, "%s:%02d:00", calculatedHour, minute);
                    callback.selection(TimeOfDay.fromString(time));
                    break;
                case SUNRISE:
                    offset = isBeforeSunrise ? (sunriseOffset * -1) : sunriseOffset;
                    callback.selection(new TimeOfDay(SunriseSunset.SUNRISE, offset));
                    break;
                case SUNSET:
                    offset = isBeforeSunset ? (sunsetOffset * -1) : sunsetOffset;
                    callback.selection(new TimeOfDay(SunriseSunset.SUNSET, offset));
                    break;
            }
        }
    }

    @Nullable @Override public Integer contentDividerLayout() {
        return R.layout.floating_divider;
    }

    @Nullable @Override public Integer headerSectionLayout() {
        return R.layout.floating_sunrise_sunset_content_header;
    }

    @Nullable @Override public Integer subContentSectionLayout() {
        return R.layout.floating_sunrises_sunset_content;
    }

    @Nullable @Override public String getTitle() {
        return null;
    }

    protected void setSunriseValue() {
        sunsetSunrisePicker.setValue(sunriseOffset);
    }

    protected void setSunsetValue() {
        sunsetSunrisePicker.setValue(sunsetOffset);
    }

    protected void setTimePickerValues() {
        hoursPicker.setValue(hour);
        minutesPicker.setValue(minute);
    }

    protected void initializeTimePicker() {
        hoursPicker.setMinValue(1);
        hoursPicker.setMaxValue(12);

        String[] minutes = new String[60];
        for (int i = 0; i < 60; i++) {
            minutes[i] = String.format(Locale.US, "%02d", i);
        }
        minutesPicker.setMinValue(0);
        minutesPicker.setMaxValue(59);
        minutesPicker.setDisplayedValues(minutes);
    }

    protected void initializeSunriseSetPicker() {
        sunsetSunrisePicker.setMinValue(0);
        sunsetSunrisePicker.setMaxValue(60);
        sunsetSunrisePicker.setWrapSelectorWheel(true);
    }

    protected void circleTimeSunriseSunsetHeaders(boolean time, boolean sunrise, boolean sunset) {
        timeHeader.setBackground(null);
        sunriseHeader.setBackground(null);
        sunsetHeader.setBackground(null);

        timeHeader.setBackground(time ? blackOutline : whiteOutline);
        sunriseHeader.setBackground(sunrise ? blackOutline : whiteOutline);
        sunsetHeader.setBackground(sunset ? blackOutline : whiteOutline);

        selection = time ? Selection.TIME : sunrise ? Selection.SUNRISE : Selection.SUNSET;
    }

    protected void circleAm(boolean circleAM) {
        amSelection.setBackground(null);
        pmSelection.setBackground(null);

        amSelection.setBackground(circleAM ? blackOutline : whiteOutline);
        pmSelection.setBackground(circleAM ? whiteOutline : blackOutline);

        isAM = circleAM;
    }

    protected void circleBeforeTextValue(boolean circleBeforeText) {
        switch (selection) {
            case SUNRISE:
                isBeforeSunrise = circleBeforeText;
                break;

            case SUNSET:
                isBeforeSunset = circleBeforeText;
                break;

            default: // No - OP
        }
        beforeSunriseSunsetText.setBackground(null);
        afterSunriseSunsetText.setBackground(null);

        beforeSunriseSunsetText.setBackground(circleBeforeText ? blackOutline : whiteOutline);
        afterSunriseSunsetText.setBackground(circleBeforeText ? whiteOutline : blackOutline);
    }

    private boolean checkIfCanSetSunriseSunset() {
        ModelSource<PlaceModel> place = PlaceModelProvider.getCurrentPlace();
        place.load();
        PlaceModel placeModel = place.get();
        if (placeModel == null) {
            return false;
        }

        return !(TextUtils.isEmpty(placeModel.getAddrGeoPrecision()) || NONE.equals(placeModel.getAddrGeoPrecision()));
    }

    protected void showTimeAndSunset(boolean showTime, boolean showSunriseSunset, boolean showUnavailable) {
        timePickerLayout.setVisibility(showTime ? View.VISIBLE : View.GONE);
        sunriseSunsetLayout.setVisibility(showSunriseSunset ? View.VISIBLE : View.GONE);
        sunriseSunsetLayoutUnavailable.setVisibility(showUnavailable ? View.VISIBLE : View.GONE);
    }

    private void initializeSelectionOutlines() {
        blackOutline = ContextCompat.getDrawable(getActivity(), R.drawable.outline_button_style_black);
        whiteOutline = ContextCompat.getDrawable(getActivity(), R.drawable.outline_button_style);
    }
}
