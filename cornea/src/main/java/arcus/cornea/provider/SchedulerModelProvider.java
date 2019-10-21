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

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.BaseCachedSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.capability.util.Addresses;
import com.iris.client.ClientEvent;
import com.iris.client.IrisClient;
import com.iris.client.capability.Schedule;
import com.iris.client.capability.Scheduler;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerList;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.event.SettableClientFuture;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelCache;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.SchedulerModel;
import com.iris.client.model.Store;
import com.iris.client.service.SchedulerService;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SchedulerModelProvider extends BaseModelProvider<SchedulerModel> {

    private static final SchedulerModelProvider instance = new SchedulerModelProvider();

    public static SchedulerModelProvider instance() {
        return instance;
    }

    private final Function<ClientEvent, List<SchedulerModel>> getSchedulers = new Function<ClientEvent, List<SchedulerModel>>() {
        @Override
        public List<SchedulerModel> apply(ClientEvent clientEvent) {
            SchedulerService.ListSchedulersResponse response = new SchedulerService.ListSchedulersResponse(clientEvent);
            return (List) cache.retainAll(Schedule.NAMESPACE, response.getSchedulers());
        }
    };

    private final IrisClient client;
    private final ModelCache cache;

    SchedulerModelProvider() {
        this(CorneaClientFactory.getClient(),
                CorneaClientFactory.getModelCache(),
                CorneaClientFactory.getStore(SchedulerModel.class));
    }

    SchedulerModelProvider(IrisClient client, ModelCache cache, Store<SchedulerModel> store) {
        super(client, cache, store);
        this.client = client;
        this.cache = cache;
    }

    @Override
    protected ClientFuture<List<SchedulerModel>> doLoad(String placeId) {
        SchedulerService.ListSchedulersRequest req = new SchedulerService.ListSchedulersRequest();
        req.setAddress("SERV:" + SchedulerService.NAMESPACE +":");
        req.setPlaceId(placeId);
        return Futures.transform(client.request(req), getSchedulers);
    }

    public ModelSource<SchedulerModel> getForTarget(String target) {
        return new TargetModelSource(target);
    }

    private class TargetModelSource extends BaseCachedSource<SchedulerModel> implements ModelSource<SchedulerModel>, Listener<ModelEvent> {

        private final ListenerList<ModelEvent> listeners = new ListenerList<>();
        private final String target;
        private AtomicBoolean triedToCreate = new AtomicBoolean(false);

        TargetModelSource(String target) {
            this.target = target;
        }


        @Override
        protected Optional<SchedulerModel> loadFromCache() {
            if(!SchedulerModelProvider.this.isLoaded()) {
                return Optional.absent();
            }
            Optional<SchedulerModel> retVal = getFromCollection(SchedulerModelProvider.this.getStore().values());
            // make sure that if it was in the cache that we trigger the added event so that listeners
            // know otherwise they will never get the event
            if(retVal.isPresent()) {
                listeners.fireEvent(new ModelAddedEvent(retVal.get()));
            }
            return retVal;
        }

        @Override
        public ClientFuture<SchedulerModel> reload() {
            // ugly...we need to make sure the load future is set on the base class before
            // we emit the model added event otherwise calling get() on this source too soon
            // results in a value of null being returned.
            ClientFuture<SchedulerModel> future = super.reload();
            future.onSuccess(new Listener<SchedulerModel>() {
                @Override
                public void onEvent(SchedulerModel schedulerModel) {
                    if(schedulerModel != null) {
                        listeners.fireEvent(new ModelAddedEvent(schedulerModel));
                    }
                }
            });
            return future;
        }

        @Override
        protected ClientFuture<SchedulerModel> doLoad() {
            final SettableClientFuture<SchedulerModel> future = new SettableClientFuture<>();
            SchedulerModelProvider.this.reload().onSuccess(new Listener<List<SchedulerModel>>() {
                @Override
                public void onEvent(List<SchedulerModel> schedulerModels) {
                    Optional<SchedulerModel> model = getFromCollection(schedulerModels);
                    if (model.isPresent()) {
                        future.setValue(model.get());
                    }
                    else if (!triedToCreate.get()) {
                        // If the scheduler didn't exist before, go ahead and create it here.
                        // This WILL NOT WORK for thermostats - If that scheduler gets deleted they will have to
                        // remove the thermostat and RE-ADD it.
                        triedToCreate.set(true);
                        SchedulerService.GetSchedulerRequest request = new SchedulerService.GetSchedulerRequest();
                        request.setAddress(Addresses.toServiceAddress(Scheduler.NAMESPACE));
                        request.setTimeoutMs(30_000);
                        request.setTarget(target);
                        client.request(request)
                              .onSuccess(new Listener<ClientEvent>() {
                                  @Override
                                  public void onEvent(ClientEvent clientEvent) {
                                      Optional<SchedulerModel> model = getFromCollection(
                                            SchedulerModelProvider.instance().getStore().values()
                                      );

                                      if (model.isPresent()) {
                                          future.setValue(model.get());
                                      }
                                      else {
                                          future.setError(
                                                new RuntimeException(
                                                      "Could not create a new schedule for device, and one did not exist."
                                                )
                                          );
                                      }
                                  }
                              });
                    }
                    else {
                        triedToCreate.set(true);
                        future.setError(
                              new RuntimeException(
                                    "Could not create a new schedule for device, and one did not exist."
                              )
                        );
                    }
                }
            });
            return future;
        }

        private Optional<SchedulerModel> getFromCollection(Iterable<SchedulerModel> models) {
            Optional<SchedulerModel> retVal = Optional.absent();
            for(SchedulerModel m : SchedulerModelProvider.this.getStore().values()) {
                if(StringUtils.equals(this.target, m.getTarget())) {
                    retVal = Optional.of(m);
                }
            }

            if(retVal.isPresent()) {
                attachCacheListeners(retVal.get());
            }

            return retVal;
        }

        private void attachCacheListeners(final SchedulerModel m) {
            Predicate<ModelEvent> predicate = new Predicate<ModelEvent>() {
                @Override
                public boolean apply(ModelEvent modelEvent) {
                    return m.getAddress().equals(modelEvent.getModel().getAddress());
                }
            };
            SchedulerModelProvider.this.cache.addModelListener(Listeners.filter(predicate, this));
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
        public void onEvent(ModelEvent modelEvent) {
            if(modelEvent instanceof ModelDeletedEvent) {
                clear();
            }
            listeners.fireEvent(modelEvent);
        }
    }
}
