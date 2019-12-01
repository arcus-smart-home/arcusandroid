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
package arcus.cornea;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.common.base.Preconditions;
import arcus.cornea.billing.BillingTokenRequest;
import arcus.cornea.billing.TokenClient;
import arcus.cornea.controller.ISetupController;
import com.iris.client.IrisClientFactory;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.event.SettableClientFuture;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.service.AccountService;
import com.iris.client.service.AccountService.CreateAccountResponse;
import com.iris.client.service.I18NService;
import com.iris.client.service.SessionService;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

final class SetupController implements ISetupController {
    @Override
    public ClientFuture<Map<String, Object>> createAccount(
          @NonNull final String platformURL,
          @NonNull final String email,
          @NonNull final String password,
          @NonNull final String opt_in) {

        Preconditions.checkNotNull(platformURL, "URL Cannot be null");
        Preconditions.checkNotNull(email, "Email cannot be null");
        Preconditions.checkNotNull(password, "Password cannot be null");
        Preconditions.checkNotNull(opt_in, "Opt-in cannot be null");

        final SettableClientFuture<Map<String, Object>> future = new SettableClientFuture<>();
        new Thread(() -> {
            try {
                CorneaClientFactory.getClient().close();
                CorneaClientFactory.getClient().setConnectionURL(platformURL);
            }
            catch (Exception ex) {
                future.setError(ex);
                return;
            }

            CorneaClientFactory
                    .getService(AccountService.class)
                    .createAccount(email, password, opt_in, null, null, null)
                    .onFailure(new Listener<Throwable>() {
                        @Override
                        public void onEvent(Throwable throwable) {
                            future.setError(throwable);
                        }
                    })
                    .onSuccess(new Listener<CreateAccountResponse>() {
                        @Override
                        public void onEvent(CreateAccountResponse response) {
                            Map<String, Object> accountInfo = new HashMap<>();
                            accountInfo.put("account", CorneaClientFactory.getModelCache()
                                    .addOrUpdate(response.getAccount()));
                            accountInfo.put("person", CorneaClientFactory.getModelCache()
                                    .addOrUpdate(response.getPerson()));
                            accountInfo.put("place", CorneaClientFactory.getModelCache()
                                    .addOrUpdate(response.getPlace()));

                            future.setValue(accountInfo);
                        }
                    });
        }).start();

        return future;
    }

    @Override
    public ClientFuture<Map<String, String>> loadLocalizedStrings(
          @NonNull final String platformURL, @Nullable final Set<String> bundleNames, @NonNull final String localeString) {
        Preconditions.checkNotNull(platformURL, "URL Cannot be null");

        final SettableClientFuture<Map<String, String>> future = new SettableClientFuture<>();

        new Thread(() -> {
            CorneaClientFactory
                    .getService(I18NService.class)
                    .loadLocalizedStrings(bundleNames, localeString)
                    .onFailure(new Listener<Throwable>() {
                        @Override
                        public void onEvent(Throwable throwable) {
                            future.setError(throwable);
                        }
                    })
                    .onSuccess(new Listener<I18NService.LoadLocalizedStringsResponse>() {
                        @Override
                        public void onEvent(I18NService.LoadLocalizedStringsResponse response) {
                            Map<String, String> stringsWithoutBundle = new HashMap<>();
                            for (Map.Entry<String, String> entries : response.getLocalizedStrings().entrySet()) {
                                String key = entries.getKey().substring(entries.getKey().indexOf(":") + 1);
                                stringsWithoutBundle.put(key, entries.getValue());
                            }

                            future.setValue(stringsWithoutBundle);
                        }
                    });
        }).start();

        return future;
    }

    public ClientFuture<String> getBillingToken(@NonNull final BillingTokenRequest billingTokenRequest) {
        Preconditions.checkNotNull(billingTokenRequest, "Billing token request cannot be null");

        billingTokenRequest.setPublicKey(CorneaClientFactory.getClient().getSessionInfo().getBillingPublicKey());
        billingTokenRequest.setTokenUrl(CorneaClientFactory.getClient().getSessionInfo().getTokenURL());
        final TokenClient client = new TokenClient();
        return client.getBillingToken(billingTokenRequest)
              .onFailure(new Listener<Throwable>() {
                  @Override
                  public void onEvent(Throwable error) {
                      String code;
                      String message;
                      if(error instanceof ErrorResponseException) {
                          code = ((ErrorResponseException) error).getCode();
                          message = error.getMessage();
                      }
                      else {
                          code = error.getClass().getSimpleName();
                          message = error.getMessage();
                      }
                      IrisClientFactory
                            .getService(SessionService.class)
                            .log("billing", "token.error." + code, message);
                  }
              });
    }
}
