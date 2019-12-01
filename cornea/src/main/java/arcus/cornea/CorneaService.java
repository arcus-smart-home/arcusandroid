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
package arcus.cornea;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;

import arcus.cornea.controller.ISetupController;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientMessage;
import com.iris.client.ClientRequest;
import com.iris.client.ErrorEvent;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.Model;
import com.iris.client.model.Store;
import com.iris.client.session.SessionEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CorneaService {
    private static Logger logger = LoggerFactory.getLogger(CorneaService.class);
    private static ISetupController setupController = new SetupController();

    public static final CorneaService INSTANCE = new CorneaService();

    public static void initialize(@Nullable String agent, @Nullable String version) {
        CorneaClientFactory.init();
        CorneaClientFactory.init(agent, version);

        INSTANCE.addMessageListener(clientMessage -> {
            if (clientMessage.getEvent() instanceof ErrorEvent) {
                ErrorEvent event = (ErrorEvent) clientMessage.getEvent();
                logger.error("CORNEA ErrorEvent: {}", event);
            }
        });
    }

    public void silentClose() {
        try {
            CorneaClientFactory.getClient().close();
        } catch (IOException ex) {
            logger.debug("Error closing connection.", ex);
        }
    }

    public void setConnectionURL(@NonNull String connectionURL) {
        Preconditions.checkNotNull(connectionURL);

        CorneaClientFactory.getClient().setConnectionURL(connectionURL);
    }

    public boolean isConnected() {
        try {
            return CorneaClientFactory.isConnected();
        }
        catch (Exception ex) {
            // If we haven't called init() yet could end up here.
            return false;
        }
    }

    public ISetupController setup() {
        return setupController;
    }

    public <M extends Model> Store<M> getStore(Class<M> storeType) {
        return CorneaClientFactory.getStore(storeType);
    }
    public <S extends com.iris.client.service.Service> S getService(Class<S> service) { return CorneaClientFactory.getService(service); }

    @Nullable
    public ListenerRegistration addSessionListener(Listener<? super SessionEvent> listener) {
        try {
            return CorneaClientFactory.getClient().addSessionListener(listener);
        }
        catch (Exception ex) {
            logger.error("Could not addSessionListener.  Is the client initialized?", ex.getClass().getSimpleName());
        }

        return null;
    }

    @Nullable
    public ListenerRegistration addRequestListener(Listener<? super ClientRequest> listener) {
        try {
            return CorneaClientFactory.getClient().addRequestListener(listener);
        }
        catch (Exception ex) {
            logger.error("Could not addRequestListener.  Is the client initialized?", ex.getClass().getSimpleName());
        }

        return Listeners.empty();
    }

    @Nullable
    public ListenerRegistration addMessageListener(Listener<? super ClientMessage> listener) {
        try {
            return CorneaClientFactory.getClient().addMessageListener(listener);
        }
        catch (Exception ex) {
            logger.info("Could not addMessageListener.  Is the client initialized?", ex.getClass().getSimpleName());
        }

        return Listeners.empty();
    }
}
