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
package arcus.app.device.adapter;

import android.content.res.Resources;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import arcus.app.R;

import java.util.List;

public class DeviceDetailViewPagerAdapter extends FragmentPagerAdapter {
    @NonNull
    private final String[] tabTitles;
    private final List<Fragment> fragments;

    public DeviceDetailViewPagerAdapter(
            Resources resources,
            FragmentManager fragmentManager,
            List<Fragment> fragments
    ) {
        super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        this.fragments = fragments;
        tabTitles = new String[] {
                resources.getString(R.string.history_filter_device),
                resources.getString(R.string.homenfamily_tab_more)
        };
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return tabTitles.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
