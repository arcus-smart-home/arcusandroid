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

import arcus.cornea.utils.DayOfWeek;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.error.type.PairingError;
import arcus.app.common.popups.DayOfTheWeekPopup;
import arcus.app.common.utils.StringUtils;
import arcus.app.device.settings.style.OnClickActionSetting;

import java.util.EnumSet;


public class SwannDaysFragment extends AbstractSwannScheduleEditorFragment {

    public static SwannDaysFragment newInstance() {
        return new SwannDaysFragment();
    }

    @Override
    public void onResume() {
        super.onResume();

        String daysString = StringUtils.getScheduleAbstract(getActivity(), getController().getScheduledDays());

        final OnClickActionSetting daysSetting = new OnClickActionSetting(getString(R.string.swann_schedule_days_days), null, daysString);
        daysSetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DayOfTheWeekPopup picker = DayOfTheWeekPopup.newInstance(getController().getScheduledDays());
                picker.setCallback(new DayOfTheWeekPopup.Callback() {
                    @Override
                    public void selectedItems(EnumSet<DayOfWeek> days) {
                        getController().setScheduledDays(days);
                        daysSetting.setSelectionAbstract(StringUtils.getScheduleAbstract(getActivity(), days));
                    }
                });

                BackstackManager.getInstance().navigateToFloatingFragment(picker, picker.getClass().getSimpleName(), true);
            }
        });

        setSetting(daysSetting);
        setTitle(R.string.swann_schedule_days);

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getController().getScheduledDays().size() == 0) {
                    ErrorManager.in(getActivity()).show(PairingError.DEVICE_SCHEDULE_REQUIRES_DAYS);
                } else {
                    goNext();
                }
            }
        });
    }

}
