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

import arcus.app.device.pairing.specialty.honeywelltcc.HoneywellWebViewSequence;
import arcus.app.device.pairing.specialty.lutron.LutronCloudToCloudPairingStep;
import arcus.app.device.pairing.specialty.nest.NestCloudToCloudPairingStep;
import arcus.app.device.pairing.steps.AbstractPairingStepFragment;

import java.util.Arrays;
import java.util.List;



public class CloudToCloudPairingStepFactory {

    private static final List<String> nestThermostatProductIds = Arrays.asList("f9a5b0");
    private static final List<String> honeywellThermostatProductIds = Arrays.asList("973d58", "1dbb3f", "d9685c");
    public static final List<String> lutronProductIds = Arrays.asList("d8ceb2", "7b2892", "3420b0", "0f1b61", "e44e37");

    public static AbstractPairingStepFragment getCloudToCloudPairingStep(String productId) {

        if (honeywellThermostatProductIds.contains(productId.toLowerCase())) {
            return HoneywellWebViewSequence.newInstance();
        }

        if (nestThermostatProductIds.contains(productId.toLowerCase())) {
            return NestCloudToCloudPairingStep.newInstance();
        }

        if (lutronProductIds.contains(productId.toLowerCase())) {
            return LutronCloudToCloudPairingStep.newInstance();
        }

        return null;
    }

}
