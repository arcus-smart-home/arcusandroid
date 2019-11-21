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
package arcus.cornea.rules;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import com.google.common.collect.Lists;
import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.RuleModelProvider;
import arcus.cornea.provider.RuleTemplateModelProvider;
import arcus.cornea.rules.model.RuleDeviceSection;
import arcus.cornea.rules.model.RuleProxyModel;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.capability.util.Addresses;
import com.iris.client.ClientRequest;
import com.iris.client.capability.Rule;
import com.iris.client.capability.RuleTemplate;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelDeletedEvent;
import com.iris.client.model.RuleModel;
import com.iris.client.model.RuleTemplateModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RuleListingController {
    public interface Callback {
        void onError(@NonNull Throwable throwable);
        void onRulesLoaded(@NonNull Map<String, RuleDeviceSection> rules);
    }

    private static final Logger logger = LoggerFactory.getLogger(RuleListingController.class);
    private static final int DEBOUNCE_INTERVAL_MS = 500;
    private Reference<Callback> callbackRef;
    private ListenerRegistration addedListenerReg;
    private ListenerRegistration changedListenerReg;
    private ListenerRegistration removedListenerReg;
    private Handler handler = new Handler(Looper.getMainLooper());

    private final Listener<ModelAddedEvent> addedListener = new Listener<ModelAddedEvent>() {
        @Override public void onEvent(ModelAddedEvent modelEvent) {
            RuleModel rule = (RuleModel) modelEvent.getModel();
            if (rule != null) {
                callOnAdded(rule);
            }
        }
    };
    private final Listener<ModelChangedEvent> changedListener = new Listener<ModelChangedEvent>() {
        @Override public void onEvent(ModelChangedEvent modelEvent) {
            RuleModel rule = (RuleModel) modelEvent.getModel();
            if (rule != null) {
                callOnUpdated(rule);
            }
        }
    };
    private final Listener<ModelDeletedEvent> removedListener = new Listener<ModelDeletedEvent>() {
        @Override public void onEvent(ModelDeletedEvent modelEvent) {
            RuleModel rule = (RuleModel) modelEvent.getModel();
            if (rule != null) {
                callOnRemoved(rule);
            }
        }
    };
    private final Listener<Throwable> errorListener = new Listener<Throwable>() {
        @Override public void onEvent(Throwable throwable) {
            callOnError(throwable);
        }
    };


    public RuleListingController() {
        callbackRef = new WeakReference<>(null);
    }

    public ListenerRegistration setCallback(@NonNull Callback callback) {
        callbackRef = new WeakReference<>(callback);

        return new ListenerRegistration() {
            @Override public boolean isRegistered () {
                return callbackRef.get() != null;
            }

            @Override public boolean remove () {
                boolean registered = isRegistered();
                callbackRef.clear();
                Listeners.clear(addedListenerReg);
                Listeners.clear(changedListenerReg);
                Listeners.clear(removedListenerReg);
                return registered;
            }
        };
    }

    public void listAllRules() {
        if (RuleModelProvider.instance().isLoaded()) {
            List<RuleModel> ruleModels = Lists.newLinkedList(RuleModelProvider.instance().getStore().values());
            processRuleModels(ruleModels);
        }
        else {
            RuleModelProvider.instance().load()
                  .onFailure(errorListener)
                  .onSuccess(new Listener<List<RuleModel>>() {
                      @Override public void onEvent(List<RuleModel> ruleModels) {
                          processRuleModels(ruleModels);
                      }
                  });
        }
    }

    @SuppressWarnings("ConstantConditions") protected void processRuleModels(@NonNull List<RuleModel> ruleModels) {
        if (ruleModels != null && !ruleModels.isEmpty()) {
            loadTemplates(ruleModels); // Only load templates if we have rules to view.
        }
        else {
            callOnRulesLoaded(Collections.<String, RuleDeviceSection>emptyMap());
        }
    }

    protected void loadTemplates(@NonNull final List<RuleModel> ruleModels) {
        RuleTemplateModelProvider.instance().load()
              .onFailure(errorListener)
              .onSuccess(new Listener<List<RuleTemplateModel>>() {
                  @Override public void onEvent(List<RuleTemplateModel> models) {
                      Map<String, RuleDeviceSection> rules = new TreeMap<>();

                      for (RuleModel ruleModel : ruleModels) {
                          RuleProxyModel proxyModel = getProxyWithCategories(ruleModel);
                          for (String category : proxyModel.getCategories()) {
                              RuleDeviceSection section = rules.get(category);
                              if (section == null) {
                                  section = new RuleDeviceSection();
                                  rules.put(category, section);
                              }

                              section.addRule(proxyModel);
                          }
                      }

                      callOnRulesLoaded(rules);

                      // Adding these last so that we remove
                      removedListenerReg = RuleModelProvider.instance().getStore().addListener(ModelDeletedEvent.class, removedListener);
                      changedListenerReg = RuleModelProvider.instance().getStore().addListener(ModelChangedEvent.class, changedListener);
                      addedListenerReg   = RuleModelProvider.instance().getStore().addListener(ModelAddedEvent.class, addedListener);
                  }
              });
    }

    public void updateRuleEnabled(RuleProxyModel model) {
        if (passesPreCheck(model)) {
            ClientRequest request = getRequest(model.getAddress(), model.isEnabled() ? Rule.CMD_ENABLE : Rule.CMD_DISABLE);
            callPlatform(request);
        }
    }

    public void deleteRule(RuleProxyModel model) {
        if (passesPreCheck(model)) {
            ClientRequest request = getRequest(model.getAddress(), Rule.CMD_DELETE);
            callPlatform(request);
        }
    }

    private boolean passesPreCheck(RuleProxyModel model) {
        if (model.getId() == null || model.getId().isEmpty()) {
            callOnError(new RuntimeException("Cannot update a model w/o an ID/Address."));
            return false;
        }

        return true;
    }

    protected ClientRequest getRequest(String ruleAddress, String command) {
        ClientRequest request = new ClientRequest();

        request.setAddress(ruleAddress);
        request.setCommand(command);
        request.setTimeoutMs(30_000);
        request.setRestfulRequest(false);

        return request;
    }

    protected void callPlatform(ClientRequest request) {
        if (!CorneaClientFactory.isConnected()) {
            callOnError(new RuntimeException("Client is not connected. Cannot continue."));
        }
        else { // Deleted, Updated, Added, will emit because of the store callbacks - only need to handle failures.
            CorneaClientFactory.getClient().request(request).onFailure(errorListener);
        }
    }

    protected void callOnError(@NonNull Throwable throwable) {
        handler.post(new WrappedRunnable<Throwable>(throwable) {
            @Override public void call(@NonNull Callback callback) {
                callback.onError(getItem());
            }
        });
    }

    protected void callOnAdded(@NonNull RuleModel ruleModel) {
        handler.post(new WrappedRunnable<RuleProxyModel>(getProxyWithCategories(ruleModel)) {
            @Override public void call(@NonNull Callback callback) {
                listAllRules();
            }
        });
    }

    protected void callOnRemoved(@NonNull RuleModel ruleModel) {
        handler.post(new WrappedRunnable<RuleProxyModel>(getProxy(ruleModel)) {
            @Override public void call(@NonNull Callback callback) {
                listAllRules();
            }
        });
    }

    protected void callOnUpdated(@NonNull RuleModel ruleModel) {
        // Updates come through one for each attribute. Debounce these so we get one update ~DEBOUNCE_INTERVAL_MS
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new WrappedRunnable<RuleProxyModel>(getProxy(ruleModel)) {
            @Override public void call(@NonNull Callback callback) {
                listAllRules();
            }
        }, DEBOUNCE_INTERVAL_MS);
    }

    protected void callOnRulesLoaded(@NonNull Map<String, RuleDeviceSection> rules) {
        handler.removeCallbacksAndMessages(null);
        handler.post(new WrappedRunnable<Map<String, RuleDeviceSection>>(rules) {
            @Override public void call(@NonNull Callback callback) {
                callback.onRulesLoaded(getItem());
            }
        });
    }

    protected RuleProxyModel getProxy(@NonNull RuleModel ruleModel) {
        return new RuleProxyModel(
              ruleModel.getId(),
              ruleModel.getName(),
              ruleModel.getDescription(),
              ruleModel.getTemplate(),
              Rule.STATE_ENABLED.equals(ruleModel.get(Rule.ATTR_STATE))
        );
    }

    protected RuleProxyModel getProxyWithCategories(@NonNull RuleModel ruleModel) {
        RuleProxyModel model = getProxy(ruleModel);

        String templateAddress = Addresses.toObjectAddress(RuleTemplate.NAMESPACE, ruleModel.getTemplate());
        ModelSource<RuleTemplateModel> template = RuleTemplateModelProvider.instance().getModel(templateAddress);
        template.load();

        RuleTemplateModel templateModel = template.get();
        if (templateModel != null) {
            model.setCategories(templateModel.getCategories());
        }

        return model;
    }

    private abstract class WrappedRunnable<T> implements Runnable {
        private final @NonNull T item;

        public WrappedRunnable(@NonNull T usedItem) {
            this.item = usedItem;
        }

        public @NonNull T getItem() {
            return item;
        }

        @Override public void run() {
            try {
                Callback callback = callbackRef.get();
                if (callback != null) {
                    call(callback);
                }
            }
            catch (Exception ex) {
                logger.error("Could not dispatch callback.", ex);
            }
        }

        public abstract void call(@NonNull Callback callback);
    }
}
