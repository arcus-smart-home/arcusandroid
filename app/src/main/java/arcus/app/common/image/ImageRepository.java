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

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.app.ArcusApplication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Singleton API that represents an image repository. Provides methods for saving, retrieving and
 * determining the existence of user-generated images.
 *
 * Note that this class only deals with user-generated imagery.
 */
public class ImageRepository {

    private final static Logger logger = LoggerFactory.getLogger(ImageRepository.class);

    private ImageRepository() {}

    public static Uri saveImage (@NonNull Context context, @NonNull Bitmap image, ImageCategory category, String placeId, String imageId) {

        String fileIdentifier = getImageFilename(category, placeId, imageId);
        Uri fileUri = uriForImage(context, category, placeId, imageId);

        logger.trace("Saving image to " + fileUri);
        try {
            FileOutputStream fos = new FileOutputStream(new File(context.getFilesDir(), fileIdentifier));
            image.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            logger.warn("Error occurred while writing image to local storage: " + e.getMessage());
        }

        return fileUri;
    }

    public static Uri getUriForImage(@NonNull Context context, ImageCategory category, String placeId, String imageId) {
        Uri imageUri = uriForImage(context, category, placeId, imageId);
        logger.trace("Loading image from " + imageUri);
        return imageUri;
    }

    public static String getImageFilename(@Nullable ImageCategory category, @Nullable String placeId, @Nullable String imageId) {
        StringBuilder builder = new StringBuilder();

        if (category != null) {
            builder.append(category);
        }

        if (placeId != null) {
            if (builder.length() > 0) {
                builder.append("-");
            }
            builder.append(placeId);
        }

        if (imageId != null) {
            if (builder.length() > 0) {
                builder.append("-");
            }
            builder.append(imageId);
        }

        builder.append(".png");
        return builder.toString();
    }

    public static boolean imageExists(Context context, ImageCategory category, String placeId, String imageId) {
        File imageFile = new File(ArcusApplication.getContext().getFilesDir(), getImageFilename(category, placeId, imageId));
        logger.trace("Image exists: " + imageFile.exists() + " at " + Uri.fromFile(imageFile));
        return imageFile.exists();
    }

    private static Uri uriForImage (Context context, ImageCategory category, String placeId, String imageId) {
        return Uri.fromFile(new File(ArcusApplication.getContext().getFilesDir(), getImageFilename(category, placeId, imageId)));
    }
}
