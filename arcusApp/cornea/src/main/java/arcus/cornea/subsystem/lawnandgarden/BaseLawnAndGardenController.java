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
package arcus.cornea.subsystem.lawnandgarden;

import arcus.cornea.subsystem.BaseSubsystemController;
import arcus.cornea.utils.ModelSource;
import com.iris.client.capability.LawnNGardenSubsystem;
import com.iris.client.model.SubsystemModel;

import org.eclipse.jdt.annotation.Nullable;


public class BaseLawnAndGardenController<C> extends BaseSubsystemController<C> {

    protected BaseLawnAndGardenController() {
        super(LawnNGardenSubsystem.NAMESPACE);
    }

    protected BaseLawnAndGardenController(ModelSource<SubsystemModel> subsystem) {
        super(subsystem);
    }

    @Nullable
    public LawnNGardenSubsystem getLawnNGardenSubsystem() {
        return (LawnNGardenSubsystem) getModel();
    }

    protected void updateView(C callback) {
        LawnNGardenSubsystem subsystem = getLawnNGardenSubsystem();
        if(subsystem != null) {
            updateView(callback, subsystem);
        }
    }

    protected void updateView(C callback, LawnNGardenSubsystem subsystem) {
        // no-op
    }
}
