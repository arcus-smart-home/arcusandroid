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
package arcus.cornea.controller;

import androidx.annotation.NonNull;

import arcus.cornea.billing.BillingTokenRequest;
import com.iris.client.event.ClientFuture;

import java.util.Map;
import java.util.Set;

public interface ISetupController {
    /**
     * Calls the platform at {@code connectionURL} and attempts to create an account.
     * Nominal Flow:
     *  Creates Account, Person, & Place.
     *  Stores values in store for later retrieval.
     *  Returns a map, with values
     *      "person" -> PersonModel Object,
     *      "place" -> PlaceModel Object,
     *      "account" -> AccountModel Object
     *
     * Error Flow:
     *  Sets the error encountered during the transaction and throws an {@link java.util.concurrent.ExecutionException}
     *  This exception will contain either a {@link java.lang.Throwable} or an
     *      {@link com.iris.client.exception.ErrorResponseException}
     *       - An {@link com.iris.client.exception.ErrorResponseException} is present when the
     *         platform encountered an error of some type.
     *
     * @param connectionURL URL to the platform.
     * @param email Email of user signing up.
     * @param password Password requested by user.
     * @param optin If the user would like to opt-in to news & offerings. Parsed with Boolean.parseBoolean
     *
     * @return Map of new user information
     */
    ClientFuture<Map<String, Object>> createAccount(@NonNull String connectionURL, @NonNull String email, @NonNull String password, @NonNull String optin);

    /**
     *
     * Gets a list of security questions.  This uses {@link java.util.Locale}.getDefault().getLanguage().
     *
     * @param platformURL URL of the platform to make the restful call to.
     * @param bundleNames bundle names to load
     * @param localeString Locale to use, default en-US
     * @return map of Security questions as "question_key" -> "question text"
     */
    ClientFuture<Map<String, String>> loadLocalizedStrings(@NonNull String platformURL, Set<String> bundleNames, String localeString);

    /**
     *
     * Submit a request to the billing provider to get a token representation of the payment data.
     *
     * @param billingTokenRequest
     * @return Token string that can be submitted to the platform.
     */
    ClientFuture<String> getBillingToken(@NonNull BillingTokenRequest billingTokenRequest);
}
