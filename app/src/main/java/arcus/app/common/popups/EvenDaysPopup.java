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
import android.view.View;
import android.widget.NumberPicker;

import arcus.app.R;
import arcus.app.common.events.FloatingDayOrDeviceSelected;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import de.greenrobot.event.EventBus;

public class EvenDaysPopup extends ArcusFloatingFragment {
    private final SimpleDateFormat headerDateFormat = new SimpleDateFormat("ccc MMM d", Locale.US);
    private NumberPicker picker;
    private Callback callback;
    private View.OnClickListener onClickListener;

    public interface Callback {
        void selected(long time);
    }

    @NonNull
    public static EvenDaysPopup newInstance() {
        return new EvenDaysPopup();
    }

    public EvenDaysPopup() {}

    public void setCallback(Callback useCallback) {
        this.callback = useCallback;
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.onClickListener = listener;
    }

    @Override
    public void setFloatingTitle() {
        title.setText(getResources().getString(R.string.choose_day_text));
    }

    @Override
    public void doContentSection() {
        Calendar current = Calendar.getInstance();
        String[] days = new String[7];
        int j=0;
        int i=0;
        do {
            if ( (current.get(Calendar.DAY_OF_MONTH) & 1) == 0 ) {
                if (i > 1) {
                    days[j] = headerDateFormat.format(current.getTime());
                }
                else {
                    days[j] = i == 0 ? getString(R.string.today) : getString(R.string.tomorrow);
                }
                j++;
                i++;
            } else {
                i++;
            }
            current.add(Calendar.DAY_OF_MONTH, 1);
        } while(i<14);

        picker = (NumberPicker) contentView.findViewById(R.id.floating_day_number_picker);
        picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
        picker.setMinValue(0);
        picker.setMaxValue(6);
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
        Calendar selected = Calendar.getInstance();
        selected.add(Calendar.DAY_OF_MONTH, -picker.getValue());
        selected.set(Calendar.HOUR_OF_DAY, 23);
        selected.set(Calendar.MINUTE, 59);
        selected.set(Calendar.SECOND, 59);
        logger.debug("Using: [{}]", DateFormat.getDateTimeInstance().format(selected.getTime()));
        EventBus.getDefault().post(new FloatingDayOrDeviceSelected(selected.getTimeInMillis()));
        if (callback != null) {
            callback.selected(selected.getTimeInMillis());
        }
    }
}
