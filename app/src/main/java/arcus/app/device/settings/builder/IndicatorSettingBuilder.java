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

import android.content.Context;
import androidx.annotation.NonNull;

import com.iris.client.capability.Indicator;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.enumeration.InvertedLedState;
import arcus.app.device.settings.style.EnumSelectionSetting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class IndicatorSettingBuilder implements SettingBuilder {

    private static final Logger logger = LoggerFactory.getLogger(IndicatorSettingBuilder.class);

    private final String title;
    private final String description;
    private final Context context;
    private EnumSelectionSetting<InvertedLedState> setting;

    private IndicatorSettingBuilder (Context context, String title, String description) {
        this.title = title;
        this.description = description;
        this.context = context;
    }

    @NonNull
    public static IndicatorSettingBuilder with(Context context, String title, String description) {
        return new IndicatorSettingBuilder(context, title, description);
    }

    @NonNull
    public IndicatorSettingBuilder addInvertedLedStateSetting(@NonNull final DeviceModel model) {

        final Indicator indicatorCap = CorneaUtils.getCapability(model, Indicator.class);

        // Does device support the indicator inverted capability?
        if (indicatorCap != null && model.get(Indicator.ATTR_INVERTED) != null) {

            logger.debug("Building indicator LED polarity setting.");
            InvertedLedState initialLedState = InvertedLedState.fromInverted(indicatorCap.getInverted());
            setting = new EnumSelectionSetting<>(context, context.getString(R.string.setting_led_status), context.getString(R.string.setting_led_status_description), InvertedLedState.class, initialLedState);

            setting.addListener(new SettingChangedParcelizedListener() {
                @Override
                public void onSettingChanged(Setting setting, Object newValue) {
                    logger.debug("Changing switch inverted property to " + newValue);
                    indicatorCap.setInverted(newValue == InvertedLedState.ON_WHEN_SWITCH_IS_OFF);
                    model.commit();
                }
            });

        } else {
            logger.debug("This device does not support the LED polarity setting.");
        }

        return this;
    }


    public Setting build () {
        return setting;
    }
}
