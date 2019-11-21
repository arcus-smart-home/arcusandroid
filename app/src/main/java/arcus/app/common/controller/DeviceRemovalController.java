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
import arcus.cornea.utils.AddressableModelSource;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ProtocolTypes;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.IrisClient;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.capability.DeviceConnection;
import com.iris.client.capability.Hub;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.Store;
import com.iris.client.util.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceRemovalController {
    private static final Logger logger = LoggerFactory.getLogger(DeviceRemovalController.class);
    private static final int  REQUEST_TIMEOUT           = 30 * 1000; // Overall Request Timeout
    private static final long ZIGBEE_TIMEOUT            = 60 * 1000;
    private static final long ZWAVE_AND_OTHER_TIMEOUT   = ZIGBEE_TIMEOUT * 5;
    @NonNull
    private static final DeviceRemovalController INSTANCE;

    private final IrisClient client;

    private AddressableModelSource<DeviceModel> deviceModel = CachedModelSource.newSource();
    @Nullable
    private WeakReference<Callback> callbackRef;
    @Nullable
    private RemovalType removalType;
    private DeviceType deviceType;
    @NonNull
    private AtomicBoolean unpairingSuccess = new AtomicBoolean(false);

    private Listener<Throwable> removalFailureListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            logger.debug("Received error during removal process", throwable);
            callUnpairingFailed();
        }
    });

    private Listener<ClientEvent> successListener = Listeners.runOnUiThread(new Listener<ClientEvent>() {
        @Override
        public void onEvent(ClientEvent throwable) {
            callUnpairingSuccess();
        }
    });

    private Listener<ModelDeletedEvent> modelDeletedEventListener = Listeners.runOnUiThread(new Listener<ModelDeletedEvent>() {
        @Override
        public void onEvent(ModelDeletedEvent mde) {
            unpairingSuccess.set(true);
            callUnpairingSuccess();
        }
    });

    private Listener<DeviceModel> modelLoadedListener = Listeners.runOnUiThread(new Listener<DeviceModel>() {
        @Override
        public void onEvent(DeviceModel model) {
            startRemoval();
        }
    });

    private Listener<Result<ClientEvent>> cancelUnpairingRequest = Listeners.runOnUiThread(new Listener<Result<ClientEvent>>() {
        @Override
        public void onEvent(Result<ClientEvent> clientEventResult) {
            callUnpairingFailed();
        }
    });

    static {
        INSTANCE = new DeviceRemovalController(CorneaClientFactory.getClient());
    }

    DeviceRemovalController(IrisClient client) {
        Preconditions.checkNotNull(client);
        this.client = client;
    }

    @NonNull
    public static DeviceRemovalController instance() {
        return INSTANCE;
    }

    public void remove(String address, Callback callback) {
        reset();
        deviceModel.setAddress(address);
        removalType = RemovalType.NORMAL;

        setCallback(callback);
        checkModel();
    }

    public void forceRemove(String address, Callback callback) {
        reset();
        deviceModel.setAddress(address);
        removalType = RemovalType.FORCE;

        setCallback(callback);
        checkModel();
    }

    public void cancelRemove(Callback callback) {
        setCallback(callback);

        String hubAddress = getHubAddress();
        if (Strings.isNullOrEmpty(hubAddress)) {
            callUnpairingFailed();
            return;
        }

        Hub.UnpairingRequestRequest unpairingRequest = new Hub.UnpairingRequestRequest();
        unpairingRequest.setAddress(hubAddress);
        unpairingRequest.setTimeout(0L); // Upairing timeout
        unpairingRequest.setTimeoutMs(REQUEST_TIMEOUT);
        unpairingRequest.setActionType(Hub.UnpairingRequestRequest.ACTIONTYPE_STOP_UNPAIRING);

        client
              .request(unpairingRequest)
              .onCompletion(cancelUnpairingRequest);
    }

    protected void reset() {
        deviceModel = CachedModelSource.newSource();
        callbackRef = new WeakReference<>(null);
        deviceType = DeviceType.OTHER;
        removalType = null;
    }

    public void setCallback(Callback callback) {
        if (callbackRef != null && callbackRef.get() != null) {
            logger.debug("Replacing callbacks with: [{}]", callback);
        }

        callbackRef = new WeakReference<>(callback);
    }

    @Nullable
    protected String getHubAddress() {
        DeviceModel model = deviceModel.get();
        if (model == null || Strings.isNullOrEmpty(model.getPlace())) {
            return null;
        }

        Store<HubModel> hubModelStore = CorneaClientFactory.getStore(HubModel.class);
        for (HubModel hubModel : hubModelStore.values()) {
            if (model.getPlace().equals(hubModel.getPlace())) {
                return hubModel.getAddress();
            }
        }

        return null;
    }

    protected void checkModel() {
        if (deviceModel.get() != null && deviceModel.isLoaded()) {
            startRemoval();
        }
        else {
            callLoading();
            deviceModel
                  .reload()
                  .onSuccess(modelLoadedListener)
                  .onFailure(removalFailureListener);
        }
    }

    protected void startRemoval() {
        if (RemovalType.FORCE.equals(removalType)) {
            setDeviceType();
            doForceRemove();
        }
        else {
            if (deviceTypeUnknown()) {
                callUnpairingFailed();
            }
            else {
                doRemove();
            }
        }
    }

    protected boolean deviceTypeUnknown() {
        if (!deviceCaps().contains(DeviceAdvanced.NAMESPACE)) {
            return true;
        }

        setDeviceType();

        return DeviceType.OTHER.equals(deviceType);
    }

    protected void setDeviceType() {
        if (!deviceCaps().contains(DeviceAdvanced.NAMESPACE)) {
            return;
        }

        DeviceAdvanced deviceAdvanced = (DeviceAdvanced) deviceModel.get();
        deviceType = DeviceType.fromProtocol(deviceAdvanced.getProtocol());
    }

    protected void doRemove() {
        if (deviceCaps().contains(DeviceConnection.NAMESPACE)) {
            if (deviceOnline()) {
                callDeviceIsUnpairing();

                Device.RemoveRequest removeRequest = new Device.RemoveRequest();
                removeRequest.setRestfulRequest(false);
                removeRequest.setAddress(deviceModel.getAddress());
                removeRequest.setTimeoutMs(REQUEST_TIMEOUT);
                removeRequest.setTimeout(DeviceType.ZIGBEE.equals(deviceType) ? ZIGBEE_TIMEOUT : ZWAVE_AND_OTHER_TIMEOUT);

                // hook up a device unpairing "removed" event handler.
                if (DeviceType.ZWAVE.equals(deviceType) || DeviceType.CAMERA.equals(deviceType)) {

                    unpairingSuccess.set(false);
                    callPlatformWithoutSuccessHandler(removeRequest);
                    final ListenerRegistration failedListener = deviceModel.addModelListener(modelDeletedEventListener, ModelDeletedEvent.class);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            if (!unpairingSuccess.get()) {
                                if (failedListener != null && failedListener.isRegistered()) {
                                    failedListener.remove();
                                }

                                callUnpairingFailed();
                            }
                        }
                    }, ZWAVE_AND_OTHER_TIMEOUT);
                }
                else {
                    callPlatform(removeRequest);
                }
            }
            else {
                callDeviceOffline();
            }

        }
        else {
            logger.debug("Unable to determine the online/offline status of the device. Marking failure.");
            callUnpairingFailed();
        }
    }

    protected void doForceRemove() {
        callDeviceIsUnpairing();

        Device.ForceRemoveRequest removeRequest = new Device.ForceRemoveRequest();
        removeRequest.setRestfulRequest(false);
        removeRequest.setAddress(deviceModel.getAddress());
        removeRequest.setTimeoutMs(REQUEST_TIMEOUT);

        callPlatform(removeRequest);
    }

    protected void callPlatformWithoutSuccessHandler(ClientRequest request) {
        client
              .request(request)
              .onFailure(removalFailureListener);
    }

    protected void callPlatform(ClientRequest request) {
        client
              .request(request)
              .onSuccess(successListener)
              .onFailure(removalFailureListener);
    }

    protected boolean deviceOnline() {
        return DeviceConnection.STATE_ONLINE.equals(deviceModel.get().get(DeviceConnection.ATTR_STATE));
    }

    @NonNull
    protected Set<String> deviceCaps() {
        DeviceModel model = deviceModel.get();
        Set<String> caps = new HashSet<>();

        if (model.getCaps() == null) {
            logger.error("Device caps is null - Is this even possible?");
            return caps;
        }

        caps.addAll(model.getCaps());
        return caps;
    }

    protected void callDeviceIsUnpairing() {
        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.deviceUnpairing(deviceType, removalType);
        }
    }

    protected void callDeviceOffline() {
        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.deviceOffline(deviceType, removalType);
        }
    }

    protected void callUnpairingSuccess() {
        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.unpairingSuccess(deviceType, removalType);
        }
    }

    protected void callUnpairingFailed() {
        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.unpairingFailure(deviceType, removalType);
        }
    }

    protected void callLoading() {
        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.showLoading();
        }
    }

    public enum DeviceType {
        ZIGBEE,
        ZWAVE,
        IPCD,
        CAMERA,
        MOCK,
        OTHER;

        @NonNull
        public static DeviceType fromProtocol(@NonNull String protocol) {
            switch (protocol) {
                case ProtocolTypes.MOCK:
                    return MOCK;
                case ProtocolTypes.SCOM:
                    return CAMERA;
                case ProtocolTypes.ZWAVE:
                    return ZWAVE;
                case ProtocolTypes.ZIGBEE:
                case ProtocolTypes.PHUE:
                    return ZIGBEE;
                case ProtocolTypes.LUTRON:
                    return ZIGBEE;
                case ProtocolTypes.HONEYWELL:
                case ProtocolTypes.IPCD:
                case ProtocolTypes.NEST:
                    return IPCD;
                default:
                    logger.debug("Unsure about protocol value: [{}], Assigning 'OTHER'", protocol);
                    return OTHER;
            }
        }
    }

    public enum RemovalType {
        NORMAL,
        FORCE
    }

    public interface Callback {
        /**
         * Called when a device goes into unpairing mode.
         * Could be searching or doing it's business but the call has been made.
         *
         * @param type Type of device being unpaired
         * @param removalType Type of removal completed.
         */
        void deviceUnpairing(DeviceType type, RemovalType removalType);

        /**
         * The device was successfully removed.
         *
         * @param type Type of device being unpaired
         * @param removalType Type of removal completed.
         */
        void unpairingSuccess(DeviceType type, RemovalType removalType);

        /**
         * Unpairing failed.
         *
         * @param type Type of device being unpaired
         * @param removalType Type of removal completed.
         */
        void unpairingFailure(DeviceType type, RemovalType removalType);

        /**
         * Device is currently offline, cannot issue the unpairing request normally.
         *
         * @param type Type of device being unpaired
         * @param removalType Type of removal completed.
         */
        void deviceOffline(DeviceType type, RemovalType removalType);

        /**
         *
         * If the device model has to be loaded, this will be called while we reload.
         *
         */
        void showLoading();
    }
}
