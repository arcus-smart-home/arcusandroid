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
package arcus.app.device.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.HubConnection;
import com.iris.client.capability.Presence;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.model.DeviceType;

import java.util.ArrayList;
import java.util.List;


public class DeviceListAdapter extends BaseAdapter {
    public static final int TYPE_ITEM = 0;
    public static final int TYPE_FOOTER = 1;

    private Context context;
    private List<DeviceModel> items;
    private LayoutInflater inflater;
    private boolean useLightColorScheme = true;
    private boolean showZwaveUnpairing  = true;

    public DeviceListAdapter(Context context, List<DeviceModel> items) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        if(items != null){
            this.items = new ArrayList<>(items);
        } else{
            this.items = new ArrayList<>();
        }
    }

    public void setDevices(List<DeviceModel> devices) {
        this.items.clear();
        this.items.addAll(devices);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return items.size() + 1;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        return (position == items.size() ? TYPE_FOOTER : TYPE_ITEM);
    }

    @Override
    public DeviceModel getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setUseLightColorScheme (boolean useLightColorScheme) {
        this.useLightColorScheme = useLightColorScheme;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int type = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (type) {
                case TYPE_ITEM:
                    convertView = inflater.inflate(R.layout.device_list_item, null);

                    holder.deviceImage = (ImageView) convertView.findViewById(R.id.device_image);
                    holder.chevronImage = (ImageView) convertView.findViewById(R.id.imgChevron);
                    holder.name = (TextView) convertView.findViewById(R.id.list_item_name);
                    holder.device = (TextView) convertView.findViewById(R.id.list_item_description);
                    holder.redDot = (ImageView) convertView.findViewById(R.id.red_dot);
                    holder.cloudImage = (ImageView) convertView.findViewById(R.id.cloud_image);


                      break;
                case TYPE_FOOTER:
                    convertView = inflater.inflate(R.layout.cell_device_listing_zwave_tools, null);

                    if (showZwaveUnpairing) {
                        holder.name = (TextView) convertView.findViewById(R.id.footer_button);
                    }
                    else {
                        View settingsView = convertView.findViewById(R.id.settings_list_layout);
                        if (settingsView != null) {
                            settingsView.setVisibility(View.GONE);
                        }
                    }
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (type == TYPE_FOOTER) {
            // Nothing to do
        } else {

            DeviceModel deviceModel = getItem(position);
            String hint = deviceModel == null ? "" : deviceModel.getDevtypehint();

            if (DeviceType.fromHint(hint).isCloudConnected()) {
                holder.cloudImage.setVisibility(View.VISIBLE);
                showCloud(holder.cloudImage, deviceModel);
                showHideRedDot(holder, deviceModel);
            }
            else {
                holder.cloudImage.setVisibility(View.GONE);
                showHideRedDot(holder, deviceModel);
            }

            holder.name.setTextColor(useLightColorScheme ? Color.WHITE : Color.BLACK);
            holder.device.setTextColor(useLightColorScheme ? Color.WHITE : Color.BLACK);
            holder.deviceImage.setImageResource(R.drawable.device_list_placeholder);

            Invert invertForStockImages = useLightColorScheme ? Invert.BLACK_TO_WHITE : Invert.NONE;

            ImageManager.with(context)
                    .putSmallDeviceImage(deviceModel)
                    .withTransformForStockImages(new BlackWhiteInvertTransformation(invertForStockImages))
                    .withTransformForUgcImages(new CropCircleTransformation())
                    .into(holder.deviceImage)
                    .execute();

            holder.chevronImage.setImageResource(useLightColorScheme ? R.drawable.device_list_chevron : R.drawable.chevron);
            holder.name.setText(deviceModel.getName());
            holder.device.setVisibility(View.GONE);
        }

        return convertView;
    }

    private void showCloud(ImageView view, DeviceModel model) {
        if (CorneaUtils.isHoneywellOffline(model) || DeviceConnection.STATE_OFFLINE.equals(model.get(DeviceConnection.ATTR_STATE))) {
            view.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.cloud_small_pink));
        }
        else {
            view.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.cloud_small_white));
        }
    }


    private void showHideRedDot(ViewHolder holder, DeviceModel model) {
        if (model != null){
            boolean deviceOffline;
            
            if (CorneaUtils.hasCapability(model, HubConnection.class)){
                deviceOffline = HubConnection.STATE_OFFLINE.equals(model.get(HubConnection.ATTR_STATE));
            } else {
                deviceOffline = DeviceConnection.STATE_OFFLINE.equals(model.get(DeviceConnection.ATTR_STATE));
            }

            if (deviceOffline && !CorneaUtils.hasCapability(model, Presence.class)){
                holder.redDot.setVisibility(View.VISIBLE);
            } else {
                holder.redDot.setVisibility(View.GONE);
            }
        }
    }

    public static class ViewHolder {
        public TextView name;
        public TextView device;
        public ImageView deviceImage;
        public ImageView chevronImage;
        public ImageView redDot;
        public ImageView cloudImage;
    }

}
