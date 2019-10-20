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
package arcus.app.subsystems.doorsnlocks.schedule.model;

import arcus.app.common.schedule.model.ScheduleCommandModelFactory;


public class GarageDoorCommandFactory implements ScheduleCommandModelFactory<GarageDoorCommand> {

    @Override
    public GarageDoorCommand newInstance() {
        return new GarageDoorCommand();
    }
}
