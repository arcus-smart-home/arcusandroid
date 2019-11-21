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
import arcus.cornea.device.contactsensor.ContactSensorController;
import arcus.cornea.device.contactsensor.ContactSensorProxyModel;
import arcus.cornea.error.ErrorModel;
import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.DeviceCardController;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.GlowableImageView;

import java.text.SimpleDateFormat;
import java.util.Locale;


public class ContactSensorCardController extends DeviceCardController implements DeviceControlCard.OnClickListener, DeviceController.Callback<ContactSensorProxyModel> {

    @Nullable
    private ContactSensorController mController;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm aaa", Locale.US);

    public ContactSensorCardController(String deviceId, Context context) {
        super(deviceId, context);

        // Construct a contact sensor Card
        DeviceControlCard deviceCard = new DeviceControlCard(context);
        deviceCard.setGlowMode(GlowableImageView.GlowMode.OPEN_CLOSE);
        deviceCard.setCallback(this);

        setCurrentCard(deviceCard);
    }

    @Override
    public void setCallback(Callback delegate) {
        super.setCallback(delegate);

        mController = ContactSensorController.newController(getDeviceId(), this);
    }

    @Override
    public void removeCallback() {
        super.removeCallback();

        mController.clearCallback();
        mController = null;
    }

    @Override
    public SimpleDividerCard getCard() {
        return getCurrentCard();
    }

    @Override
    public void show(@NonNull ContactSensorProxyModel model) {
        DeviceControlCard deviceCard = (DeviceControlCard) getCard();
        deviceCard.setRightButtonVisible(false);
        deviceCard.setLeftButtonVisible(false);

        if(deviceCard !=null){
            deviceCard.setTitle(model.getName());
            deviceCard.setDescription(model.getState().label() + '\u0020'
                    + StringUtils.getTimestampString(model.getLastStateChange()));

            deviceCard.setDeviceId(model.getDeviceId());
            deviceCard.setOffline(!model.isOnline());

            if (model.isOnline()) {
                deviceCard.setShouldGlow(model.getState().equals(ContactSensorProxyModel.State.OPENED));
            }
        }
    }

    @Override
    public void onError(ErrorModel error) {

    }

    @Override
    public void onLeftButtonClicked() {

    }

    @Override
    public void onRightButtonClicked() {

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
