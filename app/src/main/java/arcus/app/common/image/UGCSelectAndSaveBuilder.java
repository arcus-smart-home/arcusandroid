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
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.annotation.NonNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builds a complex, chained event request for user-generated content in one Java statement. When
 * dealing with user generated imagery, the process is somewhat complex:
 *
 * 1. When {@link #fromCameraOrGallery()} is requested, a dialog needs to appear letting the user choose
 * their image source.
 * 2. An Android {@link android.content.Intent} is fired to launch the camera or gallery application.
 * 3. When the user completes that intent, the app is resumed and the {@link UGCImageIntentResultHandler}
 * is fired to process the result. This builder registers itself as a listener through the {@link UGCImageSelectionListener}
 * interface.
 * 4. The user generated image is then saved to the {@link ImageRepository}.
 * 5. When the save process has completed, this builder is notified through the {@link ImageSaveListener}
 * interface, which, in turn, notifies the client through the {@link UGCImageSelectionListener} interface.
 */
public class UGCSelectAndSaveBuilder extends PicassoRequestBuilder<UGCSelectAndSaveBuilder> implements ImageRequestExecutor {

    @FunctionalInterface
    public interface SaveLocationListener {
        void savedTo(Uri fileUri);
    }

    private final Logger logger = LoggerFactory.getLogger(UGCSelectAndSaveBuilder.class);

    private String placeId, imageId;
    private boolean fromCamera, fromGallery;
    private UGCImageSelectionListener listener;
    private SaveLocationListener saveLocationListener;

    public UGCSelectAndSaveBuilder(Context context, ViewBackgroundTarget wallpaperTarget, String placeId, String imageId, ImageCategory category) {
        super(context, wallpaperTarget);

        this.placeId = placeId;
        this.imageId = imageId;
        this.category = category;

        // Always invalidate the cache when handling UGC
        super.invalidateCache = true;
    }

    @NonNull
    public UGCSelectAndSaveBuilder fromCameraOrGallery() {
        this.fromCamera = true;
        this.fromGallery = true;
        return this;
    }

    @NonNull
    public UGCSelectAndSaveBuilder fromCamera () {
        this.fromCamera = true;
        return this;
    }

    @NonNull
    public UGCSelectAndSaveBuilder fromGallery() {
        this.fromGallery = true;
        return this;
    }

    @NonNull
    public UGCSelectAndSaveBuilder withCallback (UGCImageSelectionListener listener) {
        this.listener = listener;
        return this;
    }

    @NonNull
    public UGCSelectAndSaveBuilder withSaveLocationListener(SaveLocationListener listener) {
        this.saveLocationListener = listener;
        return this;
    }

    public void execute() {

        final PicassoRequestBuilder self = this;

        logger.debug("Executing chained image request.");
        UGCImageSelectionBuilder builder = new UGCImageSelectionBuilder((Activity) context, category, placeId, imageId);

        // Select the image from the camera or gallery
        if (fromCamera && fromGallery) {
            builder = builder.fromCameraOrGallery();
        } else if (fromCamera) {
            builder = builder.fromCamera();
        } else if (fromGallery) {
            builder = builder.fromGallery();
        } else {
            throw new IllegalStateException("UGC chained image request made without specifying source. Please call .fromCamera(), .fromGallery, or .fromCameraOrGallery() first.");
        }

        // This callback fires as soon as the gallery or camera intent finishes successfully
        builder.withCallback(new UGCImageSelectionListener() {
            @Override
            public void onUGCImageSelected(final Bitmap selectedImage) {
                logger.debug("UGC image ready from intent; saving to local app storage.");
                new ImageSaveBuilder(context, category, selectedImage, placeId, imageId)

                        // This callback fires as soon as the image has been saved
                        .withCallback(new ImageSaveListener() {

                            @Override
                            public void onImageSaveComplete(boolean success, Bitmap image, final Uri fileUri) {
                                logger.debug("UGC image file saved to " + fileUri + " with success: " + success);

                                // Invoke Picasso to place the saved image into the requested view
                                locationHint = fileUri;
                                new ImageRequestExecutionTask().execute(self);

                                // Notify the client that we're done with the UGC process
                                if (listener != null) {
                                    listener.onUGCImageSelected(selectedImage);
                                }

                                if (success && saveLocationListener != null) {
                                    saveLocationListener.savedTo(fileUri);
                                }
                            }
                        }).commit();
            }
        }).execute();
    }
}
