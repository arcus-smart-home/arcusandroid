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
package arcus.app.subsystems.lightsnswitches.adapter;

import android.content.Context;

import arcus.cornea.subsystem.lightsnswitches.model.LightsNSwitchesScheduleDay;
import arcus.cornea.utils.TimeOfDay;
import arcus.app.R;
import arcus.app.common.schedule.adapter.AbstractScheduleCommandAdapter;

import java.util.List;


public class LightsNSwitchesScheduleEventAdapter extends AbstractScheduleCommandAdapter<LightsNSwitchesScheduleDay> {

    public LightsNSwitchesScheduleEventAdapter(Context context, List<LightsNSwitchesScheduleDay> events) {
        super(context);
        addAll(events);
    }

    @Override public TimeOfDay getEventTimeOfDay(int position) {
        return getItem(position).getTimeOfDay();
    }

    @Override
    public String getEventAbstract(int position) {
        LightsNSwitchesScheduleDay event = getItem(position);
        if (event.isOn() && event.getDimPercentage() != 0) {
            return getContext().getString(R.string.lightsnswitches_percentage, event.getDimPercentage());
        } else if (event.isOn()) {
            return getContext().getString(R.string.lightsnswitches_on);
        } else {
            return getContext().getString(R.string.lightsnswitches_off);
        }
    }

    @Override
    public boolean useLightColorScheme() {
        return true;
    }
}
