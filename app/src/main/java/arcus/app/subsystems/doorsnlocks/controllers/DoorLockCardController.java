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

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.device.DeviceController;
import arcus.cornea.device.doorlock.DoorLockController;
import arcus.cornea.device.doorlock.DoorLockProxyModel;
import arcus.cornea.error.ErrorModel;
import com.iris.client.capability.DoorLock;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.DeviceCardController;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.DeviceErrorType;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.GlowableImageView;
import arcus.app.device.model.DeviceType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


public class DoorLockCardController extends DeviceCardController implements DeviceControlCard.OnClickListener, DeviceController.Callback<DoorLockProxyModel> {

    public Logger logger = LoggerFactory.getLogger(DoorLockCardController.class);
    @Nullable
    private DoorLockController mController;
    private DeviceControlCard deviceCard;
    private DoorLockProxyModel doorLock;
    private boolean isBuzzingIn = false;
    private boolean setChange = false;

    public DoorLockCardController(String deviceId, Context context) {
        super(deviceId, context);

        // Construct a door lock Card
        deviceCard = new DeviceControlCard(context);
        deviceCard.setLeftImageResource(R.drawable.button_lock);

        deviceCard.setGlowMode(GlowableImageView.GlowMode.OFF);
        deviceCard.setShouldGlow(false);

        deviceCard.setDeviceId(deviceId);
        deviceCard.setCallback(this);
        deviceCard.setDeviceType(DeviceType.LOCK);
        setCurrentCard(deviceCard);
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);

        mController = DoorLockController.newController(getDeviceId(), this);
    }

    @Override
    public void removeCallback() {
        super.removeCallback();

        if (mController != null) {
            mController.clearCallback();
        }
        mController = null;
    }

    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override
    public void show(@NonNull DoorLockProxyModel model) {
        DeviceControlCard deviceCard = (DeviceControlCard) getCard();

        doorLock = model;


        if (deviceCard != null) {
            deviceCard.setTitle(model.getName());

            deviceCard.clearErrors();
            for(Map.Entry<String, String> entry : model.getErrors().entrySet()) {
                if("WARN_JAM".equals(entry.getKey())) {
                    deviceCard.addError(ArcusApplication.getContext().getString(R.string.door_lock_jam));
                }
            }

            if(doorLock.isSupportsBuzzIn()){
                deviceCard.setRightImageResource(R.drawable.button_buzzin);
            }else{
                deviceCard.setRightImageResource(0);
            }

            if (setChange) {
                setChange = false;
                return;
            }

            // Handle Offline Mode
            deviceCard.setOffline(!model.isOnline());
            if (!model.isOnline()) {
                return;
            }

            deviceCard.setDescription(model.getState().label() + '\u0020'
                    + StringUtils.getTimestampString(model.getLastStateChange()));



            switch (model.getState()){
                case UNLOCKED:
                    deviceCard.setLeftButtonEnabled(!isBuzzingIn);
                    deviceCard.setRightButtonEnabled(false);
                    if (!isBuzzingIn) updateLockButtonLockIcon();
                    deviceCard.setGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);
                    deviceCard.setShouldGlow(getDoorLockStatus().equals(DoorLock.LOCKSTATE_UNLOCKED));
                    break;
                case LOCKED:
                    isBuzzingIn = false;
                    deviceCard.setLeftButtonEnabled(true);
                    deviceCard.setRightButtonEnabled(true);
                    updateLockButtonLockIcon();
                    deviceCard.setGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);
                    deviceCard.setShouldGlow(getDoorLockStatus().equals(DoorLock.LOCKSTATE_UNLOCKED));
                    break;
                case UNLOCKING:
                    deviceCard.setLeftButtonEnabled(false);
                    deviceCard.setRightButtonEnabled(false);
                    break;
                case LOCKING:
                    deviceCard.setLeftButtonEnabled(false);
                    deviceCard.setRightButtonEnabled(false);
                    break;
            }

        }
    }



    @Override
    public void onError(ErrorModel error) {
        logger.error("Got error: {}", error);
        isBuzzingIn = false;
        deviceCard.setLeftButtonEnabled(true);
        deviceCard.setRightButtonEnabled(true);
        ErrorManager.in(((Activity) getContext())).show(DeviceErrorType.UNABLE_TO_SAVE_CHANGES);
    }

    @Override
    public void onLeftButtonClicked() {
        setChange = true;
        lockOrUnlockDoor();
    }

    @Override
    public void onRightButtonClicked() {
        buzzIn();

    }

    @Override
    public void onTopButtonClicked() {
        if (doorLock == null) return;

        navigateToDevice();
    }

    @Override
    public void onBottomButtonClicked() {
        if (doorLock == null) return;

    }

    @Override
    public void onCardClicked() {
        navigateToDevice();
    }

    private void lockOrUnlockDoor() {
        if (doorLock == null) return;

        deviceCard.setLeftButtonEnabled(false);
        deviceCard.setRightButtonEnabled(false);
        if (doorLock.getState().toString().equals(DoorLock.LOCKSTATE_LOCKED)) {
            mController.unlock();
        } else if (doorLock.getState().toString().equals(DoorLock.LOCKSTATE_UNLOCKED)) {
            mController.lock();
        }


    }

    private void buzzIn() {
        if (doorLock == null) return;
        isBuzzingIn = true;
        deviceCard.setLeftButtonEnabled(false);
        deviceCard.setRightButtonEnabled(false);
        mController.buzzIn();
    }

    // If the door is locked button should say Unlock, if it's unlocked should say Lock.
    private void updateLockButtonLockIcon() {
        int drawable;

        switch (getDoorLockStatus()) {
            case DoorLock.LOCKSTATE_LOCKED:
                drawable = R.drawable.button_unlock;
                break;
            case DoorLock.LOCKSTATE_UNLOCKED:
                drawable = R.drawable.button_lock;
                break;
            case DoorLock.LOCKSTATE_UNLOCKING:
                drawable = R.drawable.button_lock;
                break;
            case DoorLock.LOCKSTATE_LOCKING:
                drawable = R.drawable.button_unlock;
                break;
            default:
                drawable = R.drawable.button_unlock;
        }
        deviceCard.setLeftImageResource(drawable);
    }

    private String getDoorLockStatus() {
        if (doorLock != null && doorLock.getState() != null) {
            return doorLock.getState().toString();
        }

        return DoorLock.LOCKSTATE_LOCKED;
    }
}
