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

import android.app.Activity;
import androidx.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;


public abstract class AbstractBanner implements Banner {

    @Nullable
    private Activity activity;
    private int viewResourceId;
    private BannerAdapter bannerAdapter;

    public AbstractBanner (int viewResourceId) {
        this.viewResourceId = viewResourceId;
    }

    public int getViewResourceId() {
        return viewResourceId;
    }

    public void setViewResourceId(int viewResId) {
        this.viewResourceId = viewResId;
    }

    @Nullable
    public Activity getActivity() {
        return activity;
    }

    public void setActivity(@Nullable Activity activity) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity cannot be set to null.");
        }

        this.activity = activity;
    }

    public View getBannerView(ViewGroup parent) {
        if (activity == null) {
            throw new IllegalStateException("Activity cannot be null; did BannerManager inject an activity into this banner?");
        }

        return getActivity().getLayoutInflater().inflate(getViewResourceId(), parent, false);
    }

    public BannerAdapter getBannerAdapter() {
        return bannerAdapter;
    }

    @Override
    public void setBannerAdapter(BannerAdapter bannerAdapter) {
        this.bannerAdapter = bannerAdapter;
    }
}
