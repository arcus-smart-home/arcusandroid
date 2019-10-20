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

import java.util.List;

/**
 * Allows one to listen to events on a group of models without
 * it necessarilly being loaded.
 *
 * If any model is added *to this view* a ModelAddedEvent will be fired.
 * If any model is changed after being loaded a ModelChangedEvent will be fired
 * If any model is removed *from the view* a ModelDeletedEvent will be fired.
 *
 * This is generally a facade over a store to enable filtering and sorting.
 *
 */
public interface ModelListSource<M extends Model>  {

    boolean isLoaded();

    List<M> get();

    ClientFuture<List<M>> load();

    ClientFuture<List<M>> reload();

    ListenerRegistration addListener(Listener<? super List<M>> listener);

    ListenerRegistration addModelListener(Listener<? super ModelEvent> listener);

    <E extends  ModelEvent> ListenerRegistration addModelListener(Listener<? super E> listener, Class<E> type);
}
