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
import androidx.annotation.Nullable;

import com.iris.client.capability.WiFi;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.settings.builder.WifiSwitchSettingsBuilder;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;

import org.apache.commons.lang3.StringUtils;


public class WifiSwitchSettingsResolver extends DeviceSettingsResolver implements SettingsResolver {

    @Nullable
    @Override
    public SettingsList getSettings(Activity context, SettingChangedParcelizedListener listener, Object model) {
        SettingsList list = super.getSettings(context, listener, model);

        if (model != null && model instanceof DeviceModel) {
            DeviceModel deviceModel = (DeviceModel) model;
            WiFi wifi = CorneaUtils.getCapability(deviceModel, WiFi.class);

            if (wifi != null && !StringUtils.isEmpty(wifi.getSsid())) {
                list.add(WifiSwitchSettingsBuilder
                        .with(context.getString(R.string.swann_wifi_settings_title), context.getString(R.string.swann_wifi_settings_desc), wifi.getSsid(), deviceModel.getAddress())
                        .build());
            }
        }

        return list;
    }
}
