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
import android.content.Context;
import android.os.CancellationSignal;

import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.account.fingerprint.authentication.AuthenticationFailureReason;
import arcus.app.account.fingerprint.authentication.AuthenticationListener;
import arcus.app.account.fingerprint.authentication.FingerprintAuthenticator;
import arcus.app.common.utils.BiometricLoginUtils;
import arcus.app.common.utils.LoginUtils;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.samsung.android.sdk.pass.SpassFingerprint.STATUS_AUTHENTIFICATION_FAILED;
import static com.samsung.android.sdk.pass.SpassFingerprint.STATUS_AUTHENTIFICATION_SUCCESS;
import static com.samsung.android.sdk.pass.SpassFingerprint.STATUS_BUTTON_PRESSED;
import static com.samsung.android.sdk.pass.SpassFingerprint.STATUS_OPERATION_DENIED;
import static com.samsung.android.sdk.pass.SpassFingerprint.STATUS_QUALITY_FAILED;
import static com.samsung.android.sdk.pass.SpassFingerprint.STATUS_SENSOR_FAILED;
import static com.samsung.android.sdk.pass.SpassFingerprint.STATUS_TIMEOUT_FAILED;
import static com.samsung.android.sdk.pass.SpassFingerprint.STATUS_USER_CANCELLED;
import static com.samsung.android.sdk.pass.SpassFingerprint.STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE;

/**
 * Samsung's fingerprint authentication
 */

public class SamsungPass implements FingerprintAuthenticator {
    private final static Logger logger = LoggerFactory.getLogger(LoginUtils.class);

    public static final int TAG = 2;

    public static final int NO_FINGERPRINT_REGISTERED = 9000;
    public static final int HARDWARE_UNAVAIALABLE = 9001;
    public static final int TEMPORARILY_LOCKED_OUT = 9002;
    public static final int WTF = 9003;

    private Spass mSpass;
    private SpassFingerprint mSpassFingerprint;

    public SamsungPass() { mSpass = BiometricLoginUtils.initSamsungPass(getContext()); }

    @Override
    public boolean isHardwareAvailable() {
        try{
            return mSpass!= null && mSpass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT);
        } catch (Exception e){
            logger.debug("Fingerprint hardware is unavailable: ", e);
            return false;
        }
    }

    @Override
    public boolean hasRegisteredFingerprint() {
        try {
            if(isHardwareAvailable()){
                if(mSpassFingerprint == null) {
                    mSpassFingerprint = new SpassFingerprint(getContext());
                }
                return mSpassFingerprint.hasRegisteredFinger();
            }
        } catch (Exception e){
            logger.debug("No registered fingerprints: ", e);
        }
        return false;
    }

    @Override
    public void authenticate(CancellationSignal cancellationSignal, AuthenticationListener listener,
                             Fingerprint.KeepSensorActive keepSensorActive) {
        authenticate(cancellationSignal,listener, keepSensorActive, 0);

    }

    private void authenticate(final CancellationSignal cancellationSignal
            , final AuthenticationListener listener
            , final Fingerprint.KeepSensorActive keepSensorActive
            , final int retryCount){

        if(mSpassFingerprint == null) {
            mSpassFingerprint = new SpassFingerprint(getContext());
        }

        try{
             if(!isHardwareAvailable()){
                 listener.onFailure(AuthenticationFailureReason.SENSOR_UNAVAILABLE, true, "Hardware unavailable", TAG, HARDWARE_UNAVAIALABLE);
                 return;
             }
             if(!mSpassFingerprint.hasRegisteredFinger()) {
                 listener.onFailure(AuthenticationFailureReason.NO_REGISTERED_FINGERPRINTS, true, getContext().getString(R.string.pass_fingerprint_not_set_up), TAG, NO_FINGERPRINT_REGISTERED);
                 return;
             }
        } catch (Throwable throwable){
             logger.debug("Hardware and fingerprint enrolled. Unknown failure: ", throwable);
             listener.onFailure(AuthenticationFailureReason.UNKNOWN, true, String.valueOf(throwable), TAG, WTF);
             return;
        }

        cancelAuth(mSpassFingerprint);

        try {
            mSpassFingerprint.startIdentify(new SpassFingerprint.IdentifyListener() {
                @Override
                public void onFinished(int result) {
                    String fingerprintGuide;
                    switch(result) {
                        case STATUS_AUTHENTIFICATION_SUCCESS:
                            listener.onSuccess(TAG);
                            return;
                        case STATUS_TIMEOUT_FAILED:
                            fail(AuthenticationFailureReason.SENSOR_TIMEOUT, false, getContext().getString(R.string.pass_fingerprint_timeout), result);
                            break;
                        case STATUS_AUTHENTIFICATION_FAILED:
                            fail(AuthenticationFailureReason.AUTHENTICATION_FAILED, false, getContext().getString(R.string.fingerprint_not_recognized), result);
                            break;
                        case STATUS_QUALITY_FAILED:
                            fingerprintGuide = mSpassFingerprint.getGuideForPoorQuality();
                            fail(AuthenticationFailureReason.SENSOR_FAILED, false, fingerprintGuide, result);
                            break;
                        case STATUS_SENSOR_FAILED:
                            fail(AuthenticationFailureReason.SENSOR_FAILED, false, R.string.pass_fingerprint_insufficient, result);
                            break;
                        case STATUS_USER_CANCELLED_BY_TOUCH_OUTSIDE:
                        case STATUS_USER_CANCELLED:
                        case STATUS_BUTTON_PRESSED:
                            fail(AuthenticationFailureReason.USER_CANCELED, true, R.string.user_canceled, result);
                            break;
                        case STATUS_OPERATION_DENIED:
                            fail(AuthenticationFailureReason.OPERATION_DENIED, true, getContext().getString(R.string.fingerprint_failed), result);
                            break;
                        default:
                            fail(AuthenticationFailureReason.UNKNOWN, true, R.string.fingerprint_failed, result);
                            break;
                    }
                }



                /**
                 * Can only fail as many times as allowed in {@link Fingerprint#RETRY_LIMIT}
                 * After, the fingerprint sensor stops
                 *  **/
                private void fail(AuthenticationFailureReason reason, boolean fatal, int message, int status) {
                    try {
                        fail(reason, fatal, getContext().getString(message), status);
                    } catch (NullPointerException npe) {
                        // A NPE is thrown when the phone sleeps while the dialog is displayed
                        logger.debug("We lost the activity! The phone went to sleep, or the app was backgrounded" + npe);
                        return;
                    }
                }

                private void fail(AuthenticationFailureReason reason, boolean fatal, String message, int status) {
                    listener.onFailure(reason, fatal, message, TAG, status);
                    if ((!fatal || reason == AuthenticationFailureReason.SENSOR_TIMEOUT)
                            && keepSensorActive.attemptAuth(reason, retryCount)) {
                        authenticate(cancellationSignal, listener, keepSensorActive, retryCount + 1);
                    }
                }

                @Override
                public void onReady() {
                    // nothing here
                }
                @Override
                public void onStarted() {
                    // nothing here
                }
                @Override
                public void onCompleted() {
                    // nothing here
                }
            });
        } catch (Throwable t) {
            listener.onFailure(AuthenticationFailureReason.LOCKED_OUT, true, null, TAG, TEMPORARILY_LOCKED_OUT);
            return;
        }

        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            @Override
            public void onCancel() {
                cancelAuth(mSpassFingerprint);
            }
        });
    }

    @Override
    public int tag() {
        return TAG;
    }

    private static void cancelAuth(SpassFingerprint spassFingerprint){
        try {
        spassFingerprint.cancelIdentify();
        } catch (Throwable t) {
            // There's no way to query if there's an active identify request,
            // so just try to cancel and ignore any exceptions.
        }
    }

    private Context getContext() {
        return ArcusApplication.getArcusApplication().getApplicationContext();
    }
}
