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
package arcus.cornea.utils;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import arcus.cornea.CorneaClientFactory;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerList;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.Model;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelCache;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.ModelEvent;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CachedAddressableListSource<M extends Model> extends BaseCachedSource<List<M>> implements AddressableListSource<M> {
    private static final Logger logger = LoggerFactory.getLogger(CachedModelSource.class);

    public static <M extends Model> AddressableListSource<M> newList(Supplier<ClientFuture<List<M>>> supplier) {
        return get(ImmutableList.<String>of(), supplier, CorneaClientFactory.getModelCache());
    }

    public static <M extends Model> AddressableListSource<M> get(
            Collection<String> addresses,
            Supplier<ClientFuture<List<M>>> supplier
    ) {
        return get(addresses, supplier, CorneaClientFactory.getModelCache());
    }

    public static <M extends Model> AddressableListSource<M> get(
            Collection<String> addresses,
            Supplier<ClientFuture<List<M>>> supplier,
            ModelCache cache
    ) {
        return new CachedAddressableListSource<>(addresses, supplier, cache);
    }

    private final ModelCache cache;
    private final Supplier<ClientFuture<List<M>>> supplier;

    // listeners
    private final ListenerList<List<M>> listListeners = new ListenerList<>();
    private final ListenerList<ModelEvent> modelListeners = new ListenerList<>();
    private Predicate<ModelEvent> filter = new Predicate<ModelEvent>() {
        @Override
        public boolean apply(ModelEvent input) {
            return addresses.contains(input.getModel().getAddress());
        }
    };

    private List<String> addresses = ImmutableList.of();

    CachedAddressableListSource(Collection<String> addresses, Supplier<ClientFuture<List<M>>> supplier, ModelCache cache) {
        this.addresses = addresses != null ? ImmutableList.copyOf(addresses) : ImmutableList.<String>of();
        this.supplier = supplier;
        this.cache = cache;
        this.cache.addModelListener(Listeners.filter(filter, new Listener<ModelEvent>() {
            @Override
            public void onEvent(ModelEvent event) {
                if(event instanceof ModelAddedEvent) {
                    onAdded(event);
                }
                if(event instanceof ModelDeletedEvent) {
                    onRemoved(event);
                }
                modelListeners.fireEvent(event);
            }
        }));
    }

    protected void onAdded(ModelEvent event) {
        if(!isLoaded()) {
            // ignore additions while its not loaded
            return;
        }

        List<M> models = get();

        List<M> copy = models == null ? new ArrayList<M>() : new ArrayList<M>(models.size() + 1);
        if(models != null) {
            copy.addAll(models);
        }
        if(copy.add((M) event.getModel())) {
            set(copy);
        }
    }

    protected void onRemoved(ModelEvent event) {
        if(!isLoaded()) {
            // ignore removals while not loaded
            return;
        }

        List<M> models = get();

        List<M> copy = models == null ? new ArrayList<M>() : new ArrayList<M>(models.size() + 1);
        if(models != null) {
            copy.addAll(models);
        }
        if(copy.remove(event.getModel())) {
            set(copy);
        }
    }

    @Override
    protected void set(List<M> value) {
        // adding filtering here because there have been several times (not sure what the specific flow is)
        // where the following sequence can occur:
        // Construct CachedAddressableListSource or setAddresses
        // BaseCachedSource::load or BaseCachedSource::reload is invoked
        // Supplier for the source issues request that loads all entities for the place
        // this::set() is invoked all entities for the place
        // this -> BaseCachedSource::set(all entities for the place)
        // BaseCacheSource -> this::onLoaded(all entities for the place)
        // this -> listeners(all entities for the place instead of filtered)
        List<M> filtered = new ArrayList<>();
        for(M m : value) {
            if(addresses.contains(m.getAddress())) {
                filtered.add(m);
            }
        }
        Collections.sort(filtered, new Comparator<M>() {
            @Override
            public int compare(M lhs, M rhs) {
                return addresses.indexOf(lhs.getAddress()) - addresses.indexOf(rhs.getAddress());
            }
        });
        super.set(filtered);
    }

    @Override
    protected void onLoaded(List<M> value) {
        listListeners.fireEvent(value);
    }

    @Override
    protected void onCleared() {
        fireRemovedAll(modelListeners);
        listListeners.fireEvent(ImmutableList.<M>of());
    }

    @Override
    public void setAddresses(List<String> addresses, boolean skipRefreshOnClear) {
        if(ObjectUtils.equals(addresses, this.addresses) || (addresses == null && this.addresses.isEmpty())) {
            return;
        }

        clear(skipRefreshOnClear);
        this.addresses = addresses == null || addresses.isEmpty() ?
                ImmutableList.<String>of() :
                new ArrayList<>(addresses);
        load();
    }

    // FIXME: 2/11/16 This is the older behavior and not changed since we are close to release.
    // Need to update the Base/Cached/ModelSource(s) to not emit an added/deleted event
    // and instead only emit a "models changed" event but this is a larger change.
    // FIXME: 2/11/16
    @Override public void setAddresses(List<String> addresses) {
        setAddresses(addresses, false);
    }

    @Override
    protected Optional<List<M>> loadFromCache() {
        if(addresses.size() == 0) {
            return Optional.of( (List<M>) ImmutableList.<M>of() );
        }

        List<M> models = new ArrayList<>(addresses.size());
        for(String address: addresses) {
            M model = (M) cache.get(address);
            if(model == null) {
                // TODO better option than this?
                return Optional.absent();
            }
            models.add(model);
        }
        return Optional.of(models);
    }

    @Override
    protected ClientFuture<List<M>> doLoad() {
        return supplier.get();
    }

    @Override
    public ListenerRegistration addModelListener(Listener<? super ModelEvent> listener) {
        fireAddedAll(listener);
        return modelListeners.addListener(Listeners.filter(filter, listener));
    }

    @Override
    public <E extends ModelEvent> ListenerRegistration addModelListener(Listener<? super E> listener, Class<E> type) {
        if(type.isAssignableFrom(ModelAddedEvent.class)) {
            fireAddedAll((Listener<ModelAddedEvent>) listener);
        }
        return modelListeners.addListener(type, Listeners.filter(filter, listener));
    }

    @Override
    public ListenerRegistration addListener(Listener<? super List<M>> listener) {
        List<M> models = get();
        if(models != null) {
            listener.onEvent(models);
        }
        return listListeners.addListener(listener);
    }

    protected void fireAddedAll(Listener<? super ModelAddedEvent> listener) {
        List<M> models = get();
        if(models == null || models.isEmpty()) {
            return;
        }

        for(M model: models) {
            try {
                listener.onEvent(new ModelAddedEvent(model));
            }
            catch(Exception e) {
                logger.warn("Error delivering event to {}", listener, e);
            }
        }
    }

    protected void fireRemovedAll(ListenerList<? super ModelDeletedEvent> listeners) {
        if(!listeners.hasListeners()) {
            return;
        }

        List<M> models = get();
        if(models == null || models.isEmpty()) {
            return;
        }

        for(M model: models) {
            listeners.fireEvent(new ModelDeletedEvent(model));
        }
    }

}
