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
package arcus.app.subsystems.care.adapter;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.subsystem.care.model.TimeWindowModel;
import arcus.cornea.utils.DayTime;
import arcus.app.R;

import java.util.List;

public class CareBehaviorScheduleAdapter extends ArrayAdapter<TimeWindowModel> {
    public interface ItemClickListener {
        void itemClicked(TimeWindowModel timeWindowModel);
    }
    private ItemClickListener itemClickListener;

    public CareBehaviorScheduleAdapter(Context context, List<TimeWindowModel> items) {
        super(context, 0);
        addAll(items);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.care_schedule_list_item, parent, false);
        }

        TimeWindowModel timeWindowModel = getItem(position);
        View container = convertView.findViewById(R.id.list_item_container);
        container.setTag(timeWindowModel);
        container.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Object tag = v.getTag();
                if (tag == null || !(tag instanceof TimeWindowModel)) {
                    return;
                }

                ItemClickListener clickListener = itemClickListener;
                if (clickListener != null) {
                    clickListener.itemClicked((TimeWindowModel) tag);
                }
            }
        });
        ImageView imageView = (ImageView) convertView.findViewById(R.id.time_of_day_icon);
        TextView textView = (TextView) convertView.findViewById(R.id.list_item_text_view);

        if (DayTime.DAYTIME.equals(DayTime.fromHour(timeWindowModel.getStartHour()))) {
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.icon_day));
        }
        else {
            imageView.setImageDrawable(ContextCompat.getDrawable(getContext(), R.drawable.icon_night));
        }

        textView.setText(timeWindowModel.getStringRepresentation());

        return convertView;
    }

    public void setOnItemClickListener(ItemClickListener clickListener) {
        itemClickListener = clickListener;
    }
}
