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
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.view.NumberPicker;

import java.util.TreeMap;


public class WaterHeaterPickerPopup extends NumberPickerPopup{


    private static final String CURRENT_VALUE_PICKER = "CURRENT VALUE INDEX KEY";
    private static final String TREEMAP_VALUE_KEY = "TREEMAP VALUE KEY";
    private int mCurrentValue;
    private TreeMap<Integer,Integer> mTreeMap;
    @Nullable
    private OnValueChangedListener mListener;
    private NumberPicker mPicker;
    private TextView unitText;



    //treemap implemntation for custom values
    @NonNull
    public static WaterHeaterPickerPopup newInstance( int currentValue, TreeMap<Integer,Integer> map) {
        WaterHeaterPickerPopup popup = new WaterHeaterPickerPopup();
        Bundle bundle = new Bundle();
        bundle.putInt(CURRENT_VALUE_PICKER, currentValue);
        bundle.putSerializable(TREEMAP_VALUE_KEY, map );
        popup.setArguments(bundle);
        return popup;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle arguments = getArguments();
        if (arguments != null) {
            mCurrentValue = arguments.getInt(CURRENT_VALUE_PICKER);
            mTreeMap = (TreeMap<Integer, Integer>) arguments.getSerializable(TREEMAP_VALUE_KEY);
        }
    }

    @Override
    public void setFloatingTitle() {
        title.setText(getResources().getString(R.string.climate_more_temperature_title));
    }

    @Override
    public void doContentSection() {
        mPicker = (NumberPicker) contentView.findViewById(R.id.vent_open_picker);
        unitText = (TextView) contentView.findViewById(R.id.unit_text);

        unitText.setText(String.format("%sF", (char)0x00B0));

        loadConfiguration();

    }



    private void loadConfiguration() {
        String[] displayedValues = new String[mTreeMap.size()];

        for (int nC = 0; nC < displayedValues.length; nC++) {
            displayedValues[nC] = String.valueOf(mTreeMap.get(nC));
        }
        mPicker.setFormatter(new NumberPicker.Formatter() {
            @Override
            public String format(int value) {
                 return String.valueOf( mTreeMap.get(value));
            }
        });

        mPicker.setMinValue(0);
        mPicker.setMaxValue(mTreeMap.size()-1);
        mPicker.setValue(getIndexGivenValue(mTreeMap, mCurrentValue));
        mPicker.setDisplayedValues(displayedValues);

    }

    private int getIndexGivenValue(TreeMap<Integer,Integer> map, int value){

        for (Integer key : map.keySet()) {
            if (map.get(key) == value){
                return key;
            }
        }
        return mTreeMap.size()/2;

    }



    @Override
    public void doClose() {
        if (mListener == null) {
            return;
        }
        mListener.onValueChanged( ((Integer) mTreeMap.get(mPicker.getValue())).intValue());
        //you will need to have an associate data structure like a TreeMap
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
        //this is the index
        void onValueChanged(int value);
    }


}
