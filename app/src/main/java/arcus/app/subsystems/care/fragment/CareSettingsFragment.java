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
package arcus.app.subsystems.care.fragment;

import androidx.annotation.Nullable;
import android.view.View;
import android.widget.ToggleButton;

import arcus.cornea.subsystem.care.CareSettingsController;
import arcus.cornea.subsystem.care.model.Settings;
import arcus.cornea.utils.Listeners;
import com.iris.client.event.ListenerRegistration;
import arcus.app.R;
import arcus.app.common.error.ErrorManager;
import arcus.app.common.fragments.BaseFragment;

public class CareSettingsFragment extends BaseFragment implements CareSettingsController.Callback {
    ListenerRegistration listener;
    ToggleButton silentAlarm;

    public static CareSettingsFragment newInstance() {
        return new CareSettingsFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        View root = getView();
        if (root == null) {
            return;
        }

        silentAlarm = (ToggleButton) root.findViewById(R.id.care_alarm_silent_toggle);
        if (silentAlarm == null) {
            return;
        }

        listener = CareSettingsController.instance().setCallback(this);
        silentAlarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                silentAlarm.setEnabled(false);
                CareSettingsController.instance().setSilentAlarm(silentAlarm.isChecked());
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        listener = Listeners.clear(listener);
    }

    @Nullable @Override public String getTitle() {
        return getString(R.string.card_care_title);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_care_more_settings;
    }

    @Override
    public void savingChanges() {
        showProgressBar();
    }

    @Override
    public void onLoaded(Settings settings) {
        hideProgressBar();
        if (silentAlarm == null) {
            return;
        }

        silentAlarm.setEnabled(true);
        silentAlarm.setChecked(settings.isSilentAlarm());
    }

    @Override
    public void onError(Throwable exception) {
        ErrorManager.in(getActivity()).showGenericBecauseOf(exception);
    }
}
