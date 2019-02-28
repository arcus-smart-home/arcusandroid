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

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import arcus.cornea.utils.DateUtils;
import arcus.cornea.utils.TimeOfDay;
import arcus.app.R;
import arcus.app.common.sequence.SequencedFragment;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.device.pairing.nohub.swannwifi.controller.SwannWifiPairingSequenceController;
import arcus.app.device.settings.adapter.SettingsListAdapter;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingsList;


public class AbstractSwannScheduleEditorFragment extends SequencedFragment<SwannWifiPairingSequenceController> {

    private Version1TextView titleView;
    private ListView settingsList;
    protected Version1Button nextButton;

    @Nullable
    @Override
    public String getTitle() {
        return getString(R.string.swann_smart_plug);
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.fragment_swann_schedule_editor;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        titleView = (Version1TextView) view.findViewById(R.id.schedule_title);
        settingsList = (ListView) view.findViewById(R.id.setting_list);
        nextButton = (Version1Button) view.findViewById(R.id.next_button);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goNext();
            }
        });
    }

    public void setTitle (int stringResId) {
        titleView.setText(getString(stringResId));
    }

    public void setSetting (Setting setting) {
        SettingsList settings = new SettingsList();
        settings.add(setting);
        settings.setUseLightColorScheme(false);

        settingsList.setAdapter(new SettingsListAdapter(getActivity(), settings));
        settingsList.invalidate();
    }

    public String getTimeAbstract (TimeOfDay tod) {
        return DateUtils.format(tod, false);
    }
}
