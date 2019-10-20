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

import arcus.app.common.utils.ImageUtils;
import com.squareup.picasso.Transformation;


public class FilterBarTransformation implements Transformation {
    @Override
    public Bitmap transform(Bitmap source) {

        if (source.getHeight() > ImageUtils.dpToPx(45)) {
            Bitmap result = Bitmap.createBitmap(source, 0, 0, source.getWidth(), ImageUtils.dpToPx(45));
            source.recycle();

            return result;
        }

        else {
            return source;
        }
    }

    @Override
    public String key() {
        return this.getClass().getSimpleName();
    }
}
