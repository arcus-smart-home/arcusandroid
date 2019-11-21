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
import androidx.annotation.Nullable;
import android.view.View;

import com.iris.client.capability.WiFi;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.fragment.CameraNetworkFragment;
import arcus.app.device.settings.style.CenteredTextSetting;
import arcus.app.device.settings.style.LeftImageSetting;
import arcus.app.device.settings.style.TransitionToFragmentSetting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GenieSettingsBuilder implements SettingBuilder {

    private static final Logger logger = LoggerFactory.getLogger(GenieSettingsBuilder.class);

    private final Context context;
    private final String title;
    private final String description;
    @Nullable
    private Setting setting;

    private GenieSettingsBuilder(Context context, String title, String description) {
        this.context = context;
        this.title = title;
        this.description = description;
    }

    @NonNull
    public static GenieSettingsBuilder with (Context context, String title, String description) {
        return new GenieSettingsBuilder(context, title, description);
    }

    @Nullable
    public GenieSettingsBuilder buildNetworkSetting(DeviceModel model) {
        final WiFi wifiCap = CorneaUtils.getCapability(model, WiFi.class);

        if (wifiCap != null) {
            setting = new TransitionToFragmentSetting(title, description, wifiCap.getSsid(), CameraNetworkFragment.newInstance(model.getAddress()));
        }

        return this;
    }

    @NonNull
    public GenieSettingsBuilder buildDeviceSetting(@NonNull DeviceModel model){
        setting = new LeftImageSetting(title, description, null, model, true, null);

        return this;
    }

    @NonNull
    public GenieSettingsBuilder buildBuyMoreSetting() {
        setting = new CenteredTextSetting(title, description, context.getString(R.string.genie_buy_more), null, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Do Something!
            }
        });

        return this;
    }

    @Nullable
    @Override
    public Setting build() {
        return setting;
    }
}
