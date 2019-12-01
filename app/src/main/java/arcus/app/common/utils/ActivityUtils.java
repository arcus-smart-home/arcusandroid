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
package arcus.app.common.utils;

import android.content.Intent;
import android.net.Uri;

import arcus.app.ArcusApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ActivityUtils {

    private final static Logger logger = LoggerFactory.getLogger(ActivityUtils.class);

    public static void launchLearnMore() {
        launchUrl(Uri.parse(GlobalSetting.LEARN_MORE_URL));
    }

    public static void launchReduceAlarms() {
        launchUrl(Uri.parse(GlobalSetting.REDUCE_ALARMS));
    }

    /* Shopping */
    public static void launchShopNow() {
        launchUrl(Uri.parse(GlobalSetting.SHOP_NOW_URL));
    }
    public static void launchShopSecurityNow() {
        launchUrl(Uri.parse(GlobalSetting.SHOP_SECURITY_URL));
    }
    public static void launchShopSmokeNow() {
        launchUrl(Uri.parse(GlobalSetting.SHOP_SMOKE_URL));
    }
    public static void launchShopWaterNow() {
        launchUrl(Uri.parse(GlobalSetting.SHOP_WATER_URL));
    }
    public static void launchShopLightsNSwitchesNow() {
        launchUrl(Uri.parse(GlobalSetting.SHOP_LIGHTS_N_SWITCHES_URL));
    }
    public static void launchShopClimateNow() {
        launchUrl(Uri.parse(GlobalSetting.SHOP_CLIMATE_URL));
    }
    public static void launchShopDoorsNLocksNow() {
        launchUrl(Uri.parse(GlobalSetting.SHOP_DOORS_URL));
    }
    public static void launchShopCamerasNow() {
        launchUrl(Uri.parse(GlobalSetting.SHOP_CAMERAS_URL));
    }
    public static void launchShopCareNow() {
        launchUrl(Uri.parse(GlobalSetting.SHOP_CARE_URL));
    }
    public static void launchShopHomeNFamilyNow() {
        launchUrl(Uri.parse(GlobalSetting.SHOP_HOME_N_FAMILY_URL));
    }
    public static void launchShopLawnNGardenNow() {
        launchUrl(Uri.parse(GlobalSetting.SHOP_LAWN_N_GARDEN_URL));
    }
    public static void launchShopHubNow() {
        launchUrl(Uri.parse(GlobalSetting.SHOP_HUB_URL));
    }
    public static void launchSupport() {
        launchUrl(Uri.parse(GlobalSetting.SUPPORT_URL));
    }

    public static void launchUrl(Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            ArcusApplication.getArcusApplication().startActivity(intent);
        } catch (Exception e) {
            logger.error("Failed to launch browser with " + uri, e);
        }
    }

    public static void callSupport() {
        Intent callSupportIntent = new Intent(Intent.ACTION_DIAL, GlobalSetting.SUPPORT_NUMBER_URI);
        callSupportIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            ArcusApplication.getArcusApplication().startActivity(callSupportIntent);
        } catch (Exception e) {
            logger.error("Failed to call support.", e);
        }
    }
}
