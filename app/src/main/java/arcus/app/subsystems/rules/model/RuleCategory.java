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
package arcus.app.subsystems.rules.model;

import androidx.annotation.NonNull;

import arcus.app.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public enum RuleCategory {

    BUTTONS_FOBS("Buttons & Fobs", R.string.rules_category_buttonsfobs, R.drawable.icon_rule_button),
    CAMERAS_SENSORS("Cameras & Sensors", R.string.rules_category_camerassensors, R.drawable.icon_rule_camera),
    CARE("Care", R.string.rules_category_care, R.drawable.icon_rule_care),
    CLIMATE_CONTROL("Climate", R.string.rules_category_climatecontrol, R.drawable.icon_rule_climate),
    DOORS_LOCKS("Doors & Locks", R.string.rules_category_doorslocks, R.drawable.icon_rules_doorslocks),
    ENERGY_MONITORING("Energy", R.string.rules_category_energymonitoring, R.drawable.icon_rules_energy),
    LAWN_GARDEN("Lawn & Garden", R.string.rules_category_lawngarden, R.drawable.icon_rules_lawngarden),
    LIGHTS_SWITCHES("Lights & Switches", R.string.rules_category_lightswitches, R.drawable.icon_rules_lightsswitches),
    NOTIFICATION("Notifications", R.string.rules_category_notifications, R.drawable.icon_rules_notifications),
    HOME_FAMILY("Home & Family", R.string.rules_category_family_friends,R.drawable.icon_rules_family),
    OTHER("Other", R.string.rules_category_other, R.drawable.icon_rules_other),
    SMOKE_CO("Smoke & CO", R.string.rules_category_smokeco, R.drawable.icon_rules_smoke),
    SECURITY_ALARM("Security Alarm",R.string.rules_category_securityalarm, R.drawable.icon_rules_security),
    SCENES("Scene",R.string.rules_category_scenes, R.drawable.icon_rules_scenes),
    WATER("Water",R.string.rules_category_water, R.drawable.icon_rules_water),
    WINDOWS_BLINDS("Windows & Blinds",R.string.rules_category_windowsblinds, R.drawable.icon_rules_windows);

    private final static Logger logger = LoggerFactory.getLogger(RuleCategory.class);

    private final String platformTag;
    private final int titleResId;
    private final int imageResId;

    RuleCategory (String platformTag, int titleResId, int imageResId) {
        this.platformTag = platformTag;
        this.titleResId = titleResId;
        this.imageResId = imageResId;
    }

    @NonNull
    public static RuleCategory fromPlatformTag (String platformTag) {
        for (RuleCategory thisCategory : RuleCategory.values()) {
            if (thisCategory.platformTag.equalsIgnoreCase(platformTag))
                return thisCategory;
        }

        // Shouldn't ever happen, but just in case...
        logger.error("Bug! Cannot resolve category {} into a known well-defined category. There is a problem with the rule template catalog!", platformTag);
        return OTHER;
    }

    public int getTitleResId () {
        return titleResId;
    }
    public int getImageResId() { return imageResId; }
    public String getPlatformTag() {return platformTag; }
}
