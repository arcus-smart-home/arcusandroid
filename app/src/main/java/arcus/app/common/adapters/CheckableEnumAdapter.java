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
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.models.ListItemModel;
import arcus.app.device.settings.core.Localizable;

import java.util.ArrayList;
import java.util.List;


public class CheckableEnumAdapter<T extends Localizable> extends EnumAdapter<T> {

    private final SelectionChangedListener<T> listener;
    private boolean singleSelectionMode = true;

    public CheckableEnumAdapter(@NonNull Context context, @NonNull Class<T> enumeration, T initialValue, SelectionChangedListener<T> listener) {
        super(context, enumeration);
        this.listener = listener;

        setCheckedItem(initialValue);
    }

    public CheckableEnumAdapter(@NonNull Context context, @NonNull T[] enumValues, T initialValue, SelectionChangedListener<T> listener) {
        super(context, enumValues);
        this.listener = listener;

        setCheckedItem(initialValue);
    }

    private void setCheckedItem (T checkedItem) {
        for (int index = 0; index < getCount(); index++) {
            T thisValue = getEnumAt(index);
            getItem(index).setChecked(thisValue == checkedItem);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = super.getView(position, convertView, parent);

        final ListItemModel thisItem = getItem(position);

        ImageView checkbox = (ImageView) convertView.findViewById(R.id.checkbox);
        checkbox.setVisibility(View.VISIBLE);

        int checked = isLightColorScheme() ? R.drawable.circle_check_white_filled : R.drawable.circle_check_black_filled;
        int unchecked = isLightColorScheme() ? R.drawable.circle_hollow_white : R.drawable.circle_hollow_black;
        checkbox.setImageResource(thisItem.isChecked() ? checked : unchecked);

        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (singleSelectionMode) {
                    clearSelections();
                    thisItem.setChecked(true);
                } else {
                    thisItem.setChecked(!thisItem.isChecked());
                    notifyDataSetChanged();
                }

                fireSelectionChangedListener();
            }
        });

        return convertView;
    }

    public void clearSelections () {
        for (int index = 0; index < getCount(); index++) {
            ListItemModel thisItem = getItem(index);
            thisItem.setChecked(false);
        }

        notifyDataSetInvalidated();
    }

    private void fireSelectionChangedListener() {
        ArrayList<T> selections = new ArrayList<>();

        for (int index = 0; index < getCount(); index++) {
            if (getItem(index).isChecked()) {
                selections.add((T) getItem(index).getData());
            }
        }

        if (listener != null) {
            listener.onSelectionChanged(selections);
        }
    }

    public void setSingleSelectionMode (boolean enabled) {
        this.singleSelectionMode = enabled;
    }

    public interface SelectionChangedListener<T> {
        void onSelectionChanged(List<T> selection);
    }
}
