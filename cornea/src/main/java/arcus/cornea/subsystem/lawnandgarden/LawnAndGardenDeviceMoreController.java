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
package arcus.cornea.subsystem.lawnandgarden;

import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.lawnandgarden.model.LawnAndGardenControllerModel;
import arcus.cornea.subsystem.lawnandgarden.model.LawnAndGardenControllerZoneDetailModel;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.IrisClient;
import com.iris.client.capability.Capability;
import com.iris.client.capability.IrrigationZone;
import com.iris.client.capability.LawnNGardenSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class LawnAndGardenDeviceMoreController
      extends BaseLawnAndGardenController<LawnAndGardenDeviceMoreController.Callback>
{
    public interface SaveCallback {
        void onError(Throwable throwable);
        void onSuccess();
    }

    public interface Callback {
        void showDevices(List<LawnAndGardenControllerModel> controllers);
        void onError(Throwable throwable);
    }

    private static final Logger logger = LoggerFactory.getLogger(LawnAndGardenDeviceMoreController.class);
    // Since most controllers will need this - should this be moved to base?
    private final AddressableListSource<DeviceModel> irrigationControllers;
    private final Listener<ModelEvent> controllersUpdatedListener = new Listener<ModelEvent>() {
        @Override public void onEvent(ModelEvent modelEvent) {
            updateView(); // Not wrapped here on purpose.
        }
    };
    private final Listener<Throwable> errorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            onError(throwable);
        }
    });
    private Reference<SaveCallback> saveCallbackRef = new WeakReference<>(null);
    private final Listener<Throwable> saveErrorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            onSaveError(throwable);
        }
    });
    private final Listener<ClientEvent> saveSuccess = new Listener<ClientEvent>() {
        @Override public void onEvent(ClientEvent clientEvent) {
            onSaveSuccess();
        }
    };
    private final IrisClient irisClient;
    private static final LawnAndGardenDeviceMoreController INSTANCE;
    static {
        INSTANCE = new LawnAndGardenDeviceMoreController(
              DeviceModelProvider.instance().getModels(ImmutableSet.<String>of()),
              CorneaClientFactory.getClient()
        );
        INSTANCE.init();
    }


    protected LawnAndGardenDeviceMoreController(
          @NonNull AddressableListSource<DeviceModel> irrControllers,
          @NonNull IrisClient client
    ) {
        super();
        irrigationControllers = irrControllers;
        irisClient = client;
    }

    protected LawnAndGardenDeviceMoreController(
          @NonNull ModelSource<SubsystemModel> subsystem,
          @NonNull AddressableListSource<DeviceModel> irrControllers,
          @NonNull IrisClient client
    ) {
        super(subsystem);
        irrigationControllers = irrControllers;
        irisClient = client;
    }

    @Override public void init() {
        super.init();
        irrigationControllers.addModelListener(controllersUpdatedListener);
    }

    public static LawnAndGardenDeviceMoreController instance() {
        return INSTANCE;
    }

    @Override protected void onSubsystemLoaded(ModelAddedEvent event) {
        LawnNGardenSubsystem subsystem = getLawnNGardenSubsystem();
        if (subsystem == null) {
            return;
        }

        irrigationControllers.setAddresses(list(subsystem.getControllers()), true);
    }

    @Override protected void onSubsystemChanged(ModelChangedEvent event) {
        super.onSubsystemChanged(event);

        if (event.getChangedAttributes().keySet().contains(LawnNGardenSubsystem.ATTR_CONTROLLERS)) {
            LawnNGardenSubsystem subsystem = getLawnNGardenSubsystem();
            if (subsystem == null) {
                return;
            }

            irrigationControllers.setAddresses(list(subsystem.getControllers()), true);
            updateView();
        }
    }

    public ListenerRegistration setSaveCallback(SaveCallback saveCallback) {
        saveCallbackRef = new WeakReference<>(saveCallback);
        return Listeners.wrap(saveCallbackRef);
    }

    public void updateZone(LawnAndGardenControllerZoneDetailModel model) {
        if (model == null || TextUtils.isEmpty(model.getDeviceAddress())) {
            onError(new RuntimeException("Model passed in, or address were null/empty."));
            return;
        }

        String zoneNameKey = keyNameFor(IrrigationZone.ATTR_ZONENAME, model.getInternalZoneName());
        String zoneDefaultWateringTimeKey = keyNameFor(IrrigationZone.ATTR_DEFAULTDURATION, model.getInternalZoneName());

        ClientRequest clientRequest = new ClientRequest();
        clientRequest.setRestfulRequest(false);
        clientRequest.setTimeoutMs(30_000);
        clientRequest.setAddress(model.getDeviceAddress());
        clientRequest.setCommand(Capability.CMD_SET_ATTRIBUTES);
        clientRequest.setAttributes(ImmutableMap.<String, Object>of(
              zoneNameKey, model.getZoneName(),
              zoneDefaultWateringTimeKey, model.getDefaultWateringTime()
        ));

        irisClient
              .request(clientRequest)
              .onFailure(saveErrorListener)
              .onSuccess(saveSuccess);
    }

    @Override protected void updateView(final Callback callback) {
        if (!isLoaded() || !irrigationControllers.isLoaded()) {
            logger.warn("Not updating view, subsystem/devices not loaded");
            return;
        }

        List<DeviceModel> controllers = irrigationControllers.get();
        if (controllers == null || controllers.isEmpty()) {
            callback.showDevices(Collections.<LawnAndGardenControllerModel>emptyList());
            logger.error("Tried to show devices, but get() returned null/empty from list source. Attempting to load again.");
            irrigationControllers.load();
            return;
        }

        final List<LawnAndGardenControllerModel> controllerModels = new ArrayList<>(controllers.size());
        for (DeviceModel model : controllers) {
            Map<String, Collection<String>> instances = model.getInstances();
            if (instances == null || instances.isEmpty()) {
                continue;
            }

            controllerModels.add(getControllerModelFor(model, instances));
        }

        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override public void run() {
                callback.showDevices(controllerModels);
            }
        });
    }

    protected void onError(Throwable throwable) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.onError(throwable);
        }
    }

    protected void onSaveError(Throwable throwable) {
        SaveCallback callback = saveCallbackRef.get();
        if (callback != null) {
            callback.onError(throwable);
        }
    }

    protected void onSaveSuccess() {
        SaveCallback saveCallback = saveCallbackRef.get();
        if (saveCallback != null) {
            saveCallback.onSuccess();
        }
    }

    protected LawnAndGardenControllerModel getControllerModelFor(
          DeviceModel model, Map<String,
          Collection<String>> instances
    ) {
        LawnAndGardenControllerModel controllerModel = new LawnAndGardenControllerModel();
        controllerModel.setDeviceAddress(model.getAddress());
        controllerModel.setControllerName(TextUtils.isEmpty(model.getName()) ? "" : model.getName());

        for (String zoneName : instances.keySet()) {
            if (!instances.get(zoneName).contains(IrrigationZone.NAMESPACE)) {
                continue;
            }

            controllerModel.addZoneDetail(getZoneDetailModel(zoneName, model));
        }

        return controllerModel;
    }

    protected LawnAndGardenControllerZoneDetailModel getZoneDetailModel(String zoneName, DeviceModel model) {
        String customerZoneName = (String) model.get(keyNameFor(IrrigationZone.ATTR_ZONENAME, zoneName));
        Number zoneDefaultWateringTime = (Number) model.get(keyNameFor(IrrigationZone.ATTR_DEFAULTDURATION, zoneName));
        if (zoneDefaultWateringTime == null) {
            zoneDefaultWateringTime = 1; // Is this ok to default here?
        }

        Number zoneNumber = (Number) model.get(keyNameFor(IrrigationZone.ATTR_ZONENUM, zoneName));
        if (zoneNumber == null) {
            zoneNumber = 0; // Should this ever be null? Happened on Mock (HT8, not WT15) - Need physical device test.
        }

        LawnAndGardenControllerZoneDetailModel zoneModel = new LawnAndGardenControllerZoneDetailModel();
        zoneModel.setInternalZoneName(zoneName);
        zoneModel.setDeviceAddress(model.getAddress());
        zoneModel.setZoneName(customerZoneName);
        zoneModel.setZoneNumber(zoneNumber.intValue());
        zoneModel.setDefaultWateringTime(zoneDefaultWateringTime.intValue());
        zoneModel.setProductModelID(model.getProductId()); // We using this for product image (more page) or "normal device" image?

        return zoneModel;
    }

    protected String keyNameFor(String valueToFind, String instanceName) {
        return String.format("%s:%s", valueToFind, instanceName);
    }

}
