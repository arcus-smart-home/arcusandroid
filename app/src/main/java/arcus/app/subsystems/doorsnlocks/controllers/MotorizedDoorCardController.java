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
package arcus.app.subsystems.doorsnlocks.controllers;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.device.DeviceController;
import arcus.cornea.device.motorizeddoor.MotorizedDoorController;
import arcus.cornea.device.motorizeddoor.MotorizedDoorProxyModel;
import arcus.cornea.error.ErrorModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.DeviceCardController;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.GlowableImageView;
import arcus.app.device.model.DeviceType;

import java.util.Map;


public class MotorizedDoorCardController extends DeviceCardController implements DeviceControlCard.OnClickListener, DeviceController.Callback<MotorizedDoorProxyModel> {

    @Nullable
    private MotorizedDoorController mController;

    private MotorizedDoorProxyModel mModel;

    public MotorizedDoorCardController(String deviceId, Context context) {
        super(deviceId, context);

        // Construct a motorized door Card
        DeviceControlCard deviceCard = new DeviceControlCard(context);

        deviceCard.setLeftImageResource(R.drawable.button_open);
        deviceCard.setRightImageResource(R.drawable.button_close);
        deviceCard.setGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);

        deviceCard.setCallback(this);
        deviceCard.setDeviceType(DeviceType.GARAGE_DOOR);
        setCurrentCard(deviceCard);
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);

        mController = MotorizedDoorController.newController(getDeviceId(), this);
    }

    @Override
    public void removeCallback() {
        super.removeCallback();

        if (mController != null) {
            mController.clearCallback();
            mController = null;
        }
    }

    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override
    public void show(@NonNull MotorizedDoorProxyModel model) {
        DeviceControlCard deviceCard = (DeviceControlCard) getCard();

        mModel = model;

        if (deviceCard != null) {
            deviceCard.setTitle(model.getName());
            deviceCard.setDescription(model.getState().label() + '\u0020'
                    + StringUtils.getTimestampString(model.getLastStateChange()));
            deviceCard.setDeviceId(model.getDeviceId());

            deviceCard.clearErrors();

            // Handle Offline Mode
            deviceCard.setOffline(!model.isOnline());
            if (!model.isOnline()) {
                return;
            }

            deviceCard.clearErrors();
            for(Map.Entry<String, String> entry : model.getErrors().entrySet()) {
                if("ERR_OBSTRUCTION".equals(entry.getKey())) {
                    deviceCard.addError(ArcusApplication.getContext().getString(R.string.obstruction_detected));
                }
            }

            //deviceCard.setDescription(model.getState().label());
            switch (model.getState()) {
                case OPEN:
                    deviceCard.setLeftButtonEnabled(false);
                    deviceCard.setRightButtonEnabled(true);
                    deviceCard.setShouldGlow(true);
                    break;
                case CLOSING:
                case OPENING:
                    deviceCard.setLeftButtonEnabled(false);
                    deviceCard.setRightButtonEnabled(false);
                    deviceCard.setShouldGlow(false);
                    break;
                case CLOSED:
                    deviceCard.setLeftButtonEnabled(true);
                    deviceCard.setRightButtonEnabled(false);
                    deviceCard.setShouldGlow(false);
                    break;
            }
        }
    }

    @Override
    public void onError(ErrorModel error) {

    }

    @Override
    public void onLeftButtonClicked() {
        DeviceControlCard deviceCard = (DeviceControlCard) getCard();

        if (deviceCard == null || mController == null) {
            return;
        }

        // Open
        if (deviceCard.isLeftButtonEnabled() && mModel != null) {
            mController.openDoor();
        }
    }

    @Override
    public void onRightButtonClicked() {
        DeviceControlCard deviceCard = (DeviceControlCard) getCard();

        if (deviceCard == null || mController == null) {
            return;
        }

        // Close
        if (deviceCard.isRightButtonEnabled() && mModel != null) {
            mController.closeDoor();
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
