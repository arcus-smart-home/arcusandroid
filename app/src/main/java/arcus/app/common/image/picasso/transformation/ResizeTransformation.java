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
package arcus.app.common.image.picasso.transformation;

import android.graphics.Bitmap;
import androidx.annotation.NonNull;

import com.squareup.picasso.Transformation;


public class ResizeTransformation implements Transformation {

    private final int maxWidth;
    private final int maxHeight;

    public ResizeTransformation (int maxHeight, int maxWidth) {
        this.maxHeight = maxHeight;
        this.maxWidth = maxWidth;
    }

    @Override
    public Bitmap transform(@NonNull Bitmap image) {
            if (maxHeight > 0 && maxWidth > 0) {
                int width = image.getWidth();
                int height = image.getHeight();
                float ratioBitmap = (float) width / (float) height;
                float ratioMax = (float) maxWidth / (float) maxHeight;

                int finalWidth = maxWidth;
                int finalHeight = maxHeight;
                if (ratioMax > 1) {
                    finalWidth = (int) ((float)maxHeight * ratioBitmap);
                } else {
                    finalHeight = (int) ((float)maxWidth / ratioBitmap);
                }

                Bitmap resizedImage = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true);

                if (image != resizedImage && !image.isRecycled()) {
                    image.recycle();
                }

                return resizedImage;
            } else {
                return image;
            }
        }

    @NonNull
    @Override
    public String key() {
        return "ResizeTransformation(maxHeight:" + maxHeight + ", maxWidth:" + maxWidth +")";
    }
}
