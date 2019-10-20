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
package arcus.cornea.device.camera;

import com.google.common.collect.ImmutableMap;
import arcus.cornea.device.camera.model.WiFiSecurityType;
import arcus.cornea.mock.Fixtures;
import arcus.cornea.mock.MockClient;
import arcus.cornea.mock.MockClientTestCase;
import arcus.cornea.utils.SettableModelSource;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.capability.WiFiScan;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.SettableClientFuture;
import com.iris.client.model.DeviceModel;

import static junit.framework.Assert.*;

import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.List;

public class CameraWifiControllerTest extends MockClientTestCase {
    @Mock
    CameraWifiController.Callback mockCallback;

    MockClient client;
    SettableModelSource<DeviceModel> deviceModel;
    CameraWifiController cameraWifiController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        client = new MockClient();
        deviceModel = new SettableModelSource<>();
        cameraWifiController = new CameraWifiController(client, deviceModel);
    }

    @Test
    public void getAllAvailableSecurityTypes() throws Exception {
        WiFiSecurityType[] securityTypes = WiFiSecurityType.values();
        List<String> secStrings = cameraWifiController.getAllAvailableSecurityTypes();

        assertNotNull(secStrings);
        assertEquals(securityTypes.length, secStrings.size());

        for (WiFiSecurityType type : securityTypes) {
            assertTrue(secStrings.contains(type.name()));
        }
    }

    @Test
    public void getAvailableWifiNetworks() throws Exception {
        assertNotNull(cameraWifiController.getAvailableWifiNetworks());
        assertTrue(cameraWifiController.getAvailableWifiNetworks().isEmpty());
    }

    @Test
    public void startWifiScanError() throws Exception {
        cameraWifiController.setCallback(mockCallback);
        deviceModel.set((DeviceModel) Fixtures.loadModel("devices/SercommCamera.json"));

        assertNotNull(cameraWifiController.getModel());
        assertNotNull(cameraWifiController.getWiFiFromModel());
        assertNotNull(cameraWifiController.getWifiScanFromModel());

        WiFiScan.StartWifiScanRequest request = new WiFiScan.StartWifiScanRequest();
        request.setAddress(cameraWifiController.getModel().getAddress());
        request.setRestfulRequest(false);
        request.setTimeoutMs(10_000);
        request.setAttributes(ImmutableMap.<String, Object>of(WiFiScan.StartWifiScanRequest.ATTR_TIMEOUT, 60));

        client.expectRequest(request).andRespondWithException(new RuntimeException("Runtime Errors Encountered"));
        cameraWifiController.startWifiScan();
        Mockito.verify(mockCallback).onError(Mockito.any(RuntimeException.class));
        Mockito.verifyNoMoreInteractions(mockCallback);
    }

    @Test
    public void startWifiScanSuccess() throws Exception {
        cameraWifiController.setCallback(mockCallback);
        deviceModel.set((DeviceModel) Fixtures.loadModel("devices/SercommCamera.json"));

        assertNotNull(cameraWifiController.getModel());
        assertNotNull(cameraWifiController.getWiFiFromModel());
        assertNotNull(cameraWifiController.getWifiScanFromModel());

        WiFiScan.StartWifiScanRequest request = new WiFiScan.StartWifiScanRequest();
        request.setAddress(cameraWifiController.getModel().getAddress());
        request.setRestfulRequest(false);
        request.setTimeoutMs(10_000);
        request.setAttributes(ImmutableMap.<String, Object>of(WiFiScan.StartWifiScanRequest.ATTR_TIMEOUT, 60));

        ClientEvent response = new ClientEvent("EmptyMessage", cameraWifiController.getModel().getAddress());
        SettableClientFuture<ClientEvent> clientFuture = new SettableClientFuture<>();
        clientFuture.setValue(response);
        client.expectRequest(request).andReturn(clientFuture);
        cameraWifiController.startWifiScan();
        Mockito.verifyNoMoreInteractions(mockCallback);
    }

    @Test
    public void stopWifiScan() throws Exception {
        cameraWifiController.setCallback(mockCallback);
        deviceModel.set((DeviceModel) Fixtures.loadModel("devices/SercommCamera.json"));

        assertNotNull(cameraWifiController.getModel());
        assertNotNull(cameraWifiController.getWiFiFromModel());
        assertNotNull(cameraWifiController.getWifiScanFromModel());

        WiFiScan.EndWifiScanRequest request = new WiFiScan.EndWifiScanRequest();
        request.setTimeoutMs(10_000);
        request.setRestfulRequest(false);
        request.setAddress(cameraWifiController.getModel().getAddress());

        client.expectRequest(request).andAnswer(new MockClient.Answer<ClientRequest, ClientFuture<ClientEvent>>() {
            @Override
            public ClientFuture<ClientEvent> respond(ClientRequest input) throws Throwable {
                return null;
            }
        });

        cameraWifiController.stopWifiScan();
        Mockito.verifyNoMoreInteractions(mockCallback);
    }

    @Test
    public void connectToWifiNetwork() throws Exception {
        // FIXME: What's the response here?...
    }
}