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
package arcus.app.subsystems.care;

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.model.SubsystemModel;
import arcus.app.R;
import arcus.app.common.fragments.HeaderNavigationViewPagerFragment;
import arcus.app.subsystems.care.fragment.CareActivityFragment;
import arcus.app.subsystems.care.fragment.CareSettingsFragment;
import arcus.app.subsystems.care.fragment.CareStatusFragment;

import java.util.ArrayList;
import java.util.List;

public class CareParentFragment extends HeaderNavigationViewPagerFragment {

    public static CareParentFragment newInstance(int showOnLoad) {
        CareParentFragment fragment = new CareParentFragment();

        Bundle args = new Bundle();
        args.putInt(SHOW_PAGE_ON_LOAD, showOnLoad);

        fragment.setArguments(args);
        return fragment;
    }

    public static CareParentFragment newInstance() {
        return newInstance(0);
    }

    @Override public View onCreateView(
          @NonNull LayoutInflater inflater,
          ViewGroup container,
          Bundle savedInstanceState)
    {
        setHasOptionsMenu(true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);

        String title = getTitle();
        if (!TextUtils.isEmpty(title)) {
            activity.setTitle(title);
        }
    }

    @Override public void onResume() {
        super.onResume();
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        int selectedPage = args.getInt(SHOW_PAGE_ON_LOAD, -1);
        if (selectedPage == -1) {
            return;
        }

        ModelSource<SubsystemModel> careSubsystem = SubsystemController.instance().getSubsystemModel(CareSubsystem.NAMESPACE);
        SubsystemModel careSubsystemModel = careSubsystem.get();
        if (careSubsystemModel == null) {
            return;
        }

        if (CareSubsystem.ALARMSTATE_ALERT.equals(careSubsystemModel.get(CareSubsystem.ATTR_ALARMSTATE))) {
            setVisiblePageToAlarmTab();
        }
    }

    public void setVisiblePageToAlarmTab() {
        setVisiblePage(1);
        setSlidingTabLayoutVisibility(View.GONE);
    }

    @NonNull @Override public String getTitle() {
        return getActivity().getString(R.string.card_care_title);
    }

    @NonNull @Override protected List<Fragment> getFragments() {
        ArrayList<Fragment> fragments = new ArrayList<>(3);

        fragments.add(CareActivityFragment.newInstance());
        fragments.add(CareStatusFragment.newInstance());
        fragments.add(CareSettingsFragment.newInstance());

        return fragments;
    }

    @NonNull @Override protected String[] getTitles() {
        return new String[] {
              getString(R.string.alarm_type_activity_title),
              getString(R.string.alarm_type_status_title),
              getString(R.string.alarm_type_more_title)
        };
    }
}
