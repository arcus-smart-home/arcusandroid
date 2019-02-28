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
import com.google.common.collect.Lists;
import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.IrisClientFactory;
import com.iris.client.capability.Capability;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.model.Model;
import com.iris.client.model.ProMonitoringSettingsModel;

import java.util.List;

public class ProMonitoringSettingsProvider extends BaseModelProvider<ProMonitoringSettingsModel> {

    private final static ProMonitoringSettingsProvider instance = new ProMonitoringSettingsProvider();

    private ProMonitoringSettingsProvider() {
        super(ProMonitoringSettingsModel.class);
    }

    public static ProMonitoringSettingsProvider getInstance() {
        return instance;
    }

    public ClientFuture<ProMonitoringSettingsModel> getProMonSettings(String placeId) {
        ClientRequest request = new ClientRequest();
        request.setAddress("SERV:promon:" + placeId);
        request.setCommand(Capability.CMD_GET_ATTRIBUTES);

        return Futures.transform(IrisClientFactory.getClient().request(request), new Function<ClientEvent, ProMonitoringSettingsModel>() {
            @Override
            public ProMonitoringSettingsModel apply(ClientEvent clientEvent) {
                Model m = cache.addOrUpdate(clientEvent.getAttributes());
                return (ProMonitoringSettingsModel) m;
            }
        });
    }

    @Override
    protected ClientFuture<List<ProMonitoringSettingsModel>> doLoad(String placeId) {
        ClientRequest request = new ClientRequest();
        request.setAddress("SERV:promon:" + placeId);
        request.setCommand(Capability.CMD_GET_ATTRIBUTES);

        return Futures.transform(IrisClientFactory.getClient().request(request), new Function<ClientEvent, List<ProMonitoringSettingsModel>>() {
            @Override
            public List<ProMonitoringSettingsModel> apply(ClientEvent clientEvent) {
                Model m = cache.addOrUpdate(clientEvent.getAttributes());
                return Lists.newArrayList((ProMonitoringSettingsModel) m);
            }
        });
    }
}
