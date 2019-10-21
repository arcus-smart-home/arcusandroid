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

/**
 * An enumeration of different categories of images used throughout the application.
 */
public enum ImageCategory {
    /**
     * Device image; may be user generated, defaults to device type when UGC is unavailable. A large
     * device image is suitable for use in the larger circle views on the device control screens.
     */
    DEVICE_LARGE,

    /**
     * Small device image; may be user generated, defaults to device type when UGC is unavailable. A
     * small device image is suitable for use in the smaller circle views present on the device
     * listing screens.
     */
    DEVICE_SMALL,

    /**
     * Blurred background image displayed on device control screens. Typically, this value is the
     * same as the DEVICE_LARGE image, however will resolve the place image when a user-generated
     * device image is not available.
     */
    DEVICE_BACKGROUND,

    /**
     * A device type image reflects the "devTypeHint" of the device and
     * resolves to an illustration of that type, i.e., "switch", "thermostat", etc.
     */
    DEVICE_TYPE_LARGE,

    /**
     * A device type image reflects the "devTypeHint" of the device and
     * resolves to an illustration of that type, i.e., "switch", "thermostat", etc.
     */
    DEVICE_TYPE_SMALL,

    /**
     * A vendor and model specific illustration of a given device in the product catalog. Small
     * enough to be suitable for the smaller circular views present in the product catalog listings.
     */
    PRODUCT_SMALL,

    /**
     * A pairing instruction illustration. Suitable for large circle views present in the pairing
     * step screens.
     */
    PAIRING_LARGE,

    /**
     * A reconnection instruction illustration. Suitable for large circle views present in the
     * reconnect/reconfigure step screens.
     */
    RECONNECT_LARGE,

    /**
     * A vendor/brand logo illustration. Suitable for small circle views present in the device
     * catalog listings.
     */
    BRAND_SMALL,

    /**
     * A device category illustration matching one of the device categories in the product catalog
     * listings. Device categories are specified within {@link DeviceCategory}
     */
    DEVICE_CATEGORY_SMALL,

    /**
     * A photograph (user generated or built-in stock) of the current place. Suitable for large
     * circle views on the dashboard and for wallpapers.
     */
    PLACE,

    /**
     * Not a category per-se, but an indicator that the image is any Drawable resource in the
     * application.
     */
    DRAWABLE,

    /**
     * A small user-generated photograph or stock avatar illustration of a person, suitable for
     * list views and other small icons.
     */
    PERSON,

    /**
     * A large user-generated photograph or stock avatar illustration of a person, suitable for
     * detailed views.
     */
    PERSON_LARGE,

    /**
     * A blurred image of a person as displayed on some person settings screens; typically this will
     * be the photo taken by the user. If the user has not taken a photo, the place photo will be
     * used instead.
     */
    PERSON_BACKGROUND,

    /**
     * An icon representing a scene action template (i.e., "Open & Close Vents"). These are small-
     * circle images for use in list views.
     */
    SCENE_ACTION_TEMPLATE,


    /**
     * An icon representing a scene category (i.e., the squared icons for "Wake up", "Vacation",
     * "Custom", etc.). These are small images for use in list views.
     */
    SCENE_CATEGORY,


    /**
     * A small photo (typically user generated) of a pet that is assigned or associated with a pet door
     * smart-key. Useful for list views.
     */
    PET_SMALL,

    /**
     * A large photo (typically user generated) of a pet that is associated with or assigned to a pet
     * door. Useful for large circle views.
     */
    PET_LARGE;

    public boolean supportsUserGeneratedImagery () {
        return this == DEVICE_LARGE || this == DEVICE_SMALL || this == PLACE || this == PERSON || this == PERSON_LARGE;
    }
}
