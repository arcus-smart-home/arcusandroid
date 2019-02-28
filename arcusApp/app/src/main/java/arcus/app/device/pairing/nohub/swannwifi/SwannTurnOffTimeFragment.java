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
package arcus.app.device.pairing.nohub.swannwifi;

import android.view.View;

import arcus.cornea.utils.TimeOfDay;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.popups.SunriseSunsetPicker;
import arcus.app.device.settings.style.OnClickActionSetting;


public class SwannTurnOffTimeFragment extends AbstractSwannScheduleEditorFragment {

    public static SwannTurnOffTimeFragment newInstance() {
        return new SwannTurnOffTimeFragment();
    }

    @Override
    public void onResume() {
        super.onResume();

        final OnClickActionSetting timeOffSetting = new OnClickActionSetting(getString(R.string.swann_schedule_time_off), null, getTimeAbstract(getController().getScheduledTimeOff()));
        timeOffSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SunriseSunsetPicker picker = SunriseSunsetPicker.newInstance(getController().getScheduledTimeOff());
                picker.setCallback(new SunriseSunsetPicker.Callback() {
                    @Override
                    public void selection(TimeOfDay selected) {
                        getController().setScheduledTimeOff(selected);
                        timeOffSetting.setSelectionAbstract(getTimeAbstract(selected));
                    }
                });

                BackstackManager.getInstance().navigateToFloatingFragment(picker, picker.getClass().getSimpleName(), true);
            }
        });

        setSetting(timeOffSetting);
        setTitle(R.string.swann_schedule_off);
    }
}
