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
package arcus.app.device.removal.zwave;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.controller.GenericDeviceRemovalController;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.popups.AlertPopup;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1ButtonColor;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.removal.zwave.controller.ZWaveUnpairingSequenceController;


public class ZWaveUnpairingSearchFragment extends SequencedFragment<ZWaveUnpairingSequenceController> implements GenericDeviceRemovalController.Callback {

    private GenericDeviceRemovalController controller;
    private ListenerRegistration callback;

    private Version1TextView title;
    private Version1TextView deviceList;

    @NonNull
    public static ZWaveUnpairingSearchFragment newInstance() {
        return new ZWaveUnpairingSearchFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        title = (Version1TextView) view.findViewById(R.id.title);

        Version1Button button = (Version1Button) view.findViewById(R.id.done);

        if (button != null) {
            button.setColorScheme(Version1ButtonColor.WHITE);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    endSequence(true);
                }
            });
        }

        deviceList = (Version1TextView) view.findViewById(R.id.zwave_device_list);

        return view;
    }

    @Override
    public void onResume() {

        if (controller == null) {
            controller = GenericDeviceRemovalController.instance();
        }

        callback = controller.setCallback(this);

        controller.startRemovingDevices();

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Listeners.clear(callback); // Clearing the callback also makes the final "Stop Unpairing" call
    }

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.zwave_searching_header);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_zwave_unpairing_search;
    }

    @Override
    public void removalStarted() {

    }

    @Override
    public void deviceRemoved(String name) {
        if (title != null) {
            int devices = GenericDeviceRemovalController.instance().getTotalRemovedDevices();
            title.setText(getString(R.string.zwave_device_reset_start) + devices + " " + getString(R.string.zwave_devices_reset));
        }

        if (deviceList != null) {
            String devices = deviceList.getText().toString() + "\n" + name;
            deviceList.setText(devices);
        }
    }

    @Override
    public void removalStopped(int devices) {
        showTimeoutDialog();
    }

    @Override
    public void showError(Throwable throwable) {
        BackstackManager.getInstance().navigateBack();
        ErrorManager.in(getActivity()).showGenericBecauseOf(throwable);
    }

    public void showTimeoutDialog() {
        String timeoutString = getString(R.string.zwave_device_not_removed_timeout);

        if (controller.getTotalRemovedDevices() == 0) timeoutString = getString(R.string.zwave_device_not_removed) + " " + timeoutString;

        AlertPopup popup = AlertPopup.newInstance(getString(R.string.zwave_timeout),
                timeoutString, getString(R.string.device_remove_yes), getString(R.string.device_remove_no), AlertPopup.ColorStyle.PINK,
                new AlertPopup.AlertButtonCallback() {
                    @Override
                    public boolean topAlertButtonClicked() {
                        controller.continueRemovingDevices();
                        return true;
                    }

                    @Override
                    public boolean bottomAlertButtonClicked() {
                        goBack();
                        return false;
                    }

                    @Override
                    public boolean errorButtonClicked() {
                        return false;
                    }

                    @Override
                    public void close() {

                    }
                });

        showPopup(popup);

    }

    public <T extends ArcusFloatingFragment> void showPopup(@NonNull T popup) {
        BackstackManager.getInstance().navigateToFloatingFragment(popup, popup.getClass().getSimpleName(), true);
    }
}
