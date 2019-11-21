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
package arcus.app.subsystems.lawnandgarden;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.fragments.HeaderNavigationViewPagerFragment;
import arcus.app.subsystems.lawnandgarden.fragments.LawnAndGardenMoreFragment;

import java.util.ArrayList;
import java.util.List;


public class LawnAndGardenParentFragment extends HeaderNavigationViewPagerFragment {

    @NonNull
    public static LawnAndGardenParentFragment newInstance(int position){
        LawnAndGardenParentFragment fragment = new LawnAndGardenParentFragment();
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

    @NonNull
    @Override
    public String getTitle() {
        return getActivity().getString(R.string.lawn_and_garden);
    }

    @NonNull
    @Override
    protected List<Fragment> getFragments() {
        ArrayList fragments = new ArrayList<>();
        fragments.add(LawnAndGardenFragment.newInstance());
        fragments.add(LawnAndGardenMoreFragment.newInstance());
        /*fragments.add(TemperatureDevicesFragment.newInstance());
        fragments.add(MoreFragment.newInstance());*/
        return fragments;
    }

    @NonNull
    @Override
    protected String[] getTitles() {
        return new String[] {"DEVICES", "MORE"};
    }
}
