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
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import androidx.annotation.NonNull;

import com.squareup.picasso.Transformation;


public class CircularTransformation implements Transformation {

    @Override
    public Bitmap transform(@NonNull Bitmap source) {

        //crop to square
        int size = Math.min(source.getWidth(), source.getHeight());

        int x = (source.getWidth() - size) / 2;
        int y = (source.getHeight() - size) / 2;

        Bitmap squaredBitmap = Bitmap.createBitmap(source, x, y, size, size);
        if (squaredBitmap != source) {
            source.recycle();
        }

        Matrix matrix = new Matrix();
        Bitmap resizedBitmap = Bitmap.createBitmap(squaredBitmap, 0, 0, squaredBitmap.getHeight(), squaredBitmap.getWidth(), matrix, false);

        if(resizedBitmap != squaredBitmap && !squaredBitmap.isRecycled()){
            squaredBitmap.recycle();
        }

        //convert to circle
        Bitmap output = Bitmap.createBitmap(resizedBitmap.getWidth(),
                resizedBitmap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, resizedBitmap.getWidth(), resizedBitmap.getHeight());

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        canvas.drawCircle(resizedBitmap.getWidth() / 2, resizedBitmap.getHeight() / 2,
                resizedBitmap.getWidth() / 2, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(resizedBitmap, rect, rect, paint);

        if(output != resizedBitmap && !resizedBitmap.isRecycled()){
            resizedBitmap.recycle();
        }

        return output;
    }

    @NonNull
    @Override
    public String key() {
        return "circular()";
    }
}
