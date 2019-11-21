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
package arcus.app.subsystems.care.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import arcus.cornea.subsystem.care.CareHistoryController;
import arcus.cornea.subsystem.model.CareHistoryModel;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.subsystems.care.adapter.CareHistoryListAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;


public class CareAlarmHistory extends BaseFragment implements CareHistoryController.Callback {
    private ListView listView;
    private AtomicReference<String> nextToken = new AtomicReference<>(null);
    private AtomicBoolean loadingMore = new AtomicBoolean(false);
    public static final int VISIBLE_THRESHOLD = 8;
    private int lastHeaderDay = -1;

    private ListenerRegistration listenerRegistration;
    private CareHistoryListAdapter careHistoryListAdapter;

    private final AbsListView.OnScrollListener scrollListener = new AbsListView.OnScrollListener() {
        @Override public void onScrollStateChanged(AbsListView view, int scrollState) {}
        @Override public void onScroll(
              AbsListView view,
              int firstVisibleItem,
              int visibleItemCount,
              int totalItemCount
        ) {
            String token = nextToken.get();
            if (TextUtils.isEmpty(token)) {
                loadingMore.set(false);
                return;
            }

            boolean topOutOfView = (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD);
            if (!loadingMore.get() && topOutOfView) {
                loadingMore.set(true);
                CareHistoryController.instance().loadHistory(null, token);
            }
        }
    };

    @Override public View onCreateView(
          @NonNull LayoutInflater inflater,
          ViewGroup container,
          Bundle savedInstanceState
    ) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);
        listView = (ListView) view.findViewById(R.id.security_history_list);
        return view;
    }

    @NonNull @Override public String getTitle() {
        return "Care Alarm History";
    }

    @Override public Integer getLayoutId() {
        return R.layout.fragment_listview;
    }

    @Override public void onResume() {
        super.onResume();
        listenerRegistration = CareHistoryController.instance().setCallback(this);
        if (careHistoryListAdapter == null) {
            CareHistoryController.instance().loadHistory(null, null);
        }
    }

    @Override public void onPause() {
        super.onPause();
        Listeners.clear(listenerRegistration);
    }

    @Override public void onError(Throwable cause) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override public void historyLoaded(List<CareHistoryModel> entries, @Nullable String nextHistoryToken) {
        if (listView == null) {
            return;
        }

        listView.setOnScrollListener(scrollListener);
        nextToken.set(nextHistoryToken);
        loadingMore.set(false);

        List<CareHistoryModel> models = new ArrayList<>(entries.size());
        for (CareHistoryModel model : entries) {
            if (model.getCalendarDayOfYear() != lastHeaderDay) {
                models.add(model.headerCopy());
                lastHeaderDay = model.getCalendarDayOfYear();
            }

            models.add(model);
        }

        if (careHistoryListAdapter != null) {
            careHistoryListAdapter.addAll(models);
        }
        else {
            try {
                careHistoryListAdapter = new CareHistoryListAdapter(getActivity(), models);
                listView.setDivider(null);
                listView.setAdapter(careHistoryListAdapter);
            } catch (NullPointerException exception) {
                //nothing to do right now
            }
        }
    }
}
