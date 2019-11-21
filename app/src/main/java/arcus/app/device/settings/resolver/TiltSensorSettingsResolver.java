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
import android.view.View;

import com.google.common.collect.ImmutableSet;
import com.iris.client.capability.Tilt;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.ButtonListPopup;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.style.OnClickActionSetting;

import java.util.HashMap;
import java.util.Map;


public class TiltSensorSettingsResolver extends DeviceSettingsResolver {

    private final static String HORIZONTAL = "HORIZONTAL";
    private final static String VERTICAL = "VERTICAL";

    @Nullable
    @Override
    public SettingsList getSettings(final Activity context, final SettingChangedParcelizedListener listener, Object model) {
        SettingsList settings = super.getSettings(context, listener, model);

        final DeviceModel deviceModel = (DeviceModel) model;

        if (CorneaUtils.hasCapability(deviceModel, Tilt.class)) {

            final Map<String, String> orientationChoices = new HashMap<>();
            orientationChoices.put(context.getString(R.string.tilt_sensor_extra_Horizontal_text), HORIZONTAL);
            orientationChoices.put(context.getString(R.string.tilt_sensor_extra_vertical_text), VERTICAL);

            // This only seems backward... the tag represents the closed state, the UI represents the open state.
            boolean isVertical = CorneaUtils.getCapability(deviceModel, Tilt.class).getTags().contains(GlobalSetting.VERTICAL_TILT_TAG);
            String selectionAbstract = isVertical ? context.getString(R.string.device_tilt_horizontal) : context.getString(R.string.device_tilt_vertical);

            final OnClickActionSetting orientationSetting = new OnClickActionSetting(context.getString(R.string.device_tilt_orientation), context.getString(R.string.device_tilt_orientation_desc), selectionAbstract);
            orientationSetting.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ButtonListPopup orientationChooser = ButtonListPopup.newInstance(orientationChoices, R.string.device_tilt_orientation_title, -1);
                    orientationChooser.setCallback(new ButtonListPopup.Callback() {
                        @Override
                        public void buttonSelected(String buttonKeyValue) {
                            if (VERTICAL.equals(buttonKeyValue)) {
                                deviceModel.removeTags(ImmutableSet.of(GlobalSetting.VERTICAL_TILT_TAG));
                                orientationSetting.setSelectionAbstract(context.getString(R.string.device_tilt_vertical));
                            } else {
                                deviceModel.addTags(ImmutableSet.of(GlobalSetting.VERTICAL_TILT_TAG));
                                orientationSetting.setSelectionAbstract(context.getString(R.string.device_tilt_horizontal));
                            }

                            deviceModel.commit();

                            // Close the popup when a selection is made.
                            BackstackManager.getInstance().navigateBack();
                        }
                    });

                    BackstackManager.getInstance().navigateToFloatingFragment(orientationChooser, orientationChooser.getClass().getSimpleName(), true);
                }
            });
            settings.add(orientationSetting);

        }
        return settings;
    }
}
