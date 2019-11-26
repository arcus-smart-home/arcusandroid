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

public interface SwannMessageListener {

    /**
     * Indicates that message was received on the Swann socket channel. Inspect {@link SwannResponse#getType()}
     * to determine the type/meaning of the response.
     * @param message The received message.
     */
    void onMessageReceived(SwannResponse message);

    /**
     * Called to indicate the socket has closed and that the listener has shut down. It is the
     * implementers responsibility to determine if this was expected or represents an error.
     */
    void onSocketClosed();
}
