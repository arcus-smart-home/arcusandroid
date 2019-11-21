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
package arcus.app.device.pairing.catalog.controller;

import android.app.Activity;
import androidx.fragment.app.Fragment;

import arcus.cornea.subsystem.alarm.AlarmSubsystemController;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.device.pairing.catalog.ProductCatalogFragment;
import arcus.app.device.pairing.multi.controller.MultipairingSequenceController;
import arcus.app.device.pairing.steps.controller.DevicePairingStepsSequenceController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;


public class ProductCatalogSequenceController extends AbstractSequenceController  {

    private final Fragment endOfSequenceDestination;

    private static Logger logger = LoggerFactory.getLogger(ProductCatalogSequenceController.class);
    private static Map<String,Boolean> prePairingAlarmState;

    public ProductCatalogSequenceController() {
        // When sequence ends, pop backstack
        this.endOfSequenceDestination = null;
    }

    public ProductCatalogSequenceController(Fragment endOfSequenceDestination) {
        // When sequence ends, return to this fragment
        this.endOfSequenceDestination = endOfSequenceDestination;
    }

    @Override
    public void startSequence (Activity activity, Sequenceable from, Object... data) {
        logger.debug("Starting ProductCatalogSequenceController.");

        prePairingAlarmState = AlarmSubsystemController.getInstance().getAlarmActivations();
        boolean hideHubRequiredDevices = unpackArgument(0, Boolean.class, false, data);

        navigateForward(activity, ProductCatalogFragment.newInstance(hideHubRequiredDevices), data);
    }

    @Override
    public void endSequence (Activity activity, boolean isSuccess, Object... data) {
        logger.debug("Ending ProductCatalogSequenceController");
        ProductCatalogFragmentController.instance().stopPairing();

        if (endOfSequenceDestination == null) {
            BackstackManager.getInstance().navigateBack();
        } else {
            BackstackManager.getInstance().navigateBackToFragment(endOfSequenceDestination);
        }
    }

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {
        logger.debug("goNext() from ProductCatalogSequenceController; going to device pairing steps.");
        navigateForward(activity, new DevicePairingStepsSequenceController(), data);
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {
        logger.debug("goBack() from ProductCatalogSequenceController; invoking cancel on catalog controller");
        ProductCatalogFragmentController.instance().cancel();
    }

    public void goMultipairingSequence (Activity activity, ArrayList<String> deviceAddresses) {
        logger.debug("Entering multipairing sequence.");
        navigateForward(activity, new MultipairingSequenceController(), deviceAddresses);
    }

    public static Map<String, Boolean> getPrePairingAlarmActivations() {
        return prePairingAlarmState;
    }
}
