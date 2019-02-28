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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.mock.Fixtures;
import arcus.cornea.mock.MockClientTestCase;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.lawnandgarden.model.LawnAndGardenControllerModel;
import arcus.cornea.subsystem.lawnandgarden.model.LawnAndGardenControllerZoneDetailModel;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.SettableModelSource;
import com.iris.client.ClientEvent;
import com.iris.client.capability.Capability;
import com.iris.client.capability.IrrigationZone;
import com.iris.client.event.Futures;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.SubsystemModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Captor;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class LawnAndGardenDeviceMoreControllerTest extends MockClientTestCase {
    static final String ONE_ZONE_DEVICE_JSON    = "subsystems/lawnandgarden/1ZoneController.json";
    static final String ONE_ZONE_DEVICE_ADDRESS = "DRIV:dev:74df7e8d-2053-4b76-bb8f-e2fcf2b81464";
    static final String ONE_ZONE_SUBSYSTEM_JSON = "subsystems/lawnandgarden/subsystem1Zone.json";

    static final String NO_DEVICES_SUBSYSTEM_JSON = "subsystems/lawnandgarden/subsystemNoDevices.json";

    static final String TWELVE_ZONE_DEVICE_JSON    = "subsystems/lawnandgarden/12ZoneController.json";
    static final String TWELVE_ZONE_DEVICE_ADDRESS = "DRIV:dev:90909fac-fa06-400d-9cb8-f7234b58aa00";
    static final String TWELVE_ZONE_SUBSYSTEM_JSON = "subsystems/lawnandgarden/subsystemNormal.json";

    final Map<String, Object> changesToOneZone = ImmutableMap.<String, Object>of(
          IrrigationZone.ATTR_ZONENAME + ":z1", "New Zone Name",
          IrrigationZone.ATTR_DEFAULTDURATION + ":z1", 15
    );

    LawnAndGardenDeviceMoreController controller;
    SettableModelSource<SubsystemModel> source;
    AddressableListSource<DeviceModel> devices;

    @Captor ArgumentCaptor<List<LawnAndGardenControllerModel>> showDevicesCaptor;
    @Captor ArgumentCaptor<List<LawnAndGardenControllerModel>> secondShowDevicesCaptor;

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);
        Fixtures.loadModel(TWELVE_ZONE_DEVICE_JSON);
        source = new SettableModelSource<>();
        devices = DeviceModelProvider.instance().getModels(ImmutableSet.of(TWELVE_ZONE_DEVICE_ADDRESS));
        controller = new LawnAndGardenDeviceMoreController(source, devices, client());
    }

    @Test public void subsystemLoadsNormally() throws Exception {
        source.set((SubsystemModel) Fixtures.loadModel(TWELVE_ZONE_SUBSYSTEM_JSON));
        LawnAndGardenDeviceMoreController.Callback cb = Mockito.mock(LawnAndGardenDeviceMoreController.Callback.class);
        controller.setCallback(cb);

        Mockito.verify(cb, Mockito.times(1)).showDevices(showDevicesCaptor.capture());

        List<LawnAndGardenControllerModel> models = showDevicesCaptor.getValue();
        assertFalse(models.isEmpty());
        assertEquals(1, models.size());
        assertEquals(12, models.get(0).getZoneCount());

        Mockito.verifyNoMoreInteractions(cb);
    }

    @Test public void subsystemLoadsNoDevices() throws Exception {
        CorneaClientFactory.getModelCache().clearCache();
        devices = DeviceModelProvider.instance().getModels(ImmutableSet.<String>of());
        controller = new LawnAndGardenDeviceMoreController(source, devices, client());
        source.set((SubsystemModel) Fixtures.loadModel(NO_DEVICES_SUBSYSTEM_JSON));
        LawnAndGardenDeviceMoreController.Callback cb = Mockito.mock(LawnAndGardenDeviceMoreController.Callback.class);
        controller.setCallback(cb);

        Mockito.verify(cb, Mockito.times(1)).showDevices(showDevicesCaptor.capture());

        List<LawnAndGardenControllerModel> models = showDevicesCaptor.getValue();
        assertTrue(models.isEmpty());

        Mockito.verifyNoMoreInteractions(cb);
    }


    @Test public void subsystemLoads1ZoneNormally() throws Exception {
        Fixtures.loadModel(ONE_ZONE_DEVICE_JSON);

        devices = DeviceModelProvider.instance().getModels(ImmutableSet.of(ONE_ZONE_DEVICE_ADDRESS));
        controller = new LawnAndGardenDeviceMoreController(source, devices, client());
        source.set((SubsystemModel) Fixtures.loadModel(ONE_ZONE_SUBSYSTEM_JSON));
        LawnAndGardenDeviceMoreController.Callback cb = Mockito.mock(LawnAndGardenDeviceMoreController.Callback.class);
        controller.setCallback(cb);

        Mockito.verify(cb, Mockito.times(1)).showDevices(showDevicesCaptor.capture());

        List<LawnAndGardenControllerModel> models = showDevicesCaptor.getValue();
        assertFalse(models.isEmpty());
        assertEquals(1, models.size());
        assertEquals(1, models.get(0).getZoneCount());

        Mockito.verifyNoMoreInteractions(cb);
    }

    @Test public void subsystemUpdatesControllerNormally() throws Exception {
        Fixtures.loadModel(ONE_ZONE_DEVICE_JSON);

        devices = DeviceModelProvider.instance().getModels(ImmutableSet.of(ONE_ZONE_DEVICE_ADDRESS));
        controller = new LawnAndGardenDeviceMoreController(source, devices, client());
        source.set((SubsystemModel) Fixtures.loadModel(ONE_ZONE_SUBSYSTEM_JSON));
        LawnAndGardenDeviceMoreController.Callback cb = Mockito.mock(LawnAndGardenDeviceMoreController.Callback.class);
        controller.setCallback(cb);

        Mockito.verify(cb, Mockito.times(1)).showDevices(showDevicesCaptor.capture());

        List<LawnAndGardenControllerModel> models = showDevicesCaptor.getValue();
        assertFalse(models.isEmpty());
        assertEquals(1, models.size());
        assertEquals(1, models.get(0).getZoneCount());

        // Get first controllers first zone
        LawnAndGardenControllerZoneDetailModel originalDetail = models.get(0).getZoneDetails().get(0);
        assertNotNull(originalDetail);
        assertEquals(0, originalDetail.getDefaultWateringTime());

        LawnAndGardenDeviceMoreController.SaveCallback scb = Mockito.mock(LawnAndGardenDeviceMoreController.SaveCallback.class);
        controller.setSaveCallback(scb);

        // Expect that the client will send a "Set Attributes" and respond with an empty message. (Successful Response)
        ClientEvent response = new ClientEvent("EmptyMessage", ONE_ZONE_DEVICE_ADDRESS);
        client()
              .expectRequest(ONE_ZONE_DEVICE_ADDRESS, Capability.CMD_SET_ATTRIBUTES, changesToOneZone)
              .andReturn(Futures.succeededFuture(response));

        originalDetail.setDefaultWateringTime(15);
        originalDetail.setZoneName("New Zone Name");
        controller.updateZone(originalDetail);

        // Save Should be called 1 time
        Mockito.verify(scb, Mockito.times(1)).onSuccess();
        Mockito.verifyNoMoreInteractions(scb);

        // "Receive" a Value Change from the platform.
        Fixtures.updateModel(ONE_ZONE_DEVICE_ADDRESS, changesToOneZone);

        // Show devices should be called again after we receive an update for a zone/controller. (b/c of addressable source listener)
        Mockito.verify(cb, Mockito.times(1)).showDevices(secondShowDevicesCaptor.capture());
        List<LawnAndGardenControllerModel> newModels = secondShowDevicesCaptor.getValue();
        assertFalse(newModels.isEmpty());
        assertEquals(1, newModels.size());
        assertEquals(1, newModels.get(0).getZoneCount());

        // New zone details should be updated whe we're given the models again
        LawnAndGardenControllerZoneDetailModel newDetails = newModels.get(0).getZoneDetails().get(0);
        assertNotNull(newDetails);
        assertEquals(15, newDetails.getDefaultWateringTime());
        assertEquals("New Zone Name", newDetails.getZoneName());

        Mockito.verifyNoMoreInteractions(cb);
        Mockito.verifyNoMoreInteractions(scb);
    }

    @Test public void subsystemUpdatesFail() throws Exception {
        Fixtures.loadModel(ONE_ZONE_DEVICE_JSON);

        devices = DeviceModelProvider.instance().getModels(ImmutableSet.of(ONE_ZONE_DEVICE_ADDRESS));
        controller = new LawnAndGardenDeviceMoreController(source, devices, client());
        source.set((SubsystemModel) Fixtures.loadModel(ONE_ZONE_SUBSYSTEM_JSON));
        LawnAndGardenDeviceMoreController.Callback cb = Mockito.mock(LawnAndGardenDeviceMoreController.Callback.class);
        controller.setCallback(cb);

        Mockito.verify(cb, Mockito.times(1)).showDevices(showDevicesCaptor.capture());

        List<LawnAndGardenControllerModel> models = showDevicesCaptor.getValue();
        assertFalse(models.isEmpty());
        assertEquals(1, models.size());
        assertEquals(1, models.get(0).getZoneCount());

        // Get first controllers first zone
        LawnAndGardenControllerZoneDetailModel originalDetail = models.get(0).getZoneDetails().get(0);
        assertNotNull(originalDetail);
        assertEquals(0, originalDetail.getDefaultWateringTime());

        LawnAndGardenDeviceMoreController.SaveCallback scb = Mockito.mock(LawnAndGardenDeviceMoreController.SaveCallback.class);
        controller.setSaveCallback(scb);

        // Expect that the client will send a "Set Attributes" and respond with an empty message. (Successful Response)
        ClientEvent response = new ClientEvent("EmptyMessage", ONE_ZONE_DEVICE_ADDRESS);
        client()
              .expectRequest(ONE_ZONE_DEVICE_ADDRESS, Capability.CMD_SET_ATTRIBUTES, changesToOneZone)
              .andReturn(Futures.<ClientEvent>failedFuture(new RuntimeException("Failed to update.")));

        originalDetail.setDefaultWateringTime(15);
        originalDetail.setZoneName("New Zone Name");
        controller.updateZone(originalDetail);

        // onError Should be called 1 time, since we failed to update.
        Mockito.verify(scb, Mockito.times(1)).onError(Matchers.any(Exception.class));

        Mockito.verifyNoMoreInteractions(cb);
        Mockito.verifyNoMoreInteractions(scb);
    }

    @Test public void listenersDeregister() throws Exception {
        source.set((SubsystemModel) Fixtures.loadModel(TWELVE_ZONE_SUBSYSTEM_JSON));
        LawnAndGardenDeviceMoreController.Callback cb = Mockito.mock(LawnAndGardenDeviceMoreController.Callback.class);
        LawnAndGardenDeviceMoreController.SaveCallback scb = Mockito.mock(LawnAndGardenDeviceMoreController.SaveCallback.class);
        ListenerRegistration cbReg = controller.setCallback(cb);
        ListenerRegistration scbReg = controller.setSaveCallback(scb);

        assertTrue(cbReg.isRegistered());
        assertTrue(scbReg.isRegistered());

        Listeners.clear(cbReg);
        Listeners.clear(scbReg);

        assertFalse(cbReg.isRegistered());
        assertFalse(scbReg.isRegistered());
    }
}