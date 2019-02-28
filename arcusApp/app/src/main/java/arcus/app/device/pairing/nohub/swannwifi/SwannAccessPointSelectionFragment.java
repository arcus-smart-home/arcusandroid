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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import arcus.cornea.device.camera.model.AvailableNetworkModel;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.activities.BaseActivity;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.PairingError;
import arcus.app.common.machine.State;
import arcus.app.common.popups.PairingInProgressPopup;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.common.wifi.PhoneWifiHelper;
import arcus.app.device.pairing.nohub.swannwifi.controller.SwannProvisioningController;
import arcus.app.device.pairing.nohub.swannwifi.controller.SwannWifiPairingSequenceController;
import arcus.app.device.pairing.steps.AbstractPairingStepFragment;

import org.apache.commons.lang3.text.WordUtils;

import java.util.List;
import java.util.regex.Pattern;


public class SwannAccessPointSelectionFragment extends SequencedFragment<SwannWifiPairingSequenceController> implements PhoneWifiHelper.WifiScanCompleteListener, SwannProvisioningController.ProvisioningControllerListener {

    private final static String STEP_NUMBER_ARG = "pairing-step";

    private Version1TextView chillText;
    private Version1Button tryAgainButton;
    private ListView apList;
    private ArrayAdapter<String> apListAdapter;
    private ImageView pairingStepIcon;

    private final static Pattern apNamePattern = Pattern.compile(".*Smart.*Plug.[a-zA-Z0-9]{4}");

    public static SwannAccessPointSelectionFragment newInstance(int pairingStep) {
        SwannAccessPointSelectionFragment instance = new SwannAccessPointSelectionFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(STEP_NUMBER_ARG, pairingStep);
        instance.setArguments(arguments);
        return instance;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        chillText = (Version1TextView) view.findViewById(R.id.chill_text);
        apList = (ListView) view.findViewById(R.id.swann_plug_list);
        tryAgainButton = (Version1Button) view.findViewById(R.id.try_again_button);
        pairingStepIcon = (ImageView) view.findViewById(R.id.pairing_step);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        chillText.setVisibility(View.VISIBLE);
        apList.setVisibility(View.GONE);
        apListAdapter = new ArrayAdapter<>(getActivity(), R.layout.cell_access_point, R.id.ssid);
        apList.setAdapter(apListAdapter);

        startScanning();

        pairingStepIcon.setImageResource(getPairingStepIcon());
        tryAgainButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startScanning();
            }
        });

        apList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startPairing(apListAdapter.getItem(position));
            }
        });
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.swann_smart_plug);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_swann_access_point_selection;
    }

    @Override
    public void onWifiScanComplete(List<AvailableNetworkModel> results) {
        if (getActivity() == null) {
            return;
        }

        apListAdapter.clear();

        for (AvailableNetworkModel thisResult : results) {
            if (isSwannSmartPlugAp(thisResult.getSSID())) {
                apListAdapter.add(thisResult.getSSID());
            }
        }

        if (apListAdapter.getCount() == 0) {
            chillText.setText(R.string.swann_try_again_desc);
            tryAgainButton.setVisibility(View.VISIBLE);
        } else {
            chillText.setVisibility(View.GONE);
            apList.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onStateChange(State lastState, State currentState) {
        // Nothing to do
    }

    @Override
    public void onError(State state, Throwable e) {
        stopPairing();

        // Failed in a terminal state; show error based on cause
        if (state instanceof SwannProvisioningController.TerminalFailedState) {
            SwannProvisioningController.TerminalFailedState tfs = (SwannProvisioningController.TerminalFailedState) state;
            PairingError error;

            switch (tfs.cause) {
                case DEVICE_TAKEN:
                    getController().abortToFirstStep(getActivity());
                    error = PairingError.DEVICE_ALREADY_CLAIMED;
                    error.setErroredFieldName(WordUtils.capitalize(getString(R.string.swann_smart_plug).toLowerCase()));
                    break;

                case NOT_FOUND:
                    getController().abortToFirstStep(getActivity());
                    error = PairingError.DEVICE_INVALID_CREDENTIALS;
                    break;

                default:
                    getController().abortToFirstStep(getActivity());
                    error = PairingError.DEVICE_REQUIRES_RESET;
                    break;
            }

            ErrorManager.in(getActivity()).show(error);
        }

        // Failed in a non-terminal state... something's not right
        else {
            getController().abortToProductCatalog(getActivity());
            ErrorManager.in(getActivity()).show(PairingError.DEVICE_REQUIRES_RESET);
        }
    }

    @Override
    public void onSuccess(DeviceModel deviceModel) {
        stopPairing();
        goNext(deviceModel);
    }

    public static boolean isSwannSmartPlugAp (String ssid) {
        return apNamePattern.matcher(ssid).matches();
    }

    private void startScanning() {
        chillText.setText(R.string.swann_searching_for_ap);
        chillText.setVisibility(View.VISIBLE);
        tryAgainButton.setVisibility(View.GONE);

        PhoneWifiHelper.scanForAvailableNetworks(getActivity(), this);
    }

    private void startPairing(String swannApSsid) {
        apList.setEnabled(false);

        // Show "please be patient" sheet
        getController().setPairingInProgress(true);
        BackstackManager.getInstance().navigateToFloatingFragment(PairingInProgressPopup.newInstance(), PairingInProgressPopup.class.getSimpleName(), true);

        String targetNetworkSsid = getController().getHomeNetworkSsid();
        String targetNetworkPassword = getController().getHomeNetworkPassword();

        getController().setSwannApSsid(swannApSsid);

        ((BaseActivity) getActivity()).setNoNetworkErrorSupressed(true);
        SwannProvisioningController.provisionSmartPlug(SwannAccessPointSelectionFragment.this, swannApSsid, targetNetworkSsid, targetNetworkPassword);
    }

    private void stopPairing() {
        if (getActivity() == null) {
            return;
        }

        apList.setEnabled(true);

        // Clear the "please be patient" sheet
        BackstackManager.getInstance().navigateBack();
        getController().setPairingInProgress(false);

        ((BaseActivity) getActivity()).setNoNetworkErrorSupressed(false);
    }

    public int getPairingStepIcon() {
        return AbstractPairingStepFragment.getStepNumberDrawableResId(getArguments().getInt(STEP_NUMBER_ARG));
    }
}
