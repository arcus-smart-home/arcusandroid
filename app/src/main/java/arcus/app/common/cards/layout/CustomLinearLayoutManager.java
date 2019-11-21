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
package arcus.app.common.cards.layout;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomLinearLayoutManager extends LinearLayoutManager {
    private static final Logger logger = LoggerFactory.getLogger(CustomLinearLayoutManager.class);

    public CustomLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void onMeasure(@NonNull RecyclerView.Recycler recycler, RecyclerView.State state, int widthSpec, int heightSpec) {
        int width = 0;
        int height = 0;

        for (int i = 0; i < getItemCount(); i++) {
            int[] measuredWidthHeight = measureChild(recycler, i);

            if (getOrientation() == HORIZONTAL) {
                width = width + measuredWidthHeight[0];
                if (i == 0) {
                    height = measuredWidthHeight[1];
                }
            }
            else {
                height = height + measuredWidthHeight[1];
                if (i == 0) {
                    width = measuredWidthHeight[0];
                }
            }
        }

        if (View.MeasureSpec.getMode(widthSpec) == View.MeasureSpec.EXACTLY) {
            width = View.MeasureSpec.getSize(widthSpec);
        }

        if (View.MeasureSpec.getMode(heightSpec) == View.MeasureSpec.EXACTLY) {
            height = View.MeasureSpec.getSize(heightSpec);
        }

        logger.debug("Setting measured dimensions for history card to w:[{}] h:[{}]", width, height);
        setMeasuredDimension(width, height);
    }

    @NonNull
    private int[] measureChild(@NonNull RecyclerView.Recycler recycler, int position) {
        View view = recycler.getViewForPosition(position);

        if (view != null) {
            int measureSpec = View.MeasureSpec.makeMeasureSpec(position, View.MeasureSpec.UNSPECIFIED);
            int leftAndRightPadding = getPaddingLeft() + getPaddingRight();
            int topAndBottomPadding = getPaddingTop() + getPaddingBottom();

            RecyclerView.LayoutParams p = (RecyclerView.LayoutParams) view.getLayoutParams();
            int childWidthSpec = ViewGroup.getChildMeasureSpec(measureSpec, leftAndRightPadding, p.width);
            int childHeightSpec = ViewGroup.getChildMeasureSpec(measureSpec, topAndBottomPadding, p.height);

            view.measure(childWidthSpec, childHeightSpec);
            int measurements[] = {
                  view.getMeasuredWidth() + p.leftMargin + p.rightMargin,
                  view.getMeasuredHeight() + p.bottomMargin + p.topMargin
            };
            recycler.recycleView(view);
            return measurements;
        }

        return new int[2];
    }
}
