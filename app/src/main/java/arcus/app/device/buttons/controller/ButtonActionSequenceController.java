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
package arcus.app.device.buttons.controller;

import android.app.Activity;
import androidx.annotation.NonNull;

import arcus.cornea.CorneaClientFactory;
import com.iris.client.model.DeviceModel;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.device.buttons.model.Button;
import arcus.app.device.buttons.model.ButtonAction;
import arcus.app.device.buttons.model.ButtonSequenceVariant;
import arcus.app.device.settings.fragment.ButtonActionFragment;
import arcus.app.device.settings.fragment.ButtonSelectionFragment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ButtonActionSequenceController extends AbstractSequenceController implements ButtonActionController.Callback {

    private final static Logger logger = LoggerFactory.getLogger(ButtonActionSequenceController.class);

    private Sequenceable lastSequence;
    private ButtonActionController editor;

    private final Activity activity;
    private final String buttonDeviceAddress;
    private ButtonSequenceVariant variant;

    public ButtonActionSequenceController(Activity activity, ButtonSequenceVariant variant, String buttonDeviceAddress) {
        this.activity = activity;
        this.buttonDeviceAddress = buttonDeviceAddress;
        this.variant = variant;
    }

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {
        endSequence(activity, true, data);
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {
        BackstackManager.getInstance().navigateBack();
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        logger.debug("Ending ButtonActionSequenceController; last sequence was {}.", lastSequence);
        if (lastSequence == null) {
            BackstackManager.getInstance().navigateBack();
        } else {
            lastSequence.goNext(activity, this, data);
        }
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        logger.debug("Starting ButtonActionSequenceController from {}.", from);

        this.lastSequence = from;

        DeviceModel model = (DeviceModel) CorneaClientFactory.getModelCache().get(buttonDeviceAddress);
        this.editor = new ButtonActionController(this.activity, this);
        this.editor.editButtonDevice(model);
    }

    public void setSelectedButton(@NonNull Button button) {
        editor.editButton(button);
    }

    public void setSelectedAction(@NonNull ButtonAction selectedAction) {
        // Disallow another action to be assigned while a current save is pending; otherwise we
        // can wind up with multiple rules
        //if (!isInProgress()) {
            editor.assignButtonAction(selectedAction);
        /*} else {
            logger.warn("Not persisting action {} because a current action assignment is pending.", selectedAction);
        }*/
    }

    @Override
    public void onShowButtonSelector(Button[] buttons) {
        setInProgress(false);
        navigateForward(activity, ButtonSelectionFragment.newInstance(variant, buttons, buttonDeviceAddress));
    }

    @Override
    public void onShowButtonRuleEditor(ButtonAction[] actions, ButtonAction currentSelection) {
        setInProgress(false);
        navigateForward(activity, ButtonActionFragment.newInstance(variant, actions, currentSelection));
    }

    @Override
    public void onLoading() {
        setInProgress(true);
    }

    @Override
    public void onError(Throwable reason) {
        setInProgress(false);
        ErrorManager.in(activity).showGenericBecauseOf(reason);
    }

    @Override
    public void onComplete() {
        setInProgress(false);
        if (ButtonSequenceVariant.DEVICE_PAIRING.equals(variant)) {
            goNext(activity, this);
        }
        else {
            BackstackManager.getInstance().navigateBack();
        }
    }

    private boolean isInProgress() {
        if (getActiveFragment() != null && getActiveFragment() instanceof SequencedFragment) {
            return getActiveFragment().isProgressBarVisible();
        }

        return true;
    }

    private void setInProgress(boolean visible) {
        if (getActiveFragment() != null && getActiveFragment() instanceof SequencedFragment) {
            if (visible) {
                getActiveFragment().showProgressBar();
            } else {
                getActiveFragment().hideProgressBar();
            }
        }
    }
}
