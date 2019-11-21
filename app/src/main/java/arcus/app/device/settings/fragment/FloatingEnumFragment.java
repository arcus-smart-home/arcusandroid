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
import androidx.annotation.Nullable;
import android.widget.ListView;

import arcus.app.R;
import arcus.app.common.popups.ArcusFloatingFragment;
import arcus.app.device.settings.adapter.EnumeratedListAdapter;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;

import java.util.ArrayList;


public class FloatingEnumFragment extends ArcusFloatingFragment {

    // Argument bundle names
    private final static String CHOICES_LIST = "choices-list";
    private final static String DESCRIPTIONS_LIST = "descriptions-list";
    private final static String TITLE = "title";
    private final static String SETTING = "setting";
    private final static String LISTENER = "listener";
    private Setting setting;
    private final static String INITIAL_SELECTION = "initial-selection";

    private EnumeratedListAdapter adapter;

    @NonNull
    public static FloatingEnumFragment getInstance (String title, ArrayList<String> choices, ArrayList<String> descriptions, int initialSelection, SettingChangedParcelizedListener listener) {
        FloatingEnumFragment fragment = new FloatingEnumFragment();

        Bundle bundle = new Bundle();
        bundle.putStringArrayList(CHOICES_LIST, choices);
        bundle.putStringArrayList(DESCRIPTIONS_LIST, descriptions);
        bundle.putString(TITLE, title);
        bundle.putParcelable(LISTENER, listener);
        bundle.putInt(INITIAL_SELECTION, initialSelection);

        fragment.setArguments(bundle);
        return fragment;
    }

    public void setSetting(Setting setting) {
        this.setting = setting;
    }

    @Override
    public void doClose () {
        SettingChangedParcelizedListener listener = getArguments().getParcelable(LISTENER);
        if (listener != null)
            listener.onSettingChanged(setting, adapter.getSelectedValue());
    }

    @Override
    public void setFloatingTitle() {
        title.setText(getArguments().getString(TITLE));
    }

    @Override
    public void doContentSection() {
        ListView list = (ListView) contentView.findViewById(R.id.settings_list);

        // Don't render divider lines between elements
        list.setDivider(null);
        list.setDividerHeight(0);

        // Populate the list with data
        adapter = new EnumeratedListAdapter(getActivity(), getArguments().getStringArrayList(CHOICES_LIST), getArguments().getStringArrayList(DESCRIPTIONS_LIST), getArguments().getInt(INITIAL_SELECTION));
        list.setAdapter(adapter);
    }

    @Override
    public Integer contentSectionLayout() {
        return R.layout.setting_list;
    }

    @Nullable
    @Override
    public String getTitle() {
        return getArguments().getString(TITLE);
    }
}
