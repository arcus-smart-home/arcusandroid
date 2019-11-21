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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.device.DeviceController;
import arcus.cornea.device.vent.VentController;
import arcus.cornea.device.vent.VentModel;
import arcus.cornea.error.ErrorModel;
import arcus.app.R;

import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.cards.VentControlCard;
import arcus.app.common.controller.DeviceCardController;
import arcus.app.common.view.GlowableImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class VentCardController extends DeviceCardController implements VentControlCard.OnClickListener, DeviceController.Callback<VentModel> {
    private final static int MIN_PERCENTAGE = 0;
    private final static int MIN_SETTABLE_PERCENTAGE = 10;
    private final static int MAX_PERCENTAGE = 100;
    private final static int STEP_PERCENTAGE = 10;
    private Logger logger = LoggerFactory.getLogger(VentCardController.class);
    @Nullable
    private VentController mController;
    private VentModel mModel;

    public VentCardController(String deviceId, Context context) {
        super(deviceId, context);


        DeviceControlCard deviceCard = new VentControlCard(context);


        deviceCard.setLeftImageResource(R.drawable.button_minus);
        deviceCard.setRightImageResource(R.drawable.button_plus);

        deviceCard.setGlowMode(GlowableImageView.GlowMode.OFF);
        deviceCard.setBevelVisible(false);

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

        mController = VentController.newController(getDeviceId(), this);
    }

    @Override
    public void removeCallback() {
        super.removeCallback();

        mController.clearCallback();
        mController = null;
    }

    @Override
    public void show(@NonNull VentModel model) {
        VentControlCard deviceCard = (VentControlCard) getCard();
        mModel = model;

        if (deviceCard != null) {
            deviceCard.setTitle(model.getName());

            deviceCard.setDeviceId(model.getDeviceId());


            deviceCard.setOffline(!model.isOnline());
            if (!model.isOnline()) {
                return;
            }

            deviceCard.setShouldGlow(!model.isClosed());

            if(model.isClosed()){
                deviceCard.setDescription(getContext().getString(R.string.vent_closed));
            }else{
                deviceCard.setDescription(model.getOpenPercent() + "%");
            }
        }
    }

    @Override
    public void onError(ErrorModel error) {

    }




    @Override
    public void onLeftButtonClicked() {

        mController.closeVent();

    }

    @Override
    public void onRightButtonClicked() {

        mController.openVent();
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
