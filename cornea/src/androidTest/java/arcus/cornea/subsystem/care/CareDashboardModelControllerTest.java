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
package arcus.cornea.subsystem.care;

import com.google.common.collect.ImmutableMap;
import arcus.cornea.mock.Fixtures;
import arcus.cornea.mock.MockClientTestCase;
import arcus.cornea.subsystem.care.model.AlarmState;
import arcus.cornea.utils.SettableModelSource;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.model.SubsystemModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Collections;

import static org.junit.Assert.*;

public class CareDashboardModelControllerTest extends MockClientTestCase {
    CareDashboardModelController controller;
    SettableModelSource<SubsystemModel> source;
    @Captor ArgumentCaptor<AlarmState> alarmStateArgumentCaptor;

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);

        source = new SettableModelSource<>();
        controller = new CareDashboardModelController(source, client());

        expectRequestOfType(CareSubsystem.ListBehaviorsRequest.NAME)
              .andRespondFromPath("subsystems/care/listBehaviorsResponse.json");
        expectRequestOfType(CareSubsystem.ListBehaviorTemplatesRequest.NAME)
              .andRespondFromPath("subsystems/care/listBehaviorTemplatesResponse.json");
    }

    @Test public void alarmLoadsNotAlarming() throws Exception {
        expectListActivities();
        CareDashboardModelController.Callback callback = setModelSourceAndInitCallback("subsystems/care/subsystemAlarmReadyON.json");
        Mockito.verify(callback, Mockito.times(1)).showSummary(alarmStateArgumentCaptor.capture());

        CareSubsystem careSubsystem = (CareSubsystem) source.get();
        assertEquals(CareSubsystem.ALARMSTATE_READY, careSubsystem.getAlarmState());

        AlarmState alarmState = alarmStateArgumentCaptor.getValue();
        assertNotNull(alarmState);
        assertNotNull(alarmState.getEvents());
        assertTrue(alarmState.getEvents().isEmpty());
        assertEquals(1, alarmState.getActiveBehaviors());
        assertEquals(3, alarmState.getTotalBehaviors());
        assertEquals(CareSubsystem.ALARMMODE_ON, alarmState.getAlarmMode());

        assertFalse(alarmState.isAlert());
        assertNull(alarmState.getAlertActor());
        assertNull(alarmState.getAlertCause());

        Mockito.verifyNoMoreInteractions(callback);
    }

    @Test public void alarmLoadsAlarming() throws Exception {
        expectListActivities();
        CareDashboardModelController.Callback callback = setModelSourceAndInitCallback("subsystems/care/subsystemAlarmAlert.json");
        Mockito.verify(callback, Mockito.times(1)).showAlerting(alarmStateArgumentCaptor.capture());

        CareSubsystem careSubsystem = (CareSubsystem) source.get();
        assertEquals(CareSubsystem.ALARMSTATE_ALERT, careSubsystem.getAlarmState());

        AlarmState alarmState = alarmStateArgumentCaptor.getValue();
        assertNotNull(alarmState);
        assertNotNull(alarmState.getEvents());
        assertTrue(alarmState.getEvents().isEmpty());
        assertEquals(0, alarmState.getActiveBehaviors());
        assertEquals(0, alarmState.getTotalBehaviors());
        assertNull(alarmState.getAlarmMode());

        assertTrue(alarmState.isAlert());
        assertEquals("Care Alarm", alarmState.getAlertActor());
        assertEquals("Panic Rule.", alarmState.getAlertCause());

        Mockito.verifyNoMoreInteractions(callback);
    }

    @Test public void showLearnMoreLoads() throws Exception {
        CareDashboardModelController.Callback callback = setModelSourceAndInitCallback("subsystems/care/subsystemNotAvailable.json");
        Mockito.verify(callback, Mockito.times(1)).showLearnMore();
        Mockito.verifyNoMoreInteractions(callback);
    }

    @Test public void showAlert() throws Exception {
        expectListActivities();
        CareDashboardModelController.Callback callback = setModelSourceAndInitCallback("subsystems/care/subsystemAlarmAlertActor.json");
        Mockito.verify(callback, Mockito.times(1)).showAlerting(alarmStateArgumentCaptor.capture());

        CareSubsystem careSubsystem = (CareSubsystem) source.get();
        assertEquals(CareSubsystem.ALARMSTATE_ALERT, careSubsystem.getAlarmState());

        AlarmState alarmState = alarmStateArgumentCaptor.getValue();
        assertTrue(alarmState.isAlert());
        assertEquals("Care Alarm", alarmState.getAlertActor());
        assertEquals("Medicine Cabinet Reminder", alarmState.getAlertCause());
    }

    protected void expectListActivities() {
        client()
              .expectRequestOfType(CareSubsystem.ListActivityRequest.NAME)
              .andRespondWithMessage(
                    CareSubsystem.ListActivityResponse.NAME,
                    ImmutableMap.<String, Object>of("intervals", Collections.emptyList()) // Date, Map<String, String> is normal.
              );
    }

    protected CareDashboardModelController.Callback setModelSourceAndInitCallback(String modelJSONFile) {
        source.set((SubsystemModel) Fixtures.loadModel(modelJSONFile));
        assertNotNull(source.get());
        assertTrue(source.isLoaded());

        CareDashboardModelController.Callback callback = Mockito.mock(CareDashboardModelController.Callback.class);
        controller.setCallback(callback);

        return callback;
    }
}