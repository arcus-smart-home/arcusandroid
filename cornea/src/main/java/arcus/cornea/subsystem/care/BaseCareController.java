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

import androidx.annotation.CallSuper;
import androidx.annotation.Nullable;

import arcus.cornea.provider.CareBehaviorTemplateProvider;
import arcus.cornea.provider.CareBehaviorsProvider;
import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.CareSubsystem;
import com.iris.client.event.ClientFuture;
import com.iris.client.model.ModelAddedEvent;
import com.iris.client.model.SubsystemModel;

import java.util.List;
import java.util.Map;

public class BaseCareController<C> extends BaseSubsystemController<C> {
    protected BaseCareController(String namespace) {
        super(namespace);
    }

    protected BaseCareController(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
    }

    protected boolean templatesLoaded() {
        return CareBehaviorTemplateProvider.instance().isLoaded();
    }

    protected boolean behaviorsLoaded() {
        return CareBehaviorsProvider.instance().isLoaded();
    }

    @CallSuper @Override protected void onSubsystemLoaded(ModelAddedEvent event) {
        SubsystemModel careSubsystem = getModel();
        if (careSubsystem == null) {
            return;
        }

        CareBehaviorTemplateProvider.instance().setSubsystemAddress(careSubsystem.getAddress());
        CareBehaviorsProvider.instance().setSubsystemAddress(careSubsystem.getAddress());
        super.onSubsystemLoaded(event);
    }

    protected ClientFuture<List<Map<String, Object>>> reloadBehaviors() {
        return CareBehaviorsProvider.instance().reload();
    }

    protected void reloadTemplates() {
        CareBehaviorTemplateProvider.instance().reload();
    }

    protected @Nullable CareSubsystem getCareSubsystemModel() {
        return (CareSubsystem) super.getModel();
    }
}
