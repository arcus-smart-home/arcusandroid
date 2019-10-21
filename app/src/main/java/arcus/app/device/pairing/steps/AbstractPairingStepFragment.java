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

import android.os.Handler;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.listener.DismissListener;
import arcus.app.common.error.type.DeviceErrorType;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.device.model.DeviceType;
import arcus.app.device.pairing.DevicePairedPopup;
import arcus.app.device.pairing.catalog.controller.ProductCatalogFragmentController;
import arcus.app.device.pairing.steps.controller.DevicePairingStepsSequenceController;
import arcus.app.device.pairing.steps.model.DevicePairedListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public abstract class AbstractPairingStepFragment extends SequencedFragment<DevicePairingStepsSequenceController> implements DevicePairedListener {

    protected final static Logger logger = LoggerFactory.getLogger(AbstractPairingStepFragment.class);

    private Map<String, String> devicesFound = new HashMap<>();
    private boolean timeoutMessageVisible = false;
    private boolean waitingForChildDevices = false;

    private void promptSingleDevicePaired(final String deviceName, final String deviceAddress) {
        final String title = (deviceName != null ? String.valueOf(deviceName).toUpperCase() : "") + " " + getActivity().getString(R.string.device_paired_title);
        final String description = getActivity().getString(R.string.device_paired_desc);
        final String button = getActivity().getString(R.string.device_paired_btn);

        DevicePairedPopup singleDeviceFloatingFragment = DevicePairedPopup.newInstance(title, description, button,
                new DevicePairedPopup.InfoButtonCallback() {
                    @Override
                    public void infoButtonClicked() {
                        BackstackManager.getInstance().navigateBack();      // Close the floating fragment
                        getController().goSinglePairingSequence(getActivity(), deviceName, deviceAddress);
                    }
                });

        BackstackManager.getInstance().navigateToFloatingFragment(singleDeviceFloatingFragment, singleDeviceFloatingFragment.getClass().getName(), true);
    }

    private void promptMultipleDevicesPaired() {
        DevicePairedPopup multiDeviceFloatingFragment = DevicePairedPopup.newInstance(getActivity().getString(R.string.devices_paired_title),
                getActivity().getString(R.string.devices_paired_desc),
                getActivity().getString(R.string.devices_paired_btn),
                new DevicePairedPopup.InfoButtonCallback() {
                    @Override
                    public void infoButtonClicked() {
                        BackstackManager.getInstance().navigateBack();      // Close the floating fragment
                        getController().goMultipairingSequence(getActivity(), new ArrayList<>(devicesFound.keySet()));
                    }
                });

        BackstackManager.getInstance().navigateToFloatingFragment(multiDeviceFloatingFragment, multiDeviceFloatingFragment.getClass().getName(), true);
    }

    @Override
    public void onDeviceFound(final DeviceModel deviceModel) {

        String expectedDevTypeHint = getController().getProductDevTypeHint();

        // Ignore devices that have already been found/added
        if (!devicesFound.containsKey(deviceModel.getAddress())) {

            dismissFloatingFragments();

            // Keep track of newly added devices
            devicesFound.put(deviceModel.getAddress(), deviceModel.getName());

            // If the new device is what we expected (i.e., type of device user is searching for), then jump ahead...
            if (deviceModel.getDevtypehint() != null && deviceModel.getDevtypehint().equals(expectedDevTypeHint)) {

                logger.debug("New device found of type: {}", deviceModel.getDevtypehint());

                // Special case: device is bridge device and additional "child" devices should follow along behind it
                if (shouldWaitForChildDevices(deviceModel)) {
                    logger.debug("Device expects child devices to also pair; waiting for children.");

                    waitingForChildDevices = true;

                    // Wait 15 sec for child device
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            logger.debug("Timeout expired waiting for child devices. Total devices paired: {}", devicesFound.size());

                            waitingForChildDevices = false;
                            dismissFloatingFragments();

                            if (devicesFound != null && devicesFound.size() == 1) {
                                getController().goSinglePairingSequence(getActivity(), deviceModel.getName(), deviceModel.getAddress());
                            } else {
                                getController().goMultipairingSequence(getActivity(), new ArrayList<>(devicesFound.keySet()));
                            }
                        }
                    }, 15000);
                }

                // Single device found: go to name/photo fragment
                else if (devicesFound != null && devicesFound.size() == 1) {
                    getController().goSinglePairingSequence(getActivity(), deviceModel.getName(), deviceModel.getAddress());
                }

                // Multiple devices found: Go to list of devices fragment
                else {
                    getController().goMultipairingSequence(getActivity(), new ArrayList<>(devicesFound.keySet()));
                }
            }

            // Otherwise if device is not type user was browsing for, then display a "saved time" prompt unless we're waiting for child devices
            else if (!waitingForChildDevices && devicesFound != null && (!DeviceType.fromHint(deviceModel.getDevtypehint()).equals(DeviceType.GENIE_GARAGE_DOOR_CONTROLLER))) {

                // Only one new device found
                if (devicesFound.size() <= 1) {
                    promptSingleDevicePaired(deviceModel.getName(), deviceModel.getAddress());
                }

                // Multiple new devices found
                else {
                    promptMultipleDevicesPaired();
                }
            }
        }
    }

    @Override
    public void onHubPairingTimeout() {
        if (timeoutMessageVisible) {
            return;
        }

        timeoutMessageVisible = true;
        ErrorManager.in(getActivity()).withDialogDismissedListener(new DismissListener() {
            @Override
            public void dialogDismissedByReject() {
                timeoutMessageVisible = false;
                endSequence(false);
            }

            @Override
            public void dialogDismissedByAccept() {
                timeoutMessageVisible = false;
                ProductCatalogFragmentController.instance().startPairing();
            }

        }).show(DeviceErrorType.PAIRING_MODE_TIMEOUT);
    }

    private boolean shouldWaitForChildDevices(DeviceModel model) {
        return model != null && DeviceType.fromHint(model.getDevtypehint()) == DeviceType.GENIE_GARAGE_DOOR_CONTROLLER;
    }

    private void dismissFloatingFragments() {
        // If there's a floating fragment displayed close it
        if (BackstackManager.getInstance().getCurrentFragment() instanceof ArcusFloatingFragment) {
            BackstackManager.getInstance().navigateBack();      // Close the floating fragment
        }
    }

    public static int getStepNumberDrawableResId(int stepNumber) {
        switch (stepNumber) {
            case 0:
                return R.drawable.step_01;
            case 1:
                return R.drawable.step_02;
            case 2:
                return R.drawable.step_03;
            case 3:
                return R.drawable.step_04;
            case 4:
                return R.drawable.step_05;
            case 5:
                return R.drawable.step_06;
            case 6:
                return R.drawable.step_07;
            case 7:
                return R.drawable.step_08;
            case 8:
                return R.drawable.step_09;
            default:
                throw new IllegalArgumentException("Bug! No pairing step number icon for step " + stepNumber);
        }
    }
}
