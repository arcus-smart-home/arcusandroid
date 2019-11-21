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
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import arcus.cornea.CorneaClientFactory;
import com.iris.client.ClientEvent;
import com.iris.client.IrisClient;
import com.iris.client.bean.CareBehavior;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CareBehaviorsProvider extends BaseNonModelProvider<Map<String, Object>> {
    private static final CareBehaviorsProvider INSTANCE;
    static {
        INSTANCE = new CareBehaviorsProvider();
    }

    private final Function<ClientEvent, List<Map<String, Object>>> transform =
          new Function<ClientEvent, List<Map<String, Object>>>() {
              @Override
              public List<Map<String, Object>> apply(ClientEvent input) {
                  if (input == null) {
                      return Collections.emptyList();
                  }

                  CareSubsystem.ListBehaviorsResponse response = new CareSubsystem.ListBehaviorsResponse(input);
                  List<Map<String, Object>> behaviorsMap = response.getBehaviors();
                  return behaviorsMap == null ? Collections.<Map<String, Object>>emptyList() : behaviorsMap;
              }
          };

    public static CareBehaviorsProvider instance() {
        return INSTANCE;
    }

    protected CareBehaviorsProvider() { this(CorneaClientFactory.getClient()); }

    protected CareBehaviorsProvider(IrisClient client) {
        super(client);
    }

    public @Nullable Map<String, Object> getById(@NonNull String id) {
        if (!isLoaded() || TextUtils.isEmpty(id)) {
            return null;
        }

        Optional<List<Map<String, Object>>> allTpls = getAll();
        if (!allTpls.isPresent()) {
            return null;
        }

        for (Map<String, Object> item : allTpls.get()) {
            if (id.equals(item.get(CareBehavior.ATTR_ID))) {
                return item;
            }
        }

        return null;
    }

    @Override protected ClientFuture<List<Map<String, Object>>> doLoad() {
        String subsystemAddress = getSubsystemAddress();
        if (TextUtils.isEmpty(subsystemAddress)) {
            return Futures.failedFuture(new RuntimeException("Subsystem Address was not loaded. Cannot load Behaviors."));
        }

        CareSubsystem.ListBehaviorsRequest request = new CareSubsystem.ListBehaviorsRequest();
        request.setAddress(subsystemAddress);
        request.setAttributes(Collections.<String, Object>emptyMap());
        request.setTimeoutMs(DEFAULT_TIMEOUT_MS);

        return Futures.transform(getClient().request(request), transform);
    }
}
