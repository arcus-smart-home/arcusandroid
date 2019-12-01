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
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.iris.client.capability.Capability;
import com.iris.client.capability.Cloud;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.model.DeviceModel;
import arcus.app.ArcusApplication;
import arcus.app.common.banners.LutronAccountRevokedBanner;
import arcus.app.common.banners.LutronBridgeErrorBanner;
import arcus.app.common.banners.LutronDeviceDeletedBanner;
import arcus.app.common.banners.core.Banner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.device.details.model.LutronDisplayModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;



public class LutronPresenter extends DevicePresenter<LutronContract.LutronBridgeView> implements LutronContract.LutronPresenter, PropertyChangeListener {
    protected final static Logger logger = LoggerFactory.getLogger(LutronPresenter.class);

    public final static String ERROR_DELETED_LUTRON = "ERR_DELETED_LUTRON";
    public final static String ERROR_UNAUTHED_LUTRON = "ERR_UNAUTHED_LUTRON";
    public final static String ERROR_BRIDGE_LUTRON = "ERR_BRIDGE_LUTRON";

    private Set<Class> presentedBanners = new HashSet<>();
    private boolean isErrorBannerVisible = false;

    @Override
    public void startPresenting(@Nullable LutronContract.LutronBridgeView presentedView) {
        super.startPresenting(presentedView);
    }

    @Override
    public void clearAllBanners(@NonNull Activity activity) {
        removeBanner(activity, LutronDeviceDeletedBanner.class);
        removeBanner(activity, LutronAccountRevokedBanner.class);
        removeBanner(activity, LutronBridgeErrorBanner.class);
    }

    @Override
    public void requestUpdate() {
        final LutronDisplayModel lutronDisplayModel = new LutronDisplayModel();

        Map<String,String> errors = get(DeviceAdvanced.class).getErrors();
        String deviceAddress = getDeviceModel().getAddress();

        isErrorBannerVisible = false;

        if (!isDeviceConnected()) {
            isErrorBannerVisible = true;
        }

        if (!isErrorBannerVisible && errors != null && errors.containsKey(ERROR_DELETED_LUTRON)) {
            getPresentedView().showBanner(new LutronDeviceDeletedBanner(deviceAddress,
                    GlobalSetting.getDeviceSupportUri(getDeviceModel(), ERROR_DELETED_LUTRON.toLowerCase())));
            isErrorBannerVisible = true;
        } else {
            getPresentedView().removeBanner(LutronDeviceDeletedBanner.class);
        }

        if (!isErrorBannerVisible && errors != null && errors.containsKey(ERROR_UNAUTHED_LUTRON)) {
            getPresentedView().showBanner(new LutronAccountRevokedBanner(deviceAddress,
                    GlobalSetting.getDeviceSupportUri(getDeviceModel(), ERROR_UNAUTHED_LUTRON.toLowerCase())));
            isErrorBannerVisible = true;
        } else {
            getPresentedView().removeBanner(LutronAccountRevokedBanner.class);
        }

        if (!isErrorBannerVisible && errors != null && errors.containsKey(ERROR_BRIDGE_LUTRON)) {
            getPresentedView().showBanner(new LutronBridgeErrorBanner(
                    GlobalSetting.getDeviceSupportUri(getDeviceModel(), ERROR_BRIDGE_LUTRON.toLowerCase())));
            isErrorBannerVisible = true;
        } else {
            getPresentedView().removeBanner(LutronBridgeErrorBanner.class);
        }

        lutronDisplayModel.setBannerVisible(isErrorBannerVisible);

        Collection<String> caps = getDeviceModel().getCaps() == null ? Collections.<String>emptySet() : getDeviceModel().getCaps();
        lutronDisplayModel.setCloudConnected(caps.contains(Cloud.NAMESPACE));

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                if (isPresenting()) {
                    logger.debug("Presenting Lutron Bridge Display Model model: {}", lutronDisplayModel);
                    getPresentedView().updateView(lutronDisplayModel);
                }
            }
        });
    }

    @Override
    public void propertyChange(PropertyChangeEvent event) {
        if (isPresenting()) {
            requestUpdate();
        }
    }

    public DeviceModel getDeviceModel() {
        return getPresentedView().getLutronDeviceModel();
    }

    private void onError(Throwable throwable) {
        getPresentedView().onError(throwable);
    }

    private <C extends Capability> C get(Class<C> clazz) {
        C cap = CorneaUtils.getCapability(getDeviceModel(), clazz);

        if (cap == null) {
            throw new IllegalStateException("Bug! The Lutron Bridge does not support the capability: " + clazz.getSimpleName());
        }

        return cap;
    }

    private boolean isDeviceConnected() {
        if (getDeviceModel() == null) {
            return false;
        }

        return !DeviceConnection.STATE_OFFLINE.equals(getDeviceModel().get(DeviceConnection.ATTR_STATE));
    }

    @Override
    public void showBannerHelper(@NonNull Activity activity, @NonNull Banner banner) {
        showBanner(activity, banner);
    }

    private void showBanner(Activity activity, final Banner banner) {
        presentedBanners.add(banner.getClass());
        BannerManager.in(activity).showBanner(banner);
    }

    @Override
    public void clearBannerHelper(@NonNull Activity activity, @NonNull Class<? extends Banner> bannerClass) {
        removeBanner(activity, bannerClass);
    }

    private void removeBanner(Activity activity, Class<? extends Banner> bannerClazz) {
        presentedBanners.remove(bannerClazz);
        BannerManager.in(activity).removeBanner(bannerClazz);
    }

}
