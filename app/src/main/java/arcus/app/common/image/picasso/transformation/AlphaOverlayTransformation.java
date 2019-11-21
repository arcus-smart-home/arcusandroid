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
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import androidx.annotation.NonNull;

import com.squareup.picasso.Transformation;


public class AlphaOverlayTransformation implements Transformation {

    private final AlphaPreset preset;

    public AlphaOverlayTransformation (AlphaPreset preset) {
        this.preset = preset;
    }

    @Override
    public Bitmap transform(@NonNull Bitmap source) {

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setXfermode(new PorterDuffXfermode(preset.mode));
        paint.setColor(preset.color);
        paint.setAlpha(preset.alpha);

        Bitmap outBitmap = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(outBitmap);
        canvas.drawBitmap(source, 0, 0, null);
        canvas.drawRect(0, 0, outBitmap.getWidth(), outBitmap.getHeight(), paint);

        if (outBitmap != source && !source.isRecycled()) {
            source.recycle();
        }
        return outBitmap;
    }

    @NonNull
    @Override
    public String key() {
        return "AlphaOverlayTransformation (alpha:" + preset.alpha + " color:" + preset.color + " mode:" + preset.mode + ")";
    }
}
