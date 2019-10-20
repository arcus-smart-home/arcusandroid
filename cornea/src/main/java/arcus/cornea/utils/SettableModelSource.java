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

import com.google.common.collect.ImmutableMap;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerList;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.event.SettableClientFuture;
import com.iris.client.model.Model;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.ModelEvent;

import java.util.Map;

public class SettableModelSource<M extends Model> implements ModelSource<M> {
    private SettableClientFuture<M> reference = Futures.settableFuture();
    private ListenerRegistration modelListener = Listeners.empty();
    private ListenerList<ModelEvent> listeners = new ListenerList<>();

    @Override
    public boolean isLoaded() {
        return reference.isDone();
    }

    @Override
    public M get() {
        if(reference.isDone()) {
            return reference.getResult().getValue();
        }
        return null;
    }

    @Override
    public ClientFuture<M> load() {
        return reference;
    }

    @Override
    public ClientFuture<M> reload() {
        clear();
        return reference;
    }

    @Override
    public ListenerRegistration addModelListener(Listener<? super ModelEvent> listener) {
        if(reference.isDone()) {
            listener.onEvent(new ModelAddedEvent(get()));
        }
        return listeners.addListener(listener);
    }

    @Override
    public <E extends ModelEvent> ListenerRegistration addModelListener(Listener<? super E> listener, Class<E> type) {
        if(reference.isDone() && type.isAssignableFrom(ModelAddedEvent.class)) {
            listener.onEvent((E) new ModelAddedEvent(get()));
        }
        return listeners.addListener(type, listener);
    }

    public void update(String key, Object value) {
        update(ImmutableMap.of(key, value));
    }

    public void update(Map<String, Object> attributes) {
        Model m = get();
        if(m == null) {
            return;
        }
        m.updateAttributes(attributes);
        listeners.fireEvent(new ModelChangedEvent(m, attributes));
    }

    public void set(M value) {
        clear();

        if(value != null) {
            reference.setValue(value);
            listeners.fireEvent(new ModelAddedEvent(value));
        }
    }

    public void clear() {
        if(!reference.isDone()) {
            return;
        }

        listeners.fireEvent(new ModelDeletedEvent(get()));
        reference = Futures.settableFuture();
    }

}
