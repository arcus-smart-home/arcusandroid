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
package arcus.app.common.popups.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import arcus.app.R;

import java.util.ArrayList;
import java.util.List;

public abstract class CheckboxItemAdapter<T> extends ArrayAdapter<T> {

    private List<T> checkedItems;

    public boolean isSingleSelectionMode() {
        return singleSelectionMode;
    }

    public void setSingleSelectionMode(boolean singleSelectionMode) {
        this.singleSelectionMode = singleSelectionMode;
    }

    private boolean singleSelectionMode = false;

    public CheckboxItemAdapter(Context context, @NonNull List<T> listItems, @Nullable List<T> checkedItems) {
        super(context, 0, listItems);

        this.checkedItems = new ArrayList<T>();
        if (checkedItems != null) {
            this.checkedItems.addAll(checkedItems);
        }
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.checkbox_list_item, parent, false);
        }

        CheckBox checkBox = (CheckBox) convertView.findViewById(R.id.checkbox);
        checkBox.setChecked(checkedItems.contains(getItem(position)));

        TextView view = (TextView) convertView.findViewById(R.id.item_name);
        setItemText(view, getItem(position));

        return convertView;
    }

    public abstract void setItemText(TextView textView, T item);

    public List<T> toggleCheck(int position) {
        if (position > (getCount() - 1) || position < 0) {
            return checkedItems;
        }

        if (isSingleSelectionMode()) {
            checkedItems.clear();
            checkedItems.add(getItem(position));
        } else if (!checkedItems.remove(getItem(position))) {
            checkedItems.add(getItem(position));
        }

        notifyDataSetChanged();

        return checkedItems;
    }
}
