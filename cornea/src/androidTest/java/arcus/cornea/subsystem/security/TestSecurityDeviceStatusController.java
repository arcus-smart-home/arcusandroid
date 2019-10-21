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

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;
import arcus.cornea.mock.Fixtures;
import arcus.cornea.mock.MockClientTestCase;
import arcus.cornea.subsystem.security.model.AlarmDeviceSection;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.CachedAddressableListSource;
import arcus.cornea.utils.SettableModelSource;
import com.iris.client.capability.SecuritySubsystem;
import com.iris.client.event.Futures;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.SubsystemModel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.List;


public class TestSecurityDeviceStatusController extends MockClientTestCase {
    SettableModelSource<SubsystemModel> source;
    SecurityDeviceStatusController controller;
    AddressableListSource<DeviceModel> devices;

    @Before
    public void setUp() {
        // TODO fake supplier
        devices = CachedAddressableListSource.get(
                ImmutableList.<String>of(),
                Suppliers.ofInstance(
                        Futures.<List<DeviceModel>>succeededFuture(ImmutableList.<DeviceModel>of())
                )
        );
        source = new SettableModelSource<>();
        controller = new SecurityDeviceStatusController(SecuritySubsystem.ALARMMODE_ON, source, devices);
    }

    @Test
    public void testAddListenerAfterLoaded() {
        devices.load();

        source.set((SubsystemModel) Fixtures.loadModel("subsystems/security/disarmed.json"));
        SecurityDeviceStatusController.Callback callback = Mockito.mock(SecurityDeviceStatusController.Callback.class);
        controller.setCallback(callback);

        Mockito.verify(callback).updateSections(ImmutableList.<AlarmDeviceSection>of());
    }
}
