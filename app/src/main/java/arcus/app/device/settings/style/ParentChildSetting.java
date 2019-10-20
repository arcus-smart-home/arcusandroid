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
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.device.settings.core.AbstractCompositeSetting;
import arcus.app.device.settings.core.CompositeSetting;
import arcus.app.device.settings.fragment.SettingsListFragment;

/**
 * Representation of a parent-child composite setting. The parent setting renders a single cell
 * containing a title, description, selection abstract (optionally) with a chevron (">") icon.
 *
 * Clicking the parent transitions to a page containing a list view of all child settings.
 */
public class ParentChildSetting extends AbstractCompositeSetting implements CompositeSetting {

    String screenTitle = "";

    public ParentChildSetting(String title, String description) {
        super(title, description, R.layout.setting_parent);
    }

    public ParentChildSetting(String title, String description, String initialSelectionAbstract) {
        super(title, description, initialSelectionAbstract, R.layout.setting_parent);
    }

    public void setScreenTitle(String title) {
        this.screenTitle = title;
    }

    @Override
    public View getView(Context context, ViewGroup root) {
        View settingView = LayoutInflater.from(context).inflate(getLayoutId(), root, false);

        TextView title = (TextView) settingView.findViewById(R.id.setting_title);
        TextView description = (TextView) settingView.findViewById(R.id.setting_description);
        TextView selectionAbstract = (TextView) settingView.findViewById(R.id.selection_abstract);
        ImageView chevron = (ImageView) settingView.findViewById(R.id.setting_parent_chevron);
        View divider = settingView.findViewById(R.id.divider);

        title.setTextColor(isUseLightColorScheme() ? Color.WHITE : Color.BLACK);
        description.setTextColor(isUseLightColorScheme() ? context.getResources().getColor(R.color.white_with_35) : context.getResources().getColor(R.color.black_with_60));
        selectionAbstract.setTextColor(isUseLightColorScheme() ? context.getResources().getColor(R.color.white_with_35) : context.getResources().getColor(R.color.black_with_60));
        chevron.setImageResource(isUseLightColorScheme() ? R.drawable.chevron_white : R.drawable.chevron);
        divider.setBackgroundColor(isUseLightColorScheme() ? context.getResources().getColor(R.color.white_with_10) : context.getResources().getColor(R.color.black_with_10));

        title.setText(getTitle());

        // TODO: A bug in some versions of Android results in the last line of dynamically-set text being cutoff; the newline is a suggested workaround.
        description.setText(getDescription() + "\n");

        selectionAbstract.setVisibility(hasSelectionAbstract() ? View.VISIBLE : View.GONE);
        selectionAbstract.setText(getSelectionAbstract());

        settingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackstackManager.getInstance().navigateToFragment(SettingsListFragment.newInstance(getSettings(), screenTitle), true);
            }
        });

        return settingView;
    }
}
