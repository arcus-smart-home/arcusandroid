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
import android.support.annotation.Nullable;

import com.iris.client.capability.PetDoor;
import com.iris.client.model.DeviceModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.pairing.specialty.petdoor.PetDoorKeyListFragment;
import arcus.app.device.settings.style.TransitionToFragmentSetting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class PetDoorSettingsResolver extends DeviceSettingsResolver implements SettingsResolver {

    private final static Logger logger = LoggerFactory.getLogger(PetDoorSettingsResolver.class);

    @Nullable
    @Override
    public SettingsList getSettings(Activity context, SettingChangedParcelizedListener listener, Object model) {
        SettingsList settings = super.getSettings(context, listener, model);

        logger.trace("Resolving settings for pet door.");
        DeviceModel deviceModel = (DeviceModel) model;

        if (CorneaUtils.hasCapability(deviceModel, PetDoor.class)) {
            String title = ArcusApplication.getContext().getString(R.string.petdoor_smart_keys);
            String description = ArcusApplication.getContext().getString(R.string.petdoor_smart_keys_desc);

            settings.add(new TransitionToFragmentSetting(title, description, PetDoorKeyListFragment.newInstance(((DeviceModel) model).getAddress())));
        }

        return settings;
    }
}
