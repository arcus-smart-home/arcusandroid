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



public enum AuthenticationFailureReason {
    /**
     * No fingerprint sensor on the device.
     */
    NO_SENOR,
    /**
     * The sensor is unavailable for some reason.
     */
    SENSOR_UNAVAILABLE,
    /**
     * No fingerprints registered
     */
    NO_REGISTERED_FINGERPRINTS,
    /**
     * The sensor failed to read the fingerprint.
     */
    SENSOR_FAILED,
    /**
     * Too many failed attempts - user locked out for ??? seconds.
     */
    LOCKED_OUT,
    /**
     * The user canceled the operation by dismissing the dialog
     */
    USER_CANCELED,
    /**
     * The sensor was running for too long without an auth attempt.
     */
    SENSOR_TIMEOUT,
    /**
     * The fingerprint was read, but not recognized.
     */
    AUTHENTICATION_FAILED,
    /**
     * An unsupported operation attempt was made
     */
    OPERATION_DENIED,
    /**
     * Mystery of mysteries.
     * No fingerprintManager
     */
    UNKNOWN
}
