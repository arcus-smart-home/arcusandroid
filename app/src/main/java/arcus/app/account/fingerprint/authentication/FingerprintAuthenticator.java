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

import android.os.CancellationSignal;

import arcus.app.account.fingerprint.Fingerprint;



public interface FingerprintAuthenticator {
    /**
     * Return true if a fingerprint reader of this type exists on the current device.
     * <p/>
     * Don't call this method directly. Register an instance of this module with Reprint, then call
     * {@link Fingerprint#isHardwareAvailable()}
     */
    boolean isHardwareAvailable();

    /**
     * Return true if there are registered fingerprints on the current device.
     * <p/>
     * If this returns true, {@link #authenticate(CancellationSignal, AuthenticationListener,
     * Fingerprint.KeepSensorActive)}, it should be possible to perform authentication with this
     * module.
     * <p/>
     * Don't call this method directly. Register an instance of this module with Reprint, then call
     * {@link Fingerprint#hasRegisteredFingerprint()}
     */
    boolean hasRegisteredFingerprint();

    /**
     * Start a fingerprint authentication request.
     * <p/>
     * Don't call this method directly. Register an instance of this module with Reprint, then call
     * {@link Fingerprint#authenticate(AuthenticationListener)}
     *
     * @param cancellationSignal A signal that can cancel the authentication request.
     * @param listener           A listener that will be notified of the authentication status.
     * @param keepSensorActive   If true, the module should ensure the sensor
     *                           is still running, and should not call any methods on the listener.
     *                           If the predicate returns false, the module should ensure the sensor
     *                           is not running before calling {@link AuthenticationListener#onFailure(AuthenticationFailureReason,
     *                           boolean, CharSequence, int, int)}.
     */
    void authenticate(CancellationSignal cancellationSignal, AuthenticationListener listener, Fingerprint.KeepSensorActive keepSensorActive);

    /**
     * A tag uniquely identifying this class. It must be the same for all instances of each class,
     * and each class's tag must be unique among registered modules.
     */
    int tag();
}
