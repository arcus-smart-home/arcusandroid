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
package arcus.cornea.subsystem;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.IrisClient;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.Model;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.Store;
import com.iris.client.model.SubsystemModel;
import com.iris.client.service.SubsystemService;
import com.iris.client.session.SessionActivePlaceSetEvent;
import com.iris.client.session.SessionEvent;
import com.iris.client.session.SessionExpiredEvent;
import com.iris.client.util.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;


public class SubsystemController {
    private static final Logger logger = LoggerFactory.getLogger(SubsystemController.class);
    private static final SubsystemController instance = new SubsystemController(
            CorneaClientFactory.getClient(),
            CorneaClientFactory.getStore(SubsystemModel.class)
    );

    public static SubsystemController instance() {
        return instance;
    }

    private Optional<String> placeId = Optional.absent();
    private boolean subsystemsLoaded;
    private Store<SubsystemModel> subsystems;
    private ClientFuture<?> loadRef;

    SubsystemController(IrisClient client, Store<SubsystemModel> subsystems) {
        IrisClient client1 = client;
        client1.addSessionListener(new Listener<SessionEvent>() {
            @Override
            public void onEvent(SessionEvent event) {
                if (event instanceof SessionActivePlaceSetEvent) {
                    onPlaceSelected((SessionActivePlaceSetEvent) event);
                } else if (event instanceof SessionExpiredEvent) {
                    onSessionExpired((SessionExpiredEvent) event);
                }
            }
        });
        this.subsystems = subsystems;

        UUID placeId = client1.getActivePlace();
        if(placeId != null) {
            this.placeId = Optional.of( placeId.toString() );
        }
    }

    protected void onPlaceSelected(SessionActivePlaceSetEvent event) {
        placeId = Optional.of( event.getPlaceId().toString() );
        reload();
    }

    protected void onSessionExpired(SessionExpiredEvent event) {
        subsystems.clear();
    }

    public ClientFuture<?> load() {
        if(subsystemsLoaded) {
            return Futures.succeededFuture(null);
        }
        return reload();
    }

    public ClientFuture<?> reload() {
        if(loadRef != null) {
            return loadRef;
        }
        String placeId = this.placeId.orNull();
        if(placeId == null) {
            return Futures.failedFuture(new IllegalStateException("Must select a place before subsystems can be loaded"));
        }

        ClientFuture<SubsystemService.ListSubsystemsResponse> response =
            CorneaClientFactory
                .getService(SubsystemService.class)
                .listSubsystems(placeId)
                ;
        this.loadRef = response;
        // don't chain, if the completion handler returns before loadRef is set, then it will
        // be left in a bad state
        response
                // note no onComplete needed - the SubsystemService already handles the cache...
                .onFailure(new Listener<Throwable>() {
                    @Override
                    public void onEvent(Throwable throwable) {
                        // TODO do something with this...
                        logger.error("Unable to load subsystems", throwable);
                    }
                })
                .onCompletion(new Listener<Result<SubsystemService.ListSubsystemsResponse>>() {
                    @Override
                    public void onEvent(Result<SubsystemService.ListSubsystemsResponse> clientEventResult) {
                        loadRef = null;
                    }
                });
        return response;
    }

    public Store<SubsystemModel> getSubsystems() {
        return subsystems;
    }

    public ModelSource<SubsystemModel> getSubsystemModel(String namespace) {
        return new SubsystemModelSource(namespace);
    }

    private class SubsystemModelSource implements ModelSource<SubsystemModel>, Listener<ModelEvent>, Predicate<ModelEvent> {
        private String namespace;
        private Optional<SubsystemModel> model = Optional.absent();
        private Function<Object, SubsystemModel> transform = new Function<Object, SubsystemModel>() {
            @Override
            public SubsystemModel apply(Object input) {
                // TODO should this throw a NotFoundException?
                return model.orNull();
            }
        };

        SubsystemModelSource(String namespace) {
            this.namespace = namespace;
            for(SubsystemModel model: subsystems.values()) {
                if(model.getCaps().contains(namespace)) {
                    this.model = Optional.of(model);
                    break;
                }
            }
            subsystems.addListener(this);
        }

        @Override
        public boolean apply(ModelEvent event) {
            Model m = event.getModel();
            if(m == null) {
                return false;
            }
            return m.getCaps().contains(namespace);
        }

        @Override
        public void onEvent(ModelEvent event) {
            if(!apply(event)) {
                return;
            }

            if(event instanceof ModelDeletedEvent) {
                model = Optional.absent();
            }
            else {
                model = Optional.fromNullable((SubsystemModel) event.getModel());
            }
        }

        @Override
        public boolean isLoaded() {
            return model.isPresent();
        }

        @Override
        public SubsystemModel get() {
            return model.orNull();
        }

        @Override
        public ClientFuture<SubsystemModel> load() {
            SubsystemModel model = this.model.orNull();
            if(model == null) {
                return reload();
            }
            else {
                return Futures.succeededFuture(model);
            }
        }

        @Override
        public ClientFuture<SubsystemModel> reload() {
            return Futures.transform((ClientFuture<Object>) SubsystemController.this.reload(), transform);
        }

        @Override
        public ListenerRegistration addModelListener(final Listener<? super ModelEvent> listener) {
            if(isLoaded()) {
                listener.onEvent(new ModelAddedEvent(model.get()));
            }
            return subsystems.addListener(Listeners.filter(this, listener));
        }

        @Override
        public <E extends ModelEvent> ListenerRegistration addModelListener(final Listener<? super E> listener, Class<E> filteredType) {
            return subsystems.addListener(filteredType, Listeners.filter(this, listener));
        }
    }
}
