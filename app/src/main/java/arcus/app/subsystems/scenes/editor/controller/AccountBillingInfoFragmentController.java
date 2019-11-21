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
package arcus.app.subsystems.scenes.editor.controller;

import androidx.annotation.Nullable;

import arcus.cornea.model.PlacesWithRoles;
import arcus.cornea.platformcall.BillableEntitiesController;
import arcus.cornea.provider.AvailablePlacesProvider;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.Listener;
import com.iris.client.model.AccountModel;
import arcus.app.common.controller.FragmentController;

public class AccountBillingInfoFragmentController extends FragmentController<AccountBillingInfoFragmentController.Callbacks> {

    public interface Callbacks {
        void onAccountModelLoaded(AccountModel model);
        void onCorneaError(Throwable cause);
    }

    final BillableEntitiesController.AccountCallback accountCallback = new BillableEntitiesController.AccountCallback() {
        @Override public void onError(Throwable throwable) {
            onErrorResponse(throwable);
        }

        @Override public void onSuccess(@Nullable AccountModel accountModel) {
            Callbacks listener = getListener();
            if (listener != null) {
                listener.onAccountModelLoaded(accountModel);
            }
        }
    };

    public void loadAccountModel(Callbacks callbacks) {
        setListener(callbacks);

        // Updated to make sure we get the correct place's account info since we can possibly
        // be loaded into any place (owned or not) and view/update our billing info.
        AvailablePlacesProvider.instance().loadPlacesWithRoles()
              .onSuccess(Listeners.runOnUiThread(new Listener<PlacesWithRoles>() {
                  @Override public void onEvent(PlacesWithRoles placesWithRoles) {
                      BillableEntitiesController.getAccountModel(placesWithRoles, accountCallback);
                  }
              }))
              .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                  @Override public void onEvent(Throwable throwable) {
                      onErrorResponse(throwable);
                  }
              }));
    }

    protected void onErrorResponse(Throwable throwable) {
        Callbacks listener = getListener();
        if (listener != null) {
            listener.onCorneaError(throwable);
        }
    }
}
