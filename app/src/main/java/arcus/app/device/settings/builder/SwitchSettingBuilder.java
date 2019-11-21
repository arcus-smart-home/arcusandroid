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

import android.content.res.Resources;
import androidx.annotation.NonNull;

import com.iris.client.capability.Switch;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.style.BinarySetting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SwitchSettingBuilder implements SettingBuilder {

    private static final Logger logger = LoggerFactory.getLogger(SwitchSettingBuilder.class);
    private final String title;
    private final String description;
    private BinarySetting setting;

    private SwitchSettingBuilder (String title, String description) {
        this.title = title;
        this.description = description;
    }

    @NonNull
    public static SwitchSettingBuilder with (String title, String description) {
        return new SwitchSettingBuilder(title, description);
    }

    @NonNull
    public SwitchSettingBuilder addSwitchInvertedSetting(@NonNull final Resources resource, @NonNull final DeviceModel model) {
        final Switch switchCap = CorneaUtils.getCapability(model, Switch.class);

        // Does device support the switch capability?
        if (switchCap != null && model.get(Switch.ATTR_INVERTED) != null) {

            logger.debug("Building switch polarity setting.");

            boolean initialDirection = switchCap.getInverted();
            setting = new BinarySetting(resource.getString(R.string.setting_reverse_switch), resource.getString(R.string.setting_reverse_switch_description), initialDirection);

            // Update the capability when the setting changes
            setting.addListener(new SettingChangedParcelizedListener() {
                @Override
                public void onSettingChanged(Setting setting, Object newValue) {
                    logger.debug("Changing switch inverted property to " + newValue);
                    switchCap.setInverted((Boolean) newValue);
                    model.commit();
                }
            });

        } else {
            logger.debug("This device does not support the switch polarity setting.");
        }

        return this;
    }

    public Setting build () {
        return setting;
    }

}
