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
package arcus.app.device.details.presenters;

import android.app.Activity;

import androidx.annotation.NonNull;

import arcus.app.common.banners.core.Banner;
import arcus.cornea.common.PresentedView;
import arcus.cornea.common.Presenter;
import com.iris.client.model.DeviceModel;
import arcus.app.device.details.model.LutronDisplayModel;



public interface LutronContract {

    interface LutronBridgeView extends PresentedView<LutronDisplayModel> {

        void onError(Throwable throwable);

        /**
         * Invoked by the presenter to get the DeviceModel.
         *
         * @return The DeviceModel. Cannot be null.
         */
        @NonNull DeviceModel getLutronDeviceModel();

        /**
         * Called when a banner needs to be displayed.
         * @param banner banner to show.
         */
        void showBanner(@NonNull Banner banner);

        /**
         * Called when a banner needs to be displayed.
         * @param bannerClass banner class to remove.
         */
        void removeBanner(@NonNull Class<? extends Banner> bannerClass);
    }

    interface LutronPresenter extends Presenter<LutronContract.LutronBridgeView> {

        /**
         * Requests that the presenter refresh the view.
         */
        void requestUpdate();

        /**
         * Helper to remove all banners...
         * @param activity current activity
         */
        void clearAllBanners(@NonNull Activity activity);

        /**
         * Helper to clear banners so that the foreground activity doesn't need to be tracked in the application object...
         * @param activity current activity
         * @param bannerClass the banner class to remove
         */
        void clearBannerHelper(@NonNull Activity activity, @NonNull Class<? extends Banner> bannerClass);

        /**
         * Helper to show a banner so the foreground activity doesn't need to be tracked in the application object...
         * @param activity current activity
         * @param banner the banner to show
         */
        void showBannerHelper(@NonNull Activity activity, @NonNull Banner banner);
    }
}
