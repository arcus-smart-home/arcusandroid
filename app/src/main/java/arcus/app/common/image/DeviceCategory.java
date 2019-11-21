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
package arcus.app.common.image;

import androidx.annotation.NonNull;

import arcus.app.R;

/**
 * An enumeration of device categories as rendered in the produce catalog screens. Binds the
 * category to its static (built-in) image and name.
 */
public enum DeviceCategory {
    CAMERAS_SENSORS(R.drawable.icon_cat_cameras_sensors, "Cameras & Sensors"),
    CARE(R.drawable.icon_cat_care, "Care"),
    CLIMATE(R.drawable.icon_cat_climate, "Climate"),
    DOORS_LOCKS(R.drawable.icon_cat_doors_locks, "Doors & Locks"),
    ENERGY(R.drawable.icon_cat_energy, "Energy"),
    FAMILY_FRIENDS(R.drawable.icon_cat_family_friends, "Home & Family"),
    GARAGE_DOORS(R.drawable.icon_cat_garagedoor, "Garage Doors"),
    LAWN_GARDEN(R.drawable.icon_cat_lawnandgarden, "Lawn & Garden"),
    LIGHTS_SWITCHES(R.drawable.icon_cat_lights_switches, "Lights & Switches"),
    PLUMBING(R.drawable.icon_cat_plumbing, "Plumbing"),
    SECURITY(R.drawable.icon_cat_securityalarm, "Security"),
    SMOKE_CO(R.drawable.icon_cat_smoke_co, "Smoke & CO"),
    VOICE(R.drawable.icon_cat_voice, "Voice Assistant"),
    WATER(R.drawable.icon_cat_water, "Water"),
    WINDOWS_BLINDS(R.drawable.icon_cat_window_blinds, "Windows & Blinds"),
    MORE(R.drawable.icon_otheraccessories, "More"),
    UNKNOWN(R.drawable.icon_cat_placeholder, "Unknown");

    private final int imageResId;
    private final String productCatalogName;

    DeviceCategory (int imageResId, String productCatalogName) {
        this.imageResId = imageResId;
        this.productCatalogName = productCatalogName;
    }

    @NonNull
    public static DeviceCategory fromProductCategoryName(String productCategoryName) {
        for (DeviceCategory thisCategory : DeviceCategory.values()) {
            if (thisCategory.productCatalogName.equalsIgnoreCase(productCategoryName)) {
                return thisCategory;
            }
        }

        return UNKNOWN;
    }

    public int getImageResId() {
        return imageResId;
    }
}
