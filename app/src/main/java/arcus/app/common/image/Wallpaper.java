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

import arcus.cornea.provider.PlaceModelProvider;
import com.iris.client.model.DeviceModel;
import arcus.app.common.image.picasso.transformation.AlphaPreset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A wallpaper specification model. Defines the wallpaper image category, location hint (to find
 * the request image) and the alpha-overlay preset that should be applied to it.
 */
public class Wallpaper {

    private final static Logger logger = LoggerFactory.getLogger(Wallpaper.class);

    private final ImageCategory imageCategory;
    private AlphaPreset alphaPreset;
    private String personId;
    private String placeId;
    private int drawableResId;
    private DeviceModel deviceModel;

    private Wallpaper (ImageCategory imageCategory) {
        this.imageCategory = imageCategory;
    }

    @NonNull
    public static Wallpaper ofPerson (String personId) {
        Wallpaper wallpaper = new Wallpaper(ImageCategory.PERSON);
        wallpaper.personId = personId;

        return wallpaper;
    }

    @NonNull
    public static Wallpaper ofPlace (String placeId) {
        Wallpaper wallpaper = new Wallpaper(ImageCategory.PLACE);
        wallpaper.placeId = placeId;

        return wallpaper;
    }

    @NonNull
    public static Wallpaper ofCurrentPlace () {
        Wallpaper wallpaper = new Wallpaper(ImageCategory.PLACE);

        if (PlaceModelProvider.getCurrentPlace().isLoaded()) {
            wallpaper.placeId = PlaceModelProvider.getCurrentPlace().get().getId();
        } else {
            logger.error("Current place model is not loaded; using default wallpaper instead.");
            return ofDefaultWallpaper();
        }

        return wallpaper;
    }

    @NonNull
    public static Wallpaper ofDevice (DeviceModel deviceModel) {
        Wallpaper wallpaper = new Wallpaper(ImageCategory.DEVICE_BACKGROUND);
        wallpaper.placeId = PlaceModelProvider.getCurrentPlace().get().getId();
        wallpaper.deviceModel = deviceModel;

        return wallpaper;
    }

    @NonNull
    public static Wallpaper ofDevice (String placeId, DeviceModel deviceModel) {
        Wallpaper wallpaper = new Wallpaper(ImageCategory.DEVICE_BACKGROUND);
        wallpaper.placeId = placeId;
        wallpaper.deviceModel = deviceModel;

        return wallpaper;
    }

    @NonNull
    public static Wallpaper ofDefaultWallpaper () {
        Wallpaper wallpaper = new Wallpaper(ImageCategory.DRAWABLE);
        wallpaper.drawableResId = ImageManager.getDefaultWallpaperResId();

        return wallpaper;
    }

    @NonNull
    public Wallpaper darkened () {
        this.alphaPreset = AlphaPreset.DARKEN;
        return this;
    }

    @NonNull
    public Wallpaper lightend () {
        this.alphaPreset = AlphaPreset.LIGHTEN;
        return this;
    }

    public ImageCategory getImageCategory () {
        return this.imageCategory;
    }

    public AlphaPreset getAlphaPreset () {
        return this.alphaPreset;
    }

    public String getPlaceId () {
        return placeId;
    }

    public String getPersonId () {
        return personId;
    }

    public DeviceModel getDeviceModel() {
        return deviceModel;
    }

    public int getDrawableResId () { return drawableResId; }
}
