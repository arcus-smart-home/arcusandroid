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
package arcus.app.subsystems.history.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.iris.client.bean.HistoryLog;
import arcus.app.R;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.validation.Filter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HistoryFragmentListAdapter extends BaseAdapter {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a", Locale.US);
    private final SimpleDateFormat headerDateFormat = new SimpleDateFormat("ccc MMM d", Locale.US);
    private List<HistoryLog> entries;
    private Map<Integer, Integer> headerIndexes;
    private Context context;
    private Calendar calendar = Calendar.getInstance();
    private boolean showHeaders = true;

    public HistoryFragmentListAdapter(Context context, @NonNull List<HistoryLog> entries) {
        this.entries = new ArrayList<>(entries);
        Collections.sort(this.entries, CorneaUtils.DESC_HISTORY_LOG_COMPARATOR);

        this.context = context;
        headerIndexes = new HashMap<>();
        addHeaderDates(this.entries, 0);
    }

    public HistoryFragmentListAdapter(Context context, @NonNull List<HistoryLog> entries, boolean showHeaders) {
        this.entries = new ArrayList<>(entries);
        Collections.sort(this.entries, CorneaUtils.DESC_HISTORY_LOG_COMPARATOR);

        this.context = context;
        headerIndexes = new HashMap<>();
        this.showHeaders = showHeaders;
        if(this.showHeaders) {
            addHeaderDates(this.entries, 0);
        }
    }

    public void clear() {
        entries.clear();
    }

    @Override
    public int getCount() {
        return entries.size();
    }

    @Override
    public HistoryLog getItem(int position) {
        return entries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return getItem(position).getTimestamp().getTime();
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.cell_history_list_item_long, parent, false);
        }

        HistoryLog historyLog = getItem(position);

        if (showHeaders && headerIndexes.get(getDayOfYear(historyLog.getTimestamp())) == position) {
            TextView header = (TextView) convertView.findViewById(R.id.history_header_row);
            header.setText(headerDateFormat.format(historyLog.getTimestamp()));
            header.setVisibility(View.VISIBLE);
        }
        else {
            TextView header = (TextView) convertView.findViewById(R.id.history_header_row);
            header.setText("");
            header.setVisibility(View.GONE);
        }

        TextView time = (TextView) convertView.findViewById(R.id.list_time_text);
        TextView device = (TextView) convertView.findViewById(R.id.list_device_name);
        TextView message = (TextView) convertView.findViewById(R.id.list_message);

        time.setText(dateFormat.format(historyLog.getTimestamp()));
        device.setText(historyLog.getSubjectName().trim());
        message.setText(historyLog.getLongMessage().trim());

        return convertView;
    }

    public void trimListToThreshold(int threshold) {
        int listSize = entries.size();

        if (listSize >= threshold) {
            entries.subList(0, listSize - threshold).clear();
            headerIndexes.clear();
            addHeaderDates(entries, 0);
            notifyDataSetChanged();
        }
    }

    private int getDayOfYear(@NonNull Date date) {
        Calendar firstCal = Calendar.getInstance();
        firstCal.setTime(date);

        return firstCal.get(Calendar.DAY_OF_YEAR);
    }

    private void addHeaderDates(@NonNull List<HistoryLog> entries, int startingIndex) {
        for (HistoryLog log : entries) {
            calendar.setTime(log.getTimestamp());
            int dayOfYear = calendar.get(Calendar.DAY_OF_YEAR);

            if (headerIndexes.get(dayOfYear) == null) {
                headerIndexes.put(dayOfYear, startingIndex);
            }

            startingIndex++;
        }
    }

    public void appendEntries(@NonNull List<HistoryLog> entries) {
        List<HistoryLog> copy = new ArrayList<>(entries);
        Collections.sort(copy, CorneaUtils.DESC_HISTORY_LOG_COMPARATOR);

        addHeaderDates(copy, this.entries.size());
        this.entries.addAll(copy);
        notifyDataSetChanged();
    }

    public int getDayOfYearIndex(long timestamp) {
        Integer position = headerIndexes.get(getDayOfYear(new Date(timestamp)));
        return position == null ? -1 : position;
    }

    /**
     *
     * Checks the list to see if we have another day *after* the day passed in indexed.
     *
     * If we do, this will return null so we don't try to query again (since we're filtered by date).
     *
     * If we don't, this will grab the timestamp of the last entry in the (sorted) list, and pass that back to be
     * used as the "Next query token" since it's the latest entry we have so far.
     *
     * @param timestamp
     * @return nextQueryToken or null
     */
    @Nullable
    public String getNextTokenForFilter(long timestamp) {
        Integer referencedDay = getDayOfYear(new Date(timestamp));
        List<Integer> loadedDays = Lists.newArrayList(headerIndexes.keySet());

        // Sort descending so we get the last first instance of the day after that is loaded
        Collections.sort(loadedDays, Collections.reverseOrder());

        for (Integer dayBefore : loadedDays) {
            if (dayBefore < referencedDay) {
                // We have a day previous to the day referenced
                // It's fully loaded, won't need to query.
                return null;
            }
        }

        // If the list is not empty return the last item in the list as our "next" query param.
        if (!entries.isEmpty()) {
            return String.valueOf(entries.get(entries.size() - 1).getTimestamp().getTime());
        }

        return null;
    }

    public void filterExistingToDay(@NonNull Filter<HistoryLog> filter) {
        // Iterate over the list, only show items relevant to timestamp
        if (filter.apply(this.entries)) {
            headerIndexes.clear();
            addHeaderDates(this.entries, 0);
            notifyDataSetChanged();
        }
    }
}
