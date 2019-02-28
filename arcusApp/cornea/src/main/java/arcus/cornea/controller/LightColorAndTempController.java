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
package arcus.cornea.controller;

import com.google.common.collect.ImmutableMap;
import arcus.cornea.CorneaClientFactory;
import com.iris.client.ClientEvent;
import com.iris.client.ClientMessage;
import com.iris.client.ClientRequest;
import com.iris.client.IrisClient;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Color;
import com.iris.client.capability.ColorTemperature;
import com.iris.client.capability.Light;
import com.iris.client.event.Listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

public class LightColorAndTempController {
    private static final Logger logger = LoggerFactory.getLogger(LightColorAndTempController.class);
    private final static LightColorAndTempController INSTANCE;
    private final IrisClient client;

    private AtomicBoolean storeLoaded = new AtomicBoolean(false);
    private WeakReference<Callback> callbackRef;

    static {
        INSTANCE = new LightColorAndTempController(CorneaClientFactory.getClient());
    }

    public interface Callback {
        void onError(Throwable throwable);
        void onColorTempSuccess();
    }


    LightColorAndTempController(IrisClient client) {
        this.client = client;
        callbackRef = new WeakReference<>(null);
        client.addMessageListener(new Listener<ClientMessage>() {
            @Override
            public void onEvent(ClientMessage clientMessage) {
                if (clientMessage == null) {
                    return;
                }

                ClientEvent clientEvent = clientMessage.getEvent();
                switch (clientEvent.getType()) {
                    case Light.ATTR_COLORMODE:
                        onSuccess();
                        break;
                }
            }
        });
    }

    public void updateColorMode(String deviceAddress, String colorMode) {
        ClientRequest request = new ClientRequest();
        request.setCommand(Capability.CMD_SET_ATTRIBUTES);
        request.setAddress(deviceAddress);
        request.setAttributes(ImmutableMap.<String, Object>of(
                Light.ATTR_COLORMODE, colorMode
        ));
        request.setTimeoutMs(30_000);

        if (!CorneaClientFactory.isConnected()) {
            return;
        }

        CorneaClientFactory.getClient()
                .request(request)
                .onSuccess(new Listener<ClientEvent>() {
                    @Override
                    public void onEvent(ClientEvent clientEvent) {
                        onSuccess();
                    }
                })
                .onFailure(new Listener<Throwable>() {
                    @Override
                    public void onEvent(Throwable throwable) {
                        onError(throwable);
                    }
                });
    }

    public void updateColorValue(String deviceAddress, float hue, float saturation) {
        ClientRequest request = new ClientRequest();
        request.setCommand(Capability.CMD_SET_ATTRIBUTES);
        request.setAddress(deviceAddress);
        if(saturation < 0f) {
            saturation = 0f;
        }
        else if(saturation > 100f) {
            saturation = 100f;
        }
        request.setAttributes(ImmutableMap.<String, Object>of(
                Color.ATTR_HUE, hue,
                Color.ATTR_SATURATION, saturation,
                Light.ATTR_COLORMODE, Light.COLORMODE_COLOR
        ));
        request.setTimeoutMs(30_000);

        if (!CorneaClientFactory.isConnected()) {
            return;
        }

        CorneaClientFactory.getClient()
                .request(request)
                .onSuccess(new Listener<ClientEvent>() {
                    @Override
                    public void onEvent(ClientEvent clientEvent) {
                        onSuccess();
                    }
                })
                .onFailure(new Listener<Throwable>() {
                    @Override
                    public void onEvent(Throwable throwable) {
                        onError(throwable);
                    }
                });
    }

    public void updateTemperatureValue(String deviceAddress, int temp) {
        ClientRequest request = new ClientRequest();
        request.setCommand(Capability.CMD_SET_ATTRIBUTES);
        request.setAddress(deviceAddress);
        request.setAttributes(ImmutableMap.<String, Object>of(
                ColorTemperature.ATTR_COLORTEMP, temp,
                Light.ATTR_COLORMODE, Light.COLORMODE_COLORTEMP
        ));
        request.setTimeoutMs(30_000);

        if (!CorneaClientFactory.isConnected()) {
            return;
        }

        CorneaClientFactory.getClient()
                .request(request)
                .onSuccess(new Listener<ClientEvent>() {
                    @Override
                    public void onEvent(ClientEvent clientEvent) {
                        onSuccess();
                    }
                })
                .onFailure(new Listener<Throwable>() {
                    @Override
                    public void onEvent(Throwable throwable) {
                        onError(throwable);
                    }
                });
    }

    public void setCallback(Callback callback) {
        if (this.callbackRef.get() != null) {
            logger.warn("Updating callbacks with [{}].", callback);
        }

        this.callbackRef = new WeakReference<>(callback);
    }

    protected void onSuccess() {
        Callback callbacks = callbackRef.get();
        if (callbacks != null) {
            callbacks.onColorTempSuccess();
        }
    }

    protected void onError(Throwable throwable) {
        Callback callbacks = callbackRef.get();
        if (callbacks != null) {
            callbacks.onError(throwable);
        }
    }

    public static LightColorAndTempController instance() {
        return INSTANCE;
    }

}
