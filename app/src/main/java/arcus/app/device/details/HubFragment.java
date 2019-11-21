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

import androidx.annotation.NonNull;
import android.view.View;
import android.widget.TextView;

import arcus.cornea.device.hub.HubMVPContract;
import arcus.cornea.device.hub.HubPresenter;
import arcus.cornea.device.hub.HubProxyModel;
import com.iris.client.capability.HubPower;
import com.iris.client.capability.Hub;
import com.iris.client.model.Model;
import arcus.app.R;
import arcus.app.common.banners.ConfigureDeviceBanner;
import arcus.app.common.banners.NoConnectionBanner;
import arcus.app.common.banners.RunningOnBatteryBanner;
import arcus.app.common.banners.ServiceSuspendedBanner;
import arcus.app.common.banners.UpdateServicePlanBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;

import java.beans.PropertyChangeEvent;

public class HubFragment extends ArcusProductFragment implements IShowedFragment, IClosedFragment, HubMVPContract.View {
    TextView powerTopText, powerBottomText, connectBottomText;
    View wirelessIcon;
    HubMVPContract.Presenter presenter;

    @NonNull public static HubFragment newInstance() {
        return new HubFragment();
    }

    @Override public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override public void doTopSection() {}

    @Override public void doStatusSection() {
        View connectView = statusView.findViewById(R.id.hub_status_connect);
        View powerView = statusView.findViewById(R.id.hub_status_power);

        TextView connectText = (TextView) connectView.findViewById(R.id.top_status_text);
        connectBottomText = (TextView) connectView.findViewById(R.id.bottom_status_text);
        connectText.setText(R.string.connect);

        powerTopText = (TextView) powerView.findViewById(R.id.top_status_text);
        powerBottomText = (TextView) powerView.findViewById(R.id.bottom_status_text);
        wirelessIcon = statusView.findViewById(R.id.wireless_icon);
    }

    @Override public Integer statusSectionLayout() {
        return R.layout.hub_status;
    }

    @Override public void show(HubProxyModel hub) {
        if (hub.isACConnection()) {
            powerTopText.setText(R.string.power);
            powerBottomText.setText(R.string.power_source_ac);
        }
        else {
            powerTopText.setText(getString(R.string.battery));
            powerBottomText.setText(hub.getBatteryLevelString());
        }

        if (hub.isBroadbandConnection()) {
            connectBottomText.setText(R.string.broadband);
        }
        else if(hub.isWifiConnection()) {
            connectBottomText.setText(getString(R.string.hub_connection_wifi).toUpperCase());
        }
        else if (hub.isCellConnection()) {
            connectBottomText.setText(getString(R.string.hub_connection_cellular).toUpperCase());
        }
        else {
            connectBottomText.setText(null);
        }

        clearCellularBanners();

        if (hub.getCellBackupModel().serviceSuspended()) {
            BannerManager.in(getActivity()).showBanner(new ServiceSuspendedBanner());
        }
        else if (hub.getCellBackupModel().requiresConfiguration()) {
            BannerManager.in(getActivity()).showBanner(new ConfigureDeviceBanner());
        }
        else if (hub.getCellBackupModel().needsServicePlan()) {
            BannerManager.in(getActivity()).showBanner(new UpdateServicePlanBanner());
        }
    }

    @Override public void onError(Throwable throwable) {
        /* no-op */
    }

    @Override public void onShowedFragment() {
        if (presenter == null) {
            presenter = new HubPresenter(this);
        }
        presenter.load();
    }

    @Override public void onClosedFragment() {
        BannerManager.in(getActivity()).clearBanners();
    }

    @Override public void onDestroyView() {
        super.onDestroyView();
        if (presenter != null) {
            presenter.clear();
        }

        presenter = null;
    }

    /**
     * Invoked when a platform property is updated. Handles the displaying of the No Connection
     * and Running on Batteries Banners.
     *
     * @param event The property change event that occurred.
     */
    @Override
    public void propertyUpdated(@NonNull final PropertyChangeEvent event) {
        switch (event.getPropertyName()) {
            case HubPower.ATTR_SOURCE:
                if(HubPower.SOURCE_BATTERY.equals(event.getNewValue())) {
                    displayRunOnBatteryBanner();
                }
                else{
                    BannerManager.in(getActivity()).removeBanner(RunningOnBatteryBanner.class);
                }
                break;
            case Hub.ATTR_STATE:
                if(Hub.STATE_DOWN.equals(event.getNewValue())) {
                    displayNoConnectionBanner();
                }
                else if(Hub.STATE_NORMAL.equals(event.getNewValue())){
                    BannerManager.in(getActivity()).removeBanner(NoConnectionBanner.class);
                    // Check battery when connection changes (ex: on battery before reboot/line power lost while hub was down)
                    Model model = getDeviceModel();
                    if (model != null) {
                        if(!HubPower.SOURCE_MAINS.equals(model.get(HubPower.ATTR_SOURCE))) {
                            displayRunOnBatteryBanner();
                        } else {
                            BannerManager.in(getActivity()).removeBanner(RunningOnBatteryBanner.class);
                        }
                    }
                }
            default:
                logger.debug("Received Hub update: {} -> {}", event.getPropertyName(), event.getNewValue());
                super.propertyUpdated(event);
                break;
        }
    }

    protected void clearCellularBanners() {
        BannerManager.in(getActivity()).removeBanner(ServiceSuspendedBanner.class);
        BannerManager.in(getActivity()).removeBanner(ConfigureDeviceBanner.class);
        BannerManager.in(getActivity()).removeBanner(UpdateServicePlanBanner.class);
    }
}
