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
package arcus.app.device.pairing.steps.controller;

import android.app.Activity;

import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.device.pairing.multi.controller.MultipairingSequenceController;
import arcus.app.device.pairing.nohub.model.NoHubDevice;
import arcus.app.device.pairing.post.controller.PostPairingSequenceController;
import arcus.app.device.pairing.specialty.honeywelltcc.HoneywellSearching;
import arcus.app.device.pairing.specialty.honeywelltcc.HoneywellWebViewSequence;
import arcus.app.device.pairing.specialty.lutron.LutronCloudToCloudPairingStep;
import arcus.app.device.pairing.specialty.nest.NestCloudToCloudPairingStep;
import arcus.app.device.pairing.steps.AbstractPairingStepFragment;
import arcus.app.device.pairing.steps.DeviceSearchFragment;
import arcus.app.device.pairing.steps.PairingStepFragment;
import arcus.app.device.pairing.steps.model.PairingStepTransition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;


public class DevicePairingStepsSequenceController extends AbstractSequenceController {

    private final static Logger logger = LoggerFactory.getLogger(DevicePairingStepsSequenceController.class);
    private Sequenceable previousSequence;
    private String productAddress;
    private String productDevTypeHint;

    @Override
    public void goNext(final Activity activity, Sequenceable from, Object... data) {

        if (from instanceof PairingStepFragment) {
            logger.debug("Got goNext() from {}; transitioning to next pairing step", from);

            PairingStepTransition transition = PairingStepFragmentController.instance().showNextPairingStep();

            // No next pairing step, device is a no-pair device (i.e., Alexa); show instructional screens
            if (transition == PairingStepTransition.GO_NO_PAIRING) {
                String productId = PairingStepFragmentController.instance().getProductId();
                NoHubDevice noPairDevice = NoHubDevice.fromProductId(productId);

                navigateForward(activity, noPairDevice.getSequence(), previousSequence);
            }

            // No next pairing step... show searching for device fragment
            else if (transition == PairingStepTransition.GO_END) {
                String deviceName = PairingStepFragmentController.instance().getDeviceName();
                String productId = PairingStepFragmentController.instance().getProductId();

                AbstractPairingStepFragment cloudToCloudSequnce = CloudToCloudPairingStepFactory.getCloudToCloudPairingStep(productId);

                if (cloudToCloudSequnce != null) {
                    navigateForward(activity, cloudToCloudSequnce, null);
                }
                else {
                    HashMap<String,Object> ipcdAttributes = unpackArgument(0, HashMap.class, null, data);
                    String requiredFieldName = unpackArgument(1, String.class, null, data);
                    logger.debug("Transitioning to DeviceSearchFragment with required field name: {}", requiredFieldName);
                    navigateForward(activity, DeviceSearchFragment.newInstance(deviceName, ipcdAttributes, requiredFieldName), data);
                }
            }
        }
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {

        if (from instanceof PairingStepFragment) {
            logger.debug("Got goBack() from {}; transitioning to previous pairing step.", from);

            PairingStepTransition transition = PairingStepFragmentController.instance().showPreviousPairingStep();

            // No previous pairing step... end sequence
            if (transition == PairingStepTransition.GO_START) {
                endSequence(activity, false, data);
            }
        }

        else if ((from instanceof DeviceSearchFragment) || (from instanceof HoneywellWebViewSequence)
                || (from instanceof NestCloudToCloudPairingStep) || (from instanceof LutronCloudToCloudPairingStep)) {

            logger.debug("Got goBack() from {}; returning to pairing steps.", from);
            navigateForward(activity, PairingStepFragment.newInstance(productAddress, true), data);
        }
        else if (from instanceof HoneywellSearching) {
            navigateBack(activity, HoneywellWebViewSequence.newInstance(), true);
        }
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        if (previousSequence != null) {
            logger.debug("Got endSequence(); returning to previous sequenceable: {}", previousSequence);
            navigateBack(activity, previousSequence);
        } else {
            logger.debug("Got endSequence(); navigating backwards in backstack.");
            BackstackManager.getInstance().navigateBack();
        }
    }

    public void reauthorizeNestDevice(Activity activity, String abortToDeviceAddress)  {
        NestCloudToCloudPairingStep cloudToCloudSequnce = NestCloudToCloudPairingStep.newInstance(abortToDeviceAddress);
        navigateForward(activity, cloudToCloudSequnce, null);
    }

    public void startSequenceLutronDevice(Activity activity, String deviceAddress, String supportUrl)  {
        LutronCloudToCloudPairingStep cloudToCloudSequnce =
                LutronCloudToCloudPairingStep.newInstance(deviceAddress, supportUrl);
        navigateForward(activity, cloudToCloudSequnce, null);
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        logger.debug("Starting device pairing steps sequence from {}.", from);

        this.previousSequence = from;
        productAddress = unpackArgument(0, String.class, data);
        productDevTypeHint = unpackArgument(1, String.class, data);

        navigateForward(activity, PairingStepFragment.newInstance(productAddress, false), data);
    }

    public void resumeFromEndOfPairingSteps (Activity activity) {
        navigateForward(activity, PairingStepFragment.newInstance(productAddress, true));
    }

    public void goMultipairingSequence (Activity activity, ArrayList<String> pairedDeviceAddresses) {
        navigateForward(activity, new MultipairingSequenceController(), pairedDeviceAddresses);
    }

    public void goSinglePairingSequence (Activity activity, String pairedDeviceName, String pairedDeviceAddress) {
        navigateForward(activity, new PostPairingSequenceController(), pairedDeviceName, pairedDeviceAddress);
    }

    public void goTo(Activity activity, Sequenceable fragment) {
        navigateForward(activity, fragment);
    }

    public String getProductDevTypeHint () {
        return this.productDevTypeHint;
    }
}
