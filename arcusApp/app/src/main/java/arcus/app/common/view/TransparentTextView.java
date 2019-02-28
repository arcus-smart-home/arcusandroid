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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.widget.TextView;


public class TransparentTextView extends TextView {

    Bitmap mMaskBitmap;
    Canvas mMaskCanvas;
    Paint mPaint;

    Drawable mBackground;
    Bitmap mBackgroundBitmap;
    Canvas mBackgroundCanvas;
    boolean mSetBoundsOnSizeAvailable = false;

    public TransparentTextView(Context context)
    {
        super(context);
    }


    public TransparentTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        mPaint = new Paint();
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        super.setTextColor(Color.BLACK);
        super.setBackground(new ColorDrawable(Color.TRANSPARENT));
    }

    public TransparentTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mPaint = new Paint();
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
        super.setTextColor(Color.BLACK);
        super.setBackground(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public void setBackground(@NonNull Drawable bg)
    {
        mBackground = bg;
        int w = bg.getIntrinsicWidth();
        int h = bg.getIntrinsicHeight();

        // Drawable has no dimensions, retrieve View's dimensions
        if (w == -1 || h == -1)
        {
            w = getWidth();
            h = getHeight();
        }

        // Layout has not run
        if (w == 0 || h == 0)
        {
            mSetBoundsOnSizeAvailable = true;
            return;
        }

        mBackground.setBounds(0, 0, w, h);
        invalidate();
    }

    @Override
    public void setBackgroundColor(int color)
    {
        setBackground(new ColorDrawable(color));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh)
    {
        super.onSizeChanged(w, h, oldw, oldh);
        mBackgroundBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mBackgroundCanvas = new Canvas(mBackgroundBitmap);
        mMaskBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        mMaskCanvas = new Canvas(mMaskBitmap);

        if (mSetBoundsOnSizeAvailable)
        {
            mBackground.setBounds(0, 0, w, h);
            mSetBoundsOnSizeAvailable = false;
        }
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas)
    {
        // Draw background
        mBackground.draw(mBackgroundCanvas);

        // Draw mask
        mMaskCanvas.drawColor(Color.BLACK, PorterDuff.Mode.CLEAR);
        super.onDraw(mMaskCanvas);

        mBackgroundCanvas.drawBitmap(mMaskBitmap, 0.f, 0.f, mPaint);
        canvas.drawBitmap(mBackgroundBitmap, 0.f, 0.f, null);
    }
}
