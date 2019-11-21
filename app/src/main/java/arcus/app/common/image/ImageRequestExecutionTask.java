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

import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ImageView;

import arcus.app.ArcusApplication;
import arcus.app.common.image.picasso.transformation.AlphaOverlayTransformation;
import arcus.app.common.image.picasso.transformation.BlurTransformation;
import arcus.app.common.utils.PreferenceUtils;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;

import java.io.File;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An asynchronous execution of an image request created through the ImageManager. Note that not
 * all requests need to be handled asynchronously (at this level). Picasso will automatically
 * handle requests on a worker thread. However, in some cases, we can't create the Picasso request
 * without first doing some long-running activities in the ImageLocator (like querying the
 * {@link ImageRepository} over the network).
 *
 */
public class ImageRequestExecutionTask extends AsyncTask<PicassoRequestBuilder, Integer, ImageLocationSpec> {

    private final static ConcurrentHashMap<ImageView, ImageRequestExecutionTask> pendingRequests = new ConcurrentHashMap<>();
    private PicassoRequestBuilder builder;

    /**
     * Executes in a background thread and is responsible for resolving the location of an image
     * given an ImageLocationSpec as input.
     *
     * Why does this need to occur on a separate thread? Because the image resolution process may
     * require making one or more network calls (to check the existence of an image on the static
     * resource server).
     *
     * @param builders A single element array containing the b
     * @return
     */
    @NonNull
    @Override
    protected ImageLocationSpec doInBackground(PicassoRequestBuilder... builders) {
        this.builder = builders[0];
        builder.log("+{}ms Starting to execute image location resolution on AsyncTask thread.", getRequestDuration());

        if (this.builder.targetView != null) {
            pendingRequests.put(this.builder.targetView, this);
        }

        ImageLocator locator = ImageLocator.locate(builder.context, builder.category)
                .using(builder.locationHint);

        if (builder.noUserGeneratedImagery) {
            locator.noUserGeneratedImagery();
        }

        ImageLocationSpec location = locator.execute();
        builder.log("+{}ms Resolved location of {} as {}", getRequestDuration(), builder, location);
        return location;
    }

    /**
     * Executes on completion of the {@link #doInBackground(PicassoRequestBuilder[])} handler on
     * the main thread.
     *
     * This code is responsible for "unpacking" the builder and producing a Picasso
     * {@link RequestCreator} object from it and then executing that request through Picasso. (Note
     * that Picasso uses its own threading model on which to execute its requests.)
     *
     * Nothing in this method should be long running or require network access.
     *
     * @param imageLocation
     */
    @Override
    protected void onPostExecute(@Nullable ImageLocationSpec imageLocation) {

        builder.log("+{}ms Preparing Picasso request for {}", getRequestDuration(), imageLocation);

        if (builder.targetView == null && builder.target == null && builder.useAsWallpaper == false) {
            throw new IllegalStateException("No target or view specified for resulting image. Please call .into() or .useAsWallpaper() before .execute()");
        }

        if (imageLocation == null) {
            if (builder.errorDrawableResId != null) {
                imageLocation = new ImageLocation(builder.errorDrawableResId);
            } else if (builder.placeholderResId != null) {
                imageLocation = new ImageLocation(builder.placeholderResId);
            } else {
                // TODO: Log error message; no image can be found, no image to fallback to.
                throw new IllegalStateException("Image can't be located; no fallback image exists. Category: " + builder.category + " Hint: " + builder.locationHint);
            }
        }

        Picasso picasso = Picasso.with(ArcusApplication.getContext());
        picasso.setLoggingEnabled(PreferenceUtils.isPicassoInDebugMode());
        picasso.setIndicatorsEnabled(PreferenceUtils.arePicassoIndicatorsEnabled());

        // Invalidate Picasso's cache if so desired
        if (builder.invalidateCache || PreferenceUtils.isPicassoCacheDisabled()) {
            if (imageLocation.getLocation() instanceof File) {
                picasso.invalidate((File) imageLocation.getLocation());
            } else if (imageLocation.getLocation() instanceof String) {
                picasso.invalidate((String) imageLocation.getLocation());
            } else if (imageLocation.getLocation() instanceof Uri) {
                picasso.invalidate((Uri) imageLocation.getLocation());
            }
        }

        RequestCreator request = intoPicasso(imageLocation, picasso, builder.placeholderResId, builder.errorDrawableResId);
        if (request == null) {
            builder.log("Aborting image placement request because requested location is null.");
            return;
        }

        // Apply transforms (may be an empty list)
        request.transform(builder.transform);

        // Apply transforms intended only for stock photographs
        if (!imageLocation.isUserGenerated()) {
            request.transform(builder.stockImageTransforms);
        }

        // Apply transforms intended only for UGC images
        if (imageLocation.isUserGenerated()) {
            request.transform(builder.ugcImageTransforms);
        }

        if (builder.tag != null) {
            request.tag(builder.tag);
        }

        // Apply fit and center cropping
        if(builder.fit) {
            request.fit();
        }
        if (builder.centerCrop) {
            request.centerCrop();
        }
        if (builder.centerInside) {
            request.centerInside();
        }
        if (builder.targetHeight != null && builder.targetWidth != null) {
            request.resize(builder.targetWidth, builder.targetHeight);
        }
        if (builder.rotation != null) {
            request.rotate(builder.rotation);
        }

        // Setup the error and placeholder images if they were specified
        if (builder.errorDrawableResId != null) {
            request.error(builder.errorDrawableResId);
        }
        if (builder.placeholderResId != null) {
            request.placeholder(builder.placeholderResId);
        }

        if (this.builder.targetView != null) {
            if (this != pendingRequests.get(this.builder.targetView)) {
                return;
            }

            pendingRequests.remove(this.builder.targetView);
        }


        // Finally, inject the image into the target or view
        if (builder.targetView != null) {
            builder.log("+{}ms Requesting Picasso place {} into target view.", getRequestDuration(), imageLocation);
            request.into(builder.targetView, new Callback() {
                @Override
                public void onSuccess() {
                    if (builder.successCallback != null) {
                        builder.successCallback.onImagePlacementSuccess();
                    }
                }

                @Override
                public void onError() {}
            });
        } else if (builder.target != null) {
            builder.log("+{}ms Requesting Picasso place {} into target.", getRequestDuration(), imageLocation);
            request.into(builder.target);
        }

        // Apply the image as a wallpaper, if requested
        if (builder.useAsWallpaper && builder.wallpaperTarget != null) {
            RequestCreator wallpaperRequest = intoPicasso(imageLocation, Picasso.with(builder.context));

            if (builder.wallpaperOverlay != null) {
                wallpaperRequest.transform(new AlphaOverlayTransformation(builder.wallpaperOverlay));
            }

            wallpaperRequest.transform(new BlurTransformation(builder.context));

            if (!imageLocation.isUserGenerated())
                wallpaperRequest.transform(builder.stockImageTransforms);
            else
                wallpaperRequest.transform(builder.transform);

            builder.log("+{}ms Requesting Picasso place {} into wallpaper.", getRequestDuration(), imageLocation);
            wallpaperRequest.into(builder.wallpaperTarget);
        }
    }

    @Nullable
    private RequestCreator intoPicasso (@NonNull  ImageLocationSpec locationSpec, @NonNull Picasso picasso, @Nullable Integer... fallbackImageResIds) {

        Object location = locationSpec.getLocation();

        // Special case: No image location specified. Something's busted. (Missing SSR URL?)
        if (location == null) {

            // Try to use a placeholder image instead (if one has been defined)
            for (Integer thisFallback : fallbackImageResIds) {
                if (thisFallback != null && thisFallback != 0) {
                    builder.log("No image location specified for placement; falling back to image resource ID {}", thisFallback);

                    location = thisFallback;
                    break;
                }
            }

            // ... otherwise, abort
            if (location == null) {
                return null;
            }
        }

        if (location instanceof String) {
            return picasso.load((String) location);
        } else if (location instanceof Integer) {
            return picasso.load((int) location);
        } else if (location instanceof File) {
            return picasso.load((File) location);
        } else if (location instanceof Uri) {
            return picasso.load((Uri) location);
        } else {
            throw new IllegalArgumentException("Unimplemented. Don't know how to load an image specified by " + location.getClass().getSimpleName());
        }
    }

    private long getRequestDuration() {
        return System.currentTimeMillis() - builder.requestExecutionStartTime;
    }

}
