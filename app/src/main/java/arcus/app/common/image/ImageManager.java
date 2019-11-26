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
import android.app.ActivityManager;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;

import com.iris.client.bean.ActionTemplate;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.ProductModel;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.image.picasso.transformation.AlphaOverlayTransformation;
import arcus.app.common.image.picasso.transformation.AlphaPreset;
import arcus.app.common.image.picasso.transformation.BlurTransformation;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.model.DeviceType;
import arcus.app.subsystems.scenes.catalog.model.SceneCategory;
import okhttp3.OkHttpClient;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * An image management facade; provides a specific API for locating, placing and transforming
 * images within the application.
 *
 * Delegates to the Picasso library for network image handling and asynchronous placement.
 */
public class ImageManager {

    private final static Logger logger = LoggerFactory.getLogger(ImageManager.class);

    private final Context context;
    private static ViewBackgroundTarget wallpaperTarget;
    private static int defaultWallpaperResId = R.drawable.background_1;

    private ImageManager(Context context) {
        this.context = context;
    }

    @NonNull
    public static ImageManager with (Context context) {
        return new ImageManager(context);
    }

    /**
     * Enables or disables the disk cache for the life of the application. This method cannot be
     * called more than once per lifetime of the app and should therefore be called only during
     * application startup; subsequent calls have no effect on the disk cache state.
     *
     * Note that when enabling Piccaso cache indicators, you may still find that some images appear
     * as though they've been loaded from disk. This will be true for any app-packaged drawable or
     * bitmap resource that's placed by Picasso. (These are always "from disk" with or without the
     * presence of a disk cache.) Similarly, it's possible to get green-tagged images when using
     * Picasso to "load" manually pre-loaded BitmapDrawables. 
     *
     * @param diskCacheEnabled
     */
    public static void setConfiguration(Context context, boolean diskCacheEnabled, Integer cacheHeapPercent) {
        try {

            Picasso.Builder builder = new Picasso.Builder((ArcusApplication.getContext()));

            if (diskCacheEnabled) {
                builder.downloader(new OkHttp3Downloader(new OkHttpClient()));
            }

            if (cacheHeapPercent != null) {
                ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
                int memoryClass = am.getMemoryClass();

                int heapSize = (int) ((float)(1024 * 1024 * memoryClass) * ((float) cacheHeapPercent / 100.0));
                builder.memoryCache(new LruCache(heapSize));
                logger.debug("Setting Picasso in-memory LRU cache max size to {} bytes; {}% of heap sized {}", heapSize, cacheHeapPercent, 1024 * 1024 * memoryClass);
            }

            Picasso.setSingletonInstance(builder.build());

        } catch (IllegalStateException e) {
            logger.warn("Picasso setConfiguration() has already been called; ignoring request.");
        }
    }

    /**
     * Sets the view that the ImageManager will use for wallpaper operations. Ideally, an Activity
     * or parent fragment would invoke this method passing a reference to the view that covers the
     * screen. When a user calls {@link PicassoRequestBuilder#intoWallpaper(AlphaPreset)} or
     * {@link PicassoRequestBuilder#useAsWallpaper(AlphaPreset)} the requested image will be blurred
     * and alpha-overlaid then placed into the background of this view.
     *
     * @param context
     * @param view
     */
    public static void setWallpaperView(@NonNull Context context, View view) {
        wallpaperTarget = new ViewBackgroundTarget(view);
    }

    /**
     * Gets the current wallpaper view. Returns null if one has not been set.
     * @return
     */
    @Nullable
    public static View getWallpaperView () {
        if (wallpaperTarget == null) {
            return null;
        }

        return wallpaperTarget.getView();
    }

    /**
     * Specifies the default wallpaper drawable resource id. This image will be used for wallpaper
     * when no other value is available. Typically, this value should represent one of the four
     * swipeable images displayed on the login screen. This value is used for account creation (and
     * other pre-login screen wallpapers). This DOES NOT represent a stock wallpaper
     * image selection (i.e., "blue mountains") and should not be used for screens appearing after
     * a user has logged in.
     *
     * @param wallpaperResId
     */
    public static void setDefaultWallpaperResId (int wallpaperResId) {
        defaultWallpaperResId = wallpaperResId;
    }

    /**
     * Gets the previously specified default wallpaper resource id, or null if one has not been set.
     * @return
     */
    public static int getDefaultWallpaperResId () {
        return defaultWallpaperResId;
    }


    /**
     * Attempts to cancel all pending image placement requests; note that requests that are already
     * being processed by the executor will not be cancelled.
     *
     * @param activity
     */
    public static void cancelAll(Activity activity) {
        Picasso.with(activity).cancelTag(PicassoRequestBuilder.tag);
    }

    /**
     * Puts a Drawable resource (i.e., a static image from the res/ directory) as identified
     * by the given R.drawable... id. In most cases it's preferable to simply place the drawable
     * directly (i.e., via {@link android.widget.ImageView#setImageResource(int)} ) into the view
     * rather than use {@link ImageManager}, however, in cases where a transformation is needed,
     * this method is preferable.
     *
     * @param drawableResourceId
     * @return An ImageManager (builder pattern) instance
     */
    @NonNull
    public ImageRequestBuilder putDrawableResource (int drawableResourceId) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.DRAWABLE, drawableResourceId);
    }

    /**
     * Puts the default wallpaper, that is, the stock photograph last seen by the user on the launch
     * page prior to them clicking "log in" or "sign up".
     *
     * @return An ImageManager (builder pattern) instance
     */
    @NonNull
    public ImageRequestBuilder putDefaultWallpaper () {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.DRAWABLE, getDefaultWallpaperResId());
    }

    /**
     * Puts the device category image image identified by the given DeviceCategory enumerations.
     *
     * @param deviceCategory
     * @return
     */
    @NonNull
    public ImageRequestBuilder putDeviceCategoryImage(@Nullable DeviceCategory deviceCategory) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.DEVICE_CATEGORY_SMALL, deviceCategory);
    }

    /**
     * Puts the device background image. This is the image that should be displayed, blurred,
     * in the background of the device control page identified by the given device model. This will
     * be the same as device image, except when the user has not provided an image in which case
     * this will be the same as the dashboard (home) image.
     *
     * This case addresses the situation where the device image does not match the image blurred
     * behind it.
     *
     * @param deviceModel
     * @return
     */
    @NonNull
    public ImageRequestBuilder putDeviceBackgroundImage(@Nullable DeviceModel deviceModel) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.DEVICE_BACKGROUND, deviceModel);
    }

    /**
     * Puts the device background image. This is the image that should be displayed, blurred,
     * in the background of the device control page identified by the given device model. This will
     * be the same as device image, except when the user has not provided an image in which case
     * this will be the same as the dashboard (home) image.
     *
     * This case addresses the situation where the device image does not match the image blurred
     * behind it.
     *
     * @param hubModel
     * @return
     */
    @NonNull
    public ImageRequestBuilder putDeviceBackgroundImage(@Nullable HubModel hubModel) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.DEVICE_BACKGROUND, hubModel);
    }


    /**
     * Puts a pairing step image given a product catalog id and the step number identifying
     * the illustration.
     *
     * @param productId The product catalog id of the device whose pairing step illustration is
     *                  requested.
     * @param stepNumber The step number of the requested illustration.
     * @return
     */
    @NonNull
    public ImageRequestBuilder putPairingStepImage(@NonNull String productId, @NonNull String stepNumber) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.PAIRING_LARGE, new PairingStepSpecifier(productId, stepNumber));
    }

    /**
     * Puts a reconnect step image given a product catalog id and the step number identifying
     * the illustration.
     *
     * @param productId The product catalog id of the device whose reconnect step illustration is
     *                  requested.
     * @param stepNumber The step number of the requested illustration.
     * @return
     */
    @NonNull
    public ImageRequestBuilder putReconnectStepImage(@NonNull String productId, @NonNull String stepNumber) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.RECONNECT_LARGE, new PairingStepSpecifier(productId, stepNumber));
    }

    /**
     * Puts a small product image, that is, an provided image of a specific product that's
     * useful for list views. These images are (presently) only used in the product catalog when
     * viewing devices listed below the category or brand hierarchy. These images cannot be
     * user-generated and will always be provided from the platform.
     *
     * @param productId The product catalog ID of the request device's image.
     * @return
     */
    @NonNull
    public ImageRequestBuilder putSmallProductImage(@Nullable String productId) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.PRODUCT_SMALL, CorneaUtils.getNormalizedId(productId));
    }

    /**
     * Puts a small product image, that is, an provided image of a specific product that's
     * useful for list views. These images are (presently) only used in the product catalog when
     * viewing devices listed below the category or brand hierarchy. These images cannot be
     * user-generated and will always be provided from the platform.
     *
     * @param productModel The ProductModel representing the product whose image is requested.
     * @return
     */
    @NonNull
    public ImageRequestBuilder putSmallProductImage(@Nullable ProductModel productModel) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.PRODUCT_SMALL, productModel);
    }

    /**
     * Puts the large image associated with the given device. The device image may or may not be
     * user-generated. If the user has provided an image of the given device, it will be used.
     * Otherwise, the "generic" image suppled by the platform will be placed.
     *
     * @param hint A DeviceModel, HubModel or DeviceType (when only the generic image is
     *                    being requested).
     * @return
     */
    @NonNull
    public ImageRequestBuilder putLargeDeviceImage(@Nullable DeviceModel hint) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.DEVICE_LARGE, hint);
    }

    /**
     * Puts the large image associated with the given device. The device image may or may not be
     * user-generated. If the user has provided an image of the given device, it will be used.
     * Otherwise, the "generic" image suppled by the platform will be placed.
     *
     * @param hint A DeviceModel, HubModel or DeviceType (when only the generic image is
     *                    being requested).
     * @return
     */
    @NonNull
    public ImageRequestBuilder putLargeDeviceImage(@Nullable HubModel hint) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.DEVICE_LARGE, hint);
    }

    /**
     * Puts the large image associated with the given device. The device image may or may not be
     * user-generated. If the user has provided an image of the given device, it will be used.
     * Otherwise, the "generic" image supplied by the platform will be placed.
     *
     * @param hint A DeviceModel, HubModel or DeviceType (when only the generic image is
     *                    being requested).
     * @return
     */
    @NonNull
    public ImageRequestBuilder putLargeDeviceImage(@Nullable DeviceType hint) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.DEVICE_LARGE, hint);
    }

    /**
     * Puts the small image associated with the given device. The device image may or may not be
     * user-generated. If the user has provided an image of the given device, it will be used.
     * Otherwise, the "generic" image supplied by the platform will be placed.
     *
     * @param hint A DeviceModel
     * @return
     */
    @NonNull
    public ImageRequestBuilder putSmallDeviceImage(@Nullable DeviceModel hint) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.DEVICE_SMALL, hint);
    }

    /**
     * Puts the small image associated with the given device. The device image may or may not be
     * user-generated. If the user has provided an image of the given device, it will be used.
     * Otherwise, the "generic" image supplied by the platform will be placed.
     *
     * @param hint A DeviceType
     * @return
     */
    @NonNull
    public ImageRequestBuilder putSmallDeviceImage(@Nullable DeviceType hint) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.DEVICE_SMALL, hint);
    }

    /**
     * Puts the brand logo of the specified device. This image is always supplied by the platform
     * and cannot be user generated.
     *
     * @param deviceModel
     * @return
     */
    @NonNull
    public ImageRequestBuilder putBrandImage(DeviceModel deviceModel) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.BRAND_SMALL, deviceModel);
    }

    /**
     * Puts the brand logo for the specified brand name (as specified by the dynamic asset filename
     * convention document on Confluence).
     *
     * @param brandName
     * @return
     */
    @NonNull
    public ImageRequestBuilder putBrandImage(@Nullable String brandName) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.BRAND_SMALL, brandName);
    }

    /**
     * Puts the place image (i.e., the one shown on the dashboard). Image may be user-generated and fetched from the platform, or may
     * be a stock photograph.
     *
     * @return
     */
    @NonNull
    public ImageRequestBuilder putPlaceImage(@Nullable String placeId) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.PLACE, CorneaUtils.getNormalizedId(placeId));
    }

    /**
     * Puts a small person image (i.e., the user's avatar or photograph). Image may be user-generated and fetched from the platform,
     * or may be a generic avatar illustration.
     *
     * @param personId
     * @return
     */
    @NonNull
    public ImageRequestBuilder putPersonImage(@Nullable String personId) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.PERSON, CorneaUtils.getNormalizedId(personId));
    }

    /**
     * Puts a large person image (i.e., the user's avatar or photograph). Image may be user-generated and fetched from the platform,
     * or may be a generic avatar illustration.
     *
     * @param personId
     * @return
     */
    @NonNull
    public ImageRequestBuilder putLargePersonImage(@Nullable String personId) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.PERSON_LARGE, CorneaUtils.getNormalizedId(personId));
    }

    /**
     * Puts the person background image. When a user-generated photo of the user exists, that photo is used, otherwise the place
     * image is used in its place.
     *
     * @param personId
     * @param placeId
     * @return
     */
    @NonNull
    public ImageRequestBuilder putPersonBackgroundImage (@NonNull String personId, @NonNull String placeId) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.PERSON_BACKGROUND, new String[] {CorneaUtils.getNormalizedId(personId), CorneaUtils.getNormalizedId(placeId)});
    }

    /**
     * Puts the scene action template icon. This is a small-circle image useful for list views. 
     * @param template
     * @return
     */
    @NonNull
    public ImageRequestBuilder putSceneActionTemplateImage (@NonNull ActionTemplate template) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.SCENE_ACTION_TEMPLATE, template.getTypehint());
    }

    /**
     * Puts the scene category icon. This is a small, squared icon (i.e., "Vacation", "Wake up", "Custom") useful
     * for list views.
     *
     * @param sceneCategory
     * @return
     */
    @NonNull
    public ImageRequestBuilder putSceneCategoryImage (@NonNull SceneCategory sceneCategory) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.SCENE_CATEGORY, sceneCategory);
    }


    /**
     * Puts the small pet image. Note that when a UGC image is returned its size and aspect ratio is
     * not defined. The consumer should assure the image is being properly resized (and circled, when
     * necessary).
     *
     * @param petSmartKeyId
     * @return
     */
    @NonNull
    public ImageRequestBuilder putSmallPetImage (@NonNull String petSmartKeyId) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.PET_SMALL, petSmartKeyId);
    }

    /**
     * Puts the large pet image. Note that when a UGC image is returned its size and aspect ratio is
     * not defined. The consumer should assure the image is being properly resized (and circled, when
     * necessary).
     *
     * @param petSmartKeyId
     * @return
     */
    @NonNull
    public ImageRequestBuilder putLargePetImage (@NonNull String petSmartKeyId) {
        return new ImageRequestBuilder(context, wallpaperTarget, ImageCategory.PET_LARGE, petSmartKeyId);
    }

    /**
     * Puts a user generated place image (i.e., the image displayed on the dashboard) for the
     * associated place id. Produces a dialog asking the user to select whether they wish to use
     * the camera or select a photo form their camera.
     *
     * This function is intended to let the user select/create an image and place it. Use {@link #putPlaceImage(String)}
     * to put the place image into a target or view without selecting it first; that method will automatically pull a previously
     * selected user-generated image if one exists.
     *
     * @param placeId
     * @return
     */
    @NonNull
    public UGCSelectAndSaveBuilder putUserGeneratedPlaceImage(@Nullable String placeId) {
        return new UGCSelectAndSaveBuilder(context, wallpaperTarget, placeId, null, ImageCategory.PLACE);
    }

    /**
     * Puts a user generated large device image (i.e., the image displayed on the device control page) for the
     * associated place and device id. Produces a dialog asking the user to select whether they wish to use
     * the camera or select a photo form their camera.
     *
     * This function is intended to let the user select/create an image and place it. Use {@link #putLargeDeviceImage(DeviceModel)}}
     * to put the device image into a target or view without selecting it first; that method will automatically pull a previously
     * selected user-generated image if one exists.
     *
     * @param placeId
     * @param deviceId
     * @return
     */
    @NonNull
    public UGCSelectAndSaveBuilder putUserGeneratedDeviceImage (@Nullable String placeId, @Nullable String deviceId) {
        return new UGCSelectAndSaveBuilder(context, wallpaperTarget, CorneaUtils.getNormalizedId(placeId), CorneaUtils.getNormalizedId(deviceId), ImageCategory.DEVICE_LARGE);
    }


    /**
     * Puts a user generated person image (i.e., the avatar of the given user) for the
     * associated person id. Produces a dialog asking the user to select whether they wish to use
     * the camera or select a photo form their camera.
     *
     * This function is intended to let the user select/create an image and place it. Use {@link #putPersonImage(String)}
     * to put the person image into a target or view without selecting it first; that method will automatically pull a previously
     * selected user-generated image if one exists.
     *
     * @param personId
     * @return
     */
    @NonNull
    public UGCSelectAndSaveBuilder putUserGeneratedPersonImage (@Nullable String personId) {
        return new UGCSelectAndSaveBuilder(context, wallpaperTarget, null, CorneaUtils.getNormalizedId(personId), ImageCategory.PERSON);
    }

    /**
     * Puts a user generated pet image (i.e., a pet photograph associated with a pet door smart key)
     * for the associated pet door smart key.
     *
     * @param petSmartKeyId
     * @return
     */
    @NonNull
    public  UGCSelectAndSaveBuilder putUserGeneratedPetImage (@NonNull String petSmartKeyId) {
        return new UGCSelectAndSaveBuilder(context, wallpaperTarget, null, petSmartKeyId, ImageCategory.PET_LARGE);
    }

    /**
     * Sets the current wallpaper to the image specified. The specified image will be located, blurred,
     * an alpha overlay performed and then set into the wallpaper view specified via {@link #setWallpaperView(Context, View)}.
     *
     * @param wallpaper
     */
    public void setWallpaper(@NonNull Wallpaper wallpaper) {
        getWallpaperBuilder(wallpaper).intoWallpaper(wallpaper.getAlphaPreset()).execute();
    }

    public PicassoRequestBuilder putWallpaper(@NonNull Wallpaper wallpaper) {
        PicassoRequestBuilder builder = getWallpaperBuilder(wallpaper)
                .withTransform(new BlurTransformation(context));

        if (wallpaper.getAlphaPreset() != null) {
            builder.withTransform(new AlphaOverlayTransformation(wallpaper.getAlphaPreset()));
        }

        return builder;
    }

    private PicassoRequestBuilder getWallpaperBuilder(Wallpaper wallpaper) {
        ImageRequestBuilder builder;

        switch (wallpaper.getImageCategory()) {
            case PLACE:
                builder = ImageManager.with(context).putPlaceImage(wallpaper.getPlaceId());
                break;
            case DEVICE_LARGE:
            case DEVICE_SMALL:
            case DEVICE_BACKGROUND:
                builder = ImageManager.with(context).putDeviceBackgroundImage(wallpaper.getDeviceModel());
                break;
            case DRAWABLE:
                builder = ImageManager.with(context).putDrawableResource(wallpaper.getDrawableResId());
                break;
            case PERSON:
            default:
                throw new IllegalArgumentException("Wallpapers are not supported yet for " + wallpaper.getImageCategory());
        }

        return builder;
    }

}
