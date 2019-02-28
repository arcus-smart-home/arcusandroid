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

import com.google.common.collect.ImmutableMap;
import arcus.cornea.mock.Fixtures;
import arcus.cornea.mock.MockClientTestCase;
import arcus.cornea.utils.SettableModelSource;
import com.iris.client.EmptyEvent;
import com.iris.client.ErrorEvent;
import com.iris.client.capability.Atmos;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Device;
import com.iris.client.capability.Dimmer;
import com.iris.client.capability.Halo;
import com.iris.client.capability.Switch;
import com.iris.client.capability.WeatherRadio;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.*;

import static org.mockito.Mockito.*;
import static org.hamcrest.CoreMatchers.equalTo;

public class HaloControllerTest extends MockClientTestCase {
    @Mock HaloController.Callback callback;
    @Captor ArgumentCaptor<DeviceModel> deviceModelCaptor;
    HaloController haloController;
    SettableModelSource<DeviceModel> device;

    @Before public void init() {
        Fixtures.loadModel("devices/Halo.json");
        device = new SettableModelSource<>();
        device.set((DeviceModel) Fixtures.loadModel("devices/Halo.json"));
        device.load();

        haloController = new HaloController(device, client(), HaloPresenter.UPDATE_ON);
        MockitoAnnotations.initMocks(this);
    }

    @Test public void testSetClearCallback() throws Exception {
        ListenerRegistration listenerRegistration = haloController.setCallback(callback);

        assertNotNull(listenerRegistration);
        assertTrue(listenerRegistration.isRegistered());

        listenerRegistration.remove();

        assertNotNull(listenerRegistration);
        assertFalse(listenerRegistration.isRegistered());
        assertNull(haloController.callbackRef.get());
    }

    @Test public void testRefreshModel() throws Exception {
        haloController.setCallback(callback);

        // Since the model is loaded - setting the callback here will cause updateView to be called.
        verify(callback, times(1)).onSuccess(Matchers.<DeviceModel>any());

        // We want to verify when we explicitly request a model refresh - that the update method is called
        reset(callback);  // Reset the invocation counter from the above call - else we'd have to use times(2) here
        haloController.refreshModel();
        verify(callback, times(1)).onSuccess(Matchers.<DeviceModel>any());

        verifyNoMoreInteractions(callback);
    }

    @Test public void testPlayCurrentWeatherStation() throws Exception {
        setCallbackAndResetMock();

        WeatherRadio.PlayStationRequest request = new WeatherRadio.PlayStationRequest();
        request.setAddress(device.get().getAddress());
        request.setTimeoutMs(HaloController.REQUEST_TIMEOUT);
        request.setStation(((Number) device.get().get(WeatherRadio.ATTR_STATIONSELECTED)).intValue());
        request.setTime(HaloContract.PLAY_DURATION_SECONDS);

        expectRequest(request).andRespondWithMessage(EmptyEvent.NAME);

        haloController.playWeatherStation(null, HaloContract.PLAY_DURATION_SECONDS);
        verifyNoMoreInteractions(callback); // Currently nothing happens after this, but we shouldn't get an error.
    }

    @Test public void testSetSwitchOff() throws Exception {
        setCallbackAndResetMock();
        expectRequest(
              device.get().getAddress(),
              Capability.CMD_SET_ATTRIBUTES,
              ImmutableMap.<String, Object>of(
                    Switch.ATTR_STATE, Switch.STATE_ON
              )
        ).andRespondWithMessage(EmptyEvent.NAME); // Validate we send the right message

        haloController.setSwitchOn(true);

        device.update(Switch.ATTR_STATE, Switch.STATE_ON); // We "received" a value change event
        verify(callback, timeout(1_000)).onSuccess(deviceModelCaptor.capture());

        DeviceModel fromCapture = deviceModelCaptor.getValue();
        assertNotNull(fromCapture);

        assertTrue(Switch.STATE_ON.equals(fromCapture.get(Switch.ATTR_STATE)));
        verifyNoMoreInteractions(callback);
    }

    @Test public void testUpdateAfterEvent() throws Exception {
        setCallbackAndResetMock();

        device.update(Switch.ATTR_STATE, Switch.STATE_ON);
        // We should update on this since the value changed.
        verify(callback, timeout(1_000)).onSuccess(deviceModelCaptor.capture());

        DeviceModel fromCapture = deviceModelCaptor.getValue();
        assertNotNull(fromCapture);
        assertTrue(Switch.STATE_ON.equals(fromCapture.get(Switch.ATTR_STATE)));
        verifyNoMoreInteractions(callback);
    }

    @Test public void testUpdateAfterMultipleValuesChanged() throws Exception {
        setCallbackAndResetMock();

        // Something from the patform just changed w/o provocation from the user.
        device.update(ImmutableMap.<String, Object>of(
              Switch.ATTR_STATE, Switch.STATE_ON,
              Atmos.ATTR_PRESSURE, 23.48348d,
              Device.ATTR_NAME, "Fourth Floor - Bedroom"
        ));

        // We should update on this since the value changed.
        verify(callback, timeout(1_000)).onSuccess(deviceModelCaptor.capture());

        DeviceModel fromCapture = deviceModelCaptor.getValue();
        assertNotNull(fromCapture);

        assertTrue(Switch.STATE_ON.equals(fromCapture.get(Switch.ATTR_STATE)));

        Number atmos = (Number) fromCapture.get(Atmos.ATTR_PRESSURE);
        assertNotNull(atmos);
        assertEquals(atmos.doubleValue(), 23.48348d, .125); // .125 is the delta
        assertThat(fromCapture.getName(), equalTo("Fourth Floor - Bedroom"));
        verifyNoMoreInteractions(callback);
    }

    @Test public void testSetDimmer() throws Exception {
        setCallbackAndResetMock();
        expectRequest(
              device.get().getAddress(),
              Capability.CMD_SET_ATTRIBUTES,
              ImmutableMap.<String, Object>of(
                    Dimmer.ATTR_BRIGHTNESS, 30
              )
        ).andRespondWithMessage(EmptyEvent.NAME); // To validate the correct message is sent

        Number currentBrightness = (Number) device.get().get(Dimmer.ATTR_BRIGHTNESS);
        assertNotNull(currentBrightness);
        assertThat(currentBrightness.intValue(), not(equalTo(30)));

        haloController.setDimmer(10);
        haloController.setDimmer(20);
        Thread.sleep(200);
        haloController.setDimmer(30);
        haloController.setDimmer(40);
        haloController.setDimmer(50);
        haloController.setDimmer(40);
        Thread.sleep(450); // Pause in between as well - we are alllllmost at that 30% we want!
        haloController.setDimmer(30); // Yay, we got it

        verify(callback, timeout(1_000)).onSuccess(deviceModelCaptor.capture());

        // Make sure we only sent the last 30
        DeviceModel fromCapture = deviceModelCaptor.getValue();
        assertNotNull(fromCapture);

        Number brightness = (Number) fromCapture.get(Dimmer.ATTR_BRIGHTNESS);
        assertNotNull(brightness);
        assertThat(brightness.intValue(), equalTo(30));
        verifyNoMoreInteractions(callback);
    }

    @Test public void testShowError() throws Exception {
        setCallbackAndResetMock();
        expectRequest(
              device.get().getAddress(),
              Capability.CMD_SET_ATTRIBUTES,
              ImmutableMap.<String, Object>of(
                    Switch.ATTR_STATE, Switch.STATE_ON
              )
        )
        .andRespondWithError(
              new ErrorEvent("FROM_THE_PLATFORM",
                    ImmutableMap.<String, Object>of(
                          "code","you.dirty.rat",
                          "message", "You dirty rat" // Yeah yeah... it's not what Cagney actually said...
                    )
              )
        );

        haloController.setSwitchOn(true);
        verify(callback).onError(Matchers.<Exception>any());
        verifyNoMoreInteractions(callback);
    }

    @Test public void testTestDeviceCall() throws Exception {
        setCallbackAndResetMock();

        Halo.StartTestRequest request = new Halo.StartTestRequest();
        request.setAddress(device.get().getAddress());
        request.setTimeoutMs(HaloController.REQUEST_TIMEOUT);

        expectRequest(request).andRespondWithMessage(EmptyEvent.NAME);

        haloController.testDevice();
        verifyNoMoreInteractions(callback); // Do we get something back while it's testing?
    }

    protected void setCallbackAndResetMock() {
        haloController.setCallback(callback);
        verify(callback, times(1)).onSuccess(Matchers.<DeviceModel>any());
        reset(callback);
    }

}