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
package arcus.app.seasonal.christmas.fragments.adapter;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.seasonal.christmas.model.SantaHistoryItemModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class SantaHistoryListAdapter extends ArrayAdapter<SantaHistoryItemModel> {
    private static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public SantaHistoryListAdapter(Context context, List<SantaHistoryItemModel> objects) {
        super(context, 0, objects);
    }

    @Override @Nullable
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.santa_fragment_history_item, parent, false);
        }

        SantaHistoryItemModel item = getItem(position);
        if (item == null) {
            return convertView;
        }

        TextView timeText = (TextView) convertView.findViewById(R.id.history_time);
        if (timeText != null) {
            timeText.setText(simpleDateFormat.format(item.getTime()));
        }

        TextView titleText = (TextView) convertView.findViewById(R.id.history_title);
        if (titleText != null) {
            titleText.setText(item.getTitle());
        }

        TextView descriptionText = (TextView) convertView.findViewById(R.id.history_description);
        if (descriptionText != null) {
            descriptionText.setText(item.getDescription());
        }

        return (convertView);
    }
}
