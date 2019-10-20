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

import android.text.TextUtils;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import arcus.cornea.CorneaClientFactory;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.IrisClient;
import com.iris.client.capability.Capability;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerList;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.Model;
import com.iris.client.model.ModelCache;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.ModelEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CachedModelSource<M extends Model> extends BaseCachedSource<M> implements Function<ClientEvent, M>, Listener<ModelEvent>, AddressableModelSource<M> {
    private static final Logger logger = LoggerFactory.getLogger(CachedModelSource.class);

    public static <M extends Model> AddressableModelSource<M> newSource() {
        return new CachedModelSource<>("", CorneaClientFactory.getClient(), CorneaClientFactory.getModelCache());
    }

    public static <M extends Model> AddressableModelSource<M> get(String address) {
        return get(address, CorneaClientFactory.getClient(), CorneaClientFactory.getModelCache());
    }

    public static <M extends Model> AddressableModelSource<M> get(String address, IrisClient client, ModelCache cache) {
        return new CachedModelSource<M>(address, client, cache);
    }

    private ListenerList<ModelEvent> listeners = new ListenerList<>();

    private IrisClient client;
    private ModelCache cache;

    private String address;
    private ClientFuture<M> request = null;
    private int timeoutMs = 30000;

    protected CachedModelSource(String address, IrisClient client, ModelCache cache) {
        this.address = address;
        this.client = client;
        this.cache = cache;
        Predicate<ModelEvent> filter = new Predicate<ModelEvent>() {
            @Override
            public boolean apply(ModelEvent input) {
                return CachedModelSource.this.address.equals(input.getModel().getAddress());
            }
        };
        this.cache.addModelListener(Listeners.filter(filter, this));
    }

    @Override
    public String getAddress() {
        return address;
    }

    @Override
    public void setAddress(String address) {
        if(this.address.equals(address)) {
            return;
        }

        clear();
        this.address = address;
        load();
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public void setTimeoutMs(int timeoutMs) {
        this.timeoutMs = timeoutMs;
    }

    @Override
    public ListenerRegistration addModelListener(Listener<? super ModelEvent> listener) {
        return listeners.addListener(listener);
    }

    @Override
    public <E extends ModelEvent> ListenerRegistration addModelListener(Listener<? super E> listener, Class<E> type) {
        return listeners.addListener(type, listener);
    }

    @Override
    public M apply(ClientEvent input) {
        return (M) cache.addOrUpdate(input.getAttributes());
    }

    @Override
    public void onEvent(ModelEvent event) {
        if(event instanceof  ModelDeletedEvent) {
            clear();
        }
        else if(request == null) {
            set((M) event.getModel());
        }
        listeners.fireEvent(event);
    }

    protected Optional<M> loadFromCache() {
        return Optional.fromNullable((M) cache.get(address));
    }

    protected ClientFuture<M> doLoad() {
        if (TextUtils.isEmpty(address)) {
            this.request = Futures.failedFuture(new RuntimeException("Empty destination."));
        }
        else {
            ClientRequest request = new ClientRequest();
            request.setAddress(address);
            request.setCommand(Capability.CMD_GET_ATTRIBUTES);
            request.setTimeoutMs(timeoutMs);
            ClientFuture<ClientEvent> response = client.request(request);
            this.request = Futures.transform(response, this);
        }
        return this.request;
    }

}
