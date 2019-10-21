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
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.subsystem.alarm.AlarmSubsystemController;

import com.iris.client.ClientEvent;
import com.iris.client.ClientRequest;
import com.iris.client.IrisClientFactory;
import com.iris.client.capability.AlarmSubsystem;
import com.iris.client.capability.Capability;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.model.AlarmIncidentModel;
import com.iris.client.model.Model;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class AlarmIncidentProvider extends BaseModelProvider<AlarmIncidentModel> {

    private final static AlarmIncidentProvider instance = new AlarmIncidentProvider();

    private AlarmIncidentProvider() {
        super(AlarmIncidentModel.class);
    }

    public static AlarmIncidentProvider getInstance() {
        return instance;
    }

    public ClientFuture<AlarmIncidentModel> getIncident(String address) {
        if (StringUtils.isEmpty(address)) {
            return Futures.failedFuture(new IllegalArgumentException("I can't help you if you don't give me an incident address."));
        }

        ClientRequest request = new ClientRequest();
        request.setAddress(address);
        request.setCommand(Capability.CMD_GET_ATTRIBUTES);

        return Futures.transform(IrisClientFactory.getClient().request(request), new Function<ClientEvent, AlarmIncidentModel>() {
            @Override
            public AlarmIncidentModel apply(ClientEvent clientEvent) {
                Model m = cache.addOrUpdate(clientEvent.getAttributes());
                return (AlarmIncidentModel) m;
            }
        });
    }

    @Override
    protected ClientFuture<List<AlarmIncidentModel>> doLoad(String placeId) {
        return Futures.transform(AlarmSubsystemController.getInstance().requestIncidentList(), new Function<AlarmSubsystem.ListIncidentsResponse, List<AlarmIncidentModel>>() {
            @Override
            public List<AlarmIncidentModel> apply(AlarmSubsystem.ListIncidentsResponse listIncidentsResponse) {
                IrisClientFactory.getModelCache().addOrUpdate(listIncidentsResponse.getIncidents());
                return Lists.newArrayList(CorneaClientFactory.getStore(AlarmIncidentModel.class).values());
            }
        });
    }
}