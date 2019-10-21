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

import arcus.cornea.CorneaClientFactory;
import com.iris.client.ClientEvent;
import com.iris.client.ClientMessage;
import com.iris.client.IrisClient;
import com.iris.client.capability.Person;
import com.iris.client.capability.Place;
import com.iris.client.event.Listener;
import com.iris.client.model.PersonModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicBoolean;

public class InvitationController {
    private static final Logger logger = LoggerFactory.getLogger(InvitationController.class);
    private final static int MAX_TIMES_CHECK_FOR_MODEL = 10;
    private final static int INTERVAL_BETWEN_CHECK_FOR_MODEL_MS = 500;
    private final static String PIN_SUCCESS_ATTRIBUTE = "success";
    private final static int DEFAULT_TIMEOUT = 30_000;
    private final static String PERSON_PREFIX = "SERV:" + Person.NAMESPACE + ":";
    private final static String PLACE_PREFIX = "SERV:" + Place.NAMESPACE + ":";
    private final static InvitationController INSTANCE;
    private final IrisClient client;

    private AtomicBoolean storeLoaded = new AtomicBoolean(false);
    private WeakReference<Callback> callbackRef;

    static {
        INSTANCE = new InvitationController(CorneaClientFactory.getClient());
    }

    public interface Callback {
        /**
         * Called when Network IO is taking place.
         */
        void showLoading();

        void updateView();

        /**
         *
         * Called when the selected rule for editing is available and loaded
         *
         * @param personModel person being edited
         */
        void onModelLoaded(PersonModel personModel);

        /**
         * Called when a Network IO operation returned an ErrorEvent
         *
         * @param throwable error encountered
         */
        void onError(Throwable throwable);
    }


    InvitationController(IrisClient client) {
        this.client = client;
        callbackRef = new WeakReference<>(null);
        client.addMessageListener(new Listener<ClientMessage>() {
            @Override
            public void onEvent(ClientMessage clientMessage) {
                if (clientMessage == null) {
                    return;
                }

                ClientEvent clientEvent = clientMessage.getEvent();
                if(clientEvent.getType().equals(Person.InvitationPendingEvent.NAME)) {
                    updateView();
                }
            }
        });
    }

    public void setCallback(Callback callback) {
        if (this.callbackRef.get() != null) {
            logger.warn("Updating callbacks with [{}].", callback);
        }

        this.callbackRef = new WeakReference<>(callback);
    }

    protected void updateView() {
        Callback callbacks = callbackRef.get();
        if (callbacks != null) {
            callbacks.updateView();
        }
    }

    public static InvitationController instance() {
        return INSTANCE;
    }

}
