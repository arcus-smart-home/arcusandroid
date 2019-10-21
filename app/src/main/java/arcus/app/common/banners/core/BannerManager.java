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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ListView;

import arcus.app.ArcusApplication;
import arcus.app.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class BannerManager {

    private final static Logger logger = LoggerFactory.getLogger(BannerManager.class);

    private BannerManager () {
    }

    @NonNull
    public static BannerManager in (Activity activity) {
        return new BannerManager();
    }

    public boolean showBanner(@NonNull final Banner banner) {

        logger.debug("Attempting to show banner {}.", banner.getClass().getSimpleName());

        if (getBannerActivity() == null) {
            logger.error("Bug! Attempt to showBanner() with null activity. Are you invoking BannerManager from an unattached lifecycle phase?");
            return false;
        }

        banner.setBannerAdapter(getBanners());
        banner.setActivity(getActivity());

        final ListView bannersView = (ListView) getActivity().findViewById(R.id.banner_list);
        if (bannersView == null) {
            logger.warn("Not able to find/inflate banners view in this context ({}); banner will not be shown.", getActivity());
            return false;
        }

        if (getBanners() != null) {

            // Since we seem to only want to show 1 banner at a time this could be a good time to revamp BannerManager?
            // However, if we do stick with this (ListView), we could attach all banners and as each one gets addressed,
            // and then later removed, we could show the 'next in line' by doing a simple display trick in the adapter
            // Manipulate the item count -> return super.getCount() > 0 ? 1 : 0.
            getActivity().runOnUiThread(new Runnable() {
                @Override public void run() {
                    BannerAdapter bannerAdapter = getBanners();
                    if (BannerManagerHelper.canShowBanner(getActivity(), banner) && bannerAdapter != null) {
                        bannerAdapter.clear();
                        bannerAdapter.add(banner);

                        bannersView.setAdapter(bannerAdapter);
                        logger.debug("Attaching banner list with {} banners visible.", getBanners().getCount());
                    }
                }
            });

            return true;
        }

        else {
            logger.error("No banner list defined in this activity; banner {} will not be shown.", banner.getClass().getSimpleName());
        }

        return false;
    }

    public void removeBanner(@NonNull final Class<? extends Banner> bannerClass) {

        if (getBannerActivity() == null) {
            logger.error("Bug! Attempt to removeBanner() with null activity. Are you invoking BannerManager from an unattached lifecycle phase?");
            return;
        }

        if (getBanners() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getBanners().removeBanner(bannerClass);
                    getBanners().notifyDataSetChanged();
                }
            });
        }
    }

    public void clearBanners() {

        if (getBannerActivity() == null) {
            logger.error("Bug! Attempt to clearBanners() with null activity. Are you invoking BannerManager from an unattached lifecycle phase?");
            return;
        }

        BannerAdapter adapter = getBanners();
        if (adapter != null) {
            adapter.clear();
        }
    }

    @Nullable
    private BannerAdapter getBanners() {

        if (getBannerActivity() == null) {
            logger.error("Bug! Attempt to getBanners() with null activity. Are you invoking BannerManager from an unattached lifecycle phase?");
            return null;
        }

            return getBannerActivity().getBanners();
    }

    public boolean containsBanner(@NonNull Class<? extends Banner> bannerClass) {

        if (getBannerActivity() == null) {
            logger.error("Bug! Call to containsBanner() with null activity. Are you invoking BannerManager from an unattached lifecycle phase?");
            return false;
        }

            if(getBannerActivity().getBanners() != null) {
                return getBannerActivity().getBanners().containsBanner(bannerClass);
            }
            return false;
    }

    private Activity getActivity() {
        return ArcusApplication.getArcusApplication().getForegroundActivity();
    }

    private BannerActivity getBannerActivity() {
        Activity activity = ArcusApplication.getArcusApplication().getForegroundActivity();
        if (activity == null || !(activity instanceof BannerActivity)) {
            return null;
        }

        return (BannerActivity) activity;

    }
}
