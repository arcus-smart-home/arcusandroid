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
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import androidx.annotation.NonNull;

import arcus.app.ArcusApplication;
import arcus.app.R;
import com.squareup.picasso.Transformation;

public class RoundBorderTransformation implements Transformation {
    @Override
    public Bitmap transform(@NonNull Bitmap source) {
        int w = source.getWidth();
        int h = source.getHeight();

        int radius = Math.min(h / 2, w / 2);
        Bitmap output = Bitmap.createBitmap(w + 8, h + 8, Bitmap.Config.ARGB_8888);

        Paint p = new Paint();
        p.setAntiAlias(true);

        Canvas c = new Canvas(output);
        c.drawARGB(0, 0, 0, 0);
        p.setStyle(Paint.Style.FILL);

        c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);

        p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        c.drawBitmap(source, 4, 4, p);
        p.setXfermode(null);
        p.setStyle(Paint.Style.STROKE);
        p.setColor(ArcusApplication.getArcusApplication().getResources().getColor(R.color.overlay_white_with_40));
        p.setStrokeWidth(10);
        c.drawCircle((w / 2) + 4, (h / 2) + 4, radius, p);

        if (source != output && !source.isRecycled()) {
            source.recycle();
        }

        return output;
    }

    @NonNull
    @Override
    public String key() {
        return "ROUND_BORDER_TRANSFORMATION";
    }
}
