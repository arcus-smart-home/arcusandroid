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
package arcus.app.subsystems.alarm.promonitoring;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * onScrolled from http://stackoverflow.com/questions/31000964/how-to-implement-setonscrolllistener-in-recyclerview
 */

public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {
    private int visibleThreshold = 5;
    private boolean loading = true;
    LinearLayoutManager linearLayoutManager = null;
    int totalItemCount = -1;
    int lastVisibleItem = -1;
    RecyclerView recyclerView;

    public EndlessRecyclerViewScrollListener(RecyclerView recyclerView) {
        this.recyclerView = recyclerView;
        linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    }

    public EndlessRecyclerViewScrollListener(RecyclerView recyclerView, int visibleThreshold) {
        this.recyclerView = recyclerView;
        linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        this.visibleThreshold = visibleThreshold;
    }

    @Override
    public void onScrolled(RecyclerView recyclerView,
                           int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);

        totalItemCount = linearLayoutManager.getItemCount();
        lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition();
        if (!loading
                && totalItemCount <= (lastVisibleItem + visibleThreshold)) {
                onLoadMore();
            loading = true;
        }

    }

    public abstract boolean onLoadMore();
    public void setLoading(boolean loading) {
        this.loading = loading;
    }

}
