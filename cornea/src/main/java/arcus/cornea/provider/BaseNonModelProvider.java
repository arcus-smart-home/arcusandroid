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
package arcus.cornea.provider;

import androidx.annotation.CallSuper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import android.text.TextUtils;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import arcus.cornea.utils.Listeners;
import com.iris.client.ClientEvent;
import com.iris.client.ClientMessage;
import com.iris.client.IrisClient;
import com.iris.client.capability.Capability;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerList;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.session.SessionEvent;
import com.iris.client.session.SessionExpiredEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A provider of {@link M}'s that reloads on devices added/removed and clears on SessionExpired
 *
 * @param <M>
 */
public abstract class BaseNonModelProvider<M> {
    private static final Logger logger = LoggerFactory.getLogger(BaseNonModelProvider.class);
    protected static final String DEVICE = "DRIV:dev:";
    protected final static int DEFAULT_TIMEOUT_MS = 30_000;

    private final IrisClient client;
    private final AtomicLong lastRequestProcessedRef = new AtomicLong(System.currentTimeMillis());
    private final AtomicReference<String> subsystemAddressRef = new AtomicReference<>();
    private final AtomicReference<ClientFuture<List<M>>> loadRef = new AtomicReference<>();
    private final ListenerList<List<M>> providerLoadedList = new ListenerList<>();

    private final Listener<List<M>> onLoaded = Listeners.runOnUiThread(new Listener<List<M>>() {
        @Override
        public void onEvent(List<M> models) {
            onLoaded(models);
        }
    });
    private final Listener<Throwable> onLoadError = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onLoadError(throwable);
        }
    });

    @VisibleForTesting BaseNonModelProvider(IrisClient client) {
        Preconditions.checkNotNull(client);

        this.client = client;
        this.client.addSessionListener(new Listener<SessionEvent>() {
            @Override
            public void onEvent(SessionEvent event) {
                if (event instanceof SessionExpiredEvent) {
                    onSessionExpired();
                }
            }
        });
        this.client.addMessageListener(new Listener<ClientMessage>() {
            @Override
            public void onEvent(ClientMessage clientMessage) {
                if (clientMessage == null) {
                    return;
                }

                ClientEvent clientEvent = clientMessage.getEvent();
                if (clientEvent == null) {
                    return;
                }

                String eventType = clientEvent.getType();
                if (!(Capability.EVENT_ADDED.equals(eventType) || Capability.EVENT_DELETED.equals(eventType))) {
                    return;
                }

                String source = String.valueOf(clientEvent.getSourceAddress());
                if (source.startsWith(DEVICE)) {
                    reload();
                }
            }
        });
    }

    protected void onSessionExpired() {
        clearProvider();
    }

    @CallSuper public void clearProvider() {
        logger.debug("Setting loadRef Null - clearProvider() called.");
        loadRef.set(null);
        subsystemAddressRef.set(null);
    }

    public ClientFuture<List<M>> load() {
        ClientFuture<List<M>> load = loadRef.get();
        if(load != null && load.isDone()) {
            return load;
        }
        return reload();
    }

    public ClientFuture<List<M>> reload() {
        ClientFuture<List<M>> load = loadRef.get();
        if(load != null && !load.isDone()) {
            return load;
        }

        ClientFuture<List<M>> response =
                doLoad()
                    .onSuccess(onLoaded)
                    .onFailure(onLoadError);
        this.loadRef.set(response);
        return response;
    }

    /**
     * Indicates whether or not the {@link M}s of this type have been loaded.  When this is true load().isDone() will also be true.
     * NOTE there may still be an empty list returned, but it means there are no items of that type.
     *
     * @return
     */
    public boolean isLoaded() {
        ClientFuture<List<M>> load = loadRef.get();
        return load != null && load.isDone();
    }

    protected abstract ClientFuture<List<M>> doLoad();

    protected @NonNull IrisClient getClient() {
        return this.client;
    }

    protected void onLoaded(List<M> models) {
        providerLoadedList.fireEvent(models == null ? Collections.<M>emptyList() : models);
    }

    protected void onLoadError(Throwable cause) {
        logger.error("Unable to load models", cause);
    }

    public void setSubsystemAddress(@NonNull String subsystemAddress) {
        if (TextUtils.isEmpty(subsystemAddress)) {
            subsystemAddressRef.set(null);
            logger.debug("Clearing subsystem address ref due to null address passed");
            return;
        }

        String old = subsystemAddressRef.getAndSet(subsystemAddress);
        if (TextUtils.isEmpty(old) || !old.equals(subsystemAddress)) {
            reload();
        }
    }

    protected @Nullable String getSubsystemAddress() {
        return subsystemAddressRef.get();
    }

    public Optional<List<M>> getAll() {
        ClientFuture<List<M>> load = loadRef.get();
        if (load != null && load.isDone()) {
            try {
                return Optional.fromNullable(load.get());
            }
            catch (Exception ex) {
                logger.error("Error getting values, returning an empty List.", ex);
                return Optional.absent();
            }
        }
        else {
            logger.error("Provider not loaded, returning an empty List.");
            return Optional.absent();
        }
    }

    public ListenerRegistration addItemsLoadedListener(Listener<? super List<M>> listener) {
        return addItemsLoadedListener(listener, true);
    }

    public ListenerRegistration addItemsLoadedListener(Listener<? super List<M>> listener, boolean replayLast) {
        ClientFuture<List<M>> load = loadRef.get();
        if(replayLast && load != null && load.isDone()) {
            try {
                listener.onEvent(load.get());
            } catch (Exception ex) {
                logger.error("Exception loading values from reference.", ex);
            }
        }

        return providerLoadedList.addListener(listener);
    }
}
