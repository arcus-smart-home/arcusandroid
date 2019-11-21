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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.app.R;
import arcus.app.common.view.Version1TextView;
import arcus.app.common.view.NumberPicker;


public class AlarmRequirementPickerPopup extends ArcusFloatingFragment {

    private NumberPicker mPicker;

    private String mTitle;
    private String mPickerTitle;

    private int mValue = 0;
    private int mMin = 0;
    private int mMax = 1;

    @Nullable
    private OnTimeChangedListener mListener;

    @NonNull
    public static AlarmRequirementPickerPopup newInstance(String title, String sideTitle, int defaultValue, int min, int max) {
        AlarmRequirementPickerPopup fragment = new AlarmRequirementPickerPopup();

        fragment.setTitle(title);
        fragment.setSideTitle(sideTitle);
        fragment.setDefaultValue(defaultValue);
        fragment.setMin(min);
        fragment.setMax(max);

        return fragment;
    }

    @Override
    public void setFloatingTitle() {
        title.setText(mTitle);
    }

    @Override
    public void doContentSection() {

        mPicker = (NumberPicker) contentView.findViewById(R.id.picker);

        Version1TextView leftTitleView = (Version1TextView) contentView.findViewById(R.id.text);

        leftTitleView.setText(mPickerTitle);

        mPicker.setMinValue(mMin);
        if(mMax > 3) {
            mMax = 3;
        }
        mPicker.setMaxValue(mMax);
        mPicker.setValue(mValue);
    }

    @Override
    public void doClose() {
        if (mListener == null) {
            return;
        }
        mValue = mPicker.getValue();
        mListener.onClose(mValue);
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_single_number_picker;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setSideTitle(String title) {
        mPickerTitle = title;
    }

    public void setDefaultValue(int value) {
        mValue = value;
    }

    public void setMin(int value) {
        mMin = value;
    }

    public void setMax(int value) {
        mMax = value;
    }


    public void setOnTimeChangedListener(OnTimeChangedListener listener) {
        this.mListener = listener;
    }

    public interface OnTimeChangedListener {
        void onClose(int value);
    }
}
