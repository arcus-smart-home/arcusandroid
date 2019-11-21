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

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicBlur;

import arcus.app.ArcusApplication;
import com.squareup.picasso.Transformation;

public class BlurTransformation implements Transformation {
    private final float scale;
    private final float radius;
    private final Context context;

    public BlurTransformation (Context context) {
        this.scale = 0.1f;
        this.radius = 20.0f;
        this.context = context;
    }

    @Override
    public Bitmap transform(@NonNull Bitmap source) {

        int width = Math.round(source.getWidth() * scale);
        int height = Math.round(source.getHeight() * scale);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(source, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(getContext());
        final ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        final Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        final Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);

        theIntrinsic.setRadius(radius);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        if (inputBitmap != source && !inputBitmap.isRecycled()) {
            inputBitmap.recycle();
        }

        if (outputBitmap != source && !source.isRecycled()) {
            source.recycle();
        }

        return outputBitmap;
    }

    private Context getContext() {
        if (context == null) {
            return ArcusApplication.getContext();
        } else {
            return context;
        }
    }

    @NonNull
    @Override
    public String key() {
        return "BlurTransformation(scale: " + scale + " radius:" + radius + ")";
    }
}

