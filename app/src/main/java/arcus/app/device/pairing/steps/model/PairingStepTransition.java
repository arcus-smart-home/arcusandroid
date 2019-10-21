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
package arcus.app.device.pairing.steps.model;


public enum PairingStepTransition {

    /**
     * Attempt to go back from first step; return to catalog
     */
    GO_START,

    /**
     * User clicked back midway through pairing steps, go to previous step
     */
    GO_PREVIOUS,

    /**
     * User clicked next midway through pairing steps, go to next step
     */
    GO_NEXT,

    /**
     * User clicked next on last catalog-provided step, go to "searching for device" page
     */
    GO_END,

    /**
     * User clicked next on a special device (i.e., Amazon Alexa) that does not pair but that
     * has information "no-pairing" instructions.
     */
    GO_NO_PAIRING
}
