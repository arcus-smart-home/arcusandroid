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
package arcus.app.subsystems.scenes.schedule;

import arcus.cornea.utils.DayOfWeek;
import arcus.cornea.utils.TimeOfDay;
import com.iris.client.capability.Scene;
import arcus.app.common.schedule.model.AbstractScheduleCommandModel;
import arcus.app.common.utils.GlobalSetting;


public class SceneCommand extends AbstractScheduleCommandModel {

    // Default command time (when creating a new event)
    private final static TimeOfDay DEFAULT_COMMAND_TIME = new TimeOfDay(18, 0, 0);

    public SceneCommand() {
        super(GlobalSetting.SCENE_SCHEDULER_NAME, DEFAULT_COMMAND_TIME, DayOfWeek.SUNDAY);
        setCommandMessageType(Scene.FireRequest.NAME);
    }


}
