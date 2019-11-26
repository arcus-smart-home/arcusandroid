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
package arcus.app.device.settings;

import android.app.Activity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.device.model.DeviceType;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;
import arcus.app.device.settings.resolver.ButtonDeviceSettingsResolver;
import arcus.app.device.settings.resolver.CameraSettingsResolver;
import arcus.app.device.settings.resolver.ContactSensorSettingsResolver;
import arcus.app.device.settings.resolver.DeviceSettingsResolver;
import arcus.app.device.settings.resolver.GenieSettingsResolver;
import arcus.app.device.settings.resolver.HaloSettingsResolver;
import arcus.app.device.settings.resolver.HubSettingsResolver;
import arcus.app.device.settings.resolver.SettingsResolver;
import arcus.app.device.settings.resolver.SomfyBlindsResolver;
import arcus.app.device.settings.resolver.SwitchSettingsResolver;
import arcus.app.device.settings.resolver.TiltSensorSettingsResolver;
import arcus.app.device.settings.resolver.WaterSoftenerSettingsResolver;
import arcus.app.device.settings.resolver.WifiSwitchSettingsResolver;

import org.apache.commons.lang3.StringUtils;

/**
 * A public API for determining and building settings for a given device or hub.
 */
public class SettingsManager {

    private final Activity context;
    private final HubModel hub;
    private final DeviceModel device;
    private final SettingChangedParcelizedListener listener;

    private SettingsManager (Activity context, SettingChangedParcelizedListener listener, DeviceModel device, HubModel hub) {
        this.context = context;
        this.device = device;
        this.hub = hub;
        this.listener = listener;
    }

    @Nullable
    public static SettingsManager with (@Nullable Activity context, @Nullable SettingChangedParcelizedListener listener, @Nullable DeviceModel device) {
        if (context == null)
            throw new IllegalArgumentException("Context may not be null.");

        if (device == null)
            throw new IllegalArgumentException("Device may not be null.");

        return new SettingsManager(context, listener, device, null);
    }

    @Nullable
    public static SettingsManager with (@Nullable Activity context, @Nullable DeviceModel device) {
        return with(context, null, device);
    }

    @Nullable
    public static SettingsManager with (@Nullable Activity context, @Nullable SettingChangedParcelizedListener listener, @Nullable HubModel hub) {
        if (context == null)
            throw new IllegalArgumentException("Context may not be null.");

        if (hub == null)
            throw new IllegalArgumentException("Hub may not be null.");

        return new SettingsManager(context, listener, null, hub);
    }

    @Nullable
    public static SettingsManager with (@Nullable Activity context, @Nullable HubModel hub) {
        return with(context, null, hub);
    }

    @Nullable
    public SettingsList getSettings() {
        if (hub != null) {
            return new HubSettingsResolver().getSettings(context, listener, hub);
        } else {
            SettingsResolver resolver = findResolver(device.getProductId(), DeviceType.fromHint(device.getDevtypehint()));
            return resolver == null ? null : resolver.getSettings(context, listener, device);
        }
    }

    @NonNull SettingsResolver findResolver(@NonNull String productId, @NonNull DeviceType deviceType) {
        SettingsResolver productSpecificResolver = findResolverForProduct(productId);
        if (productSpecificResolver != null) {
            return productSpecificResolver;
        }

        else {
            return findResolverForDeviceType(deviceType);
        }
    }

    @Nullable
    private SettingsResolver findResolverForProduct(@NonNull String productId) {

        // No product-specific resolver for devices not in product catalog
        if (StringUtils.isEmpty(productId)) {
            return null;
        }

        switch (productId.toLowerCase()) {
            case GlobalSetting.SWANN_WIFI_PLUG_PRODUCT_ID:
                return new WifiSwitchSettingsResolver();
        }

        return null;
    }

    @NonNull
    private SettingsResolver findResolverForDeviceType (@NonNull DeviceType deviceType) {

        switch (deviceType) {
            //removing the settings option for fans
            //case FAN_CONTROL:
            //    return new FanSettingsResolver();
            case SWITCH:
                return new SwitchSettingsResolver();
            case TILT_SENSOR:
                return new TiltSensorSettingsResolver();
            case CONTACT:
                return new ContactSensorSettingsResolver();
            case KEYFOB:
            case PENDANT:
            case BUTTON:
                return new ButtonDeviceSettingsResolver();
            case CAMERA:
                return new CameraSettingsResolver();
            case WATER_SOFTENER:
                return new WaterSoftenerSettingsResolver();
            case GENIE_GARAGE_DOOR_CONTROLLER:
                return new GenieSettingsResolver();
            case SOMFYV1BLINDS:
                return new SomfyBlindsResolver();
            case HALO:
                return new HaloSettingsResolver();
            default:
                return new DeviceSettingsResolver();
        }
    }
}
