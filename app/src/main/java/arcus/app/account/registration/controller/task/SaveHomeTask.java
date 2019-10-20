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
package arcus.app.account.registration.controller.task;

import android.text.TextUtils;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.SessionController;
import arcus.cornea.controller.SubscriptionController;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import com.iris.capability.util.Addresses;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.bean.StreetAddress;
import com.iris.client.capability.Account;
import com.iris.client.capability.Capability;
import com.iris.client.capability.Person;
import com.iris.client.capability.Place;
import com.iris.client.event.Listener;
import com.iris.client.model.AccountModel;
import com.iris.client.model.PersonModel;
import com.iris.client.model.PlaceModel;
import com.iris.client.util.Result;
import arcus.app.integrations.Address;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SaveHomeTask {
    private static final Logger logger = LoggerFactory.getLogger(SaveHomeTask.class);
    private static final String DEFAULT_COUNTRY = "US";
    private static final int TO_IN_MS = 30_000;
    private final ArcusTask.ArcusTaskListener taskListener;
    private final AddAnotherPlaceCallback anotherPlaceCallback;
    private final String nickName;
    private final Address address;
    private final String placeID;
    private final Listener<ClientEvent> setLocationAttributes = new Listener<ClientEvent>() {
        @Override
        public void onEvent(ClientEvent clientEvent) {
            setLocationAttributes();
        }
    };

    private final Listener<ClientEvent> setTzNext = new Listener<ClientEvent>() {
        @Override
        public void onEvent(ClientEvent clientEvent) {
            setTimeZoneInfo();
        }
    };

    private final Listener<Throwable> failedListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onError(throwable);
        }
    });
    private final Listener<Result<ClientEvent>> completionListener = Listeners.runOnUiThread(new Listener<Result<ClientEvent>>() {
        @Override
        public void onEvent(Result<ClientEvent> clientEventResult) {
            onCompleteSuccess();
        }
    });
    private final Listener<Throwable> addFailedListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override
        public void onEvent(Throwable throwable) {
            onAddError(throwable);
        }
    });

    public interface AddAnotherPlaceCallback {
        void onError(Throwable throwable);
        void onSuccess(String newPlaceAddress);
    }

    public SaveHomeTask(String nickname, Address address, ArcusTask.ArcusTaskListener listener) {
        this(null, nickname, address, listener);
    }

    public SaveHomeTask(String placeID, String nickname, Address address, ArcusTask.ArcusTaskListener listener) {
        this.placeID = placeID;
        this.address = address;
        this.nickName = nickname;
        this.taskListener = listener;
        this.anotherPlaceCallback = null;
    }

    public SaveHomeTask(String nickname, Address address, AddAnotherPlaceCallback listener) {
        this.placeID = null;
        this.address = address;
        this.nickName = nickname;
        this.taskListener = null;
        this.anotherPlaceCallback = listener;
    }

    public void sendToPlatform() {
        if (!CorneaClientFactory.isConnected()) {
            onError(new RuntimeException("Client not connected"));
            return;
        }

        final UUID placeUUID;
        if (TextUtils.isEmpty(placeID)) {
            placeUUID = CorneaClientFactory.getClient().getActivePlace();
            if (placeUUID == null) {
                onError(new RuntimeException("Could not obtain Place ID"));
                return;
            }
        }
        else {
            try {
                placeUUID = UUID.fromString(placeID);
            }
            catch (Exception ex) {
                onError(ex);
                return;
            }
        }

        if(!TextUtils.isEmpty(nickName)) {
            CorneaClientFactory.getClient()
                    .request(createRequest(getAttributesForPlaceNameRequest(), placeUUID))
                    .onSuccess(new Listener<ClientEvent>() {
                        @Override
                        public void onEvent(ClientEvent clientEvent) {
                            updateAddress(placeUUID.toString());
                        }})
                    .onFailure(failedListener);
        }
        else {
            updateAddress(placeUUID.toString());
        }


    }

    private void updateAddress(String placeID) {
        Place.UpdateAddressRequest request = new Place.UpdateAddressRequest();
        request.setAddress(Addresses.toObjectAddress(Place.NAMESPACE, placeID));
        request.setStreetAddress(getAttributesForUpdatePlaceRequest());

        CorneaClientFactory.getClient()
                .request(request)
                .onFailure(failedListener)
                .onSuccess(setLocationAttributes);
    }

    public void addNewPlace(String serviceLevel) {
        AccountModel accountModel = SessionController.instance().getAccount();
        if (accountModel == null || TextUtils.isEmpty(serviceLevel)) {
            onError(new RuntimeException("Could not fetch account model/service level, was null/empty."));
            return;
        }

        // {"placeAttributes", "population", "serviceLevel", "addons"}
        if(SubscriptionController.isProfessional()) {
            serviceLevel = Place.SERVICELEVEL_PREMIUM;
        }
        accountModel.addPlace(getAttributesForRequest(), null, serviceLevel, null)
              .onFailure(addFailedListener)
              .onSuccess(Listeners.runOnUiThread(new Listener<Account.AddPlaceResponse>() {
                  @Override public void onEvent(Account.AddPlaceResponse response) {
                      final PlaceModel placeModel = (PlaceModel) CorneaClientFactory.getModelCache().addOrUpdate(response.getPlace());
                      if (placeModel == null) { // Hmm...
                          onAddError(new RuntimeException("Returned place was null. It was created?"));
                      }
                      else {
                          setTimeZoneForAddedPlace(placeModel);
                      }
                  }
              }));
    }

    public void promoteToAccount(PersonModel personModel) {
        if (personModel == null) {
            onAddError(new RuntimeException("Person was null, cannot complete request."));
        }
        else {
            personModel
                  .promoteToAccount(getAttributesForRequest())
                  .onFailure(addFailedListener)
                  .onSuccess(Listeners.runOnUiThread(new Listener<Person.PromoteToAccountResponse>() {
                      @Override public void onEvent(Person.PromoteToAccountResponse response) {
                          final PlaceModel placeModel = (PlaceModel) CorneaClientFactory.getModelCache().addOrUpdate(response.getPlace());
                          if (placeModel == null) { // Hmm...
                              onAddError(new RuntimeException("Returned place was null. It was created?"));
                          }
                          else {
                              setTimeZoneForAddedPlace(placeModel);
                          }
                      }
                  }));
        }
    }

    protected void setTimeZoneForAddedPlace(final PlaceModel placeModel) {
        placeModel.setTzName(address.getTimeZoneName());
        placeModel.setTzId(address.getTimeZoneId());
        placeModel.setTzUsesDST(address.isDst());
        placeModel.setTzOffset(address.getUtcOffset());
        placeModel.commit()
              .onCompletion(Listeners.runOnUiThread(new Listener<Result<ClientEvent>>() {
                  @Override public void onEvent(Result<ClientEvent> clientEventResult) {
                      onAddCompleteSuccess(String.valueOf(placeModel.get(Place.ATTR_ID)));
                  }
              }));
    }

    protected Map<String, Object> getAttributesForPlaceNameRequest() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(Place.ATTR_NAME, nickName);
        return attributes;
    }

    protected Map<String, Object> getAttributesForRequest() {
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(Place.ATTR_NAME, nickName);
        attributes.put(Place.ATTR_STREETADDRESS1, address.getStreet());
        attributes.put(Place.ATTR_STREETADDRESS2, address.getStreet2());
        attributes.put(Place.ATTR_CITY, address.getCity());
        attributes.put(Place.ATTR_STATE, address.getState());
        attributes.put(Place.ATTR_ZIPCODE, address.getZipCode());
        attributes.put(Place.ATTR_ADDRLATITUDE, address.getLat());
        attributes.put(Place.ATTR_ADDRLONGITUDE, address.getLng());
        attributes.put(Place.ATTR_COUNTRY, DEFAULT_COUNTRY);

        return attributes;
    }

    protected Map<String, Object> getAttributesForUpdatePlaceRequest() {
        StreetAddress addressBean = new StreetAddress();
        addressBean.setLine1(address.getStreet());
        addressBean.setLine2(address.getStreet2());
        addressBean.setCity(address.getCity());
        addressBean.setState(address.getState());
        addressBean.setZip(address.getZipCode());

        return addressBean.toMap();
    }

    protected Map<String, Object> getLocationAttributes() {
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(Place.ATTR_ADDRLATITUDE, address.getLat());
        attributes.put(Place.ATTR_ADDRLONGITUDE, address.getLng());

        return attributes;
    }


    private void setLocationAttributes() {
        if (!CorneaClientFactory.isConnected()) {
            onError(new RuntimeException("Client not connected"));
            return;
        }

        final UUID placeUUID;
        if (TextUtils.isEmpty(placeID)) {
            placeUUID = CorneaClientFactory.getClient().getActivePlace();
            if (placeUUID == null) {
                onError(new RuntimeException("Could not obtain Place ID"));
                return;
            }
        }
        else {
            try {
                placeUUID = UUID.fromString(placeID);
            }
            catch (Exception ex) {
                onError(ex);
                return;
            }
        }

        CorneaClientFactory.getClient()
                .request(createRequest(getLocationAttributes(), placeUUID))
                .onSuccess(setTzNext)
                .onFailure(failedListener);
    }

    private void setTimeZoneInfo() {
        UUID placeID = CorneaClientFactory.getClient().getActivePlace();
        if (placeID == null) {
            tempPassFor1_4Release(); // We can continue here for now; was discussed that this would be changing in 1.5
            return;
        }

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(Place.ATTR_TZUSESDST, address.isDst());
        attributes.put(Place.ATTR_TZNAME, address.getTimeZoneName());
        attributes.put(Place.ATTR_TZID, address.getTimeZoneId());
        attributes.put(Place.ATTR_TZOFFSET, address.getUtcOffset());

        CorneaClientFactory.getClient()
              .request(createRequest(attributes, placeID))
              .onCompletion(completionListener);
        // We need to come back later and change to a success/fail listener and then proceed to appropriate screens
        // if need be (not sure if the edit screen will work the same, but for sure on new account screen)
    }

    private ClientRequest createRequest(Map<String, Object> attributes, UUID placeID) {
        ClientRequest request = new ClientRequest();
        request.setAddress(Addresses.toObjectAddress(Place.NAMESPACE, placeID.toString()));
        request.setCommand(Capability.CMD_SET_ATTRIBUTES);
        request.setAttributes(attributes);
        request.setRestfulRequest(false);
        request.setTimeoutMs(TO_IN_MS);

        return request;
    }

    private void onError(final Throwable throwable) {
        if (taskListener == null) {
            return;
        }

        try {
            taskListener.onError((Exception) throwable);
        }
        catch (Exception ex) {
            logger.debug("Caught exception while calling callback.", ex);
        }
    }

    private void onCompleteSuccess() {
        if (taskListener == null) {
            return;
        }

        try {
            taskListener.onComplete(true);
        }
        catch (Exception ex) {
            logger.debug("Caught exception while calling callback.", ex);
        }
    }

    private void onAddError(Throwable throwable) {
        if (anotherPlaceCallback == null) {
            return;
        }

        try {
            anotherPlaceCallback.onError(throwable);
        }
        catch (Exception ex) {
            logger.debug("Caught exception while calling callback.", ex);
        }
    }

    private void onAddCompleteSuccess(String newPlaceID) {
        if (anotherPlaceCallback == null) {
            return;
        }

        try {
            anotherPlaceCallback.onSuccess(newPlaceID);
        }
        catch (Exception ex) {
            logger.debug("Caught exception while calling callback.", ex);
        }
    }

    private void tempPassFor1_4Release() {
        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (taskListener == null) {
                    return;
                }

                try {
                    taskListener.onComplete(true);
                }
                catch (Exception ex) {
                    logger.debug("Caught exception while calling callback.", ex);
                }
            }
        });
    }
}
