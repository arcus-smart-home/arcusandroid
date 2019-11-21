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
package arcus.cornea.platformcall;

import androidx.annotation.NonNull;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.utils.Listeners;
import com.iris.capability.util.Addresses;
import com.iris.client.ClientEvent;
import com.iris.client.ClientMessage;
import com.iris.client.ClientRequest;
import com.iris.client.capability.Account;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Person;
import com.iris.client.capability.Place;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;

// Person Place Account Removal Controller...
public class PPARemovalController {
    private final int TIMEOUT_MS = 20_000;
    private ListenerRegistration loggedOutListener;

    public interface RemovedCallback {
        void onSuccess();
        void onError(Throwable throwable);
    }

    RemovedCallback removedCallback;

    public PPARemovalController(@NonNull RemovedCallback callback) {
        removedCallback = callback;
    }
    public void removeAccountAndLogin(String accountAddress) {
        Account.DeleteRequest request = new Account.DeleteRequest();
        request.setAddress(Addresses.toObjectAddress(Account.NAMESPACE, Addresses.getId(accountAddress)));
        request.setDeleteOwnerLogin(true);
        request.setTimeoutMs(TIMEOUT_MS);

        callPlatformForSessionBootedEvent(request);
    }

    public void removeAccessToPlaceFor(String placeAddress, String personAddress) {
        Person.RemoveFromPlaceRequest request = new Person.RemoveFromPlaceRequest();
        request.setPlaceId(Addresses.getId(placeAddress));
        request.setAddress(Addresses.toObjectAddress(Person.NAMESPACE, Addresses.getId(personAddress)));
        request.setTimeoutMs(TIMEOUT_MS);

        callPlatform(request);
    }

    public void removePlace(String placeAddress) {
        Place.DeleteRequest request = new Place.DeleteRequest();
        request.setAddress(Addresses.toObjectAddress(Place.NAMESPACE, Addresses.getId(placeAddress)));
        request.setTimeoutMs(TIMEOUT_MS);

        callPlatform(request);
    }

    public void removePerson(String placeID, String personAddress) {
        Person.RemoveFromPlaceRequest request = new Person.RemoveFromPlaceRequest();
        request.setPlaceId(Addresses.getId(placeID));
        request.setAddress(Addresses.toObjectAddress(Person.NAMESPACE, Addresses.getId(personAddress)));
        request.setTimeoutMs(TIMEOUT_MS);

        callPlatform(request);
    }

    public void deletePersonLogin(String personAddress) {
        Person.DeleteLoginRequest request = new Person.DeleteLoginRequest();
        request.setAddress(Addresses.toObjectAddress(Person.NAMESPACE, Addresses.getId(personAddress)));
        request.setTimeoutMs(TIMEOUT_MS);

        callPlatformForSessionBootedEvent(request);
    }

    public void cancelInvite(String targetPlaceAddress, String code) {
        Place.CancelInvitationRequest request = new Place.CancelInvitationRequest();
        request.setAddress(Addresses.toObjectAddress(Place.NAMESPACE, Addresses.getId(targetPlaceAddress)));
        request.setCode(code);
        request.setTimeoutMs(TIMEOUT_MS);

        callPlatform(request);
    }

    protected void callPlatform(ClientRequest request) {
        try {
            CorneaClientFactory.getClient()
                  .request(request)
                  .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                      @Override public void onEvent(Throwable throwable) {
                          if (removedCallback != null) {
                              removedCallback.onError(throwable);
                          }
                          Listeners.clear(loggedOutListener);
                      }
                  }))
                  .onSuccess(Listeners.runOnUiThread(new Listener<ClientEvent>() {
                      @Override public void onEvent(ClientEvent clientEvent) {
                          if (removedCallback != null) {
                              removedCallback.onSuccess();
                          }
                      }
                  }));
        }
        catch (Exception ex) {
            if (removedCallback != null) {
                removedCallback.onError(ex);
            }
        }
    }

    protected void callPlatformForSessionBootedEvent(ClientRequest request) {
        loggedOutListener = CorneaClientFactory.getClient().addMessageListener(Listeners.runOnUiThread(
              new Listener<ClientMessage>() {
                  @Override public void onEvent(ClientMessage clientMessage) {
                      if (!(clientMessage.getEvent() instanceof Capability.DeletedEvent)) {
                          return;
                      }

                      Capability.DeletedEvent deletedEvent = new Capability.DeletedEvent(clientMessage.getEvent());
                      boolean bootSession = Boolean.TRUE.equals(deletedEvent.getAttribute("bootSession"));
                      boolean accountDeleted = String.valueOf(clientMessage.getSource()).startsWith(Addresses.toServiceAddress(Account.NAMESPACE));
                      if (bootSession || accountDeleted) {
                          if (removedCallback != null) {
                              removedCallback.onSuccess();
                              removedCallback = null; // Clear this since we're going to circumventing the listeners in the request.
                          }
                          Listeners.clear(loggedOutListener);
                      }
                  }
              }
        ));

        callPlatform(request);
    }
}
