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

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import androidx.annotation.StringRes;

import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.account.fingerprint.authentication.AuthenticationFailureReason;
import arcus.app.account.fingerprint.authentication.AuthenticationListener;
import arcus.app.account.fingerprint.authentication.FingerprintAuthenticator;
import arcus.app.common.utils.LoginUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static android.hardware.fingerprint.FingerprintManager.FINGERPRINT_ERROR_CANCELED;
import static android.hardware.fingerprint.FingerprintManager.FINGERPRINT_ERROR_HW_UNAVAILABLE;
import static android.hardware.fingerprint.FingerprintManager.FINGERPRINT_ERROR_LOCKOUT;
import static android.hardware.fingerprint.FingerprintManager.FINGERPRINT_ERROR_TIMEOUT;
import static android.hardware.fingerprint.FingerprintManager.FINGERPRINT_ERROR_UNABLE_TO_PROCESS;

/**
 * Android native fingerprint authentication
 */
@TargetApi(Build.VERSION_CODES.M)
public class NativeFingerprint implements FingerprintAuthenticator {
    private final static Logger logger = LoggerFactory.getLogger(LoginUtils.class);

    public static final int TAG = 1;

    private static final int HARDWARE_UNAVAILABLE = 7000;
    private static final int FINGERPRINT_MANAGER_ERROR = 8000;
    private static final int FINGERPRINT_AUTHENTICATION_FAILED = 9000;


    public NativeFingerprint() {}

    private FingerprintManager getFingerprintManager() {
        try {
            return getContext().getSystemService(FingerprintManager.class);
        } catch (NoClassDefFoundError e) {
            logger.debug("FingerprintManager class not found: ", String.valueOf(e));
        }
        return null;
    }

    @Override
    public boolean isHardwareAvailable() {
        final FingerprintManager fingerprintManager = getFingerprintManager();
        if(fingerprintManager == null) {
            return false;
        }
        try {
            return fingerprintManager.isHardwareDetected();
        } catch (SecurityException | NullPointerException e) {
            logger.debug("FingerprintManager NPE: ", String.valueOf(e));
            return false;
        }
    }

    @Override
    public boolean hasRegisteredFingerprint() throws SecurityException {
        final FingerprintManager fingerprintManager = getFingerprintManager();
        return fingerprintManager != null && fingerprintManager.hasEnrolledFingerprints();
    }

    @Override
    public void authenticate(CancellationSignal cancellationSignal, AuthenticationListener listener,
                             Fingerprint.KeepSensorActive keepSensorActive) {
        authenticate(cancellationSignal, listener, keepSensorActive, 0);
    }

    private void authenticate(final CancellationSignal cancellationSignal,
                      final AuthenticationListener listener,
                      final Fingerprint.KeepSensorActive keepSensorActive,
                      final int retryCount) {

        // Get the FingerPrint Manager, authentication callback, and cancellation signal object (for authentication)
        final FingerprintManager fingerprintManager = getFingerprintManager();
        final FingerprintManager.AuthenticationCallback callback = new AuthenticationCallback(retryCount, keepSensorActive, cancellationSignal, listener);


        if(fingerprintManager == null){
            listener.onFailure(AuthenticationFailureReason.UNKNOWN, true, getString(R.string.hardware_unavailable), TAG, HARDWARE_UNAVAILABLE);
            return;
        }

        try{
            fingerprintManager.authenticate(null, cancellationSignal, 0, callback, null);
        } catch (NullPointerException npe){
            listener.onFailure(AuthenticationFailureReason.UNKNOWN, true, getString(R.string.authentication_failed), TAG, FINGERPRINT_MANAGER_ERROR);
        }
    }

    /**
     * Fingerprint Authentication callbacks
     */
    class AuthenticationCallback extends FingerprintManager.AuthenticationCallback {
        private final Fingerprint.KeepSensorActive mKeepSensorActive;
        private final CancellationSignal mCancellationSignal;
        private final AuthenticationListener mListener;
        private int mRetryCount;

        public AuthenticationCallback(int retryCount, Fingerprint.KeepSensorActive keepSensorActive, CancellationSignal cancellationSignal, AuthenticationListener listener){
            mRetryCount = retryCount;
            mKeepSensorActive = keepSensorActive;
            mCancellationSignal = cancellationSignal;
            mListener = listener;
        }

        @Override
        public void onAuthenticationError(int errorCode, CharSequence errString) {
            // Unrecoverable error occurred - ex: "Too many attempts"
            AuthenticationFailureReason failure;
            String failureMessage= (String) errString;
            switch (errorCode){
                case FINGERPRINT_ERROR_HW_UNAVAILABLE:
                    failure = AuthenticationFailureReason.SENSOR_UNAVAILABLE;
                    fail(failure, false, failureMessage, errorCode);
                    break;
                case FINGERPRINT_ERROR_UNABLE_TO_PROCESS:
                    failure = AuthenticationFailureReason.SENSOR_FAILED;
                    fail(failure, false, failureMessage, errorCode);
                    break;
                case FINGERPRINT_ERROR_TIMEOUT:
                    failure = AuthenticationFailureReason.SENSOR_TIMEOUT;
                    fail(failure, false, failureMessage, errorCode);
                    break;
                case FINGERPRINT_ERROR_LOCKOUT:
                    failure = AuthenticationFailureReason.LOCKED_OUT;
                    fail(failure, true, failureMessage, errorCode);
                    break;
                case FINGERPRINT_ERROR_CANCELED:
                    // Don't cancel
                    break;
            }
        }

        @Override
        public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
            // A recoverable error has occurred - show help text
            fail(AuthenticationFailureReason.AUTHENTICATION_FAILED, false,
                    helpString,
                    FINGERPRINT_AUTHENTICATION_FAILED);
        }

        @Override
        public void onAuthenticationFailed() {
            // Fingerprint read, but not recognized. Limited retries

            // If too many attempts, cancel and send fatal
            if(!mKeepSensorActive.attemptAuth(AuthenticationFailureReason.SENSOR_FAILED, mRetryCount + 1)){
                mCancellationSignal.cancel();
                fail(AuthenticationFailureReason.AUTHENTICATION_FAILED, true,
                        getString(R.string.fingerprint_failed),
                        FINGERPRINT_AUTHENTICATION_FAILED);
            }

            // Otherwise, update the UI and try again
            fail(AuthenticationFailureReason.AUTHENTICATION_FAILED, false,
                    getString(R.string.fingerprint_not_recognized),
                    FINGERPRINT_AUTHENTICATION_FAILED);
        }

        @Override
        public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
            mListener.onSuccess(TAG);
        }



        /**
         * If fingerprint read, but not recognized, try up to the limit
         * If a recoverable error, unlimited tries
         * If an unrecoverable error, fail immediately to normal login
         */
        private void fail(AuthenticationFailureReason reason, boolean fatal, CharSequence message, int status){
            if(fatal){
                mListener.onFailure(reason, true, message, TAG, status);
            }
            if(!fatal && mKeepSensorActive.attemptAuth(reason, mRetryCount)){
                authenticate(mCancellationSignal, mListener, mKeepSensorActive, mRetryCount +1);
            }
            mListener.onFailure(reason, false, message, TAG, status);
        }
    }

    @Override
    public int tag() {
        return TAG;
    }

    private String getString(@StringRes int stringRes) {
        return getContext().getString(stringRes);
    }

    private Context getContext() {
        return ArcusApplication.getArcusApplication().getApplicationContext();
    }
}
