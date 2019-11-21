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
package arcus.app.device.settings.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;

import java.util.List;


public class EnumeratedListAdapter extends ArrayAdapter<String> {

    public int selectedItem;

    private List<String> descriptions;

    public EnumeratedListAdapter(Context context, @NonNull List<String> values, List<String> descriptions, int initialSelection) {
        super(context, 0, values);
        this.selectedItem = initialSelection;
        this.descriptions = descriptions;
    }

    @Nullable
    @Override
    public View getView(final int position, @Nullable View convertView, ViewGroup parent) {

        final View view;

        if (convertView != null) {
            view = convertView;
        } else {
            view = LayoutInflater.from(getContext()).inflate(R.layout.setting_enum_value, parent, false);
        }

        TextView valueView = (TextView) view.findViewById(R.id.enum_value);
        valueView.setText(getItem(position).toUpperCase());

        TextView descriptionView = (TextView) view.findViewById(R.id.enum_description);;

        if (descriptions.size() > 0) {
            descriptionView.setText(descriptions.get(position));
            descriptionView.setVisibility(View.VISIBLE);
        } else {
            descriptionView.setVisibility(View.GONE);
        }

        boolean checked = position == selectedItem;
        ImageView checkbox = (ImageView) view.findViewById(R.id.enum_checkbox);
        checkbox.setImageResource(checked ? R.drawable.circle_check_black_filled : R.drawable.circle_hollow_black);

        final EnumeratedListAdapter self = this;
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedItem = position;
                self.notifyDataSetChanged();
            }
        });

        return view;
    }

    public String getSelectedValue () {
        return getItem(selectedItem);
    }
}
