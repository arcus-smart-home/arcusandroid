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
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.device.camera.model.AvailableNetworkModel;
import arcus.cornea.device.camera.model.WiFiSecurityType;
import arcus.app.R;
import arcus.app.common.utils.ImageUtils;

import java.util.List;

import pl.droidsonroids.gif.GifImageView;


public class WiFiNetworkListItemAdapter extends BaseAdapter {
    private static final int TYPE_ITEM = 0;
    private static final int TYPE_LOADING = 1;

    private Context context;
    private List<AvailableNetworkModel> items;
    private boolean isLoading = false;

    private LayoutInflater inflater;

    public WiFiNetworkListItemAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    public WiFiNetworkListItemAdapter(Context context, List<AvailableNetworkModel> items, boolean isLoading) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.items = items;
        this.isLoading = isLoading;
    }

    public void setLoading(boolean isLoading) {
        this.isLoading = isLoading;
    }

    public void setNetworks(List<AvailableNetworkModel> networks) {
        this.items.clear();
        this.items.addAll(networks);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return isLoading ? 1 + items.size() : items.size();
    }

    @Override
    public int getViewTypeCount() {
        return isLoading ? 2 : 1;
    }

    @Override
    public int getItemViewType(int position) {
        return isLoading ? (position == 0 ? TYPE_LOADING : TYPE_ITEM) : TYPE_ITEM;
    }

    @Override
    public AvailableNetworkModel getItem(int position) {
        if (isLoading) return null;
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        int type = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (type) {
                case TYPE_ITEM:
                    convertView = inflater.inflate(R.layout.list_item_network_device, null);
                    holder.titleView = (TextView) convertView.findViewById(R.id.tv_network_name);
                    holder.securityView = (ImageView) convertView.findViewById(R.id.iv_network_availability);
                    holder.signalView = (ImageView) convertView.findViewById(R.id.iv_signal_strength);
                    break;
                case TYPE_LOADING:
                    convertView = inflater.inflate(R.layout.list_item_loading, null);
                    holder.loadingView = (GifImageView) convertView.findViewById(R.id.loading_image);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        if (isLoading && position == 0) {
            holder.loadingView = new GifImageView(context);
            holder.loadingView.setImageResource(R.drawable.preloader_black);
            holder.loadingView.setMaxWidth(ImageUtils.dpToPx(context, 50));
            holder.loadingView.setMaxHeight(ImageUtils.dpToPx(context, 50));
            holder.loadingView.setAdjustViewBounds(true);
        } else {

            AvailableNetworkModel data = items.get(isLoading ? 0 : position);

            String ssid = data.getSSID();
            WiFiSecurityType security = data.getSecurity();
            Integer signalStrength = data.getSignal();

            if (data.isCustom()) {
                holder.titleView.setText("Custom");
                holder.securityView.setVisibility(View.INVISIBLE);
                holder.signalView.setVisibility(View.INVISIBLE);
            } else {
                holder.titleView.setText(ssid);
                holder.securityView.setVisibility(View.VISIBLE);
                holder.signalView.setVisibility(View.VISIBLE);

        /* set up image for network state */
                if (security == null || security.compareTo(WiFiSecurityType.NONE) == 0) {
                    holder.securityView.setVisibility(View.INVISIBLE);
                } else {
                    holder.securityView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.lock));
                }

        /* set up image for signal strength */
                if (signalStrength == null) return convertView;

                if ((signalStrength / 33) == 0) {
                    holder.signalView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.wifi_33));
                } else if ((signalStrength / 33) == 1) {
                    holder.signalView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.wifi_66));
                } else if ((signalStrength / 33) >= 2) {
                    holder.signalView.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.wifi_100));
                }
            }
        }

        return convertView;
    }

    public static class ViewHolder {
        public TextView titleView;
        public GifImageView loadingView;
        public ImageView securityView;
        public ImageView signalView;
    }

}
