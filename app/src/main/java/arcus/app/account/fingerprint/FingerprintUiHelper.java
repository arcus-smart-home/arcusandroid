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
import arcus.app.R;
import arcus.app.account.fingerprint.authentication.AuthenticationFailureReason;
import arcus.app.account.fingerprint.authentication.AuthenticationListener;
import arcus.app.common.view.CircularImageView;
import arcus.app.common.view.ScleraTextView;



public class FingerprintUiHelper {

    private static long ERROR_TIMEOUT_MILLIS = 1800;

    private ScleraTextView mFingerprintStatus;
    private CircularImageView mFingerprintImg;

    private final Callback mCallback;

    /**
     * Constructor for {@link FingerprintUiHelper}
     */
    public FingerprintUiHelper(CircularImageView fingerprintImg, ScleraTextView fingerprintStatus, Callback callback) {
        mFingerprintImg = fingerprintImg;
        mFingerprintStatus = fingerprintStatus;
        mCallback = callback;
    }

    /**
     * Set the status text and image and initialize the fingerprint sensor
     *
     * Get the result of the authentication attempt from the listener and update the UI
     * If the boolean is true at {@link AuthenticationListener#onFailure(AuthenticationFailureReason, boolean, CharSequence, int, int)}, the sensor has stopped and we call back {@link FingerprintPopup#onUiFailure()}f
     */
    public void startSensor(@NonNull Resources resources){
        mFingerprintImg.setImageResource(R.drawable.fingerprint_icon_small);
        mFingerprintStatus.setText(mFingerprintStatus.getResources().getString(R.string.fingerprint_touch_sensor));

        Fingerprint.initialize(resources);

        // If there's no fingerprint/hardware, kill the popup
        if(!Fingerprint.isHardwareAvailable()){
            mCallback.onUiFailure();
        }
        Fingerprint.authenticate( new AuthenticationListener() {
            @Override
            public void onSuccess(int tag) {
                mFingerprintImg.setImageResource(R.drawable.success_check_icon);
                mFingerprintStatus.setTextColor(mFingerprintStatus.getResources().getColor(R.color.sclera_green));
                mFingerprintStatus.setText(mFingerprintStatus.getResources().getString(R.string.fingerprint_recognized));
                mCallback.onUiSuccess();
            }

            @Override
            public void onFailure(AuthenticationFailureReason reason, boolean fatal, CharSequence errorMessage, int tag,
                                  int errorCode) {
                showError(errorMessage);
                if(fatal){
                    // The sensor has stopped -- fail everything and stop trying
                    showUnrecoverableError(mFingerprintStatus.getResources().getString(R.string.fingerprint_failed));
                    mCallback.onUiFailure();
                }
            }
        });
    }

    private void showError(CharSequence error) {
        mFingerprintImg.setImageResource(R.drawable.warning_icon_alert);
        mFingerprintStatus.setText(error);
        mFingerprintStatus.setTextColor(mFingerprintStatus.getResources().getColor(R.color.sclera_alert));
        mFingerprintStatus.removeCallbacks(mResetErrorTextRunnable);
        mFingerprintStatus.postDelayed(mResetErrorTextRunnable, ERROR_TIMEOUT_MILLIS);
    }
    private void showUnrecoverableError(CharSequence error) {
        mFingerprintImg.setImageResource(R.drawable.warning_icon_alert);
        mFingerprintStatus.setText(error);
        mFingerprintStatus.setTextColor(mFingerprintStatus.getResources().getColor(R.color.sclera_alert));
        mFingerprintStatus.removeCallbacks(mResetErrorTextRunnable);
    }
    private Runnable mResetErrorTextRunnable = new Runnable() {
        @Override
        public void run() {
            mFingerprintStatus.setTextColor(mFingerprintStatus.getResources().getColor(R.color.grey_title));
            mFingerprintStatus.setText(mFingerprintStatus.getResources().getString(R.string
                    .fingerprint_touch_sensor));
            mFingerprintImg.setImageResource(R.drawable.fingerprint_icon_small);
        }
    };
    public interface Callback {
        void onUiSuccess();
        void onUiFailure();
    }
}
