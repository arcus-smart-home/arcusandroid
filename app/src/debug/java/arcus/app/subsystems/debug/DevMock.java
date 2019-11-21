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
package arcus.app.subsystems.debug;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Base64;

import com.google.common.collect.ImmutableList;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.model.StringPair;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientEvent;
import com.iris.client.ClientMessage;
import com.iris.client.ClientRequest;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Device;
import com.iris.client.capability.DeviceAdvanced;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.session.SessionInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DevMock {
    private ListenerRegistration addedListener;
    private WeakReference<Callback> callbackRef = new WeakReference<>(null);
    private final Listener<ClientMessage> deviceAddedListener = Listeners.runOnUiThread(
          new Listener<ClientMessage>() {
              @Override public void onEvent(ClientMessage clientMessage) {
                  ClientEvent event = clientMessage.getEvent();
                  if (event == null || !(event instanceof Capability.AddedEvent)) {
                      return;
                  }

                  if (!"MOCK".equals(event.getAttribute(DeviceAdvanced.ATTR_PROTOCOL))) {
                      return;
                  }

                  Callback callback = callbackRef.get();
                  if (callback != null) {
                      Map<String, Object> attributes = event.getAttributes();
                      if (attributes == null) {
                          attributes = Collections.emptyMap();
                      }

                      callback.onSuccess(attributes);
                  }

                  Listeners.clear(addedListener);
              }
          }
    );
    private final Listener<Throwable> errorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            Callback callback = callbackRef.get();
            if (callback != null) {
                callback.onFailure(throwable);
            }
        }
    });
    private final String[] MOCK_TYPES = {
          "Blind",
          "Button",
          "Camera",
          "ContactSensor",
          "DimmerNoSwitch",
          "DimmerSwitch",
          "ElectricalMeter",
          "FanControl",
          "Fallback / Unsupported Device",
          "GarageDoor",
          "GenieGarageDoor",
          "GlassBreakSensor",
          "Halo",
          "HaloPlus",
          "IrrigationController1Zone",
          "IrrigationController12Zone",
          "KeyFob1Button",
          "KeyFob2Button",
          "KeyFob4Button",
          "KeyPad",
          "LightBulb",
          "LightBulbWithDimmer",
          "LightBulbWithDimmerAndColorTemp",
          "LockNoBuzzin",
          "LockWithBuzzin",
          "MotionSensor",
          "OTADevice",
          "Pendant",
          "PetDoor",
          "Shade",
          "Siren",
          "SmokeCOSensor",
          "SmokeSensor",
          "SomfyV1Bridge",
          "SomfyV1Shade",
          "Switch",
          "Thermostat",
          "ThermostatNoHumidity",
          "HoneywellTCCThermostat",
          "TiltSensor",
          "TiltSensorWithContact",
          "TwinstarSpaceHeater",
          "Vent",
          "WaterHeater",
          "WaterLeakDetector",
          "WaterSoftener",
          "WaterValve",
    };

    public interface Callback {
        void onSuccess(@NonNull Map<String, Object> attributes);
        void onFailure(Throwable throwable);
    }


    public DevMock() {
    }

    public List<String> getMockSelectionStringList() {
        return Arrays.asList(MOCK_TYPES);
    }

    public List<StringPair> getMockSelections() {
        List<StringPair> pairs = new ArrayList<>(MOCK_TYPES.length + 1);
        for (String mock : MOCK_TYPES) {
            pairs.add(new StringPair(mock, "Mockitron"));
        }

        return pairs;
    }

    public List<String> getMockSelectionsAsList() {
        return ImmutableList.copyOf(MOCK_TYPES);
    }

    public ListenerRegistration createMockDevice(String mockType, @Nullable Callback callback) {
        SessionInfo sessionInfo = CorneaClientFactory.getClient().getSessionInfo();
        if (!CorneaClientFactory.isConnected() || sessionInfo == null) {
            if (callback != null) {
                callback.onFailure(new RuntimeException("Client not connected/established session."));
            }
            return Listeners.clear(null);
        }

        String activePlaceId = CorneaClientFactory.getClient().getActivePlace().toString();
        String activeAccountId = null;
        for (SessionInfo.PlaceDescriptor descriptors : sessionInfo.getPlaces()) {
            if (descriptors.getPlaceId().equals(activePlaceId)) {
                activeAccountId = descriptors.getAccountId();
                break;
            }
        }
        if (TextUtils.isEmpty(activeAccountId)) {
            if (callback != null) {
                callback.onFailure(new RuntimeException("Could not determine account."));
            }
            return Listeners.clear(null);
        }

        callbackRef = new WeakReference<>(callback);
        addedListener = CorneaClientFactory.getClient().addMessageListener(deviceAddedListener);

        ClientRequest request = new ClientRequest();
        request.setAddress(String.format("SERV:%s:", Device.NAMESPACE));
        request.setCommand(String.format("%s:%s%s%s", "platform", "Add", "Device", "Request"));
        request.setAttribute("protocolName", "MOCK");
        request.setAttribute("deviceId", generateFakeId());
        request.setAttribute("accountId", activeAccountId);
        request.setAttribute("placeId", activePlaceId);

        List<List<String>> protocolAttributes = new ArrayList<>(1);
        protocolAttributes.add(Arrays.asList(
              String.format("%s:%s", "MOCK", "Capability"),
              String.class.getName(),
              mockType));
        request.setAttribute("protocolAttributes", protocolAttributes);
        request.setTimeoutMs(30_000);

        CorneaClientFactory
              .getClient()
              .request(request)
              .onFailure(errorListener);

        return Listeners.wrap(callbackRef, new Runnable() {
            @Override public void run() {
                Listeners.clear(addedListener);
            }
        });
    }

    protected String generateFakeId() {
        UUID uuid = UUID.randomUUID();
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        byte [] id = new byte[16];
        for(int i=0; i<8; i++) {
            id[i] = (byte)(msb & 0xff);
            id[i + 8] = (byte)(lsb & 0xff);
            lsb >>= 8;
            msb >>= 8;
        }

        return Base64.encodeToString(id, Base64.NO_WRAP);
    }
}
