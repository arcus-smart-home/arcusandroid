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
package arcus.cornea.subsystem.security;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import arcus.cornea.mock.Fixtures;
import arcus.cornea.mock.MockClientTestCase;
import arcus.cornea.subsystem.security.model.ArmedModel;
import arcus.cornea.subsystem.security.model.ArmingModel;
import arcus.cornea.utils.SettableModelSource;
import com.iris.client.capability.SecurityAlarmMode;
import com.iris.client.capability.SecuritySubsystem;
import com.iris.client.model.SubsystemModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Date;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;


public class TestSecurityStatusController extends MockClientTestCase {
    SettableModelSource<SubsystemModel> source;
    SecurityStatusController controller;

    @Before
    public void setUp() {
        source = new SettableModelSource<>();
        controller = new SecurityStatusController(source);
    }

    @Test
    public void testShowOff() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/security/disarmed.json"));
        SecurityStatusController.AlarmCallback callback = Mockito.mock(SecurityStatusController.AlarmCallback.class);

        controller.setAlarmCallback(callback);
        Mockito.verify(callback).showOff(Mockito.notNull(Date.class));
    }

    @Test
    public void testEventFlow() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/security/disarmed.json"));
        SecurityStatusController.AlarmCallback callback = Mockito.mock(SecurityStatusController.AlarmCallback.class);

        controller.setAlarmCallback(callback);

        // trigger an arming value change
        source.update(ImmutableMap.<String, Object>of(
                SecuritySubsystem.ATTR_ALARMSTATE, SecuritySubsystem.ALARMSTATE_ARMING,
                SecuritySubsystem.ATTR_ALARMMODE, SecuritySubsystem.ALARMMODE_ON
        ));

        source.update(SecuritySubsystem.ATTR_ALARMSTATE, SecuritySubsystem.ALARMSTATE_ARMED);
        source.update(SecuritySubsystem.ATTR_ALARMSTATE, SecuritySubsystem.ALARMSTATE_ALERT);
        source.update(SecuritySubsystem.ATTR_ALARMSTATE, SecuritySubsystem.ALARMSTATE_DISARMED);

        ArmingModel model = new ArmingModel(SecuritySubsystem.ALARMMODE_ON, 5, 0);

        verify(callback, times(2)).showOff(Mockito.notNull(Date.class));
        verify(callback).showArming(model);
        verify(callback).showArmed(Mockito.notNull(ArmedModel.class));
        // TODO deeper testing of triggers
        // verify(callback).showAlert(Mockito.notNull(Trigger.class), Mockito.notNull(Date.class));
    }

    @Test
    public void testButtonCallbackBypassedAndOffline() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/security/disarmed.json"));

        source.update(SecuritySubsystem.ATTR_ALARMMODE, SecuritySubsystem.ALARMMODE_PARTIAL);
        source.update(SecuritySubsystem.ATTR_OFFLINEDEVICES, ImmutableSet.of(
                "SERV:dev:offline1",
                "SERV:dev:offline2",
                "SERV:dev:offline3"
        ));
        source.update(SecuritySubsystem.ATTR_BYPASSEDDEVICES, ImmutableSet.of(
                "SERV:dev:bypassed1",
                "SERV:dev:bypassed2"
        ));
        source.update(SecuritySubsystem.ATTR_ARMEDDEVICES, ImmutableSet.of(
                "SERV:dev:partial1",
                "SERV:dev:partial2"
        ));
        source.update(SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_ON, ImmutableSet.of(
                "SERV:dev:offline1",
                "SERV:dev:offline2",
                "SERV:dev:offline3",
                "SERV:dev:bypassed1",
                "SERV:dev:bypassed2",
                "SERV:dev:partial1",
                "SERV:dev:partial2",
                "SERV:dev:on1",
                "SERV:dev:on2",
                "SERV:dev:on3"
        ));
        source.update(SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_PARTIAL, ImmutableSet.of(
                "SERV:dev:offline1",
                "SERV:dev:offline2",
                "SERV:dev:offline3",
                "SERV:dev:bypassed1",
                "SERV:dev:bypassed2",
                "SERV:dev:partial1",
                "SERV:dev:partial2"
        ));

        SecurityStatusController.ButtonCallback callback = Mockito.mock(SecurityStatusController.ButtonCallback.class);
        controller.setButtonCallback(callback);

        verify(callback).updateAllDevices("10 Devices"); // in partial mode
        verify(callback).updatePartialDevices("3 Offline, 2 Open");
    }

    @Test
    public void testButtonCallbackBypassedOnly() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/security/disarmed.json"));

        source.update(SecuritySubsystem.ATTR_ALARMMODE, SecuritySubsystem.ALARMMODE_ON);
        source.update(SecuritySubsystem.ATTR_BYPASSEDDEVICES, ImmutableSet.of(
                "SERV:dev:bypassed1",
                "SERV:dev:bypassed2"
        ));
        source.update(SecuritySubsystem.ATTR_ARMEDDEVICES, ImmutableSet.of(
                "SERV:dev:partial1",
                "SERV:dev:partial2",
                "SERV:dev:on1",
                "SERV:dev:on2",
                "SERV:dev:on3"
        ));
        source.update(SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_ON, ImmutableSet.of(
                "SERV:dev:bypassed1",
                "SERV:dev:bypassed2",
                "SERV:dev:partial1",
                "SERV:dev:partial2",
                "SERV:dev:on1",
                "SERV:dev:on2",
                "SERV:dev:on3"
        ));
        source.update(SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_PARTIAL, ImmutableSet.of(
                "SERV:dev:bypassed1",
                "SERV:dev:bypassed2",
                "SERV:dev:partial1",
                "SERV:dev:partial2"
        ));

        SecurityStatusController.ButtonCallback callback = Mockito.mock(SecurityStatusController.ButtonCallback.class);
        controller.setButtonCallback(callback);

        verify(callback).updateAllDevices("2 Open");
        verify(callback).updatePartialDevices("4 Devices"); // in on mode
    }

    @Test
    public void testButtonCallbackOfflineOnly() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/security/disarmed.json"));

        source.update(SecuritySubsystem.ATTR_ALARMMODE, SecuritySubsystem.ALARMMODE_ON);
        source.update(SecuritySubsystem.ATTR_OFFLINEDEVICES, ImmutableSet.of(
                "SERV:dev:offline1",
                "SERV:dev:offline2",
                "SERV:dev:offline3"
        ));
        source.update(SecuritySubsystem.ATTR_ARMEDDEVICES, ImmutableSet.of(
                "SERV:dev:partial1",
                "SERV:dev:partial2",
                "SERV:dev:on1",
                "SERV:dev:on2",
                "SERV:dev:on3"
        ));
        source.update(SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_ON, ImmutableSet.of(
                "SERV:dev:offline1",
                "SERV:dev:offline2",
                "SERV:dev:offline3",
                "SERV:dev:partial1",
                "SERV:dev:partial2",
                "SERV:dev:on1",
                "SERV:dev:on2",
                "SERV:dev:on3"
        ));
        source.update(SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_PARTIAL, ImmutableSet.of(
                "SERV:dev:offline1",
                "SERV:dev:offline2",
                "SERV:dev:offline3",
                "SERV:dev:partial1",
                "SERV:dev:partial2"
        ));

        SecurityStatusController.ButtonCallback callback = Mockito.mock(SecurityStatusController.ButtonCallback.class);
        controller.setButtonCallback(callback);

        verify(callback).updateAllDevices("3 Offline");
        verify(callback).updatePartialDevices("5 Devices"); // in on mode
    }

    @Test
    public void testButtonCallbackAllOnline() {
        source.set((SubsystemModel) Fixtures.loadModel("subsystems/security/disarmed.json"));

        source.update(SecuritySubsystem.ATTR_ALARMMODE, SecuritySubsystem.ALARMMODE_ON);
        source.update(SecuritySubsystem.ATTR_ARMEDDEVICES, ImmutableSet.of(
                "SERV:dev:partial1",
                "SERV:dev:partial2",
                "SERV:dev:on1",
                "SERV:dev:on2",
                "SERV:dev:on3"
        ));
        source.update(SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_ON, ImmutableSet.of(
                "SERV:dev:partial1",
                "SERV:dev:partial2",
                "SERV:dev:on1",
                "SERV:dev:on2",
                "SERV:dev:on3"
        ));
        source.update(SecurityAlarmMode.ATTR_DEVICES + ":" + SecuritySubsystem.ALARMMODE_PARTIAL, ImmutableSet.of(
                "SERV:dev:partial1",
                "SERV:dev:partial2"
        ));

        SecurityStatusController.ButtonCallback callback = Mockito.mock(SecurityStatusController.ButtonCallback.class);
        controller.setButtonCallback(callback);

        verify(callback).updateAllDevices("5 Devices");
        verify(callback).updatePartialDevices("2 Devices");
    }

}
