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

import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.care.model.Settings;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.IrisClient;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.Model;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.SubsystemModel;

import java.util.Set;

public class CareSettingsController extends BaseSubsystemController<CareSettingsController.Callback> {
    public interface Callback {
        void savingChanges();
        void onLoaded(Settings settings);
        void onError(Throwable exception);
    }

    private static final CareSettingsController INSTANCE;
    static {
        INSTANCE = new CareSettingsController(CareSubsystem.NAMESPACE);
        INSTANCE.init();
    }

    public static CareSettingsController instance() {
        return INSTANCE;
    }

    protected CareSettingsController(String namespace) {
        super(namespace);
    }

    CareSettingsController(ModelSource<SubsystemModel> subsystem, IrisClient client) {
        super(subsystem);
    }

    @Override protected void onSubsystemChanged(ModelChangedEvent event) {
        super.onSubsystemChanged(event);
        Set<String> changed = event.getChangedAttributes().keySet();
        if (changed.contains(CareSubsystem.ATTR_SILENT)) {
            updateView();
        }
    }

    public void setSilentAlarm(boolean isSilent) {
        CareSubsystem careSubsystem = (CareSubsystem) getModel();
        if (careSubsystem == null) {
            updateView();
            return;
        }

        showSavingChanges();
        careSubsystem.setSilent(isSilent);
        ((Model) careSubsystem)
              .commit()
              .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                  @Override
                  public void onEvent(Throwable throwable) {
                      Callback callback = getCallback();
                      if (callback == null) {
                          return;
                      }

                      callback.onError(throwable);
                  }
              }));
    }

    protected void showSavingChanges() {
        Callback callback = getCallback();
        if (callback == null) {
            return;
        }

        callback.savingChanges();
    }

    @Override protected void updateView(Callback callback) {
        CareSubsystem careSubsystem = (CareSubsystem) getModel();
        if (careSubsystem == null) {
            return;
        }

        callback.onLoaded(new Settings(Boolean.TRUE.equals(careSubsystem.getSilent())));
    }
}
