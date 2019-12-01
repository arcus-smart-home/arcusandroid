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
package arcus.app.account.fingerprint;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import arcus.app.account.fingerprint.authentication.AuthenticationFailureReason;
import arcus.app.account.fingerprint.authentication.AuthenticationListener;

/**
 * Fingerprint
 */

public class Fingerprint {

    private static final int RETRY_LIMIT = 2;

    private static FingerprintHelper mHelper = FingerprintHelper.getInstance();

    public interface KeepSensorActive {
        /**
         * Return true if we should try authenticating  again after a non-fatal error
         *
         * @param reason            the reason authentication failed
         * @param retryCounter      the number of times we've retried
         */
        boolean attemptAuth(AuthenticationFailureReason reason, int retryCounter);
    }

    /**
     * Load all fingerprint authentication methods available
     *
     * This calls Samsung Pass, followed by Android's native FingerprintManager
     */
    public static void initialize(@NonNull Resources resources){
        mHelper.initialize(resources);
    }

    /**
     * Return true if fingerprint hardware exists on the device
     */
    public static boolean isHardwareAvailable(){
        return mHelper.isHardwareAvailable();
    }

    /**
     * Return true if at least 1 fingerprint is registered
     */
    public static boolean hasRegisteredFingerprint(){
        return mHelper.hasRegisteredFingerprint();
    }

    /**
     * Start an authentication request
     *
     * @param listener      the listener that will be notified of authentication events
     */
    public static void authenticate (AuthenticationListener listener){
        authenticate(listener, defaultRetries());
    }

    /**
     *  Start an authentication request
     *
     *  if {@link #isHardwareAvailable()} or {@link #hasRegisteredFingerprint()} return false,
     *  do not authenticate. The listener's {@link AuthenticationListener#onFailure} will be called with the failure reason.
     *
     *  errorMessage will be non-null, fatal will be true, and other values are unspecified
     *
     *  @param listener     The listener that will be notified of authentication events
     *  @param keepActive   Will be called after each failure. If true, the fingerprint sensor
     *                      will remain active and the listner will not be called. If false, the
     *                      sensor will be turned off and onFailure will be called.
     */

    public static void authenticate(AuthenticationListener listener, KeepSensorActive keepActive) {
        mHelper.authenticate(listener, keepActive);
    }

    /**
     * Cancel any active authentication requests
     */
    public static void cancelAuthentication(){
        mHelper.cancelAuthentication();
    }


    private static KeepSensorActive maximumRetries(final int maxRetries) {
        return new Fingerprint.KeepSensorActive(){
            private int tries = 0;

            @Override
            public boolean attemptAuth(AuthenticationFailureReason reason, int retryCounter) {
                return reason != AuthenticationFailureReason.SENSOR_TIMEOUT || tries ++ < maxRetries;
            }
        };
    }

    private static KeepSensorActive defaultRetries(){
        return maximumRetries(RETRY_LIMIT); }
}
