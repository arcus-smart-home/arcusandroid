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
package arcus.app.subsystems.care.view;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.view.NumberPicker;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class CareDurationPickerPopup extends ArcusFloatingFragment {
    public interface SelectionListener {
        void timeSelected(long start, long end, long duration);
    }

    private TextView endDurationTextView;
    private NumberPicker left;
    private NumberPicker right;
    private long startTime;

    private static final String TITLE_KEY = "TITLE";
    private static final String START_TIME = "START_TIME";
    private static final String END_TIME = "END_TIME";
    private static final String DISPLAY_FORMAT = "DISPLAY_FORMAT";

    private final SimpleDateFormat sdf = new SimpleDateFormat("cccc h:mm a", Locale.getDefault());
    private WeakReference<SelectionListener> selectionListener = new WeakReference<>(null);
    private android.widget.NumberPicker.OnValueChangeListener valueChangeListener =
          new android.widget.NumberPicker.OnValueChangeListener() {
              @Override public void onValueChange(android.widget.NumberPicker picker, int oldVal, int newVal) {
                  if (startTime == -1) {
                      return;
                  }

                  long endTime = startTime + getDuration();
                  if (endDurationTextView != null) {
                      endDurationTextView.setText(sdf.format(new Date(endTime)).toUpperCase());
                  }
              }
          };

    @NonNull public static CareDurationPickerPopup newInstance(
          String title,
          long startTime,
          @Nullable Long endTime,
          @Nullable String timeDisplayFormat
    ) {
        CareDurationPickerPopup fragment = new CareDurationPickerPopup();

        Bundle bundle = new Bundle(4);
        bundle.putString(TITLE_KEY, title);
        bundle.putLong(START_TIME, startTime);
        bundle.putLong(END_TIME, endTime == null ? -1 : endTime);
        bundle.putString(DISPLAY_FORMAT, timeDisplayFormat);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override public void setFloatingTitle() {
        if (TextUtils.isEmpty(getTitle())) {
            title.setVisibility(View.GONE);
        }
        else {
            title.setText(getTitle());
        }
    }

    @Override public void doContentSection() {
        initializePickers();
    }

    public void setSelectionListener(SelectionListener listener) {
        selectionListener = new WeakReference<>(listener);
    }

    protected void initializePickers() {
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        String providedFormat = args.getString(DISPLAY_FORMAT);
        if (!TextUtils.isEmpty(providedFormat)) {
            sdf.applyPattern(providedFormat);
        }

        TextView endTextView = (TextView) contentView.findViewById(R.id.care_end_text_view);
        endTextView.setText(getString(R.string.generic_end_text));

        endDurationTextView = (TextView) contentView.findViewById(R.id.care_duration_text_view);

        startTime = args.getLong(START_TIME, -1);
        left = (NumberPicker) contentView.findViewById(R.id.hours_picker);
        right = (NumberPicker) contentView.findViewById(R.id.minutes_picker);
        left.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        right.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        left.setMinValue(0);
        left.setMaxValue(23);

        right.setMaxValue(0);
        right.setMaxValue(59);

        left.setWrapSelectorWheel(false);
        right.setWrapSelectorWheel(true);

        left.setOnValueChangedListener(valueChangeListener);
        right.setOnValueChangedListener(valueChangeListener);

        long endTime = args.getLong(END_TIME);
        if (endTime == -1) {
            return;
        }

        Long hours = TimeUnit.MILLISECONDS.toHours(endTime - startTime);
        Long minutes = TimeUnit.MILLISECONDS.toMinutes(endTime - startTime - TimeUnit.HOURS.toMillis(hours));

        left.setValue(hours.intValue());
        right.setValue(minutes.intValue());
        endDurationTextView.setText(sdf.format(new Date(endTime)).toUpperCase());
    }

    @Override public Integer contentSectionLayout() {
        return R.layout.care_floating_duration_picker;
    }

    @Nullable @Override public String getTitle() {
        String title = null;

        Bundle args = getArguments();
        if (args != null) {
            title = args.getString(TITLE_KEY, null);
        }

        return title;
    }

    @Override public void doClose() {
        SelectionListener listener = selectionListener.get();
        if (listener == null || startTime == -1) {
            return;
        }

        long duration = getDuration();
        listener.timeSelected(startTime, startTime + duration, duration);
    }

    protected long getDuration() {
        return getHoursMillis(left.getValue()) + getMinutesMillis(right.getValue());
    }

    protected long getHoursMillis(long time) {
        return TimeUnit.HOURS.toMillis(time);
    }

    protected long getMinutesMillis(long time) {
        return TimeUnit.MINUTES.toMillis(time);
    }
}
