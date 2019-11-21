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
import android.view.View;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.view.Version1TextView;
import arcus.app.common.view.NumberPicker;

import java.util.Arrays;


public class NumberPickerPopup extends ArcusFloatingFragment {

    private static final String NUMBER_PICKER_TYPE_KEY = "NUMBER PICKER TYPE KEY";
    private static final String CURRENT_VALUE_KEY = "CURRENT VALUE KEY";
    private static final String MIN_OUTLIER = "MIN_OUTLIER KEY";
    private static final String MIN_VALUE = "MIN.VALUE";
    private static final String MAX_VALUE = "MAX.VALUE";
    private static final String STEP_VALUE = "STEP.VALUE";
    private static final int MIN_TEMP_VAL = 45;
    private static final int MAX_TEMP_VAL = 95;



    private int mMax;
    private int mMin;
    private int mStep;
    private int mCurrentValue;
    private int mCoolSetPoint;
    private int mHeatSetPoint;
    private String customTitle;
    private String customDescription;


    @Nullable
    private NumberPickerType mType;
    @Nullable
    private OnValueChangedListener mListener;
    private NumberPicker mPicker;
    private TextView unitText;
    private Version1TextView description;

    public enum NumberPickerType{
        VENT("VENT OPEN"),
        LOW("LOW"),
        HIGH("HIGH"),
        MIN_MAX("TEMP"),
        BRIGHTNESS("BRIGHTNESS"),
        PERCENT("PERCENT"),
        WATER_HEATER("TEMPERATURE"),
        MINUTES("DURATION"),
        DURATION("DURATION"),
        DAY("DAYS"),
        TIMES("CHOOSE");

        NumberPickerType(String title){
            this.title = title;
        }

        private String title;

        public String getTitle() {
            return title;
        }
    }

    @NonNull
    public static NumberPickerPopup newInstance(NumberPickerType type, int currentValue){
        NumberPickerPopup popup = new NumberPickerPopup();
        Bundle bundle = new Bundle();
        bundle.putSerializable(NUMBER_PICKER_TYPE_KEY,type);
        bundle.putInt(CURRENT_VALUE_KEY, currentValue);
        popup.setArguments(bundle);
        return popup;
    }

    @NonNull
    public static NumberPickerPopup newInstance(NumberPickerType type, int minValue, int maxValue, int currentValue) {
        NumberPickerPopup popup = new NumberPickerPopup();
        Bundle bundle = new Bundle();
        bundle.putInt(MIN_VALUE, minValue);
        bundle.putInt(MAX_VALUE, maxValue);
        bundle.putSerializable(NUMBER_PICKER_TYPE_KEY, type);
        bundle.putInt(CURRENT_VALUE_KEY, currentValue < minValue ? minValue : currentValue > maxValue ? maxValue : currentValue);
        popup.setArguments(bundle);
        return popup;
    }

    @NonNull
    public static NumberPickerPopup newInstance(NumberPickerType type, int minValue, int maxValue, int currentValue, int step) {
        NumberPickerPopup popup = new NumberPickerPopup();
        Bundle bundle = new Bundle();
        bundle.putInt(MIN_VALUE, minValue);
        bundle.putInt(MAX_VALUE, maxValue);
        bundle.putSerializable(NUMBER_PICKER_TYPE_KEY, type);
        bundle.putInt(CURRENT_VALUE_KEY, currentValue < minValue ? minValue : currentValue > maxValue ? maxValue : currentValue);
        bundle.putInt(STEP_VALUE, step);
        popup.setArguments(bundle);
        return popup;
    }


    //for future use possibly
    @NonNull
    public static NumberPickerPopup newInstance(NumberPickerType type, int minValue, int maxValue, int currentValue, int step, int minOutlier) {
        NumberPickerPopup popup = new NumberPickerPopup();
        Bundle bundle = new Bundle();
        bundle.putInt(MIN_VALUE, minValue);
        bundle.putInt(MAX_VALUE, maxValue);
        bundle.putInt(MIN_OUTLIER, minOutlier);
        bundle.putSerializable(NUMBER_PICKER_TYPE_KEY, type);
        bundle.putInt(CURRENT_VALUE_KEY, currentValue < minValue ? minValue : currentValue > maxValue ? maxValue : currentValue);
        bundle.putInt(STEP_VALUE, step);
        popup.setArguments(bundle);
        return popup;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            mType = (NumberPickerType) arguments.getSerializable(NUMBER_PICKER_TYPE_KEY);
            mCurrentValue = arguments.getInt(CURRENT_VALUE_KEY);
            mStep = arguments.getInt(STEP_VALUE, -1);
            mMin = arguments.getInt(MIN_VALUE, -1);
            mMax = arguments.getInt(MAX_VALUE, -1);
        }
    }

    @Override
    public void setFloatingTitle() {
        if(customTitle == null) {
            title.setText(mType.getTitle());
        }
        else {
            title.setText(customTitle);
        }
    }

    private void setDescription() {
        if(customDescription != null && description != null) {
            description.setText(customDescription);
            description.setVisibility(View.VISIBLE);
        }
    }

    public void setFloatingTitle(String title) {
        customTitle = title;
    }

    public void setDescription(String description) {
        customDescription = description;
    }

    @Override
    public void doContentSection() {
        mPicker = (NumberPicker) contentView.findViewById(R.id.vent_open_picker);
        unitText = (TextView) contentView.findViewById(R.id.unit_text);
        description = (Version1TextView) contentView.findViewById(R.id.description);
        setDescription();
        try {
            switch (mType) {
                case VENT:
                    loadVent();
                    break;
                case HIGH:
                    unitText.setText((char) 0x00B0 + "F");
                    loadHigh();
                    break;
                case LOW:
                    unitText.setText((char) 0x00B0 + "F");
                    loadLow();
                    break;
                case MIN_MAX:
                    unitText.setText((char) 0x00B0 + "F");
                    loadMinMax();
                    break;
                case TIMES:
                    unitText.setText("Time(s)");
                    loadMinMax();
                    break;
                case BRIGHTNESS:
                    unitText.setText("%");
                    loadBrightness();
                    break;
                case PERCENT:
                    unitText.setText("%");
                    loadPercentage();
                    break;
                case WATER_HEATER:
                    unitText.setText(String.format("%sF", (char)0x00B0));
                    loadWaterHeater();
                    break;
                case MINUTES:
                    unitText.setText(String.format("%s", "MINS"));
                    loadCameraMinutes();
                    break;
                case DAY:
                    unitText.setText(String.format("%s", "DAY(S)"));
                    if (mStep == -1) mStep = 1;
                    loadConfiguration(mMin, mMax, mCurrentValue, mStep);
                    break;
                case DURATION:
                    unitText.setText(String.format("%s", "MINS"));
                    if (mStep == -1) mStep = 1;
                    loadConfiguration(mMin, mMax, mCurrentValue, mStep);
                    break;
            }

            mPicker.setWrapSelectorWheel(false);
        }catch (Exception e){
            logger.error("Can't set displayed value of the picker: {}",e);
        }
    }

    private void loadVent(){
        unitText.setText("%");
        mMin = 0;
        mMax = 100;
        mStep = 10;

        loadConfiguration(mMin, mMax, mCurrentValue, mStep);
    }

    private void loadLow(){
        if (mMin == -1) mMin = MIN_TEMP_VAL;
        if (mMax == -1) mMax = mCoolSetPoint -3;
        if (mStep == -1) mStep = 1;

        loadConfiguration(mMin, mMax, mCurrentValue, mStep);
    }

    private void loadHigh() {
        if (mMin == -1) mMin = mHeatSetPoint + 3;
        if (mMax == -1) mMax = MAX_TEMP_VAL;
        if (mStep == -1) mStep = 1;

        loadConfiguration(mMin, mMax, mCurrentValue, mStep);
    }

    private void loadMinMax() {
        if (mMin == -1) mMin = MIN_TEMP_VAL;
        if (mMax == -1) mMax = MAX_TEMP_VAL;
        if (mStep == -1) mStep = 1;

        loadConfiguration(mMin, mMax, mCurrentValue, mStep);
    }

    private void loadBrightness() {
        if (mStep == -1) mStep = 1;
        if (mMin == -1) mMin = 0;
        if (mMax == -1) mMax = 100;

        loadConfiguration(mMin, mMax, mCurrentValue, mStep);
    }

    private void loadPercentage() {
        if (mStep == -1) mStep = 1;
        if (mMin == -1) mMin = 0;
        if (mMax == -1) mMax = 100;

        loadConfiguration(mMin, mMax, mCurrentValue, mStep);
    }

    // Water heater max temp can vary ?
    private void loadWaterHeater() {
        int minValue = 0;
        int maxValue = 0;
        int stepValue = 0;

        if (getArguments() != null) {
            minValue = getArguments().getInt(MIN_VALUE, MIN_TEMP_VAL);
            maxValue = getArguments().getInt(MAX_VALUE, MAX_TEMP_VAL);
            stepValue = getArguments().getInt(STEP_VALUE, 1);

        }

        loadConfiguration(minValue, maxValue, mCurrentValue, stepValue);
    }

    private void loadCameraMinutes() {
        if (mStep == -1) mStep = 1;
        loadConfiguration(1, 20, mCurrentValue, mStep);
    }

    private void loadConfiguration(int min, int max, int current, int step) {

        int minIndex = 0;
        int maxIndex = (max - min) / step;
        int currentIndex = Math.max(0, ((current - min) / step));

        String[] displayedValues = new String[maxIndex - minIndex + 1];

        for (int index = 0; index < displayedValues.length; index++) {
            displayedValues[index] = String.valueOf(min + (index * step));
        }

        logger.debug("Configuring number picker with current value: {} (index={}) min value: {} (index={}), max value: {} (index={}), step: {}, values: {}", current, currentIndex, min, minIndex, max, maxIndex, step, Arrays.asList(displayedValues));

        mPicker.setDisplayedValues(displayedValues);
        mPicker.setMinValue(minIndex);
        mPicker.setMaxValue(maxIndex);
        mPicker.setValue(currentIndex);
    }


    public void setCoolSetPoint(int coolSetPoint) {
        this.mCoolSetPoint = coolSetPoint;
    }

    public void setHeatSetPoint(int heatSetPoint) {
        this.mHeatSetPoint = heatSetPoint;
    }


    @Override
    public void doClose() {
        if (mListener == null) {
            return;
        }

        logger.error("Number picker selection index: {}, min value: {}, step: {}", mPicker.getValue(), mMin, mStep);
        mListener.onValueChanged(mMin + (mPicker.getValue() * mStep));
        removeListener();
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.vent_pop_up_content;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    public void setOnValueChangedListener(OnValueChangedListener listener){
        this.mListener = listener;
    }

    public void removeListener(){
        this.mListener = null;
    }

    public interface OnValueChangedListener{
        void onValueChanged(int value);
    }
}
