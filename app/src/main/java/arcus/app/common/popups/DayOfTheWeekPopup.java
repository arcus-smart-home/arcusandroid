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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.common.collect.Lists;
import arcus.cornea.utils.DayOfWeek;
import arcus.app.R;
import arcus.app.common.popups.adapter.CheckboxItemAdapter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class DayOfTheWeekPopup extends ArcusFloatingFragment {
    @NonNull
    private static String DAYS = "DAYS";
    private Callback callback;

    public interface Callback {
        void selectedItems(EnumSet<DayOfWeek> dayOfWeek);
    }

    @NonNull
    public static DayOfTheWeekPopup newInstance(EnumSet<DayOfWeek> selectedDays) {
        DayOfTheWeekPopup popup = new DayOfTheWeekPopup();
        Bundle bundle = new Bundle(1);
        bundle.putSerializable(DAYS, selectedDays);
        popup.setArguments(bundle);
        return popup;
    }

    @NonNull
    public static DayOfTheWeekPopup newInstance() {
        DayOfTheWeekPopup popup = new DayOfTheWeekPopup();
        Bundle bundle = new Bundle(1);
        bundle.putSerializable(DAYS, null);
        popup.setArguments(bundle);
        return popup;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    @Override
    public void setFloatingTitle() {
        if (getTitle() == null) {
            title.setVisibility(View.GONE);
        }
        else {
            title.setText(getTitle());
        }
    }

    @Override @SuppressWarnings({"unchecked"})
    public void doContentSection() {
        ListView listView = (ListView) contentView.findViewById(R.id.floating_list_view);
        listView.setFooterDividersEnabled(false);
        List<DayOfWeek> selectedDays = new ArrayList<>();
        List<DayOfWeek> weekDays = Lists.newArrayList(DayOfWeek.values());
        if (getArguments() != null) {
            Serializable object = getArguments().getSerializable(DAYS);
            if (object != null) {
                EnumSet<DayOfWeek> items = (EnumSet<DayOfWeek>) object;
                selectedDays = Lists.newArrayList(items.iterator());
            }
        }
        final CheckboxItemAdapter<DayOfWeek> adapter = new CheckboxItemAdapter<DayOfWeek>(getActivity(), weekDays, selectedDays) {
            @Override
            public void setItemText(@NonNull TextView textView, @NonNull DayOfWeek item) {
                textView.setText(item.name().toUpperCase());
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                List<DayOfWeek> daysSelected = adapter.toggleCheck(position);
                if (callback != null) {
                    if (daysSelected != null && !daysSelected.isEmpty()) {
                        callback.selectedItems(EnumSet.copyOf(daysSelected));
                    }
                    else {
                        callback.selectedItems(EnumSet.noneOf(DayOfWeek.class));
                    }
                }
            }
        });
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.floating_list_picker;
    }

    @NonNull
    @Override
    public String getTitle() {
        return "REPEAT EVENT ON";
    }
}
