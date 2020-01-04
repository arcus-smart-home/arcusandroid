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
package arcus.app.account.settings.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import arcus.app.account.settings.WalkthroughType;
import arcus.app.account.settings.walkthroughs.ClimateWalkthroughFragment;
import arcus.app.account.settings.walkthroughs.HistoryWalkthroughFragment;
import arcus.app.account.settings.walkthroughs.IntroductionArcusFragment;
import arcus.app.account.settings.walkthroughs.RulesWalkthroughFragment;
import arcus.app.account.settings.walkthroughs.ScenesWalkthroughFragment;
import arcus.app.account.settings.walkthroughs.SecurityWalkthroughFragment;

public class WalkthroughPagerAdapter extends FragmentStatePagerAdapter {

    private final int size;
    private final WalkthroughType mFromFrag;

    public WalkthroughPagerAdapter(FragmentManager fm, int size,  WalkthroughType fromFrag){
        super(fm);
        this.size = size;
        mFromFrag = fromFrag;
    }


    @Override
    public Fragment getItem(int position) {
        switch (mFromFrag){
            case CLIMATE:
                return ClimateWalkthroughFragment.newInstance(position);
            case HISTORY:
                return HistoryWalkthroughFragment.newInstance(position);
            case INTRO:
                return IntroductionArcusFragment.newInstance(position);
            case RULES:
                return RulesWalkthroughFragment.newInstance(position);
            case SCENES:
                return ScenesWalkthroughFragment.newInstance(position);
            case SECURITY:
                return SecurityWalkthroughFragment.newInstance(position);
            default:
                return IntroductionArcusFragment.newInstance(position);
        }
    }

    @Override
    public int getCount() {
        return size;
    }

}
