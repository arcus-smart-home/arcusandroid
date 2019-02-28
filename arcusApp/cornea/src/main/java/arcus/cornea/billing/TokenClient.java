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
package arcus.cornea.billing;

import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import com.google.common.base.Strings;
import com.google.common.net.HttpHeaders;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.SettableClientFuture;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.impl.ClientMessageSerializer;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.Map;

@SuppressWarnings({
        "WeakerAccess",
        "ConstantConditions"
})
public class TokenClient {
    private static final String APP_XML = "application/xml";
    private static final String APP_FORM = "application/x-www-form-urlencoded";

    private final OkHttpClient okHttpClient;

    public TokenClient() {
        this(new OkHttpClient());
    }

    @VisibleForTesting
    TokenClient(OkHttpClient client) {
        this.okHttpClient = client;
    }

    public ClientFuture<String> getBillingToken(@NonNull BillingTokenRequest request) {
        if (request == null) {
            throw new IllegalStateException("Request cannot be null.");
        }

        if (Strings.isNullOrEmpty(request.getTokenURL())) {
            throw new IllegalStateException("Token URL cannot be null");
        }

        if (Strings.isNullOrEmpty(request.getPublicKey())) {
            throw new IllegalStateException("Public Key cannot be null");
        }

        return doGetBillingToken(request, request.getTokenURL());
    }

    private ClientFuture<String> doGetBillingToken(final BillingTokenRequest billingInfoRequest, final String tokenURL) {
        final SettableClientFuture<String> future = new SettableClientFuture<>();

        RequestBody requestBody = buildRequestBody(billingInfoRequest);
        Request request = buildRequest(tokenURL, requestBody);

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                future.setError(e);
            }

            @Override
            public void onResponse(Response response) throws IOException {
                try {
                    String responseString = response.body().string();
                    RecurlyJSONResponse message = ClientMessageSerializer
                            .deserialize(responseString, RecurlyJSONResponse.class);
                    if (message.isError()) {
                        future.setError(new ErrorResponseException(message.getCode(), message.getMessage()));
                    } else {
                        future.setValue(message.getID());
                    }
                } catch (Exception ex) {
                    future.setError(ex);
                }
            }
        });

        return future;
    }

    @VisibleForTesting
    protected Request buildRequest(final String tokenURL, RequestBody requestBody) {
        return new Request.Builder()
                .addHeader(HttpHeaders.ACCEPT, APP_XML)
                .addHeader(HttpHeaders.CONTENT_TYPE, APP_FORM)
                .url(tokenURL)
                .post(requestBody)
                .build();
    }

    @VisibleForTesting
    protected RequestBody buildRequestBody(final BillingTokenRequest billingInfoRequest) {
        FormEncodingBuilder formEncodingBuilder = new FormEncodingBuilder();

        for (Map.Entry<String, String> item : billingInfoRequest.getMappings().entrySet()) {
            formEncodingBuilder.add(item.getKey(), item.getValue());
        }

        return formEncodingBuilder.build();
    }
}
