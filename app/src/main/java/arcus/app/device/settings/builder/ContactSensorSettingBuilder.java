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
package arcus.app.device.settings.builder;

import android.app.Activity;
import android.content.res.Resources;
import androidx.annotation.NonNull;

import com.iris.client.capability.Contact;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.enumeration.SensorAssignedType;
import arcus.app.device.settings.style.EnumSelectionSetting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ContactSensorSettingBuilder implements SettingBuilder {

    private static final Logger logger = LoggerFactory.getLogger(ContactSensorSettingBuilder.class);

    private final Activity activity;
    private final String title;
    private final String description;

    private String selectionAbstract;
    private EnumSelectionSetting<SensorAssignedType> setting;

    private ContactSensorSettingBuilder (Activity activity, String title, String description) {
        this.activity = activity;
        this.title = title;
        this.description = description;
    }

    @NonNull
    public static ContactSensorSettingBuilder with (Activity activity, String title, String description) {
        return new ContactSensorSettingBuilder(activity, title, description);
    }

    @NonNull
    public ContactSensorSettingBuilder addContactSensorSetting(@NonNull final DeviceModel model) {

        final Contact contactSensor = CorneaUtils.getCapability(model, Contact.class);
        final Resources resources = activity.getResources();

        //  does device support the Contact capability?
        if (contactSensor != null && model.getDevtypehint().equalsIgnoreCase("Contact")) {

            selectionAbstract = contactSensor.getUsehint();
            if (selectionAbstract == null) {
                selectionAbstract = Contact.USEHINT_OTHER;
            }

            logger.debug("Building Contact Setting.");
            SensorAssignedType sensorAssignedType = SensorAssignedType.get(contactSensor.getUsehint());
            setting = new EnumSelectionSetting<>(activity, resources.getString(R.string.setting), resources.getString(R.string.setting_sensor_desc), selectionAbstract, SensorAssignedType.class, sensorAssignedType);

            setting.addListener(new SettingChangedParcelizedListener() {

                @Override
                public void onSettingChanged(Setting setting, @NonNull Object newValue) {
                    logger.debug("Changing contact sensor assigned to: " + newValue);
                    selectionAbstract = newValue.toString();
                    setting.setSelectionAbstract(selectionAbstract);

                    model.set(Contact.ATTR_USEHINT, newValue);
                    model.commit();
                }
            });

        } else {
            logger.debug("This device does not support the contact sensor capability");
        }

        return this;
    }

    @Override
    public Setting build() {
        return setting;
    }
}
