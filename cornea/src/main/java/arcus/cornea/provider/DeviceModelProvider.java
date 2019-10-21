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

import com.google.common.base.Function;
import arcus.cornea.CorneaClientFactory;
import com.iris.client.ClientEvent;
import com.iris.client.IrisClient;
import com.iris.client.capability.Device;
import com.iris.client.capability.Place;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.ModelCache;
import com.iris.client.model.Store;

import java.util.List;


public class DeviceModelProvider extends BaseModelProvider<DeviceModel> {
    private static final DeviceModelProvider INSTANCE = new DeviceModelProvider();

    public static DeviceModelProvider instance() {
        return INSTANCE;
    }

    private final IrisClient client;
    private final ModelCache cache;

    private final Function<ClientEvent, List<DeviceModel>> getDevices =
            new Function<ClientEvent, List<DeviceModel>>() {
                @Override
                public List<DeviceModel> apply(ClientEvent input) {
                    Place.ListDevicesResponse response = new Place.ListDevicesResponse(input);
                    return (List) cache.retainAll(Device.NAMESPACE, response.getDevices());
                }
            };

    DeviceModelProvider() {
        this(
                CorneaClientFactory.getClient(),
                CorneaClientFactory.getModelCache(),
                CorneaClientFactory.getStore(DeviceModel.class)
        );
    }

    DeviceModelProvider(
            IrisClient client,
            ModelCache cache,
            Store<DeviceModel> store
    ) {
        super(client, cache, store);
        this.client = client;
        this.cache = cache;
    }

    @Override
    protected ClientFuture<List<DeviceModel>> doLoad(String placeId) {
        Place.ListDevicesRequest request = new Place.ListDevicesRequest();
        request.setAddress("SERV:" + Place.NAMESPACE + ":" + placeId);
        return Futures
                .transform(
                        client.request(request),
                        getDevices
                );
    }
}
