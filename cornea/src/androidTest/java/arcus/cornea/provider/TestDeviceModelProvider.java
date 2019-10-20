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
package arcus.cornea.provider;

import android.os.Looper;

import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.MoreExecutors;
import arcus.cornea.mock.Fixtures;
import arcus.cornea.mock.MockClient;
import arcus.cornea.mock.MockClientTestCase;
import arcus.cornea.mock.MockListener;
import arcus.cornea.mock.Responses;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelListSource;
import com.iris.client.capability.Device;
import com.iris.client.capability.Place;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.service.SubsystemService;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;


public class TestDeviceModelProvider extends MockClientTestCase {
    UUID placeId = UUID.fromString("d54ce224-7f82-49b6-9e0c-0f4d2090aa2e");
    List<Map<String, Object>> emptyResponse =
            Responses
                    .loadResponse(SubsystemService.ListSubsystemsResponse.class, "place/EmptyListDevicesResponse.json")
                    .getSubsystems();
    List<Map<String, Object>> devicesResponse =
            Responses
                    .loadResponse(SubsystemService.ListSubsystemsResponse.class, "place/ListDevicesResponse.json")
                    .getSubsystems();

    DeviceModelProvider provider;

    String deviceAddress = "DRIV:dev:e81cfc78-fbca-4b1c-a727-2cf59c89198d";

    @Before
    public void setUp() {
        if(Looper.myLooper() == null) {
            Looper.prepare();
        }
        // everything should happen inline to make testing easier, this is valid since all callbacks
        // should happen on the UX thread in a real system, android tests just happen on a background thread
        LooperExecutor.setMainExecutor(MoreExecutors.directExecutor());
        // don't use the cached version, we need to make sure it gets the current client context, etc
        provider = new DeviceModelProvider();
    }

    @Test
    public void testEmptyResponse() throws Exception {
        expectListByPlaceRequest()
                .andRespondFromPath("place/EmptyListDevicesResponse.json");

        assertFalse(provider.isLoaded());

        client().setActivePlace(placeId.toString());

        provider.load().get();
        assertTrue(provider.isLoaded());
        assertEquals(0, provider.getStore().size());
    }

    @Test
    public void testNonEmptyResponse() {
        expectListByPlaceRequest()
                .andRespondFromPath("place/ListDevicesResponse.json");

        assertFalse(provider.isLoaded());

        client().setActivePlace(placeId.toString());

        assertTrue(provider.isLoaded());
        assertEquals(2, provider.getStore().size());
    }

    @Test
    public void testDeviceAdded() {
        expectListByPlaceRequest()
                .andRespondFromPath("place/EmptyListDevicesResponse.json");

        client().setActivePlace(placeId.toString());

        MockListener<ModelEvent> listener = new MockListener<>();
        MockListener<List<DeviceModel>> listListener = new MockListener<>();
        ModelListSource<DeviceModel> source = provider.getModels(ImmutableList.of(deviceAddress));

        source.addModelListener(listener);
        source.addListener(listListener);
        {
            // no added events
            assertFalse(listener.hasEvents());
            // should be loaded with an empty list
            assertEquals(ImmutableList.of(), listListener.poll().get());
            assertFalse(listListener.hasEvents());
        }

        Model model = Fixtures.loadModel("devices/MockBlind.json");
        {
            ModelAddedEvent event = (ModelAddedEvent) listener.poll().get();
            assertEquals(model, event.getModel());
            assertFalse(listener.hasEvents());

            assertEquals(ImmutableList.of(model), listListener.poll().get());
            assertFalse(listListener.hasEvents());
        }
    }

    @Test
    public void testDeviceUpdated() {
        expectListByPlaceRequest()
                .andRespondFromPath("place/ListDevicesResponse.json");

        client().setActivePlace(placeId.toString());
        Model model = Fixtures.loadModel("devices/MockBlind.json");

        MockListener<ModelEvent> listener = new MockListener<>();
        MockListener<List<DeviceModel>> listListener = new MockListener<>();
        ModelListSource<DeviceModel> source = provider.getModels(ImmutableList.of("DRIV:dev:e81cfc78-fbca-4b1c-a727-2cf59c89198d"));

        source.addModelListener(listener);
        source.addListener(listListener);

        // add immediately on register
        {
            ModelAddedEvent event = (ModelAddedEvent) listener.poll().get();
            assertEquals(model, event.getModel());
            assertFalse(listener.hasEvents());

            assertEquals(ImmutableList.of(model), listListener.poll().get());
            assertFalse(listListener.hasEvents());
        }

        // update
        Fixtures.updateModel(model.getAddress(), Device.ATTR_NAME, "A new name");
        {
            ModelChangedEvent event = (ModelChangedEvent) listener.poll().get();
            assertEquals(model, event.getModel());
            assertFalse(listener.hasEvents());

            // this shouldn't affect the list listeners
            assertFalse(listListener.hasEvents());
        }
    }

    @Test
    public void testDeviceRemoved() {
        expectListByPlaceRequest()
                .andRespondFromPath("place/ListDevicesResponse.json");

        client().setActivePlace(placeId.toString());
        Model model = Fixtures.loadModel("devices/MockBlind.json");

        MockListener<ModelEvent> listener = new MockListener<>();
        MockListener<List<DeviceModel>> listListener = new MockListener<>();
        ModelListSource<DeviceModel> source = provider.getModels(ImmutableList.of(deviceAddress));

        source.addModelListener(listener);
        source.addListener(listListener);

        // add immediately on register
        {
            ModelAddedEvent event = (ModelAddedEvent) listener.poll().get();
            assertEquals(model, event.getModel());
            assertFalse(listener.hasEvents());

            assertEquals(ImmutableList.of(model), listListener.poll().get());
            assertFalse(listListener.hasEvents());
        }

        // update
        Fixtures.deleteModel(deviceAddress);
        {
            ModelDeletedEvent event = (ModelDeletedEvent) listener.poll().get();
            assertEquals(model, event.getModel());
            assertFalse(listener.hasEvents());

            assertEquals(ImmutableList.of(), listListener.poll().get());
            assertFalse(listListener.hasEvents());
        }
    }

    @Test
    public void testIgnoreOtherDevices() {
        expectListByPlaceRequest()
                .andRespondFromPath("place/ListDevicesResponse.json");

        client().setActivePlace(placeId.toString());
        Model model = Fixtures.loadModel("devices/MockDimmer.json");

        MockListener<ModelEvent> listener = new MockListener<>();
        MockListener<List<DeviceModel>> listListener = new MockListener<>();
        ModelListSource<DeviceModel> source = provider.getModels(ImmutableList.of(deviceAddress));

        source.addModelListener(listener);
        source.addListener(listListener);

        // add immediately on register
        {
            ModelAddedEvent event = (ModelAddedEvent) listener.poll().get();
            assertNotNull(event.getModel());
            assertFalse(listener.hasEvents());

            assertEquals(1, listListener.poll().get().size());
            assertFalse(listListener.hasEvents());
        }

        Fixtures.updateModel(model.getAddress(), Device.ATTR_NAME, "A new name");
        Fixtures.deleteModel(model.getAddress());
        Fixtures.loadModel("devices/MockDimmer.json");

        assertFalse(listener.hasEvents());
        assertFalse(listListener.hasEvents());
    }

    protected MockClient.ClientResponseBuilder expectListByPlaceRequest() {
        return expectRequest("SERV:" + Place.NAMESPACE + ":" + placeId.toString(), Place.ListDevicesRequest.NAME);
    }
}
