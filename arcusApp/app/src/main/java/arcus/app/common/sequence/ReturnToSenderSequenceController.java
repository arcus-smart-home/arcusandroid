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
package arcus.app.common.sequence;

import android.app.Activity;

import arcus.app.common.backstack.BackstackManager;

/**
 * A trivial sequence controller intended to always return the user to the last displayed fragment
 * (equivalent to pressing the back button) when transitioning away from the current {@link SequencedFragment}.
 *
 * This controller is appropriate for use when invoking a {@link SequencedFragment} as a "singleton"
 * (i.e., the only fragment in sequence).
 */
public class ReturnToSenderSequenceController extends AbstractSequenceController {

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {
        BackstackManager.getInstance().navigateBack();
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {
        BackstackManager.getInstance().navigateBack();
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        BackstackManager.getInstance().navigateBack();
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        // Nothing to do
    }
}
