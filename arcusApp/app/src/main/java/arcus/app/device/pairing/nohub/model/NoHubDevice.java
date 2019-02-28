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
package arcus.app.device.pairing.nohub.model;

import android.support.annotation.Nullable;

import arcus.app.common.sequence.Sequenceable;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.device.pairing.nohub.alexa.controller.VoiceAssistantNoPairingSequenceController;
import arcus.app.device.pairing.nohub.swannwifi.controller.SwannWifiPairingSequenceController;


public enum NoHubDevice {

    SWANN_WIFI(GlobalSetting.SWANN_WIFI_PLUG_PRODUCT_ID),
    ECHO(GlobalSetting.AMAZON_ECHO_PRODUCT_ID),
    TAP(GlobalSetting.AMAZON_TAP_PRODUCT_ID),
    DOT(GlobalSetting.AMAZON_DOT_PRODUCT_ID),
    GOOGLE_HOME(GlobalSetting.GOOGLE_HOME_PRODUCT_ID);

    private final String productId;

    NoHubDevice(String productId) {
        this.productId = productId;
    }

    public Sequenceable getSequence () {
        switch (this) {
            case ECHO: return new VoiceAssistantNoPairingSequenceController(ECHO);
            case TAP: return new VoiceAssistantNoPairingSequenceController(TAP);
            case DOT: return new VoiceAssistantNoPairingSequenceController(DOT);
            case GOOGLE_HOME: return new VoiceAssistantNoPairingSequenceController(GOOGLE_HOME);
            case SWANN_WIFI: return new SwannWifiPairingSequenceController();

            default: throw new IllegalArgumentException("Bug! Not implemented");
        }
    }

    public String getProductId () { return this.productId; }

    public static boolean isNoPairDevice (String productId) {
        return fromProductId(productId) != null;
    }

    public boolean isAlexaDevice() {
        return this == ECHO || this == TAP || this == DOT;
    }

    public boolean isGoogleHomeDevice() {
        return this == GOOGLE_HOME;
    }

    @Nullable public static NoHubDevice fromProductId (String productId) {
        for (NoHubDevice thisNoPairDevice : NoHubDevice.values()) {
            if (thisNoPairDevice.productId.equalsIgnoreCase(productId)) {
                return thisNoPairDevice;
            }
        }

        return null;
    }
}