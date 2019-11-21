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
package arcus.cornea.controller;

import androidx.annotation.NonNull;

import arcus.cornea.RuleController;
import arcus.cornea.dto.RuleCategoryCounts;
import com.iris.client.event.ClientFuture;
import com.iris.client.model.RuleModel;

public interface IRuleController {
    void listRules(@NonNull final RuleController.RuleCallbacks callbacks);
    void listSections(@NonNull final RuleController.RuleCallbacks callbacks);
    void getRuleTemplatesByCategory(@NonNull String category, @NonNull RuleController.RuleTemplateCallbacks callbacks);
    ClientFuture<RuleCategoryCounts> getCategories();
    ClientFuture<RuleCategoryCounts> reloadCategories();

    void enableRule(@NonNull final RuleModel model, @NonNull final RuleController.RuleUpdateListeners listeners);
    void disableRule(@NonNull final RuleModel model, @NonNull final RuleController.RuleUpdateListeners listeners);
}
