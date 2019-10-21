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
package arcus.cornea.subsystem;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.mock.MockClientTestCase;
import arcus.cornea.mock.MockListener;
import arcus.cornea.mock.Responses;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.SafetySubsystem;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.Store;
import com.iris.client.model.SubsystemModel;
import com.iris.client.service.SubsystemService;
import com.iris.client.session.SessionActivePlaceSetEvent;
import com.iris.client.session.SessionExpiredEvent;

import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.fail;


public class TestSubsystemController extends MockClientTestCase {
    UUID placeId = UUID.randomUUID();
    List<Map<String, Object>> subsystemModels =
            Responses
                .loadResponse(SubsystemService.ListSubsystemsResponse.class, "subsystems/ListSubsystemsResponse.json")
                .getSubsystems();

    Store<SubsystemModel> subsystems;
    SubsystemController controller;

    @Before
    public void setUp() {
        subsystems = CorneaClientFactory.getStore(SubsystemModel.class);
        controller = new SubsystemController(client(), subsystems);
    }

    protected void expectLoadByPlaceThenReturn(Collection<Map<String, Object>> subsystems) {
        SubsystemService.ListSubsystemsRequest request = new SubsystemService.ListSubsystemsRequest();
        request.setPlaceId(placeId.toString());
        request.setAddress("SERV:" + SubsystemService.NAMESPACE + ":");

        expectRequest(
                request.getAddress(),
                request.getCommand(),
                request.getAttributes()
        )
        .andRespondFromPath("subsystems/ListSubsystemsResponse.json");
    }

    protected void expectLoadByPlaceThenReturn() {
        expectLoadByPlaceThenReturn(subsystemModels);
    }

    @Test
    public void testLoadAndClear() throws Exception {
        expectLoadByPlaceThenReturn();

        client().setActivePlace(placeId.toString()).get();

        assertEquals(subsystemModels.size(), subsystems.size());

        client().logout().get();

        assertEquals(0, subsystems.size());
    }

    @Test
    public void testLoadWithoutPlace() throws Exception {
        MockListener<ModelEvent> listener = new MockListener<>();
        expectLoadByPlaceThenReturn();

        // getSecuritySubsystem model before load
        ModelSource<SubsystemModel> ref = controller.getSubsystemModel(SafetySubsystem.NAMESPACE);
        ref.addModelListener(listener);

        assertFalse(ref.isLoaded());
        assertFalse(listener.hasEvents());

        // request an initial load, should load the whole store
        try {
            ref.load().get();
            fail("Allowed getSecuritySubsystem with no place");
        }
        catch(Exception e) {
            // expected
        }
    }

    @Test
    public void testLoadOnPlaceActivated() throws Exception {
        MockListener<ModelEvent> listener = new MockListener<>();
        expectLoadByPlaceThenReturn();

        // getSecuritySubsystem model before load
        ModelSource<SubsystemModel> ref = controller.getSubsystemModel(SafetySubsystem.NAMESPACE);
        ref.addModelListener(listener);

        assertFalse(ref.isLoaded());
        assertFalse(listener.hasEvents());

        controller.onPlaceSelected(new SessionActivePlaceSetEvent(placeId));

        assertTrue(ref.isLoaded());
        ModelEvent event = listener.poll().get();
        assertTrue(event instanceof ModelAddedEvent);
        assertEquals(ref.get(), event.getModel());
        assertFalse(listener.hasEvents());

        // calling load again should be a no-op
        assertEquals(ref.get(), ref.load().get());
        assertFalse(listener.hasEvents());
    }

    @Test
    public void testLoadAfterPlaceActivated() throws Exception {
        MockListener<ModelEvent> listener = new MockListener<>();
        expectLoadByPlaceThenReturn();

        controller.onPlaceSelected(new SessionActivePlaceSetEvent(placeId));

        // getSecuritySubsystem model after load
        ModelSource<SubsystemModel> ref = controller.getSubsystemModel(SafetySubsystem.NAMESPACE);
        ref.addModelListener(listener);

        assertTrue(ref.isLoaded());
        ModelEvent event = listener.poll().get();
        assertTrue(event instanceof ModelAddedEvent);
        assertEquals(ref.get(), event.getModel());
        assertFalse(listener.hasEvents());

        // calling load again should be a no-op
        assertEquals(ref.get(), ref.load().get());
        assertFalse(listener.hasEvents());
    }

    @Test
    public void testReload() throws Exception {
        MockListener<ModelEvent> listener = new MockListener<>();
        expectLoadByPlaceThenReturn();

        controller.onPlaceSelected(new SessionActivePlaceSetEvent(placeId));

        // getSecuritySubsystem model after load
        ModelSource<SubsystemModel> ref = controller.getSubsystemModel(SafetySubsystem.NAMESPACE);
        ref.addModelListener(listener);

        {
            assertTrue(ref.isLoaded());
            ModelEvent event = listener.poll().get();
            assertTrue(event instanceof ModelAddedEvent);
            assertEquals(ref.get(), event.getModel());
            assertFalse(listener.hasEvents());
        }

        // calling load again should call it again
        assertEquals(ref.get(), ref.reload().get());
        {
            ModelEvent event = listener.poll().get();
            assertEquals(ModelChangedEvent.class, event.getClass());
            assertEquals(ref.get(), event.getModel());
            assertFalse(listener.hasEvents());
        }
    }

    @Test
    public void testClearThenLoad() throws Exception {
        MockListener<ModelEvent> listener = new MockListener<>();
        expectLoadByPlaceThenReturn();

        controller.onPlaceSelected(new SessionActivePlaceSetEvent(placeId));

        // getSecuritySubsystem model after load
        ModelSource<SubsystemModel> ref = controller.getSubsystemModel(SafetySubsystem.NAMESPACE);
        ref.addModelListener(listener);

        {
            assertTrue(ref.isLoaded());
            ModelEvent event = listener.poll().get();
            assertTrue(event instanceof ModelAddedEvent);
            assertEquals(ref.get(), event.getModel());
            assertFalse(listener.hasEvents());
        }

        controller.onSessionExpired(new SessionExpiredEvent());

        assertFalse(ref.isLoaded());
        {
            ModelEvent event = listener.poll().get();
            assertEquals(ModelDeletedEvent.class, event.getClass());
            assertFalse(listener.hasEvents());
        }

        // calling load again should load it
        SubsystemModel m = ref.load().get();
        {
            ModelEvent event = listener.poll().get();
            // FIXME the cache fires a ModelChangedEvent when a deleted model is re-added, weird
//            assertEquals(ModelAddedEvent.class, event.getClass());
            assertFalse(listener.hasEvents());
        }

    }

}
