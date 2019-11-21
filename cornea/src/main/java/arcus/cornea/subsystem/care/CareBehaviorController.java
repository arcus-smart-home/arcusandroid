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

import androidx.annotation.NonNull;
import android.text.TextUtils;

import arcus.cornea.provider.CareBehaviorTemplateProvider;
import arcus.cornea.provider.CareBehaviorsProvider;
import arcus.cornea.subsystem.care.model.CareBehaviorModel;
import arcus.cornea.subsystem.care.model.CareBehaviorTemplateModel;
import arcus.cornea.subsystem.care.model.CareKeys;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.IrisClient;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.exception.ErrorResponseException;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

public class CareBehaviorController extends BaseCareController<CareBehaviorController.Callback> {
    private static final Logger logger = LoggerFactory.getLogger(CareBehaviorController.class);
    private static final String CARE_NAME_IN_USE = "care.name_in_use";
    private static final String CARE_INVALID_SCHEDLE = "care.duplicate_windows";

    public interface Callback {
        void unsupportedTemplate();
        void editTemplate(CareBehaviorModel editingModel, CareBehaviorTemplateModel templateModel);
        void onError(Throwable error);
    }

    public interface SaveCallback {
        void invalidName();
        void invalidSchedule();
        void saveSuccessful(String behaviorID);
    }

    enum Mode {
        ADD,
        EDIT
    }

    private static final CareBehaviorController INSTANCE;
    static {
        INSTANCE = new CareBehaviorController(CareSubsystem.NAMESPACE);
        INSTANCE.init();
    }
    public static CareBehaviorController instance() {
        return INSTANCE;
    }


    private Mode mode = Mode.ADD;
    private AtomicReference<String> currentTemplateID = new AtomicReference<>();
    private AtomicReference<CareBehaviorTemplateModel> currentTemplate = new AtomicReference<>();
    private AtomicReference<CareBehaviorModel> editingModel = new AtomicReference<>();
    private Reference<SaveCallback> saveCallbackRef = new WeakReference<>(null);

    private final Listener<List<Map<String, Object>>> reloadTemplatesSuccess = Listeners.runOnUiThread(
          new Listener<List<Map<String, Object>>>() {
              @Override
              public void onEvent(List<Map<String, Object>> maps) {
                  Callback callback = getCallback();
                  if (callback == null) {
                      return;
                  }

                  doAddEditBehavior(currentTemplateID.get(), callback);
              }
          }
    );
    private final Listener<Throwable> errorListener = Listeners.runOnUiThread(
          new Listener<Throwable>() {
              @Override
              public void onEvent(Throwable cause) {
                  if (cause instanceof ErrorResponseException) {
                      ErrorResponseException ex = (ErrorResponseException) cause;
                      if (CARE_NAME_IN_USE.equals(ex.getCode())) {
                          nameInUseError();
                          return;
                      }
                      else if (CARE_INVALID_SCHEDLE.equals(ex.getCode())) {
                          invalidScheduleError();
                          return;
                      }
                  }

                  onError(cause);
              }
          }
    );
    private final Listener<CareSubsystem.AddBehaviorResponse> saveResponse = Listeners.runOnUiThread(
          new Listener<CareSubsystem.AddBehaviorResponse>() {
              @Override
              public void onEvent(CareSubsystem.AddBehaviorResponse addBehaviorResponse) {
                  successfulSave(addBehaviorResponse.getId());
              }
          }
    );
    private final Listener<CareSubsystem.UpdateBehaviorResponse> updateResponse = Listeners.runOnUiThread(
          new Listener<CareSubsystem.UpdateBehaviorResponse>() {
              @Override
              public void onEvent(CareSubsystem.UpdateBehaviorResponse updateBehaviorResponse) {
                  String id = editingModel.get() != null ? editingModel.get().getBehaviorID() : "";
                  updateSuccessful(id);
              }
          }
    );

    protected CareBehaviorController(String namespace) {
        super(namespace);
    }

    protected CareBehaviorController(ModelSource<SubsystemModel> subsystem, IrisClient client) {
        super(subsystem);
    }

    public ListenerRegistration setSaveCallback(SaveCallback callback) {
        this.saveCallbackRef = new WeakReference<>(callback);

        return Listeners.wrap(saveCallbackRef);
    }

    protected SaveCallback getSaveCallback() {
        if (saveCallbackRef == null) {
            return null;
        }

        return saveCallbackRef.get();
    }

    public void hardReset() {
        currentTemplateID.set(null);
        editingModel.set(null);
        currentTemplate.set(null);
    }

    public void editExistingBehaviorByID(String existingTemplateID) {
        Callback callback = getCallback();
        if (callback == null) {
            return;
        }

        String previous = currentTemplateID.getAndSet(existingTemplateID);
        if (!TextUtils.isEmpty(previous) && previous.equals(existingTemplateID)) { // Editing the same.
            callback.editTemplate(editingModel.get(), currentTemplate.get());
            return;
        }

        editingModel.set(null);
        currentTemplate.set(null);
        mode = Mode.EDIT;
        if (behaviorsLoaded() && templatesLoaded()) {
            doAddEditBehavior(existingTemplateID, callback);
        }
        else {
            CareBehaviorTemplateProvider.instance()
                  .reload()
                  .onSuccess(reloadTemplatesSuccess)
                  .onFailure(errorListener);
            CareBehaviorsProvider.instance()
                  .reload()
                  .onSuccess(reloadTemplatesSuccess)
                  .onFailure(errorListener);
        }
    }

    public void addBehaviorByTemplateID(String templateID) {
        Callback callback = getCallback();
        if (callback == null) {
            return;
        }

        String previous = currentTemplateID.getAndSet(templateID);
        if (!TextUtils.isEmpty(previous) && previous.equals(templateID)) { // Editing the same.
            callback.editTemplate(editingModel.get(), currentTemplate.get());
            return;
        }

        editingModel.set(null);
        currentTemplate.set(null);

        mode = Mode.ADD;
        if (templatesLoaded()) {
            doAddEditBehavior(templateID, callback);
        }
        else {
            CareBehaviorTemplateProvider.instance()
                  .reload()
                  .onSuccess(reloadTemplatesSuccess)
                  .onFailure(errorListener);
        }
    }

    protected void onError(final Throwable cause) {
        Callback callback = getCallback();
        if (callback != null) {
            callback.onError(cause);
        }
    }

    protected void doAddEditBehavior(@NonNull String searchFor, @NonNull Callback callback) {
        CareBehaviorModel behaviorModel = null;
        CareBehaviorTemplateModel templateModel = null;
        String templateID = searchFor;

        if (Mode.EDIT.equals(mode)) {
            Map<String, Object> behavior = CareBehaviorsProvider.instance().getById(searchFor);
            if (behavior != null) {
                templateID = (String) behavior.get(CareKeys.ATTR_BEHAVIOR_TEMPLATEID.attrName());
                behaviorModel = CareBehaviorModel.fromMap(behavior, templateID);
            }
        }

        Map<String, Object> template = CareBehaviorTemplateProvider.instance().getById(templateID);
        if (template != null) {
            if (behaviorModel == null) {
                behaviorModel = CareBehaviorModel.fromMap(template, templateID);
            }
            templateModel = CareBehaviorTemplateModel.fromMap(template);
        }

        if (behaviorModel == null || templateModel == null) {
            logger.debug("Unable to create behavior/template {} {}", behaviorModel, templateModel);
            callback.unsupportedTemplate();
        }
        else {
            behaviorModel.setRequiresTimeWindows(templateModel.requiresTimeWindows());
            editingModel.set(behaviorModel);
            currentTemplate.set(templateModel);
            callback.editTemplate(behaviorModel, templateModel);
        }
    }

    public void save() {
        Callback callback = getCallback();
        if (callback == null) {
            return;
        }

        CareSubsystem careSubsystem = getCareSubsystemModel();
        CareBehaviorModel careBehaviorModel = editingModel.get();
        if (careSubsystem == null) {
            callback.onError(new RuntimeException("Unable to get subsystem model."));
            return;
        }
        else if (careBehaviorModel == null) {
            callback.onError(new RuntimeException("Unable to get care behavior model."));
            return;
        }

        if (Mode.EDIT.equals(mode)) {
            careSubsystem.updateBehavior(careBehaviorModel.toMap())
                  .onSuccess(updateResponse)
                  .onFailure(errorListener);
        }
        else {
            careSubsystem.addBehavior(careBehaviorModel.toMap())
                  .onSuccess(saveResponse)
                  .onFailure(errorListener);
        }
    }

    protected void invalidScheduleError() {
        SaveCallback callback = getSaveCallback();
        if (callback == null) {
            return;
        }

        callback.invalidSchedule();
    }

    protected void nameInUseError() {
        SaveCallback callback = getSaveCallback();
        if (callback == null) {
            return;
        }

        callback.invalidName();
    }

    protected void successfulSave(String id) {
        hardReset();

        SaveCallback callback = getSaveCallback();
        if (callback == null) {
            return;
        }

        callback.saveSuccessful(id);
    }

    protected void updateSuccessful(String id) {
        hardReset();
        reloadBehaviors();

        SaveCallback callback = getSaveCallback();
        if (callback == null) {
            return;
        }

        callback.saveSuccessful(id);
    }
}
