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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import arcus.cornea.model.StringPair;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.CapabilityUtils;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.IrrigationZone;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelEvent;
import arcus.app.R;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.details.IrrigationTimeChangedEvent;
import arcus.app.device.model.DeviceType;

import org.apache.commons.lang3.StringUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class IrrigationZoneDurationFragment extends ArcusFloatingFragment {
    private static final String SELECTIONS = "SELECTIONS";
    private static final String SELECTED   = "SELECTED";
    private static final String MODEL_ADDR = "MODEL.ADDR";
    private static final String TITLE = "TITLE";
    private static final String ABSTRACT     = "ABSTRACT";
    private static final String DIVIDER      = "DIVIDER";
    private static final int MAX_WATERING_HOUR = 3;   // max watering time is 240 min
    private DeviceModel deviceModel;
    private NumberPicker picker;
    private NumberPicker stationPicker;

    private ArrayList<StringPair> bundleStrings;

    private CapabilityUtils capabilityUtils;
    private String[] zoneNameArr = new String[12];
    private String[] zoneNameArrTrunk = new String[12];
    private String[] zoneNameNumString = new String[12];
    private int[] zoneDefaultDuration = new int[12];

    private IrrigationTimeChangedEvent wateringEvent = null;

    private TextView stationTxtButton;
    private TextView durationTxtButton;
    private LinearLayout stationLayout;
    private LinearLayout durationLayout;
    private Version1TextView zoneSelection;
    private Version1TextView durationSelection;
    private WeakReference<Callback> callbackRef = new WeakReference<>(null);

    public interface Callback {
        void selectionComplete(String deviceId, String zone, StringPair selected);
    }

    public void setCallback(Callback callback) {
        callbackRef = new WeakReference<>(callback);
    }

    @NonNull
    public static IrrigationZoneDurationFragment newInstance(String modelAddr, List<StringPair> selections, @Nullable String selected) {
        IrrigationZoneDurationFragment fragment = new IrrigationZoneDurationFragment();

        ArrayList<StringPair> values;
        if (selections == null) {
            values = new ArrayList<>();
        }
        else {
            values = new ArrayList<>(selections);
        }

        Bundle data = new Bundle(3);
        data.putString(MODEL_ADDR, modelAddr);
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

        capabilityUtils = new CapabilityUtils(deviceModel);

        getZoneNames();

        stationPicker = (NumberPicker) contentView.findViewById(R.id.station_picker);
        stationPicker.setMinValue(1);
        stationPicker.setValue(1);
        stationPicker.setMaxValue(zoneNameArr.length);
        for(int i = 0; i < zoneNameArr.length; i++){
            zoneNameArrTrunk[i]=StringUtils.abbreviate(zoneNameArr[i],20);
        }
        stationPicker.setDisplayedValues(zoneNameArrTrunk);

        updateTimeSelector();

        zoneSelection = (Version1TextView) contentView.findViewById(R.id.zone_selected);
        durationSelection = (Version1TextView) contentView.findViewById(R.id.duration_selected);
    }

    private void updateTimeSelector() {
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

        boolean showDivider = args.getBoolean(DIVIDER, false);
        View divider = contentView.findViewById(R.id.picker_title_divider);
        if (showDivider) {
            if (divider != null) {
                divider.setVisibility(View.VISIBLE);
            }
        }
        else {
            if (divider != null) {
                divider.setVisibility(View.GONE);
            }
        }

        picker = (NumberPicker) contentView.findViewById(R.id.floating_day_number_picker);
        picker.setMinValue(0);
        picker.setMaxValue(bundleSize - 1);
        picker.setDisplayedValues(pickerValues);
        picker.setValue(selectedIndex != -1 ? selectedIndex : 0);
    }

    private void getZoneNames() {
        if (capabilityUtils != null) {
            for (String instance : capabilityUtils.getInstanceNames()) {
                String name = (String) capabilityUtils.getInstanceValue(instance, IrrigationZone.ATTR_ZONENAME);
                Double number = (Double) capabilityUtils.getInstanceValue(instance, IrrigationZone.ATTR_ZONENUM);
                int zoneNum = 1;
                if (number != null) {
                    zoneNum = number.intValue();
                }
                name = name != null ? name : getString(R.string.irrigation_zone) + " " + zoneNum;
                zoneNameArr[zoneNum - 1] = name;
                zoneNameNumString[zoneNum-1] = instance;

                int defaultDuration = 1;
                if(capabilityUtils.getInstanceValue(instance, IrrigationZone.ATTR_DEFAULTDURATION) != null) {
                    defaultDuration = getDefaultDuration(zoneNum);
                }
                zoneDefaultDuration[zoneNum-1] = defaultDuration;
            }
        }
    }

    private int getDefaultDuration(int zoneNum) {
        Number dfltWater = (Number) deviceModel.get(String.format("%s:%s", IrrigationZone.ATTR_DEFAULTDURATION, "z"+zoneNum));
        return (dfltWater != null) ? dfltWater.intValue() : 1;
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.irrigation_zone_duration_picker;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup parentGroup = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);

        stationTxtButton = (TextView) parentGroup.findViewById(R.id.txtStation);
        durationTxtButton = (TextView) parentGroup.findViewById(R.id.txtDuration);
        durationLayout = (LinearLayout) parentGroup.findViewById(R.id.layout_duration);
        stationLayout = (LinearLayout) parentGroup.findViewById(R.id.layout_station);
        stationPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        stationTxtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                durationLayout.setVisibility(View.GONE);
                stationLayout.setVisibility(View.VISIBLE);
                stationTxtButton.setBackgroundResource(R.drawable.outline_button_style_black);
                durationTxtButton.setBackgroundResource(0);
            }
        });

        durationTxtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                durationLayout.setVisibility(View.VISIBLE);
                stationLayout.setVisibility(View.GONE);
                durationTxtButton.setBackgroundResource(R.drawable.outline_button_style_black);
                stationTxtButton.setBackgroundResource(0);
            }
        });

        stationPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                int defaultDuration = zoneDefaultDuration[stationPicker.getValue()-1];
                for(int nSelection = 0; nSelection < bundleStrings.size(); nSelection++) {
                    StringPair pair = bundleStrings.get(nSelection);
                    if(Integer.parseInt(pair.getKey()) == defaultDuration) {
                        picker.setValue(nSelection);
                        break;
                    }
                }

                updateSelectionValues();
            }
        });

        picker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int oldValue, int newValue) {
                updateSelectionValues();
            }
        });

        updateSelectionValues();
        return parentGroup;
    }

    private void updateSelectionValues() {
        zoneSelection.setText(zoneNameArr[stationPicker.getValue()-1]);

        int selectedIndex = picker.getValue();
        if (selectedIndex >= bundleStrings.size() || selectedIndex < 0) {
            return;
        }
        String time = bundleStrings.get(selectedIndex).getValue();

        durationSelection.setText(time);
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
        int selectedIndex = picker.getValue();
        if (selectedIndex >= bundleStrings.size() || selectedIndex < 0) {
            return;
        }

        int zone = stationPicker.getValue();
        String zoneInd = zoneNameNumString[zone-1];

        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.selectionComplete(deviceModel.getId(), zoneInd, bundleStrings.get(selectedIndex));
        }
    }
}
