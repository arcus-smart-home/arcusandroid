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

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.device.settings.builder.CameraSettingBuilder;
import arcus.app.device.settings.builder.ParentChildSettingBuilder;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;

import java.util.Collection;


public class CameraSettingsResolver extends DeviceSettingsResolver implements SettingsResolver {

    @Nullable
    @Override
    public SettingsList getSettings(@NonNull Activity context, SettingChangedParcelizedListener listener, Object model) {
        SettingsList settings = super.getSettings(context, listener, model);

        if (model instanceof DeviceModel) {

            DeviceModel deviceModel = (DeviceModel) model;
            ParentChildSettingBuilder builder = ParentChildSettingBuilder.with(
                    context.getString(R.string.setting_camera_settings),
                    context.getString(R.string.setting_camera_configurations)
            ).dontPromoteOnlyChild();
            Collection<String> caps = ((DeviceModel) model).getCaps();
            if (caps != null) {
                builder
                        .addChildSetting(
                                CameraSettingBuilder.with(context, context.getString(R.string.settings_wifi_network_title), context.getString(R.string.settings_wifi_network_desc))
                                        .buildBleNetworkSetting(deviceModel)
                                        .build(), false
                        )
                        .addChildSetting(
                                CameraSettingBuilder
                                        .with(context, context.getString(R.string.setting_live_stream_image_resolution_title), context.getString(R.string.setting_live_stream_image_resolution_desc))
                                        .buildCameraResolutionSetting(deviceModel)
                                        .build(), false
                        )
                        .addChildSetting(
                                CameraSettingBuilder
                                        .with(context, context.getString(R.string.setting_motion_sensitivity_title), context.getString(R.string.setting_motion_sensitivity_desc))
                                        .buildMotionSensitivitySetting(deviceModel)
                                        .build(), false
                        )
                        .addChildSetting(
                                CameraSettingBuilder.with(context, context.getString(R.string.setting_camera_rotate_title), context.getString(R.string.setting_camera_rotate_desc))
                                        .buildRotateCameraSetting(deviceModel)
                                        .build(), false
                        );
            } else {
                builder
                        .addChildSetting(
                                CameraSettingBuilder.with(context, context.getString(R.string.settings_wifi_network_title), context.getString(R.string.settings_wifi_network_desc))
                                        .buildNetworkSetting(deviceModel)
                                        .build(), false
                        )
                        .addChildSetting(
                                CameraSettingBuilder.with(context, context.getString(R.string.setting_image_resolution_title), context.getString(R.string.setting_image_resolution_desc))
                                        .buildCameraResolutionSetting(deviceModel)
                                        .build(), false
                        )
                        .addChildSetting(
                                CameraSettingBuilder.with(context, context.getString(R.string.setting_frame_rate_title), context.getString(R.string.setting_frame_rate_desc))
                                        .buildFrameRateSetting(deviceModel)
                                        .build(), false
                        )
                        .addChildSetting(
                                CameraSettingBuilder.with(context, context.getString(R.string.setting_camera_led_enable_title), context.getString(R.string.setting_camera_led_enable_desc))
                                        .buildLedSetting(deviceModel)
                                        .build(), false
                        )
                        .addChildSetting(
                                CameraSettingBuilder.with(context, context.getString(R.string.setting_camera_rotate_title), context.getString(R.string.setting_camera_rotate_desc))
                                        .buildRotateCameraSetting(deviceModel)
                                        .build(), false
                        )
                        .addChildSetting(
                                CameraSettingBuilder.with(context, context.getString(R.string.camera_local_stream_title), context.getString(R.string.camera_local_stream_desc))
                                        .buildLocalUsernamePassword(deviceModel)
                                        .build(),
                                false
                        );
            }

            // Insert second to last
            settings.add(builder.build());
        }

        return settings;
    }
}
