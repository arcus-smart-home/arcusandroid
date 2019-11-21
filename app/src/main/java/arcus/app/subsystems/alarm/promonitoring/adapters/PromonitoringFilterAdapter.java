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
import arcus.app.subsystems.alarm.promonitoring.models.PromonitoringFilter;

import java.util.List;

public class PromonitoringFilterAdapter extends ArrayAdapter<PromonitoringFilter> {
    private Context context;
    private PromonitoringFilter checked;

    public PromonitoringFilterAdapter(Context context, @NonNull List<PromonitoringFilter> options, PromonitoringFilter checked) {
        super(context, 0, options);

        this.context = context;
        this.checked = checked;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.show_all_menu_item, parent, false);
        }

        if (position == 0) {  // Hide chevron from "All" option.
            convertView.findViewById(R.id.imgChevron).setVisibility(View.INVISIBLE);
        }

        TextView view = (TextView) convertView.findViewById(R.id.show_all_menu_item_text);
        view.setText(getItem(position).getTitle(getContext()));

        ImageView checkBox = (ImageView) convertView.findViewById(R.id.action_checkbox);
        checkBox.setImageResource(getItem(position) == checked ? R.drawable.circle_check_white_filled : R.drawable.circle_hollow_white);

        return convertView;
    }
}
