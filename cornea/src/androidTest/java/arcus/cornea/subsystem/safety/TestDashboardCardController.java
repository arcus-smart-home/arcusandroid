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
import arcus.cornea.mock.Fixtures;
import arcus.cornea.mock.MockClientTestCase;
import arcus.cornea.subsystem.safety.model.Alarm;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.SettableModelSource;
import com.iris.client.bean.TriggerEvent;
import com.iris.client.capability.SafetySubsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.SubsystemModel;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestDashboardCardController extends MockClientTestCase {

    private SettableModelSource<SubsystemModel> source;
    private AddressableListSource<DeviceModel> devices;
    private DashboardCardController.Callback callback;
    private ListenerRegistration registration;

    @Before
    public void setUp() {
        source = new SettableModelSource<>();
        devices = Mockito.mock(AddressableListSource.class);
        DashboardCardController controller = new DashboardCardController(source, devices);
        callback = Mockito.mock(DashboardCardController.Callback.class);
        registration = controller.setCallback(callback);
    }

    @After
    public void tearDown() {
        registration.remove();
        Mockito.verify(devices).addListener(Matchers.any(Listener.class));
        Mockito.verify(devices).addModelListener(Matchers.any(Listener.class));
    }

    @Test
    public void testShowUnsatisfiable() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/safety/unsatisfiable.json"));
        Mockito.verify(callback).showUnsatisfiableCopy();
    }

    @Test
    public void testSummaryReadyAllOnline() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/safety/ready.json"));
        String summary = "No smoke, CO or water leaks detected.";
        Mockito.verify(callback).showSummary(summary);
    }

    @Test
    public void testSummaryNoWaterCOOffline() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/safety/ready.json"));

        Map<String,Object> updates = new HashMap<>();
        updates.put(SafetySubsystem.ATTR_SENSORSTATE, ImmutableMap.of(
                "GAS", "NONE",
                "SMOKE", "SAFE",
                "CO", "OFFLINE",
                "WATER", "NONE"
        ));
        source.update(updates);

        String summary = "No smoke or CO detected.";

        Mockito.verify(callback).showSummary(summary);
    }

    @Test
    public void testSummaryJustSmoke() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/safety/ready.json"));

        Map<String,Object> updates = new HashMap<>();
        updates.put(SafetySubsystem.ATTR_SENSORSTATE, ImmutableMap.of(
                "GAS", "NONE",
                "SMOKE", "SAFE",
                "CO", "NONE",
                "WATER", "NONE"
        ));
        source.update(updates);

        String summary = "No smoke detected.";

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
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/safety/clearing.json"));
        String summary = "No CO or water leaks detected.  Clearing smoke.";
        Mockito.verify(callback).showSummary(summary);
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
