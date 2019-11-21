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
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import arcus.cornea.model.StringPair;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelEvent;
import arcus.app.R;
import arcus.app.device.model.DeviceType;

import java.util.ArrayList;
import java.util.List;

public class HoursMinutePickerFragment extends ArcusFloatingFragment {
    private static final String SELECTIONS = "SELECTIONS";
    private static final String SELECTED   = "SELECTED";
    private static final String MODEL_ADDR = "MODEL.ADDR";
    private static final String TITLE = "TITLE";
    private static final String ZONE = "ZONE";
    private static final String ABSTRACT     = "ABSTRACT";
    private DeviceModel deviceModel;
    private NumberPicker picker;
    private HoursMinutePickerFragment.Callback callback;
    private ArrayList<StringPair> bundleStrings;

    public interface Callback {
        void selectionComplete(String deviceId, String zone, StringPair selected);
    }

    public void setCallback(HoursMinutePickerFragment.Callback callback) {
        this.callback = callback;
    }

    @NonNull
    public static HoursMinutePickerFragment newInstance(String modelID, String title, String zone,
                                                        List<StringPair> selections, @Nullable String selected) {
        HoursMinutePickerFragment fragment = new HoursMinutePickerFragment();

        ArrayList<StringPair> values;
        if (selections == null) {
            values = new ArrayList<>();
        }
        else {
            values = new ArrayList<>(selections);
        }
        Bundle data = new Bundle(5);
        data.putString(MODEL_ADDR, modelID);
        data.putString(TITLE, title);
        data.putString(ZONE, zone);
        data.putSerializable(SELECTIONS, values);
        data.putString(SELECTED, selected);
        fragment.setArguments(data);

        return fragment;
    }

    @Override
    public void setFloatingTitle() {
        title.setText(getArguments().getString(TITLE, ""));
    }

    @Override
    public void doContentSection() {
        String deviceAddress = getArguments().getString(MODEL_ADDR, "");

        ModelSource<DeviceModel> m = DeviceModelProvider.instance().getModel(deviceAddress);
        m.addModelListener(Listeners.runOnUiThread(new Listener<ModelEvent>() {
            public void onEvent(ModelEvent e) {
                if (e instanceof ModelAddedEvent) {
                    // model is loaded
                }
            }
        }));
        m.load();

        if (m.get() != null) {
            deviceModel = m.get();
        }

        Bundle args = getArguments();
        if (args == null) {
            return; // Nothing to show...
        }

        try {
            bundleStrings = (ArrayList<StringPair>) args.getSerializable(SELECTIONS);
            if (bundleStrings == null) {
                return; // Again, nothing to show.
            }
        }
        catch (Exception ex) {
            logger.error("Could not deserialize SELECTIONS", ex);
            return;
        }

        String selectedValue = args.getString(SELECTED);
        int selectedIndex = -1;
        int bundleSize = bundleStrings.size();
        String[] pickerValues = new String[bundleSize];
        for (int i = 0; i < bundleSize; i++) {
            pickerValues[i] = bundleStrings.get(i).getValue();
            if (bundleStrings.get(i).getKey().equals(selectedValue)) {
                selectedIndex = i;
            }
        }

        String abstractText = args.getString(ABSTRACT);
        if (!TextUtils.isEmpty(abstractText)) {
            TextView view = (TextView) contentView.findViewById(R.id.day_number_picker_abstract);
            if (view != null) {
                view.setText(abstractText);
                view.setVisibility(View.VISIBLE);
            }
        }

        picker = (NumberPicker) contentView.findViewById(R.id.floating_day_number_picker);
        picker.setMinValue(0);
        picker.setMaxValue(bundleSize - 1);
        picker.setDisplayedValues(pickerValues);
        picker.setValue(selectedIndex != -1 ? selectedIndex : 0);
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_day_picker;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void doClose() {
        if (deviceModel == null) {
            return;
        }

        if (DeviceType.fromHint(deviceModel.getDevtypehint()).equals(DeviceType.IRRIGATION)) {
            handleIrrigationClose();
        }
    }

    private void handleIrrigationClose() {
        if (callback == null || bundleStrings == null) {
            return;
        }

        int selectedIndex = picker.getValue();
        if (selectedIndex >= bundleStrings.size() || selectedIndex < 0) {
            return;
        }
        String zone = getArguments().getString(ZONE);
        callback.selectionComplete(deviceModel.getId(), zone, bundleStrings.get(selectedIndex));

    }
}
