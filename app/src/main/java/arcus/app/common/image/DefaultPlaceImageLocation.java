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

import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.utils.PreferenceUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;


public class DefaultPlaceImageLocation implements ImageLocationSpec {

    private static final Logger logger = LoggerFactory.getLogger(DefaultPlaceImageLocation.class);

    private final String placeId;
    private final String[] defaultImages = {
            /*  House images, not nature.
             *  Keeping assets 0-5 so background is only updated for fresh install */
            "default_place_background_6",
            "default_place_background_7",
            "default_place_background_8",
            "default_place_background_9",
            "default_place_background_10",
            "default_place_background_11"
    };

    public DefaultPlaceImageLocation(String forPlaceId) {
        this.placeId = forPlaceId;
    }

    @Override
    public Object getLocation() {

        // Map of previously assigned place IDs to wallpaper images
        Map<String,String> placeBackgroundMap = PreferenceUtils.getPlaceBackgroundMap();

        // If this place already has a wallpaper assigned, use it
        if (placeBackgroundMap.containsKey(placeId)) {
            return getResourceIdForImage(placeBackgroundMap.get(placeId));
        }

        String nextImage = defaultImages[placeBackgroundMap.keySet().size() % defaultImages.length];
        logger.debug("No default place image assigned for {}; using '{}' as wallpaper", placeId, nextImage);

        placeBackgroundMap.put(placeId, nextImage);
        PreferenceUtils.putPlaceBackgroundMap(placeBackgroundMap);

        return getResourceIdForImage(nextImage);
    }

    @Override
    public boolean isUserGenerated() {
        // Never user generated, by definition
        return false;
    }

    private int getResourceIdForImage (String imageName) {

        int resourceId;

        try {
            resourceId = ArcusApplication.getContext().getResources().getIdentifier(imageName, "drawable", ArcusApplication.getContext().getPackageName());
        } catch (Exception e) {
            logger.error("Failed to find image resource for default place image called '{}'", imageName);
            resourceId = 0;
        }

        // If all hell breaks loose, use the (new) default image
        return resourceId == 0 ? R.drawable.default_place_background_6 : resourceId;
    }

    @Override
    public String toString() {
        return "DefaultPlaceImageLocation{" +
                "placeId='" + placeId + '\'' +
                ", defaultImages=" + Arrays.toString(defaultImages) +
                '}';
    }
}
