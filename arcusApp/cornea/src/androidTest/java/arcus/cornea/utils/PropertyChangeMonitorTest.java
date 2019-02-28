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
package arcus.cornea.utils;

import android.os.Looper;

import com.google.common.base.Function;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.mock.Fixtures;
import arcus.cornea.mock.MockClientTestCase;
import com.iris.client.ClientMessage;
import com.iris.client.capability.Capability;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;

public class PropertyChangeMonitorTest extends MockClientTestCase {
    static final String DEV_ADDRESS = "DRIV:dev:93b273b4-269c-4121-b5fc-58c4a30f622d";
    static final String ATTRIBUTE   = "indicator:enabled";
    static final String WRONG_ATTRIBUTE = "devpow:linecapable";

    PropertyChangeMonitor monitor;
    @Mock PropertyChangeMonitor.Callback callback;
    @Mock Function<String, ?> function;

    @Before public void setUp() {
        Fixtures.loadModel("devices/MockDimmer.json");

        MockitoAnnotations.initMocks(PropertyChangeMonitorTest.this);
        monitor = new PropertyChangeMonitor(client(), Looper.getMainLooper());
    }

    @After public void tearDown() {
        CorneaClientFactory.getModelCache().clearCache();
    }

    @Test public void clearsOnPlaceReset() throws Exception {
        monitor.startMonitorFor(DEV_ADDRESS, ATTRIBUTE, 5_000, callback, null, null);
        assertTrue(monitor.hasAnyChangesFor(DEV_ADDRESS));

        client().logout();

        assertFalse(monitor.hasAnyChangesFor(DEV_ADDRESS));
        Mockito.verifyZeroInteractions(callback);
    }

    @Test public void successfullyUpdatesAndRemovesMonitor() throws Exception {
        monitor.startMonitorFor(DEV_ADDRESS, ATTRIBUTE, 1_000, callback, null, null);
        assertTrue(monitor.hasAnyChangesFor(DEV_ADDRESS));

        receiveValueChange(ATTRIBUTE, true);

        Mockito.verify(callback, Mockito.timeout(1_000).times(1)).requestSucceeded(DEV_ADDRESS, ATTRIBUTE);
        assertFalse(monitor.hasAnyChangesFor(DEV_ADDRESS));
    }

    @Test public void doesNotUpdateOnWrongAttribute() throws Exception {
        monitor.startMonitorFor(DEV_ADDRESS, ATTRIBUTE, 500, callback, null, null);
        assertTrue(monitor.hasAnyChangesFor(DEV_ADDRESS));

        receiveValueChange(WRONG_ATTRIBUTE, true);

        Mockito.verify(callback, Mockito.timeout(550).times(1)).requestTimedOut(DEV_ADDRESS, ATTRIBUTE);
        assertFalse(monitor.hasAnyChangesFor(DEV_ADDRESS));
    }

    @Test public void doesNotUpdateOnMismatchedAttributeValue() throws Exception {
        monitor.startMonitorFor(DEV_ADDRESS, ATTRIBUTE, 500, callback, false, null);
        assertTrue(monitor.hasAnyChangesFor(DEV_ADDRESS));

        receiveValueChange(ATTRIBUTE, true); // We're expecting false above.

        Mockito.verify(callback, Mockito.timeout(550).times(1)).requestTimedOut(DEV_ADDRESS, ATTRIBUTE);
        assertFalse(monitor.hasAnyChangesFor(DEV_ADDRESS));
    }

    @Test public void doesUpdateOnMatchedAttributeValue() throws Exception {
        monitor.startMonitorFor(DEV_ADDRESS, ATTRIBUTE, 500, callback, false, null);
        assertTrue(monitor.hasAnyChangesFor(DEV_ADDRESS));

        receiveValueChange(ATTRIBUTE, false);

        Mockito.verify(callback, Mockito.timeout(550).times(1)).requestSucceeded(DEV_ADDRESS, ATTRIBUTE);
        assertFalse(monitor.hasAnyChangesFor(DEV_ADDRESS));
    }

    @Test public void altFunctionIsCalledOnFailure() throws Exception {
        monitor.startMonitorFor(DEV_ADDRESS, ATTRIBUTE, 5, null, null, function);
        assertTrue(monitor.hasAnyChangesFor(DEV_ADDRESS));

        Mockito.verify(function, Mockito.timeout(10).times(1)).apply(DEV_ADDRESS);

        assertFalse(monitor.hasAnyChangesFor(DEV_ADDRESS));
    }

    @Test public void hasChangesFor() throws Exception {
        monitor.startMonitorFor(DEV_ADDRESS, ATTRIBUTE, 5, callback, null, null);
        assertTrue(monitor.hasAnyChangesFor(DEV_ADDRESS));
        Mockito.verify(callback, Mockito.timeout(10).times(1)).requestTimedOut(DEV_ADDRESS, ATTRIBUTE);
    }

    @Test public void isRemovedAfterFailure() throws Exception {
        monitor.startMonitorFor(DEV_ADDRESS, ATTRIBUTE, 5, callback, null, null);

        assertTrue(monitor.hasAnyChangesFor(DEV_ADDRESS));
        Mockito.verify(callback, Mockito.timeout(10).times(1)).requestTimedOut(DEV_ADDRESS, ATTRIBUTE);

        assertFalse(monitor.hasAnyChangesFor(DEV_ADDRESS));
    }

    @Test public void removesOnDemand() throws Exception {
        monitor.startMonitorFor(DEV_ADDRESS, ATTRIBUTE, 5, callback, null, null);
        assertTrue(monitor.hasAnyChangesFor(DEV_ADDRESS));

        monitor.removeAllFor(DEV_ADDRESS);
        assertFalse(monitor.hasAnyChangesFor(DEV_ADDRESS));
    }

    protected void receiveValueChange(String attribute, Object value) {
        client().received(
              ClientMessage.builder()
                    .withType(Capability.ValueChangeEvent.NAME)
                    .withSource(DEV_ADDRESS)
                    .isRequest(false)
                    .withAttribute(attribute, value)
                    .create()
        );
    }
}