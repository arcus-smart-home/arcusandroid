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
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.model.PlaceAndRoleModel;
import arcus.cornea.model.PlacesWithRoles;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.service.SessionService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class AvailablePlacesProvider {

    private static final AvailablePlacesProvider INSTANCE = new AvailablePlacesProvider();

    public static AvailablePlacesProvider instance() {
        return INSTANCE;
    }

    public ClientFuture<List<PlaceAndRoleModel>> load() {
        return Futures.transform(listPlaces(), new Function<SessionService.ListAvailablePlacesResponse, List<PlaceAndRoleModel>>() {
            @Override public List<PlaceAndRoleModel> apply(SessionService.ListAvailablePlacesResponse input) {
                List<Map<String, Object>> places = input.getPlaces();
                if (places == null) {
                    places = Collections.emptyList();
                }

                List<PlaceAndRoleModel> placeItems = new ArrayList<>(places.size() + 1);
                for (Map<String, Object> place : places) {
                    placeItems.add(new PlaceAndRoleModel(place));
                }

                Collections.sort(placeItems);
                return placeItems;
            }
        });
    }

    public ClientFuture<PlacesWithRoles> loadPlacesWithRoles() {
        return Futures.transform(listPlaces(), new Function<SessionService.ListAvailablePlacesResponse, PlacesWithRoles>() {
            @Override public PlacesWithRoles apply(SessionService.ListAvailablePlacesResponse input) {
                List<Map<String, Object>> places = input.getPlaces();
                if (places == null) {
                    places = Collections.emptyList();
                }

                PlaceAndRoleModel primaryPlace = null;
                List<PlaceAndRoleModel> ownedPlaces = new ArrayList<>(places.size() + 1);
                List<PlaceAndRoleModel> unownedPlaces = new ArrayList<>(places.size() + 1);
                for (Map<String, Object> place : places) {
                    PlaceAndRoleModel placeAndRoleModel = new PlaceAndRoleModel(place);
                    if (placeAndRoleModel.isOwner()) {
                        ownedPlaces.add(placeAndRoleModel);
                    }
                    else {
                        unownedPlaces.add(placeAndRoleModel);
                    }
                    if (placeAndRoleModel.isPrimary()) {
                        primaryPlace = placeAndRoleModel;
                    }
                }

                return new PlacesWithRoles(ownedPlaces, unownedPlaces, primaryPlace);
            }
        });
    }

    protected ClientFuture<SessionService.ListAvailablePlacesResponse> listPlaces() {
         return CorneaClientFactory.getService(SessionService.class).listAvailablePlaces();
    }
}
