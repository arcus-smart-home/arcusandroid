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
package arcus.app.common.popups;

import androidx.annotation.Nullable;

import arcus.app.R;


public class PairingInProgressPopup extends ArcusFloatingFragment {

    public static PairingInProgressPopup newInstance() {
        return new PairingInProgressPopup();
    }

    public PairingInProgressPopup() {
        super(false);       // Do not show a close box on this sheet
    }

    @Override
    public void setFloatingTitle() {
        title.setText(getTitle());
    }

    @Override
    public void doContentSection() {
        // Nothing to do
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.popup_pairing_in_progress;
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.swann_pairing);
    }

    @Override
    public boolean onBackPressed() {
        // User cannot close this by pressing back
        return true;
    }
}
