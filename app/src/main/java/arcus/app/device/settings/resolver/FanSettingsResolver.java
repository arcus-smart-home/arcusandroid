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

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.device.settings.builder.IndicatorSettingBuilder;
import arcus.app.device.settings.builder.ParentChildSettingBuilder;
import arcus.app.device.settings.builder.SwitchSettingBuilder;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates the business logic associated with determining which settings should be made
 * available to the given fan device.
 */
public class FanSettingsResolver extends DeviceSettingsResolver implements SettingsResolver {

    private final static Logger logger = LoggerFactory.getLogger(FanSettingsResolver.class);

    @Override
    public SettingsList getSettings(@NonNull Activity context, SettingChangedParcelizedListener listener, Object model) {
        SettingsList settings = super.getSettings(context, listener, model);

        logger.trace("Resolving settings for fan.");
        DeviceModel deviceModel = (DeviceModel) model;

        String title = context.getString(R.string.setting);
        String description = context.getString(R.string.setting_fan_description);

        // Insert second to last
        settings.add(ParentChildSettingBuilder.with(title, description)
                .dontPromoteOnlyChild()
                .addChildSetting(
                        IndicatorSettingBuilder.with(context, context.getString(R.string.setting_led_status), context.getString(R.string.setting_led_status_description))
                                .addInvertedLedStateSetting(deviceModel)
                                .build(), false
                )
                .addChildSetting(
                        SwitchSettingBuilder.with(context.getString(R.string.setting_reverse_switch), context.getString(R.string.setting_reverse_switch_description))
                                .addSwitchInvertedSetting(context.getResources(), deviceModel)
                                .build(), false
                )
                .build());

        return settings;
    }
}
