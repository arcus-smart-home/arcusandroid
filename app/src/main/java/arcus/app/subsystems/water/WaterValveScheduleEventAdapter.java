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
package arcus.app.subsystems.water;

import android.content.Context;

import arcus.cornea.utils.TimeOfDay;
import arcus.app.R;
import arcus.app.common.schedule.adapter.AbstractScheduleCommandAdapter;
import arcus.app.subsystems.water.schedule.WaterScheduleDay;

import java.util.List;


public class WaterValveScheduleEventAdapter extends AbstractScheduleCommandAdapter<WaterScheduleDay> {

    public WaterValveScheduleEventAdapter(Context context, List<WaterScheduleDay> events) {
        super(context);
        addAll(events);
    }

    @Override public TimeOfDay getEventTimeOfDay(int position) {
        return getItem(position).getTimeOfDay();
    }

    @Override
    public String getEventAbstract(int position) {
        WaterScheduleDay event = getItem(position);

        if (event.isOn()){
            return getContext().getString(R.string.water_sched_valve_open);
        } else {
            return getContext().getString(R.string.water_sched_valve_close);
        }


    }

    @Override
    public boolean useLightColorScheme() {
        return true;
    }
}
