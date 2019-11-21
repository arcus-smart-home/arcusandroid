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
package arcus.cornea;

import androidx.annotation.NonNull;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import arcus.cornea.controller.IRuleController;
import arcus.cornea.dto.RuleCategoryCounts;
import arcus.cornea.provider.RuleModelProvider;
import arcus.cornea.provider.RuleTemplateModelProvider;

import arcus.cornea.utils.Listeners;
import com.iris.client.capability.Rule;
import com.iris.client.event.ClientFuture;
import com.iris.client.event.Futures;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.RuleModel;
import com.iris.client.model.RuleTemplateModel;
import com.iris.client.service.RuleService;
import com.iris.client.session.SessionActivePlaceSetEvent;
import com.iris.client.session.SessionEvent;
import com.iris.client.session.SessionExpiredEvent;
import com.iris.client.util.Result;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class RuleController implements IRuleController {
    private static final Logger logger = LoggerFactory.getLogger(RuleController.class);
    private static final RuleController INSTANCE;
    public static final String OTHER = "Other";
    public static final String ALL_RULES = "All Rules";
    private AtomicReference<ClientFuture<RuleCategoryCounts>> ruleCategoryCountsFuture;
    private ListenerRegistration modelListener;
    private static final Comparator<RuleTemplateModel> sortTemplatesByName = new Comparator<RuleTemplateModel>() {
        @Override
        public int compare(RuleTemplateModel lhs, RuleTemplateModel rhs) {
            if (Strings.isNullOrEmpty(lhs.getName()) || Strings.isNullOrEmpty(rhs.getName())) {
                return 0;
            }

            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    };
    private static final Comparator<RuleModel> sortRulesByName = new Comparator<RuleModel>() {
        @Override
        public int compare(RuleModel lhs, RuleModel rhs) {
            if (Strings.isNullOrEmpty(lhs.getName()) || Strings.isNullOrEmpty(rhs.getName())) {
                return 0;
            }

            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    };

    static {
        INSTANCE = new RuleController();
    }

    RuleController() { // Make injectable?
        ruleCategoryCountsFuture = new AtomicReference<>(null);

        // On logout -> login these are reloaded immediately.  However, if logging in for the first time, they wait until looked at
        RuleModelProvider.instance().reload();
        RuleTemplateModelProvider.instance().reload(); // Load the tempates in the event the user goes Login -> Side menu -> Rules
        CorneaClientFactory.getClient().addSessionListener(new Listener<SessionEvent>() {
            @Override
            public void onEvent(SessionEvent sessionEvent) {
                if (sessionEvent instanceof SessionActivePlaceSetEvent || sessionEvent instanceof SessionExpiredEvent) {
                    reset();
                }
            }
        });
    }

    static IRuleController instance() {
        return INSTANCE;
    }

    protected void reset() {
        ruleCategoryCountsFuture.set(null);
        if (modelListener != null) {
            modelListener.remove();
        }
    }


    /**
     *
     * Attempts to disable a rule.
     * Checks the model passed in to see if already disabled and invokes success callback if it is.
     *
     * @param model model to disable
     * @param listeners callback listeners
     */
    public void disableRule(@NonNull final RuleModel model, @NonNull final RuleUpdateListeners listeners) {
        Preconditions.checkNotNull(model);
        Preconditions.checkNotNull(listeners);

        if (model.getState().equals(RuleModel.STATE_DISABLED)) {
            // If already disabled and attempting to disable the rule....
            listeners.ruleUpdateSuccess(model.getId());
            return;
        }

        model.disable().onCompletion(Listeners.runOnUiThread(new Listener<Result<Rule.DisableResponse>>() {
            @Override
            public void onEvent(Result<Rule.DisableResponse> clientEventResult) {
                if (clientEventResult.isError()) {
                    listeners.ruleUpdateFailed(clientEventResult.getError());
                }
                else {
                    listeners.ruleUpdateSuccess(model.getId());
                }
            }
        }));
    }

    /**
     *
     * Attempts to enable a rule.
     * Checks the model passed in to see if already enabled and invokes success callback if it is.
     *
     * @param model model to disable
     * @param listeners callback listeners
     */
    public void enableRule(@NonNull final RuleModel model, @NonNull final RuleUpdateListeners listeners) {
        Preconditions.checkNotNull(model);
        Preconditions.checkNotNull(listeners);

        if (model.getState().equals(RuleModel.STATE_ENABLED)) {
            // If already enabled and attempting to enable the rule....
            listeners.ruleUpdateSuccess(model.getId());
            return;
        }

        model.enable().onCompletion(Listeners.runOnUiThread(new Listener<Result<Rule.EnableResponse>>() {
            @Override
            public void onEvent(Result<Rule.EnableResponse> clientEventResult) {
                if (clientEventResult.isError()) {
                    listeners.ruleUpdateFailed(clientEventResult.getError());
                }
                else {
                    listeners.ruleUpdateSuccess(model.getId());
                }
            }
        }));
    }

    @Override public void listRules(@NonNull final RuleCallbacks callbacks) {
        if (RuleModelProvider.instance().isLoaded()) {
            callbacks.rulesLoaded(Lists.newArrayList(RuleModelProvider.instance().getStore().values()));
            return;
        }

        RuleModelProvider.instance() // This uses the future's result, we need the stores result if it's loaded already.
              .load()
              .onSuccess(
                    Listeners.runOnUiThread(new Listener<List<RuleModel>>() {
                        @Override public void onEvent(List<RuleModel> ruleModels) {
                            try {
                                callbacks.rulesLoaded(ruleModels);
                            }
                            catch (Exception ex) {
                                logger.debug("Cannot emit callback for rules loaded.", ex);
                            }
                        }
                    })
              )
              .onFailure(
                    Listeners.runOnUiThread(new Listener<Throwable>() {
                        @Override public void onEvent(Throwable throwable) {
                            callbacks.requestError(throwable);
                        }
                    })
              );
    }


    public void listSections(@NonNull final RuleCallbacks callbacks) {
        Preconditions.checkNotNull(callbacks);
        Listeners.clear(modelListener);
        modelListener = RuleModelProvider.instance().addStoreLoadListener(Listeners.runOnUiThread(new Listener<List<RuleModel>>() {
            @Override
            public void onEvent(List<RuleModel> ruleModels) {

                Map<String,RuleDeviceSection> map = new TreeMap<>();
                //for each rule model
                for (RuleModel ruleModel : ruleModels) {
                    String  categoryResult = ALL_RULES;
                    if (map.containsKey(categoryResult)){
                        RuleDeviceSection ruleDeviceSection =  map.get(categoryResult);
                        ruleDeviceSection.addRule(ruleModel);
                    } else {
                        RuleDeviceSection ruleDeviceSection =  new RuleDeviceSection();
                        ruleDeviceSection.addRule(ruleModel);
                        map.put(categoryResult,ruleDeviceSection );
                    }

                }
                callbacks.sectionsLoaded(map);
            }
        }));



    }


    @Override
    public void getRuleTemplatesByCategory(@NonNull final String category, @NonNull final RuleTemplateCallbacks callback) {
        if (Strings.isNullOrEmpty(category)) {
            return;
        }
        Preconditions.checkNotNull(callback);

        RuleTemplateModelProvider.instance().reload().onSuccess(Listeners.runOnUiThread(new Listener<List<RuleTemplateModel>>() {
            @Override
            public void onEvent(List<RuleTemplateModel> ruleTemplateModels) {
                List<RuleTemplateModel> results = new ArrayList<>();

                for (RuleTemplateModel model : ruleTemplateModels) {
                    if (model.getCategories().contains(category)) {
                        results.add(model);
                    }
                }

                Collections.sort(results, sortTemplatesByName);
                callback.templatesLoaded(results);
            }
        }))
        .onFailure(new Listener<Throwable>() {
            @Override
            public void onEvent(Throwable throwable) {
                logger.debug("Received Error fetching templates. Using old (possibly stale) values.", throwable);
                callback.templatesLoaded(Lists.newArrayList(RuleTemplateModelProvider.instance().getStore().values(sortTemplatesByName)));
            }
        });
    }

    @Override
    public ClientFuture<RuleCategoryCounts> getCategories() {
        ClientFuture<RuleCategoryCounts> future = ruleCategoryCountsFuture.get();
        if (future != null && (future.isDone() && !future.isError())) { // done and not an error.
            return future;
        }
        else {
            return doGetCategories();
        }
    }

    @Override
    public ClientFuture<RuleCategoryCounts> reloadCategories() {
        ClientFuture<RuleCategoryCounts> future = ruleCategoryCountsFuture.get();
        if (future != null && !future.isDone()) {
            return future;
        }
        else {
            return doGetCategories();
        }
    }

    private ClientFuture<RuleCategoryCounts> doGetCategories() {
        UUID place = CorneaClientFactory.getClient().getActivePlace();
        if (place == null) {
            return Futures.failedFuture(new RuntimeException("Is Client connected? Place was missing."));
        }

        RuleService ruleService = CorneaClientFactory.getService(RuleService.class);
        String activePlace = place.toString();

        ClientFuture<RuleCategoryCounts> future = Futures.transform(ruleService.getCategories(activePlace),
              new Function<RuleService.GetCategoriesResponse, RuleCategoryCounts>() {
                  @Override
                  public RuleCategoryCounts apply(RuleService.GetCategoriesResponse input) {
                      return new RuleCategoryCounts(input);
                  }
              });

        ruleCategoryCountsFuture.set(future);
        return future;
    }

    public interface RuleUpdateListeners {
        void ruleUpdateSuccess(String modelID);
        void ruleUpdateFailed(Throwable throwable);
    }

    public interface RuleCallbacks {
        void rulesLoaded(@NonNull List<RuleModel> rules);
        //the string is the ID
        void sectionsLoaded(Map<String,RuleDeviceSection> mapList);
        void requestError(Throwable throwable);
    }

    public interface RuleTemplateCallbacks {
        void templatesLoaded(List<RuleTemplateModel> models);
    }
}
