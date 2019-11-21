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
package arcus.app.device.details.garage;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.iris.client.ClientEvent;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.DevicePower;
import com.iris.client.capability.MotorizedDoor;
import com.iris.client.event.Listener;
import com.iris.client.util.Result;
import arcus.app.R;
import arcus.app.common.banners.HueNotPairedBanner;
import arcus.app.common.banners.ImageAndTextBanner;
import arcus.app.common.banners.MultipleErrorsBanner;
import arcus.app.common.banners.core.BannerManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.DeviceErrorType;
import arcus.app.common.fragments.IClosedFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.GlowableImageView;
import arcus.app.device.details.ArcusProductFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.util.HashMap;
import java.util.Map;


public class GarageDoorFragment extends ArcusProductFragment implements IShowedFragment, IClosedFragment {

    private static final Logger logger = LoggerFactory.getLogger(GarageDoorFragment.class);
    private ImageButton openCloseBtn;
    private boolean setChange = false;
    private TextView batteryTopText;
    private TextView batteryBottomText;

    @NonNull
    public static GarageDoorFragment newInstance() {
        GarageDoorFragment fragment = new GarageDoorFragment();
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
        openCloseBtn = (ImageButton) statusView.findViewById(R.id.garage_door_open_close_btn);
        batteryTopText = (TextView) statusView.findViewById(R.id.top_status_text);
        batteryBottomText = (TextView) statusView.findViewById(R.id.bottom_status_text);

        updateGarageDoorCloseButtonIcon(getGarageDoorState());

        openCloseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeOrOpenGarageDoor();
            }
        });

        setImageGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);
        updateImageGlow();

        updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
    }


    @Override
    public boolean shouldGlow() {
        return getGarageDoorState().equals(MotorizedDoor.DOORSTATE_OPEN) ||
                getGarageDoorState().equals(MotorizedDoor.DOORSTATE_CLOSING);
    }

    @Override
    public void propertyUpdated(@NonNull final PropertyChangeEvent event) {
        // Setting a value on the model itself causes a property change once commit is called.
        if (setChange) {
            setChange = false;
            return;
        }

        switch (event.getPropertyName()) {
            case DevicePower.ATTR_BATTERY:
                updatePowerSourceAndBattery(batteryTopText, batteryBottomText);
                break;
            case MotorizedDoor.ATTR_DOORSTATE:
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateGarageDoorCloseButtonIcon(String.valueOf(event.getNewValue()));
                    }
                });
                logger.debug("Garage door state changed from {} to {}", event.getOldValue(), event.getNewValue());
                break;
            default:
                super.propertyUpdated(event);
                break;
        }
        updateBanner();
    }

    private String getGarageDoorState(){
        final MotorizedDoor motorizedDoor = getCapability(MotorizedDoor.class);
        if(motorizedDoor !=null && motorizedDoor.getDoorstate()!=null){
            return motorizedDoor.getDoorstate();
        }
        return MotorizedDoor.DOORSTATE_CLOSED;
    }

    private void updateGarageDoorCloseButtonIcon(@NonNull final String garageDoorState) {
        int drawable;
        switch (garageDoorState) {
            case MotorizedDoor.DOORSTATE_CLOSED:
                drawable = R.drawable.button_open;
                openCloseBtn.setBackground(getResources().getDrawable(drawable));
                if(getErrors().size() > 0) {
                    setOpenCloseButtonEnabled(openCloseBtn, false);
                } else {
                    setOpenCloseButtonEnabled(openCloseBtn, true);
                }
                break;
            case MotorizedDoor.DOORSTATE_OPEN:
                drawable = R.drawable.button_close;
                openCloseBtn.setBackground(getResources().getDrawable(drawable));
                if(getErrors().size() > 0) {
                    setOpenCloseButtonEnabled(openCloseBtn, false);
                } else {
                    setOpenCloseButtonEnabled(openCloseBtn, true);
                }
                break;
            case MotorizedDoor.DOORSTATE_CLOSING:
                drawable = R.drawable.closing_61x61;
                openCloseBtn.setBackground(getResources().getDrawable(drawable));
                setOpenCloseButtonEnabled(openCloseBtn, false);
                break;
            case MotorizedDoor.DOORSTATE_OPENING:
                drawable = R.drawable.opening_61x61;
                openCloseBtn.setBackground(getResources().getDrawable(drawable));
                setOpenCloseButtonEnabled(openCloseBtn, false);
                break;
            case MotorizedDoor.DOORSTATE_OBSTRUCTION:
                //todo: display error message?
                drawable = R.drawable.obstructed_61x61;
                openCloseBtn.setBackground(getResources().getDrawable(drawable));
                setOpenCloseButtonEnabled(openCloseBtn,false);
                logger.error("Garage door obstruction: {}", garageDoorState);
                break;
            default:
                logger.debug("Received update for [{}] of [{}]", MotorizedDoor.ATTR_DOORSTATE, garageDoorState);
        }
        updateImageGlow();
    }

    private void closeOrOpenGarageDoor() {
        if (getCapability(MotorizedDoor.class) == null) {
            return;
        }

        String desiredState = MotorizedDoor.DOORSTATE_CLOSED;
        if (getGarageDoorState().equals(MotorizedDoor.DOORSTATE_CLOSED)) {
            desiredState = MotorizedDoor.DOORSTATE_OPEN;
        }

        setChange = true;
        setOpenCloseButtonEnabled(openCloseBtn,false);
        getDeviceModel().set(MotorizedDoor.ATTR_DOORSTATE, desiredState);
        getDeviceModel().commit().onCompletion(new Listener<Result<ClientEvent>>() {
            @Override
            public void onEvent(@NonNull Result<ClientEvent> clientEventResult) {
                if (clientEventResult.isError()) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setOpenCloseButtonEnabled(openCloseBtn, true);
                        }
                    });
                    ErrorManager.in(getActivity()).show(DeviceErrorType.UNABLE_TO_SAVE_CHANGES);
                }
            }
        });
        // State of the fake world - keep a copy of the model so we can show the correct state in the UI until the event change
        getDeviceModel().set(MotorizedDoor.ATTR_DOORSTATE,
                MotorizedDoor.DOORSTATE_OPEN.equals(desiredState) ? MotorizedDoor.DOORSTATE_OPENING : MotorizedDoor.DOORSTATE_CLOSING);
    }


    @Override
    public Integer statusSectionLayout() {
        return R.layout.garage_door_status;
    }

    @Override
    public void onShowedFragment() {
        updateGarageDoorCloseButtonIcon(getGarageDoorState());
        checkConnection();
        updateBanner();
    }



    @Override
    public void onClosedFragment() {
        BannerManager.in(getActivity()).removeBanner(ImageAndTextBanner.class);
        BannerManager.in(getActivity()).removeBanner(MultipleErrorsBanner.class);
        BannerManager.in(getActivity()).removeBanner(HueNotPairedBanner.class);
    }

    private void updateBanner() {
        if (getDeviceModel() != null) {
            int errorCount = getErrors().size();

            if (errorCount > 1) {
                //showMultipleErrorBanner();
            } else if (errorCount == 1) {
                showSingleErrorBanner(getErrors());
            } else if (errorCount == 0) {
                BannerManager.in(getActivity()).removeBanner(ImageAndTextBanner.class);
                BannerManager.in(getActivity()).removeBanner(MultipleErrorsBanner.class);
                BannerManager.in(getActivity()).removeBanner(HueNotPairedBanner.class);
            }
        }
    }

    private void showSingleErrorBanner(@NonNull Map<String, String> errorMap) {
        String errorId = "";
        String errorDesc = "";

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                setOpenCloseButtonEnabled(openCloseBtn, false);
            }
        });
        for (Map.Entry<String, String> entry : errorMap.entrySet()) {
            errorId = entry.getKey();
            errorDesc = entry.getValue();
        }

        if(errorId.equals("ERR_OBSTRUCTION")) {
            String link = GlobalSetting.getDeviceSupportUri(getDeviceModel(), errorId);
            final HueNotPairedBanner banner = new HueNotPairedBanner(getString(R.string.obstruction_detected), link, ContextCompat.getColor(getContext(), R.color.pink_banner), false);
            BannerManager.in(getActivity()).showBanner(banner);
        }
    }

    private Map<String, String> getErrors() {
        Map<String, String> errorMap = new HashMap<>();

        if (getDeviceModel() != null && ((DeviceAdvanced)getDeviceModel()).getErrors() != null) {
            errorMap = ((DeviceAdvanced)getDeviceModel()).getErrors();
        }

        return errorMap;
    }
}
