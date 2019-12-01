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

import android.Manifest;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.activities.PermissionsActivity;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.utils.PreferenceUtils;
import arcus.app.common.view.CircularImageView;
import arcus.app.common.view.ScleraTextView;

import java.util.ArrayList;



public class FingerprintPopup extends DialogFragment implements FingerprintUiHelper.Callback, PermissionsActivity.PermissionCallback, View.OnClickListener {

    private static final String TITLE_KEY = "TITLE";
    private static final String SUBTITLE_KEY = "SUBTITLE";

    private CoordinatorLayout coordinatorLayout;

    private CircularImageView statusImg;
    private ScleraTextView promptTitle;
    private ScleraTextView promptMsg;
    private ScleraTextView statusTxt;
    private ScleraTextView cancel;

    private String title;
    private String subtitle;

    private FingerprintUiHelper mUiHelper;
    private FingerprintPopupCallback mListener;

    private FingerprintPopup fragment;

    public FingerprintPopup() {}

    public static FingerprintPopup newInstance(String title,
                                               String subtitle,
                                               @NonNull FingerprintPopupCallback listener){
        FingerprintPopup popup = new FingerprintPopup();

        Bundle bundle = new Bundle();
        bundle.putString(TITLE_KEY, title);
        bundle.putString(SUBTITLE_KEY, subtitle);

        popup.setArguments(bundle);
        popup.setListener(listener);
        return popup;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_fingerprint_prompt, container, false);

        // do not create a new fragment when activity is re-created
        setRetainInstance(true);
        setCancelable(false);

        final Bundle args = getArguments();
        if (args != null) {
            title = args.getString(TITLE_KEY, getString(R.string.fingerprint_sign_in));
            subtitle = args.getString(SUBTITLE_KEY, getString(R.string.fingerprint_instructions));
        }

        coordinatorLayout = (CoordinatorLayout) view.findViewById(R.id.coordinator_layout);

        promptTitle = (ScleraTextView) view.findViewById(R.id.prompt_title);
        promptMsg = (ScleraTextView) view.findViewById(R.id.prompt_message);
        statusImg = (CircularImageView) view.findViewById(R.id.fingerprint_status_img);
        statusTxt = (ScleraTextView) view.findViewById(R.id.fingerprint_status_txt);
        cancel = (ScleraTextView) view.findViewById(R.id.cancel_prompt);
        cancel.setOnClickListener(this);

        fragment = this;
        ((PermissionsActivity) getActivity()).setPermissionCallback(fragment);

        ArrayList<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.USE_FINGERPRINT);

        ((PermissionsActivity) getActivity()).checkPermission(permissions, GlobalSetting.PERMISSION_USE_FINGERPRINT,
                R.string.permission_rationale_fingerprint);

        mUiHelper = new FingerprintUiHelper(
                statusImg,
                statusTxt,
                this);

        mUiHelper.startSensor(getResources());

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        promptTitle.setText(title);
        promptMsg.setText(subtitle);
    }


    @Override
    public void onPause() {
        super.onPause();
        Fingerprint.cancelAuthentication();
        dismiss();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        // Work around allowStateLoss for the dialog
        // https://stackoverflow.com/questions/27580306/dismissed-dialog-fragment-reappears-again-when-app-is-resumed
    }

    /**
     * UI helper callbacks and what happens when the user clicks cancel
     */
    @Override
    public void onUiSuccess() {
        if(mListener != null){
            mListener.successfullyAuthenticated();

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 2000);
        }
    }

    @Override
    public void onUiFailure() {
        if (mListener != null) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    // Opt out of fingerprint
                    PreferenceUtils.setUseFingerPrint(false);
                    mListener.failedAuthentication();
                    dismiss();
                }
            }, 2000);
        }
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch(id) {
            case R.id.cancel_prompt:

                if (mListener != null) {
                    mListener.onCanceled();
                }
                dismiss();
        }
    }

    @Override
    public void permissionsUpdate(int permissionType, ArrayList<String> permissionsDenied, ArrayList<String> permissionsDeniedNeverAskAgain) {

        String message;

        // Check for fingerprint permission (even though it's not dangerous...)
        if(permissionsDenied.contains(Manifest.permission.USE_FINGERPRINT)) {
            message = getActivity().getString(R.string.permission_rationale_fingerprint);
            ((PermissionsActivity)getActivity()).showSnackBarForPermissions(message);
        }
        if (permissionsDeniedNeverAskAgain.contains(Manifest.permission.USE_FINGERPRINT)) {
            message = getActivity().getString(R.string.permission_denied_forever_fingerprint);
            ((PermissionsActivity)getActivity()).showSnackBarForPermissions(message);
        }
    }

    /**
     * For the caller
     */
    private void setListener(FingerprintPopupCallback listener) {
        mListener = listener;
    }

    public interface FingerprintPopupCallback {
        void successfullyAuthenticated();
        void failedAuthentication();
        void onCanceled();
    }
}
