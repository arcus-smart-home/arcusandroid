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

import arcus.cornea.device.climate.SpaceHeaterControllerDetailsModel;
import arcus.cornea.device.climate.SpaceHeaterDeviceController;
import arcus.cornea.utils.Listeners;
import com.iris.capability.util.Addresses;
import com.iris.client.capability.SpaceHeater;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.DeviceCardController;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.DeviceErrorType;
import arcus.app.common.popups.MultiButtonPopup;
import arcus.app.subsystems.climate.cards.SpaceHeaterDeviceControlCard;

import java.util.ArrayList;



public class SpaceHeaterCardController extends DeviceCardController implements SpaceHeaterDeviceControlCard.OnClickListener,
        SpaceHeaterDeviceController.Callback, MultiButtonPopup.OnButtonClickedListener {
    private ListenerRegistration mListener;
    private SpaceHeaterDeviceController mController;
    private SpaceHeaterControllerDetailsModel mModel;

    public SpaceHeaterCardController(String deviceId, Context context) {
        super(deviceId, context);
        SpaceHeaterDeviceControlCard deviceCard = new SpaceHeaterDeviceControlCard(context);

        deviceCard.setLeftImageResource(R.drawable.button_minus);
        deviceCard.setRightImageResource(R.drawable.button_plus);
        deviceCard.setRightButtonEnabled(false);
        deviceCard.setLeftButtonEnabled(false);

        /*deviceCard.setGlowMode(GlowableImageView.GlowMode.OFF);
        deviceCard.setShouldGlow(false);*/

        deviceCard.setDeviceId(deviceId);
        deviceCard.setCallback(this);
        deviceCard.setBottomImageResource(R.drawable.outline_rounded_button_style);

        setCurrentCard(deviceCard);
    }

    @Override public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override public void setCallback(Callback delegate) {
        super.setCallback(delegate);
        mController = SpaceHeaterDeviceController.newController(getDeviceId());
        mListener = mController.setCallback(this);
    }

    @Override
    public void removeCallback() {
        super.removeCallback();
        Listeners.clear(mListener);
        mController = null;
    }

    @Override
    public void onLeftButtonClicked() {
        SpaceHeaterDeviceControlCard deviceCard = (SpaceHeaterDeviceControlCard) getCard();
        if(mModel == null || mController == null || deviceCard == null || !deviceCard.isLeftButtonEnabled()) {
            return;
        }
        disableAllButtons();
        mController.leftButtonEvent();
    }

    @Override public void onRightButtonClicked() {
        if (mModel == null || mController == null) {
            return;
        }
        disableAllButtons();
        mController.rightButtonEvent();
    }

    @Override public void onTopButtonClicked() {
        navigateToDevice();
    }

    @Override public void onBottomButtonClicked() {
        ArrayList<String> buttons = new ArrayList<>();
        buttons.add(SpaceHeater.HEATSTATE_OFF);
        buttons.add(SpaceHeater.HEATSTATE_ON);
        MultiButtonPopup popup = MultiButtonPopup.newInstance(getContext().getString(R.string.power), buttons);
        popup.setOnButtonClickedListener(this);
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }

    @Override public void onCardClicked() {
        navigateToDevice();
    }

    // Device Controller - Show device state.
    @Override public void showDeviceControls(SpaceHeaterControllerDetailsModel model) {
        SpaceHeaterDeviceControlCard deviceCard = (SpaceHeaterDeviceControlCard) getCard();
        mModel = model;

        if (deviceCard != null) {
            deviceCard.setTitle(model.getDeviceName());
            deviceCard.setDeviceId(Addresses.getId(model.getDeviceAddress()));

            deviceCard.setLeftImageResource(R.drawable.button_minus);
            deviceCard.setRightImageResource(R.drawable.button_plus);

            // Handle Offline Mode
            deviceCard.setOffline(!model.isOnline());
            deviceCard.setIsInOta(model.isInOTA());
            if (deviceCard.isOffline() || deviceCard.isInOta()) {
                deviceCard.setLeftImageResource(0);
                deviceCard.setRightImageResource(0);
                deviceCard.setBottomButtonEnabled(false);
                return;
            }

            deviceCard.setBottomButtonEnabled(model.isBottomButtonEnabled());
            deviceCard.setLeftButtonEnabled(model.isLeftButtonEnabled());
            deviceCard.setRightButtonEnabled(model.isRightButtonEnabled());

            deviceCard.setCurrentTemp(model.getCurrentTemp());

            if(model.isDeviceEcoOn() || !model.isDeviceModeOn()) {
                deviceCard.setLeftImageResource(0);
                deviceCard.setRightImageResource(0);
                deviceCard.setLeftButtonEnabled(false);
                deviceCard.setRightButtonEnabled(false);
            }

            deviceCard.setSetPoint(model.getSetPoint());
            deviceCard.setDeviceEcoOn(model.isDeviceEcoOn());
            deviceCard.setDeviceModeOn(model.isDeviceModeOn());
            /*String description  = "-";
            if(model.isDeviceModeOn() && !model.isDeviceEcoOn()) {
                description = String.format(getContext().getString(R.string.spaceheater_on_eco_off), model.getCurrentTemp(), model.getSetPoint());
            }
            else if(model.isDeviceModeOn() && model.isDeviceEcoOn()) {
                description = String.format(getContext().getString(R.string.spaceheater_on_eco_on), model.getCurrentTemp());
            }
            else if(!model.isDeviceModeOn() && model.isDeviceEcoOn()) {
                description = getContext().getString(R.string.spaceheater_off_eco_on);
            }*/

            deviceCard.setBottomImageText(model.isDeviceModeOn() ? getContext().getString(R.string.on) : getContext().getString(R.string.OFF));
            //deviceCard.setDescription(description);
        }
    }


    // DeviceController - Error happened.
    @Override public void errorOccurred(Throwable error) {
        if (mModel != null) {
            showDeviceControls(mModel);
        }

        ErrorManager.in(((Activity) getContext())).show(DeviceErrorType.UNABLE_TO_SAVE_CHANGES);
    }

    protected void disableAllButtons() {
        SpaceHeaterDeviceControlCard deviceCard = (SpaceHeaterDeviceControlCard) getCard();
        if (deviceCard == null) {
            return;
        }

        /*deviceCard.setLeftButtonEnabled(false);
        deviceCard.setRightButtonEnabled(false);
        deviceCard.setBottomButtonEnabled(false);*/
    }

    @Override
    public void onButtonClicked(String buttonValue) {
        if (mController != null){
            disableAllButtons();
            mController.updateSpaceHeaterMode(buttonValue);
        }
    }
}
