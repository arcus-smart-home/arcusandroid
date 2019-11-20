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

import android.net.Uri;

import arcus.app.BuildConfig;
import arcus.cornea.SessionController;
import com.iris.client.model.DeviceModel;
import com.iris.client.session.SessionInfo;

public class GlobalSetting {
    private static String getRedirectUrl() {
        SessionInfo sessionInfo = SessionController.instance().getSessionInfo();
        if (sessionInfo != null) {
            return sessionInfo.getRedirectBaseUrl() + "/";
        } else {
            return "";
        }
    }

    public static final String ARCUS_PLATFORM_WS = BuildConfig.ARCUS_BASE_PLATFORM_URL;

    public static final int HISTORY_LOG_ENTRIES_DASH = 3;
    public static final Uri NO_CONNECTION_HUB_SUPPORT_URL = Uri.parse(getRedirectUrl() + "s_hub");
    public static final Uri NO_CONNECTION_SUPPORT_URL = Uri.parse(getRedirectUrl() + "s_devicetroubleshooting");
    public static final Uri VALVE_SUPPORT_URI = Uri.parse(getRedirectUrl() + "s_leaksmartshutoffvalve");
    public static final Uri HALO_SUPPORT_URI = Uri.parse(getRedirectUrl() + "s_halo");
    public static final Uri SWANN_PAIRING_SUPPORT_URI = Uri.parse(getRedirectUrl() + "support/devices/switch/162918/err_android_wifi");
    public static final Uri SECURITY_CALLTREE_SUPPORT = Uri.parse(getRedirectUrl() + "s_promonitoring");
    public static final Uri NOAA_WEATHER_RADIO_COVERAGE_URI = Uri.parse(getRedirectUrl() + "noaa_maps");
    public static final Uri WATERHEATER_HEATER_SUPPORT_NUMBER = Uri.parse("tel:5551212");
    public static final Uri SUPPORT_NUMBER_URI = Uri.parse("tel:5551212");
    public static final String PRO_MONITORING_STATION_NUMBER = "<a href=\"tel:\\5551212\">555-1212</a>";
    public static final Uri PRO_MONITORING_ADD_CONTACT_LEARN_MORE = Uri.parse(getRedirectUrl() + "s_promonitoring");
    public static final Uri HUB_TIMEOUT_HELP = Uri.parse(getRedirectUrl() + "s_hub");
    public static final Uri NEST_RATELIMIT_HELP = Uri.parse(getRedirectUrl() + "s_nest");
    public static final String TWINSTAR_SUPPORT_NUMBER = "555-1212";
    public static final Uri RING_NOT_PURPLE = Uri.parse(getRedirectUrl() + "s_hub_purple_ring");
    public static final Uri HUB_ALREADY_PAIRED_UPGRADE = Uri.parse(getRedirectUrl() + "s_hub_already_paired");

    public static final int TIMEOUT_PER_GET_SECONDS = 30;

    public static final String SWANN_WIFI_PLUG_PRODUCT_ID = "162918";
    public static final String AMAZON_ECHO_PRODUCT_ID = "7dfa41";
    public static final String AMAZON_TAP_PRODUCT_ID = "7dfa42";
    public static final String AMAZON_DOT_PRODUCT_ID = "7dfa43";
    public static final String GOOGLE_HOME_PRODUCT_ID = "1b1036";

    public static final String IS_HUB_OR_DEVICE = "is hub or device";
    public static final String DEVICE_NAME = "device name";

    public static final String FAVORITE_TAG = "FAVORITE";
    public static final String VERTICAL_TILT_TAG = "closedOnUpright";

    public static final String STEP_STRING = "STEP";
    public static final String IS_LAST_STEP = "LAST";

    public static final String CREDIT_INFO_FIRST_NAME_KEY = "credit first name";
    public static final String CREDIT_INFO_LAST_NAME_KEY = "credit last name";
    public static final String CREDIT_INFO_YEAR_KEY = "credit year";
    public static final String CREDIT_INFO_MONTH_KEY = "credit month";
    public static final String CREDIT_INFO_ADDRESS1_KEY = "credit address1";
    public static final String CREDIT_INFO_ADDRESS2_KEY = "credit address2";
    public static final String CREDIT_INFO_CITY_KEY = "credit city";
    public static final String CREDIT_INFO_STATE_KEY = "credit state";
    public static final String CREDIT_INFO_ZIPCODE_KEY = "credit zip";
    public static final String CREDIT_INFO_COUNTRY_KEY = "credit country";
    public static final String CREDIT_INFO_CARD_NUMBER_KEY = "credit card number";
    public static final String CREDIT_INFO_VERIFICATION_CODE_KEY = "credit verification code";

    public static final long HUB_PAIRING_MODE_TIME = 300000L;  //  5 minutes

    /* Device specific redirects */
    public static final String HUE_IMPROPERLY_PAIRED_DEVICE_URL = getRedirectUrl() + "s_improperlypairedhue";
    public static final String KITTING_NEED_HELP_URL = getRedirectUrl() + "s_hub_kitting_help";
    public static final String KITTING_TUTORIAL_VIDEO_URL = getRedirectUrl() + "y_kit_tutorial";
    public static final String INDOOR_PLUG_WIFI_NEED_HELP_URL = getRedirectUrl() + "s_gs_indoor_wifi_need_help";
    public static final String OUTDOOR_PLUG_WIFI_NEED_HELP_URL = getRedirectUrl() + "s_gs_outdoor_wifi_need_help";
    public static final String INDOOR_PLUG_BLE_NEED_HELP_URL = getRedirectUrl() + "s_gs_indoor_ble_need_help";
    public static final String OUTDOOR_PLUG_BLE_NEED_HELP_URL = getRedirectUrl() + "s_gs_outdoor_ble_need_help";
    public static final String INDOOR_PLUG_FACTORY_RESET_STEPS_URL = getRedirectUrl() + "s_gs_indoor_factory_reset";
    public static final String OUTDOOR_PLUG_FACTORY_RESET_STEPS_URL = getRedirectUrl() + "s_gs_outdoor_factory_reset";

    //  Dashboard sliding menu
    public static final String SUPPORT_URL = getRedirectUrl() + "support";
    public static final String LEARN_MORE_URL = "http://www.example.com";
    public static final String T_AND_C_LINK = "https://www.example.com/terms-of-service/";
    public static final String PRIVACY_LINK = "https://www.example.com/privacy-statement/";
    public static final String SHOP_NOW_URL = "https://www.example.com/products/";

    public static final String CHECK_WATER_HARDNESS_URL = "http://www.example.com/Plumbing/Water-Filtration-Water-Softeners/Water-Softening-Filtration-Accessories/_/N-1z10xx5Z1z10xau/pl#!";
    public static final String WHAT_SALT_SHOULD_USE_URL = "http://www.example.com/Plumbing/Water-Filtration-Water-Softeners/Salt/_/N-1z10xx1/pl";
    public static final String ALEXA_PAIRING_INSTRUCTIONS_URL = getRedirectUrl() + "s_alexa";
    public static final String GOOGLE_PAIRING_INSTRUCTIONS_URL = getRedirectUrl() + "s_googleassistant";

    /* Website - Product Shopping */
    public static final String SHOP_SECURITY_URL = getRedirectUrl() + "p_security";
    public static final String SHOP_SMOKE_URL = getRedirectUrl() + "p_smoke";
    public static final String SHOP_WATER_URL = getRedirectUrl() + "p_water";
    public static final String SHOP_LIGHTS_N_SWITCHES_URL = getRedirectUrl() + "p_lights";
    public static final String SHOP_CLIMATE_URL = getRedirectUrl() + "p_climate";
    public static final String SHOP_DOORS_URL = getRedirectUrl() + "p_doors";
    public static final String SHOP_CAMERAS_URL = getRedirectUrl() + "p_cameras";
    public static final String SHOP_CARE_URL = getRedirectUrl() + "p_care";
    public static final String SHOP_HOME_N_FAMILY_URL = getRedirectUrl() + "p_home";
    public static final String SHOP_LAWN_N_GARDEN_URL = getRedirectUrl() + "p_lawn";
    public static final String SHOP_HUB_URL = getRedirectUrl() + "p_hub";
    public static final String HUB_FACTORY_RESET_STEPS_URL = getRedirectUrl() + "s_hub_factory_reset";
    public static final String SWANN_CAMERA_FACTORY_RESET_STEPS_URL = getRedirectUrl() + "s_swann_factory_reset";
    public static final String HUB_BLE_NEED_HELP_URL = getRedirectUrl() + "s_v3_hub_ble_need_help";
    public static final String SWANN_BLE_NEED_HELP_URL = getRedirectUrl() + "s_swann_ble_need_help";
    public static final String HUB_WIFI_NEED_HELP_URL = getRedirectUrl() + "s_v3_hub_wifi_need_help";
    public static final String SWANN_WIFI_NEED_HELP_URL = getRedirectUrl() + "s_swann_wifi_need_help";

    /* Website - Solutions */
    public static final String REDUCE_ALARMS = getRedirectUrl() + "s_reducealarms";
    public static final String ACTIVATE_CARDS_URL = getRedirectUrl() + "s_intro_cards";

    /* YouTube */
    public static final String V2_HUB_TUTORIAL = getRedirectUrl() + "y_v2_hub_tutorial";
    public static final String V3_HUB_TUTORIAL = getRedirectUrl() + "y_v3_hub_tutorial";

    //  Contact Sensor extra pairing data
    public final static String SCENE_SCHEDULER_NAME = "FIRE";

    public final static String DEVICE_SPECIFIC_SUPPORT_URI = "support/devices/%s/%s/%s";

    public static String getDeviceSupportUri(DeviceModel model, String errorId) {
        return getRedirectUrl() + String.format(DEVICE_SPECIFIC_SUPPORT_URI, model.getDevtypehint().replaceAll("\\s", "").toLowerCase(), model.getProductId(), errorId.replaceAll("\\s", "").toLowerCase());
    }

    public enum AlertCardTags {
        TOP_CARD,
        STATUS_CARD,
        ALL_DEVICES_CARD,
        PARTIAL_DEVICES_CARD,
        HISTORY_CARD,
        NOTIFICATIONS_CARD,
        ALARM_ACTIVE
    }

    public static final int PERMISSION_CAMERA = 990;
    public static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 993;
    public static final int PERMISSION_ACCESS_COARSE_LOCATION = 994;
    public static final int PERMISSION_READ_CONTACTS = 996;
    public static final int PERMISSION_USE_FINGERPRINT = 997;
}
