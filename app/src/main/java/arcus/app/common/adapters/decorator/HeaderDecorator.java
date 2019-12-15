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
package arcus.app.common.adapters.decorator;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Region;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.ViewGroup;

/**
 * This draws a header item at the top of a RV and keeps it there until another header is incoming then it starts to
 * translate it's position for the incoming header.
 *
 * Modified Version of:
 * https://github.com/takahr/pinned-section-item-decoration/blob/master/library/src/main/java/com/kiguruming/recyclerview/itemdecoration/PinnedHeaderItemDecoration.java
 * and from https://github.com/beworker/pinned-section-listview
 *
 */
public class HeaderDecorator extends RecyclerView.ItemDecoration {
    RecyclerView.Adapter rvAdapter = null;
    LinearLayoutManager layoutMgr = null;

    View pinnedView = null;
    int pinnedHeaderAdapterPosition = -1, rvItems = -1, pinnedViewTop, pinnedHeaderEndAt, pinnedHeaderHeight, rvWidthMeasureSpec, rvHeight;
    Rect canvasClippedBounds;

    SparseBooleanArray pinnedViewTypes = new SparseBooleanArray();
    final int headerViewType;

    public HeaderDecorator(int headerViewType) {
        this.headerViewType = headerViewType;
    }

    @Override
    public void onDraw(Canvas canvas, RecyclerView recyclerView, RecyclerView.State state) {
        initializeLocalVariables(recyclerView);
        createPinnedHeader(recyclerView);

        if (pinnedView != null) {
            final View v = recyclerView.findChildViewUnder(canvas.getWidth() / 2, pinnedHeaderEndAt + 1);
            pinnedViewTop = isHeaderView(recyclerView, v) ? (v.getTop() - pinnedHeaderHeight) : 0;

            canvasClippedBounds = canvas.getClipBounds();
            canvasClippedBounds.top = pinnedViewTop + pinnedHeaderHeight;
            canvas.clipRect(canvasClippedBounds);
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        if (pinnedView != null && canvasClippedBounds != null) {
            canvasClippedBounds.top = 0; // Reset clip top, but still need the original bounds
            c.clipRect(canvasClippedBounds);

            if (pinnedViewTop != 0) { // If we're starting to offset then translate the view's bounds so we don't overlap
                c.translate(0, pinnedViewTop);
            }

            pinnedView.draw(c);
        }
    }

    protected void createPinnedHeader(RecyclerView recyclerView) {
        if (layoutMgr == null) {
            return;
        }

        final int pinnedHeaderPosition = findPinnedHeaderPosition(layoutMgr.findFirstVisibleItemPosition());
        if (pinnedHeaderPosition >= 0 && pinnedHeaderAdapterPosition != pinnedHeaderPosition) {
            pinnedHeaderAdapterPosition = pinnedHeaderPosition;
            createAndBindHeader(recyclerView);

            pinnedHeaderEndAt = pinnedView.getTop() + pinnedView.getHeight();
            pinnedHeaderHeight = pinnedView.getHeight();
        }
    }

    protected int findPinnedHeaderPosition(int fromPosition) {
        if (fromPosition > rvItems) {
            return -1;
        }

        for (int position = fromPosition; position >= 0; position--) {
            final int viewType = rvAdapter.getItemViewType(position);
            if (isPinnedViewType(viewType)) {
                return position;
            }
        }

        return -1;
    }

    protected boolean isPinnedViewType(int viewType) {
        if (pinnedViewTypes.indexOfKey(viewType) < 0) {
            pinnedViewTypes.put(viewType, viewType == headerViewType);

            return viewType == headerViewType;
        }

        return pinnedViewTypes.get(viewType);
    }

    protected boolean isHeaderView(RecyclerView parent, View v) {
        int position = parent.getChildAdapterPosition(v);
        return (position != RecyclerView.NO_POSITION) && isPinnedViewType(rvAdapter.getItemViewType(position));
    }

    protected void initializeLocalVariables(RecyclerView recyclerView) {
        RecyclerView.Adapter adapter = recyclerView.getAdapter();
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (rvAdapter == adapter && layoutMgr == manager) {
            return;
        }

        pinnedView = null;
        pinnedHeaderAdapterPosition = -1;
        pinnedViewTypes.clear();

        rvAdapter  = adapter;
        rvItems    = adapter.getItemCount();
        rvHeight   = recyclerView.getHeight() - recyclerView.getPaddingTop() - recyclerView.getPaddingBottom();
        rvWidthMeasureSpec = View.MeasureSpec.makeMeasureSpec(recyclerView.getWidth() - recyclerView.getPaddingLeft() - recyclerView.getPaddingRight(), View.MeasureSpec.EXACTLY);

        // Only handle Vertical LL managers
        if (!(manager instanceof LinearLayoutManager) || LinearLayoutManager.HORIZONTAL == ((LinearLayoutManager) manager).getOrientation()) {
            layoutMgr = null;
            return;
        }

        layoutMgr = (LinearLayoutManager) manager;
    }

    protected int getNewPinnedViewHeight() {
        ViewGroup.LayoutParams layoutParams = pinnedView.getLayoutParams();
        if (layoutParams == null) { // Only happens if it's not attached to a view group
            return 0;
        }

        return layoutParams.height;
    }

    protected int heightMeasureSpec(int fromHeight) {
        if (fromHeight > rvHeight) {
            fromHeight = rvHeight;
        }

        return View.MeasureSpec.makeMeasureSpec(fromHeight, View.MeasureSpec.UNSPECIFIED);
    }

    @SuppressWarnings("unchecked")
    protected void createAndBindHeader(RecyclerView recyclerView) {
        RecyclerView.ViewHolder vh = rvAdapter.createViewHolder(recyclerView, rvAdapter.getItemViewType(pinnedHeaderAdapterPosition));
        rvAdapter.bindViewHolder(vh, pinnedHeaderAdapterPosition);
        pinnedView = vh.itemView;

        pinnedView.measure(rvWidthMeasureSpec, heightMeasureSpec(getNewPinnedViewHeight()));
        pinnedView.layout(0, 0, pinnedView.getMeasuredWidth(), pinnedView.getMeasuredHeight());
    }
}
