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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import androidx.annotation.NonNull;

import com.squareup.picasso.Transformation;

public class BlackWhiteInvertTransformation implements Transformation {
    private final Paint paint = new Paint();
    private final Invert invert;

    public BlackWhiteInvertTransformation(Invert invert) {
        this.invert = invert;
    }

    @Override
    public Bitmap transform(@NonNull Bitmap source) {
        if (invert == Invert.NONE) {
            return source;
        } else if (invert == Invert.BLACK_TO_WHITE) {
            paint.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));
        } else {
            paint.setColorFilter(new PorterDuffColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN));
        }

        Bitmap bitmapResult = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmapResult);
        canvas.drawBitmap(source, 0, 0, paint);

        if (bitmapResult != source && !source.isRecycled()) {
            source.recycle();
        }

        return bitmapResult;
    }

    @NonNull
    @Override
    public String key() {
        return "BlackWhiteInvertTransformation2(invert: " + invert + ")";
    }
}
