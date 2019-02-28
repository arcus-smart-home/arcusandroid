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
package arcus.cornea.subsystem.care;

import com.google.common.base.Optional;
import arcus.cornea.provider.CareBehaviorTemplateProvider;
import arcus.cornea.provider.CareBehaviorsProvider;
import arcus.cornea.subsystem.care.model.CareBehaviorModel;
import arcus.cornea.subsystem.care.model.CareBehaviorTemplateModel;
import arcus.cornea.subsystem.care.model.CareKeys;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.LooperExecutor;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class CareBehaviorListController extends BaseCareController<CareBehaviorListController.Callback> {
    private static final Logger logger = LoggerFactory.getLogger(CareBehaviorListController.class);

    private AtomicBoolean performingAction = new AtomicBoolean(false);
    private final Listener<Throwable> onErrorListener = Listeners.runOnUiThread(
          new Listener<Throwable>() {
              @Override public void onEvent(Throwable throwable) {
                  reset();
                  onError(throwable);
              }
          }
    );
    private final Listener<CareSubsystem.RemoveBehaviorResponse> deleteListener =
          new Listener<CareSubsystem.RemoveBehaviorResponse>() {
              @Override
              public void onEvent(CareSubsystem.RemoveBehaviorResponse removeBehaviorResponse) {
                  reset();
                  reloadBehaviors()
                        .onFailure(onErrorListener)
                        .onSuccess(reloadListener);
              }
          };
    private final Listener<CareSubsystem.UpdateBehaviorResponse> updateListener =
          new Listener<CareSubsystem.UpdateBehaviorResponse>() {
              @Override
              public void onEvent(CareSubsystem.UpdateBehaviorResponse updateBehaviorResponse) {
                  reset();
                  updateView();
              }
          };
    private final Listener<List<Map<String, Object>>> reloadListener =
          new Listener<List<Map<String, Object>>>() {
              @Override
              public void onEvent(List<Map<String, Object>> maps) {
                  updateView();
              }
          };

    public interface Callback {
        void onError(Throwable throwable);
        void showBehaviors(List<CareBehaviorModel> templates);
    }



    public static CareBehaviorListController instance() {
        return INSTANCE;
    }

    private static final CareBehaviorListController INSTANCE;
    static {
        INSTANCE = new CareBehaviorListController(CareSubsystem.NAMESPACE);
        INSTANCE.init();
    }
    private final Listener<List<Map<String, Object>>> loadedListener = new Listener<List<Map<String, Object>>>() {
        @Override
        public void onEvent(List<Map<String, Object>> maps) {
            updateView();
        }
    };

    protected CareBehaviorListController(String namespace) {
        super(namespace);
    }

    protected CareBehaviorListController(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
    }

    @Override public ListenerRegistration setCallback(Callback callback) {
        reset();
        reloadBehaviors().onSuccess(reloadListener);
        return super.setCallback(callback);
    }

    public void deleteBehavior(String id) {
        boolean alreadyDeleting = performingAction.getAndSet(true);
        if (alreadyDeleting) {
            return;
        }

        CareSubsystem careSubsystem = getCareSubsystemModel();
        if (careSubsystem == null) {
            onError(new RuntimeException("Unable to load subsystem."));
            reset();
            return;
        }

        careSubsystem
              .removeBehavior(id)
              .onFailure(onErrorListener)
              .onSuccess(deleteListener);
    }

    public void enableBehavior(String id, boolean enable) {
        boolean alreadyEnabling = performingAction.getAndSet(true);
        if (alreadyEnabling) {
            return;
        }

        CareSubsystem careSubsystem = getCareSubsystemModel();
        if (careSubsystem == null) {
            onError(new RuntimeException("Unable to load subsystem."));
            reset();
            return;
        }

        Map<String, Object> behavior = CareBehaviorsProvider.instance().getById(id);
        if (behavior == null) {
            onError(new RuntimeException("Unable to load behavior."));
            reset();
            return;
        }

        behavior.put(CareKeys.ATTR_BEHAVIOR_ENABLED.attrName(), enable);
        careSubsystem
              .updateBehavior(behavior)
              .onFailure(onErrorListener)
              .onSuccess(updateListener);
    }

    protected void reset() {
        performingAction.set(false);
    }

    public boolean deletingOrEnablingBehavior() {
        return performingAction.get();
    }

    @Override protected void updateView(final Callback callback) {
        if (!behaviorsLoaded()) {
            logger.debug("Not updating view - Behaviors not loaded.");
            CareBehaviorsProvider.instance().load().onSuccess(loadedListener);
            return;
        }

        LooperExecutor.getMainExecutor().execute(new Runnable() {
            @Override
            public void run() {
                callback.showBehaviors(buildBehaviorModels());
            }
        });
    }

    protected void onError(final Throwable throwable) {
        final Callback callback = getCallback();
        if (callback == null) {
            return;
        }

        callback.onError(throwable);
    }

    protected List<CareBehaviorModel> buildBehaviorModels() {
        Optional<List<Map<String, Object>>> behaviorMaps = CareBehaviorsProvider.instance().getAll();
        if (!behaviorMaps.isPresent()) {
            return Collections.emptyList();
        }

        List<CareBehaviorModel> careBehaviors = new ArrayList<>(behaviorMaps.get().size());
        for (Map<String, Object> behavior : behaviorMaps.get()) {
            String templateID = (String) behavior.get(CareKeys.ATTR_BEHAVIOR_TEMPLATEID.attrName());
            CareBehaviorModel model = CareBehaviorModel.fromMap(behavior, templateID);

            Map<String, Object> template = CareBehaviorTemplateProvider.instance().getById(templateID);
            if (template != null) {
                model.setDescription(CareBehaviorTemplateModel.fromMap(template).getDescription());
            }

            careBehaviors.add(model);
        }

        return careBehaviors;
    }
}
