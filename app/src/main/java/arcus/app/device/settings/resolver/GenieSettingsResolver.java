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
package arcus.app.device.settings.resolver;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.settings.builder.GenieSettingsBuilder;
import arcus.app.device.settings.builder.ParentChildSettingBuilder;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;

import java.util.ArrayList;
import java.util.List;


public class GenieSettingsResolver extends DeviceSettingsResolver {

    @Nullable
    @Override
    public SettingsList getSettings(@NonNull Activity context, SettingChangedParcelizedListener listener, Object model) {
        SettingsList settings = super.getSettings(context, listener, model);

        if (model instanceof DeviceModel) {

            DeviceModel deviceModel = (DeviceModel) model;

            settings.add(ParentChildSettingBuilder.with(context.getString(R.string.genie_settings), context.getString(R.string.genie_settings_configurations))
                    .addChildSetting(
                            GenieSettingsBuilder.with(context, context.getString(R.string.genie_wifi_network_title), context.getString(R.string.genie_wifi_network_des))
                                    .buildNetworkSetting(deviceModel)
                                    .build(), false
                    )
                    .build());


            ParentChildSettingBuilder builder = ParentChildSettingBuilder.with(context.getString(R.string.genie_devices), context.getString(R.string.genie_devices_configurations))
                    .dontPromoteOnlyChild();

            // Determine Devices
            List<DeviceModel> devices = new ArrayList<>();

            // Check Connected Devices Against All Devices
            List<DeviceModel> allDevices = SessionModelManager.instance().getDevices();

            if (allDevices != null) {

                devices = CorneaUtils.filterBridgeChildDeviceModelsByParentAddress(allDevices, "PROT:IPCD:" + ((DeviceAdvanced) deviceModel).getProtocolid());

                // Insert Buy More Text
//            if (devices.size() < 3) {
//                builder.addChildSetting(
//                        GenieSettingsBuilder.with(context, context.getString(R.string.genie_buy_more_description), null)
//                                .buildBuyMoreSetting()
//                                .build(), false);
//            }

                for (DeviceModel door : devices) {
                    builder.addChildSetting(
                            GenieSettingsBuilder.with(context, door.getName(), door.getModel())
                                    .buildDeviceSetting(door)
                                    .build(), false);
                }
            }

            settings.add(builder.build());
        }

        return settings;
    }
}
