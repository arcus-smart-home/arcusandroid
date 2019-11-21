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
package arcus.app.common.view.comboseekbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import android.util.TypedValue;

/**
 * seekbar background with text on it.
 * 
 * @author sazonov-adm
 * 
 */
public class ComboSeekBarThumb extends Drawable {
	/**
	 * paints.
	 */
	private Paint circlePaint;
	private Context mContext;
	private float mRadius;

	public ComboSeekBarThumb(Context context, int color) {
		mContext = context;
		mRadius = toPix(9);
		setColor(color);
	}

	public void setColor(int color) {
		circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		circlePaint.setColor((0xFF << 24) + (color & 0x00FFFFFF));
		invalidateSelf();
	}
	
	public float getRadius() {
		return mRadius;
	}

	@Override
	protected final boolean onStateChange(int[] state) {
		invalidateSelf();
		return false;
	}

	@Override
	public final boolean isStateful() {
		return true;
	}

	@Override
	public final void draw(@NonNull Canvas canvas) {
		int height = this.getBounds().centerY();
		int width = this.getBounds().centerX();
		canvas.drawCircle(width + mRadius, height, mRadius, circlePaint);
	}

	@Override
	public int getIntrinsicHeight() {
		return (int) (mRadius * 2);
	}

	@Override
	public int getIntrinsicWidth() {
		return (int) (mRadius * 2);
	}

	@Override
	public final int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}

	@Override
	public void setAlpha(int alpha) {
	}

	@Override
	public void setColorFilter(ColorFilter cf) {
	}

	private float toPix(int size) {
		return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, size,
				mContext.getResources().getDisplayMetrics());
	}
}
