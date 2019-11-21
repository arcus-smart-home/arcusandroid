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
import android.text.TextUtils;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import arcus.cornea.CorneaClientFactory;
import com.iris.client.ClientEvent;
import com.iris.client.IrisClient;
import com.iris.client.bean.CareBehaviorTemplate;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CareBehaviorTemplateProvider extends BaseNonModelProvider<Map<String, Object>> {
    private static final CareBehaviorTemplateProvider INSTANCE;
    static {
        INSTANCE = new CareBehaviorTemplateProvider();
    }

    private final Function<ClientEvent, List<Map<String, Object>>> transform =
          new Function<ClientEvent, List<Map<String, Object>>>() {
              @Override
              public List<Map<String, Object>> apply(ClientEvent input) {
                  if (input == null) {
                      return Collections.emptyList();
                  }

                  CareSubsystem.ListBehaviorTemplatesResponse response = new CareSubsystem.ListBehaviorTemplatesResponse(input);
                  List<Map<String, Object>> templatesMap = response.getBehaviorTemplates();
                  return templatesMap == null ? Collections.<Map<String, Object>>emptyList() : templatesMap;
              }
          };

    public static CareBehaviorTemplateProvider instance() {
        return INSTANCE;
    }

    protected CareBehaviorTemplateProvider() {
        this(CorneaClientFactory.getClient());
    }

    protected CareBehaviorTemplateProvider(IrisClient client) {
        super(client);
    }

    public @Nullable Map<String, Object> getById(String templateID) {
        if (TextUtils.isEmpty(templateID)) {
            return null;
        }

        Optional<List<Map<String, Object>>> allItems = getAll();
        if (allItems.isPresent()) {
            for (Map<String, Object> item : allItems.get()) {
                if (templateID.equals(item.get(CareBehaviorTemplate.ATTR_ID))) {
                    return item;
                }
            }
        }

        return null;
    }

    @Override protected ClientFuture<List<Map<String, Object>>> doLoad() {
        String subsystemAddress = getSubsystemAddress();
        if (TextUtils.isEmpty(subsystemAddress)) {
            return Futures.failedFuture(new RuntimeException("Subsystem Address was not loaded. Cannot load Behaviors."));
        }

        CareSubsystem.ListBehaviorTemplatesRequest request = new CareSubsystem.ListBehaviorTemplatesRequest();
        request.setAddress(subsystemAddress);
        request.setAttributes(Collections.<String, Object>emptyMap());
        request.setTimeoutMs(DEFAULT_TIMEOUT_MS);

        return Futures.transform(getClient().request(request), transform);
    }
}
