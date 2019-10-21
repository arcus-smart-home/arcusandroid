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
package arcus.cornea.subsystem.safety;

import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.safety.model.Settings;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.SafetySubsystem;
import com.iris.client.event.Listener;
import com.iris.client.event.ListenerRegistration;
import com.iris.client.model.Model;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.ModelEvent;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.WeakReference;
import java.util.Set;

public class SettingsController {

    public interface Callback {
        void showSettings(Settings settings);
        void showUpdateError(Throwable t, Settings currentSettings);
    }

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    private static final SettingsController instance = new SettingsController(
            SubsystemController.instance().getSubsystemModel(SafetySubsystem.NAMESPACE));

    public static SettingsController instance() {
        return instance;
    }

    private ModelSource<SubsystemModel> safety;
    private WeakReference<Callback> callback = new WeakReference<>(null);

    SettingsController(ModelSource<SubsystemModel> safety) {
        this.safety = safety;
        attachListeners();
    }

    private void attachListeners() {
        this.safety.addModelListener(Listeners.runOnUiThread(new Listener<ModelEvent>() {
            @Override
            public void onEvent(ModelEvent modelEvent) {
                if (modelEvent instanceof ModelAddedEvent) {
                    onAdded();
                } else if (modelEvent instanceof ModelChangedEvent) {
                    onChanged(((ModelChangedEvent) modelEvent).getChangedAttributes().keySet());
                }
            }
        }));
    }

    public ListenerRegistration setCallback(Callback callback) {
        if(this.callback.get() == null) {
            logger.warn("Replacing existing callback");
        }
        this.callback = new WeakReference<>(callback);
        updateSettings();
        return Listeners.wrap(this.callback);
    }

    public SafetySubsystem getSubsystem() {
        safety.load();
        return (SafetySubsystem) safety.get();
    }

    public void setSettings(Settings settings) {
        final SafetySubsystem safety = getSubsystem();
        if(safety == null) {
            return;
        }

        safety.setSilentAlarm(settings.isSilentAlarm());
        safety.setWaterShutOff(settings.isWaterShutoffEnabled());

        final Callback callback = this.callback.get();

        // success will result in a value change which will update the view
        ((Model) safety).commit()
                .onFailure(Listeners.runOnUiThread(new Listener<Throwable>() {
                    @Override
                    public void onEvent(Throwable throwable) {
                        if(callback != null) {
                            callback.showUpdateError(throwable,
                                    Settings.builder().from(safety).build());
                        }
                    }
                }));
    }

    private void onAdded() {
        updateSettings();
    }

    private void onChanged(Set<String> changes) {
        if(changes.contains(SafetySubsystem.ATTR_SILENTALARM) || changes.contains(SafetySubsystem.ATTR_WATERSHUTOFF)) {
            updateSettings();
        }
    }

    private void updateSettings() {
        Callback callback = this.callback.get();
        if(callback == null) {
            return;
        }
        SafetySubsystem safety = getSubsystem();
        if(safety == null) {
            return;
        }
        callback.showSettings(Settings.builder().from(safety).build());
    }
}
