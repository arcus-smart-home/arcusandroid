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
package arcus.app.subsystems.alarm.promonitoring.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.subsystems.alarm.promonitoring.models.HistoryListItemModel;

import java.util.List;

public class ProMonitoringHistoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final int historyCellLayoutId;
    private List<HistoryListItemModel> items; // All items, with the header(s)

    public ProMonitoringHistoryAdapter(int historyCellLayoutId, @NonNull List<HistoryListItemModel> historyEntries) {
        this.items = historyEntries;
        this.historyCellLayoutId = historyCellLayoutId;

        // On click of one of these (adding a check mark or deleting it from the selected list)
        // update the respective model (checked -> not checked taking from, or putting back to, popular/not popular)
        // If checked has 0 items, add the nonCheckedItem in it's list
        setHasStableIds(true);
    }

    public HistoryListItemModel getItemAt(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).getStyle().ordinal();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((HistoryListItemModelViewHolder) holder).bind(items.get(position));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (HistoryListItemModel.HistoryListItemStyle.values()[viewType]) {
            case SECTION_HEADING:
                return new HeaderItemViewHolder(inflater.inflate(R.layout.weather_alert_category_header_item, parent, false));
            case HISTORY_ITEM:
                return new HistoryItemViewHolder(inflater.inflate(historyCellLayoutId, parent, false));
            case HISTORY_DETAIL_DISCLOSURE:
                return new IncidentItemViewHolder(inflater.inflate(historyCellLayoutId, parent, false));
            case NO_ACTIVITY:
                return new NoActivityItemViewHolder(inflater.inflate(R.layout.cell_history_no_activity, parent, false));
            default:
                throw new IllegalArgumentException("Bug! Unimplemented history cell type");
        }
    }

    private interface HistoryListItemModelViewHolder {
        void bind(HistoryListItemModel model);
    }

    public void addAll(List<HistoryListItemModel> items) {
        this.items.addAll(items);
        notifyItemInserted(0);
    }

    public void add(HistoryListItemModel item) {
        items.add(0, item);
    }

    public void removeAll() {
        this.items.clear();
        notifyDataSetChanged();
    }

    public void updateItems(List<HistoryListItemModel> items) {
        this.items = items;
        notifyDataSetChanged();
    }


    /**
     * Viewholder for simple history items (i.e., not an incident with disclosure chevron.
     */
    private class HistoryItemViewHolder extends RecyclerView.ViewHolder implements HistoryListItemModelViewHolder {

        private TextView time;
        private TextView device;
        private TextView message;
        private TextView header;

        public HistoryItemViewHolder(View itemView) {
            super(itemView);
            time = (TextView) itemView.findViewById(R.id.list_time_text);
            device = (TextView) itemView.findViewById(R.id.list_device_name);
            message = (TextView) itemView.findViewById(R.id.list_message);
            header = (TextView) itemView.findViewById(R.id.history_header_row);
        }

        public void bind(HistoryListItemModel model) {
            time.setText(model.getTimestampString());
            device.setText(model.getTitle().toUpperCase());
            message.setText(model.getSubtitle());
            header.setVisibility(View.GONE);
        }
    }

    /**
     * Viewholder for date headers.
     */
    private class HeaderItemViewHolder extends RecyclerView.ViewHolder implements HistoryListItemModelViewHolder {

        private TextView title;

        public HeaderItemViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.category_title);
        }

        public void bind(HistoryListItemModel data) {
            title.setText(data.getTitle());
        }
    }

    /**
     * Viewholder for "You have no alarm activity" placeholder cells
     */
    private class NoActivityItemViewHolder extends RecyclerView.ViewHolder implements HistoryListItemModelViewHolder {

        private TextView title;

        public NoActivityItemViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
        }

        public void bind(HistoryListItemModel data) {
            title.setText(data.getTitle());
        }
    }

    /**
     * Viewholder for incident history items (i.e., those with a disclosure chevron and icon)
     */
    private class IncidentItemViewHolder extends HistoryItemViewHolder implements HistoryListItemModelViewHolder {

        private ImageView icon;
        private ImageView chevron;

        public IncidentItemViewHolder(View itemView) {
            super(itemView);
            icon = (ImageView) itemView.findViewById(R.id.history_image);
            chevron = (ImageView) itemView.findViewById(R.id.chevron);
        }

        @Override
        public void bind(HistoryListItemModel model) {
            super.bind(model);

            // Should never be null, but just in case...
            if (model.getAbstractIcon() != null) {
                icon.setVisibility(View.VISIBLE);
                chevron.setVisibility(View.VISIBLE);
                icon.setImageResource(model.getAbstractIcon());
            }
        }
    }
}
