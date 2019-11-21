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


public class TimePickerPopup extends ArcusFloatingFragment {

    private NumberPicker mLeftPicker;
    private NumberPicker mRightPicker;

    private String mTitle;
    private String mLeftTitle;
    private String mRightTitle;

    private int mLeftValue = 0;
    private int mLeftMin = 0;
    private int mLeftMax = 60;

    private int mRightValue = 0;
    private int mRightMin = 0;
    private int mRightMax = 59;

    private int mLowerBounds = 0;
    private int mUpperBounds = 3659;

    @Nullable
    private OnTimeChangedListener mListener;

    @NonNull
    public static TimePickerPopup newInstance(String title, String leftTitle, String rightTitle, int leftValue, int leftMin, int leftMax, int rightValue, int rightMin, int rightMax, int lowerBounds, int upperBounds) {
        TimePickerPopup fragment = new TimePickerPopup();

        fragment.setTitle(title);
        fragment.setLeftTitle(leftTitle);
        fragment.setRightTitle(rightTitle);
        fragment.setLeftValue(leftValue);
        fragment.setLeftMin(leftMin);
        fragment.setLeftMax(leftMax);
        fragment.setRightValue(rightValue);
        fragment.setRightMin(rightMin);
        fragment.setRightMax(rightMax);
        fragment.setLowerBounds(lowerBounds);
        fragment.setUpperBounds(upperBounds);


        return fragment;
    }

    public static TimePickerPopup newInstance(String title, String leftTitle, String rightTitle, int leftValue, int leftMin, int leftMax, int rightValue, int rightMin, int rightMax) {
        return newInstance(title, leftTitle, rightTitle, leftValue, leftMin, leftMax, rightValue, rightMin, rightMax, 0, 3659);
    }

        @Override
    public void setFloatingTitle() {
        title.setText(mTitle);
    }

    @Override
    public void doContentSection() {

        mLeftPicker = (NumberPicker) contentView.findViewById(R.id.left_picker);
        mRightPicker = (NumberPicker) contentView.findViewById(R.id.right_picker);

        Version1TextView leftTitleView = (Version1TextView) contentView.findViewById(R.id.left_text);
        Version1TextView rightTitleView = (Version1TextView) contentView.findViewById(R.id.right_text);

        leftTitleView.setText(mLeftTitle);
        rightTitleView.setText(mRightTitle);

        mLeftPicker.setMinValue(mLeftMin);
        mLeftPicker.setMaxValue(mLeftMax);
        mLeftPicker.setValue(mLeftValue);

        mRightPicker.setMinValue(mRightMin);
        mRightPicker.setMaxValue(mRightMax);
        mRightPicker.setValue(mRightValue);

        mRightPicker.setWrapSelectorWheel(false);
        mRightPicker.setWrapSelectorWheel(true);

        mLeftPicker.setOnScrollListener(new android.widget.NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(android.widget.NumberPicker view, int scrollState) {
                if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {

                    int seconds = mLeftPicker.getValue() * 60 + mRightValue;

                    if (seconds < mLowerBounds || seconds > mUpperBounds)  {
                        mLeftPicker.setValue(mLeftValue);
                    } else {
                        mLeftValue = mLeftPicker.getValue();
                    }

                    if (mListener != null)
                        mListener.onTimeChanged(mLeftValue, mRightValue);
                }
            }
        });

        mRightPicker.setOnScrollListener(new android.widget.NumberPicker.OnScrollListener() {
            @Override
            public void onScrollStateChange(android.widget.NumberPicker view, int scrollState) {

                int seconds = mLeftValue * 60 + mRightPicker.getValue();

                if (seconds < mLowerBounds || seconds > mUpperBounds)  {
                    mRightPicker.setValue(mRightValue);
                } else {
                    mRightValue = mRightPicker.getValue();
                }

                if (mListener != null)
                    mListener.onTimeChanged(mLeftValue, mRightValue);
            }
        });
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_time_picker;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void doClose() {
        if (mListener != null)
            mListener.onExit(mLeftValue, mRightValue);
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public void setLeftTitle(String title) {
        mLeftTitle = title;
    }

    public void setRightTitle(String title) {
        mRightTitle = title;
    }

    public void setLeftValue(int value) {
        mLeftValue = value;
    }

    public void setLeftMin(int value) {
        mLeftMin = value;
    }

    public void setLeftMax(int value) {
        mLeftMax = value;
    }

    public void setRightValue(int value) {
        mRightValue = value;
    }

    public void setRightMin(int value) {
        mRightMin = value;
    }

    public void setRightMax(int value) {
        mRightMax = value;
    }

    public void setLowerBounds(int value) {
        mLowerBounds = value;
    }

    public void setUpperBounds(int value) {
        mUpperBounds = value;
    }

    public void setOnTimeChangedListener(OnTimeChangedListener listener) {
        this.mListener = listener;
    }

    public void removeOnTimeChangedListener() {
        this.mListener = null;
    }

    public interface OnTimeChangedListener {
        void onTimeChanged(int leftValue, int rightValue);
        void onAccept(int leftValue, int rightValue);
        void onExit(int leftValue, int rightValue);
    }
}
