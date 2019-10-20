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

import com.iris.client.ClientEvent;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.util.Result;

import java.util.TimerTask;

public class DebouncedRequest extends TimerTask {
    private DeviceModel model;
    private Listener<Throwable> onError;
    private Listener<ClientEvent> onSuccess;
    private Listener<Result<ClientEvent>> onCompletion;
    private DebounceCallback callback;

    public DebouncedRequest(DeviceModel model) {
        this.model = model;
    }

    public void setCallbackHandler(DebounceCallback callback) {
        this.callback = callback;
    }

    public void setOnError(Listener<Throwable> onError) {
        this.onError = onError;
    }

    public void setOnSuccess(Listener<ClientEvent> onSuccess) {
        this.onSuccess = onSuccess;
    }

    public void setOnCompletion(Listener<Result<ClientEvent>> onCompletion) {
        this.onCompletion = onCompletion;
    }

    @Override
    public void run() {
        ClientFuture<ClientEvent> request = model.commit();
        if(callback != null) {
            callback.commitEvent();
        }
        if (onError != null) {
            request.onFailure(onError);
        }
        if (onSuccess != null) {
            request.onSuccess(onSuccess);
        }
        if (onCompletion != null) {
            request.onCompletion(onCompletion);
        }
    }

    public interface DebounceCallback {
        void commitEvent();
    }
}
