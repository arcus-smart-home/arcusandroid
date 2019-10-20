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

import com.google.common.base.Optional;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.event.Listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseCachedSource<S> {
    private static final Logger logger = LoggerFactory.getLogger(CachedModelSource.class);

    private boolean loaded = false;
    private ClientFuture<S> request = null;

    public boolean isLoaded() {
        return loaded;
    }

    public S get() {
        if(request == null) {
            return null;
        }
        if(!request.isDone()) {
            return null;
        }
        if(request.isError()) {
            return null;
        }
        try {
            return request.get();
        }
        catch (Exception e) {
            logger.warn("Unexpected exception loading cache", e);
            return null;
        }
    }

    public ClientFuture<S> load() {
        if(request != null) {
            return request;
        }

        Optional<S> result = loadFromCache();
        if(result.isPresent()) {
            set(result.get());
            return request;
        }
        return reload();
    }

    public ClientFuture<S> reload() {
        ClientFuture<S> request = this.request;
        if(request != null && !request.isDone()) {
            return request;
        }
        this.request = doLoad().onSuccess(new Listener<S>() {
            @Override
            public void onEvent(S value) {
                set(value);
            }
        });
        return this.request;
    }

    protected abstract Optional<S> loadFromCache();

    protected abstract ClientFuture<S> doLoad();

    protected void set(S value) {
        request = Futures.succeededFuture(value);
        loaded = true;
        onLoaded(value);
    }

    // FIXME: 2/11/16 This is the older behavior and not changed since we are close to release.
    // Need to update the Base/Cached/ModelSource(s) to not emit an added/deleted event
    // and instead only emit a "models changed" event but this is a larger change.
    // FIXME: 2/11/16
    protected void clear() {
        clear(false);
    }

    protected void clear(boolean suppressEvent) {
        request = null;
        loaded = false;
        if(!suppressEvent) {
            onCleared();
        }
    }

    protected void onLoaded(S value) {

    }

    protected void onCleared() {

    }
}
