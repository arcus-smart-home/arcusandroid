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


public class ScaleTransformation implements Transformation {

    private float scaleToWidth;
    private float scaleToHeight;

    public ScaleTransformation(float scale){
        this.scaleToWidth = scale;
        this.scaleToHeight = scale;
    }

    @Override
    public Bitmap transform(@NonNull Bitmap source) {

        int width = source.getWidth();
        int height = source.getHeight();

        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleToWidth, scaleToHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap result = Bitmap.createBitmap(
                source, 0, 0, width, height, matrix, false);

        if(result!=source && !source.isRecycled()){
            source.recycle();
        }

        return result;
    }

    @NonNull
    @Override
    public String key() {
        return "scale()";
    }
}
