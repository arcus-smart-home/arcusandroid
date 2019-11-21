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
package arcus.app.common.image.picasso.target;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Intended to be for targeting the Left / Right navigation "Thumbs"
 * on the device details screens.  When you're navigating through the list of paired devices.
 *
 */
public class ScaleTransformationTarget implements Target {
    private static final Logger logger = LoggerFactory.getLogger(ScaleTransformationTarget.class);
    private float PERCENT_OFF_SCREEN = 0.7f;
    private ImageView imageView;
    private int width = 0;
    private Side targetSide;

    public enum Side {
        LEFT,
        RIGHT
    }

    public ScaleTransformationTarget(@NonNull ImageView imageView, @NonNull Side targetSide) {
        this.imageView = imageView;
        this.targetSide = targetSide;
    }

    @Override
    public void onBitmapLoaded(@NonNull Bitmap bitmap, Picasso.LoadedFrom from) {
        imageView.setImageBitmap(bitmap);
        width = bitmap.getWidth();
        adjustLayoutParameters();
    }

    public void setPercentOffScreen(float percentOffScreen) {
        PERCENT_OFF_SCREEN = percentOffScreen;
    }

    @Override
    public void onBitmapFailed(@Nullable Drawable errorDrawable) {
        if (errorDrawable == null || errorDrawable.getBounds() == null) {
            return; // No image or can't get width.
        }

        try {
            imageView.setImageDrawable(errorDrawable);
            width = errorDrawable.getBounds().width();
            adjustLayoutParameters();
        }
        catch (Exception ex) {
            logger.debug("Exception setting onBitmapFailed", ex);
        }
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
    }

    private void adjustLayoutParameters() {
        if (targetSide.equals(Side.LEFT)) {
            adjustForLeftSide();
        }
        else {
            adjustForRightSide();
        }
    }

    private void adjustForRightSide() {
        int amountOffscreen  = (int)(width * PERCENT_OFF_SCREEN);
        RelativeLayout.LayoutParams rightLP = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        rightLP.setMargins(amountOffscreen, 0, -1 * amountOffscreen, 0);
        imageView.setLayoutParams(rightLP);
    }

    private void adjustForLeftSide() {
        int amountOffscreen  = (int)(width * PERCENT_OFF_SCREEN);
        RelativeLayout.LayoutParams leftLP = (RelativeLayout.LayoutParams) imageView.getLayoutParams();
        leftLP.setMargins(-1 * amountOffscreen, 0, amountOffscreen, 0);
        imageView.setLayoutParams(leftLP);
    }
}
