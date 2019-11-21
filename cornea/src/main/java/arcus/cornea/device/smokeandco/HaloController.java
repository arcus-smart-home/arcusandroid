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
package arcus.cornea.device.smokeandco;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.EASCodeProvider;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.DebouncedRequest;
import arcus.cornea.utils.DebouncedRequestScheduler;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.IrisClient;
import com.iris.client.bean.EasCode;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Dimmer;
import com.iris.client.capability.Halo;
import com.iris.client.capability.Switch;
import com.iris.client.capability.WeatherRadio;
import com.iris.client.connection.ConnectionState;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.service.NwsSameCodeService;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("WeakerAccess")
public class HaloController implements DebouncedRequest.DebounceCallback {
    public interface Callback {
        void onError(Throwable throwable);
        void onSuccess(DeviceModel deviceModel);
    }

    public interface LocationCallback {
        void onStateListLoaded(List<Map<String, Object>> stateList);
        void onCountyListLoaded(List<String> countyList);
        void onSAMECodeLoaded(String sameCode);
    }

    public interface alertCallback {
        void codesLoaded(ArrayList<String> easCodes);
    }

    public static final int REQUEST_TIMEOUT = 30_000;
    public static final int DEBOUNCE_DELAY = 500;

    protected Reference<Callback> callbackRef = new WeakReference<>(null);
    protected Reference<LocationCallback> postPairingCallbackRef = new WeakReference<>(null);
    protected alertCallback alertsCallbackRef;
    private final IrisClient irisClient;
    private final DebouncedRequestScheduler requestScheduler;
    private ModelSource<DeviceModel> haloDevice = CachedModelSource.newSource();
    private final Listener<DeviceModel> loadListener = new Listener<DeviceModel>() {
        @Override public void onEvent(DeviceModel deviceModel) {
            update();
        }
    };
    private final Listener<NwsSameCodeService.ListSameStatesResponse> stateListListener = new Listener<NwsSameCodeService.ListSameStatesResponse>() {
        @Override public void onEvent(NwsSameCodeService.ListSameStatesResponse response) {
            final LocationCallback callback = postPairingCallbackRef.get();
            if (callback == null) {
                return;
            }

            callback.onStateListLoaded(response.getSameStates());
        }
    };
    private final Listener<ClientEvent> countyListListener = new Listener<ClientEvent>() {
        //@Override public void onEvent(NwsSameCodeService.ListSameCountiesResponse response) {
        @Override public void onEvent(ClientEvent event) {
            NwsSameCodeService.ListSameCountiesResponse response = new NwsSameCodeService.ListSameCountiesResponse(event);
            final LocationCallback callback = postPairingCallbackRef.get();
            if (callback == null) {
                return;
            }

            callback.onCountyListLoaded(response.getCounties());
        }
    };
    private final Listener<ClientEvent> sameCodeListener = new Listener<ClientEvent>() {
        @Override public void onEvent(ClientEvent event) {
            NwsSameCodeService.GetSameCodeResponse response = new NwsSameCodeService.GetSameCodeResponse(event);
            final LocationCallback callback = postPairingCallbackRef.get();
            if (callback == null) {
                return;
            }
            callback.onSAMECodeLoaded(response.getCode());
        }
    };
    private final Listener<ClientEvent> debouncedSuccessListener = new Listener<ClientEvent>() {
        @Override public void onEvent(ClientEvent clientEvent) {
            update();
        }
    };
    private final Listener<Throwable> genericErrorListener = new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            emitError(throwable);
        }
    };
    private final Set<String> updateValues;

    public @VisibleForTesting HaloController(
          @NonNull ModelSource<DeviceModel> deviceModel,
          @NonNull IrisClient irisClient,
          @Nullable Set<String> monitorProperties
    ) {
        haloDevice = deviceModel;
        haloDevice.load();
        updateValues = monitorProperties;

        if (updateValues != null) {
            haloDevice.addModelListener(new Listener<ModelEvent>() {
                @Override public void onEvent(ModelEvent modelEvent) {
                    if (!(modelEvent instanceof ModelChangedEvent)) {
                        return;
                    }

                    ModelChangedEvent mce = (ModelChangedEvent) modelEvent;
                    Set<String> results = Sets.intersection(mce.getChangedAttributes().keySet(), updateValues);
                    if (!results.isEmpty()) {
                        update();
                    }
                }
            });
        }

        this.irisClient = irisClient;
        requestScheduler = new DebouncedRequestScheduler(DEBOUNCE_DELAY);
    }

    public @NonNull ListenerRegistration setCallback(@Nullable Callback callback) {
        this.callbackRef = new WeakReference<>(callback);
        if (haloDevice.isLoaded()) {
            update();
        }
        else {
            haloDevice.load().onSuccess(loadListener);
        }

        return new ListenerRegistration() {
            @Override public boolean isRegistered() {
                return callbackRef.get() != null;
            }

            @Override public boolean remove() {
                boolean isRegistered = isRegistered();
                callbackRef.clear();
                return isRegistered;
            }
        };
    }

    public @NonNull ListenerRegistration setLocationCallback(@Nullable LocationCallback callback) {
        this.postPairingCallbackRef = new WeakReference<>(callback);

        return new ListenerRegistration() {
            @Override public boolean isRegistered() {
                return postPairingCallbackRef.get() != null;
            }

            @Override public boolean remove() {
                boolean isRegistered = isRegistered();
                postPairingCallbackRef.clear();
                return isRegistered;
            }
        };
    }

    public @NonNull void setAlertCallback(@Nullable alertCallback callback) {
        alertsCallbackRef = callback;
    }

    public void refreshModel() {
        haloDevice.load().onSuccess(loadListener).onFailure(genericErrorListener);
    }

    public @NonNull Map<String, Object> clearChanges() {
        DeviceModel deviceModel = haloDevice.get();

        if (deviceModel != null) {
            Map<String, Object> changedValues = deviceModel.getChangedValues();
            deviceModel.clearChanges();

            return changedValues;
        }

        return Collections.emptyMap();
    }

    public void testDevice() {
        if (!clientConnectedOrEmitError()) {
            return;
        }

        DeviceModel deviceModel = deviceModelOrEmitError();
        if (deviceModel == null) {
            return;
        }

        Halo.StartTestRequest testRequest = new Halo.StartTestRequest();
        testRequest.setAddress(deviceModel.getAddress());
        testRequest.setRestfulRequest(false);
        testRequest.setTimeoutMs(REQUEST_TIMEOUT);

        irisClient.request(testRequest).onFailure(genericErrorListener);
    }

    public void stopPlayingRadio() {
        if (!clientConnectedOrEmitError()) {
            return;
        }

        DeviceModel deviceModel = deviceModelOrEmitError();
        if (deviceModel == null) {
            return;
        }

        if (!nonNullCollection(deviceModel.getCaps()).contains(WeatherRadio.NAMESPACE)) {
            emitError(new RuntimeException(WeatherRadio.NAMESPACE + " is not in the devices capabilities."));
            return;
        }

        WeatherRadio.StopPlayingStationRequest request = new WeatherRadio.StopPlayingStationRequest();
        request.setAddress(deviceModel.getAddress());
        request.setTimeoutMs(REQUEST_TIMEOUT);
        request.setRestfulRequest(false);

        irisClient.request(request).onFailure(genericErrorListener);
    }

    public void playWeatherStation(Integer selectedStation, int durationSeconds) {
        if (!clientConnectedOrEmitError()) {
            return;
        }

        DeviceModel deviceModel = deviceModelOrEmitError();
        if (deviceModel == null) {
            return;
        }

        if (!nonNullCollection(deviceModel.getCaps()).contains(WeatherRadio.NAMESPACE)) {
            emitError(new RuntimeException(WeatherRadio.NAMESPACE + " is not in the devices capabilities."));
            return;
        }

        WeatherRadio.PlayStationRequest request = new WeatherRadio.PlayStationRequest();
        request.setAddress(deviceModel.getAddress());
        request.setTimeoutMs(REQUEST_TIMEOUT);
        request.setRestfulRequest(false);
        request.setStation(selectedStation == null ? getSelectedStation(deviceModel) : selectedStation);
        request.setTime(durationSeconds);

        irisClient.request(request).onFailure(genericErrorListener);
    }

    public void stopWeatherStation() {
        if (!clientConnectedOrEmitError()) {
            return;
        }

        DeviceModel deviceModel = deviceModelOrEmitError();
        if (deviceModel == null) {
            return;
        }

        if (!nonNullCollection(deviceModel.getCaps()).contains(WeatherRadio.NAMESPACE)) {
            emitError(new RuntimeException(WeatherRadio.NAMESPACE + " is not in the devices capabilities."));
            return;
        }

        WeatherRadio.StopPlayingStationRequest request = new WeatherRadio.StopPlayingStationRequest();
        request.setAddress(deviceModel.getAddress());
        request.setTimeoutMs(REQUEST_TIMEOUT);
        request.setRestfulRequest(false);

        irisClient.request(request).onFailure(genericErrorListener);
    }

    public void setSwitchOn(boolean isOn) {
        if (!clientConnectedOrEmitError()) {
            return;
        }

        DeviceModel deviceModel = deviceModelOrEmitError();
        if (deviceModel == null) {
            return;
        }

        irisClient
              .request(setAttributesRequest(deviceModel, Switch.ATTR_STATE, isOn ? Switch.STATE_ON : Switch.STATE_OFF))
              .onFailure(genericErrorListener);
    }

    public void setDimmer(@IntRange(from = 1, to = 100) int dimmerPercent) {
        if (!clientConnectedOrEmitError()) {
            return;
        }

        DeviceModel deviceModel = deviceModelOrEmitError();
        if (deviceModel == null) {
            return;
        }

        deviceModel.set(Dimmer.ATTR_BRIGHTNESS, dimmerPercent);

        DebouncedRequest debouncedRequest = new DebouncedRequest(deviceModel);
        debouncedRequest.setOnError(genericErrorListener);
        debouncedRequest.setOnSuccess(debouncedSuccessListener);

        requestScheduler.schedule(Dimmer.ATTR_BRIGHTNESS, debouncedRequest);
    }

    protected void emitError(Throwable throwable) {
        final Callback callback = callbackRef.get();
        if (callback == null) {
            return;
        }

        callback.onError(throwable);
    }

    protected void update() {
        final Callback callback = callbackRef.get();
        if (callback == null) {
            return;
        }

        DeviceModel deviceModel = deviceModelOrEmitError();
        if (deviceModel != null) {
            callback.onSuccess(deviceModel);
        }
    }

    protected @Nullable DeviceModel deviceModelOrEmitError() {
        DeviceModel deviceModel = haloDevice.get();
        if (deviceModel == null) {
            emitError(new RuntimeException("Unable to get model address - model loaded?"));
            return null;
        }

        return deviceModel;
    }

    protected boolean clientConnectedOrEmitError() {
        if (irisClient != null && ConnectionState.CONNECTED.equals(irisClient.getConnectionState())) {
            return true;
        }
        else {
            emitError(new RuntimeException("Client null/not connected."));
            return false;
        }
    }

    protected Collection nonNullCollection(Collection collection) {
        return collection == null ? Collections.emptySet() : collection;
    }

    @SuppressWarnings("unchecked")
    protected @NonNull Map<String, String> nonNullMap(Object object) {
        if (object == null) {
            return Collections.emptyMap();
        } else {
            try {
                return (Map<String, String>) object;
            } catch (Exception ex) { // Can't coerce, log?
                return Collections.emptyMap();
            }
        }
    }

    protected ClientRequest setAttributesRequest(@NonNull DeviceModel deviceModel, String attribute, Object value) {
        ClientRequest request = new ClientRequest();

        request.setCommand(Capability.CMD_SET_ATTRIBUTES);
        request.setTimeoutMs(REQUEST_TIMEOUT);
        request.setRestfulRequest(false);
        request.setAddress(deviceModel.getAddress());
        request.setAttribute(attribute, value);

        return request;
    }

    public ClientFuture getStateNames() {
        NwsSameCodeService sameCodeService = CorneaClientFactory.getService(NwsSameCodeService.class);
        return sameCodeService.listSameStates().onSuccess(Listeners.runOnUiThread(stateListListener)).onFailure(Listeners.runOnUiThread(genericErrorListener));
    }

    public ClientFuture getCountyNames(String sameStateCode) {
        NwsSameCodeService.ListSameCountiesRequest request = new NwsSameCodeService.ListSameCountiesRequest();
        request.setAddress("SERV:" + NwsSameCodeService.NAMESPACE + ":");
        request.setRestfulRequest(true);
        request.setStateCode(sameStateCode);
        ClientFuture<ClientEvent> response = irisClient.request(request);
        response.onSuccess(Listeners.runOnUiThread(countyListListener)).onFailure(Listeners.runOnUiThread(genericErrorListener));
        return response;
    }

    public ClientFuture getSameCode(String stateCode, String county) {

        NwsSameCodeService.GetSameCodeRequest request = new NwsSameCodeService.GetSameCodeRequest();
        request.setAddress("SERV:" + NwsSameCodeService.NAMESPACE + ":");
        request.setRestfulRequest(true);
        request.setCounty(county);
        request.setStateCode(stateCode);
        ClientFuture<ClientEvent> response = irisClient.request(request);

        response.onSuccess(Listeners.runOnUiThread(sameCodeListener)).onFailure(Listeners.runOnUiThread(genericErrorListener));
        return response;
    }

    public void setLocation(String locationCode) {
        WeatherRadio radio = (WeatherRadio) haloDevice.get();
        radio.setLocation(locationCode);
        haloDevice.get().commit();
    }

    protected int getSelectedStation(@NonNull DeviceModel deviceModel) {
        try {
            Number selected = (Number) deviceModel.get(WeatherRadio.ATTR_STATIONSELECTED);
            if (selected == null) {
                return 0;
            }

            return selected.intValue();
        }
        catch (Exception ex) {
            return 0;
        }
    }

    public int getSelectedStation() {
        return getSelectedStation(haloDevice.get());
    }

    public void setSelectedStation(int station) {
        if(!(haloDevice.get() instanceof WeatherRadio)) {
            return;
        }
        WeatherRadio radio = (WeatherRadio) haloDevice.get();
        radio.setStationselected(station);
        haloDevice.get().commit();
    }

    public void getStationList(Listener<WeatherRadio.ScanStationsResponse> onSuccess, Listener<Throwable> onFailure) {
        if(!(haloDevice.get() instanceof WeatherRadio)) {
            return;
        }
        WeatherRadio radio = (WeatherRadio) haloDevice.get();
        radio.scanStations().onSuccess(onSuccess).onFailure(onFailure);
    }

    // Used this method so we can keep the interface (contract) the same for dependencies.
    public List<String> getRoomTypes() {
        Set<String> rooms = doGetRoomTypes().keySet();
        List<String> room = Lists.newArrayList(rooms);
        Collections.sort(room);
        return room;
    }

    public void setRoomType(String roomType) {
        if(!(haloDevice.get() instanceof Halo) || roomType == null) {
            return;
        }

        String selectedRoom = doGetRoomTypes().get(roomType);
        if (selectedRoom == null) { // Try setting with the passed in room type...
            selectedRoom = roomType;
        }

        Halo halo = (Halo) haloDevice.get();
        halo.setRoom(selectedRoom);
        haloDevice.get().commit();
    }

    public String getSelectedRoomType() {
        if(!(haloDevice.get() instanceof Halo)) {
            return null;
        }

        Halo halo = (Halo) haloDevice.get();
        String room = halo.getRoom();
        Map<String, String> roomMap = doGetRoomTypes();

        for (Map.Entry<String, String> entry : roomMap.entrySet()) {
            if(entry.getValue().equals(room)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Get the room types from the device model. The keys / values will be swapped.
     *
     * @return map of user string->key
     */
    protected Map<String, String> doGetRoomTypes() {
        DeviceModel deviceModel = deviceModelOrEmitError();
        if (deviceModel != null) {
            Map<String, String> roomTypes =  nonNullMap(deviceModel.get(Halo.ATTR_ROOMNAMES));
            return HashBiMap.create(roomTypes).inverse();
        } else {
            return Collections.emptyMap();
        }
    }

    public void getEasCodes() {
        final DeviceModel deviceModel = haloDevice.get();
        if (deviceModel == null) {
            return;
        }
        EASCodeProvider.instance().load()
                .onSuccess(new Listener<List<EasCode>>() {
                    @Override
                    public void onEvent(List<EasCode> easCodes) {
                        ArrayList<String> codes = new ArrayList<>();
                        Collection<String> currentOnDevice = collectionOrEmpty(deviceModel.get(WeatherRadio.ATTR_ALERTSOFINTEREST));
                        for (EasCode easCode : easCodes) {
                            codes.add(easCode.getName());
                        }

                        Collections.sort(codes);
                        if (alertsCallbackRef == null) {
                            return;
                        }
                        alertsCallbackRef.codesLoaded(codes);

                    }
                }).onFailure(genericErrorListener);
    }

    protected Collection<String> collectionOrEmpty(Object fromThis) {
        if (fromThis == null) {
            return Collections.emptySet();
        } else {
            try {
                return (Collection<String>) fromThis;
            } catch (Exception ex) {
                return Collections.emptySet();
            }
        }
    }

    // For de-bounced requests
    @Override public void commitEvent() {
        update();
    }
}
