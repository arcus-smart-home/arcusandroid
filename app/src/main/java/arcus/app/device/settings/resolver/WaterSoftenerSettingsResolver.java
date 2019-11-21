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

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.utils.CorneaUtils;

import arcus.app.device.settings.builder.EcoWaterSettingBuilder;
import arcus.app.device.settings.builder.ParentChildSettingBuilder;
import arcus.app.device.settings.builder.WaterSoftenerSettingBuilder;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.fragment.BuySaltFragment;
import arcus.app.device.settings.fragment.RechargeNowFragment;
import arcus.app.device.settings.fragment.RechargeTimeFragment;
import arcus.app.device.settings.fragment.WaterHardnessLevelFragment;

import java.util.Collections;

public class WaterSoftenerSettingsResolver implements SettingsResolver {

    @Nullable @Override public SettingsList getSettings(
            Activity context,
            SettingChangedParcelizedListener listener,
            Object model
    ) {
        if (context == null || model == null || !(model instanceof DeviceModel)) {
            return new SettingsList(Collections.<Setting>emptyList());
        }

        SettingsList settingsList = new SettingsList();

        DeviceModel device = (DeviceModel) model;
        String deviceId = CorneaUtils.getIdFromAddress(device.getAddress());
        Setting rechargeNow = WaterSoftenerSettingBuilder.settingsFor()
            .withTitle(context.getString(R.string.water_softener_recharge_now_title).toUpperCase())
            .withDescription(context.getString(R.string.water_softener_recharge_now_des))
            .clickOpensFragment(RechargeNowFragment.newInstance(deviceId))
            .build();
        settingsList.add(rechargeNow);

        Setting rechargeTime = WaterSoftenerSettingBuilder.settingsFor()
            .withTitle(context.getString(R.string.water_softener_recharge_time_title).toUpperCase())
            .withDescription(context.getString(R.string.water_softener_recharge_time_des))
            .clickOpensFragment(RechargeTimeFragment.newInstance(deviceId))
            .build();
        settingsList.add(rechargeTime);

        Setting waterHardness = WaterSoftenerSettingBuilder.settingsFor()
            .withTitle(context.getString(R.string.water_softener_water_hardness_level_title).toUpperCase())
            .withDescription(context.getString(R.string.water_softener_water_hardness_level_des))
            .clickOpensFragment(WaterHardnessLevelFragment.newInstance(deviceId))
            .build();
        settingsList.add(waterHardness);

        Setting saltType = WaterSoftenerSettingBuilder.settingsFor()
            .withTitle(context.getString(R.string.water_softener_salt_type_title).toUpperCase())
            .withDescription(context.getString(R.string.water_softener_salt_type_des))
            .clickOpensFragment(BuySaltFragment.newInstance())
            .build();
        settingsList.add(saltType);

        Setting waterFlow = ParentChildSettingBuilder.with(context.getString(R.string.water_softener_water_flow_title), context.getString(R.string.water_softener_water_flow_des))
                .dontPromoteOnlyChild()
                .addChildSetting(
                        EcoWaterSettingBuilder.with(context, context.getString(R.string.water_softener_water_flow_title), context.getString(R.string.water_softener_ecowater_settings_desc))
                                .buildWaterFlowSettingsDescription((DeviceModel)model)
                                .build(), false
                )
                .addChildSetting(
                        EcoWaterSettingBuilder.with(context, context.getString(R.string.water_softener_continuous_flow_title), context.getString(R.string.water_softener_continuous_flow_des))
                                .buildContinuousWaterFlowSetting((DeviceModel)model)
                                .build(), false
                )
                .addChildSetting(
                        EcoWaterSettingBuilder.with(context, context.getString(R.string.water_softener_excessive_flow_title), context.getString(R.string.water_softener_excessive_flow_des))
                                .buildExcessiveWaterFlowSetting((DeviceModel)model)
                                .build(), false
                )
                .build();
        if(waterFlow != null) {
            settingsList.add(waterFlow);
        }

        return settingsList;
    }
}
