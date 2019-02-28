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
package arcus.cornea.subsystem.presence;

import arcus.cornea.provider.DeviceModelProvider;
import arcus.cornea.provider.PersonModelProvider;
import arcus.cornea.subsystem.SubsystemController;
import arcus.cornea.subsystem.presence.model.PresenceModel;
import arcus.cornea.subsystem.presence.model.PresenceState;
import arcus.cornea.utils.AddressableListSource;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.PresenceSubsystem;
import com.iris.client.model.DeviceModel;
import com.iris.client.model.PersonModel;
import com.iris.client.model.SubsystemModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PresenceStatusController extends BasePresenceController<PresenceStatusController.Callback> {

    public interface Callback {
        void showHome(List<PresenceModel> home);
        void showAway(List<PresenceModel> away);
    }

    private static final PresenceStatusController instance;

    static {
        instance = new PresenceStatusController();
        instance.init();
    }

    public static PresenceStatusController instance() {
        return instance;
    }

    PresenceStatusController() {
        this(
                SubsystemController.instance().getSubsystemModel(PresenceSubsystem.NAMESPACE),
                PersonModelProvider.instance().newModelList(),
                PersonModelProvider.instance().newModelList(),
                DeviceModelProvider.instance().newModelList()
        );
    }

    PresenceStatusController(
            ModelSource<SubsystemModel> subsystem,
            AddressableListSource<PersonModel> peopleAtHome,
            AddressableListSource<PersonModel> peopleAway,
            AddressableListSource<DeviceModel> allDevices) {

        super(subsystem, peopleAtHome, peopleAway, allDevices);
    }

    @Override
    protected void updateView(Callback callback, PresenceSubsystem subsystem) {
        callback.showHome(buildHome());
        callback.showAway(buildAway());
    }

    private List<PresenceModel> buildHome() {
        List<PresenceModel> models = new ArrayList<>();
        addPeople(models, peopleAtHome.get(), PresenceState.HOME);
        models.addAll(devicesAtHome());
        Collections.sort(models, presenceSorter);
        return models;
    }

    private List<PresenceModel> buildAway() {
        List<PresenceModel> models = new ArrayList<>();
        addPeople(models, peopleAway.get(), PresenceState.AWAY);
        models.addAll(devicesAway());
        return models;
    }
}
