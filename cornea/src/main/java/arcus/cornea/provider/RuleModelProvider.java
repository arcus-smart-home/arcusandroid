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
import com.iris.client.IrisClient;
import com.iris.client.capability.Rule;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.model.ModelCache;
import com.iris.client.model.RuleModel;
import com.iris.client.model.Store;
import com.iris.client.service.RuleService;

import java.util.LinkedList;
import java.util.List;

public class RuleModelProvider extends BaseModelProvider<RuleModel> {
    private static final RuleModelProvider INSTANCE = new RuleModelProvider();

    public static RuleModelProvider instance() {
        return INSTANCE;
    }

    private final IrisClient client;
    private final ModelCache cache;

    private final Function<RuleService.ListRulesResponse, List<RuleModel>> getRules =
          new Function<RuleService.ListRulesResponse, List<RuleModel>>() {
              @SuppressWarnings({"unchecked"}) @Override
              public List<RuleModel> apply(RuleService.ListRulesResponse input) {
                  return (List) cache.retainAll(Rule.NAMESPACE, input.getRules());
              }
          };

    public RuleModelProvider() {
        this(
              CorneaClientFactory.getClient(),
              CorneaClientFactory.getModelCache(),
              CorneaClientFactory.getStore(RuleModel.class)
        );
    }

    public RuleModelProvider(
          IrisClient client,
          ModelCache cache,
          Store<RuleModel> store
    ) {
        super(client, cache, store);
        this.client = client;
        this.cache = cache;
    }

    public ClientFuture<List<RuleModel>> getRules() {
        if (RuleModelProvider.instance().isLoaded()) {
            List<RuleModel> rules = new LinkedList<>();
            Iterable<RuleModel> rulesIterable = RuleModelProvider.instance().getStore().values();
            for (RuleModel ruleModel : rulesIterable) {
                rules.add(ruleModel);
            }
            return Futures.succeededFuture(rules);
        } else {
            return RuleModelProvider.instance().load();
        }
    }

    @Override
    protected ClientFuture<List<RuleModel>> doLoad(String placeId) {
        try {
            ClientFuture<RuleService.ListRulesResponse> request = CorneaClientFactory.getService(RuleService.class).listRules(placeId);
            return Futures.transform(request, getRules);
        } catch (Exception ex) {
            return Futures.failedFuture(ex);
        }
    }
}
