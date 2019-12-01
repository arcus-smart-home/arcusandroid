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
package arcus.app.device.settings.fragment.presenter;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.common.BasePresenter;
import com.iris.client.model.DeviceModel;
import arcus.app.device.buttons.controller.ButtonActionController;
import arcus.app.device.buttons.model.Button;
import arcus.app.device.buttons.model.ButtonAction;
import arcus.app.device.buttons.model.ButtonDevice;
import arcus.app.device.buttons.model.FobButton;
import arcus.app.device.settings.fragment.contract.ButtonSelectionContract;
import arcus.cornea.provider.RuleModelProvider;
import arcus.cornea.utils.Listeners;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ButtonSelectionPresenter extends BasePresenter implements ButtonSelectionContract.ButtonSelectionPresenter {

    private static final Logger logger = LoggerFactory.getLogger(ButtonSelectionPresenter.class);

    private ButtonDevice selectedButtonDevice;
    private String selectedDeviceAddress;
    private ButtonActionController buttonActionController = new ButtonActionController();

    public void getButtonsActions(@NonNull final String deviceAddress, Button[] displayButtons) {

        DeviceModel device = (DeviceModel) CorneaClientFactory.getModelCache().get(deviceAddress);

        if (!ButtonDevice.isButtonDevice(device.getProductId())) {
            throw new IllegalArgumentException("Device is not a ButtonDevice that can be edited with this controller.");
        }

        selectedButtonDevice = ButtonDevice.fromProductId(device.getProductId());
        selectedDeviceAddress = device.getAddress();

        logger.debug("Editing button actions for device {}.", selectedButtonDevice);

        FobButton[] buttons = (FobButton[]) displayButtons;
        getButtonAction(buttons);
    }

    private void getButtonAction(@NonNull final FobButton[] buttons) {
        logger.debug("Editing button {} of device {}.", buttons, selectedButtonDevice);

        RuleModelProvider
                .instance()
                .getRules()
                .onSuccess(Listeners.runOnUiThread(rules ->{
                    logger.debug("Loaded rules; got {} rule instances.", rules.size());

                    ButtonAction[] assignableActions = buttonActionController.getAssignableActionsForDevice(selectedButtonDevice);

                    for (FobButton thisButton : buttons) {
                        String currentAction = buttonActionController.getCurrentButtonAction(rules, selectedDeviceAddress, assignableActions, thisButton).toString();
                        thisButton.setButtonAction(currentAction);
                    }

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            getPresentedView().updateView(buttons);
                        }
                    });
                }))
                .onFailure(Listeners.runOnUiThread(throwable -> {
                    logger.debug("Failed to load rules due to: {}", throwable.getMessage());
                }));
    }
}
