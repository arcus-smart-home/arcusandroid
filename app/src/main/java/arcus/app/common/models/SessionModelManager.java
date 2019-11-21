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
package arcus.app.common.models;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import arcus.cornea.dto.HubDeviceModelDTO;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.HubModelProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.capability.Device;
import com.iris.client.capability.Hub;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.HubModel;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.utils.CorneaUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

@Deprecated // Use DeviceModelProvider/HubModelProvider
public class SessionModelManager {

    public enum EventType {
        DEVICE_UPDATE_EVENT,
        DEVICE_FETCH_EVENT,
        ERROR_EVENT,
        HUB_NOT_FOUND_EVENT,
        HUB_UPDATE_EVENT,
        HUB_FETCH_EVENT,
    }

    public interface SessionModelChangeListener {
        void onSessionModelChangeEvent(SessionModelChangedEvent event);
    }



    private static final Logger logger = LoggerFactory.getLogger(BaseFragment.class);
    private static final SessionModelManager instance;
    static {
        instance = new SessionModelManager();
        instance.setupDeviceStoreListener();
        instance.setupHubStoreListener();
        DeviceModelProvider.instance().load();
        HubModelProvider.instance().load();
    }
    private List<SessionModelChangeListener> sessionModelManagerListeners = new ArrayList<>();

    private ListenerRegistration deviceStoreLoadedListener;
    private ListenerRegistration deviceStoreChangedListener;
    private ListenerRegistration hubStoreLoadedListener;
    private ListenerRegistration hubStoreChangedListener;

    private final Listener<List<DeviceModel>> onDeviceStoreLoaded = new Listener<List<DeviceModel>>() {
        @Override
        public void onEvent(List<DeviceModel> deviceModels) {
            addDeviceChangedListener();
        }
    };
    private final Listener<ModelEvent> deviceModelEvent = Listeners.runOnUiThread(new Listener<ModelEvent>() {
        @Override
        public void onEvent(ModelEvent event) {
            if (event instanceof ModelChangedEvent) { // Only want to notify here if the name changes.
                ModelChangedEvent mae = (ModelChangedEvent) event;
                Set<String> changedKeys = mae.getChangedAttributes().keySet();

                if (changedKeys.contains(Device.ATTR_NAME)) {
                    notifyListeners(new SessionModelChangedEvent(EventType.DEVICE_UPDATE_EVENT, event));
                }
            }
            else {
                notifyListeners(new SessionModelChangedEvent(EventType.DEVICE_UPDATE_EVENT, event));
            }
        }
    });

    private final Listener<List<HubModel>> onHubStoreLoaded = Listeners.runOnUiThread(new Listener<List<HubModel>>() {
        @Override
        public void onEvent(List<HubModel> hubModels) {
            addHubChangedListener();
        }
    });
    private final Listener<ModelEvent> hubModelEvent = Listeners.runOnUiThread(new Listener<ModelEvent>() {
        @Override
        public void onEvent(ModelEvent event) {
            if (event instanceof ModelChangedEvent) {
                ModelChangedEvent mae = (ModelChangedEvent) event;
                Set<String> changedKeys = mae.getChangedAttributes().keySet();

                if (changedKeys.contains(Hub.ATTR_NAME) || changedKeys.contains(Hub.ATTR_STATE)) {
                    notifyListeners(new SessionModelChangedEvent(EventType.HUB_UPDATE_EVENT, event));
                }
            }
            else {
                notifyListeners(new SessionModelChangedEvent(EventType.HUB_UPDATE_EVENT, event));
            }
        }
    });

    public static SessionModelManager instance() {
        return instance;
    }

    private void setupDeviceStoreListener() {
        deviceStoreLoadedListener = Listeners.clear(deviceStoreLoadedListener);
        deviceStoreLoadedListener = DeviceModelProvider.instance().addStoreLoadListener(onDeviceStoreLoaded);
    }
    private void setupHubStoreListener() {
        hubStoreLoadedListener = Listeners.clear(hubStoreLoadedListener);
        hubStoreLoadedListener = HubModelProvider.instance().addStoreLoadListener(onHubStoreLoaded);
    }

    private void addDeviceChangedListener() {
        deviceStoreChangedListener = Listeners.clear(deviceStoreChangedListener);
        deviceStoreChangedListener = DeviceModelProvider.instance().getStore().addListener(deviceModelEvent);
    }
    private void addHubChangedListener() {
        hubStoreChangedListener = Listeners.clear(hubStoreChangedListener);
        hubStoreChangedListener = HubModelProvider.instance().getStore().addListener(hubModelEvent);
    }

    private List<DeviceModel> getSortedDevices() {
        List<DeviceModel> models = Lists.newArrayList(DeviceModelProvider.instance().getStore().values());
        Collections.sort(models, CorneaUtils.deviceModelComparator);

        return models;
    }

    @Deprecated
    public static void reset() {
        //instance = null;
    }

    @Nullable
    private Iterable<DeviceModel> getUnsortedDevices() {
        if (!devicesLoaded()) {
            return null;
        }

        return DeviceModelProvider.instance().getStore().values();
    }

    @Nullable
    public List<DeviceModel> getDevices() {
        if (!devicesLoaded()) {
            return null;
        }

        return getSortedDevices();
    }

    @Nullable
    public List<DeviceModel> getDevicesWithHub() {
        if (!devicesLoaded())  {
            getHubThenDevices();
            return null;
        }

        List<DeviceModel> devices = new ArrayList<>();
        HubModel hub = HubModelProvider.instance().getHubModel();
        if (hub != null) {
            devices.add(new HubDeviceModelDTO(hub));
        }
        devices.addAll(getSortedDevices());

        return devices;
    }

    public int deviceCount(boolean withHub) {
        int hubCount = withHub ? HubModelProvider.instance().getStore().size() : 0;
        return DeviceModelProvider.instance().getStore().size() + hubCount;
    }

    public int indexOf(DeviceModel model, boolean withHub) {
        if (model == null || !devicesLoaded()) {
            return -1;
        }

        return indexOf(model.getId(), withHub);
    }

    public int indexOf(String deviceId, boolean withHub) {
        if (Strings.isNullOrEmpty(deviceId) || !devicesLoaded()) {
            return -1;
        }

        int deviceIndex = indexOf(deviceId);
        if (deviceIndex == -1) {
            return -1;
        }

        int hubCount = withHub ? HubModelProvider.instance().getStore().size() : 0;
        return deviceIndex + hubCount;
    }

    private int indexOf(String id) {
        List<DeviceModel> models = getSortedDevices();
        for (int i = 0; i < models.size(); i++) {
            if (id.equals(models.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }

    @Nullable
    public DeviceModel getDeviceWithId(String deviceId, boolean withHub){
        if (Strings.isNullOrEmpty(deviceId)) {
            logger.error("Can not lookup device with empty/null ID");
            return null;
        }

        Iterable<DeviceModel> deviceModels = withHub ? getDevicesWithHub() : getUnsortedDevices();
        if (deviceModels == null) {
            logger.debug("No devices stored was null? [{}]", deviceModels == null);
            return null;
        }

        for(DeviceModel deviceModel : deviceModels){
            if(deviceModel.getId().equals(deviceId)){
                return deviceModel;
            }
        }

        logger.warn("SessionModelManager could not find device with id [{}]", deviceId);
        return null;
    }

    @NonNull
    public List<DeviceModel> getDevicesWithTag(String tag) {
        List<DeviceModel> models = getDevices();
        List<DeviceModel> taggedDevices = new ArrayList<>();

        if (models == null) {
            return taggedDevices;
        }

        for (DeviceModel model : models) {
            if (model.getTags() != null && model.getTags().contains(tag)) {
                taggedDevices.add(model);
            }
        }

        return taggedDevices;
    }

    @Nullable
    public HubModel getHubModel() {
        HubModel model = HubModelProvider.instance().getHubModel();
        if (model == null) {
            getHubThenDevices();
            return null;
        }
        return model;
    }

    @Nullable
    public String getHubID() {
        HubModel model = HubModelProvider.instance().getHubModel();
        if (model == null) {
            return null;
        }
        return model.getId();
    }

    private void getHubThenDevices() {
        HubModelProvider.instance().load();
        DeviceModelProvider.instance().load();
    }

    private boolean devicesLoaded() {
        return DeviceModelProvider.instance().isLoaded();
    }

    private boolean hubLoaded() {
        return HubModelProvider.instance().isLoaded();
    }


    // Events
    public void addSessionModelChangeListener(@NonNull SessionModelChangeListener l) {
        sessionModelManagerListeners.add(l);
    }

    public boolean isListenerRegistered(@NonNull SessionModelChangeListener listener) {
        return sessionModelManagerListeners.contains(listener);
    }

    public boolean removeSessionModelChangeListener(@NonNull SessionModelChangeListener l) {
        return sessionModelManagerListeners.remove(l);
    }

    private void notifyListeners(@NonNull SessionModelChangedEvent event) {
        for (SessionModelChangeListener listener : sessionModelManagerListeners) {
            try {
                listener.onSessionModelChangeEvent(event);
            }
            catch (Exception ex) {
                logger.debug("Caught exception trying to dispatch Session Model Event", ex);
            }
        }
    }

    public class SessionModelChangedEvent {
        @Nullable private ModelEvent modelEvent;
        @Nullable private Throwable  error;
        @NonNull  private EventType  eventType;

        public SessionModelChangedEvent(
              @NonNull EventType eventType
        ) {
            this(eventType, null, null);
        }

        public SessionModelChangedEvent(
              @NonNull EventType eventType, @Nullable ModelEvent modelEvent
        ) {
            this(eventType, modelEvent, null);
        }

        public SessionModelChangedEvent(
              @NonNull EventType eventType, @Nullable Throwable error
        ) {
            this(eventType, null, error);
        }

        public SessionModelChangedEvent(
              @NonNull EventType eventType, @Nullable ModelEvent modelEvent, @Nullable Throwable error
        ) {
            this.modelEvent = modelEvent;
            this.eventType = eventType;
            this.error = error;
        }

        @NonNull
        public EventType getEventType() {
            return eventType;
        }

        @Nullable
        public ModelEvent getModelEvent() {
            return modelEvent;
        }

        @Nullable
        public Throwable getError() {
            return error;
        }
    }
}
