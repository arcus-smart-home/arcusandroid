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

import com.iris.client.capability.WeatherRadio;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.device.more.WeatherAlertCategorySelection;
import arcus.app.device.settings.fragment.HaloLocationFragment;
import arcus.app.device.settings.fragment.HaloStationSelectionFragment;
import arcus.app.device.settings.builder.HaloSettingsBuilder;
import arcus.app.device.settings.builder.ParentChildSettingBuilder;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.fragment.HaloTestDeviceFragment;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class HaloSettingsResolver implements SettingsResolver {
    @Nullable @Override public SettingsList getSettings(
          Activity context,
          SettingChangedParcelizedListener listener,
          Object model
    ) {
        if (context == null || model == null || !(model instanceof DeviceModel)) {
            return new SettingsList(Collections.<Setting>emptyList());
        }

        DeviceModel device = (DeviceModel) model;
        Setting testSetting = HaloSettingsBuilder.settingsFor()
              .withTitle(context.getString(R.string.testing_text).toUpperCase())
              .withDescription(context.getString(R.string.test_your_placeholder, "Halo"))
              .clickOpensFragment(HaloTestDeviceFragment.newInstance(device.getAddress()))
              .build();

        Collection<String> caps = device.getCaps();
        if (caps == null || !caps.contains(WeatherRadio.NAMESPACE)) {
            return new SettingsList(Collections.singletonList(testSetting));
        }

        Setting weatherAlerts = HaloSettingsBuilder.settingsFor()
              .withTitle(context.getString(R.string.weather_alerts_text).toUpperCase())
              .withDescription(context.getString(R.string.weather_alerts_setting_description))
               .clickOpensFragment(WeatherAlertCategorySelection.newInstance(device.getAddress()))
              .build();

        Setting weatherRadio = ParentChildSettingBuilder.with(context.getString(R.string.weather_radio_setting).toUpperCase(), context.getString(R.string.weather_radio_setting_description))
                .dontPromoteOnlyChild()
                .withTitle(context.getString(R.string.weather_radio_setting).toUpperCase())
                .addChildSetting(
                        HaloSettingsBuilder.settingsFor()
                        .withTitle(context.getString(R.string.location_information_setting).toUpperCase())
                        .withDescription(context.getString(R.string.location_information_setting_description))
                        .clickOpensFragment(HaloLocationFragment.newInstance(device.getAddress(), true))
                        .build(), false

                )
                .addChildSetting(
                        HaloSettingsBuilder.settingsFor()
                                .withTitle(context.getString(R.string.weather_radio_setting).toUpperCase())
                                .withDescription(context.getString(R.string.weather_radio_setting_child_description))
                                .clickOpensFragment(HaloStationSelectionFragment.newInstance(device.getAddress(), true))
                                .build(), false

                )
                .build();

        return new SettingsList(Arrays.asList(testSetting, weatherAlerts, weatherRadio));
    }
}
