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

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import arcus.cornea.device.DeviceController;
import arcus.cornea.device.watersoftener.WaterSoftenerController;
import arcus.cornea.device.watersoftener.WaterSoftenerProxyModel;
import arcus.cornea.error.ErrorModel;
import arcus.app.R;
import arcus.app.common.cards.DeviceControlCard;
import arcus.app.common.cards.SimpleDividerCard;
import arcus.app.common.controller.DeviceCardController;
import arcus.app.common.view.GlowableImageView;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class WaterSoftenerCardController extends DeviceCardController implements DeviceControlCard.OnClickListener, DeviceController.Callback<WaterSoftenerProxyModel> {

    private Logger logger = LoggerFactory.getLogger(WaterSoftenerCardController.class);
    @Nullable
    private WaterSoftenerController mController;
    private WaterSoftenerProxyModel mModel;


    public WaterSoftenerCardController(String deviceId, Context context) {
        super(deviceId, context);

        DeviceControlCard deviceCard = new DeviceControlCard(context);

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

        mController = WaterSoftenerController.newController(getDeviceId(), this);
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
    public void show(@NonNull WaterSoftenerProxyModel model) {
        DeviceControlCard deviceCard = (DeviceControlCard) getCard();
        mModel = model;

        if (deviceCard != null) {
            deviceCard.setTitle(model.getName());
            deviceCard.setDeviceId(model.getDeviceId());
            deviceCard.setOffline(!model.isOnline());
            deviceCard.setDescription(getContext().getString(R.string.water_softener_salt_level_with_percent, model.getSaltLevel()));
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
