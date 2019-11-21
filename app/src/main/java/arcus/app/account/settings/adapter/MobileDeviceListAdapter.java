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
package arcus.app.account.settings.adapter;

import android.content.Context;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.iris.client.model.MobileDeviceModel;
import arcus.app.R;
import arcus.app.common.view.Version1TextView;


public class MobileDeviceListAdapter extends ArrayAdapter<MobileDeviceModel> {

    private boolean editEnabled = false;
    private OnDeleteListener listener;

    public interface OnDeleteListener {
        void onDelete(MobileDeviceModel mobileDeviceModel);
    }

    public MobileDeviceListAdapter(Context context) {
        super(context, 0);
    }

    @Nullable
    @Override
    public View getView(final int position, @Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cell_mobile_device, parent, false);
        }

        Version1TextView deviceName = (Version1TextView) convertView.findViewById(R.id.device_name);
        Version1TextView deviceType = (Version1TextView) convertView.findViewById(R.id.device_type);
        ImageView deleteButton = (ImageView) convertView.findViewById(R.id.delete_button);
        View divider = convertView.findViewById(R.id.divider);

        MobileDeviceModel model = getItem(position);
        deviceName.setText(getDeviceName(model));
        deviceType.setText(getContext().getString(R.string.device_type, String.valueOf(model.getDeviceModel())));

        deleteButton.setVisibility(isEditEnabled() ? View.VISIBLE : View.GONE);
        divider.setVisibility(View.VISIBLE);

        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener != null) {
                    listener.onDelete(getItem(position));
                }
                MobileDeviceListAdapter.super.remove(getItem(position));
            }
        });

        return convertView;
    }


    /**
     * Attempts to parse the OS type and version from the strings presents in the MobileDeviceModel.
     * This is a bad idea and should be refactored. No assurances that these string formats won't
     * change in the future.
     *
     * @param deviceModel
     * @return
     */
    public String getDeviceName (MobileDeviceModel deviceModel) {
        if ("ios".equalsIgnoreCase(deviceModel.getOsType())) {

            // Assumes iOS version string looks like
            if (deviceModel.getOsVersion() != null && deviceModel.getOsVersion().split(" ").length == 4) {
                return getContext().getString(R.string.device_name, deviceModel.getOsType().toUpperCase(), deviceModel.getOsVersion().split(" ")[1]);
            } else {
                return deviceModel.getOsType().toUpperCase();
            }
        } else if ("android".equalsIgnoreCase(deviceModel.getOsType())) {
            if (deviceModel.getOsVersion() != null && deviceModel.getOsVersion().split(" ").length == 3) {
                return getContext().getString(R.string.device_name, deviceModel.getOsType().toUpperCase(), deviceModel.getOsVersion().split(" ")[2]);
            } else {
                return deviceModel.getOsType().toUpperCase();
            }
        }

        return getContext().getString(R.string.device_name_unknown);
    }

    public void setEditEnabled (boolean enabled) {
        this.editEnabled = enabled;
        notifyDataSetChanged();
    }

    public boolean isEditEnabled () {
        return editEnabled;
    }

    public void setOnDeleteListener (OnDeleteListener listener) {
        this.listener = listener;
    }

}
