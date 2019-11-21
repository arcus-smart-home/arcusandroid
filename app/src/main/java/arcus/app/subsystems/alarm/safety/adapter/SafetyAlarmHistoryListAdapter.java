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
package arcus.app.subsystems.alarm.safety.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import arcus.cornea.subsystem.safety.model.HistoryEvent;
import arcus.app.R;
import arcus.app.common.utils.CorneaUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class SafetyAlarmHistoryListAdapter extends BaseAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;

    private List<HistoryEvent> entries;
    private Map<Integer, Integer> headerIndexes;
    private Calendar calendar = Calendar.getInstance();

    private final SimpleDateFormat headerDateFormat = new SimpleDateFormat("ccc MMM d", Locale.US);
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aaa", Locale.US);

    private Context mContext;

    private LayoutInflater mInflater;

    public SafetyAlarmHistoryListAdapter(@NonNull Context context){
        this.entries = new ArrayList<>();
        headerIndexes = new HashMap<>();
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Context context1 = context;
    }

    public void setHistoryEntries(final List<HistoryEvent> entries){
        this.entries = entries;
        Collections.sort(this.entries, CorneaUtils.DESC_HISTORY_EVENT_COMPARATOR);
        addHeaderDates(this.entries, 0);

        notifyDataSetChanged();
    }

    private void addHeaderDates(@NonNull List<HistoryEvent> entries, int startingIndex){
        for (HistoryEvent log : entries) {
            calendar.setTime(log.getTriggeredAt());
            int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

            if (headerIndexes.get(dayOfYear) == null) {
                headerIndexes.put(dayOfYear, startingIndex);
            }

            startingIndex++;
        }
    }

    public void clear() {
        entries.clear();
        headerIndexes.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public HistoryEvent getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getTriggeredAt().getTime();
    }

    @Nullable
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        ViewHolder holder;

        final HistoryEvent historyEvent = getItem(position);

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.cell_alarm_history, parent, false);
            holder.headerTitle = (TextView) convertView.findViewById(R.id.history_header_title);
            holder.triggeredTime = (TextView) convertView.findViewById(R.id.alarm_history_time);
            holder.triggeredBy = (TextView) convertView.findViewById(R.id.alarm_history_triggered_by);
            holder.disarmedBy = (TextView) convertView.findViewById(R.id.alarm_history_disarmed_by);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (headerIndexes.get(getDayOfYear(historyEvent.getTriggeredAt())) == position) {
            holder.headerTitle.setVisibility(View.VISIBLE);
            holder.headerTitle.setText(headerDateFormat.format(historyEvent.getTriggeredAt()));
        }else{
            holder.headerTitle.setVisibility(View.GONE);
            holder.headerTitle.setText("");
        }

        holder.triggeredTime.setText(timeFormat.format(historyEvent.getTriggeredAt()));
        holder.triggeredBy.setText("Triggered by " + historyEvent.getTriggeredBy());
        holder.disarmedBy.setText("Disarmed by " + historyEvent.getDisarmedBy());

        return convertView;
    }

    private int getDayOfYear(@NonNull Date date) {
        Calendar firstCal = Calendar.getInstance();
        firstCal.setTime(date);

        return firstCal.get(Calendar.DAY_OF_YEAR);
    }

    public static class ViewHolder {
        public TextView headerTitle;
        public TextView triggeredTime;
        public TextView triggeredBy;
        public TextView disarmedBy;

    }
}
