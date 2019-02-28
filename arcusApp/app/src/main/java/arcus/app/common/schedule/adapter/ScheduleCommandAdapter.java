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
package arcus.app.common.schedule.adapter;

import android.content.Context;

import arcus.cornea.utils.TimeOfDay;
import arcus.app.common.schedule.model.ScheduleCommandModel;

import java.util.List;

/**
 * ListView adapter for rendering scheduled commands represented by a {@link ScheduleCommandModel}.
 */
public class ScheduleCommandAdapter extends AbstractScheduleCommandAdapter<ScheduleCommandModel> {

    private final boolean useLightColorScheme;

    public ScheduleCommandAdapter(Context context, List<ScheduleCommandModel> scheduleCommandModels, boolean useLightColorScheme) {
        super(context);
        addAll(scheduleCommandModels);
        this.useLightColorScheme = useLightColorScheme;
    }

    @Override public TimeOfDay getEventTimeOfDay(int position) {
        return getItem(position).getTime();
    }

    @Override
    public String getEventAbstract(int position) {
        return getItem(position).getCommandAbstract();
    }

    @Override
    public boolean useLightColorScheme() {
        return useLightColorScheme;
    }
}
