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
import androidx.annotation.NonNull;

import arcus.cornea.utils.Listeners;
import com.iris.client.capability.EcowaterWaterSoftener;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.enumeration.EcoWaterFlowRate;
import arcus.app.device.settings.style.BinarySetting;
import arcus.app.device.settings.style.CenteredTextSetting;
import arcus.app.device.settings.style.ListSelectionSetting;

public class EcoWaterSettingBuilder implements SettingBuilder {

    private Activity context;
    private final String title;
    private final String description;
    private Setting setting;

    private EcoWaterSettingBuilder (Activity context, String title, String description) {
        this.context = context;
        this.title = title;
        this.description = description;
    }

    @NonNull
    public static EcoWaterSettingBuilder with (Activity context, String title, String description) {
        return new EcoWaterSettingBuilder(context, title, description);
    }

    @NonNull public EcoWaterSettingBuilder buildWaterFlowSettingsDescription(final DeviceModel model) {
        final EcowaterWaterSoftener ecoWaterCapability = CorneaUtils.getCapability(model, EcowaterWaterSoftener.class);

        String settingsDescription = context.getString(R.string.water_softener_ecowater_settings_desc);

        if (ecoWaterCapability != null) {
            setting = new CenteredTextSetting(settingsDescription, null, null, null, null);
        }

        return this;
    }


    @NonNull
    public EcoWaterSettingBuilder buildContinuousWaterFlowSetting (@NonNull final DeviceModel model) {
        final EcowaterWaterSoftener ecoWaterCapability = CorneaUtils.getCapability(model, EcowaterWaterSoftener.class);
        if (ecoWaterCapability != null) {

            EcoWaterFlowRate currentFlowRate = EcoWaterFlowRate.fromFlowRate(ecoWaterCapability.getContinuousRate());

            // Update the UI if for some reason alert is set to false while the rate is not 0.0
            if(ecoWaterCapability.getAlertOnContinuousUse() == false) {
                currentFlowRate = EcoWaterFlowRate.fromFlowRate(0.0);
            }

            setting = new ListSelectionSetting(title, description, currentFlowRate.getTitlesList(context),
                    currentFlowRate.getDescriptionsList(context), context.getString(currentFlowRate.getTitleTextId()),
                    context.getString(currentFlowRate.getAbstractTextId()));

            setting.addListener(new SettingChangedParcelizedListener() {
                @Override
                public void onSettingChanged(Setting setting, @NonNull Object newValue) {

                    final EcoWaterFlowRate newFlowRate = EcoWaterFlowRate.fromtitleText(context, newValue.toString());
                    setting.setSelectionAbstract(context.getString(newFlowRate.getAbstractTextId()));
                    ecoWaterCapability.setContinuousRate(newFlowRate.getFlowRate());
                    if (newFlowRate.getFlowRate() == 0.0) {
                        ecoWaterCapability.setAlertOnContinuousUse(false);
                    } else {
                        ecoWaterCapability.setAlertOnContinuousUse(true);
                    }

                    model.commit().onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                        @Override
                        public void onEvent(Throwable throwable) {
                            ErrorManager.in(context).showGenericBecauseOf(throwable);
                        }
                    }));

                }
            });

        }

        return this;
    }

    @NonNull public EcoWaterSettingBuilder buildExcessiveWaterFlowSetting(final DeviceModel model) {
        final EcowaterWaterSoftener ecoWaterCapability = CorneaUtils.getCapability(model, EcowaterWaterSoftener.class);
        if (ecoWaterCapability != null) {

            boolean alertOnExcessive = Boolean.TRUE.equals(ecoWaterCapability.getAlertOnExcessiveUse());
            setting = new BinarySetting(title, String.format(description, CorneaUtils.getAccountHolder().getFirstName()), alertOnExcessive);
            setting.addListener(new SettingChangedParcelizedListener() {
                @Override
                public void onSettingChanged(Setting setting, Object newValue) {

                    boolean isExcessiveWater =  (boolean) newValue;
                    ecoWaterCapability.setAlertOnExcessiveUse(isExcessiveWater);
                    model.commit().onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                        @Override
                        public void onEvent(Throwable throwable) {
                            ErrorManager.in(context).showGenericBecauseOf(throwable);
                        }
                    }));
                }
            });
        }

        return this;
    }

    @Override
    public Setting build() {
        return setting;
    }
}
