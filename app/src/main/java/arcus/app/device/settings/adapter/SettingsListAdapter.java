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
package arcus.app.device.settings.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingAbstractChangedListener;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.core.SettingsList;

/**
 * A {@link android.widget.ListView} adapter that produces a list of {@link Setting} objects.
 */
public class SettingsListAdapter extends ArrayAdapter<Setting> {

    public SettingsListAdapter(Context context, @NonNull SettingsList settingsList) {
        super(context, 0, settingsList.getSettings());

        if(settingsList.getSettings()==null){
            return;
        }

        for (Setting thisSetting : settingsList.getSettings()) {

            // Add a change listener so that we can invalidate the list anytime a value changes
            thisSetting.addListener(new SettingChangedParcelizedListener() {
                @Override
                public void onSettingChanged(Setting setting, Object newValue) {
                    notifyDataSetChanged();
                }
            });

            // Add an abstract change listener so that we can invalidate the list anytime an abstract changes
            thisSetting.addListener(new SettingAbstractChangedListener() {
                @Override
                public void onSettingAbstractChanged() {
                    notifyDataSetChanged();
                }
            });

            // If the user specified a color scheme on the list, have the setting element inherit it; otherwise let the setting determine its own color scheme.
            if (settingsList.isUseLightColorScheme() != null) {
                thisSetting.setUseLightColorScheme(settingsList.isUseLightColorScheme());
            }
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getItem(position).getView(getContext(), parent);
    }
}
