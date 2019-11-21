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
package arcus.app.device.details;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import android.view.View;

import arcus.app.R;
import arcus.app.common.banners.HueNotPairedBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.image.ImageManager;


public class HueFallbackFragment extends ArcusProductFragment implements IShowedFragment, IClosedFragment {

    @NonNull
    public static HueFallbackFragment newInstance(){
        HueFallbackFragment fragment = new HueFallbackFragment();

        return fragment;
    }


    @Override
    public void doDeviceImageSection() {
        super.doDeviceImageSection();
    }

    @Override
    public Integer topSectionLayout() {
        return null;
    }

    @Override
    public void doTopSection() {

    }

    @Override public void doStatusSection() {
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.hue_fallback_status;
    }

    @Override
    protected ColorFilter getOnlineColorFilter() {
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0f);
        return new ColorMatrixColorFilter(cm);
    }

    @Override
    public int getColorFilterValue() {
        float[] colorHSV = new float[] { 0f, 0f, 0.5f };
        return Color.HSVToColor(colorHSV);
    }

    @Override
    public void updateBackground(boolean isConnected) {
        try {
            ColorMatrix cm = new ColorMatrix();
            cm.setSaturation(0f);
            final ColorFilter filter = new ColorMatrixColorFilter(cm);
            final View bgView = ImageManager.getWallpaperView();
            if (bgView != null) {
                final Drawable bgDrawable = bgView.getBackground();
                if (bgDrawable != null) {
                    bgDrawable.setColorFilter(filter);
                }
            }
        }catch (Exception e){
            logger.error("Can't change background color filter: {}", e);
        }
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
        BannerManager.in(getActivity()).showBanner(new HueNotPairedBanner());
    }

    @Override
    public void onClosedFragment() {
        BannerManager.in(getActivity()).removeBanner(HueNotPairedBanner.class);
    }
}
