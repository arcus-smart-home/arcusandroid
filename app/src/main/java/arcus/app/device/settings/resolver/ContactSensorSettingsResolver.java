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

import com.iris.client.capability.Contact;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.settings.builder.ContactSensorSettingBuilder;
import arcus.app.device.settings.builder.ParentChildSettingBuilder;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Encapsulates the business logic associated with determining which settings should be made
 * available to the given contact sensor device.
 */

public class ContactSensorSettingsResolver extends DeviceSettingsResolver implements SettingsResolver {

    private final static Logger logger = LoggerFactory.getLogger(ContactSensorSettingsResolver.class);

    @Override
    public SettingsList getSettings(@NonNull Activity activity, SettingChangedParcelizedListener listener, Object model) {
        SettingsList settings = super.getSettings(activity, listener, model);

        String assignedSensor;

        logger.trace("Resolving settings for contact sensor.");
        DeviceModel deviceModel = (DeviceModel) model;

        Contact contactSensor = CorneaUtils.getCapability(deviceModel, Contact.class);
        assignedSensor = contactSensor.getUsehint();

        String title = activity.getString(R.string.setting);
        String description = activity.getString(R.string.setting_sensor_desc);

        // Insert second to last
        settings.add(ParentChildSettingBuilder.with(title, description, assignedSensor)
                .addChildSetting(
                        ContactSensorSettingBuilder.with(activity, activity.getString(R.string.setting), activity.getString(R.string.setting_sensor_desc))
                        .addContactSensorSetting(deviceModel)
                        .build(), true)
                .build());

        return settings;
    }
}
