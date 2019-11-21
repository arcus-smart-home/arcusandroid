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
package arcus.app.common.view;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

import arcus.app.R;


@SuppressLint("AppCompatCustomView")
public class CircularImageView extends ImageView {

    @Nullable
    private Bitmap resizedCircularImage;
    private boolean bevelVisible = true;
    protected int imageHeight;
    protected int imageWidth;
    protected int viewHeight;
    protected int viewWidth;
    private int bevelStrokeWidth = 15;

    public CircularImageView(Context context) {
        super(context);
    }

    public CircularImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CircularImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(21)
    public CircularImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setImageBitmap (@Nullable Bitmap bitmap) {
        resizedCircularImage = bitmap;

        // Special case: remove image altogether
        if (bitmap == null) {
            super.setImageBitmap(null);
        } else {
            imageHeight = resizedCircularImage.getHeight();
            imageWidth = resizedCircularImage.getWidth();
            viewHeight = imageHeight + (bevelStrokeWidth * 2);
            viewWidth = imageWidth + (bevelStrokeWidth * 2);

            setBevelVisible(bevelVisible);
        }
    }

    public void setImageDrawable (@Nullable Drawable drawable) {

        // Special case: remove image altogether
        if (drawable == null) {
            super.setImageDrawable(null);
            return;
        }

        this.setImageBitmap(((BitmapDrawable) drawable).getBitmap());
    }

    public void setBevelStrokeWidth (int bevelStrokeWidth) {
        this.bevelStrokeWidth = bevelStrokeWidth;
    }

    public void setBevelVisible(boolean visible) {

        this.bevelVisible = visible;

        // Nothing to do if image hasn't yet been specified
        if (resizedCircularImage != null) {
            Bitmap beveledImage = resizedCircularImage;

            if (visible) {
                beveledImage = drawBevel(beveledImage);
            }

            super.setImageDrawable(new BitmapDrawable(getResources(), beveledImage));
        }
    }

    private Bitmap drawBevel(@NonNull Bitmap bitmap){
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int radius = Math.min(h / 2 + (bevelStrokeWidth / 2), w / 2 + (bevelStrokeWidth / 2));
        Bitmap output = Bitmap.createBitmap(w + (bevelStrokeWidth * 2), h + (bevelStrokeWidth * 2), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(output);

        // Draw the source image (i.e., picture inside the circle)
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);
        c.drawARGB(0, 0, 0, 0);
        c.drawBitmap(bitmap, bevelStrokeWidth, bevelStrokeWidth, paint);

        // Draw the bevel around it
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(getResources().getColor(R.color.overlay_white_with_40));
        paint.setStrokeWidth(bevelStrokeWidth);
        c.drawCircle((w / 2) + bevelStrokeWidth, (h / 2) + bevelStrokeWidth, radius, paint);

        return output;
    }
}
