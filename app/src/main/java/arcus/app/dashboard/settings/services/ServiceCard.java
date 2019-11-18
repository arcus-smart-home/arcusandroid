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
package arcus.app.dashboard.settings.services;

import arcus.app.R;

/**
 * An enumeration of service cards.
 *
 * Note that Per ITWO-3586, all "coming soon" cards should appear at the bottom of this list. Take
 * care to maintain this ordering as new service cards come online.
 */
public enum ServiceCard {
    FAVORITES(R.string.favorites, R.string.card_favorites_desc, R.drawable.favorite_light_22x20, R.drawable.favorite_light_22x20),   // No small icon for favorites
    HISTORY(R.string.card_history_title, R.string.card_history_desc, R.drawable.history, R.drawable.history),
    LIGHTS_AND_SWITCHES(R.string.card_lights_and_switches_title, R.string.card_lights_and_switches_desc, R.drawable.dashboard_lightsswitches, R.drawable.dashboard_lightsswitches),
    SECURITY_ALARM(R.string.card_alarms_title, R.string.card_history_desc, R.drawable.icon_service_safetyalarm, R.drawable.dashboard_alarm),
    CLIMATE(R.string.card_climate_title, R.string.card_climate_desc, R.drawable.icon_service_climate, R.drawable.dashboard_climate),
    DOORS_AND_LOCKS(R.string.card_doors_and_locks_title, R.string.card_doors_and_locks_desc, R.drawable.icon_service_doorlocks, R.drawable.dashboard_doorslocks),
    CAMERAS(R.string.card_cameras_title, R.string.card_cameras_desc, R.drawable.icon_service_camera, R.drawable.dashboard_camera),
    CARE(R.string.card_care_title, R.string.card_care_desc, R.drawable.icon_service_care, R.drawable.dashboard_care),
    HOME_AND_FAMILY(R.string.card_home_and_family_title, R.string.card_home_and_family_desc, R.drawable.icon_service_familyfriends, R.drawable.dashboard_homefamily),
    LAWN_AND_GARDEN(R.string.card_lawn_and_garden_title, R.string.card_lawn_and_garden_desc, R.drawable.icon_service_lawngarden, R.drawable.dashboard_lawngarden),
    WATER(R.string.card_water_title, R.string.card_water_desc, R.drawable.icon_service_water, R.drawable.dashboard_water),
    WINDOWS_AND_BLINDS(R.string.card_windows_and_blinds_title, R.string.card_windows_and_blinds_desc, R.drawable.icon_service_windowblinds, R.drawable.dashboard_windowsblinds),
    ENERGY(R.string.card_energy_title, R.string.card_energy_desc, R.drawable.icon_service_energy, R.drawable.dashboard_energy),
    FEATURE(R.string.card_feature_name, R.string.card_feature_desc, R.drawable.feature_light_23x20, R.drawable.feature_light_23x20);

    private final int titleStringResId;
    private final int descriptionStringResId;
    private final int iconDrawableResId;
    private final int smallIconDrawableResId;

    ServiceCard (int titleId, int serviceDescriptionId, int iconDrawableId, int smallIconDrawableId) {
        this.titleStringResId = titleId;
        this.descriptionStringResId = serviceDescriptionId;
        this.iconDrawableResId = iconDrawableId;
        this.smallIconDrawableResId = smallIconDrawableId;
    }

    public int getTitleStringResId () { return this.titleStringResId; }
    public int getDescriptionStringResId () { return this.descriptionStringResId; }
    public int getIconDrawableResId () { return this.iconDrawableResId; }
    public int getSmallIconDrawableResId () { return this.smallIconDrawableResId; }
}
