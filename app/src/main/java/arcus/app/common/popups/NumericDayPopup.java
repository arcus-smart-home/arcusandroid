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

import arcus.app.R;

public class NumericDayPopup extends ArcusFloatingFragment {
    private static final String TITLE = "TITLE";
    private NumberPicker picker;
    private Callback callback;
    private View.OnClickListener onClickListener;

    public interface Callback {
        void selected(int time);
    }

    @NonNull
    public static NumericDayPopup newInstance() {
        return new NumericDayPopup();
    }

    public static NumericDayPopup newInstance(String title) {
        NumericDayPopup fragment = new NumericDayPopup();

        Bundle bundle = new Bundle(1);
        bundle.putString(TITLE, title);
        fragment.setArguments(bundle);

        return fragment;
    }

    public NumericDayPopup() {}

    public void setCallback(Callback useCallback) {
        this.callback = useCallback;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.onClickListener = listener;
    }

    @Override
    public void setFloatingTitle() {
        String customTitle = getArguments().getString(TITLE, "");
        if(customTitle.equals("")) {
            title.setText("Days");
        }
        else {
            title.setText(customTitle);
        }
    }

    @Override
    public void doContentSection() {
        int maxDaysToShow = 7;

        String[] days = new String[maxDaysToShow];

        //1-7 day(s) picker
        for (int dayNumber = 1; dayNumber < maxDaysToShow+1 ; dayNumber++) {
            int arrayNumber = dayNumber-1;
            if(dayNumber>1) days[arrayNumber] = (dayNumber)+" Days";
            else days[arrayNumber] = (dayNumber)+" Day";
        }
        picker = (NumberPicker) contentView.findViewById(R.id.floating_day_number_picker);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setMinValue(0);
        picker.setMaxValue(maxDaysToShow - 1);
        picker.setDisplayedValues(days);
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

    @Override public void onClick(@NonNull View v) {
        if (onClickListener == null) {
            super.onClick(v);
        }
        else {
            onClickListener.onClick(v);
        }
    }

    @Override public void doClose() {
        if (callback != null) {
            callback.selected(picker.getValue()+1);
        }
    }

}
