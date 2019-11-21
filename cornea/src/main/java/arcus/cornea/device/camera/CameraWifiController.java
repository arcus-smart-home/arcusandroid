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
package arcus.cornea.device.camera;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.device.camera.model.AvailableNetworkModel;
import arcus.cornea.device.camera.model.CameraConnectionSettingModel;
import arcus.cornea.device.camera.model.WiFiSecurityType;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import com.iris.client.ClientEvent;
import com.iris.client.ClientMessage;
import com.iris.client.IrisClient;
import com.iris.client.capability.WiFi;
import com.iris.client.capability.WiFiScan;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class CameraWifiController {
    public interface Callback {
        /**
         * Called when something goes wrong.  Berry Berry wrong.
         *
         * @param error Error encountered.
         */
        void onError(Throwable error);

        /**
         * Called anytime there is a change in the status of the connection.
         *
         * These should trigger this callback:
         * Switch from:
         * Wifi -> Ethernet
         * Ethernet -> Wifi
         *
         * @param settings The current camera settings.
         */
        void showCameraConnection(CameraConnectionSettingModel settings);

        /**
         *
         * Informs the UI there are available networks to be shown. While this will never be null, it can be empty.
         *
         * @param availableNetworkModels avaialble networks to show
         */
        void showScanResults(@NonNull List<AvailableNetworkModel> availableNetworkModels);

        /**
         * Called after a save to the model has been made.  This does not mean that the device
         * has successfully switched over to WiFi/Ethernet, only that the settings hit the platform
         * and back to us w/o error.
         */
        void showSavingSuccess();
    }

    private static final Logger logger = LoggerFactory.getLogger(CameraWifiController.class);
    private static final int TIMEOUT_SECONDS = 60;
    private final ModelSource<DeviceModel> source;
    private final List<AvailableNetworkModel> networks;
    private final Set<String> UPDATE_ON = ImmutableSet.of(
          WiFi.ATTR_STATE,
          WiFi.ATTR_SSID,
          WiFi.ATTR_SECURITY
    );
    private final IrisClient client;
    private WeakReference<Callback> callback;
    private ListenerRegistration modelListenerReg;
    private ListenerRegistration wifiScanResultListener;
    private List<String> availableSecurityTypes;
    private final Listener<Throwable> onErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            showError(throwable);
        }
    });
    private final Listener<DeviceModel> onDeviceLoadedListener = new Listener<DeviceModel>() {
        @Override
        public void onEvent(DeviceModel deviceModel) {
            startWifiScan();
        }
    };
    private Listener<ModelEvent> modelEventListener = Listeners.runOnUiThread(new Listener<ModelEvent>() {
        @Override
        public void onEvent(ModelEvent event) {
            if (!(event instanceof ModelChangedEvent)) {
                return;
            }

            ModelChangedEvent mce = (ModelChangedEvent) event;
            Set<String> changedProperties = mce.getChangedAttributes().keySet();
            Set<String> intersection = Sets.intersection(changedProperties, UPDATE_ON);
            if (intersection.isEmpty()) {
                return;
            }

            showCameraConnection();
        }
    });
    private final Listener<ClientEvent> saveSuccessListener = Listeners.runOnUiThread(
          new Listener<ClientEvent>() {
              @Override
              public void onEvent(ClientEvent response) {
                  if (!WiFi.ConnectResponse.NAME.equals(response.getType())) {
                      return;
                  }

                  WiFi.ConnectResponse connectResponse = new WiFi.ConnectResponse(response);
                  if (WiFi.ConnectResponse.STATUS_OK.equals(connectResponse.getStatus())) {
                      onSavingSuccess();
                  }
                  else {
                      showError(new RuntimeException("Connection Refused."));
                  }
              }
          }
    );
    private final ListenerRegistration defaultListenerRegistration = new ListenerRegistration() {
        @Override
        public boolean isRegistered() {
            return CameraWifiController.this.callback.get() != null;
        }

        @Override
        public boolean remove() {
            return doClear();
        }
    };

    public static CameraWifiController newController(String deviceId, Callback callback) {
        ModelSource<DeviceModel> source = DeviceModelProvider.instance().getModel(deviceId);
        CameraWifiController controller = new CameraWifiController(CorneaClientFactory.getClient(), source);
        controller.setCallback(callback);
        return controller;
    }

    CameraWifiController(IrisClient irisClient, ModelSource<DeviceModel> modelSource) {
        this.client = irisClient;
        this.source = modelSource;
        this.networks = new ArrayList<>();
        this.callback = new WeakReference<>(null);
        this.availableSecurityTypes = new ArrayList<>();
        this.modelListenerReg = this.source.addModelListener(modelEventListener);
        this.source.load().onSuccess(onDeviceLoadedListener);
    }

    public List<String> getAllAvailableSecurityTypes() {
        if (availableSecurityTypes.isEmpty()) {
            availableSecurityTypes = WiFiSecurityType.getNames();
        }

        return availableSecurityTypes;
    }

    public List<AvailableNetworkModel> getAvailableWifiNetworks() {
        return Collections.unmodifiableList(networks);
    }

    public void startWifiScan() {
        Listeners.clear(wifiScanResultListener);
        wifiScanResultListener = CorneaClientFactory.getClient().addMessageListener(new Listener<ClientMessage>() {
            @Override
            public void onEvent(ClientMessage clientMessage) {
                if (clientMessage == null) {
                    return;
                }

                ClientEvent event = clientMessage.getEvent();
                if (event == null || !WiFiScan.WiFiScanResultsEvent.NAME.equals(event.getType())) {
                    return;
                }

                WiFiScan.WiFiScanResultsEvent results = new WiFiScan.WiFiScanResultsEvent(clientMessage.getEvent());
                for (Map<String, Object> result : results.getScanResults()) {
                    AvailableNetworkModel scanResult = new AvailableNetworkModel(result);
                    networks.remove(scanResult);

                    if (scanResult.getSignal() != 0) {
                        networks.add(scanResult);
                    }
                }

                showScanResults();
            }
        });

        WiFiScan wiFiScan = getWifiScanFromModel();
        if (wiFiScan == null) {
            showError(new RuntimeException("Unable to process request. Model does not contain WiFi Scan."));
            return;
        }

        DeviceModel model = getModel();
        if (model == null) {
            return;
        }

        WiFiScan.StartWifiScanRequest request = new WiFiScan.StartWifiScanRequest();
        request.setAddress(model.getAddress());
        request.setRestfulRequest(false);
        request.setTimeoutMs(10_000);
        request.setAttributes(
              ImmutableMap.<String, Object>of(
                    WiFiScan.StartWifiScanRequest.ATTR_TIMEOUT, TIMEOUT_SECONDS
              )
        );

        client.request(request).onFailure(onErrorListener);
    }

    public void stopWifiScan() {
        WiFiScan wiFiScan = getWifiScanFromModel();
        if (wiFiScan == null) {
            return; // No Op.
        }

        wifiScanResultListener = Listeners.clear(wifiScanResultListener);

        DeviceModel model = getModel();
        if (model == null) {
            return;
        }

        WiFiScan.EndWifiScanRequest request = new WiFiScan.EndWifiScanRequest();
        request.setTimeoutMs(10_000);
        request.setRestfulRequest(false);
        request.setAddress(model.getAddress());

        client.request(request);
    }

    public void connectToWifiNetwork(@NonNull String ssid, @NonNull WiFiSecurityType security, @NonNull char[] password) {
        if (Strings.isNullOrEmpty(ssid)) {
            return;
        }

        WiFi wiFi = getWiFiFromModel();
        if (wiFi == null) {
            showError(new RuntimeException("Unable to process request. Model is unavailable."));
            return;
        }

        DeviceModel model = getModel();
        if (model == null) {
            showError(new RuntimeException("Unable to process request. Model is unavailable."));
            return;
        }

        WiFi.ConnectRequest request = new WiFi.ConnectRequest();
        request.setAddress(model.getAddress());
        request.setRestfulRequest(false);
        request.setTimeoutMs(10_000);
        request.setSsid(ssid);
        request.setSecurity(security.name());
        request.setKey(new String(password));

        client.request(request)
              .onSuccess(saveSuccessListener)
              .onFailure(onErrorListener);

        Arrays.fill(password, '\u0000');
    }

    public CameraConnectionSettingModel getCameraConfiguration() {
        return buildCameraModel();
    }

    public void clearCallback() {
        doClear();
    }

    public @NonNull ListenerRegistration setCallback(@Nullable Callback callback) {
        if(this.callback.get() != null) {
            logger.warn("Replacing existing callback");
        }
        this.callback = new WeakReference<>(callback);

        if(!modelListenerReg.isRegistered()) {
            modelListenerReg = source.addModelListener(modelEventListener);
        }

        return defaultListenerRegistration;
    }

    protected @Nullable Callback getCallback() {
        return this.callback.get();
    }

    protected @Nullable WiFi getWiFiFromModel() {
        DeviceModel model = getModel();
        if (model == null) {
            return null;
        }

        if (model.getCaps() == null || !model.getCaps().contains(WiFi.NAMESPACE)) {
            return null;
        }

        return (WiFi) model;
    }

    protected @Nullable WiFiScan getWifiScanFromModel() {
        DeviceModel model = getModel();
        if (model == null) {
            return null;
        }

        if (model.getCaps() == null || !model.getCaps().contains(WiFiScan.NAMESPACE)) {
            return null;
        }

        return (WiFiScan) model;
    }

    protected @Nullable DeviceModel getModel() {
        return source.get();
    }

    protected boolean doClear() {
        modelListenerReg = Listeners.clear(modelListenerReg);
        wifiScanResultListener = Listeners.clear(wifiScanResultListener);
        if(callback.get() == null) {
            return false;
        }
        callback.clear();
        stopWifiScan();
        return true;
    }

    protected void showCameraConnection() {
        Callback cb = getCallback();
        if (cb != null) {
            try {
                cb.showCameraConnection(buildCameraModel());
            }
            catch (Exception ex) {
                logger.error("Caught ex trying to update Camera Connection View.", ex);
            }
        }
    }

    protected void showError(@NonNull final Throwable t) {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Callback cb = getCallback();
                if (cb != null) {
                    cb.onError(t);
                }
            }
        });
    }

    protected void showScanResults() {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Callback callback = getCallback();
                if (callback != null) {
                    logger.debug("Showing scan results of [{}]", getAvailableWifiNetworks());
                    callback.showScanResults(getAvailableWifiNetworks());
                }
            }
        });
    }

    protected void onSavingSuccess() {
        Callback callback = getCallback();
        if (callback != null) {
            callback.showSavingSuccess();
        }
    }

    protected CameraConnectionSettingModel buildCameraModel() {
        DeviceModel model = getModel();
        if (model == null) {
            return new CameraConnectionSettingModel(null, WiFiSecurityType.NONE, false, false);
        }

        WiFiSecurityType secType;
        try {
            secType = WiFiSecurityType.valueOf(String.valueOf(model.get(WiFi.ATTR_SECURITY)));
        }
        catch (Exception ex) { // IllegalArgumentEx.
            secType = WiFiSecurityType.NONE;
        }
        return new CameraConnectionSettingModel(
              String.valueOf(model.get(WiFi.ATTR_SSID)),
              secType,
              WiFi.STATE_CONNECTED.equalsIgnoreCase(String.valueOf(model.get(WiFi.ATTR_STATE))),
              Boolean.TRUE.equals(model.get(WiFi.ATTR_ENABLED)));
    }

    protected void addAvailableNetwork(@NonNull AvailableNetworkModel network) {
        this.networks.add(network);
    }
}
