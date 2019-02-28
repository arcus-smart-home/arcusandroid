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
package arcus.app.device.pairing.nohub.swannwifi;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.utils.GlobalSetting;
import arcus.app.common.view.Version1Button;
import arcus.app.device.pairing.nohub.swannwifi.controller.SwannWifiPairingSequenceController;

import java.util.ArrayList;
import java.util.Collections;



public class SwannRequestPermissionFragment extends SequencedFragment<SwannWifiPairingSequenceController> implements BaseActivity.PermissionCallback {

    private Version1Button continueButton;

    public static SwannRequestPermissionFragment newInstance() {
        return new SwannRequestPermissionFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        continueButton = (Version1Button) view.findViewById(R.id.continue_button);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((BaseActivity) getActivity()).setPermissionCallback(SwannRequestPermissionFragment.this);
                ((BaseActivity) getActivity()).checkPermission(Collections.singletonList(Manifest.permission.ACCESS_COARSE_LOCATION), GlobalSetting.PERMISSION_ACCESS_COARSE_LOCATION, R.string.permission_rationale_location_swann);
            }
        });

        return view;
    }

    @Nullable
    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_swann_request_permission;
    }

    @Override
    public void permissionsUpdate(int permissionType, ArrayList<String> permissionsDenied, ArrayList<String> permissionsDeniedNeverAskAgain) {
        if (((BaseActivity) getActivity()).hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            getController().goNext(getActivity(), this, (Object[]) null);
        }

        if (permissionsDeniedNeverAskAgain.contains(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            ((BaseActivity) getActivity()).showSnackBarForPermissions(getString(R.string.permission_rationale_location_swann_snack));
        }
    }
}