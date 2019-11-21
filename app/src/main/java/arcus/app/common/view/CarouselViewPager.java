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
package arcus.app.common.view;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.util.AttributeSet;

public class CarouselViewPager extends ViewPager {
    CarouselViewPagerAdapter adapter;
    int currentPage = 0, lastPage = 0;

    public CarouselViewPager(Context context) {
        super(context);
    }

    public CarouselViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override public void setAdapter(PagerAdapter pagerAdapter) {
        super.setAdapter(pagerAdapter);
        if (pagerAdapter instanceof CarouselViewPagerAdapter) {
            adapter = (CarouselViewPagerAdapter) pagerAdapter;
        }

        addOnPageChangeListener(new PageChangeListener());
    }

    @Override public void setCurrentItem(int item) {
        setCurrentItem(item, false);
    }

    @Override public void setCurrentItem(int item, boolean smoothScroll) {
        if (adapter != null) {
            item = adapter.inferRequestedSetIndex(item);
        }

        super.setCurrentItem(item, smoothScroll);
    }

    @Override public int getCurrentItem() {
        int item = super.getCurrentItem();
        if (adapter != null) {
            item = adapter.inferRequestedIndex(item);
        }

        return item;
    }

    public int getRealCount() {
        return adapter == null ? getAdapter().getCount() : adapter.getRealCount();
    }

    public void scrollLeftOne() {
        int current = super.getCurrentItem();
        if (current - 1 < 0) {
            return;
        }

        super.setCurrentItem(current - 1, true);
    }

    public void scrollRightOne() {
        int current = super.getCurrentItem();
        if (current + 1 > adapter.getCount()) {
            return;
        }

        super.setCurrentItem(current + 1, true);
    }

    /**
     * Get the index in the backing array where the information is being drawn from.
     *
     * @return index in backing array
     */
    public int getItemIndexToLeft() {
        int realCount = getRealCount();
        int itemPosition = getCurrentItem() - 1;
        return (itemPosition < 0) ? realCount - 1 : itemPosition;
    }

    /**
     * Get the index in the backing array where the information is being drawn from.
     *
     * @return index in backing array
     */
    public int getItemIndexToRight() {
        int realCount = getRealCount();
        int itemPosition = getCurrentItem() + 1;
        return (itemPosition == realCount) ? 0 : itemPosition;
    }

    public @Nullable Fragment getCurrentFragment() {
        return adapter.getItem(super.getCurrentItem());
    }

    public @Nullable Fragment getLastFragment() { return adapter.getItem(lastPage); }

    class PageChangeListener implements ViewPager.OnPageChangeListener {
        @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
        @Override public void onPageScrollStateChanged(int state) {}
        @Override public void onPageSelected(int position) {
            lastPage = currentPage;
            currentPage = position;
        }
    }
}
