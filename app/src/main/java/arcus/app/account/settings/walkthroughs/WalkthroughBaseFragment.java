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
package arcus.app.account.settings.walkthroughs;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import arcus.app.R;
import arcus.app.account.settings.WalkthroughType;
import arcus.app.account.settings.adapter.WalkthroughPagerAdapter;
import arcus.app.common.fragments.BaseFragment;

public class WalkthroughBaseFragment extends BaseFragment {
    public interface DestroyedCallback {
        void walkthroughFragmentDestroyed();
    }

    private TextView pageCountTextView;
    private WalkthroughPagerAdapter adapter;
    private Reference<DestroyedCallback> callbackRef = new WeakReference<>(null);


    @NonNull
    public static WalkthroughBaseFragment newInstance(WalkthroughType walkthroughType){
        WalkthroughBaseFragment walkthroughBaseFragment = new WalkthroughBaseFragment();
        Bundle args = new Bundle();
        args.putSerializable("WALKTHROUGH_TYPE", walkthroughType);
        walkthroughBaseFragment.setArguments(args);
        return walkthroughBaseFragment;
    }

    public void setCallback(DestroyedCallback destroyedCallback) {
        this.callbackRef = new WeakReference<>(destroyedCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        hideActionBar();
    }

    @Override
    public void onPause() {
        super.onPause();
        showActionBar();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DestroyedCallback callback = callbackRef.get();
        if (callback != null) {
            callback.walkthroughFragmentDestroyed();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle args = getArguments();
        if (args == null) {
            return;
        }

        WalkthroughType walkthroughType = (WalkthroughType) args.getSerializable("WALKTHROUGH_TYPE");
        adapter = null;
        switch(walkthroughType){
            case CLIMATE:
                adapter = new WalkthroughPagerAdapter(getFragmentManager(), 5, WalkthroughType.CLIMATE);
                break;
            case HISTORY:
                adapter = new WalkthroughPagerAdapter(getFragmentManager(), 1, WalkthroughType.HISTORY);
                break;
            case SECURITY:
                adapter = new WalkthroughPagerAdapter(getFragmentManager(), 6, WalkthroughType.SECURITY);
                break;
            case INTRO:
                adapter = new WalkthroughPagerAdapter(getFragmentManager(), 7, WalkthroughType.INTRO);
                break;
            case SCENES:
                adapter = new WalkthroughPagerAdapter(getFragmentManager(), 5, WalkthroughType.SCENES);
                break;
            case RULES:
                adapter = new WalkthroughPagerAdapter(getFragmentManager(), 6, WalkthroughType.RULES);
                break;
        }

        if (adapter == null) {
            return;
        }

        ViewPager viewPager = view.findViewById(R.id.walkthrough_view_pager);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.getCurrentItem();

        pageCountTextView = view.findViewById(R.id.page_counter);
        pageCountTextView.setText(getPageCountText(1));
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected( int i) {
                pageCountTextView.setText(getPageCountText(i + 1));
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
    }

    private String getPageCountText(int onPage) {
        return onPage + "/" + adapter.getCount();
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_walkthrough_base;
    }
}
