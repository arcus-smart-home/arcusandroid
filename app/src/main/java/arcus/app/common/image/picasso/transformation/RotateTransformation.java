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
import android.graphics.Matrix;
import androidx.annotation.NonNull;

import com.squareup.picasso.Transformation;


public class RotateTransformation implements Transformation {

    private final float angle;

    public RotateTransformation(float angle) {
        this.angle = angle;
    }

    @Override
    public Bitmap transform(@NonNull Bitmap source) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        Bitmap output = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);

        if (output != source && !source.isRecycled()) {
            source.recycle();
        }

        return output;
    }

    @NonNull
    @Override
    public String key() {
        return "RotateTransformation(angle: " + angle +")";
    }
}
