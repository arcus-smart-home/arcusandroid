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
package arcus.app.common.controller;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.SessionController;
import arcus.cornea.model.PersonModelProxy;
import arcus.cornea.model.PlaceAndRoleModel;
import arcus.cornea.model.PlacesWithRoles;
import arcus.cornea.utils.DateUtils;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.bean.PersonAccessDescriptor;
import com.iris.client.capability.Person;
import com.iris.client.capability.Place;
import com.iris.client.event.Listener;
import com.iris.client.model.PersonModel;
import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.models.ModelType;
import arcus.app.common.models.ModelTypeListItem;
import arcus.cornea.common.ViewRenderType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlacesAndPeopleController {
    public interface Callback {
        void onError(Throwable throwable);
        void onSuccess(@NonNull List<ModelTypeListItem> persons, Map<PlaceAndRoleModel, List<PersonModelProxy>> peoplePerPlaceMap);
    }

    private boolean addedOwnedHeader = false, addedUnownedHeader = false;
    long  viewID = System.currentTimeMillis();
    PlacesWithRoles placesWithRoles;
    Callback callback;
    List<ModelTypeListItem> peopleValues;
    List<PlaceAndRoleModel> allPlaces;
    Map<PlaceAndRoleModel, List<PersonModelProxy>> peoplePerPlaceMap;
    final Listener<Throwable> errorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            errorEncountered(throwable);
        }
    });

    public PlacesAndPeopleController(@NonNull PlacesWithRoles placesWithRoles, @NonNull Callback callback) {
        this.placesWithRoles = Preconditions.checkNotNull(placesWithRoles);
        this.callback = Preconditions.checkNotNull(callback);

        this.peopleValues = new ArrayList<>(placesWithRoles.getTotalPlaces() + 1);

        this.allPlaces = new ArrayList<>(placesWithRoles.getTotalPlaces() + 1);
        this.allPlaces.addAll(placesWithRoles.getSortedOwnedPlaces());
        this.allPlaces.addAll(placesWithRoles.getSortedUnownedPlaces());

        peoplePerPlaceMap = new HashMap<>(placesWithRoles.getTotalPlaces() + 1);
    }

    public void getPeopleAtEachPlace() {
        doGetPeopleAtEachPlace();
    }

    protected void doGetPeopleAtEachPlace() {
        if (allPlaces.isEmpty()) {
            LooperExecutor.getMainExecutor().execute(new Runnable() {
                @Override public void run() {
                    requestsSuccessful();
                }
            });
        }
        else {
            PlaceAndRoleModel place = allPlaces.remove(0);
            if (!addedOwnedHeader && place.isOwner()) {
                addedOwnedHeader = true;
                ModelTypeListItem header = new ModelTypeListItem(ViewRenderType.HEADER_VIEW, ModelType.HEADER_TYPE, viewID++);
                header.setText1(ArcusApplication.getContext().getString(R.string.account_owner));
                peopleValues.add(header);
            }
            else if (!addedUnownedHeader && !place.isOwner()) {
                addedUnownedHeader = true;
                ModelTypeListItem header = new ModelTypeListItem(ViewRenderType.HEADER_VIEW, ModelType.HEADER_TYPE, viewID++);
                header.setText1(ArcusApplication.getContext().getString(R.string.people_guest));
                peopleValues.add(header);
            }

            ModelTypeListItem placeItem = new ModelTypeListItem(ViewRenderType.PLACE_VIEW, ModelType.PLACE_TYPE, viewID++);
            placeItem.setText1(String.valueOf(place.getName()).toUpperCase());
            placeItem.setText2(place.getStreetAddress1());
            placeItem.setText3(place.getCityStateZip());
            placeItem.setModelID(place.getPlaceId());
            peopleValues.add(placeItem);

            getPeople(place);
        }
    }

    protected void getPeople(final PlaceAndRoleModel place) {
        ClientRequest request = new ClientRequest();
        request.setAddress(place.getAddress());
        request.setCommand(Place.ListPersonsWithAccessRequest.NAME);
        request.setRestfulRequest(false);
        request.setTimeoutMs(30_000);

        CorneaClientFactory.getClient()
              .request(request)
              .onFailure(errorListener)
              .onSuccess(new Listener<ClientEvent>() {
                  @SuppressWarnings({"unchecked"}) @Override public void onEvent(ClientEvent event) {
                      Place.ListPersonsWithAccessResponse response = new Place.ListPersonsWithAccessResponse(event);
                      List<Map<String, Object>> descriptors = response.getPersons();
                      List<PersonModelProxy> people = new ArrayList<>(descriptors.size() + 1);
                      PersonModel loggedIn = SessionController.instance().getPerson();
                      for (Map<String, Object> descriptor : descriptors) {
                          PersonAccessDescriptor person = new PersonAccessDescriptor(descriptor);
                          boolean loggedInCurrently = loggedIn != null && loggedIn.getId().equals(person.getPerson().get(Person.ATTR_ID));
                          if (!loggedInCurrently) { // So we don't show the 'viewer' (their info is under 'profile')
                              people.add(new PersonModelProxy(person.getPerson(), person.getRole()));
                          }
                      }

                      listInvitedPeople(place, people);
                  }
              });
    }

    protected void listInvitedPeople(final PlaceAndRoleModel place, final List<PersonModelProxy> existing) {
        ClientRequest request = new ClientRequest();
        request.setAddress(place.getAddress());
        request.setCommand(Place.PendingInvitationsRequest.NAME);
        request.setRestfulRequest(false);
        request.setTimeoutMs(30_000);

        CorneaClientFactory.getClient()
              .request(request)
              .onFailure(errorListener)
              .onSuccess(new Listener<ClientEvent>() {
                  @Override public void onEvent(ClientEvent clientEvent) {
                      Place.PendingInvitationsResponse response = new Place.PendingInvitationsResponse(clientEvent);
                      List<Map<String, Object>> invitations = response.getInvitations();

                      List<PersonModelProxy> people = new ArrayList<>(invitations.size() + existing.size() + 1);
                      for (Map<String, Object> item : invitations) {
                          people.add(PersonModelProxy.fromInvitation(item));
                      }

                      people.addAll(existing);
                      Collections.sort(people);

                      peoplePerPlaceMap.put(place, people);
                      addPersons(place, people);

                      getPeopleAtEachPlace();
                  }
              });
    }

    protected void addPersons(PlaceAndRoleModel place, List<PersonModelProxy> personModelProxies) {
        for (PersonModelProxy person : personModelProxies) {
            ModelTypeListItem item = new ModelTypeListItem(ViewRenderType.PERSON_VIEW, ModelType.PERSON_TYPE, viewID++);
            item.setText1(person.getFullName().toUpperCase());
            item.setModelID(person.getPersonID());

            if (person.isInvited()) {
                item.setText2(ArcusApplication.getContext().getString(R.string.invitation_date, DateUtils.format(new Date(person.getInvitedDate()))));
            }

            item.setAssociatedPlaceModel(place);
            item.setAdditionalData(person);
            peopleValues.add(item);
        }
    }

    protected void errorEncountered(Throwable throwable) {
        if (callback != null) {
            callback.onError(throwable);
        }
        cleanup();
    }

    protected void requestsSuccessful() {
        if (callback != null) {
            callback.onSuccess(peopleValues, peoplePerPlaceMap);
        }
        cleanup();
    }

    protected void cleanup() {
        placesWithRoles = null;
        allPlaces = null;
        peopleValues = null;
        callback = null;
    }
}
