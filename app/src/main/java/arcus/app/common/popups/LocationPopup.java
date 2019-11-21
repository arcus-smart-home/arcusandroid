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
import android.text.TextUtils;
import android.view.View;
import android.widget.NumberPicker;

import arcus.app.R;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class LocationPopup extends ArcusFloatingFragment {
    @NonNull
    private static String LOCATIONS = "LOCATIONS";
    private static String TITLE = "TITLE";
    private static String TYPE = "TYPE";
    private Callback callback;
    NumberPicker picker;
    List<String> locations = new ArrayList<>();

    public interface Callback {
        void selectedItem(String item, String type);
    }

    @NonNull
    public static LocationPopup newInstance(ArrayList<String> locations, String title, String type) {
        LocationPopup popup = new LocationPopup();
        Bundle bundle = new Bundle(1);
        bundle.putSerializable(LOCATIONS, locations);
        bundle.putString(TITLE, title);
        bundle.putString(TYPE, type);
        popup.setArguments(bundle);
        return popup;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void setFloatingTitle() {
        if (getTitle().equals("")) {
            title.setVisibility(View.GONE);
        }
        else {
            title.setText(getTitle());
        }
    }

    @Override @SuppressWarnings({"unchecked"})
    public void doContentSection() {
        if (getArguments() != null) {
            Serializable object = getArguments().getSerializable(LOCATIONS);
            if (object != null) {
                locations = (ArrayList<String>) object;
            }
        }

        picker = (NumberPicker) contentView.findViewById(R.id.floating_day_number_picker);
        picker.setVisibility(View.GONE);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setMinValue(0);

        String[] displayedValues = new String[locations.size()];
        for (int i = 0; i < locations.size(); i++) {
            displayedValues[i] = locations.get(i);
        }

        picker.setMaxValue(displayedValues.length - 1);
        picker.setDisplayedValues(displayedValues);
        picker.setVisibility(View.VISIBLE);
    }

    @Override
    public void doClose() {
        callback.selectedItem(locations.get(picker.getValue()), getArguments().getString(TYPE));
    }

    @Override public Integer contentSectionLayout() {
        return R.layout.floating_day_picker;
    }

    @NonNull
    @Override
    public String getTitle() {
        String title = getArguments().getString(TITLE);
        if(!TextUtils.isEmpty(title)) {
            return title;
        }
        return "";
    }
}
