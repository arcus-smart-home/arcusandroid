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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.CropCircleTransformation;

import java.util.ArrayList;
import java.util.List;


public class CheckedDeviceListAdapter extends BaseAdapter {

    private class ViewHolder {
        ImageView deviceImage;
        ImageView checkImage;
        ProgressBar progress;
        TextView name;
        TextView device;
    }

    private class DeviceProgress {
        private DeviceModel mDevice;
        private Double mProgress;
        private Boolean mComplete;

        protected DeviceProgress(DeviceModel model, Double progress) {
            mDevice = model;
            mProgress = progress;
            mComplete = false;
        }

        public DeviceModel getDeviceModel() {
            return mDevice;
        }

        public Double getProgress() {
            return mProgress;
        }

        public void setProgress(Double progress) {
            mProgress = progress < .99 ? progress * 100.0 : progress;
        }

        public Boolean isComplete() {
            return mComplete;
        }

        public void setComplete(Boolean complete) {
            mComplete = complete;
        }
    }

    private final Context mContext;

    @NonNull
    private List<DeviceProgress> mData = new ArrayList<>();

    public CheckedDeviceListAdapter(Context context) {
        this.mContext = context;
    }

    public CheckedDeviceListAdapter(Context context, @NonNull List<DeviceModel> devicesPaired) {
        this.mContext = context;

        for (DeviceModel model : devicesPaired)
            mData.add(new DeviceProgress(model, 0.0));
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public DeviceProgress getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {

        ViewHolder viewHolder;

        DeviceProgress deviceData = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.cell_download_device_list_item, parent, false);

            viewHolder = new ViewHolder();
            viewHolder.deviceImage = (ImageView) convertView.findViewById(R.id.device_image);
            viewHolder.checkImage = (ImageView) convertView.findViewById(R.id.checkmark);
            viewHolder.progress = (ProgressBar) convertView.findViewById(R.id.progress);
            viewHolder.name = (TextView) convertView.findViewById(R.id.list_item_name);
            viewHolder.device = (TextView) convertView.findViewById(R.id.list_item_description);

            viewHolder.deviceImage.setImageResource(R.drawable.device_list_placeholder);
            ImageManager.with(mContext)
                    .putSmallDeviceImage(deviceData.getDeviceModel())
                    .withTransform(new CropCircleTransformation())
                    .fit().centerCrop()
                    .into(viewHolder.deviceImage)
                    .execute();

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (deviceData != null) {
            viewHolder.checkImage.setImageResource(R.drawable.icon_checkmark);
            viewHolder.checkImage.setVisibility(deviceData.isComplete() ? View.VISIBLE : View.GONE);

            viewHolder.name.setText(deviceData.getDeviceModel().getName());
            viewHolder.device.setText(deviceData.getDeviceModel().getVendor());

            viewHolder.progress.setProgress(deviceData.getProgress().intValue());
        }

        return (convertView);
    }

    public void add(DeviceModel model) {
        mData.add(new DeviceProgress(model, 0.0));
    }

    public void markAsComplete(DeviceModel model) {
        for (DeviceProgress item : mData) {
            if (item.getDeviceModel().getAddress().equals(model.getAddress())) {
                item.setComplete(true);
            }
        }
        notifyDataSetChanged();
    }

    public void updateProgress(DeviceModel model, Double progress) {
        for (DeviceProgress item : mData) {
            if (item.getDeviceModel().getAddress().equals(model.getAddress())) {
                item.setProgress(progress);
                break;
            }
        }
        notifyDataSetChanged();
    }

    public boolean deviceExist(@NonNull DeviceModel model) {
        for (DeviceProgress item : mData) {
            if (item.getDeviceModel().getAddress().equals(model.getAddress())) {
                return true;
            }
        }

        return false;
    }

    public boolean isListComplete() {
        for (DeviceProgress progress : mData) {
            if (!progress.isComplete()) {
                return false;
            }
        }

        return true;
    }
}
