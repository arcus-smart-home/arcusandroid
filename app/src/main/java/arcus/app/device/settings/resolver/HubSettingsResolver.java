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

import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;

/**
 * Encapsulates the business logic associated with determining which settings should be made
 * available to the given hub.
 */
public class HubSettingsResolver implements SettingsResolver {
    @Override
    public SettingsList getSettings(Activity context, SettingChangedParcelizedListener listener, Object model) {
//        SettingsList settings = new SettingsList();
//
//        if (model != null && model instanceof HubModel) {
//
//            String name = ((HubModel) model).getName();
//            String address = ((HubModel) model).getAddress();
//
//            // Device Information
//            Fragment deviceFragment = NameDeviceFragment.newInstance(NameDeviceFragment.ScreenVariant.SETTINGS, name, address);
//            settings.add(new TransitionToFragmentSetting(name, context.getString(R.string.device_more_product_name_instr), deviceFragment));
//
//            // Product Information
//            Fragment productFragment = ProductInfoFragment.newInstance(address);
//            settings.add(new TransitionToFragmentSetting(context.getString(R.string.device_more_product_information),
//                    context.getString(R.string.device_more_product_information_instr), productFragment));
//
//            // Connectivity Information
//            Fragment connectivityFragment = HubConnectivityAndPowerFragment.newInstance(address);
//            settings.add(new TransitionToFragmentSetting(context.getString(R.string.device_more_connectivity),
//                    context.getString(R.string.device_more_connectivity_instr), connectivityFragment));
//
//            // Firmware Information
//            Fragment firmwareFragment = HubFirmwareFragment.newInstance();
//            settings.add(new TransitionToFragmentSetting(context.getString(R.string.device_more_firmware),
//                    context.getString(R.string.device_more_firmware_instr), firmwareFragment));
//        }
//
//        return settings;

        // Return null for now.  If we are going to use settings from here,
        // the more fragment that this is loaded into needs updating.

        return null;
    }
}
