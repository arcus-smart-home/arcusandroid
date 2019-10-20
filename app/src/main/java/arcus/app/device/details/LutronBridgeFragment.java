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

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.device.details.model.LutronDisplayModel;
import arcus.app.device.details.presenters.LutronContract;
import arcus.app.device.details.presenters.LutronPresenter;

public class LutronBridgeFragment extends ArcusProductFragment implements IShowedFragment, IClosedFragment, LutronContract.LutronBridgeView {
    
    private LutronContract.LutronPresenter presenter = new LutronPresenter();

    @NonNull public static LutronBridgeFragment newInstance() {
        return new LutronBridgeFragment();
    }

    @Override public Integer topSectionLayout() { return R.layout.device_top_schedule; }

    @Override public void doTopSection() {}

    @Override public Integer statusSectionLayout() { return null; }

    @Override public void doStatusSection() {}

    @Nullable
    public Integer deviceImageSectionLayout() {
        return R.layout.device_image_section;
    }

    @Override
    public void onShowedFragment() {
        presenter.startPresenting(this);
        presenter.requestUpdate();
    }

    @Override
    public void onClosedFragment() {
        BannerManager.in(getActivity()).clearBanners();
        presenter.stopPresenting();
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {
        /* no-op */
    }

    @Override public void onError(Throwable throwable) {
        /* no-op */
    }

    @Override
    public DeviceModel getLutronDeviceModel(){return getDeviceModel();}


    public void updateView(LutronDisplayModel lutronDisplayModel) {

        cloudIcon.setVisibility(lutronDisplayModel.isCloudConnected() ? View.VISIBLE : View.GONE);

        if (lutronDisplayModel.isBannerVisible()) {
            updateBackground(false);
            setBottomViewAlerting(true);
        }
        else {
            updateBackground(true);
            setBottomViewAlerting(false);
        }
    }
}
