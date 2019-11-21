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
package arcus.app.common.controller;

import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import com.iris.client.ClientEvent;
import com.iris.client.ClientMessage;
import com.iris.client.ClientRequest;
import com.iris.client.IrisClient;
import com.iris.client.capability.Hub;
import com.iris.client.capability.HubAdvanced;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.Store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class GenericDeviceRemovalController {
    public interface Callback {
        /**
         * Callback to denote that removal has started on the hub.
         */
        void removalStarted();

        /**
         * Callback to denote a device was removed
         *
         * @param name name of the device removed
         */
        void deviceRemoved(String name);

        /**
         * Callback to denote that removal has stopped
         *
         * @param devices number of devices removed
         */
        void removalStopped(int devices);

        /**
         * Callback to denote an error has happend when trying to send the request.
         *
         * @param throwable
         */
        void showError(Throwable throwable);
    }

    private static final Logger logger = LoggerFactory.getLogger(GenericDeviceRemovalController.class);
    public  static final String UNPAIR_CRITICAL_ERROR = "UNPAIR_CRITICAL_ERROR";

    private static final int  REQUEST_TIMEOUT           = 30 * 1000; // Overall Request Timeout
    private static final long ZWAVE_AND_OTHER_TIMEOUT   = 60 * 1000 * 2; // 2 Minutes

    private static final GenericDeviceRemovalController INSTANCE;
    private final IrisClient client;

    private final AtomicInteger devicesRemoved = new AtomicInteger(0);
    private WeakReference<Callback> callbackRef = new WeakReference<>(null);
    private ListenerRegistration devicesRemovedRegistration;
    private ListenerRegistration removedDeviceEventMessageReg;
    private final Listener<ModelDeletedEvent> deviceDeletedEvent  = Listeners.runOnUiThread(new Listener<ModelDeletedEvent>() {
        @Override
        public void onEvent(ModelDeletedEvent modelDeletedEvent) {
            if (modelDeletedEvent.getModel() instanceof DeviceModel) {
                devicesRemoved.incrementAndGet();
                DeviceModel model = (DeviceModel) modelDeletedEvent.getModel();
                showDeviceRemoved(String.valueOf(model.getName()));
            }
        }
    });
    private final Listener<ClientEvent> unpairingStarted = Listeners.runOnUiThread(new Listener<ClientEvent>() {
        @Override
        public void onEvent(ClientEvent clientEvent) {
            showRemovalStarted();
            devicesRemovedRegistration = CorneaClientFactory.getStore(DeviceModel.class).addListener(ModelDeletedEvent.class, deviceDeletedEvent);
            removedDeviceEventMessageReg = CorneaClientFactory.getClient().addMessageListener(removedMessageListener);

            // Come back in 2 minutes and showDevicesRemoved - may be 0 if the user did not hit cancel and
            // no devices were deleted (or we missed the messages)
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Listeners.clear(devicesRemovedRegistration);
                    Listeners.clear(removedDeviceEventMessageReg);
                    showDevicesRemoved();
                }
            }, ZWAVE_AND_OTHER_TIMEOUT);
        }
    });
    private final Listener<ClientEvent> unpairingStopped = Listeners.runOnUiThread(new Listener<ClientEvent>() {
        @Override
        public void onEvent(ClientEvent clientEvent) {
            Listeners.clear(devicesRemovedRegistration);
            Listeners.clear(removedDeviceEventMessageReg);
            showDevicesRemoved();
        }
    });
    private final Listener<Throwable> failedListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            showError(throwable);
        }
    });
    private final Listener<ClientMessage> removedMessageListener = new Listener<ClientMessage>() {
        @Override
        public void onEvent(ClientMessage clientMessage) {
            if (clientMessage == null || clientMessage.getEvent() == null) {
                return;
            }

            if (HubAdvanced.UnpairedDeviceRemovedEvent.NAME.equals(clientMessage.getEvent().getType())) {
                HubAdvanced.UnpairedDeviceRemovedEvent event = new HubAdvanced.UnpairedDeviceRemovedEvent(clientMessage.getEvent());
                final String deviceName = String.valueOf(event.getDevTypeGuess());
                devicesRemoved.incrementAndGet();

                LooperExecutor.getMainExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        showDeviceRemoved(deviceName);
                    }
                });
            }
        }
    };

    static {
        INSTANCE = new GenericDeviceRemovalController();
    }

    public static GenericDeviceRemovalController instance() {
        return INSTANCE;
    }

    GenericDeviceRemovalController() {
        this(CorneaClientFactory.getClient());
    }

    GenericDeviceRemovalController(IrisClient client) {
        Preconditions.checkNotNull(client);
        this.client = client;
    }

    public ListenerRegistration setCallback(Callback callback) {
        callbackRef = new WeakReference<>(callback);

        return new ListenerRegistration() {
            @Override
            public boolean isRegistered() {
                return GenericDeviceRemovalController.this.callbackRef.get() != null;
            }

            @Override
            public boolean remove() {
                return doClear();
            }
        };
    }

    public void startRemovingDevices() {
        devicesRemoved.set(0);
        continueRemovingDevices();
    }

    public void continueRemovingDevices() {
        String hubAddress = getHubAddress();
        if (Strings.isNullOrEmpty(hubAddress)) {
            showError(hubAddressNotFound());
            return;
        }

        Hub.UnpairingRequestRequest unpairingStartRequest = new Hub.UnpairingRequestRequest();
        unpairingStartRequest.setAddress(hubAddress);
        unpairingStartRequest.setTimeout(ZWAVE_AND_OTHER_TIMEOUT);
        unpairingStartRequest.setTimeoutMs(REQUEST_TIMEOUT);
        unpairingStartRequest.setActionType(Hub.UnpairingRequestRequest.ACTIONTYPE_START_UNPAIRING);
        unpairingStartRequest.setForce(false);

        client.request(unpairingStartRequest)
              .onSuccess(unpairingStarted)
              .onFailure(failedListener);
    }

    public void cancelRemoveDevice() {
        String hubAddress = getHubAddress();
        if (Strings.isNullOrEmpty(hubAddress)) {
            showError(hubAddressNotFound());
            return;
        }

        client.request(getStopRequest(hubAddress))
              .onSuccess(unpairingStopped)
              .onFailure(failedListener);
    }

    public int getTotalRemovedDevices() {
        return devicesRemoved.get();
    }

    protected boolean doClear() {
        Listeners.clear(devicesRemovedRegistration);
        Listeners.clear(removedDeviceEventMessageReg);
        callbackRef = new WeakReference<>(null);

        String hubAddress = getHubAddress();
        if (Strings.isNullOrEmpty(hubAddress)) {
            showError(hubAddressNotFound());
            return false;
        }

        client.request(getStopRequest(hubAddress));

        return true;
    }

    protected ClientRequest getStopRequest(String hubAddress) {
        Hub.UnpairingRequestRequest stopUnpairingRequest = new Hub.UnpairingRequestRequest();
        stopUnpairingRequest.setAddress(hubAddress);
        stopUnpairingRequest.setTimeoutMs(REQUEST_TIMEOUT);
        stopUnpairingRequest.setActionType(Hub.UnpairingRequestRequest.ACTIONTYPE_STOP_UNPAIRING);

        return stopUnpairingRequest;
    }

    private Throwable hubAddressNotFound() {
        return new ErrorResponseException(UNPAIR_CRITICAL_ERROR, "Unable to determine hub address to start unpairing.");
    }

    protected @Nullable Callback getCallback() {
        if (callbackRef != null) {
            return callbackRef.get();
        }

        return null;
    }

    protected void showRemovalStarted() {
        Callback callback = getCallback();

        if (callback != null) {
            callback.removalStarted();
        }
    }

    protected void showDeviceRemoved(@NonNull String name) {
        Callback callback = getCallback();

        if (callback != null) {
            callback.deviceRemoved(name);
        }
    }

    protected void showDevicesRemoved() {
        Callback callback = getCallback();

        if (callback != null) {
            callback.removalStopped(devicesRemoved.get());
        }
    }

    protected void showError(Throwable throwable) {
        Callback callback = getCallback();

        if (callback != null) {
            callback.showError(throwable);
        }
    }

    @Nullable
    protected String getHubAddress() {
        if (!CorneaClientFactory.isConnected()) {
            return null;
        }

        UUID placeID = CorneaClientFactory.getClient().getActivePlace();
        if (placeID == null) {
            return null;
        }

        String placeString = placeID.toString();
        Store<HubModel> hubModelStore = CorneaClientFactory.getStore(HubModel.class);
        for (HubModel hubModel : hubModelStore.values()) {
            if (placeString.equals(hubModel.getPlace())) {
                return hubModel.getAddress();
            }
        }

        return null;
    }
}
