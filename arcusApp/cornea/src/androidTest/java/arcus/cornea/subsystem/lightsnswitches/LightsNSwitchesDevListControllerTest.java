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
package arcus.cornea.subsystem.lightsnswitches;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import arcus.cornea.mock.Fixtures;
import arcus.cornea.mock.MockClientTestCase;
import arcus.cornea.mock.MockSupplier;
import arcus.cornea.subsystem.lightsnswitches.model.LightsNSwitchesDevice;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.CachedAddressableListSource;
import arcus.cornea.utils.SettableModelSource;
import com.iris.client.capability.Dimmer;
import com.iris.client.capability.Light;
import com.iris.client.capability.Switch;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.SubsystemModel;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Captor;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class LightsNSwitchesDevListControllerTest extends MockClientTestCase {
    LightsNSwitchesDevListController controller;
    SettableModelSource<SubsystemModel> source;
    AddressableListSource<DeviceModel> devices;

    // From the subsystem JSON.
    List<String> deviceAddresses = ImmutableList.of(
          "DRIV:dev:b3dccba6-e639-4aac-a066-300181f03274",
          "DRIV:dev:4d685070-2ca5-4eae-89e3-46c5fa1725f8",
          "DRIV:dev:75ccd745-6505-41a1-8564-11ed70a0daa6",
          "DRIV:dev:d46d0cd8-64d7-4c7f-9f0d-099c81be00d0",
          "DRIV:dev:a401fe9e-43e5-46f2-b998-1bfc0b7079d8"
    );

    @Captor
    ArgumentCaptor<List<LightsNSwitchesDevice>> lightsNSwitchesDevices;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        source = new SettableModelSource<>();
        devices = CachedAddressableListSource.get(deviceAddresses,
              new MockSupplier(ImmutableSet.of(Switch.NAMESPACE, Dimmer.NAMESPACE, Light.NAMESPACE), deviceAddresses)
        );

        controller = new LightsNSwitchesDevListController(source, devices);
    }

    @Test
    public void doNotLoadDevicesView() throws Exception {
        devices.load();

        source.set((SubsystemModel) Fixtures.loadModel("subsystems/lightsnswitches/subsystem_no_devs.json"));
        LightsNSwitchesDevListController.Callback callback = Mockito.mock(LightsNSwitchesDevListController.Callback.class);
        controller.setCallback(callback);

        Mockito.verifyZeroInteractions(callback);
    }

    @Test
    public void loadDevicesView() throws Exception {
        devices.load();

        source.set((SubsystemModel) Fixtures.loadModel("subsystems/lightsnswitches/subsystem.json"));
        LightsNSwitchesDevListController.Callback callback = Mockito.mock(LightsNSwitchesDevListController.Callback.class);
        controller.setCallback(callback);

        Mockito.verify(callback, Mockito.times(1)).showDevices(lightsNSwitchesDevices.capture());
        List<LightsNSwitchesDevice> devices = lightsNSwitchesDevices.getValue();

        assertNotNull(devices);
        assertEquals(5, devices.size());
        for (LightsNSwitchesDevice device : devices) {
            // Because of the way that the device type is calculated, this will always come back as a
            // light (remove the Light.NAMESPACE in the above caps, and update this as well to not fail)
            assertTrue(LightsNSwitchesDevice.Type.LIGHT.equals(device.getDeviceType()));
        }

        Mockito.verifyNoMoreInteractions(callback);
    }
}