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
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.common.base.Strings;
import arcus.app.R;
import arcus.app.common.view.Version1TextView;
import arcus.app.common.view.NumberPicker;

public class DurationPickerPopup extends ArcusFloatingFragment {
    private Version1TextView timerText;
    private Version1TextView indefiniteText;
    private View indefiniteTextContainer;
    private View timePickersContainer;
    private Drawable blackOutline;
    private Drawable whiteOutline;
    private NumberPicker leftPicker;
    private NumberPicker rightPicker;

    private static final String TITLE_KEY = "TITLE";
    @NonNull
    private String fragmentTitle = "";
    private SelectionListener selectionListener;
    @Nullable
    private android.widget.NumberPicker.OnValueChangeListener valueChangeListener = new android.widget.NumberPicker.OnValueChangeListener() {
        @Override
        public void onValueChange(android.widget.NumberPicker picker, int oldVal, int newVal) {
            if (selectionListener != null) {
                selectionListener.timeSelected(leftPicker.getValue(), rightPicker.getValue());
            }
        }
    };

    @NonNull
    public static DurationPickerPopup newInstance(String title, SelectionListener listener) {
        DurationPickerPopup fragment = new DurationPickerPopup();

        Bundle bundle = new Bundle(1);
        bundle.putString(TITLE_KEY, title);
        fragment.setArguments(bundle);

        fragment.selectionListener = listener;

        return fragment;
    }

    @Override
    public void setFloatingTitle() {
        if (Strings.isNullOrEmpty(getTitle())) {
            title.setVisibility(View.GONE);
        }
        else {
            title.setText(getTitle());
        }
    }

    @Override
    public void doContentSection() {
        initializePickers();

        timerText = (Version1TextView) contentView.findViewById(R.id.timer_text_number_picker);
        indefiniteText = (Version1TextView) contentView.findViewById(R.id.indefinite_text_number_picker);

        timePickersContainer = contentView.findViewById(R.id.time_picker_container);
        indefiniteTextContainer = contentView.findViewById(R.id.time_picker_indefinitely_container);

        blackOutline = ContextCompat.getDrawable(getActivity(), R.drawable.outline_button_style_black);
        whiteOutline = ContextCompat.getDrawable(getActivity(), R.drawable.outline_button_style);

        timerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOutlineColorBlack(timerText, true);
                setOutlineColorBlack(indefiniteText, false);
                timePickersContainer.setVisibility(View.VISIBLE);
                indefiniteTextContainer.setVisibility(View.GONE);
            }
        });
        indefiniteText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setOutlineColorBlack(timerText, false);
                setOutlineColorBlack(indefiniteText, true);
                timePickersContainer.setVisibility(View.GONE);
                indefiniteTextContainer.setVisibility(View.VISIBLE);

                if (selectionListener != null) {
                    selectionListener.indefiniteSelected();
                }
            }
        });
    }

    protected void setOutlineColorBlack(@NonNull TextView textView, boolean selected) {
        textView.setBackground(selected ? blackOutline : whiteOutline);
    }

    protected void initializePickers() {
        leftPicker = (NumberPicker) contentView.findViewById(R.id.hours_picker);
        rightPicker = (NumberPicker) contentView.findViewById(R.id.minutes_picker);
        leftPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        rightPicker.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        leftPicker.setMinValue(0);
        leftPicker.setMaxValue(23);

        rightPicker.setMaxValue(0);
        rightPicker.setMaxValue(59);

        leftPicker.setWrapSelectorWheel(false);
        rightPicker.setWrapSelectorWheel(true);

        leftPicker.setOnValueChangedListener(valueChangeListener);
        rightPicker.setOnValueChangedListener(valueChangeListener);
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_duration_picker;
    }

    @Nullable
    @Override
    public String getTitle() {
        String title = null;

        if (getArguments() != null) {
            title = getArguments().getString(TITLE_KEY);
        }

        return title;
    }

    public interface SelectionListener {
        void timeSelected(Integer leftValue, Integer rightValue);
        void indefiniteSelected();
    }
}
