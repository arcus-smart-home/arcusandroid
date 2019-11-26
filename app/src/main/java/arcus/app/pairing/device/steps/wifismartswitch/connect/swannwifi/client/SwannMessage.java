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

/**
 * Represents a message sent to, or received from, the Swann provisioning server running on the
 * Smart Plug device.
 */
public class SwannMessage {

    private final byte[] bytes;

    public SwannMessage(byte[] bytes) {
        this.bytes = bytes;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public String toHexString () {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for(byte b: bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "SwannMessage{" +
                "bytes=" + new String(bytes) +
                '}';
    }

    protected boolean startsWith (byte[] prefix) {
        if (getBytes().length < prefix.length) {
            return false;
        }

        for (int index = 0; index < prefix.length; index++) {
            if (prefix[index] != getBytes()[index]) {
                return false;
            }
        }

        return true;
    }
}
