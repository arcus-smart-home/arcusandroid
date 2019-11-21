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
import androidx.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.device.settings.core.AbstractSetting;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingChangedParcelizedListener;
import arcus.app.device.settings.fragment.FloatingEnumFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides common functionality for setting views that render lists of items.
 */
public abstract class AbstractEnumeratedSetting extends AbstractSetting implements Setting {

    @NonNull
    private ArrayList<String> enumValues = new ArrayList<>();
    private ArrayList<String> enumDescriptions = new ArrayList<>();
    private String currentSelection;
    private String oldSelection;

    public AbstractEnumeratedSetting (String title, String description, @NonNull List<String> enumValues, String currentSelection) {
        this(title, description, enumValues, null, currentSelection, null);
    }

    public AbstractEnumeratedSetting (String title, String description, @NonNull List<String> enumValues, String currentSelection, String selectionAbstract) {
        this(title, description, enumValues, null, currentSelection, selectionAbstract);
    }

    public AbstractEnumeratedSetting (String title, String description, @NonNull List<String> enumValues, @NonNull List<String> enumDescriptions, String currentSelection, String selectionAbstract) {
        super(title, description, selectionAbstract, R.layout.setting_parent);
        this.enumValues.addAll(enumValues);
        this.currentSelection = currentSelection;
        this.oldSelection = currentSelection;

        if (enumDescriptions != null) {
            this.enumDescriptions.addAll(enumDescriptions);
        }

    }

    @Override
    public View getView(final Context context, ViewGroup root) {
        View settingView = LayoutInflater.from(context).inflate(getLayoutId(), root, false);

        TextView title = (TextView) settingView.findViewById(R.id.setting_title);
        TextView description = (TextView) settingView.findViewById(R.id.setting_description);
        TextView selectionAbstract = (TextView) settingView.findViewById(R.id.selection_abstract);
        FrameLayout selectionAbstractRegion = (FrameLayout) settingView.findViewById(R.id.selection_abstract_region);
        ImageView chevron = (ImageView) settingView.findViewById(R.id.setting_parent_chevron);
        View divider = settingView.findViewById(R.id.divider);

        title.setTextColor(isUseLightColorScheme() ? Color.WHITE : Color.BLACK);
        description.setTextColor(isUseLightColorScheme() ? context.getResources().getColor(R.color.white_with_35) : context.getResources().getColor(R.color.black_with_60));
        selectionAbstract.setTextColor(isUseLightColorScheme() ? context.getResources().getColor(R.color.white_with_35) : context.getResources().getColor(R.color.black_with_60));
        chevron.setImageResource(isUseLightColorScheme() ? R.drawable.chevron_white : R.drawable.chevron);
        divider.setBackgroundColor(isUseLightColorScheme() ? context.getResources().getColor(R.color.white_with_10) : context.getResources().getColor(R.color.black_with_10));

        title.setText(getTitle());
        if (getDescription() == null) {
            description.setVisibility(View.GONE);
        } else {
            // TODO: A bug in some versions of Android results in the last line of dynamically-set text being cutoff; the newline is a suggested workaround.
            description.setText(getDescription() + "\n");
        }

        selectionAbstractRegion.setVisibility(hasSelectionAbstract() ? View.VISIBLE : View.GONE);
        if (hasSelectionAbstract()) {
            selectionAbstract.setText(getSelectionAbstract().toUpperCase());
        }

        final AbstractEnumeratedSetting self = this;

        settingView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Initialize the floating fragment with data...
                FloatingEnumFragment floatingFragment = FloatingEnumFragment.getInstance(getTitle(), enumValues, enumDescriptions, enumValues.indexOf(currentSelection), new SettingChangedParcelizedListener() {
                    @Override
                    public void onSettingChanged(Setting setting, @NonNull Object newValue) {
                        oldSelection = currentSelection;
                        currentSelection = newValue.toString();
                        fireSettingChangedAdapter(self, newValue.toString());
                    }
                });
                floatingFragment.setSetting(AbstractEnumeratedSetting.this);

                // ... then transition to that fragment.
                BackstackManager.getInstance().navigateToFloatingFragment(floatingFragment, floatingFragment.getClass().getName(), true);
            }
        });

        return settingView;
    }

    public final void revertSelection() {
        currentSelection = oldSelection;
    }
}
