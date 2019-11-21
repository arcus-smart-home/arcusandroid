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
import android.view.View;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.device.buttons.controller.ButtonActionSequenceController;
import arcus.app.device.buttons.model.ButtonDevice;
import arcus.app.device.buttons.model.ButtonSequenceVariant;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.style.OnClickActionSetting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ButtonDeviceSettingsResolver extends DeviceSettingsResolver implements SettingsResolver {

    private final static Logger logger = LoggerFactory.getLogger(FanSettingsResolver.class);

    @Nullable
    @Override
    public SettingsList getSettings(@NonNull final Activity context, SettingChangedParcelizedListener listener, Object model) {
        SettingsList settings = super.getSettings(context, listener, model);

        logger.debug("Resolving settings for button device.");

        if (model instanceof DeviceModel) {

            final DeviceModel deviceModel = (DeviceModel) model;

            if (ButtonDevice.isButtonDevice(deviceModel.getProductId())) {

                String title = context.getString(R.string.setting);
                String description = context.getString(R.string.setting_edit_button_controls);

                // Insert second to last
                settings.add(new OnClickActionSetting(title, description, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new ButtonActionSequenceController(context, ButtonSequenceVariant.SETTINGS, deviceModel.getAddress()).startSequence(context, null);
                    }
                }));
            }
        }

        return settings;
    }
}
