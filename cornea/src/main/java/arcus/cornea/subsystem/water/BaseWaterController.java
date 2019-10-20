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
package arcus.cornea.subsystem.water;

import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.WaterSubsystem;
import com.iris.client.model.SubsystemModel;

import org.eclipse.jdt.annotation.Nullable;


public class BaseWaterController<C> extends BaseSubsystemController<C> {

    protected BaseWaterController() {
        super(WaterSubsystem.NAMESPACE);
    }

    protected BaseWaterController(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
    }

    @Nullable
    public WaterSubsystem getWaterSubsystem() {
        return (WaterSubsystem) getModel();
    }

    // TODO deprecate this version
    protected void updateView(C callback) {
        WaterSubsystem subsystem = getWaterSubsystem();
        if(subsystem != null) {
            updateView(callback, subsystem);
        }
    }

    protected void updateView(C callback, WaterSubsystem subsystem) {
        // no-op
    }
}
