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

import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;

/**
 * Encapsulates the business logic associated with determining which settings should be made
 * available for a standard device including favorites, product info and status.
 */
public class DeviceSettingsResolver implements SettingsResolver {

    // Used for resolving standard device more settings
    @NonNull
    @Override
    public SettingsList getSettings(Activity context, SettingChangedParcelizedListener listener, Object model) {

        SettingsList settings = new SettingsList();

//        // Favorites Setting
//        BinarySetting favoriteSetting = new BinarySetting(context.getString(R.string.favorites_title),
//                context.getString(R.string.device_more_favorite_instr), false);
//        favoriteSetting.addListener(listener);
//        settings.add(favoriteSetting);
//
//        if (model != null && model instanceof DeviceModel) {
//
//            String name = ((DeviceModel) model).getName();
//            String deviceAddress = ((DeviceModel) model).getId();
//
//            // Device Information
//            Fragment deviceFragment = NameDeviceFragment.newInstance(NameDeviceFragment.ScreenVariant.SETTINGS, name, deviceAddress);
//            settings.add(new TransitionToFragmentSetting(name, context.getString(R.string.device_more_product_name_instr), deviceFragment));
//
//            // Product Information
//            Fragment productFragment = ProductInfoFragment.newInstance(deviceAddress);
//            settings.add(new TransitionToFragmentSetting(context.getString(R.string.device_more_product_information),
//                    context.getString(R.string.device_more_product_information_instr), productFragment));
//
//            // Connectivity Information
//            // HUB ONLY
//
//            // Firmware Information
//            // HUB ONLY
//        }

        return settings;
    }
}
