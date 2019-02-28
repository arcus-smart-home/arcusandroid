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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.common.PresentedView;
import arcus.app.R;
import arcus.app.common.fragments.HeaderNavigationViewPagerFragment;
import arcus.app.subsystems.alarm.promonitoring.presenters.AlarmProviderOfflinePresenter;

import java.util.ArrayList;
import java.util.List;


public class ProMonitoringAlarmParentFragment extends HeaderNavigationViewPagerFragment implements PresentedView {

    private AlarmProviderOfflinePresenter presenter = new AlarmProviderOfflinePresenter();

    @NonNull
    public static ProMonitoringAlarmParentFragment newInstance(int showOnLoad){
        ProMonitoringAlarmParentFragment fragment = new ProMonitoringAlarmParentFragment();
        Bundle args = new Bundle();
        args.putInt(SHOW_PAGE_ON_LOAD, showOnLoad);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.startPresenting(this);
        getActivity().setTitle(getTitle());
    }


    @Override
    public void onPause() {
        super.onPause();
        presenter.stopPresenting();
    }

    @NonNull
    @Override
    public String getTitle() {
        return getActivity().getString(R.string.alarms).toUpperCase();
    }

    @NonNull
    @Override
    protected List<Fragment> getFragments() {
        ArrayList<Fragment> fragments = new ArrayList<>(3);

        fragments.add(ProMonitoringStatusFragment.newInstance());
        fragments.add(ProMonitoringActivityFragment.newInstance());
        fragments.add(ProMonitoringMoreFragment.newInstance());

        return fragments;
    }

    @NonNull
    @Override
    protected String[] getTitles() {
        return new String[] {
                getString(R.string.alarm_type_status_title),
                getString(R.string.alarm_type_activity_title),
                getString(R.string.alarm_type_more_title)
        };
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {
        // Nothing to do.
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        // Nothing to do.
    }

    @Override
    public void updateView(@NonNull Object model) {
        // Nothing to do.
    }
}
