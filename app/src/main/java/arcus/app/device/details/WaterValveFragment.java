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

import android.net.Uri;
import androidx.annotation.NonNull;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.iris.client.ClientEvent;
import com.iris.client.capability.DevicePower;
import com.iris.client.capability.Valve;
import com.iris.client.event.Listener;
import com.iris.client.util.Result;
import arcus.app.R;
import arcus.app.common.banners.SingleErrorBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.DeviceErrorType;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.GlowableImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;


public class WaterValveFragment extends ArcusProductFragment implements IShowedFragment, IClosedFragment{

    private static final Logger logger = LoggerFactory.getLogger(WaterValveFragment.class);
    private TextView batteryTopText;
    private TextView batteryBottomText;
    private ImageButton openCloseBtn;
    private boolean setChange = false;

    @NonNull
    public static WaterValveFragment newInstance() {
        WaterValveFragment fragment = new WaterValveFragment();
        return fragment;
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
        View testView = statusView.findViewById(R.id.water_valve_status_test);
        View batteryView = statusView.findViewById(R.id.water_valve_status_battery);

        TextView openBottomText = (TextView) testView.findViewById(R.id.bottom_status_text);
        TextView openTopText = (TextView) testView.findViewById(R.id.top_status_text);
        openTopText.setText(getActivity().getResources().getString(R.string.smoke_detector_last_test));
        openBottomText.setText(getLastTested());

        batteryTopText = (TextView) batteryView.findViewById(R.id.top_status_text);
        batteryBottomText = (TextView) batteryView.findViewById(R.id.bottom_status_text);

        openCloseBtn = (ImageButton) statusView.findViewById(R.id.water_valve_open_close_btn);

        updatePowerSourceAndBattery(batteryTopText, batteryBottomText);

        updateValveCloseButtonIcon();

        openCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeOrOpenValve();
            }
        });

        setImageGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);
        updateImageGlow();
    }

    @Override
    public boolean shouldGlow() {
        return getValveState().equals(Valve.VALVESTATE_OPEN);
    }

    private String getValveState(){
        final Valve valve = getCapability(Valve.class);
        if(valve !=null && valve.getValvestate()!=null){
            return valve.getValvestate();
        }
        return Valve.VALVESTATE_CLOSED;
    }

    @Override
    public void propertyUpdated(@NonNull PropertyChangeEvent event) {
        // Setting a value on the model itself causes a property change once commit is called.
        if (setChange) {
            setChange = false;
            return;
        }

        switch (event.getPropertyName()) {
            case DevicePower.ATTR_SOURCE:
            case DevicePower.ATTR_BATTERY:
                updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
                break;
            case Valve.ATTR_VALVESTATE:
                updateValveStateProperty(String.valueOf(event.getNewValue()));
                logger.debug("Water valve state changed from {} to {}", event.getOldValue(), event.getNewValue());
                break;
            default:
                super.propertyUpdated(event);
                break;
        }
    }

    private void updateValveStateProperty(@NonNull final String valveState){
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                removeErrorBanners();
                switch (valveState) {
                    case Valve.VALVESTATE_OBSTRUCTION:
                        setOpenCloseButtonEnabled(openCloseBtn, false);
                        showSingleErrorBanner();
                        break;
                    case Valve.VALVESTATE_CLOSED:
                        setOpenCloseButtonEnabled(openCloseBtn, true);
                        updateImageGlow();
                        updateValveCloseButtonIcon();
                        break;
                    case Valve.VALVESTATE_OPEN:
                        setOpenCloseButtonEnabled(openCloseBtn, true);
                        updateImageGlow();
                        updateValveCloseButtonIcon();
                        break;
                    case Valve.VALVESTATE_OPENING:
                    case Valve.VALVESTATE_CLOSING:
                    default:
                        logger.debug("Received update for [{}] of [{}]", Valve.ATTR_VALVESTATE, valveState);
                }
            }
        });
    }

    private void updateValveCloseButtonIcon() {
        int drawable = getValveState().equals(Valve.VALVESTATE_CLOSED) ? R.drawable.button_open : R.drawable.button_close;
        openCloseBtn.setBackground(getResources().getDrawable(drawable));
    }

    private void closeOrOpenValve() {
        if (getCapability(Valve.class) == null) {
            return;
        }
        if (getValveState().equals(Valve.VALVESTATE_OBSTRUCTION)) {
            setOpenCloseButtonEnabled(openCloseBtn, false);
            return;
        }

        String desiredState = Valve.VALVESTATE_CLOSED;
        if (getValveState().equals(Valve.VALVESTATE_CLOSED)) {
            desiredState = Valve.VALVESTATE_OPEN;
        }

        setChange = true;
        setOpenCloseButtonEnabled(openCloseBtn,false);
        getDeviceModel().set(Valve.ATTR_VALVESTATE, desiredState);
        getDeviceModel().commit().onCompletion(new Listener<Result<ClientEvent>>() {
            @Override
            public void onEvent(@NonNull Result<ClientEvent> clientEventResult) {
                if (clientEventResult.isError()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setOpenCloseButtonEnabled(openCloseBtn,true);
                        }
                    });
                    ErrorManager.in(getActivity()).show(DeviceErrorType.UNABLE_TO_SAVE_CHANGES);
                }
            }
        });
    }

    @Override
    public Integer statusSectionLayout() {
        return R.layout.water_valve_status;
    }

    @Override
    public void onShowedFragment() {
        checkConnection();
        checkErrorBanner();
    }

    @Override
    public void onClosedFragment() {
        removeErrorBanners();
    }

    private void showSingleErrorBanner() {
        if (getDeviceModel() == null) {
            return;
        }

        topView.setVisibility(View.INVISIBLE);

        String bannerText = getString(R.string.valve_obstruction_error);
        String buttonText = getString(R.string.get_support).toUpperCase();
        Uri buttonLink = GlobalSetting.VALVE_SUPPORT_URI;

        final SingleErrorBanner banner = new SingleErrorBanner(bannerText, buttonText, buttonLink);
        BannerManager.in(getActivity()).showBanner(banner);
    }

    protected void removeErrorBanners() {
        BannerManager.in(getActivity()).removeBanner(SingleErrorBanner.class);
    }

    private void checkErrorBanner(){
        removeErrorBanners();
        updateValveStateProperty(getValveState());
    }
}
