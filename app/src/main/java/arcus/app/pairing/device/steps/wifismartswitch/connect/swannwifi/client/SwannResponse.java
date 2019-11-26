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
package arcus.app.pairing.device.steps.wifismartswitch.connect.swannwifi.client;

import java.util.Arrays;

/**
 * Represents a message received from the Swann provisioning server running on the smart plug
 * device.
 */
public class SwannResponse extends SwannMessage {

    public enum ResponseType {
        ACK("#/SAMOK#".getBytes()),
        NACK("#/SAMNG#".getBytes()),
        MAC("#/SAMM4#".getBytes()),
        UNKNOWN("".getBytes());

        public final byte[] prefix;

        ResponseType (byte[] prefix) {
            this.prefix = prefix;
        }
    }

    public SwannResponse(byte[] bytes) {
        super(bytes);
    }

    public boolean isSuccess() {
        ResponseType type = getType();
        return type == ResponseType.ACK || type == ResponseType.MAC;
    }

    public ResponseType getType () {
        if (startsWith(ResponseType.ACK.prefix)) {
            return ResponseType.ACK;
        }
        else if (startsWith(ResponseType.NACK.prefix)) {
            return ResponseType.NACK;
        }
        else if (startsWith(ResponseType.MAC.prefix)) {
            return ResponseType.MAC;
        }
        else {
            return ResponseType.UNKNOWN;
        }
    }

    public SwannMessage getPayload () {
        if (getType() == ResponseType.MAC) {
            return new SwannMessage(Arrays.copyOfRange(getBytes(), ResponseType.MAC.prefix.length + 1, getBytes().length));
        } else {
            return new SwannMessage(new byte[0]);
        }
    }

}
