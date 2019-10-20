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
package arcus.app.account.fingerprint.authentication;

/**
 * A listener that is notified of the results of fingerprint authentication
 */

public interface AuthenticationListener {

    /**
     * Called after a fingerprint is successfully authenticated
     *
     * @param tag   The {@link FingerprintAuthenticator#tag()} identifying the method used for authentication
     */
    void onSuccess(int tag);

    /**
     * Called after an error or an authentication failure
     *
     * @param reason        The reason it failed
     * @param fatal         If true, the sensor is no longer active. If false, the sensor is
     *                      still active and one or more callbacks will be called.
     * @param errorMessage
     * @param tag          The {@link FingerprintAuthenticator#tag()} identifying the method used for authentication
     * @param errorCode     The specific error code returned by the auth method's SDK
     */
    void onFailure(AuthenticationFailureReason reason,
                   boolean fatal,
                   CharSequence errorMessage,
                   int tag,
                   int errorCode);
}
