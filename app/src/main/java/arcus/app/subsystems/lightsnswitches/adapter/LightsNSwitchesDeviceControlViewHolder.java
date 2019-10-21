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
package arcus.app.subsystems.lightsnswitches.adapter;

import android.view.View;
import android.widget.ImageView;

import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder;
import arcus.cornea.device.lightsnswitches.LightSwitchProxyModel;
import arcus.app.R;
import arcus.app.common.utils.DeviceSeekArc;
import arcus.app.common.view.GlowableImageView;
import arcus.app.common.view.Version1TextView;


public class LightsNSwitchesDeviceControlViewHolder extends AbstractDraggableItemViewHolder {

    public Version1TextView leftButton;
    public Version1TextView rightButton;
    public GlowableImageView deviceImage;
    public DeviceSeekArc percentageArc;
    public Version1TextView deviceName;
    public View chevronClickRegion;
    public ImageView dragHandle;
    public ImageView chevron;
    public ImageView colorTempSettings;

    public LightSwitchProxyModel model;

    public View onlineContainer;
    public View offlineContainer;
    public View bannerServiceCardErrorContainer;
    public View cloudIcon;

    public ImageView offlineDeviceImage;
    public Version1TextView offlineDeviceName;
    public Version1TextView bannerDescription;
    public Version1TextView fullBannerDescription;

    public LightsNSwitchesDeviceControlViewHolder(View itemView) {
        super(itemView);

        leftButton = (Version1TextView) itemView.findViewById(R.id.left_button);
        rightButton = (Version1TextView) itemView.findViewById(R.id.right_button);
        deviceImage = (GlowableImageView) itemView.findViewById(R.id.device_image);
        percentageArc = (DeviceSeekArc) itemView.findViewById(R.id.percent_arc);
        colorTempSettings = (ImageView) itemView.findViewById(R.id.color_temp_settings);
        deviceName = (Version1TextView) itemView.findViewById(R.id.device_name);
        chevronClickRegion = itemView.findViewById(R.id.chevron_click_region);
        dragHandle = (ImageView) itemView.findViewById(R.id.drag_handle);
        chevron = (ImageView) itemView.findViewById(R.id.chevron);
        cloudIcon = (ImageView) itemView.findViewById(R.id.cloud_icon);

        onlineContainer = itemView.findViewById(R.id.online_container);
        offlineContainer = itemView.findViewById(R.id.offline_container);
        offlineDeviceName = (Version1TextView) itemView.findViewById(R.id.device_name_offline);
        offlineDeviceImage = (ImageView) itemView.findViewById(R.id.offline_device_image);

        bannerServiceCardErrorContainer = itemView.findViewById(R.id.banner_container);
        bannerDescription = (Version1TextView) itemView.findViewById(R.id.service_card_error_description);
        fullBannerDescription = (Version1TextView) itemView.findViewById(R.id.device_error_description);
    }
}
