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
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.CorneaClientFactory;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.ProductModel;
import arcus.app.R;
import arcus.app.common.utils.ImageUtils;
import arcus.app.common.utils.StringUtils;
import arcus.app.device.model.DeviceType;
import arcus.app.subsystems.scenes.catalog.model.SceneCategory;

import java.io.File;
import java.io.IOException;

/**
 * An image location resolver. Responsible for determing the location of a requested image, given
 * the image's category and a "hint" about how to find it.
 *
 * This class encapsulates all image fallback logic (for example, the business rule that if a user-
 * generated photo doesn't exist for a device then we should display a stock illustration).
 *
 * Device hints are specific to each image category. For instance, when resolving a device image the
 * hint will be the device identifier (or DeviceModel); when resolving a person image, the hint
 * will be the person id.
 */
public class ImageLocator {

    private final Context context;
    private final ImageCategory category;
    private Object hint;
    private boolean noUserGeneratedImagery;

    private ImageLocator (Context context, ImageCategory category) {
        this.category = category;
        this.context = context;
    }

    @NonNull
    public static ImageLocator locate (Context context, ImageCategory category) {
        return new ImageLocator(context, category);
    }

    @NonNull
    public ImageLocator noUserGeneratedImagery () {
        this.noUserGeneratedImagery = true;
        return this;
    }

    @NonNull
    public ImageLocator using (Object hint) {
        this.hint = hint;
        return this;
    }

    @NonNull
    public ImageLocationSpec execute () {
        return getLocation();
    }

    @NonNull
    private ImageLocationSpec locateLargeDeviceImage () {
        if (hint instanceof DeviceModel || hint instanceof HubModel) {

            String deviceId = (hint instanceof DeviceModel) ? ((DeviceModel) hint).getId() : ((HubModel) hint).getId();
            String placeId = (hint instanceof DeviceModel) ? ((DeviceModel) hint).getPlace().toString() : ((HubModel) hint).getPlace().toString();

            // If a user-generated image exists, use it...
            if (!noUserGeneratedImagery && ImageRepository.imageExists(context, ImageCategory.DEVICE_LARGE, placeId, deviceId)) {
                Uri imageUri = ImageRepository.getUriForImage(context, ImageCategory.DEVICE_LARGE, placeId, deviceId);
                return new ImageLocation(imageUri, true);
            }

            // ... otherwise, fallback to the system defined image
            return locateLargeProductImage();
        }

        else if (hint instanceof DeviceType) {
            return new ImageLocation(getLargeDeviceTypeUrl(((DeviceType) hint).getHint()));
        }

        throw new IllegalStateException("Don't know how to locate image using " + hint.getClass().getSimpleName());
    }

    @NonNull
    private ImageLocationSpec locateSmallDeviceImage () {
        if (hint instanceof DeviceModel) {
            DeviceModel deviceModel = (DeviceModel) hint;
            String placeId = String.valueOf(deviceModel.getPlace());

            // If a user-generated image exists, use it...
            if (!noUserGeneratedImagery && ImageRepository.imageExists(context, ImageCategory.DEVICE_LARGE, placeId, deviceModel.getId())) {
                Uri imageUri = ImageRepository.getUriForImage(context, ImageCategory.DEVICE_LARGE, placeId, deviceModel.getId());
                return new ImageLocation(imageUri, true);
            }

            // Otherwise, fallback to the product image
            return locateSmallProductImage();
        }

        // Given a DeviceType best we can do is return the generic 'dtype' illustration
        else if (hint instanceof DeviceType) {
            return new ImageLocation(getSmallDeviceTypeUrl(((DeviceType) hint).getHint()));
        }

        throw new IllegalStateException("Don't know how to locate image using " + hint.getClass().getSimpleName());
    }

    @NonNull
    private ImageLocationSpec locateLargeProductImage () {

        if (hint instanceof String) {
            return new ImageLocation(getLargeProductUrl((String) hint));
        }

        String productImageUrl = null;
        DeviceType deviceType = DeviceType.NOT_SUPPORTED;

        if (hint instanceof DeviceModel) {
            DeviceModel model = (DeviceModel) hint;

            if (!StringUtils.isEmpty(((DeviceModel)hint).getProductId())) {
                productImageUrl = getLargeProductUrl(model.getProductId());
                deviceType = DeviceType.fromHint(model.getDevtypehint());
            } else if (!StringUtils.isEmpty(model.getDevtypehint())) {
                deviceType = DeviceType.fromHint(model.getDevtypehint());
                return new ImageLocation(getLargeDeviceTypeUrl(deviceType.getHint()));
            }

        }

        if (hint instanceof ProductModel) {
            ProductModel model = (ProductModel) hint;

            productImageUrl = getLargeProductUrl(model.getId());
            deviceType = DeviceType.fromHint(model.getScreen());
        }

        if (hint instanceof HubModel) {
            deviceType = DeviceType.MAIN_HUB;
        }

        // Does this product have a custom icon?
        if (productImageUrl != null && ImageExistenceChecker.onServer(productImageUrl)) {
            return new ImageLocation(productImageUrl);
        }

        // If not, fall back to the generic device type image
        else {
            return new ImageLocation(getLargeDeviceTypeUrl(deviceType.getHint()));
        }
    }

    @NonNull
    private ImageLocationSpec locateSmallProductImage () {
        if (hint instanceof String) {
            return new ImageLocation(getSmallProductUrl((String) hint));
        }

        String productImageUrl = null;
        DeviceType deviceType = DeviceType.NOT_SUPPORTED;

        if (hint instanceof DeviceModel) {
            DeviceModel model = (DeviceModel) hint;

            if (!StringUtils.isEmpty(((DeviceModel)hint).getProductId())) {
                productImageUrl = getSmallProductUrl(model.getProductId());
                deviceType = DeviceType.fromHint(model.getDevtypehint());
            } else if (!StringUtils.isEmpty(model.getDevtypehint())) {
                deviceType = DeviceType.fromHint(model.getDevtypehint());
                return new ImageLocation(getSmallDeviceTypeUrl(deviceType.getHint()));
            }

        }

        if (hint instanceof ProductModel) {
            ProductModel model = (ProductModel) hint;

            productImageUrl = getSmallProductUrl(model.getId());
            deviceType = DeviceType.fromHint(model.getScreen());
        }

        // Does this product have a custom icon?
        if (productImageUrl != null && ImageExistenceChecker.onServer(productImageUrl)) {
            return new ImageLocation(productImageUrl);
        }

        // If not, fall back to the generic device type image
        else {
            return new ImageLocation(getSmallDeviceTypeUrl(deviceType.getHint()));
        }
    }

    @NonNull
    private ImageLocationSpec locateSmallBrandImage () {
        if (hint instanceof DeviceModel) {
            return new ImageLocation(getBrandImageUrl((DeviceModel) hint));
        } else if (hint instanceof String) {
            return new ImageLocation(getBrandImageUrl((String) hint));
        }

        throw new IllegalStateException("Don't know how to locate image using " + hint.getClass().getSimpleName());
    }

    @NonNull
    private ImageLocationSpec locateSmallDeviceCategory () {
        if (hint instanceof DeviceCategory) {
            return new ImageLocation(((DeviceCategory) hint).getImageResId());
        }

        throw new IllegalStateException("Don't know how to locate image using " + hint.getClass().getSimpleName());
    }

    @NonNull
    private ImageLocationSpec locateDrawable () {
        if (hint instanceof Integer) {
            if (((Integer) hint) == 0) {
                throw new IllegalArgumentException("Image resource ID must be non-zero.");
            }

            return new ImageLocation(hint);
        }

        throw new IllegalStateException("Don't know how to locate image using " + hint.getClass().getSimpleName());
    }

    @NonNull
    private ImageLocationSpec locateDeviceBackground () {
        if (hint instanceof DeviceModel || hint instanceof HubModel) {

            String placeId = (hint instanceof DeviceModel) ? ((DeviceModel)hint).getPlace().toString() : ((HubModel)hint).getPlace().toString();
            String deviceId = (hint instanceof DeviceModel) ? ((DeviceModel)hint).getId() : ((HubModel)hint).getId();

            // Return user-generated image if one exists...
            if (!noUserGeneratedImagery && ImageRepository.imageExists(context, ImageCategory.DEVICE_LARGE, placeId, deviceId)) {
                Uri imageUri = ImageRepository.getUriForImage(context, ImageCategory.DEVICE_LARGE, placeId, deviceId);
                return new ImageLocation(imageUri, true);
            }

            // Otherwise... return the place image
            hint = placeId;
            return locatePlaceImage();
        }

        throw new IllegalStateException("Don't know how to locate image using " + hint.getClass().getSimpleName());
    }

    @NonNull
    private ImageLocationSpec locatePlaceImage() {
        if (hint instanceof String) {
            String placeId = (String) hint;

            if (!noUserGeneratedImagery && ImageRepository.imageExists(context, ImageCategory.PLACE, placeId, null)) {
                Uri imageUri = ImageRepository.getUriForImage(context, ImageCategory.PLACE, placeId, null);
                return new ImageLocation(imageUri, true);
            }

            return new DefaultPlaceImageLocation(placeId);
        }

        throw new IllegalStateException("Don't know how to locate image using " + hint.getClass().getSimpleName());
    }

    @NonNull
    private ImageLocationSpec locateLargePairingImage () {
        if (hint instanceof PairingStepSpecifier) {
            PairingStepSpecifier spec = (PairingStepSpecifier) hint;
            return new ImageLocation(getPairingStepImageUrl(spec.productId, spec.stepNumber));
        }

        throw new IllegalStateException("Don't know how to locate image using " + hint.getClass().getSimpleName());
    }

    @NonNull
    private ImageLocationSpec locateLargeReconnectImage () {
        PairingStepSpecifier spec = (PairingStepSpecifier) hint;
        return new ImageLocation(getReconnectImageUrl(spec.productId, spec.stepNumber));
    }

    @NonNull
    private ImageLocationSpec locatePersonBackground () {
        if (hint instanceof String[]) {
            String personId = ((String[]) hint)[0];
            String placeId = ((String[]) hint)[1];

            // User has supplied photo of themselves
            if (!noUserGeneratedImagery && ImageRepository.imageExists(context, ImageCategory.PERSON, null, personId)) {
                Uri imageUri = ImageRepository.getUriForImage(context, ImageCategory.PERSON, null, personId);
                return new ImageLocation(imageUri, true);
            }

            // Otherwise, fall back to place photo
            else {
                hint = placeId;
                return locatePlaceImage();
            }
        }

        throw new IllegalStateException("Don't know how to locate image using " + hint.getClass().getSimpleName());
    }

    @NonNull
    private ImageLocationSpec locatePerson () {
        if (hint instanceof String) {

            String personId = (String) hint;

            if (!noUserGeneratedImagery && ImageRepository.imageExists(context, ImageCategory.PERSON, null, personId)) {
                Uri imageUri = ImageRepository.getUriForImage(context, ImageCategory.PERSON, null, personId);
                return new ImageLocation(imageUri, true);
            }

            return new ImageLocation(R.drawable.icon_user_small_white);
        }

        throw new IllegalArgumentException("Don't know how to locate image using " + hint.getClass().getSimpleName());
    }

    @NonNull
    private ImageLocationSpec locateLargePerson() {
        if (hint instanceof String) {
            String personId = (String) hint;

            if (!noUserGeneratedImagery && ImageRepository.imageExists(context, ImageCategory.PERSON, null, personId)) {
                Uri imageUri = ImageRepository.getUriForImage(context, ImageCategory.PERSON, null, personId);
                return new ImageLocation(imageUri, true);
            }

            return new ImageLocation(R.drawable.icon_user_large_white);
        }

        throw new IllegalArgumentException("Don't know how to locate image using " + hint.getClass().getSimpleName());
    }

    @NonNull
    private ImageLocationSpec locateSceneActionTemplate () {
        if (hint instanceof String) {
            String actionTypeHint = (String) hint;
            String sceneActionTemplateUrl = getSceneActionTemplateUrl(actionTypeHint);

            return new ImageLocation(sceneActionTemplateUrl);
        }

        throw new IllegalArgumentException("Don't know how to locate image using " + hint.getClass().getSimpleName());
    }

    @NonNull
    private ImageLocationSpec locateSceneCategory () {
        if (hint instanceof SceneCategory) {
            return new ImageLocation(((SceneCategory) hint).getIconResId());
        }

        throw new IllegalArgumentException("Don't know how to locate image using " + hint.getClass().getSimpleName());
    }

    @NonNull
    private ImageLocationSpec locateSmallPetImage () {
        if (hint instanceof String) {
            String petSmartKeyId = (String) hint;

            if (!noUserGeneratedImagery && ImageRepository.imageExists(context, ImageCategory.PET_LARGE, null, petSmartKeyId)) {
                Uri imageUri = ImageRepository.getUriForImage(context, ImageCategory.PET_LARGE, null, petSmartKeyId);
                return new ImageLocation(imageUri, true);
            }

            return new ImageLocation(R.drawable.pet_smart_key_small);
        }

        throw new IllegalArgumentException("Don't know how to locate image using " + hint.getClass().getSimpleName());
    }

    @NonNull
    private ImageLocationSpec locateLargePetImage () {
        if (hint instanceof String) {
            String petSmartKeyId = (String) hint;

            if (!noUserGeneratedImagery && ImageRepository.imageExists(context, ImageCategory.PET_LARGE, null, petSmartKeyId)) {
                Uri imageUri = ImageRepository.getUriForImage(context, ImageCategory.PET_LARGE, null, petSmartKeyId);
                return new ImageLocation(imageUri, true);
            }

            return new ImageLocation(R.drawable.pet_smart_key_large);
        }

        throw new IllegalArgumentException("Don't know how to locate image using " + hint.getClass().getSimpleName());
    }

    @NonNull
    private ImageLocationSpec getLocation () {

        if (hint instanceof File || hint instanceof Uri) {
            return new ImageLocation(hint);
        }

        switch (category) {
            case DEVICE_LARGE:
            case DEVICE_TYPE_LARGE:
                return locateLargeDeviceImage();
            case DEVICE_SMALL:
            case DEVICE_TYPE_SMALL:
                return locateSmallDeviceImage();
            case PRODUCT_SMALL:
                return locateSmallProductImage();
            case PAIRING_LARGE:
                return locateLargePairingImage();
            case RECONNECT_LARGE:
                return locateLargeReconnectImage();
            case BRAND_SMALL:
                return locateSmallBrandImage();
            case DEVICE_CATEGORY_SMALL:
                return locateSmallDeviceCategory();
            case DRAWABLE:
                return locateDrawable();
            case PLACE:
                return locatePlaceImage();
            case DEVICE_BACKGROUND:
                return locateDeviceBackground();
            case PERSON:
                return locatePerson();
            case PERSON_LARGE:
                return locateLargePerson();
            case PERSON_BACKGROUND:
                return locatePersonBackground();
            case SCENE_ACTION_TEMPLATE:
                return locateSceneActionTemplate();
            case SCENE_CATEGORY:
                return locateSceneCategory();
            case PET_SMALL:
                return locateSmallPetImage();
            case PET_LARGE:
                return locateLargePetImage();

            // Better never happen...
            default:
                throw new IllegalArgumentException("Bug! Unhandled ImageCategory: " + category);
        }
    }

    @NonNull
    private String getBrandImageUrl(@Nullable DeviceModel model) {
        String modelTypeHint = "";

        if (model != null) {
            modelTypeHint = model.getVendor();
        }

        return getBrandImageUrl(modelTypeHint);
    }

    @Nullable
    private String getLargeDeviceTypeUrl(String deviceType) {
        String encodedArgument = StringUtils.sanitize(deviceType);
        try {
            return getBaseUrl() + "/o/dtypes/" + encodedArgument + "/type_large-and-" + ImageUtils.getScreenDensity() +".png";
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    private String getSmallDeviceTypeUrl(String deviceType) {
        String encodedArgument = StringUtils.sanitize(deviceType);
        try {
            return getBaseUrl() + "/o/dtypes/" + encodedArgument + "/type_small-and-" + ImageUtils.getScreenDensity() +".png";
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    private String getBrandImageUrl(String brandName) {
        String encodedArgument = StringUtils.sanitize(brandName);
        try {
            return getBaseUrl() + "/o/brands/" + encodedArgument + "/brand_small-and-" + ImageUtils.getScreenDensity() +".png";
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    private String getLargeProductUrl(String productId) {
        String encodedArgument = StringUtils.sanitize(productId);
        try {
            return getBaseUrl() + "/o/products/" + encodedArgument + "/product_large-and-" + ImageUtils.getScreenDensity() + ".png";
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    private String getSmallProductUrl(String productId) {
        String encodedArgument = StringUtils.sanitize(productId);
        try {
            return getBaseUrl() + "/o/products/" + encodedArgument + "/product_small-and-" + ImageUtils.getScreenDensity() + ".png";
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    private String getPairingStepImageUrl (String productId, String stepNumber) {
        String encodedProductId = StringUtils.sanitize(productId);
        String encodedStepNumber = StringUtils.sanitize(stepNumber);
        try {
            return getBaseUrl() + "/o/products/" + encodedProductId + "/pair/pair" + encodedStepNumber + "_large-and-" + ImageUtils.getScreenDensity() + ".png";
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    private String getReconnectImageUrl(String productId, String stepNumber) {
        String encodedProductId = StringUtils.sanitize(productId);
        String encodedStepNumber = StringUtils.sanitize(stepNumber);
        try {
            return getBaseUrl() + "/o/products/" + encodedProductId + "/reconnect/reconnect" + encodedStepNumber + "_large-and-" + ImageUtils.getScreenDensity() + ".png";
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    private String getSceneActionTemplateUrl (String actionTypeHint) {
        String encodedActionTypeHint = StringUtils.sanitize(actionTypeHint);
        try {
            return getBaseUrl() + "/o/actions/" + encodedActionTypeHint + "/" + encodedActionTypeHint + "_small-and-" + ImageUtils.getScreenDensity() + ".png";
        } catch (IOException e) {
            return null;
        }
    }

    @Nullable
    private String getBaseUrl() throws IOException {
        try {
            return CorneaClientFactory.getClient().getSessionInfo().getStaticResourceBaseUrl();
        }
        catch (Exception ex) {
            throw new IOException("Static resource server URL is not available.");
        }
    }
}
