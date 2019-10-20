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
package arcus.app.common.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.view.Version1TextView;

import java.util.Arrays;
import java.util.List;


public class CheckableListAdapter extends ArrayAdapter<String> {

    private List<Integer> disabledItems;
    private String[] array;
    private int selectedItem = 0;
    private AdapterCallback callback;
    private boolean lightColorScheme = true;

    public interface AdapterCallback {
        void onSelectionChanged();
    }

    public CheckableListAdapter(Context context, int resource, String[] array, int defaultSelection) {
        super(context, resource);
        selectedItem = defaultSelection;
        this.array = array;
    }

    public CheckableListAdapter(Context context, int resource, String[] array, int defaultSelection, AdapterCallback callback) {
        super(context, resource);
        selectedItem = defaultSelection;
        this.array = array;
        this.callback = callback;
    }

    public void setLightColorScheme (boolean lightColorScheme) {
        this.lightColorScheme = lightColorScheme;
    }

    public boolean isLightColorScheme() {
        return lightColorScheme;
    }

    public int getSelectedItem() {
        return selectedItem;
    }

    public void setDisabledItems(Integer... disabledItems) {
        this.disabledItems = Arrays.asList(disabledItems);
    }

    @Override
    public boolean areAllItemsEnabled () {
        return disabledItems == null || disabledItems.size() == 0;
    }

    @Override
    public boolean isEnabled(int position) {
        return areAllItemsEnabled() || !disabledItems.contains(position);
    }

    @Override
    public int getCount() {
        return array.length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cell_checkable_item, parent, false);
        }
        Version1TextView text = (Version1TextView) convertView.findViewById(R.id.title);
        ImageView checkbox = (ImageView) convertView.findViewById(R.id.checkbox);

        int checked = isLightColorScheme() ? R.drawable.circle_check_black_filled : R.drawable.circle_check_white_filled;
        int unchecked = isLightColorScheme() ? R.drawable.circle_hollow_black : R.drawable.circle_hollow_white;

        checkbox.setImageResource(selectedItem == position ? checked : unchecked);
        checkbox.setVisibility(View.VISIBLE);

        text.setText(array[position]);
        if (!isEnabled(position)) {
            text.setTextColor(Color.GRAY);
        } else {
            if(isLightColorScheme()) {
                text.setTextColor(Color.BLACK);
            }
            else {
                text.setTextColor(Color.WHITE);
            }
        }

        convertView.setTag(position);
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isEnabled((int)v.getTag())) {
                    selectedItem = (int)v.getTag();
                    notifyDataSetChanged();
                    if (callback != null) {
                        callback.onSelectionChanged();
                    }
                }
            }
        });

        return convertView;
    }
}
