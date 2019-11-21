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
package arcus.app.subsystems.water.controllers;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.device.DeviceController;
import arcus.cornea.device.valve.ValveController;
import arcus.cornea.device.valve.ValveProxyModel;
import arcus.cornea.error.ErrorModel;
import com.iris.client.capability.Valve;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.DeviceCardController;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.DeviceErrorType;
import arcus.app.common.view.GlowableImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ValveCardController extends DeviceCardController implements DeviceControlCard.OnClickListener, DeviceController.Callback<ValveProxyModel> {

    private Logger logger = LoggerFactory.getLogger(ValveCardController.class);
    @Nullable
    private ValveController mController;
    private ValveProxyModel mModel;


    public ValveCardController(String deviceId, Context context) {
        super(deviceId, context);

        DeviceControlCard deviceCard = new DeviceControlCard(context);

        deviceCard.setLeftImageResource(R.drawable.button_open);
        deviceCard.setRightImageResource(R.drawable.button_close);
        deviceCard.setGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);

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

        mController = ValveController.newController(getDeviceId(), this);
    }

    @Override
    public void removeCallback() {
        super.removeCallback();

        mController.clearCallback();
        mController = null;

    }

    /*
     * Valve Callback
     */

    @Override
    public void show(@NonNull ValveProxyModel model) {
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

            deviceCard.clearErrors();
            if(Valve.VALVESTATE_OBSTRUCTION.equals(model.getValveState())) {
                deviceCard.addError(ArcusApplication.getContext().getString(R.string.valve_obstruction));
            }

            if(model.getValveState().equals(Valve.VALVESTATE_OPEN)){
                deviceCard.setLeftButtonEnabled(false);
                deviceCard.setRightButtonEnabled(true);

                deviceCard.setShouldGlow(true);


            } else if(model.getValveState().equals(Valve.VALVESTATE_CLOSED)){
                deviceCard.setLeftButtonEnabled(true);
                deviceCard.setRightButtonEnabled(false);

                deviceCard.setShouldGlow(false);
            } else {
                deviceCard.setLeftButtonEnabled(false);
                deviceCard.setRightButtonEnabled(false);

                deviceCard.setShouldGlow(false);
            }
        }
    }

    @Override
    public void onError(ErrorModel error) {
        logger.error("Got error: {}", error);
        DeviceControlCard deviceCard = (DeviceControlCard) getCard();

        deviceCard.setLeftButtonEnabled(false);
        deviceCard.setRightButtonEnabled(false);
        ErrorManager.in(((Activity) getContext())).show(DeviceErrorType.UNABLE_TO_SAVE_CHANGES);
    }

    /*
     * DeviceCard Button Callbacks
     */


    @Override
    public void onLeftButtonClicked() {
        DeviceControlCard deviceCard = (DeviceControlCard) getCard();
        if (mModel != null && deviceCard.isLeftButtonEnabled() && !mModel.getValveState().equals(Valve.VALVESTATE_OPEN)) {
            mController.setValveState(true);
            deviceCard.setLeftButtonEnabled(false);
            deviceCard.setRightButtonEnabled(false);
        }
    }

    @Override
    public void onRightButtonClicked() {
        DeviceControlCard deviceCard = (DeviceControlCard) getCard();
        if (mModel != null && deviceCard.isRightButtonEnabled() && !mModel.getValveState().equals(Valve.VALVESTATE_CLOSED)) {
            mController.setValveState(false);
            deviceCard.setLeftButtonEnabled(false);
            deviceCard.setRightButtonEnabled(false);
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
