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
package arcus.app.subsystems.doorsnlocks;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.fragments.HeaderNavigationViewPagerFragment;

import java.util.ArrayList;
import java.util.List;


public class DoorsNLocksParentFragment extends HeaderNavigationViewPagerFragment {

    @NonNull
    public static DoorsNLocksParentFragment newInstance(int position){
        DoorsNLocksParentFragment fragment = new DoorsNLocksParentFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(SELECTED_POSITION,position);
        fragment.setArguments(bundle);
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
        getActivity().setTitle(getTitle());
    }

    @NonNull
    @Override
    public String getTitle() {
        return getActivity().getString(R.string.doors_and_locks_title);
    }

    @NonNull
    @Override
    protected List<Fragment> getFragments() {
        ArrayList<Fragment> fragments = new ArrayList<>();
        fragments.add(DoorsNLocksDeviceFragment.newInstance());
        fragments.add(DoorsNLocksAccessFragment.newInstance());
        fragments.add(DoorsNLocksScheduleFragment.newInstance());
        fragments.add(DoorsNLocksMoreFragment.newInstance());
        return fragments;
    }

    @NonNull
    @Override
    protected String[] getTitles() {
        return new String[] {getString(R.string.doors_devices), getString(R.string.doors_access), getString(R.string.doors_schedule), getString(R.string.doors_more)};
    }
}
