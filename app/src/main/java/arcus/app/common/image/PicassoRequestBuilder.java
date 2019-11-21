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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import arcus.app.common.image.picasso.transformation.AlphaPreset;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Transformation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


public class PicassoRequestBuilder<T extends ImageRequestExecutor> {

    private final static Logger logger = LoggerFactory.getLogger(PicassoRequestBuilder.class);

    @Nullable
    final Context context;
    final ViewBackgroundTarget wallpaperTarget;

    ImageCategory category;
    Object locationHint;
    boolean fit, centerCrop, invalidateCache, centerInside, useAsWallpaper, noUserGeneratedImagery;
    @NonNull
    List<Transformation> transform = new ArrayList<>();
    @NonNull
    List<Transformation> stockImageTransforms = new ArrayList<>();
    @NonNull
    List<Transformation> ugcImageTransforms = new ArrayList<>();
    Integer errorDrawableResId;
    Integer placeholderResId;
    @Nullable
    ImageView targetView;
    @Nullable
    Target target;
    Integer targetWidth, targetHeight;
    Float rotation;
    AlphaPreset wallpaperOverlay;
    long requestExecutionStartTime;
    ImageSuccessCallback successCallback;

    final static Object tag = "arcus-image";

    public PicassoRequestBuilder (@Nullable Context context, ViewBackgroundTarget wallpaperTarget) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null.");
        }

        this.wallpaperTarget = wallpaperTarget;
        this.context = context;
    }

    public PicassoRequestBuilder (Context context, ViewBackgroundTarget wallpaperTarget, ImageCategory category, Object locationHint) {
        this.context = context;
        this.category = category;
        this.locationHint = locationHint;
        this.wallpaperTarget = wallpaperTarget;
    }

    /**
     * Suppress any user generated imagery for the current request and instead return only the
     * platform-supplied image/illustration. Has no effect when placing an image category that
     * doesn't support user generated content.
     *
     * @return
     */
    @NonNull
    public PicassoRequestBuilder noUserGeneratedImagery() {
        this.noUserGeneratedImagery = true;
        return this;
    }

    /**
     * Applies the given transformation to the requested image. This build step may be invoked
     * multiple times; each transform will be applied in the request sequence to the produced
     * image.
     *
     * @param transform
     * @return
     */
    @NonNull
    public PicassoRequestBuilder withTransform (Transformation transform) {
        this.transform.add(transform);
        return this;
    }

    /**
     * Applies the given transformation to the requested image at the front of the transform
     * queue. See {@link #withTransform(Transformation)}.
     *
     * @param transform
     * @return
     */
    @NonNull
    public PicassoRequestBuilder withFirstTransform(Transformation transform) {
        this.transform.add(0, transform);
        return this;
    }

    /**
     * Applies the given transformation to the requested image if the enabled flag is true.
     * @param transform
     * @param enabled
     * @return
     */
    @NonNull
    public PicassoRequestBuilder withTransform (Transformation transform, boolean enabled) {
        if (enabled) {
            this.transform.add(transform);
        }
        return this;
    }

    /**
     * Applies the given transform if and only if the request image is NOT user generated. This is
     * useful for applying transformations that are applicable only to the stock illustrations
     * (for example, inverting a black and white illustration).
     *
     * @param transform
     * @return
     */
    @NonNull
    public PicassoRequestBuilder withTransformForStockImages(Transformation transform) {
        this.stockImageTransforms.add(transform);
        return this;
    }

    /**
     * Applies the given transform if and only if the request image is user generated. This is
     * useful for applying transformations that are applicable only to the user-generated photographs
     * (for example, applying a circle transformation).
     *
     * @param transform
     * @return
     */
    @NonNull
    public PicassoRequestBuilder withTransformForUgcImages(Transformation transform) {
        this.ugcImageTransforms.add(transform);
        return this;
    }

    /**
     * Puts the provided drawable resource in lieu of the requested image if an error prevents the
     * system from producing the requested image.
     *
     * @param errorDrawableResId
     * @return
     */
    @NonNull
    public PicassoRequestBuilder withError(int errorDrawableResId) {
        this.errorDrawableResId = errorDrawableResId;
        return this;
    }

    /**
     * Temporarily puts the provided drawable resource while the requested image is being prepared.
     *
     * @param placeholderResId
     * @return
     */
    @NonNull
    public PicassoRequestBuilder withPlaceholder (int placeholderResId) {
        this.placeholderResId = placeholderResId;
        return this;
    }

    /**
     * Specify a callback that should be fired after the requested image has been placed.
     * @param successCallback
     * @return
     */
    @NonNull
    public PicassoRequestBuilder withSuccessCallback (ImageSuccessCallback successCallback) {
        this.successCallback = successCallback;

        if (this.wallpaperTarget != null) {
            this.wallpaperTarget.setSuccessCallback(successCallback);
        }

        return this;
    }

    /**
     * Scale the image so that it fits the bounds of the target view. Cannot be used when placing
     * the image into a target with {@link #into(Target)}.
     * @return
     */
    @NonNull
    public PicassoRequestBuilder fit () {
        this.fit = true;
        return this;
    }

    /**
     * When cropping the image to fit the bounds of a view, crop the image such that its center
     * appears in the center of the target view. Cannot be used when placing
     * the image into a target with {@link #into(Target)}.
     * @return
     */
    @NonNull
    public PicassoRequestBuilder centerCrop () {
        this.centerCrop = true;
        return this;
    }

    @NonNull
    public PicassoRequestBuilder centerInside () {
        this.centerInside = true;
        return this;
    }

    @NonNull
    public PicassoRequestBuilder invalidateCache () {
        this.invalidateCache = true;
        return this;
    }

    @NonNull
    public PicassoRequestBuilder resize (int targetWidth, int targetHeight) {
        this.targetWidth = targetWidth;
        this.targetHeight = targetHeight;
        return this;
    }

    @NonNull
    public PicassoRequestBuilder rotate (float rotation) {
        this.rotation = rotation;
        return this;
    }

    /**
     * In addition to placing the image into a target or view, also blur and alpha-overlay the
     * image before placing it into the background of the wallpaper view. Use {@link ImageManager#setWallpaperView(Context, View)}
     * to specify which view provides the wallpaper target.
     * @param alphaOverlay
     * @return
     */
    @NonNull
    public PicassoRequestBuilder useAsWallpaper (AlphaPreset alphaOverlay) {
        this.useAsWallpaper = true;
        this.wallpaperOverlay = alphaOverlay;
        return this;
    }

    /**
     * Specifies the ImageView into which the final image, placeholder and error images should be
     * drawn.
     *
     * This method must be the last invocation in the builder; it causes the image to be fetched
     * and prepared as currently built.
     *
     * @param targetView
     */
    @NonNull
    public T into (@Nullable ImageView targetView) {
        if (targetView == null) {
            throw new IllegalArgumentException("Target view cannot be null.");
        }

        this.targetView = targetView;
        return (T) this;
    }

    /**
     * Specifies a Picasso target into which the image should be delivered. DANGER: This method
     * has the potential to introduce lots of non-deterministic bugs into the app. The target object
     * provided as an argument MUST be retained using a strong reference from the caller. DO NOT create
     * annonymous instances of target or otherwise create a target instance as a local method variable
     * as these will be garbage collected before the image manager/Picasso is able to deliver the
     * image.
     *
     * @param target
     * @return
     */
    @NonNull
    public T into (@Nullable Target target) {
        if (target == null) {
            throw new IllegalArgumentException("Target cannot be null.");
        }

        this.target = target;
        return (T) this;
    }

    /**
     * Specifies that the prepared image should be placed into the wallpaper after applying a blur
     * and alpha-overlay transformation. (When invoking this method is unnecessary to specify these
     * transformations explicity--they will be applied automatically.)
     * 
     * @param alphaOverlay
     * @return
     */
    @NonNull
    public T intoWallpaper(AlphaPreset alphaOverlay) {
        this.useAsWallpaper = true;
        this.wallpaperOverlay = alphaOverlay;
        return (T) this;
    }

    protected void log(String message, Object... variables) {
        if (context != null && Picasso.with(context).isLoggingEnabled()) {
            logger.debug(message, variables);
        }
    }

    @Override
    public String toString() {
        return "PicassoRequestBuilder{" +
                "category=" + category +
                ", locationHint=" + locationHint +
                ", fit=" + fit +
                ", centerCrop=" + centerCrop +
                ", invalidateCache=" + invalidateCache +
                ", centerInside=" + centerInside +
                ", useAsWallpaper=" + useAsWallpaper +
                ", noUserGeneratedImagery=" + noUserGeneratedImagery +
                ", transform=" + transform +
                ", stockImageTransforms=" + stockImageTransforms +
                ", ugcImageTransforms=" + ugcImageTransforms +
                ", errorDrawableResId=" + errorDrawableResId +
                ", placeholderResId=" + placeholderResId +
                ", targetView=" + targetView +
                ", target=" + target +
                ", targetWidth=" + targetWidth +
                ", targetHeight=" + targetHeight +
                ", rotation=" + rotation +
                ", wallpaperOverlay=" + wallpaperOverlay +
                ", requestExecutionStartTime=" + requestExecutionStartTime +
                '}';
    }
}
