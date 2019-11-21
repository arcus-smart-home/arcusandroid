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

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import arcus.cornea.CorneaClientFactory;
import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.subsystem.safety.model.Alarm;
import arcus.cornea.utils.Listeners;
import arcus.cornea.utils.ModelSource;
import arcus.cornea.utils.NonNullValues;
import com.iris.client.capability.SafetySubsystem;
import com.iris.client.event.Listener;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.Model;
import com.iris.client.model.ModelChangedEvent;
import com.iris.client.model.SubsystemModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

public class EarlySmokeController extends BaseSubsystemController<EarlySmokeController.Callback> {
    public interface Callback {
        void showError(Throwable error); // Show Boom didn't seem right :P
        void showAlarm(List<Alarm> alarmDevices);
    }

    private static final Logger logger = LoggerFactory.getLogger(EarlySmokeController.class);
    private final Listener unsafeParametersListener = Listeners.runOnUiThread(new Listener() {
        @Override
        public void onEvent(Object dangerWillRobinson) {
            updateView();
        }
    });

    private final static EarlySmokeController INSTANCE;
    static {
        INSTANCE = new EarlySmokeController(SafetySubsystem.NAMESPACE);
        INSTANCE.init();
    }

    EarlySmokeController(String namespace) {
        super(namespace);
    }

    @SuppressWarnings("unused")
    @VisibleForTesting
    EarlySmokeController(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
    }

    public static EarlySmokeController instance() {
        return INSTANCE;
    }

    @Override
    protected void onSubsystemChanged(ModelChangedEvent event) {
        Set<String> changed = event.getChangedAttributes().keySet();
        if (changed.contains(SafetySubsystem.ATTR_SMOKEPREALERTDEVICES) || changed.contains(SafetySubsystem.ATTR_SMOKEPREALERT)) {
            updateView();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void updateView(Callback callback) {
        SafetySubsystem subsystem = (SafetySubsystem) getModel();
        if (subsystem == null) {
            logger.debug("Not loaded, returning until'st we are");
            return;
        }

        if (!DeviceModelProvider.instance().isLoaded()) {
            DeviceModelProvider.instance().load().onSuccess(unsafeParametersListener);
            return;
        }

        Date triggeredTime = NonNullValues.date(subsystem.getLastSmokePreAlertTime());
        List<Alarm> triggered = new ArrayList<>();
        for (String device : subsystem.getSmokePreAlertDevices()) {
            DeviceModel model = getDevice(device);
            if (model != null) {
                triggered.add(new Alarm(model.getId(), model.getName(), "", triggeredTime));
            }
        }

        callback.showAlarm(triggered);
    }

    @Nullable
    private DeviceModel getDevice(String address) {
        Model model = CorneaClientFactory.getModelCache().get(address);
        if (model == null || !(model instanceof DeviceModel)) {
            logger.debug("Couldn't find device/was of wrong type, skipping...... {}", model);
            return null;
        }

        return (DeviceModel) model;
    }
}
