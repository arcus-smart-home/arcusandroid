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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.widget.ImageView;

import arcus.app.common.image.picasso.transformation.CropCircleTransformation;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

public class DeviceImageTarget implements Target {
    private ImageView imageView;

    public DeviceImageTarget(ImageView imageView, Context context) {
        this.imageView = imageView;
        Context context1 = context;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public void onBitmapFailed(@Nullable Drawable errorDrawable) {
        if (errorDrawable != null) {
            imageView.setImageBitmap(new CropCircleTransformation().transform(((BitmapDrawable) errorDrawable).getBitmap()));
        }
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {
        imageView.setImageDrawable(placeHolderDrawable);
    }
}
