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
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import arcus.cornea.device.DeviceController;
import arcus.cornea.device.blinds.SomfyBlindsDeviceController;
import arcus.cornea.error.ErrorModel;
import com.iris.client.capability.Somfyv1;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.ButtonListPopup;
import arcus.app.device.settings.builder.ParentChildSettingBuilder;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.fragment.SomfyCustomizationFragment;
import arcus.app.device.settings.style.BinarySetting;
import arcus.app.device.settings.style.OnClickActionSetting;
import arcus.app.device.settings.style.TitleBarSetting;

import java.util.HashMap;
import java.util.Map;


public class SomfyBlindsResolver extends DeviceSettingsResolver implements SettingsResolver, SomfyBlindsDeviceController.Callback,
        DeviceController.Callback {
    private SomfyBlindsDeviceController controller;

    @Nullable
    @Override
    public SettingsList getSettings(@NonNull final Activity context, SettingChangedParcelizedListener listener, Object model) {
        SettingsList settings = super.getSettings(context, listener, model);


        DeviceModel deviceModel = (DeviceModel) model;
        if(controller == null) {
            controller = SomfyBlindsDeviceController.newController(deviceModel.getId(), this, this);
        }

        if (!(deviceModel instanceof Somfyv1)) {
            return settings;
        }

        settings.add(ParentChildSettingBuilder.with(context.getString(R.string.setting_blind_settings), context.getString(R.string.blind_configuration))
                .dontPromoteOnlyChild()
                .addChildSetting(buildChannelController(context.getString(R.string.blinds_channel)), false)
                .addChildSetting(buildTypeSetting(context, (Somfyv1) deviceModel), false)
                .addChildSetting(buildCustomizationSetting(context), false)
                .addChildSetting(buildDirectionSetting(context, (Somfyv1) deviceModel), false)
                .build());
        return settings;
    }

    private TitleBarSetting buildChannelController(String title) {
        return new TitleBarSetting(title, Integer.toString((int)controller.getChannel()));
    }

    private OnClickActionSetting buildTypeSetting(final Context context, Somfyv1 model) {
        if(model==null){
            return new OnClickActionSetting(context.getString(R.string.setting_blind_type_header),
                    null, "");
        }
        String selectionAbstract = model.getType().equals(Somfyv1.TYPE_BLIND) ? context.getString(R.string.blinds_tilt) : context.getString(R.string.blinds_raise_lower);
        final OnClickActionSetting typeSetting = new OnClickActionSetting(context.getString(R.string.setting_blind_type_header),
                null, selectionAbstract);
        typeSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Map<String, String> typeChoices = new HashMap<>();
                final String TILT = context.getString(R.string.blinds_tilt);
                final String RAISE_LOWER = context.getString(R.string.blinds_raise_lower);
                typeChoices.put(TILT, TILT);
                typeChoices.put(RAISE_LOWER, RAISE_LOWER);
                ButtonListPopup typeChooser = ButtonListPopup.newInstance(typeChoices, R.string.setting_blind_type_header, -1);
                typeChooser.setCallback(new ButtonListPopup.Callback() {
                    @Override
                    public void buttonSelected(String buttonKeyValue) {
                        if (TILT.equals(buttonKeyValue)) {
                            controller.setType(Somfyv1.TYPE_BLIND);
                            typeSetting.setSelectionAbstract(TILT);
                        } else {
                            controller.setType(Somfyv1.TYPE_SHADE);
                            typeSetting.setSelectionAbstract(RAISE_LOWER);
                        }
                        // Close the popup when a selection is made.
                        BackstackManager.getInstance().navigateBack();
                    }
                });

                BackstackManager.getInstance().navigateToFloatingFragment(typeChooser, typeChooser.getClass().getSimpleName(), true);
            }
        });
        return typeSetting;
    }

    private OnClickActionSetting buildCustomizationSetting(final Context context) {
        final OnClickActionSetting typeSetting = new OnClickActionSetting(context.getString(R.string.customization),
                null, "");
        typeSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(new SomfyCustomizationFragment(), true);
            }
        });
        return typeSetting;
    }

    private BinarySetting buildDirectionSetting(Context context, Somfyv1 model) {
        if(model==null){
            return new BinarySetting("REVERSE BLIND DIRECTION", null, false);
        }
        final boolean isReversed = Somfyv1.REVERSED_REVERSED.equals(model.getReversed());
        BinarySetting setting = new BinarySetting(context.getString(R.string.somfy_blind_reverse), null, isReversed);
        setting.addListener(new SettingChangedParcelizedListener() {
            @Override
            public void onSettingChanged(Setting setting, Object newValue) {
                controller.setReversed(!Somfyv1.REVERSED_REVERSED.equals(model.getReversed()));
            }
        });
        return setting;
    }

    @Override
    public void errorOccurred(Throwable throwable) {

    }

    @Override
    public void updateView() {

    }

    @Override
    public void show(Object model) {

    }

    @Override
    public void onError(ErrorModel error) {

    }
}

