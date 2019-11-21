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

import com.iris.client.capability.WiFi;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.fragment.NetworkSettingsFragment;
import arcus.app.device.settings.style.TransitionToFragmentSetting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 *  Encapsulates the business logic associated with determining which settings should be made
 * available to the given switch device.
 */
public class SwitchSettingsResolver extends DeviceSettingsResolver implements SettingsResolver {

    private final static Logger logger = LoggerFactory.getLogger(FanSettingsResolver.class);

    @Override
    public SettingsList getSettings(@NonNull Activity context, SettingChangedParcelizedListener listener, Object model) {
        SettingsList settings = super.getSettings(context, listener, model);

        if (!(model instanceof DeviceModel)) {
            return settings;
        }

        logger.debug("Resolving settings for switch.");
        DeviceModel deviceModel = (DeviceModel) model;

// Switch settings were requested to be disabled pending further UX discussions... Only turning on the WiFi setting for now.
//        String title = context.getString(R.string.setting);
//        String description = context.getString(R.string.setting_switch_description);
//
//        settings.add(ParentChildSettingBuilder.with(title, description)
//                .addChildSetting(
//                        SwitchSettingBuilder.with(context.getString(R.string.setting_reverse_switch), context.getString(R.string.setting_reverse_switch_description))
//                                .addSwitchInvertedSetting(context.getResources(), deviceModel)
//                                .build(), false
//                )
//                .addChildSetting(
//                        IndicatorSettingBuilder.with(context, context.getString(R.string.setting_led_status), context.getString(R.string.setting_led_status_description))
//                                .addInvertedLedStateSetting(deviceModel)
//                                .build(), false
//                )
//                .build());

        Collection<String> caps = deviceModel.getCaps();
        if (caps != null && caps.contains(WiFi.NAMESPACE)) {
            settings.add(
                    new TransitionToFragmentSetting(
                            context.getString(R.string.settings_wifi_network_title),
                            context.getString(R.string.settings_wifi_network_desc),
                            NetworkSettingsFragment.newInstance(deviceModel.getAddress()))
            );
        }

        return settings;
    }
}
