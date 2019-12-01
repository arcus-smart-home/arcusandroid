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
package arcus.app.common.utils;

import android.Manifest;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.hardware.fingerprint.FingerprintManagerCompat;

import arcus.app.ArcusApplication;
import arcus.app.R;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.SsdkVendorCheck;
import com.samsung.android.sdk.pass.Spass;
import com.samsung.android.sdk.pass.SpassFingerprint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BiometricLoginUtils {

    private final static Logger logger = LoggerFactory.getLogger(LoginUtils.class);
    private static boolean hasPassFingerprintEnrolled;

    /**
     * canFingerprint() is used to determine whether we should show the 'Fingerprint' option in
     * Side Menu > Profile > Settings, and whether the fingerprint enrollment popup appears
     */
    public static boolean canFingerprint(@NonNull Activity activity) {
        Context context = getApplicationContext();
        // Check if uses Pass and has an enrolled fingerprint
        if (initSamsungPass(context) != null){
            if(hasPassFingerprintEnrolled(context)) return true;
        }

        // If no Pass with fingerprints, check if we can use native fingerprint
        if(canUseFingerprint(activity)){
            return true;
        }

        // Finally, check if we can use Pass even if no fingerprints are enrolled
        if (initSamsungPass(context) != null) {
            return true;
        }

        // Can't use fingerprint - don't show in settings menu
        return false;
    }

    /**
     * fingerprintUnavailable() is used to enable/disable the toggle to use fingerprint in
     * Side Menu > Profile > Settings > Fingerprint (when it's available)
     *
     * also in LaunchActivity#initializeFingerprintPrompt(String, int, int)
     * to prevent the use of fingerprint login if, while the app is backgrounded, the user removes fingerprints or
     * disables screen lock
     *
     * Potential use: toast/snack bar for why fingerprint is unavailable.
     */
    public static String fingerprintUnavailable(@NonNull Activity activity) {
        Context context = getApplicationContext();

        KeyguardManager keyguardManager = null;
        FingerprintManagerCompat fingerprintManager = null;

        String message;

        // Make sure we can use fingerprint
        if (BiometricLoginUtils.canFingerprint(activity)) {
            keyguardManager = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            fingerprintManager = FingerprintManagerCompat.from(context);
        }

        message = "";
        if (SsdkVendorCheck.isSamsungDevice()) {
            if (!hasPassFingerprintEnrolled) {
                message = context.getString(R.string.pass_fingerprint_not_set_up);
            }
        }
        else if(fingerprintManager != null && keyguardManager != null){
            if(fingerprintPermissionGranted(activity)){
                if (!fingerprintManager.hasEnrolledFingerprints()){    // Native no fingerprints?
                    message = context.getString(R.string.fingerprint_not_set_up);
                    if (!keyguardManager.isKeyguardSecure()) {       // Native no lock screen?
                        message = context.getString(R.string.lock_screen_not_set_up);
                    }
                }
            } else {
                message = context.getString(R.string.fingerprint_failed);
            }
        }
        return message;
    }

    private static boolean canUseFingerprint(@NonNull Activity activity){
        Context context = getApplicationContext();
        if (fingerprintPermissionGranted(activity)) {
            return FingerprintManagerCompat.from(context).isHardwareDetected();
        }
        return false;
    }

    private static boolean hasPassFingerprintEnrolled(Context context){
        if(initSamsungPass(context) != null){
            SpassFingerprint fingerprint = new SpassFingerprint(context);
            if (fingerprint.hasRegisteredFinger()) {
                return hasPassFingerprintEnrolled = true;
            }
        }
        return hasPassFingerprintEnrolled =false;
    }

    public static Spass initSamsungPass(Context context) {
        Spass sPass = new Spass();
        try {
            sPass.initialize(context);
            /* initialize()
               Initializes the Pass package
               Checks if this is a Samsung device
               Checks if the device supports the Pass package
               Checks if the Pass package libraries are installed
             */
            if(sPass.isFeatureEnabled(Spass.DEVICE_FINGERPRINT)) {
                hasPassFingerprintEnrolled = true;
                return sPass;
            }
        } catch (SsdkUnsupportedException | UnsupportedOperationException e) {
            // This is thrown for non-Samsung devices
            logger.info("This is not a Samsung device and it uses fingerprint: ", e);
        }
        return null;
    }

    private static boolean fingerprintPermissionGranted(@NonNull Activity activity) {
        Context context = getApplicationContext();
        boolean granted = false;

        // The protection level of USE_FINGERPRINT permission (added API 23) is normal instead of dangerous.
        // http://developer.android.com/reference/android/Manifest.permission.html#USE_FINGERPRINT
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if (!(ActivityCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) ==
                    PackageManager.PERMISSION_GRANTED)) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{android.Manifest.permission.USE_FINGERPRINT},
                        GlobalSetting.PERMISSION_USE_FINGERPRINT);

                granted = true;
            } else {
                granted = true;
            }
        }
        return granted;
    }

    private static Context getApplicationContext(){
        return ArcusApplication.getContext();
    }
}
