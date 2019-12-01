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
import arcus.cornea.dto.RuleCategoryCounts;

import com.iris.client.IrisClient;
import com.iris.client.capability.RuleTemplate;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.model.ModelCache;
import com.iris.client.model.RuleTemplateModel;
import com.iris.client.model.Store;
import com.iris.client.service.RuleService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class RuleTemplateModelProvider extends BaseModelProvider<RuleTemplateModel> {
    private static final RuleTemplateModelProvider INSTANCE = new RuleTemplateModelProvider();

    public static RuleTemplateModelProvider instance() {
        return INSTANCE;
    }

    private final IrisClient client;
    private final ModelCache cache;

    private static final Comparator<RuleTemplateModel> SORTED_BY_NAME = (lhs, rhs) -> {
        if (lhs.getName() == null || rhs.getName() == null) {
            return 0;
        }

        return lhs.getName().compareToIgnoreCase(rhs.getName());
    };

    private final Function<RuleService.ListRuleTemplatesResponse, List<RuleTemplateModel>> getRuleTemplates =
          new Function<RuleService.ListRuleTemplatesResponse, List<RuleTemplateModel>>() {
              @SuppressWarnings({"unchecked"}) @Override
              public List<RuleTemplateModel> apply(RuleService.ListRuleTemplatesResponse input) {
                  return (List) cache.retainAll(RuleTemplate.NAMESPACE, input.getRuleTemplates());
              }
          };

    public RuleTemplateModelProvider() {
        this(
              CorneaClientFactory.getClient(),
              CorneaClientFactory.getModelCache(),
              CorneaClientFactory.getStore(RuleTemplateModel.class)
        );
    }

    public RuleTemplateModelProvider(
          IrisClient client,
          ModelCache cache,
          Store<RuleTemplateModel> store
    ) {
        super(client, cache, store);
        this.client = client;
        this.cache = cache;
    }

    public ClientFuture<List<RuleTemplateModel>> getTemplatesByCategoryName(String category) {
        return reload().transform(templates -> {
            if (templates == null) {
                return Collections.emptyList();
            }

            List<RuleTemplateModel> results = new ArrayList<>();

            for (RuleTemplateModel model : templates) {
                if (model.getCategories().contains(category)) {
                    results.add(model);
                }
            }

            Collections.sort(results, SORTED_BY_NAME);
            return results;
        });
    }

    public ClientFuture<RuleCategoryCounts> getRuleCategoryCounts() {
        String placeId = getPlaceID();
        if (placeId == null) {
            return Futures.failedFuture(new RuntimeException("Is Client connected? Place was missing."));
        } else {
            RuleService ruleService = CorneaClientFactory.getService(RuleService.class);
            return Futures.transform(ruleService.getCategories(placeId), RuleCategoryCounts::new);
        }
    }

    @Override
    protected ClientFuture<List<RuleTemplateModel>> doLoad(String placeId) {
        ClientFuture<RuleService.ListRuleTemplatesResponse> request =
              CorneaClientFactory.getService(RuleService.class).listRuleTemplates(placeId);

        return Futures.transform(request, getRuleTemplates);
    }
}
