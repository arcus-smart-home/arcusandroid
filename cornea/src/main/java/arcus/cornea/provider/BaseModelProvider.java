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

import android.support.annotation.Nullable;

import com.google.common.base.Preconditions;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.CachedAddressableListSource;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.IrisClient;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerList;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.Model;
import com.iris.client.model.ModelCache;
import com.iris.client.model.Store;
import com.iris.client.session.SessionActivePlaceSetEvent;
import com.iris.client.session.SessionEvent;
import com.iris.client.session.SessionExpiredEvent;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Base class for controllers that manage a single type of
 * object, such as Devices, Subsystems, etc
 */
public abstract class BaseModelProvider<M extends Model> {
    private static final Logger logger = LoggerFactory.getLogger(BaseModelProvider.class);

    protected final IrisClient client;
    protected final ModelCache cache;
    protected final Store<M> store;
    private final AtomicReference<String> placeId = new AtomicReference<>();
    private final AtomicReference<ClientFuture<List<M>>> loadRef = new AtomicReference<>();
    private final ListenerList<List<M>> storeLoadedListeners = new ListenerList<>();
    private final AtomicBoolean storeLoaded = new AtomicBoolean(false);

    private final Supplier<ClientFuture<List<M>>> supplier =
            new Supplier<ClientFuture<List<M>>>() {
                @Override
                public ClientFuture<List<M>> get() {
                    return reload();
                }
            };


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

    protected BaseModelProvider(Class<M> type) {
        this(CorneaClientFactory.getClient(), CorneaClientFactory.getModelCache(), CorneaClientFactory.getStore(type));
    }

    protected BaseModelProvider(IrisClient client, ModelCache cache, Store<M> store) {
        Preconditions.checkNotNull(client);
        Preconditions.checkNotNull(cache);
        Preconditions.checkNotNull(store);
        this.client = client;
        Listener<SessionEvent> onSessionEvent = Listeners.runOnUiThread(new Listener<SessionEvent>() {
            @Override
            public void onEvent(SessionEvent event) {
                if (event instanceof SessionActivePlaceSetEvent) {
                    onPlaceSelected((SessionActivePlaceSetEvent) event);
                }
                if (event instanceof SessionExpiredEvent) {
                    onSessionExpired((SessionExpiredEvent) event);
                }
            }
        });
        this.client.addSessionListener(onSessionEvent);
        this.store  = store;
        this.cache = cache;

        UUID placeId = this.client.getActivePlace();
        if(placeId != null) {
            this.placeId.set(placeId.toString());
        }
    }

    protected void onPlaceSelected(SessionActivePlaceSetEvent event) {
        String newPlaceId = event.getPlaceId().toString();
        if(!ObjectUtils.equals(newPlaceId, getPlaceID())) {
            store.clear();
        }

        placeId.set( event.getPlaceId().toString() );
        reload();
    }

    protected void onSessionExpired(SessionExpiredEvent event) {
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

        String placeId = getPlaceID();
        if(placeId == null) {
            return Futures.failedFuture(new IllegalStateException("Must select a place before data can be loaded"));
        }

        storeLoaded.set(false);

        ClientFuture<List<M>> response =
                doLoad(placeId)
                    .onSuccess(onLoaded)
                    .onFailure(onLoadError);
        this.loadRef.set(response);
        return response;
    }

    protected @Nullable String getPlaceID() {
        return this.placeId.get();
    }

    /**
     * Indicates whether or not the models of this type have
     * been loaded.  When this is true load().isDone() will
     * also be true.
     *
     * NOTE there may still be an empty list
     * returned, but it means there are no devices of that
     * type associated with this place.
     * @return
     */
    public boolean isLoaded() {
        ClientFuture<?> load = loadRef.get();
        return load != null && load.isDone();
    }

    public Store<M> getStore() {
        return store;
    }

    public ModelSource<M> getModel(String address) {
        return CachedModelSource.get(address, client, cache);
    }

    public AddressableListSource<M> newModelList() {
        return getModels(ImmutableList.<String>of());
    }

    public AddressableListSource<M> getModels(Collection<String> addresses) {
        AddressableListSource<M> source = CachedAddressableListSource.get(addresses, supplier, cache);
        source.load();
        return source;
    }

    // TODO add this when needed
//    public ModelListSource<M> getModels(Predicate<? super M> predicate, Comparator<M> sortBy) {
//
//    }
//
    protected abstract ClientFuture<List<M>> doLoad(String placeId);

    protected void onLoaded(List<M> models) {
        store.replace(models);
        storeLoaded.set(true);
        fireStoreLoaded(models);
    }

    protected void onLoadError(Throwable cause) {
        // TODO do something with this...
        logger.error("Unable to load models", cause);
    }

    private void fireStoreLoaded(List<M> models) {
        storeLoadedListeners.fireEvent(models);
    }

    public ListenerRegistration addStoreLoadListener(Listener<? super List<M>> listener) {
        if(storeLoaded.get()) {
            listener.onEvent(Lists.newLinkedList(store.values()));
        }
        return storeLoadedListeners.addListener(listener);
    }

}
