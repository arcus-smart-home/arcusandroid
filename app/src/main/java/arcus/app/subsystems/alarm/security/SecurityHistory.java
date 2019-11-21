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
package arcus.app.subsystems.alarm.security;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Adapter;
import android.widget.ListView;

import arcus.cornea.dto.HistoryLogEntries;
import arcus.cornea.subsystem.security.SecurityStatusController;
import arcus.cornea.utils.Listeners;
import com.iris.client.bean.HistoryLog;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.subsystems.history.adapters.HistoryFragmentListAdapter;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SecurityHistory extends BaseFragment implements SecurityStatusController.HistoryLogCallback {
    private AtomicReference<String> nextToken = new AtomicReference<>(null);
    private AtomicBoolean loadingMore = new AtomicBoolean(false);
    public static final int VISIBLE_THRESHOLD = 8;
    public static final int IN_MEMORY_THRESHOLD = 2_500;
    private ListenerRegistration historyLogCallbackRef;
    private ListView listView;

    public static SecurityHistory newInstance() {
        return new SecurityHistory();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        listView = (ListView) view.findViewById(R.id.security_history_list);
        listView.setAdapter(new HistoryFragmentListAdapter(getActivity(), new ArrayList<HistoryLog>()));
        return view;
    }

    public void onResume() {
        super.onResume();
        historyLogCallbackRef = SecurityStatusController.instance().setHistoryLogCallback(this);
        ((HistoryFragmentListAdapter) listView.getAdapter()).clear();
        SecurityStatusController.instance().loadHistory(null, null);
        showProgressBar();
    }

    @Override
    public void historyLoaded(HistoryLogEntries entries) {
        hideProgressBar();

        if (listView == null) {
            return;
        }

        nextToken.set(entries.getNextToken());
        HistoryFragmentListAdapter adapter = (HistoryFragmentListAdapter) listView.getAdapter();
        adapter.appendEntries(entries.getEntries());

        loadingMore.set(false);
        setupEndlessScroll();
    }

    private void setupEndlessScroll() {
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == SCROLL_STATE_IDLE) {
                    trimEntries();
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                HistoryFragmentListAdapter adapter = getAdapter();
                if (nextToken.get() == null || adapter == null) {
                    return;
                }

                boolean topOutOfView = (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD);
                if (!loadingMore.get() && topOutOfView) {
                    endlessScroll();
                }
            }

            private void endlessScroll() {
                String token = nextToken.get();
                if (token != null) {
                    loadingMore.set(true);
                    SecurityStatusController.instance().loadHistory(null, token);
                }
                else {
                    loadingMore.set(false);
                }
            }
        });
    }

    @Nullable
    private HistoryFragmentListAdapter getAdapter() {
        if (listView == null) {
            return null;
        }

        Adapter adapter = listView.getAdapter();
        if (adapter instanceof HistoryFragmentListAdapter) {
            return (HistoryFragmentListAdapter) adapter;
        }

        return  null;
    }

    private void trimEntries() {
        HistoryFragmentListAdapter adapter = getAdapter();
        if (adapter == null) {
            return;
        }

        try {
            loadingMore.set(true); // Don't load any new entries while we are trimming.
            adapter.trimListToThreshold(IN_MEMORY_THRESHOLD);
        }
        catch (Exception ex) {
            logger.debug("Caught ex trying to trim list:", ex);
        }
        finally {
            loadingMore.set(false);
        }
    }

    public void onPause() {
        super.onPause();
        hideProgressBar();
        Listeners.clear(historyLogCallbackRef);
    }

    @Override @Nullable
    public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_listview;
    }
}
