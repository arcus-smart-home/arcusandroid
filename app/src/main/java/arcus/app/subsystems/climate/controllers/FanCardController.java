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
package arcus.app.subsystems.climate.controllers;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.device.DeviceController;
import arcus.cornea.device.fan.FanController;
import arcus.cornea.device.fan.FanProxyModel;
import arcus.cornea.error.ErrorModel;
import arcus.app.R;
import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.DeviceCardController;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.DeviceErrorType;
import arcus.app.common.view.GlowableImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class FanCardController extends DeviceCardController implements DeviceControlCard.OnClickListener, DeviceController.Callback<FanProxyModel> {

    private Logger logger = LoggerFactory.getLogger(FanCardController.class);
    @Nullable
    private FanController mController;
    private FanProxyModel mModel;


    public FanCardController(String deviceId, Context context) {
        super(deviceId, context);

        // Construct a Fan Card
        DeviceControlCard deviceCard = new DeviceControlCard(context);

        deviceCard.setLeftImageResource(R.drawable.button_on);
        deviceCard.setRightImageResource(R.drawable.button_off);

        deviceCard.setGlowMode(GlowableImageView.GlowMode.OFF);
        deviceCard.setShouldGlow(false);

        deviceCard.setDeviceId(deviceId);
        deviceCard.setCallback(this);

        setCurrentCard(deviceCard);
    }

    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);

        mController = FanController.newController(getDeviceId(), this);
    }

    @Override
    public void removeCallback() {
        super.removeCallback();

        mController.clearCallback();
        mController = null;

    }

    /*
     * Fan Callback
     */

    @Override
    public void show(@NonNull FanProxyModel model) {
        DeviceControlCard deviceCard = (DeviceControlCard) getCard();
        mModel = model;

        if (deviceCard != null) {
            deviceCard.setTitle(model.getName());

            deviceCard.setDeviceId(model.getDeviceId());

            // Handle Offline Mode
            deviceCard.setOffline(!model.isOnline());
            if (!model.isOnline()) {
                return;
            }

            if(!model.isOn()){
                deviceCard.setLeftButtonEnabled(true);
                deviceCard.setRightButtonEnabled(false);
                deviceCard.setDescription("Off");
                deviceCard.setLeftImageResource(R.drawable.button_on);
            }else{
                deviceCard.setLeftButtonEnabled(true);
                deviceCard.setRightButtonEnabled(true);
                deviceCard.setDescription("On " + model.getFanSpeed().getMode());

                switch (model.getFanSpeed()) {
                    case LOW:
                        deviceCard.setLeftImageResource(R.drawable.button_med);
                        break;
                    case MEDIUM:
                        deviceCard.setLeftImageResource(R.drawable.button_high);
                        break;
                    case HIGH:
                        deviceCard.setLeftImageResource(R.drawable.button_low);
                        break;
                    default:
                        deviceCard.setLeftImageResource(R.drawable.button_on);
                        break;
                }
            }
        }
    }

    @Override
    public void onError(ErrorModel error) {
        logger.error("Got error: {}", error);
        DeviceControlCard deviceCard = (DeviceControlCard) getCard();

        deviceCard.setLeftButtonEnabled(true);
        deviceCard.setRightButtonEnabled(true);
        ErrorManager.in(((Activity) getContext())).show(DeviceErrorType.UNABLE_TO_SAVE_CHANGES);
    }

    /*
     * DeviceCard Button Callbacks
     */


    @Override
    public void onLeftButtonClicked() {
        DeviceControlCard deviceCard = (DeviceControlCard) getCard();
        if (mController != null && mModel != null && deviceCard != null) {
            if (deviceCard.isLeftButtonEnabled() && !mModel.isOn()) {
                mController.turnFanOn();
            } else {
                mController.updateFanSpeed(mModel.getFanSpeed().next());
            }
        }
    }

    @Override
    public void onRightButtonClicked() {
        DeviceControlCard deviceCard = (DeviceControlCard) getCard();
        if (mController != null && mModel != null && deviceCard != null) {
            // Close
            if (deviceCard.isRightButtonEnabled()) {
                mController.turnFanOff();
            }
        }
    }

    @Override
    public void onTopButtonClicked() {
        navigateToDevice();
    }

    @Override
    public void onBottomButtonClicked() {

    }

    @Override
    public void onCardClicked() {
        navigateToDevice();
    }

}
