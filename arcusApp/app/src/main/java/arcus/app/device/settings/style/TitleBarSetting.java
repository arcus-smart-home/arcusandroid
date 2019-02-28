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
package arcus.app.device.settings.style;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.device.settings.core.AbstractSetting;
import arcus.app.device.settings.core.Setting;


public class TitleBarSetting extends AbstractSetting implements Setting  {

    public TitleBarSetting(String title, String description) {
        super(title, description, R.layout.title_bar_setting);
    }

    @Override
    public View getView(Context context, ViewGroup root) {

        View settingView = LayoutInflater.from(context).inflate(getLayoutId(), root, false);

        TextView title = (TextView) settingView.findViewById(R.id.setting_title);
        TextView information = (TextView) settingView.findViewById(R.id.setting_information);
        View divider = settingView.findViewById(R.id.divider);

        title.setText(getTitle());
        information.setText(getDescription());
        divider.setBackgroundColor(isUseLightColorScheme() ? context.getResources().getColor(R.color.white_with_10) : context.getResources().getColor(R.color.black_with_10));

        return settingView;
    }
}
