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

import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.google.common.base.Function;
import arcus.cornea.CorneaClientFactory;
import com.iris.capability.util.Addresses;
import com.iris.client.ClientEvent;
import com.iris.client.IrisClient;
import com.iris.client.bean.PersonAccessDescriptor;
import com.iris.client.capability.Person;
import com.iris.client.capability.Place;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.model.ModelCache;
import com.iris.client.model.PersonModel;
import com.iris.client.model.Store;
import com.iris.client.session.SessionActivePlaceSetEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class PersonModelProvider extends BaseModelProvider<PersonModel> {
    private final ConcurrentMap<String, String> roleMap = new ConcurrentHashMap<>(20);

    private static final PersonModelProvider instance = new PersonModelProvider(
            CorneaClientFactory.getClient(),
            CorneaClientFactory.getModelCache(),
            CorneaClientFactory.getStore(PersonModel.class)
    );

    public static PersonModelProvider instance() {
        return instance;
    }

    private final IrisClient client;
    private final ModelCache cache;

    public final Function<ClientEvent, List<PersonModel>> getPeople =
            new Function<ClientEvent, List<PersonModel>>() {
                @Override
                public List<PersonModel> apply(ClientEvent clientEvent) {
                    Place.ListPersonsWithAccessResponse response = new Place.ListPersonsWithAccessResponse(clientEvent);
                    List<Map<String, Object>> personsWithAccessDescriptors = response.getPersons();
                    if (personsWithAccessDescriptors == null) {
                        return Collections.emptyList();
                    }

                    List<Map<String, Object>> personsOnly = new ArrayList<>(personsWithAccessDescriptors.size() + 1);
                    for (Map<String, Object> personsWithAccessDescriptor : personsWithAccessDescriptors) {
                        PersonAccessDescriptor descriptor = new PersonAccessDescriptor(personsWithAccessDescriptor);
                        String address = (String) descriptor.getPerson().get(Person.ATTR_ADDRESS);
                        if (!TextUtils.isEmpty(address)) {
                            roleMap.put(address, descriptor.getRole());
                        }
                        personsOnly.add(descriptor.getPerson());
                    }

                    return (List) cache.retainAll(Person.NAMESPACE, personsOnly);
                }
            };

    PersonModelProvider(IrisClient client, ModelCache cache, Store<PersonModel> store) {
        super(client, cache, store);
        this.client = client;
        this.cache = cache;
    }

    @Override protected void onPlaceSelected(SessionActivePlaceSetEvent event) {
        roleMap.clear();

        super.onPlaceSelected(event);
    }

    @Override
    protected ClientFuture<List<PersonModel>> doLoad(String placeId) {
        Place.ListPersonsWithAccessRequest request = new Place.ListPersonsWithAccessRequest();
        request.setAddress("SERV:" + Place.NAMESPACE + ":" + placeId);
        return Futures.transform(client.request(request), getPeople);
    }

    /**
     * Gets the role of the person relative to the currently active place ONLY.
     *
     * @param personIDOrAddress address or ID of the person.
     *
     * @return Role or Null if not in the list
     */
    public @NonNull String getRole(String personIDOrAddress) {
        if (TextUtils.isEmpty(personIDOrAddress)) {
            return PersonAccessDescriptor.ROLE_HOBBIT;
        }

        String role = roleMap.get(Addresses.toObjectAddress(Person.NAMESPACE, Addresses.getId(personIDOrAddress)));
        return TextUtils.isEmpty(role) ? PersonAccessDescriptor.ROLE_HOBBIT : role;
    }
}
