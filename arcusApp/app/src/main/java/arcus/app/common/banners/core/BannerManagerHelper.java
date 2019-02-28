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
package arcus.app.common.banners.core;

import android.app.Activity;

import arcus.app.common.banners.ConfigureDeviceBanner;
import arcus.app.common.banners.EarlySmokeWarningBanner;
import arcus.app.common.banners.FirmwareUpdatingBanner;
import arcus.app.common.banners.InvitationBanner;
import arcus.app.common.banners.NoConnectionBanner;
import arcus.app.common.banners.NoHubConnectionBanner;
import arcus.app.common.banners.RunningOnBatteryBanner;
import arcus.app.common.banners.ServiceSuspendedBanner;
import arcus.app.common.banners.UpdateServicePlanBanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class BannerManagerHelper {
    private static final Logger logger = LoggerFactory.getLogger(BannerManagerHelper.class);

    public static boolean canShowBanner(Activity activity, Banner bannerToShow) {
        if (activity == null || bannerToShow == null) {
            logger.debug("Activity/Banner trying to show was null - bailing.");
            return false;
        }

        if (!(activity instanceof BannerActivity)) {
            logger.debug("Incorrect activity type - cannot show [{}]", bannerToShow.getClass().getSimpleName());
            return false;
        }

        BannerAdapter bannerAdapter = ((BannerActivity) activity).getBanners();
        if (bannerAdapter == null) {
            logger.debug("bannerAdapter was null - cannot show [{}]", bannerToShow.getClass().getSimpleName());
            return false;
        }

        // We're not showing anything - we can display any banner requested right now
        if (bannerAdapter.getCount() == 0) {
            logger.debug("Displaying [{}] - not showing any banners currently", bannerToShow.getClass().getSimpleName());
            return true;
        }

        // Any "connection" banner beats all
        if (bannerToShow instanceof NoHubConnectionBanner || bannerToShow instanceof NoConnectionBanner) {
            return true; // Always show connection banners
        } else if (bannerToShow instanceof EarlySmokeWarningBanner) {
            return true;
        }

        // Any InvitationBanner wins, if we're not showing a higher order banner
        boolean showingHigherOrderBanner = bannerAdapter.containsBanner(NoHubConnectionBanner.class) || bannerAdapter.containsBanner(NoConnectionBanner.class);
        if (!showingHigherOrderBanner && bannerToShow instanceof InvitationBanner) {
            return true;
        }

        // Any Service Suspended banner wins, if we're not showing a higher order banner
        showingHigherOrderBanner = showingHigherOrderBanner || bannerAdapter.containsBanner(FirmwareUpdatingBanner.class);
        if (!showingHigherOrderBanner && bannerToShow instanceof ServiceSuspendedBanner) {
            return true;
        }

        // Any Configure Device banner wins, if we're not showing a higher order banner
        showingHigherOrderBanner = showingHigherOrderBanner || bannerAdapter.containsBanner(ServiceSuspendedBanner.class);
        if (!showingHigherOrderBanner && bannerToShow instanceof ConfigureDeviceBanner) {
            return true;
        }

        // Any Update Service Plan banner wins, if we're not showing a higher order banner
        showingHigherOrderBanner = showingHigherOrderBanner || bannerAdapter.containsBanner(ConfigureDeviceBanner.class);
        if (!showingHigherOrderBanner && bannerToShow instanceof UpdateServicePlanBanner) {
            return true;
        }

        // Any Invite/Battery banner wins, if we're not showing a higher order banner
        showingHigherOrderBanner = showingHigherOrderBanner || bannerAdapter.containsBanner(UpdateServicePlanBanner.class);
        if (!showingHigherOrderBanner && (bannerToShow instanceof FirmwareUpdatingBanner || bannerToShow instanceof RunningOnBatteryBanner)) {
            return true;
        }

        // If we aren't showing one of the "Higher Priority" banners above showingHigherOrderBanner will be false,
        // invert to say we can override the unspecified banner and use the one requested.
        logger.debug("Returning [{}] - to specify if we should show a banner.", !showingHigherOrderBanner);
        return !showingHigherOrderBanner;
    }
}
