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
package arcus.app.common.fragments;

import android.os.Bundle;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.common.adapters.HeaderNavigationViewPagerAdapter;
import arcus.app.common.view.NoSwipeViewPager;
import arcus.app.common.view.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;


public class HeaderNavigationViewPagerFragment extends BaseFragment {

    protected static final String SHOW_PAGE_ON_LOAD = "show_this_page";
    protected static final String SELECTED_POSITION = "selected_position";

    private int mCurrentSelectedPosition = 0;
    private int mLastSelectedPosition = 0;

    private SlidingTabLayout mSlidingTabLayout;
    private NoSwipeViewPager mViewPager;
    private HeaderNavigationViewPagerAdapter mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(SELECTED_POSITION);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view =  super.onCreateView(inflater, container, savedInstanceState);

        mViewPager = (NoSwipeViewPager) view.findViewById(R.id.fragment_header_navigation_viewpager);
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.fragment_header_navigation_sliding_tabs);

        populate();

        mViewPager.setOffscreenPageLimit(1);

        mSlidingTabLayout.setDistributeEvenly(false);
        mSlidingTabLayout.setBackgroundColor(getResources().getColor(R.color.overlay_white_with_20));
        mSlidingTabLayout.setSelectedIndicatorColors(getResources().getColor(android.R.color.transparent));
        mSlidingTabLayout.setViewPager(mViewPager);

        mSlidingTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mLastSelectedPosition = mCurrentSelectedPosition;
                mCurrentSelectedPosition = position;
                updatePageUI(position, mLastSelectedPosition);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        Bundle args = getArguments();
        if (args != null) {
            int loadPosition = args.getInt(SHOW_PAGE_ON_LOAD, -1);
            if (loadPosition != -1) {
                mCurrentSelectedPosition = loadPosition;
            }
        }

        mViewPager.post(new Runnable() {
            @Override
            public void run() {
                mViewPager.setCurrentItem(mCurrentSelectedPosition, false);
                if (mCurrentSelectedPosition == 0) {
                    updatePageUI(mCurrentSelectedPosition, null);
                }
            }
        });

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_POSITION, mCurrentSelectedPosition);
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_header_navigation;
    }

    public void setSlidingTabLayoutVisibility (int visibility) {
        mSlidingTabLayout.setVisibility(visibility);
    }

    private void populate(){
        if(mAdapter == null) {
            mAdapter = new HeaderNavigationViewPagerAdapter(getActivity(), getChildFragmentManager(), getFragments(), getTitles());
        }
        mAdapter.notifyDataSetChanged();
        mViewPager.setAdapter(mAdapter);
    }

    private void updatePageUI(int currentPosition, Integer lastPosition){
        final BaseFragment fragment = (BaseFragment) mAdapter.getFragment(currentPosition);
        if (fragment instanceof IShowedFragment) {
            ((IShowedFragment) fragment).onShowedFragment();
        }

        // Initially there is no "last position"; don't fire any close listener
        if (lastPosition != null) {
            final BaseFragment lastFragment = (BaseFragment) mAdapter.getFragment(lastPosition);
            if (lastFragment instanceof IClosedFragment) {
                ((IClosedFragment) lastFragment).onClosedFragment();
            }
        }
    }

    protected void setVisiblePage(@IntRange(from =0) int selectedPage) {
        try {
            mLastSelectedPosition = mCurrentSelectedPosition;
            mCurrentSelectedPosition = selectedPage;

            mViewPager.setCurrentItem(mCurrentSelectedPosition, false);
            updatePageUI(mCurrentSelectedPosition, null);
        } catch (Exception ignored) {}
    }

    protected List<Fragment> getFragments() {
        ArrayList fragments = new ArrayList<>();

        return fragments;
    }

    protected String[] getTitles() {
        String[] titles = {};

        return titles;
    }
}
