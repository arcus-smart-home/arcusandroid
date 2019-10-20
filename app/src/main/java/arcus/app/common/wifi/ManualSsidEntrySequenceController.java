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
package arcus.app.common.wifi;

import android.app.Activity;

import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.sequence.AbstractSequenceController;
import arcus.app.common.sequence.Sequenceable;


public class ManualSsidEntrySequenceController extends AbstractSequenceController {

    public interface ManualSsidEntryListener {
        void onManualSsidEntry(String ssid);
    }

    private ManualSsidEntryListener listener;

    @Override
    public void goNext(Activity activity, Sequenceable from, Object... data) {
        throw new IllegalStateException("Bug! goNext() not defined in this sequence.");
    }

    @Override
    public void goBack(Activity activity, Sequenceable from, Object... data) {
        BackstackManager.getInstance().navigateBack();
    }

    @Override
    public void endSequence(Activity activity, boolean isSuccess, Object... data) {
        String enteredSsid = unpackArgument(0, String.class, data);

        BackstackManager.getInstance().navigateBack();

        if (listener != null) {
            listener.onManualSsidEntry(enteredSsid);
        }
    }

    @Override
    public void startSequence(Activity activity, Sequenceable from, Object... data) {
        listener = unpackArgument(0, ManualSsidEntryListener.class, data);
        navigateForward(activity, ManualSsidEntryFragment.newInstance());
    }
}
