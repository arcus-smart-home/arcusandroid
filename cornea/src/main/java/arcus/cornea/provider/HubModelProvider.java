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
package arcus.cornea.provider;

import androidx.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import arcus.cornea.CorneaClientFactory;
import com.iris.capability.util.Addresses;
import com.iris.client.ClientEvent;
import com.iris.client.IrisClient;
import com.iris.client.capability.Place;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.model.HubModel;
import com.iris.client.model.Model;
import com.iris.client.model.ModelCache;
import com.iris.client.model.Store;

import java.util.ArrayList;
import java.util.List;

public class HubModelProvider extends BaseModelProvider<HubModel> {
    private static final HubModelProvider INSTANCE;
    static {
        INSTANCE = new HubModelProvider(
              CorneaClientFactory.getClient(),
              CorneaClientFactory.getModelCache(),
              CorneaClientFactory.getStore(HubModel.class)
        );
    }
    private final IrisClient irisClient;
    private final ModelCache modelCache;
    private final Function<ClientEvent, List<HubModel>> getHubs = new Function<ClientEvent, List<HubModel>>() {
        @Override
        public List<HubModel> apply(ClientEvent input) {
            Place.GetHubResponse response = new Place.GetHubResponse(input);
            Model model = modelCache.addOrUpdate(response.getHub());
            if (model != null) {
                model.refresh(); // Load hub values.
            }
            return model == null ? new ArrayList<HubModel>() : Lists.newArrayList((HubModel) model);
        }
    };

    public static HubModelProvider instance() { return INSTANCE; }

    protected HubModelProvider(IrisClient client, ModelCache cache, Store<HubModel> store) {
        super(client, cache, store);
        this.irisClient = client;
        this.modelCache = cache;
    }

    @Nullable
    public HubModel getHubModel() {
        if (!isLoaded()) {
            return null;
        }

        List<HubModel> hubModels = Lists.newArrayList(getStore().values());
        return hubModels.isEmpty() ? null : hubModels.get(0);
    }

    @Override
    protected ClientFuture<List<HubModel>> doLoad(String placeId) {
        try {
            Place.GetHubRequest request = new Place.GetHubRequest();
            request.setTimeoutMs(30_000);
            request.setRestfulRequest(false);
            String placeID = irisClient.getActivePlace().toString();
            request.setAddress(Addresses.toServiceAddress(Place.NAMESPACE) + placeID);

            return Futures.transform(irisClient.request(request), getHubs);
        }
        catch (Exception ex) {
            return Futures.failedFuture(ex);
        }
    }
}
