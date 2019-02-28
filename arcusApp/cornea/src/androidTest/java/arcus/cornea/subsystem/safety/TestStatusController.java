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
package arcus.cornea.subsystem.safety;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import arcus.cornea.mock.Fixtures;
import arcus.cornea.mock.MockClientTestCase;
import arcus.cornea.subsystem.safety.model.Alarm;
import arcus.cornea.subsystem.safety.model.SensorSummary;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.SettableModelSource;
import com.iris.client.bean.TriggerEvent;
import com.iris.client.capability.SafetySubsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.PersonModel;
import com.iris.client.model.SubsystemModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;

// rlg:  ignoring for now, need to revisit and fix after many bug fixes that require more mocking of
// the subsystem
@Ignore
public class TestStatusController extends MockClientTestCase {

    private SettableModelSource<SubsystemModel> source;
    private AddressableListSource<PersonModel> personSource;
    private AddressableListSource<DeviceModel> devices;
    private SafetyStatusController.Callback callback;
    private ListenerRegistration registration;

    @Before
    public void setUp() {
        personSource = Mockito.mock(AddressableListSource.class);
        devices = Mockito.mock(AddressableListSource.class);
        source = new SettableModelSource<>();

        SafetyStatusController controller = new SafetyStatusController(source, personSource, devices);
        callback = Mockito.mock(SafetyStatusController.Callback.class);
        registration = controller.setCallback(callback);
    }

    @After
    public void tearDown() {
        Mockito.verify(devices).addListener(Matchers.any(Listener.class));
        Mockito.verify(devices).addModelListener(Matchers.any(Listener.class));
        Mockito.verify(personSource).addListener(Matchers.any(Listener.class));
        Mockito.verify(personSource).addModelListener(Matchers.any(Listener.class));
        registration.remove();
    }

    @Test
    public void testShowUnsatisfiable() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/safety/unsatisfiable.json"));
        Mockito.verify(callback).showUnsatisfiableCopy();
    }

    @Test
    public void testSummaryReadyAllOnline() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/safety/ready.json"));
        SensorSummary summary = new SensorSummary("NO", "NO", "NO");
        Mockito.verify(callback).showSummary(summary);
    }

    @Test
    public void testSummaryDeviceGoesOffline() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/safety/ready.json"));

        Map<String,Object> updates = new HashMap<>();
        updates.put(SafetySubsystem.ATTR_ACTIVEDEVICES, ImmutableSet.of("DRIV:dev:27d7f549-40ee-4f74-ab3b-d5313eaab6da"));
        updates.put(SafetySubsystem.ATTR_SENSORSTATE, ImmutableMap.of(
                "GAS", "NONE",
                "SMOKE", "OFFLINE",
                "CO", "OFFLINE",
                "WATER", "SAFE"
        ));
        source.update(updates);

        SensorSummary summary = new SensorSummary("1 OFFLINE", "1 OFFLINE", "NO");
        Mockito.verify(callback).showSummary(summary);
    }

    @Test
    public void testAlarm() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/safety/ready.json"));

        Date triggerDate = new Date();
        triggerAlarm(triggerDate);

        Alarm alarm = new Alarm(
                "27d7f549-40ee-4f74-ab3b-d5313eaab6da",
                null,
                "SMOKE DETECTED",
                triggerDate);
        List<Alarm> triggers = new ArrayList<>(1);
        triggers.add(alarm);

        Mockito.verify(callback).showAlarm(triggers);
    }

    @Test
    public void testAlarmToClearingDeviceHasntCleared() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/safety/ready.json"));

        Date triggerDate = new Date();
        triggerAlarm(triggerDate);
        triggerClear(false);

        SensorSummary summary = new SensorSummary("CLEARING", "NO", "NO");
        assertEquals("CLEARING", summary.getSmokeStatus());

        Mockito.verify(callback).showSummary(summary);
    }

    @Test
    public void testAlarmToClearingDeviceCleared() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/safety/ready.json"));

        Date triggerDate = new Date();
        triggerAlarm(triggerDate);
        triggerClear(true);

        SensorSummary summary = new SensorSummary("NO", "NO", "NO");
        assertEquals("NO", summary.getSmokeStatus());

        // once for initial load, once after the alarm is cleared
        Mockito.verify(callback, Mockito.times(2)).showSummary(summary);
    }

    // simulate the behavior when the clear button is pressed
    private void triggerClear(boolean deviceClearedFirst) {
        List<Map<String,Object>> triggers = new LinkedList<>(((SafetySubsystem) source.get()).getTriggers());

        Map<String,Object> updates = new HashMap<>();
        updates.put(SafetySubsystem.ATTR_TRIGGERS, ImmutableList.of());

        if(deviceClearedFirst) {
            updates.put(SafetySubsystem.ATTR_PENDINGCLEAR, ImmutableList.of());
            updates.put(SafetySubsystem.ATTR_ALARM, "READY");
            Map<String,String> sensors = new HashMap<>(((SafetySubsystem) source.get()).getSensorState());
            sensors.put("SMOKE", "SAFE");
            updates.put(SafetySubsystem.ATTR_SENSORSTATE, sensors);
        } else {
            updates.put(SafetySubsystem.ATTR_ALARM, "CLEARING");
            updates.put(SafetySubsystem.ATTR_PENDINGCLEAR, triggers);
            Map<String,String> sensors = new HashMap<>(((SafetySubsystem) source.get()).getSensorState());
            sensors.put("SMOKE", "DETECTED");
            updates.put(SafetySubsystem.ATTR_SENSORSTATE, sensors);
        }

        source.update(updates);
    }

    private void triggerAlarm(Date date) {
        Map<String,Object> trigger = new HashMap<>();
        trigger.put(TriggerEvent.ATTR_DEVICE, "DRIV:dev:27d7f549-40ee-4f74-ab3b-d5313eaab6da");
        trigger.put(TriggerEvent.ATTR_TIME, date.getTime());
        trigger.put(TriggerEvent.ATTR_TYPE, "SMOKE");

        Map<String,String> sensors = new HashMap<>(((SafetySubsystem) source.get()).getSensorState());
        sensors.put("SMOKE", "DETECTED");

        Map<String,Object> updates = new HashMap<>();
        updates.put(SafetySubsystem.ATTR_ALARM, "ALERT");
        updates.put(SafetySubsystem.ATTR_TRIGGERS, ImmutableList.of(trigger));
        updates.put(SafetySubsystem.ATTR_SENSORSTATE, sensors);
        source.update(updates);
    }
}
