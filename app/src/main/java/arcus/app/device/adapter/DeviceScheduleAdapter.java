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
package arcus.app.device.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.models.ArcusSchedule;

import java.util.ArrayList;
import java.util.List;


public class DeviceScheduleAdapter extends BaseAdapter {

    private List<ArcusSchedule> scheduleList;

    private LayoutInflater inflater;

    public DeviceScheduleAdapter(@NonNull Context context) {
        init();
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Context context1 = context;
    }

    private static class ViewHolder {
        public TextView day;
        public TextView onTime;
        public TextView offTime;
    }


    @Override
    public int getCount() {
        return scheduleList.size();
    }

    @Override
    public ArcusSchedule getItem(int position) {
        return scheduleList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder;
        final ArcusSchedule arcusSchedule = getItem(position);
        if (view == null) {
            view = inflater.inflate(R.layout.device_schedule_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.day = (TextView) view.findViewById(R.id.device_schedule_day);
            viewHolder.onTime = (TextView) view.findViewById(R.id.device_schedule_on_time);
            viewHolder.offTime = (TextView) view.findViewById(R.id.device_schedule_off_time);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        String dayS = "";
        switch (position){
            case 0:
                dayS = "MON";
                break;
            case 1:
                dayS = "TUE";
                break;
            case 2:
                dayS = "WED";
                break;
            case 3:
                dayS = "THU";
                break;
            case 4:
                dayS = "FRI";
                break;
            case 5:
                dayS = "SAT";
                break;
            case 6:
                dayS = "SUN";
                break;
        }
        viewHolder.day.setText(dayS);

        viewHolder.onTime.setText(arcusSchedule.getOnTime());
        viewHolder.offTime.setText(arcusSchedule.getOffTime());

        return view;
    }

    private void init() {
        scheduleList = new ArrayList<ArcusSchedule>();
        scheduleList.add(new ArcusSchedule("8:30A", "10A"));
        scheduleList.add(new ArcusSchedule("", ""));
        scheduleList.add(new ArcusSchedule("8:30A", "10A"));
        scheduleList.add(new ArcusSchedule("", ""));
        scheduleList.add(new ArcusSchedule("8:30A", "10A"));
        scheduleList.add(new ArcusSchedule("10A", "12P"));
        scheduleList.add(new ArcusSchedule("", ""));

    }
}
