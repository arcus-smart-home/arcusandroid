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

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.NumberPicker;

import arcus.cornea.subsystem.model.TimeZoneModel;
import arcus.app.R;
import arcus.app.common.utils.StringUtils;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimezonePickerPopup extends ArcusFloatingFragment {
    public final static String SELECTED_ZONE_ID = "SELECTED_ZONE_ID";
    public final static String TIME_ZONES = "TIME_ZONES";
    public final static String FORCE_NOTIFICATION = "FORCE_NOTIFICATION";

    protected NumberPicker picker;
    protected List<TimeZoneModel> timezones = new ArrayList<>();
    protected boolean forceNotification = false;
    protected Reference<Callback> callbackRef = new SoftReference<>(null);

    public interface Callback {
        void timeZoneSelected(TimeZoneModel timeZone);
    }

    @NonNull public static TimezonePickerPopup newInstance(List<TimeZoneModel> timeZoneModels) {
        return newInstance(null, timeZoneModels);
    }

    @NonNull public static TimezonePickerPopup newInstance(String selectedZoneId, List<TimeZoneModel> timeZoneModels) {
        TimezonePickerPopup instance = new TimezonePickerPopup();
        Bundle arguments = new Bundle();
        arguments.putString(SELECTED_ZONE_ID, selectedZoneId);

        ArrayList<TimeZoneModel> zones;
        if (timeZoneModels == null) {
            zones = new ArrayList<>();
        }
        else {
            zones = new ArrayList<>(timeZoneModels);
        }

        arguments.putSerializable(TIME_ZONES, zones);
        instance.setArguments(arguments);

        return instance;
    }

    @NonNull public static TimezonePickerPopup newInstance(String selectedZoneId, List<TimeZoneModel> timeZoneModels, boolean forceNotification) {
        TimezonePickerPopup instance = new TimezonePickerPopup();
        Bundle arguments = new Bundle();
        arguments.putString(SELECTED_ZONE_ID, selectedZoneId);

        ArrayList<TimeZoneModel> zones;
        if (timeZoneModels == null) {
            zones = new ArrayList<>();
        }
        else {
            zones = new ArrayList<>(timeZoneModels);
        }

        arguments.putSerializable(TIME_ZONES, zones);
        arguments.putBoolean(FORCE_NOTIFICATION, forceNotification);
        instance.setArguments(arguments);

        return instance;
    }

    @Override public void setFloatingTitle() {
        title.setText(getResources().getString(R.string.settings_place_timezone));
    }

    @Override public void doContentSection() {
        timezones = getTimezones();
        picker = (NumberPicker) contentView.findViewById(R.id.floating_day_number_picker);
        picker.setVisibility(View.GONE);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setMinValue(0);

        String[] displayedValues = new String[timezones.size()];
        for (int i = 0; i < timezones.size(); i++) {
            displayedValues[i] = timezones.get(i).getName();
        }

        picker.setMaxValue(displayedValues.length - 1);
        picker.setDisplayedValues(displayedValues);
        picker.setValue(getIndexOfCurrentZoneOrDefault(0));
        picker.setVisibility(View.VISIBLE);

        Bundle args = getArguments();
        if (args == null) {
            forceNotification = false;
        } else {
            forceNotification = args.getBoolean(FORCE_NOTIFICATION, false);
        }
    }

    public void setCallback(Callback callback) {
        callbackRef = new SoftReference<>(callback);
    }

    @Override public Integer contentSectionLayout() {
        return R.layout.floating_day_picker;
    }

    @NonNull @Override public String getTitle() {
        return "";
    }

    @Override public void doClose() {
        if (timezones.isEmpty()) {
            return; // In the event the list was closed before the tz's loaded (by the "X" image)
        }

        Callback callback = callbackRef.get();
        if (callback == null) {
            return;
        }

        int current = getIndexOfCurrentZoneOrDefault(-1);
        int selected = picker.getValue();

        if (current == -1 || current != selected || forceNotification) { // Only emit when no previous value was set OR a new value was selected
            callback.timeZoneSelected(timezones.get(picker.getValue()));
        }
        callbackRef.clear();
    }

    private int getIndexOfCurrentZoneOrDefault(int dflt) {
        Bundle args = getArguments();
        if (args == null) {
            return dflt;
        }

        String selectedZone = args.getString(SELECTED_ZONE_ID, null);
        if (StringUtils.isEmpty(selectedZone)) {
            return dflt;
        }

        for (int index = 0; index < timezones.size(); index++) {
            if (selectedZone.equalsIgnoreCase(timezones.get(index).getId())) {
                return index;
            }
        }

        return dflt;
    }

    @SuppressWarnings({"unchecked"}) protected List<TimeZoneModel> getTimezones() {
        Bundle args = getArguments();
        if (args == null) {
            return Collections.emptyList();
        }

        List<TimeZoneModel> zones = (List<TimeZoneModel>) args.getSerializable(TIME_ZONES);
        if (zones == null) {
            return Collections.emptyList();
        }

        return zones;
    }
}
