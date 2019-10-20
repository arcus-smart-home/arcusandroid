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
package arcus.cornea.device.hub;

import com.google.common.collect.ImmutableMap;
import arcus.cornea.mock.Fixtures;
import arcus.cornea.mock.MockClientTestCase;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.SettableModelSource;
import com.iris.client.capability.Capability;
import com.iris.client.capability.HubConnection;
import com.iris.client.capability.HubPower;
import com.iris.client.capability.HubVolume;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.HubModel;
import com.iris.client.model.SubsystemModel;
import com.iris.client.service.SubsystemService;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class HubControllerTest extends MockClientTestCase {
    private static final String HUB_ID = "LWW-1140";
    private static final long lastChangedTime = 1468925042462L;

    @Mock HubController.Callback callback;
    SettableModelSource<HubModel> hubSource;
    HubController hubController;
    HubProxyModel hubProxyModel = new HubProxyModel(HUB_ID);

    @Before public void setupTest() {
        expectRequestOfType(SubsystemService.ListSubsystemsRequest.NAME)
              .andRespondFromPath("responses/listSubsystemsResponse.json");

        hubSource = new SettableModelSource<>();
        hubSource.set((HubModel) Fixtures.loadModel("devices/Hub.json"));
        hubSource.load();
        hubController = new HubController(hubSource.get(), new SettableModelSource<SubsystemModel>());
        hubController.setCallback(callback);
    }

    @Test public void testSetClearListeners() throws Exception {
        ListenerRegistration listenerRegistration = hubController.setCallback(callback);

        assertNotNull(listenerRegistration);
        assertTrue(listenerRegistration.isRegistered());
        assertTrue(hubController.modelListenerRegistered());
        assertTrue(hubController.subsystemListenerRegistered());

        Listeners.clear(listenerRegistration);
        assertFalse(listenerRegistration.isRegistered());
        assertFalse(hubController.modelListenerRegistered());
        assertFalse(hubController.subsystemListenerRegistered());
    }

    @Test public void testErrorListener() throws Exception {
        hubController = new HubController(null, new SettableModelSource<SubsystemModel>());
        hubController.setCallback(callback);
        hubController.load();

        Mockito.verify(callback, Mockito.times(1)).onError(Matchers.any(Throwable.class));
        Mockito.verifyNoMoreInteractions(callback);
    }

    @Test public void testLoad() throws Exception {
        hubController.load();

        Mockito.verify(callback, Mockito.timeout(1_000).times(1)).show(hubProxyModel);
        Mockito.verifyNoMoreInteractions(callback);
    }

    @Test public void testPropertyUpdate() throws Exception {
        Fixtures.updateModel(hubSource.get().getAddress(),
              ImmutableMap.<String, Object>of(HubConnection.ATTR_STATE, HubConnection.STATE_PAIRING)
        );

        Mockito.verify(callback, Mockito.timeout(1_000).times(1)).show(hubProxyModel);
        Mockito.verifyNoMoreInteractions(callback);
    }

    @Test public void testMultiplePropertyUpdate() throws Exception {
        Fixtures.updateModel(hubSource.get().getAddress(),
              ImmutableMap.<String, Object>of(
                    HubConnection.ATTR_STATE, HubConnection.STATE_PAIRING,
                    HubPower.ATTR_SOURCE, HubPower.ATTR_BATTERY
              )
        );

        // Do we want to throttle this?
        Mockito.verify(callback, Mockito.timeout(1_000).times(2)).show(hubProxyModel);
        Mockito.verifyNoMoreInteractions(callback);
    }

    @Test public void testRefresh() throws Exception {
        expectRequestOfType(Capability.CMD_GET_ATTRIBUTES)
              .andRespondWithMessage( // Change something so we emit an event
                    Capability.EVENT_GET_ATTRIBUTES_RESPONSE,
                    ImmutableMap.<String, Object>of(HubVolume.ATTR_VOLUME, HubVolume.VOLUME_MID)
              );

        hubController.refresh();

        Mockito.verify(callback, Mockito.timeout(1_000).times(1)).show(hubProxyModel);
        Mockito.verifyNoMoreInteractions(callback);
    }

    @Test public void testGetOnlineDays() throws Exception {
        long expected = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastChangedTime);
        long actual   = hubController.getOnlineDays();
        assertEquals(expected, actual);
    }

    @Test public void testGetOnlineHours() throws Exception {
        long expected = TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - lastChangedTime) % 24;
        long actual   = hubController.getOnlineHours();
        assertEquals(expected, actual);
    }

    @Test public void testGetOnlineMinutes() throws Exception {
        long expected = TimeUnit.MILLISECONDS.toMinutes(System.currentTimeMillis() - lastChangedTime) % 60;
        long actual   = hubController.getOnlineMinutes();
        assertEquals(expected, actual);
    }

    @Test public void testGetBatteryLevel() throws Exception {
        double battery = hubController.getBatteryLevel();
        assertEquals(98, battery, .5); // +/- 1/2 percent.
    }
}