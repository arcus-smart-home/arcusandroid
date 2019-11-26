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
 * Represents a request message made to the Swann provisioning server.
 */
public class SwannRequest extends SwannMessage {

    public enum RequestType {
        REBOOT("#/SAMRS#".getBytes()),
        PASSWORD("#/SAMPW#".getBytes()),
        SSID("#/SAMSS#".getBytes()),
        MAC("#/SAMMR#".getBytes());

        public final byte[] prefix;

        RequestType (byte[] prefix) {
            this.prefix = prefix;
        }
    }

    private static final int DATA_LENGTH_POS = 8;
    private final RequestType type;

    public SwannRequest(RequestType type, byte[] bytes) {
        super(bytes);
        this.type = type;
    }

    public RequestType getType() {
        return type;
    }

    public static SwannRequest ofMacRequest() {
        byte[] message = new byte[DATA_LENGTH_POS+1];
        byte[] header = RequestType.MAC.prefix;
        System.arraycopy(header,0,message,0,header.length);
        message[DATA_LENGTH_POS] = 0;

        return new SwannRequest(RequestType.MAC, message);
    }

    public static SwannRequest ofSetTargetSsid(String targetSSID) {
        byte[] message = new byte[DATA_LENGTH_POS+1+ targetSSID.length()];
        byte[] header = RequestType.SSID.prefix;
        byte[] payload = targetSSID.getBytes();
        System.arraycopy(header,0,message,0,header.length);
        message[DATA_LENGTH_POS] = (byte)targetSSID.length();
        System.arraycopy(payload,0,message, DATA_LENGTH_POS +1, targetSSID.length());

        return new SwannRequest(RequestType.SSID, message);
    }

    public static SwannRequest ofSetTargetPassword(String targetPass) {
        byte[] message = new byte[DATA_LENGTH_POS+1+ targetPass.length()];
        byte[] header = RequestType.PASSWORD.prefix;
        byte[] payload = targetPass.getBytes();
        System.arraycopy(header,0,message,0,header.length);
        message[DATA_LENGTH_POS] = (byte)targetPass.length();
        for (int i = 0 ; i < targetPass.length(); i++){
            message[DATA_LENGTH_POS+1+i] = (byte) (payload[i] + 10);
        }

        return new SwannRequest(RequestType.PASSWORD, message);
    }

    public static SwannRequest ofRebootRequest() {
        byte[] message = new byte[DATA_LENGTH_POS+1];
        byte[] header = RequestType.REBOOT.prefix;
        System.arraycopy(header,0,message,0,header.length);
        message[DATA_LENGTH_POS] = 0;

        return new SwannRequest(RequestType.REBOOT, message);
    }

}
