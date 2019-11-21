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
package arcus.app.subsystems.camera.adapter;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class EndlessScrollRecycleListener extends RecyclerView.OnScrollListener {
    private int previousTotalItemCount = 0; // Total number of items in the data-set after the last load
    private boolean loading = true;
    int firstVisibleItem, visibleItemCount, totalItemCount;
    int visibleThreshold = 1;
    LinearLayoutManager mLayoutManager;

    public EndlessScrollRecycleListener(int visibleThreshold) {
        this.visibleThreshold = visibleThreshold;
    }

    @Override public void onScrolled(RecyclerView mRecyclerView, int dx, int dy) {
        super.onScrolled(mRecyclerView, dx, dy);
        if (mLayoutManager == null) {
            mLayoutManager = (LinearLayoutManager) mRecyclerView.getLayoutManager();
        }

        visibleItemCount = mRecyclerView.getChildCount();
        totalItemCount = mLayoutManager.getItemCount();
        firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();
        onScroll(firstVisibleItem, visibleItemCount, totalItemCount);
    }

    public void onScroll(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // If the total item count is 0 and the previous isn't, list is invalidated and should be reset
        if (totalItemCount < previousTotalItemCount) {
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) {
                this.loading = true;
            }
        }

        // If it’s still loading, we check to see if the dataset count has changed, if so we conclude it has finished
        // loading and update the current page number and total item count.
        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        // If it isn’t currently loading, we check to see if we have breached the visibleThreshold and need to reload more data.
        // If we do need to reload some more data, we execute onLoadMore to fetch the data.
        if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            loading = true;
            onLoadMore();
        }
    }

    public abstract void onLoadMore();
}
