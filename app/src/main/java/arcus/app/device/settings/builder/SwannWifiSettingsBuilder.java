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
import android.support.annotation.NonNull;

import arcus.app.R;
import arcus.app.common.utils.StringUtils;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.enumeration.WifiSecurityStandard;
import arcus.app.device.settings.style.EnumSelectionSetting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SwannWifiSettingsBuilder implements SettingBuilder {

    private Logger logger = LoggerFactory.getLogger(SwannWifiSettingsBuilder.class);

    private Setting setting;
    private final Context context;

    private SwannWifiSettingsBuilder(Context context) {
        this.context = context;
    }

    public static SwannWifiSettingsBuilder with (Context context) {
        return new SwannWifiSettingsBuilder(context);
    }

    @NonNull
    public SwannWifiSettingsBuilder buildWifiSecuritySetting(final Context context) {
        WifiSecurityStandard currentSetting = WifiSecurityStandard.NONE;

        setting = new EnumSelectionSetting<>(context, context.getString(R.string.swann_wifi_security_setting), null, StringUtils.getAbstract(context, currentSetting), WifiSecurityStandard.class, currentSetting);
        setting.addListener(new SettingChangedParcelizedListener() {
            @Override
            public void onSettingChanged(Setting setting, Object newValue) {
                setting.setSelectionAbstract(StringUtils.getAbstract(context, newValue));
            }
        });

        return this;
    }


    public Setting build() {
        return setting;
    }
}
