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
package arcus.app.subsystems.rules.adapters;

import android.content.Context;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;

import arcus.app.R;
import arcus.app.common.models.ListItemModel;
import arcus.app.common.view.Version1TextView;


public class RuleRepeatAdapter extends ArrayAdapter<ListItemModel> {

    private CheckableChevronClickListener listener;

    public RuleRepeatAdapter(Context context) {
        super(context, 0);
    }

    public void setCheckableChevronClickListener (CheckableChevronClickListener listener) {
        this.listener = listener;
    }

    public void setChecked (int position, boolean isChecked) {
        super.getItem(position).setData(isChecked);
        notifyDataSetInvalidated();
    }

    @Nullable
    @Override
    public View getView(final int position, @Nullable View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cell_checkable_chevron_item, parent, false);
        }

        final ListItemModel thisItem = getItem(position);
        final boolean isChecked = (boolean) thisItem.getData();

        ImageView checkbox = (ImageView) convertView.findViewById(R.id.checkbox);
        Version1TextView title = (Version1TextView) convertView.findViewById(R.id.title);
        Version1TextView subtitle = (Version1TextView) convertView.findViewById(R.id.subtitle);
        LinearLayout chevronClickRegion = (LinearLayout) convertView.findViewById(R.id.chevron_click_region);
        LinearLayout checkboxClickRegion = (LinearLayout) convertView.findViewById(R.id.checkbox_click_region);

        chevronClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onChevronRegionClicked(position, thisItem);
                }
            }
        });

        checkboxClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onCheckboxRegionClicked(position, thisItem, isChecked);
                }
            }
        });

        title.setText(thisItem.getText());
        subtitle.setText(thisItem.getSubText());
        checkbox.setImageResource(isChecked ? R.drawable.circle_check_black_filled : R.drawable.circle_hollow_black);

        return convertView;
    }
}
