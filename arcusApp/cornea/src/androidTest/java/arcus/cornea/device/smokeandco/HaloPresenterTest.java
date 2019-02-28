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
import com.iris.client.capability.Dimmer;
import com.iris.client.capability.Switch;
import com.iris.client.model.DeviceModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class HaloPresenterTest extends MockClientTestCase {
    HaloPresenter haloPresenter;
    SettableModelSource<DeviceModel> deviceModel;

    @Mock HaloController haloController;
    @Mock HaloContract.View haloView;
    @Captor ArgumentCaptor<HaloModel> haloModelCaptor;

    @Before public void setup() throws Exception {
        deviceModel = new SettableModelSource<>();
        deviceModel.set((DeviceModel) Fixtures.loadModel("devices/Halo.json"));

        MockitoAnnotations.initMocks(this);

        haloPresenter = new HaloPresenter(haloController);
        haloPresenter.startPresenting(haloView);
    }

    @Test public void testSetView() throws Exception {
        HaloContract.View customView = mock(HaloContract.View.class);

        haloPresenter.startPresenting(customView);
        HaloContract.View presenterShows = haloPresenter.getView();

        assertThat(presenterShows, equalTo(customView));
    }

    @Test public void testClearReferences() throws Exception {
        HaloContract.View view = haloPresenter.getView();
        assertThat(view, notNullValue());

        haloPresenter.stopPresenting();
        view = haloPresenter.getView();

        assertThat(view, nullValue());
    }

    // Right now this doesn't update anything I'm aware of (from the driver), just provides a failure case
    // @Test public void testPlayCurrentStation() throws Exception {}
    // @Test public void testPlayWeatherStation() throws Exception {}

    @Test public void testFailureEmitsError() throws Exception {
        FailedMessageAnswer answer = new FailedMessageAnswer();
        doAnswer(answer).when(haloController).playWeatherStation(2, HaloContract.PLAY_DURATION_SECONDS);

        haloPresenter.playWeatherStation(2);
        verify(haloView).onError(Matchers.<Exception>any());

        verifyNoMoreInteractions(haloView);
    }

    @Test public void testSetSwitchOn() throws Exception {
        UpdateValueSuccessAnswer answer = new UpdateValueSuccessAnswer(ImmutableMap.<String, Object>of(Switch.ATTR_STATE, Switch.STATE_ON));
        doAnswer(answer).when(haloController).setSwitchOn(true);
        haloPresenter.setSwitchOn(true);

        verify(haloView, times(1)).updateView(haloModelCaptor.capture());

        HaloModel haloModel = haloModelCaptor.getValue();
        assertThat(haloModel, notNullValue());
        assertThat(haloModel.isLightOn(), is(equalTo(true)));
    }

    @Test public void testSetSwitchOff() throws Exception {
        UpdateValueSuccessAnswer answer = new UpdateValueSuccessAnswer(ImmutableMap.<String, Object>of(Switch.ATTR_STATE, Switch.STATE_OFF));
        doAnswer(answer).when(haloController).setSwitchOn(false);
        haloPresenter.setSwitchOn(false);

        verify(haloView, times(1)).updateView(haloModelCaptor.capture());

        HaloModel haloModel = haloModelCaptor.getValue();
        assertThat(haloModel, notNullValue());
        assertThat(haloModel.isLightOn(), is(equalTo(false)));
    }

    @Test public void testSetDimmer() throws Exception {
        UpdateValueSuccessAnswer answer = new UpdateValueSuccessAnswer(ImmutableMap.<String, Object>of(Dimmer.ATTR_BRIGHTNESS, 77));
        doAnswer(answer).when(haloController).setDimmer(77);
        haloPresenter.setDimmer(77);

        verify(haloView, times(1)).updateView(haloModelCaptor.capture());

        HaloModel haloModel = haloModelCaptor.getValue();
        assertThat(haloModel, notNullValue());
        assertThat(haloModel.getDimmerPercent(), is(equalTo(77)));
    }

    @Test public void testTempString() throws Exception {
        String dash = haloPresenter.tempString(null);
        assertThat(dash, equalTo(dash));

        String notDash = haloPresenter.tempString(55.4d);
        assertThat(notDash, not(equalTo("-"))); // A value was calculated.
    }

    @Test public void testStringWithMultiplier() throws Exception {
        String num = haloPresenter.stringWithMultiplier(100, true, 5);
        assertThat(num, is(equalTo("500.0")));
    }

    @Test public void testStringMultiplierDash() throws Exception {
        String num = haloPresenter.stringWithMultiplier(null, false, 5);
        assertThat(num, is(equalTo("-")));
    }

    @Test public void testNumber() throws Exception {
        Number oneHundred = haloPresenter.numberOrNull(100);
        assertThat(100, is(equalTo(oneHundred)));
    }

    @Test public void testNumberOrNull() throws Exception {
        Number oneHundred = haloPresenter.numberOrNull(null);
        assertThat(oneHundred, nullValue());
    }

    @Test public void testNonNullCollection() throws Exception {
        assertThat(haloPresenter.nonNullCollection(null), notNullValue());
    }

    @Test public void testMap() throws Exception {
        assertThat(haloPresenter.mapOrNull(new HashMap()), notNullValue());
    }

    @Test public void testNullMap() throws Exception {
        assertThat(haloPresenter.mapOrNull(null), nullValue());
    }

    public class UpdateValueSuccessAnswer implements Answer<Void> {
        private Map<String, Object> update;

        public UpdateValueSuccessAnswer(Map<String, Object> updateValues) {
            this.update = updateValues;
        }

        @Override public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
            deviceModel.update(update);
            haloPresenter.getHaloCallback().onSuccess(deviceModel.get());
            return null;
        }
    }

    public class FailedMessageAnswer implements Answer<Void> {
        @Override public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
            haloPresenter.getHaloCallback().onError(new RuntimeException("Failed."));
            return null;
        }
    }
}