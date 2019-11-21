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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.models.DeviceMode;

import java.util.ArrayList;

public class PopupDeviceModeListAdapter extends ArrayAdapter<DeviceMode> implements AdapterView.OnItemClickListener {
    private boolean hideChecks = false;
    private String checkedItem;
    private DeviceMode currentMode;

    public PopupDeviceModeListAdapter(@NonNull Context context, @NonNull ArrayList<DeviceMode> deviceModes, @NonNull String checkedItem) {
        super(context, R.layout.floating_device_mode_picker_item, deviceModes);
        this.checkedItem = checkedItem;
    }

    public void setHideChecks(boolean hideChecks) {
        this.hideChecks = hideChecks;
    }

    public void setSelection(String selection) {
        checkedItem = selection;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.floating_device_mode_picker_item, parent, false);
        }

        currentMode = getItem(position);
        ImageView checkBox = (ImageView) convertView.findViewById(R.id.action_checkbox);
        if (hideChecks) {
            checkBox.setVisibility(View.GONE);
        }
        else {
            checkBox.setVisibility(View.VISIBLE);
            if (currentMode.getTitle().equals(checkedItem)) {
                checkBox.setImageResource(R.drawable.circle_check_black_filled);
            }
            else {
                checkBox.setImageResource(R.drawable.circle_hollow_black);
            }
        }

        TextView name = (TextView) convertView.findViewById(R.id.list_item_name);
        TextView description = (TextView) convertView.findViewById(R.id.list_item_description);
        ImageView deviceImage = (ImageView) convertView.findViewById(R.id.device_image);
        deviceImage.setVisibility(View.GONE);

        name.setText(currentMode.getTitle());
        if(currentMode.getDescription() == null || currentMode.getDescription().equals("")) {
            description.setVisibility(View.GONE);
        }
        else {
            description.setText(currentMode.getDescription());
        }

        return(convertView);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }
}
