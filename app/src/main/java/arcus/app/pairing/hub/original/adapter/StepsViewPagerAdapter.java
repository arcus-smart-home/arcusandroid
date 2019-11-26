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
package arcus.app.pairing.hub.original.adapter;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class StepsViewPagerAdapter extends FragmentPagerAdapter {

    private List<Fragment> fragments;

    private FragmentManager fm;
    private Map<Integer,String> fragmentTags;

    private Context context;

    public StepsViewPagerAdapter(Context context,FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        this.fragments = fragments;
        this.fm = fm;
        this.context = context;
        fragmentTags = new HashMap<>();
    }

    @Override
    public Fragment getItem(int i) {
        return fragments.get(i);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @NonNull
    @Override
    public CharSequence getPageTitle(int position) {
        return "";
    }

    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Object object = super.instantiateItem(container, position);
        if(object instanceof Fragment){
            //record fragment tag here
            Fragment f = (Fragment) object;
            String tag = f.getTag();
            fragmentTags.put(position, tag);
        }
        return object;
    }

    public void addFragment(@NonNull Class<Fragment> fragment, String title,Bundle b){
        fragments.add(Fragment.instantiate(context,fragment.getName(),b));
    }

    @Nullable
    public Fragment getFragment(int position){
        String tag = fragmentTags.get(position);
        if(tag ==null) return null;
        return fm.findFragmentByTag(tag);
    }
}
