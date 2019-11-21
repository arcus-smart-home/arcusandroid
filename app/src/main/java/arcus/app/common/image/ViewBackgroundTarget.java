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

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.view.View;

import arcus.app.ArcusApplication;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

/**
 * A Picasso target to place an image into the background of a view. This stoooooopid little class
 * exists only because Picasso doesn't natively support an .intoBackground() method.
 *
 * WARNING, WARNING, WARNING: Correct use of this class is tricky! Picasso maintains only a weak reference to it. Thus
 * you cannot instantiate this as either an anonymous inner class, or a method local variable as
 * both instances will sometimes be garbage collected before Picasso has a chance to invoke them.
 * In this situation, nothing will happen--no error, no placeholder image, no nothing. It's
 * non-deterministic and impossible to debug without turning on debug mode.
 * enabled.
 */
public class ViewBackgroundTarget implements Target {

    @Nullable private final View view;
    @Nullable private ImageSuccessCallback successCallback;

    public ViewBackgroundTarget (@Nullable View view) {
        this.view = view;
    }

    @Nullable
    public View getView () {
        return this.view;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        if (view != null && bitmap != null) {
            view.setBackground(new BitmapDrawable(ArcusApplication.getContext().getResources(), bitmap));

            if (successCallback != null) {
                successCallback.onImagePlacementSuccess();
            }
        }
        setSuccessCallback(null);
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        if (view != null && errorDrawable != null) {
            view.setBackground(errorDrawable);
        }
        setSuccessCallback(null);
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        if (view != null && placeHolderDrawable != null) {
            view.setBackground(placeHolderDrawable);
        }
    }

    /**
     * This needs to be set for each invocation. This is cleared after each success or failure of the image loading.
     *
     * @param successCallback
     */
    public void setSuccessCallback(@Nullable ImageSuccessCallback successCallback) {
        this.successCallback = successCallback;
    }
}
