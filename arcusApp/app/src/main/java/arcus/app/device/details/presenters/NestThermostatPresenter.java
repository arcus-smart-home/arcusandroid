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
package arcus.app.device.details.presenters;

import android.view.View;

import arcus.cornea.utils.TemperatureUtils;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.NestThermostat;
import arcus.app.R;
import arcus.app.common.banners.ClickableAbstractTextBanner;
import arcus.app.common.banners.NestAccountRevokedBanner;
import arcus.app.common.banners.NestDeletedBanner;
import arcus.app.common.banners.TemperatureLockBanner;
import arcus.app.common.banners.core.Banner;
import arcus.app.common.utils.ActivityUtils;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.utils.Range;
import arcus.app.device.details.model.ThermostatDisplayModel;
import arcus.app.device.details.model.ThermostatOperatingMode;

import java.util.Map;



public class NestThermostatPresenter extends BaseThermostatPresenter {

    public final static String ERROR_AUTH_REVOKED = "ERR_UNAUTHED";
    public final static String ERROR_RATE_LIMITED = "ERR_RATELIMIT";
    public final static String ERROR_DELETED = "ERR_DELETED";

    private boolean isErrorBannerVisible = false;

    @Override
    public void stopPresenting() {
        removeBanner(TemperatureLockBanner.class);
        removeBanner(NestAccountRevokedBanner.class);
        removeBanner(ClickableAbstractTextBanner.class);

        super.stopPresenting();
    }

    @Override
    boolean isCloudConnected() {
        return true;
    }

    @Override
    Range<Integer> getRestrictedSetpointRange() {

        if (get(NestThermostat.class).getLocked() && getOperatingMode() != ThermostatOperatingMode.ECO && getOperatingMode() != ThermostatOperatingMode.OFF) {
            int lockMin = TemperatureUtils.roundCelsiusToFahrenheit(get(NestThermostat.class).getLockedtempmin());
            int lockMax = TemperatureUtils.roundCelsiusToFahrenheit(get(NestThermostat.class).getLockedtempmax());

            return new Range<>(lockMin, lockMax);
        }

        return new Range<>(null, null);
    }

    @Override
    boolean isLeafEnabled() {
        return get(NestThermostat.class).getHasleaf();
    }

    @Override
    protected void updateView(final ThermostatDisplayModel model) {
        Map<String,String> errors = get(DeviceAdvanced.class).getErrors();
        isErrorBannerVisible = false;

        if (!isDeviceConnected()) {
            isErrorBannerVisible = true;
        }

        if (!isErrorBannerVisible && errors != null && errors.containsKey(ERROR_DELETED)) {
            showBanner(new NestDeletedBanner(getDeviceModel().getAddress()));
            isErrorBannerVisible = true;
        } else {
            removeBanner(NestDeletedBanner.class);
        }

        if (!isErrorBannerVisible && errors != null && errors.containsKey(ERROR_AUTH_REVOKED)) {
            showBanner(new NestAccountRevokedBanner(getDeviceModel().getAddress()));
            isErrorBannerVisible = true;
        } else {
            removeBanner(NestAccountRevokedBanner.class);
        }

        if (!isErrorBannerVisible && errors != null && errors.containsKey(ERROR_RATE_LIMITED)) {
            showBanner(buildRateLimtedBanner());
            isErrorBannerVisible = true;
        } else {
            removeBanner(ClickableAbstractTextBanner.class);
        }

        if (!isErrorBannerVisible && model.getMinSetpointStopValue() != null && model.getMaxSetpointStopValue() != null) {
            showBanner(new TemperatureLockBanner(model.getMinSetpointStopValue(), model.getMaxSetpointStopValue()));
        } else {
            removeBanner(TemperatureLockBanner.class);
        }

        // Update in case we've updated banner states
        model.setControlDisabled(isControlDisabled());

        super.updateView(model);
        updateFooterState(isControlDisabled());
    }

    @Override
    boolean isControlDisabled() {
        return isErrorBannerVisible;
    }

    private Banner buildRateLimtedBanner() {
        ClickableAbstractTextBanner banner = new ClickableAbstractTextBanner(R.string.nest_ratelimited, R.string.get_support);
        banner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityUtils.launchUrl(GlobalSetting.NEST_RATELIMIT_HELP);
            }
        });

        return banner;
    }
}