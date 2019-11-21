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
package arcus.app.subsystems.alarm.safety;

import androidx.annotation.Nullable;
import android.widget.CheckBox;

import arcus.app.R;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.common.utils.PreferenceUtils;


public class HaloAlertSnoozed extends ArcusFloatingFragment {
    CheckBox checkbox;

    @Nullable
    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public void setFloatingTitle() {
        title.setText("");
    }

    @Override
    public void doContentSection() {
        checkbox = (CheckBox) contentView.findViewById(R.id.checkbox_care_dont_show_again);
    }

    @Override
    public void doClose() {
        PreferenceUtils.setShowWeatherRadioSnooze(!checkbox.isChecked());
    }

    @Override public void onResume() {
        super.onResume();
        showFullScreen(true);
    }

    @Override public void onPause() {
        super.onPause();
        showFullScreen(false);
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.snooze_alert_fragment;
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fullscreen_arcus_popup_fragment;
    }
}
