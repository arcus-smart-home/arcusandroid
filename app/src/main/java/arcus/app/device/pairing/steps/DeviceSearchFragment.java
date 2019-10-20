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
package arcus.app.device.pairing.steps;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.listener.DismissListener;
import arcus.app.common.error.type.PairingError;
import arcus.app.device.pairing.steps.controller.DeviceSearchFragmentController;

import java.util.HashMap;

public class DeviceSearchFragment extends AbstractPairingStepFragment implements DeviceSearchFragmentController.Callbacks {

    private static final String DEVICE_NAME = "DEVICE_NAME";
    private static final String IPCD_REQ_ATTRS = "IPCD_ATTRS";
    private static final String INPUT_FIELD_NAME = "INPUT_FIELD";

    private TextView deviceName;
    private HashMap<String,Object> ipcdAttributes;

    public static DeviceSearchFragment newInstance(String deviceName, HashMap<String,Object> ipcdRegistrationRequestAttributes, String inputFieldName) {
        DeviceSearchFragment instance = new DeviceSearchFragment();
        Bundle arguments = new Bundle();
        arguments.putString(DEVICE_NAME, deviceName);
        arguments.putSerializable(IPCD_REQ_ATTRS, ipcdRegistrationRequestAttributes);
        arguments.putString(INPUT_FIELD_NAME, inputFieldName);
        instance.setArguments(arguments);

        return instance;
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        deviceName = ((TextView) view.findViewById(R.id.device_searching_name));
        return view;
    }

    @Override
    public void onResume () {
        super.onResume();
        getActivity().invalidateOptionsMenu();

        String deviceNameString = getArguments().getString(DEVICE_NAME);
        ipcdAttributes = (HashMap<String,Object>) getArguments().getSerializable(IPCD_REQ_ATTRS);

        deviceName.setText(getResources().getString(R.string.device_searching_text, deviceNameString));

        DeviceSearchFragmentController.instance().setListener(this);
        if (ipcdAttributes == null) {
            // Searching for zigbee/zwave device
            DeviceSearchFragmentController.instance().searchForDevice(getActivity());
        } else {
            // Searching for ipcd device
            DeviceSearchFragmentController.instance().registerIpcdDevice(getActivity(), ipcdAttributes);
        }
    }

    @Override
    public void onPause () {
        super.onPause();
        DeviceSearchFragmentController.instance().removeListener();
    }

    @NonNull
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_searching_device;
    }

    @Override
    public void onCorneaError(Throwable cause) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(cause);
    }

    @Override
    public void onDeviceAlreadyClaimed() {
        PairingError error = PairingError.DEVICE_ALREADY_CLAIMED;
        error.setErroredFieldName(getArguments().getString(INPUT_FIELD_NAME));
        ErrorManager.in(getActivity()).show(error);
    }

    @Override
    public void onDeviceNotFound() {
        PairingError error = PairingError.DEVICE_NOT_FOUND;
        error.setErroredFieldName(getArguments().getString(INPUT_FIELD_NAME));
        ErrorManager.in(getActivity()).withDialogDismissedListener(new DismissListener() {
            @Override
            public void dialogDismissedByReject() {

            }

            @Override
            public void dialogDismissedByAccept() {
                // Pressed X, Move Back after the dialog has a chance to be dismissed
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        goBack(getActivity(), this, null);
                    }
                }, 1000);

            }
        }).show(error);
    }
}

