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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import arcus.cornea.utils.LooperExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class CarouselViewPagerAdapter<T> extends FragmentStatePagerAdapter {
    public static final int MAX_ITEMS = 1_000; // Adjust higher if 1000 (500 on each side of center view) isn't enough
    public static final int START_ITEM = MAX_ITEMS / 2;
    private boolean useNaturalOrdering = true;
    private List<T> items;
    private SparseArray<Fragment> fragments = new SparseArray<>(4); // Assume visible offscreen pages = 1

    public CarouselViewPagerAdapter(FragmentManager fm, List<T> itemList) {
        super(fm);
        items = (itemList == null) ? new ArrayList<T>() : new ArrayList<>(itemList);
    }

    protected abstract Fragment getFragmentFor(int position);

    /**
     * If you have items 1-100 (index 0-99) and you want to have: 96 97 98 99 0 1 2 3 4 5 then set this to true (default)
     * If you want the above to be 5 4 3 2 1 0 99 98 97 96 then set this to false
     *
     * @param naturalOrdering use natural ordering or not
     */
    public void setUseNaturalOrdering(boolean naturalOrdering) {
        useNaturalOrdering = naturalOrdering;
    }

    public int inferRequestedSetIndex(int position) {
        int realCount = getRealCount();
        if (realCount < 2) {
            return position;
        }

        return (position % realCount) + getStartItem();
    }

    public int inferRequestedIndex(int position) {
        int realCount = getRealCount();
        if (realCount < 2) {
            return position;
        }

        int offset = (getStartItem() - position) % realCount;

        // if the offset is < 0 we are going to the 'left'
        if (offset < 0) {
            offset += realCount;
        }

        if (useNaturalOrdering) {
            return (offset == 0) ? offset : items.size() - offset;
        }

        return offset;
    }

    @Override public Fragment getItem(int position) {
        int offset = inferRequestedIndex(position);
        // log -> ("Adapter", "getItem: position->[" + position + "]   offset->[" + offset + "]");

        Fragment f = fragments.get(position, getFragmentFor(offset));
        fragments.put(position, f);

        return f;
    }



    @Override public void destroyItem(ViewGroup container, int position, Object object) {
        fragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public int getStartItem() {
        return START_ITEM;
    }

    @Override public int getCount() {
        int rc = getRealCount();
        if (rc < 2) {
            return rc;
        }

        return MAX_ITEMS;
    }

    public int getRealCount() {
        return items.size();
    }

    public void add(final T item) {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override public void run() {
                items.add(item);
                notifyDataSetChanged();
                notifyDataSetChanged();
            }
        });
    }

    public void remove(T item) {
        int index = items.indexOf(item);
        if (index != -1) {
            remove(index);
        }
    }

    public void remove(final int position) {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override public void run() {
                items.remove(position);
                notifyDataSetChanged();
            }
        });
    }

    public @NonNull List<T> getItems() {
        return Collections.unmodifiableList(items);
    }

    @Override public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
