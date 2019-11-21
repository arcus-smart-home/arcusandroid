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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SeekBar;

import arcus.app.R;
import arcus.app.common.utils.ImageUtils;

import java.util.ArrayList;
import java.util.List;

public class ComboSeekBar extends SeekBar {
	@NonNull
	private List<ComboSeekBarSelectionListener> listeners = new ArrayList<>();

	private ComboSeekBarThumb mThumb;
	@NonNull
	private List<Dot> mDots = new ArrayList<Dot>();
	private OnItemClickListener mItemClickListener;
	@Nullable
	private Dot prevSelected = null;
	private boolean isSelected = false;
	private int mColor;
	private int mTextSize;
	private boolean mIsMultiline;
	private int mSelectedStrokeWidth;
	private int mUnselectedStrokeWidth;
	private int mUnselectedLineAlpha;
	private int mUnselectedDotAlpha;


	/**
	 * @param context
	 *            context.
	 */
	public ComboSeekBar(Context context) {
		super(context);
	}

	/**
	 * @param context
	 *            context.
	 * @param attrs
	 *            attrs.
	 */
	public ComboSeekBar(@NonNull Context context, AttributeSet attrs) {
		super(context, attrs);
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ComboSeekBar);

		mColor = a.getColor(R.styleable.ComboSeekBar_myColor, Color.WHITE);
		mTextSize = a.getDimensionPixelSize(R.styleable.ComboSeekBar_textSize, 30);
		mIsMultiline = a.getBoolean(R.styleable.ComboSeekBar_multiline, false);
		mSelectedStrokeWidth = a.getInt(R.styleable.ComboSeekBar_selectedLineWidth, 3);
		mUnselectedStrokeWidth = a.getInt(R.styleable.ComboSeekBar_unselectedLineWidth, 3);
		mUnselectedLineAlpha = a.getInt(R.styleable.ComboSeekBar_unselectedLineAlpha, 40);
		mUnselectedDotAlpha = a.getInt(R.styleable.ComboSeekBar_unselectedDotAlpha, 255);

		a.recycle();
		mThumb = new ComboSeekBarThumb(context, mColor);
		setThumb(mThumb);
		setProgressDrawable(new ComboSeekBarSlider(this.getProgressDrawable(), this, mThumb.getRadius(), mDots, mColor, mTextSize, mIsMultiline, mSelectedStrokeWidth, mUnselectedStrokeWidth, mUnselectedLineAlpha, mUnselectedDotAlpha));

		setPadding(0, 0, 0, ImageUtils.dpToPx(context, 10));
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		isSelected = false;
		return super.onTouchEvent(event);
	}

	/**
	 * @param color
	 *            color.
	 */
	public void setColor(int color) {
		mColor = color;
		mThumb.setColor(color);
		setProgressDrawable(new ComboSeekBarSlider(this.getProgressDrawable(), this, mThumb.getRadius(), mDots, color, mTextSize, mIsMultiline, mSelectedStrokeWidth, mUnselectedStrokeWidth, mUnselectedLineAlpha, mUnselectedDotAlpha));
	}

	public synchronized void setSelection(int position) {
		if ((position < 0) || (position >= mDots.size())) {
			throw new IllegalArgumentException("Position is out of bounds:" + position);
		}
		for (Dot dot : mDots) {
			dot.isSelected = dot.id == position;
		}

		isSelected = true;
		invalidate();
	}

	public int getSelection() {
		for (int index = 0; index < mDots.size(); index++) {
			if (mDots.get(index).isSelected) {
				return index;
			}
		}

		// Should't be possible
		return 0;
	}

	public void setAdapter(@NonNull List<String> dots) {
		mDots.clear();
		int index = 0;
		for (String dotName : dots) {
			Dot dot = new Dot();
			dot.text = dotName;
			dot.id = index++;
			mDots.add(dot);
		}
		initDotsCoordinates();
	}

	@Override
	public void setThumb(Drawable thumb) {
		if (thumb instanceof ComboSeekBarThumb) {
			mThumb = (ComboSeekBarThumb) thumb;
		}
		super.setThumb(thumb);
	}

	@Override
	protected synchronized void onDraw(Canvas canvas) {
		if ((mThumb != null) && (mDots.size() > 1)) {
			if (isSelected) {
				for (Dot dot : mDots) {
					if (dot.isSelected) {
						Rect bounds = mThumb.copyBounds();
						bounds.right = dot.mX;
						bounds.left = dot.mX;
						mThumb.setBounds(bounds);
						break;
					}
				}
			} else {
				int intervalWidth = mDots.get(1).mX - mDots.get(0).mX;
				Rect bounds = mThumb.copyBounds();
				// find nearest dot
				if ((mDots.get(mDots.size() - 1).mX - bounds.centerX()) < 0) {
					bounds.right = mDots.get(mDots.size() - 1).mX;
					bounds.left = mDots.get(mDots.size() - 1).mX;
					mThumb.setBounds(bounds);

					for (Dot dot : mDots) {
						dot.isSelected = false;
					}
					mDots.get(mDots.size() - 1).isSelected = true;
					handleClick(mDots.get(mDots.size() - 1));
				} else {
					for (int i = 0; i < mDots.size(); i++) {
						if (Math.abs(mDots.get(i).mX - bounds.centerX()) <= (intervalWidth / 2)) {
							bounds.right = mDots.get(i).mX;
							bounds.left = mDots.get(i).mX;
							mThumb.setBounds(bounds);
							mDots.get(i).isSelected = true;
							handleClick(mDots.get(i));
						} else {
							mDots.get(i).isSelected = false;
						}
					}
				}
			}
		}
		super.onDraw(canvas);
		fireSelectionChangeEvent();
	}

	private void handleClick(@NonNull Dot selected) {
		if ((prevSelected == null) || (prevSelected.equals(selected) == false)) {
			if (mItemClickListener != null) {
				mItemClickListener.onItemClick(null, this, selected.id, selected.id);
			}
			prevSelected = selected;
		}
	}

	@Override
	protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		ComboSeekBarSlider d = (ComboSeekBarSlider) getProgressDrawable();

		int thumbHeight = mThumb == null ? 0 : mThumb.getIntrinsicHeight();
		int dw = 0;
		int dh = 0;
		if (d != null) {
			dw = d.getIntrinsicWidth();
			dh = Math.max(thumbHeight, d.getIntrinsicHeight());
		}

		dw += getPaddingLeft() + getPaddingRight();
		dh += getPaddingTop() + getPaddingBottom();

		setMeasuredDimension(resolveSize(dw, widthMeasureSpec), resolveSize(dh, heightMeasureSpec));
	}

	/**
	 * dot coordinates.
	 */
	private void initDotsCoordinates() {
		float intervalWidth = (getWidth() - (mThumb.getRadius() * 2)) / (mDots.size() - 1);
		for (Dot dot : mDots) {
			dot.mX = (int) (mThumb.getRadius() + intervalWidth * (dot.id));
		}
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		initDotsCoordinates();
	}

	/**
	 * Sets a listener to receive events when a list item is clicked.
	 * 
	 * @param clickListener
	 *            Listener to register
	 * 
	 * @see ListView#setOnItemClickListener(OnItemClickListener)
	 */
	public void setOnItemClickListener(OnItemClickListener clickListener) {
		mItemClickListener = clickListener;
	}

	public void addSelectionListener (ComboSeekBarSelectionListener listener) {
		this.listeners.add(listener);
	}

	private void fireSelectionChangeEvent () {
		for (ComboSeekBarSelectionListener thisListener : listeners) {
			thisListener.onSelectionChanged(this, this.getSelection());
		}
	}

	public static class Dot {
		public int id;
		public int mX;
		public String text;
		public boolean isSelected = false;

		@Override
		public boolean equals(@NonNull Object o) {
			return ((Dot) o).id == id;
		}
	}
}
