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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import arcus.cornea.controller.SubscriptionController;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.popups.CheckboxListPopup;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.utils.RecyclerViewItemClickListener;
import arcus.app.subsystems.alarm.promonitoring.adapters.ProMonitoringHistoryAdapter;
import arcus.app.subsystems.alarm.promonitoring.models.HistoryListItemModel;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmActivityContract;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmActivityPresenter;

import java.util.ArrayList;
import java.util.List;

public class ProMonitoringActivityFragment extends BaseFragment implements AlarmActivityContract.AlarmActivityView, CheckboxListPopup.Callback, IShowedFragment {

    private AlarmActivityPresenter presenter = new AlarmActivityPresenter();
    private AlarmActivityContract.AlarmActivityFilter selectedFilterType = AlarmActivityContract.AlarmActivityFilter.SECURITY;
    private RecyclerView recyclerView;
    private ProMonitoringHistoryAdapter adapter;
    private TextView selectedFilter;
    private RelativeLayout filterClickRegion;
    private String selectedFilterName;
    private EndlessRecyclerViewScrollListener scrollListener;

    public static ProMonitoringActivityFragment newInstance() {
        return new ProMonitoringActivityFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = super.onCreateView(inflater, container, savedInstanceState);

        if (SubscriptionController.isPremiumOrPro()) {
            view.findViewById(R.id.history_fragment).setVisibility(View.VISIBLE);
            view.findViewById(R.id.empty_list_view).setVisibility(View.GONE);
        }

        recyclerView = (RecyclerView) view.findViewById(R.id.alarm_history_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(getContext(), new RecyclerViewItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                HistoryListItemModel model = adapter.getItemAt(position);
                if (model.getIncidentAddress() != null) {
                    BackstackManager.getInstance().navigateToFragment(ProMonitoringIncidentFragment.newInstance(model.getIncidentAddress()), true);
                }
            }
        }));
        adapter = new ProMonitoringHistoryAdapter(R.layout.cell_history_list_item_long, new ArrayList<HistoryListItemModel>());
        recyclerView.setAdapter(adapter);
        setupDefaultEndlessScroll();


        filterClickRegion = (RelativeLayout) view.findViewById(R.id.totalDeviceRelLayout);
        filterClickRegion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckboxListPopup pop = CheckboxListPopup.newInstance(getString(R.string.alarm_choose_an_alarm), Lists.newArrayList(getString(R.string.promon_filter_smoke_and_co), getString(R.string.promon_filter_security_and_panic), getString(R.string.promon_filter_water_leak)), Lists.newArrayList(selectedFilter.getText().toString()), true);
                pop.setCallback(ProMonitoringActivityFragment.this);
                pop.setOnCloseHandler(new ArcusFloatingFragment.OnCloseHandler() {
                    @Override
                    public void onClose() {
                        if(scrollListener != null) {
                            scrollListener.setLoading(true);
                        }
                        if(adapter != null) {
                            adapter.removeAll();
                        }
                        selectedFilter.setText(selectedFilterName);
                        presenter.requestUpdate(Sets.immutableEnumSet(selectedFilterType));
                        PreferenceUtils.putAlarmActivityFilter(selectedFilterName);
                    }
                });
                BackstackManager.getInstance().navigateToFloatingFragment(pop, pop.getClass().getSimpleName(), true);
            }
        });


        selectedFilter = (TextView) view.findViewById(R.id.filter);
        if(!TextUtils.isEmpty(PreferenceUtils.getAlarmActivityFilter())) {
            selectedFilter.setText(PreferenceUtils.getAlarmActivityFilter());
        } else {
            selectedFilter.setText(AlarmActivityContract.AlarmActivityFilter.SECURITY.toString(getResources()));
        }

        return view;
    }

    @NonNull
    @Override
    public String getTitle() { return ""; }

    @Override
    public Integer getLayoutId() { return R.layout.fragment_promon_activity_list; }

    @Override
    public void onResume() {
        super.onResume();
        presenter.startPresenting(this);
        presenter.requestUpdate(Sets.immutableEnumSet(AlarmActivityContract.AlarmActivityFilter.fromString(getResources(), selectedFilter.getText().toString())));
    }

    @Override
    public void onPause() {
        super.onPause();
        presenter.stopPresenting();
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {
        showProgressBar();
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        hideProgressBar();
    }

    @Override
    public void updateView(@NonNull List<HistoryListItemModel> model) {
        hideProgressBar();

        if(recyclerView != null) {
            adapter = (ProMonitoringHistoryAdapter) recyclerView.getAdapter();
            adapter.updateItems(model);
            adapter.notifyDataSetChanged();
        }

        if(scrollListener != null && model.size() > 0) {
            scrollListener.setLoading(false);
        }
    }

    @Override
    public void onItemsSelected(List<String> selections) {
        if (selections.size() > 0) {
            selectedFilterName = selections.get(0);
            selectedFilterType = AlarmActivityContract.AlarmActivityFilter.fromString(getResources(), selectedFilterName);
        }
    }

    private void setupDefaultEndlessScroll() {
        scrollListener = new EndlessRecyclerViewScrollListener(recyclerView, 20) {
            @Override
            public boolean onLoadMore() {

                // Not legal to invoke presenter if it's not presenting (i.e., has attached view...)
                if (presenter.isPresenting()) {
                    showProgressBar();
                    presenter.fetchNextSet(Sets.immutableEnumSet(selectedFilterType));
                }

                return false;
            }
        };
        // Adds the scroll listener to RecyclerView
        recyclerView.addOnScrollListener(scrollListener);
    }

    @Override
    public void onShowedFragment() {
    }
}
