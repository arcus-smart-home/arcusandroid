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
package arcus.app.device.settings.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import arcus.app.R;
import arcus.app.common.fragments.BaseFragment;
import arcus.app.common.fragments.IShowedFragment;
import arcus.app.device.settings.adapter.SettingsListAdapter;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingsList;

import java.util.List;

/**
 * A fragment representing a list of {@link Setting}.
 */
public class SettingsListFragment extends BaseFragment implements IShowedFragment {

    private ListView settingsListView;

    private List<Setting> settings;

    private static final String SCREEN_TITLE = "SCREEN_TITLE";

    @NonNull
    public static SettingsListFragment newInstance (List<Setting> settings) {

        // TODO: Need to move this into a bundle...

        SettingsListFragment slf = new SettingsListFragment();
        slf.settings = settings;

        return slf;
    }

    @NonNull
    public static SettingsListFragment newInstance (List<Setting> settings, String title) {

        SettingsListFragment slf = new SettingsListFragment();
        Bundle bundle = new Bundle(2);
        bundle.putSerializable(SCREEN_TITLE, title);
        slf.setArguments(bundle);
        slf.settings = settings;

        return slf;
    }

    @NonNull
    @Override
    public String getTitle() {
        if (getArguments() != null && !TextUtils.isEmpty((String)getArguments().getSerializable(SCREEN_TITLE))) {
            return (String) getArguments().getSerializable(SCREEN_TITLE);
        }
        return "SETTINGS";
    }

    @Override
    public Integer getLayoutId() {
        return R.layout.setting_list;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup parentGroup = (ViewGroup) super.onCreateView(inflater, container, savedInstanceState);
        settingsListView = (ListView) parentGroup.findViewById(R.id.settings_list);

        onShowedFragment();

        return parentGroup;
    }

    @Override public void onResume() {
        super.onResume();
        setTitle();
    }

    @Override
    public void onShowedFragment() {
        if(settings != null) {
            settingsListView.setAdapter(new SettingsListAdapter(getActivity(), new SettingsList(settings)));
            settingsListView.deferNotifyDataSetChanged();
        }
    }
}
