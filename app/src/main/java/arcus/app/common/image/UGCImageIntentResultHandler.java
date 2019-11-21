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

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.app.common.image.picasso.transformation.RotateTransformation;
import arcus.app.common.utils.ImageUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Delegate class to handle user-generate image intent results. When the camera or gallery intent
 * completes, it returns to this app and passes us information about the selected (or taken) image.
 * This class is responsible for handling that callback.
 */
public class UGCImageIntentResultHandler {

    private final static int MAX_IMAGE_WIDTH_DP = 500;
    private final static int MAX_IMAGE_HEIGHT_DP = 500;

    private static final Logger logger = LoggerFactory.getLogger(UGCImageIntentResultHandler.class);
    private static final UGCImageIntentResultHandler instance = new UGCImageIntentResultHandler();
    private UGCImageSelectionListener receiver;

    private UGCImageIntentResultHandler() {}

    @NonNull
    public static UGCImageIntentResultHandler getInstance() {
        return instance;
    }

    public void registerReceiver (UGCImageSelectionListener receiver) {
        this.receiver = receiver;
    }

    public void onImageGallerySelect (@NonNull Activity activity, int resultCode, @Nullable Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            logger.debug("User completed gallery intent.");

            if (data == null) {
                logger.error("Intent data from gallery activity is null.");
                return;
            }

            Uri imageUri = data.getData();
            if (imageUri == null) {
                logger.error("Image URL from gallery activity is null.");
                return;
            }

            Bitmap image = loadScaledBitmapFromUri(activity, imageUri, ImageUtils.dpToPx(activity, 190), ImageUtils.dpToPx(activity, 190));

            if (image == null) {
                logger.error("Failed to open input stream for selected image {}", imageUri);
                return;
            }

            logger.debug("Loaded selected image from gallery; normalizing rotation and size");
            image = normalizeImage(imageUri.getPath(), image);

            if (this.receiver != null) {
                this.receiver.onUGCImageSelected(image);
            }
            logger.debug("Custom Image: KB: => [{}], Height: [{}], Width: [{}]", (image.getByteCount() / 1024), image.getHeight(), image.getWidth());
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            logger.debug("User canceled gallery intent.");
        }
        else {
            logger.debug("Received error result code from gallery intent: " + resultCode);
        }
    }

    public void onCameraImageCapture (Activity activity, int resultCode, Intent data) {

        if (resultCode == Activity.RESULT_OK) {
            logger.debug("User completed camera intent.");

            File imageFile = new File(Environment.getExternalStorageDirectory(), UGCImageSelectionBuilder.LAST_CAMERA_IMAGE);
            if (!imageFile.exists()) {
                logger.error("Camera intent failed to create image file");
                return;
            }

            Bitmap image = loadScaledBitmapFromFile(imageFile.getAbsolutePath(), ImageUtils.dpToPx(activity, 190), ImageUtils.dpToPx(activity, 190));
            if (image == null) {
                logger.warn("Image captured from camera is null or cannot be loaded.");
                return;
            }

            logger.debug("Got image from camera intent; normalizing rotation and size");
            image = normalizeImage(imageFile.getAbsolutePath(), image);

            if (this.receiver != null) {
                this.receiver.onUGCImageSelected(image);
            }
        }
        else if (resultCode == Activity.RESULT_CANCELED) {
            logger.debug("User canceled camera intent.");
        }
        else {
            logger.debug("Received error result code from camera intent: " + resultCode);
        }
    }

    public void onCameraImageCapture (Activity activity, File imageFile) {
        if (!imageFile.exists()) {
                logger.error("Provided file does not exist." + imageFile);
                return;
            }

                Bitmap image = loadScaledBitmapFromFile(imageFile.getAbsolutePath(), ImageUtils.dpToPx(activity, 190), ImageUtils.dpToPx(activity, 190));
        if (image == null) {
                logger.warn("Image captured from camera is null or cannot be loaded.");
                return;
            }

                logger.debug("Got image from camera intent; normalizing rotation and size");
        image = normalizeImage(imageFile.getAbsolutePath(), image);

                if (this.receiver != null) {
                this.receiver.onUGCImageSelected(image);
            }
    }

    public static Bitmap loadScaledBitmapFromFile(String file, int reqWidth, int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(file, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateImageSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(file, options);
    }

    public static Bitmap loadScaledBitmapFromUri(Activity activity, Uri imageUri, int reqWidth, int reqHeight) {

        try {
            InputStream imageStream = activity.getContentResolver().openInputStream(imageUri);

            // First decode with inJustDecodeBounds=true to check dimensions
            final BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(imageStream, null, options);

            if (imageStream != null) {
                imageStream.close();
            }

            // Calculate inSampleSize
            options.inSampleSize = calculateImageSampleSize(options, reqWidth, reqHeight);

            // Decode bitmap with inSampleSize set
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inJustDecodeBounds = false;

            imageStream = activity.getContentResolver().openInputStream(imageUri);
            return BitmapFactory.decodeStream(imageStream, null, options);
        } catch (IOException e) {
            logger.error("An error occurred decoding image stream: {}", e);
            return null;
        }
    }

    public static int calculateImageSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((height / inSampleSize) > reqHeight && (width / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private Bitmap normalizeImage(String filePath, Bitmap image) {

        try {
            ExifInterface ei = new ExifInterface(filePath);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

            Bitmap transformedImage;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    logger.debug("Correcting image rotation 90 degrees.");
                    transformedImage = new RotateTransformation(90).transform(image);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    logger.debug("Correcting image rotation 180 degrees.");
                    transformedImage = new RotateTransformation(180).transform(image);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    logger.debug("Correcting image rotation 270 degrees.");
                    transformedImage = new RotateTransformation(270).transform(image);
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                    logger.debug("Image is correctly oriented; no transformation required.");
                    return image;
                default:
                    logger.warn("Image is not correctly oriented, but no correction is implemented for orientation: " + orientation);
                    return image;
            }

            if (image != null) {
                image.recycle();
            }

            return transformedImage;

        } catch (IOException e) {
            logger.error("Failed to read EXIF information on captured photo; image may be rotated.");
        }

        return image;
    }
}
