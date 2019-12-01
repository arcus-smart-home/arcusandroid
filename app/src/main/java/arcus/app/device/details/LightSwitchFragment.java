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

import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.iris.client.capability.PowerUse;
import com.iris.client.capability.Switch;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.banners.core.Banner;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.utils.CorneaUtils;
import arcus.app.device.details.model.LutronDisplayModel;
import arcus.app.device.details.presenters.LutronContract;
import arcus.app.device.details.presenters.LutronPresenter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;


public class LightSwitchFragment extends ArcusProductFragment implements IShowedFragment, LutronContract.LutronBridgeView {
    private static final Logger logger = LoggerFactory.getLogger(LightSwitchFragment.class);
    private final static String REPORTS_ENERGY = "REPORTS_ENERGY";


    private LutronContract.LutronPresenter presenter = new LutronPresenter();
    private ToggleButton toggleButton;
    private TextView powerUsageBottomText;
    private LinearLayout powerUsageElement;
    private boolean settingChange = false;
    private boolean hideEnergy = false;

    @NonNull
    public static LightSwitchFragment newInstance(@Nullable Boolean reportsEnergy) {
        LightSwitchFragment fragment = new LightSwitchFragment();

        Bundle bundle = new Bundle(1);
        bundle.putBoolean(REPORTS_ENERGY, reportsEnergy);
        fragment.setArguments(bundle);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Bundle args = getArguments();
        if(args != null) {
            hideEnergy = !args.getBoolean(REPORTS_ENERGY, true);
        }
    }

    @Override
    public Integer topSectionLayout() {
        return R.layout.device_top_schedule;
    }

    @Override
    public void doTopSection() {
    }

    @Override
    public void doStatusSection() {
        View energyView = statusView.findViewById(R.id.light_switch_status_energy);
        toggleButton = (ToggleButton) statusView.findViewById(R.id.light_switch_toggle_button);

        TextView powerUsageTopText = (TextView) energyView.findViewById(R.id.top_status_text);
        powerUsageBottomText = (TextView) energyView.findViewById(R.id.bottom_status_text);
        powerUsageElement = (LinearLayout) energyView.findViewById(R.id.light_switch_status_energy);
        if(hideEnergy) {
            powerUsageElement.setVisibility(View.INVISIBLE);
        } else {
            powerUsageElement.setVisibility(View.VISIBLE);
        }

        powerUsageTopText.setText(getString(R.string.energy_usage_text));
        updatePowerUsage(powerUsageBottomText);

        toggleButton.setChecked(shouldGlow());
        updateImageGlow();

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleButton.setEnabled(false);
                updateCheckedState(toggleButton.isChecked());
            }
        });
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.light_switch_status;
    }

    @Override
    public void onShowedFragment() {
        presenter.startPresenting(this);
        presenter.requestUpdate();

        checkConnection();
        boolean supportsPowerUse = CorneaUtils.hasCapability(getDeviceModel(), PowerUse.class);
        powerUsageElement.setVisibility(supportsPowerUse ? View.VISIBLE : View.GONE);
    }

    private void updateCheckedState(final boolean isChecked) {
        if (getDeviceModel() == null) {
            logger.debug("Unable to access model. Cannot change state. Model: {}", getDeviceModel());
            return;
        }

        settingChange = true;
        getDeviceModel().set(Switch.ATTR_STATE, (isChecked ? Switch.STATE_ON : Switch.STATE_OFF));
        getDeviceModel().commit().onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                // Property Change listener will update UI
                logger.error("Could not update switch state from: [{}] to [{}]", !isChecked, isChecked, throwable);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        onShowedFragment();
    }

    @Override
    public void onPause() {
        Activity activity = getActivity();
        if (activity != null) {
            presenter.clearAllBanners(activity);
        }
        presenter.stopPresenting();
        super.onPause();
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        if (settingChange) {
            settingChange = false;
            return;
        }

        boolean shouldGlow = shouldGlow();
        switch (event.getPropertyName()) {
            case Switch.ATTR_STATE:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toggleButton.setEnabled(true);
                    }
                });
                updateToggleButton(toggleButton, shouldGlow);
                updateImageGlow();
                break;
            case PowerUse.ATTR_INSTANTANEOUS:
                updatePowerUsage(powerUsageBottomText);
                break;

            default:
                super.propertyUpdated(event);
                break;
        }
    }

    @Override
    public boolean shouldGlow() {
        return getSwitchState().equals(Switch.STATE_ON);
    }

    public String getSwitchState() {
        if (getDeviceModel().get(Switch.ATTR_STATE) != null) {
            return String.valueOf(getDeviceModel().get(Switch.ATTR_STATE));
        }

        return Switch.STATE_OFF;
    }

    @Override
    public void onPending(@Nullable Integer progressPercent) {
        /* no-op */
    }

    @Override
    public void onError(Throwable throwable) {
        /* no-op */
    }

    @Override
    public void updateView(@NonNull LutronDisplayModel lutronDisplayModel) {
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

    @NonNull
    @Override
    public DeviceModel getLutronDeviceModel() {
        return getDeviceModel();
    }

    @Override
    public void showBanner(@NonNull Banner banner) {
        Activity activity = getActivity();
        if (activity != null) {
            presenter.showBannerHelper(activity, banner);
        }
    }

    @Override
    public void removeBanner(@NonNull Class<? extends Banner> bannerClass) {
        Activity activity = getActivity();
        if (activity != null) {
            presenter.clearBannerHelper(activity, bannerClass);
        }
    }
}
