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
package arcus.app.device.pairing.catalog.model;

import com.iris.client.capability.Hub;
import arcus.app.R;
import arcus.app.device.settings.core.Localizable;


public enum HubPairingState implements Localizable {
    NO_HUB(R.string.hub_pairing_in_pairing_mode_text),
    HUB_OFFLINE(R.string.hub_offline_text),
    NOT_IN_PAIRING_MODE(R.string.hub_pairing_not_in_pairing_mode_text),
    PAIRING(R.string.hub_pairing_in_pairing_mode_text),
    PAIRING_REQUESTED(R.string.hub_pairing_requested_text),
    UNPAIRING(R.string.hub_pairing_in_unpairing_mode_text);

    private final int stringResId;

    HubPairingState (int stringResId) {
        this.stringResId = stringResId;
    }

    @Override
    public int getStringResId () {
        return this.stringResId;
    }

    public static HubPairingState fromHubState (String hubState) {
        switch (hubState) {
            case Hub.STATE_PAIRING:
                return PAIRING;
            case Hub.STATE_UNPAIRING:
                return UNPAIRING;
            case Hub.STATE_NORMAL:
                return NOT_IN_PAIRING_MODE;
            case Hub.STATE_DOWN:
                return HUB_OFFLINE;
            default:
                return NO_HUB;
        }
    }
}
