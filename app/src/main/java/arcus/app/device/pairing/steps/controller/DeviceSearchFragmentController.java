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
package arcus.app.device.pairing.steps.controller;

import android.app.Activity;
import androidx.annotation.NonNull;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.HubModelProvider;
import com.iris.client.ClientEvent;
import com.iris.client.ClientMessage;
import com.iris.client.ClientRequest;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Hub;
import com.iris.client.event.Listener;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import arcus.app.common.controller.FragmentController;
import arcus.app.device.pairing.steps.model.DevicePairedListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Map;


public class DeviceSearchFragmentController extends FragmentController<DeviceSearchFragmentController.Callbacks> {

    private static final String CMD_REGISTER_DEVICE = "bridgesvc:RegisterDevice";
    private static final String ADDR_IPCD_BRIDGE = "BRDG::IPCD";
    private static final String ERR_DEV_NOT_FOUND = "request.destination.notfound";
    private static final String ERR_DEV_ALREADY_CLAIMED = "request.invalid";


    private static final Logger logger = LoggerFactory.getLogger(DeviceSearchFragmentController.class);
    private static final DeviceSearchFragmentController instance = new DeviceSearchFragmentController();

    private Activity activity;
    private DeviceSearchFragmentController () {}

    public interface Callbacks extends DevicePairedListener {
        void onCorneaError(Throwable cause);
        void onDeviceAlreadyClaimed();
        void onDeviceNotFound();
    }

    public static DeviceSearchFragmentController instance () {
        return instance;
    }

    public void registerIpcdDevice (final Activity activity, final Map<String,Object> requestAttributes) {
        this.activity = activity;

        ClientRequest request = new ClientRequest();
        request.setCommand(CMD_REGISTER_DEVICE);
        request.setAddress(ADDR_IPCD_BRIDGE);
        request.setAttributes(requestAttributes);
        request.setRestfulRequest(false);

        // Request to register the device...
        CorneaClientFactory.getClient().request(request).onSuccess(new Listener<ClientEvent>() {
            @Override
            public void onEvent(ClientEvent clientEvent) {
                // Registration request received okay; now wait for device model to be added
                searchForDevice(activity);
            }
        }).onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                if (throwable instanceof ErrorResponseException) {

                    // Device not found; either device has not connected to the platform, or user mistyped registration code
                    if (((ErrorResponseException) throwable).getCode().equals(ERR_DEV_NOT_FOUND)) {
                        fireOnDeviceNotFound();
                    }

                    // Device found, but already registered to another account
                    else if (((ErrorResponseException) throwable).getCode().equals(ERR_DEV_ALREADY_CLAIMED)) {
                        fireOnDeviceAlreadyClaimed();
                    }

                    else {
                        fireOnCorneaError(throwable);
                    }
                }

                else {
                    fireOnCorneaError(throwable);
                }
            }
        });
    }

    public void searchForDevice (Activity activity) {
        this.activity = activity;

        CorneaClientFactory.getClient().addMessageListener(new Listener<ClientMessage>() {
            @Override
            public void onEvent(@NonNull ClientMessage clientMessage) {
                if (clientMessage.getEvent() instanceof Capability.AddedEvent) {
                    final String deviceAddress = String.valueOf(clientMessage.getEvent().getAttribute(Capability.ATTR_ADDRESS));
                    DeviceModelProvider.instance().getModel(deviceAddress).load().onSuccess(new Listener<DeviceModel>() {
                        @Override
                        public void onEvent(DeviceModel deviceModel) {
                            fireOnDeviceFound(deviceModel);
                        }
                    }).onFailure(new Listener<Throwable>() {
                        @Override
                        public void onEvent(Throwable throwable) {
                            logger.error("Failed to load device model for newly added device {}. Something ain't right.", deviceAddress);
                        }
                    });
                }
            }
        });

        // Listen for changes to the hub pairing state
        HubModel activeHub = HubModelProvider.instance().getHubModel();
        if (activeHub != null) {
            activeHub.addPropertyChangeListener(new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent event) {
                    if (Hub.ATTR_STATE.equals(event.getPropertyName()) && Hub.STATE_NORMAL.equals(event.getNewValue())) {
                        fireOnHubPairingTimeout();
                    }
                }
            });
        }
    }

    private void fireOnCorneaError (final Throwable cause) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onCorneaError(cause);
                    }
                }
            });
        }
    }

    private void fireOnDeviceNotFound () {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onDeviceNotFound();
                    }
                }
            });
        }
    }

    private void fireOnDeviceAlreadyClaimed () {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Callbacks listener = getListener();
                    if (listener != null) {
                        listener.onDeviceAlreadyClaimed();
                    }
                }
            });
        }
    }

    private void fireOnDeviceFound (final DeviceModel deviceModel) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DevicePairedListener listener = getListener();
                    if (listener != null) {
                        listener.onDeviceFound(deviceModel);
                    }
                }
            });
        }
    }

    private void fireOnHubPairingTimeout () {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DevicePairedListener listener = getListener();
                    if (listener != null) {
                        listener.onHubPairingTimeout();
                    }
                }
            });
        }
    }
}
