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

import com.google.common.base.Optional;
import arcus.cornea.mock.MockClientTestCase;
import arcus.cornea.mock.MockListener;
import com.iris.client.ClientMessage;
import com.iris.client.capability.Capability;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.capability.Place;

import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class CareBehaviorsProviderTest extends MockClientTestCase {

    static final String CARE_ADDRESS = "SERV:subcare:2e37d90f-a1df-491f-b5f2-242f4f4833c4";
    CareBehaviorsProvider provider;

    @Before public void setUp() throws Exception {
        provider = new CareBehaviorsProvider();
    }

    @Test public void testNormalLoad() throws Exception {
        baseWithBehaviors();
    }

    @Test public void testEmptyLoad() throws Exception {
        expectRequestOfType(CareSubsystem.ListBehaviorsRequest.NAME)
              .andRespondFromPath("subsystems/care/listBehaviorsResponse.json");

        assertFalse(provider.isLoaded());

        provider.setSubsystemAddress(CARE_ADDRESS);
        provider.load().get();
        assertTrue(provider.isLoaded());

        Optional<List<Map<String, Object>>> results = provider.getAll();
        assertTrue(results.isPresent());
        assertFalse(results.get().isEmpty());
    }

    @Test public void testReloadsOnAddDevice() throws Exception {
        baseWithBehaviors();

        final MockListener<List<Map<String, Object>>> mockListener = new MockListener<>();
        provider.addItemsLoadedListener(mockListener, false);
        assertFalse(mockListener.hasEvents());

        expectRequestOfType(Place.ListDevicesRequest.NAME)
              .andRespondWithMessage(
                    ClientMessage.builder()
                          .withType(Capability.EVENT_ADDED)
                          .withSource("DRIV:dev:8de71942-1090-4c6b-a5a1-3a0aaaa4d58c")
                          .create()
              );
        client().request(new Place.ListDevicesRequest());

        Optional<List<Map<String, Object>>> newQuery = mockListener.poll();
        assertTrue(newQuery.isPresent());
        assertNotNull(newQuery.get());
        assertFalse(newQuery.get().isEmpty());
    }

    @Test public void testReloadsOnDeleteDevice() throws Exception {
        baseWithBehaviors();

        final MockListener<List<Map<String, Object>>> mockListener = new MockListener<>();
        provider.addItemsLoadedListener(mockListener, false);
        assertFalse(mockListener.hasEvents());

        expectRequestOfType(Place.ListDevicesRequest.NAME)
              .andRespondWithMessage(
                    ClientMessage.builder()
                          .withType(Capability.EVENT_DELETED)
                          .withSource("DRIV:dev:8de71942-1090-4c6b-a5a1-3a0aaaa4d58c")
                          .create()
              );
        client().request(new Place.ListDevicesRequest());

        Optional<List<Map<String, Object>>> newQuery = mockListener.poll();
        assertTrue(newQuery.isPresent());
        assertNotNull(newQuery.get());
        assertFalse(newQuery.get().isEmpty());
    }

    @Test public void testClearsOnSessionExpired() throws Exception {
        baseWithBehaviors();

        client().logout();
        assertFalse(provider.isLoaded());
    }

    @Test public void testClearsOnSessionExpiredAndReload() throws Exception {
        baseWithBehaviors();

        client().logout();
        assertFalse(provider.isLoaded());

        baseWithBehaviors();
    }

    protected void baseWithBehaviors() throws Exception {
        expectRequestOfType(CareSubsystem.ListBehaviorsRequest.NAME)
              .andRespondFromPath("subsystems/care/listBehaviorsResponse.json");

        assertFalse(provider.isLoaded());

        provider.setSubsystemAddress(CARE_ADDRESS);
        provider.load().get();
        assertTrue(provider.isLoaded());

        Optional<List<Map<String, Object>>> results = provider.getAll();
        assertTrue(results.isPresent());
        assertFalse(results.get().isEmpty());
    }
}