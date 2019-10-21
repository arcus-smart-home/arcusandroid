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

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.model.PlaceAndRoleModel;
import arcus.cornea.utils.AddressableModelSource;
import arcus.cornea.utils.CachedModelSource;
import com.iris.capability.util.Addresses;
import com.iris.client.capability.Place;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.event.SettableClientFuture;
import com.iris.client.model.PlaceModel;
import com.iris.client.session.SessionActivePlaceSetEvent;
import com.iris.client.session.SessionEvent;

import java.util.List;


public class PlaceModelProvider {

    private final static AddressableModelSource<PlaceModel> placeSource = CachedModelSource.newSource();

    static {
        // Load the current place model if it's been set. (Logged In)
        if (CorneaClientFactory.getClient().getActivePlace() != null) {
            loadPlaceModel(CorneaClientFactory.getClient().getActivePlace().toString());
        }

        // ... and update the model whenever the active place changes
        CorneaClientFactory.getClient().addSessionListener(new Listener<SessionEvent>() {
            @Override
            public void onEvent(SessionEvent sessionEvent) {
                // If the active place changed
                if (sessionEvent instanceof SessionActivePlaceSetEvent) {
                    SessionActivePlaceSetEvent activePlaceSetEvent = (SessionActivePlaceSetEvent) sessionEvent;
                    loadPlaceModel(activePlaceSetEvent.getPlaceId().toString());
                }
            }
        });
    }

    private static void loadPlaceModel (String placeId) {
        placeSource.setAddress(Addresses.toObjectAddress(Place.NAMESPACE, placeId));
        placeSource.load();
    }

    public static ClientFuture<PlaceModel> getPlace(String placeAddress) {
        return CachedModelSource.<PlaceModel>get(placeAddress).load();
    }

    public static AddressableModelSource<PlaceModel> getCurrentPlace () {
        return placeSource;
    }

    public static ClientFuture<PlaceModel> getPrimaryPlace() {
        final SettableClientFuture<PlaceModel> placeModelFuture = new SettableClientFuture<>();
        AvailablePlacesProvider.instance().load()
              .onSuccess(new Listener<List<PlaceAndRoleModel>>() {
                  @Override public void onEvent(List<PlaceAndRoleModel> placeAndRoleModels) {
                      boolean found = false;
                      for (PlaceAndRoleModel place : placeAndRoleModels) {
                          if (place.isPrimary() && place.isOwner()) {
                              found = true;
                              loadModelIntoFuture(place.getPlaceId(), placeModelFuture);
                              break;
                          }
                      }

                      if (!found) {
                          placeModelFuture.setError(new RuntimeException("Could not find a primary place this person is associated to."));
                      }
                  }
              })
              .onFailure(new Listener<Throwable>() {
                  @Override public void onEvent(Throwable throwable) {
                      placeModelFuture.setError(throwable);
                  }
              });
        return placeModelFuture;
    }

    static void loadModelIntoFuture(String placeID, final SettableClientFuture<PlaceModel> placeModelFuture) {
        AddressableModelSource<PlaceModel> place = CachedModelSource.get(Addresses.toObjectAddress(Place.NAMESPACE, placeID));
        place.load()
            .onSuccess(new Listener<PlaceModel>() {
                @Override public void onEvent(PlaceModel placeModel) {
                    placeModelFuture.setValue(placeModel);
                }
            })
            .onFailure(new Listener<Throwable>() {
                @Override public void onEvent(Throwable throwable) {
                    placeModelFuture.setError(throwable);
                }
            });
    }
}
