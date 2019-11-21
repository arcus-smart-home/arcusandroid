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
package arcus.app.common.banners.core;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;


public class BannerAdapter extends ArrayAdapter<Banner> {

    public BannerAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getItem(position).getBannerView(parent);
    }

    /**
     * Gets the visible banner associated with the given banner class, or null if a banner of
     * the request type is not visible.
     *
     * @param bannerClass
     * @param <T>
     * @return
     */
    @Nullable
    public <T extends Banner> T getBanner (@NonNull Class<T> bannerClass) {
        for (int index = 0; index < getCount(); index++) {
            Banner thisBanner = getItem(index);
            if (bannerClass.equals(thisBanner.getClass())) {
                return (T) thisBanner;
            }
        }

        return null;
    }

    /**
     * Adds the provided banner to the list of visible banners. Note that only banner instance of
     * a given type is allowed to be displayed at once. If a banner of the requested type is already
     * present, no action will be taken.
     *
     * @param banner
     */
    @Override
    public void add (@NonNull Banner banner) {
        if (!containsBanner(banner.getClass())) {
            super.add(banner);
        }
    }

    /**
     * Determines if a banner of the given class is currently displayed.
     * @param bannerClass
     * @return
     */
    public boolean containsBanner (@NonNull Class<? extends Banner> bannerClass) {
        return getBanner(bannerClass) != null;
    }

    /**
     * Attempts to remove the banner matching the given banner class.
     * @param bannerClass
     */
    public void removeBanner (@NonNull Class<? extends Banner> bannerClass) {
        Banner banner = getBanner(bannerClass);

        if (banner != null) {
            remove(banner);
            notifyDataSetChanged();
        }
    }
}
