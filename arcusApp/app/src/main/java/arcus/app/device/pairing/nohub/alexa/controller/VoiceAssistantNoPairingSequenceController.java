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
package arcus.app.device.pairing.nohub.alexa.controller;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.dashboard.HomeFragment;
import arcus.app.device.pairing.catalog.controller.ProductCatalogFragmentController;
import arcus.app.device.pairing.multi.controller.MultipairingSequenceController;
import arcus.app.device.pairing.nohub.alexa.VoiceSkillsFragment;
import arcus.app.device.pairing.nohub.model.NoHubDevice;
import arcus.app.device.pairing.steps.controller.DevicePairingStepsSequenceController;

import java.util.ArrayList;


public class VoiceAssistantNoPairingSequenceController extends AbstractSequenceController {

    private final NoHubDevice voiceAssistantDevice;

    public VoiceAssistantNoPairingSequenceController(NoHubDevice voiceAssistantDevice) {
        this.voiceAssistantDevice = voiceAssistantDevice;
    }

    // A reference back to the product catalog provided pairing steps sequence
    private DevicePairingStepsSequenceController previousStepsSequence;

    // A reference back to whatever sequence was used to launch the product catalog pairing steps
    // (as of this writing, only ever the ProductCatalogSequenceController)
    private Sequenceable previousRootSequence;

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {
        throw new IllegalStateException("Bug! goNext() not defined in this sequence");
    }

    public void goAlexaInstructions(Activity activity) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GlobalSetting.ALEXA_PAIRING_INSTRUCTIONS_URL)));
    }

    public void goGoogleInstructions(Activity activity) {
        activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(GlobalSetting.GOOGLE_PAIRING_INSTRUCTIONS_URL)));
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {
        if (from instanceof VoiceSkillsFragment) {
            previousStepsSequence.resumeFromEndOfPairingSteps(activity);
        } else {
            navigateBack(activity, VoiceSkillsFragment.newInstance(voiceAssistantDevice), data);
        }
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {

        // Special case: When Alexa pairing done, show multipairing list if user had paired devices
        // from within the catalog.
        if (ProductCatalogFragmentController.instance().getDevicesPaired().size() > 0) {
            navigateForward(activity, new MultipairingSequenceController(), new ArrayList<>(ProductCatalogFragmentController.instance().getDevicesPaired().keySet()));
        }

        // Normal case: Return to root sequence (product catalog) when done with Alexa
        else {
            // Per ITWO-6746 user should return to dashboard, not root sequence
            // navigateBack(activity, previousRootSequence, data);

            BackstackManager.getInstance().navigateBackToFragment(HomeFragment.newInstance());
        }
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        this.previousStepsSequence = (DevicePairingStepsSequenceController) from;
        this.previousRootSequence = unpackArgument(0, Sequenceable.class, data);

        navigateForward(activity, VoiceSkillsFragment.newInstance(voiceAssistantDevice), data);
    }
}
