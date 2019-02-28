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

import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.Model;
import com.iris.client.model.ModelEvent;

/**
 * Allows one to listen to events on a model without
 * it necessarilly being loaded.
 *
 * If the model is loaded a ModelAddedEvent will be fired.
 * If the model is changed after being loaded a ModelChangedEvent will be fired
 * If the model is removed from the cache a ModelRemovedEvent will be fired.
 *
 * This is similar to a Store with a single element, and the added
 * load() / reload() methods.
 *
 */
public interface ModelSource<M extends Model>  {

    boolean isLoaded();

    M get();

    ClientFuture<M> load();

    ClientFuture<M> reload();

    ListenerRegistration addModelListener(Listener<? super ModelEvent> listener);

    <E extends  ModelEvent> ListenerRegistration addModelListener(Listener<? super E> listener, Class<E> type);
}
