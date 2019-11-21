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
package arcus.cornea.utils;

import androidx.annotation.NonNull;

import com.google.common.base.Preconditions;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.IrisClient;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.util.Result;

import java.util.TimerTask;

public class DebouncedClientRequest extends TimerTask {
    private final IrisClient irisClient;
    private final ClientRequest request;
    private Listener<Throwable> onError;
    private Listener<ClientEvent> onSuccess;
    private Listener<Result<ClientEvent>> onCompletion;

    public DebouncedClientRequest(@NonNull IrisClient client, @NonNull ClientRequest request) {
        Preconditions.checkNotNull(client);
        Preconditions.checkNotNull(request);

        this.irisClient = client;
        this.request = request;
    }

    public void setOnError(Listener<Throwable> onError) {
        this.onError = onError;
    }

    public void setOnSuccess(Listener<ClientEvent> onSuccess) {
        this.onSuccess = onSuccess;
    }

    @Override
    public void run() {
        ClientFuture<ClientEvent> requestFuture = irisClient.request(request);
        if (onError != null) {
            requestFuture.onFailure(onError);
        }
        if (onSuccess != null) {
            requestFuture.onSuccess(onSuccess);
        }
    }
}
