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
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.models.ListItemModel;

import java.util.List;

public class CareTemplatesListAdapter extends ArrayAdapter<ListItemModel> {

    public CareTemplatesListAdapter(Context context, int resource) {
        this(context);
    }

    public CareTemplatesListAdapter(Context context, int resource, int textViewResourceId) {
        super(context, resource, textViewResourceId);
    }

    public CareTemplatesListAdapter(Context context, int resource, ListItemModel[] objects) {
        super(context, resource, objects);
    }

    public CareTemplatesListAdapter(Context context, int resource, int textViewResourceId, ListItemModel[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public CareTemplatesListAdapter(Context context, int resource, List<ListItemModel> objects) {
        super(context, resource, objects);
    }

    public CareTemplatesListAdapter(Context context, int resource, int textViewResourceId, List<ListItemModel> objects) {
        super(context, resource, textViewResourceId, objects);
    }

    public CareTemplatesListAdapter(Context context) {
        super(context, 0);
    }

    @Override public View getView(int position, View convertView, ViewGroup parent) {
        ListItemModel item = getItem(position);
        if (item.isHeadingRow()) {
            return getViewForHeading(item, parent);
        }
        else {
            boolean nextItemHeader = false;
            try {
                nextItemHeader = getItem(position + 1).isHeadingRow();
            }
            catch (Exception ignored) { // Last Item
                nextItemHeader = true;
            }
            return getViewForNonHeader(item, parent, nextItemHeader);
        }
    }

    private View getViewForNonHeader(@NonNull ListItemModel item, ViewGroup parent, boolean isNextHeader) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.behavior_item, parent, false);
        TextView text;
        TextView subText;
        View divider;

        text = (TextView) view.findViewById(R.id.headerText);
        subText = (TextView) view.findViewById(R.id.subText);
        divider = view.findViewById(R.id.item_divider);

        text.setText(item.getText());
        subText.setText(item.getSubText());
        if (divider != null) {
            if (isNextHeader) {
                divider.setVisibility(View.GONE);
            }
            else {
                divider.setVisibility(View.VISIBLE);
            }
        }

        return view;
    }

    private View getViewForHeading (@NonNull ListItemModel headingData, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.behavior_heading_item, parent, false);

        TextView headingLabel = (TextView) view.findViewById(R.id.heading_text);
        headingLabel.setText(headingData.getText());

        view.setEnabled(false);
        view.setOnClickListener(null);

        return view;
    }

}
