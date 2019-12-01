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

import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.CancellationSignal;

import androidx.annotation.NonNull;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.account.fingerprint.authentication.AuthenticationFailureReason;
import arcus.app.account.fingerprint.authentication.AuthenticationListener;
import arcus.app.account.fingerprint.authentication.FingerprintAuthenticator;
import arcus.app.common.utils.LoginUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Methods for performing fingerprint authentication
 */

public class FingerprintHelper {
    private final static Logger logger = LoggerFactory.getLogger(LoginUtils.class);

    private CancellationSignal mCancellationSignal;
    private FingerprintAuthenticator mAuthenticator;
    private String sensorMissing = "";
    private String fingerprintNotSetup = "";

    private static final FingerprintHelper INSTANCE = new FingerprintHelper();

    public static FingerprintHelper getInstance() {
        return INSTANCE;
    }

    private FingerprintHelper(){}

    public FingerprintHelper initialize(@NonNull Resources resources) {
        if (mAuthenticator != null) return this;

        sensorMissing = resources.getString(R.string.fingerprint_sensor_hardware_missing);
        fingerprintNotSetup = resources.getString(R.string.fingerprint_not_set_up);

        // Try Pass first
        try {
            mAuthenticator = new SamsungPass();
            registerAuthenticator(mAuthenticator);
        } catch (Exception e) {
            // nothing to do. Try native next
            logger.debug("Couldn't use Pass. Trying native fingerprint.");
        }

        // Native fingerprint is supported on M+ only.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            registerAuthenticator(new NativeFingerprint());
        }

        return this;
    }

    public FingerprintHelper registerAuthenticator(FingerprintAuthenticator authenticator) {
        if (authenticator == null || mAuthenticator != null && authenticator.tag() == mAuthenticator.tag()) {
            return this;
        }

        if (authenticator.isHardwareAvailable()) {
            mAuthenticator = authenticator;
        }
        return this;
    }

    public boolean isHardwareAvailable() {
        logger.debug("The authentication method has no fingerprint sensor.");
        return mAuthenticator != null && mAuthenticator.isHardwareAvailable();
    }

    public boolean hasRegisteredFingerprint() {
        logger.debug("The authentication method has registered fingerprints.");
        return mAuthenticator !=null && mAuthenticator.hasRegisteredFingerprint();
    }

    /**
     * Start an authentication request
     * @param listener              The listener to be notified
     * @param keepSensorActive      Determines if we should retry
     */
    public void authenticate(final AuthenticationListener listener,
                                    Fingerprint.KeepSensorActive keepSensorActive){
        // If somehow the phone has no way to detect fingerprints and we got here, send a fatal message and kick back
        // to username/password login. This shouldn't happen.
        if(mAuthenticator == null) {
            logger.debug("Failing fingerprint authentication - Unsupported device - at {}", getClass().getSimpleName());
            listener.onFailure(AuthenticationFailureReason.NO_SENOR, true,
                    "no authentication method", 0, 0);
            return;
        }

        // If there's no hardware to detect fingerprint, send a fatal message and kick back to username/password login.
        if(!mAuthenticator.isHardwareAvailable()){
            logger.debug("Failing fingerprint authentication for reason {}, at:  ", AuthenticationFailureReason.NO_SENOR, getClass().getSimpleName());
            listener.onFailure(AuthenticationFailureReason.NO_SENOR, true,
                    sensorMissing, 0, 0);
            return;
        }

        // If there are no fingerprints enrolled, send a fatal message and kick back to username/password login.
        if(!mAuthenticator.hasRegisteredFingerprint()){
            logger.debug("Failing fingerprint authentication for reason {}, at:  ", AuthenticationFailureReason.NO_REGISTERED_FINGERPRINTS, getClass().getSimpleName());
            listener.onFailure(AuthenticationFailureReason.NO_REGISTERED_FINGERPRINTS, true,
                    fingerprintNotSetup, 0, 0);
        }

        mCancellationSignal = new CancellationSignal();
        mAuthenticator.authenticate(mCancellationSignal, listener, keepSensorActive);
    }

    public void cancelAuthentication(){
        if(mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
    }
}
