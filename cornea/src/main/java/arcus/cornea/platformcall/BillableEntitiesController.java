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
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.model.PlacesWithRoles;
import arcus.cornea.utils.CachedModelSource;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import com.iris.capability.util.Addresses;
import com.iris.client.capability.Account;
import com.iris.client.capability.Place;
import com.iris.client.event.Listener;
import com.iris.client.model.AccountModel;
import com.iris.client.model.PlaceModel;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class BillableEntitiesController {
    private static final int FOR_BILLABLE = 0x0A;
    private static final int FOR_ACCOUNT  = 0x0B;
    private final Reference<Callback> callbackRef;
    private final Reference<AccountCallback> accountCallbackRef;
    final Listener<Throwable> errorListener = Listeners.runOnUiThread(new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            onError(throwable);
        }
    });
    final Comparator<PlaceModel> placeModelComparator = new Comparator<PlaceModel>() {
        @Override public int compare(PlaceModel lhs, PlaceModel rhs) {
            String lhsName = String.valueOf(lhs.getName());
            String rhsName = String.valueOf(rhs.getName());

            return lhsName.compareToIgnoreCase(rhsName);
        }
    };
    private int requestType;

    public interface Callback {
        void onError(Throwable throwable);
        void onSuccess(@Nullable List<PlaceModel> places);
    }
    public interface AccountCallback {
        void onError(Throwable throwable);
        void onSuccess(@Nullable AccountModel accountModel);
    }

    protected BillableEntitiesController(@NonNull final Callback callback) {
        callbackRef = new SoftReference<>(callback);
        accountCallbackRef = new SoftReference<>(null);
        requestType = FOR_BILLABLE;
    }

    protected BillableEntitiesController(@NonNull final AccountCallback callback) {
        callbackRef = new SoftReference<>(null);
        accountCallbackRef = new SoftReference<>(callback);
        requestType = FOR_ACCOUNT;
    }

    public static void listBillablePlaces(@NonNull PlacesWithRoles placesWithRoles, @NonNull final Callback callback) {
        Preconditions.checkNotNull(callback);
        Preconditions.checkNotNull(placesWithRoles);
        new BillableEntitiesController(callback).determineAccount(placesWithRoles);
    }

    public static void getAccountModel(@NonNull PlacesWithRoles placesWithRoles, @NonNull final AccountCallback callback) {
        Preconditions.checkNotNull(callback);
        Preconditions.checkNotNull(placesWithRoles);
        new BillableEntitiesController(callback).determineAccount(placesWithRoles);
    }

    protected void determineAccount(PlacesWithRoles placesWithRoles) {
        if (placesWithRoles.ownsPlaces() && placesWithRoles.getPrimaryPlace() != null) {
            loadPlace(placesWithRoles.getPrimaryPlace().getPlaceId());
        }
        else {
            onSuccess(Collections.<PlaceModel>emptyList());
        }
    }

    protected void loadPlace(String placeID) { // Load the place ID associated with the 'primary place'
        CachedModelSource.<PlaceModel>get(Addresses.toObjectAddress(Place.NAMESPACE, Addresses.getId(placeID)))
              .load()
              .onFailure(errorListener)
              .onSuccess(new Listener<PlaceModel>() {
                  @Override public void onEvent(PlaceModel placeModel) {
                      loadAccount(placeModel.getAccount());
                  }
              });
    }

    protected void loadAccount(String accountID) { // Load the account associated with the 'primary place'
        CachedModelSource.<AccountModel>get(Addresses.toObjectAddress(Account.NAMESPACE, Addresses.getId(accountID)))
              .load()
              .onFailure(errorListener)
              .onSuccess(new Listener<AccountModel>() {
                  @Override public void onEvent(AccountModel accountModel) {
                      switch(requestType) {
                          case FOR_ACCOUNT:
                              onSuccess(accountModel);
                              break;

                          case FOR_BILLABLE:
                              listBillablePlaces(accountModel);
                              break;

                          default:
                              break;
                      }
                  }
              });
    }

    protected void listBillablePlaces(AccountModel accountModel) { // List the billable entities associated with the 'primary place'
        if (accountModel == null) {
            LooperExecutor.getMainExecutor().execute(new Runnable() {
                @Override public void run() {
                    onError(new RuntimeException("Cannot load account model to list billable entities. Model was null."));
                }
            });
            return;
        }

        accountModel
              .listPlaces()
              .onFailure(errorListener)
              .onSuccess(Listeners.runOnUiThread(new Listener<Account.ListPlacesResponse>() {
                  @SuppressWarnings({"unchecked"}) @Override public void onEvent(Account.ListPlacesResponse listPlacesResponse) {
                      List<PlaceModel> places = (List) CorneaClientFactory.getModelCache().addOrUpdate(listPlacesResponse.getPlaces());
                      if (places != null) {
                          Collections.sort(places, placeModelComparator);
                      }
                      else {
                          places = new ArrayList<>();
                      }

                      onSuccess(places);
                  }
              }));
    }

    protected void onError(Throwable throwable) {
        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.onError(throwable);
            callbackRef.clear();
        }

        AccountCallback callback2 = accountCallbackRef.get();
        if (callback2 != null) {
            callback2.onError(throwable);
            accountCallbackRef.clear();
        }
    }

    protected void onSuccess(List<PlaceModel> places) {
        Callback callback = callbackRef.get();
        if (callback != null) {
            callback.onSuccess(places);
            callbackRef.clear();
        }
    }

    protected void onSuccess(AccountModel accountModel) {
        AccountCallback callback = accountCallbackRef.get();
        if (callback != null) {
            callback.onSuccess(accountModel);
            callbackRef.clear();
        }
    }
}
