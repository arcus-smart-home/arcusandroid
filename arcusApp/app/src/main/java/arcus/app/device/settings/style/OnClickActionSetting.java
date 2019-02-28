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
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.device.settings.core.AbstractSetting;
import arcus.app.device.settings.core.Setting;
import arcus.app.device.settings.core.SettingAbstractChangedListener;

/**
 * A setting that renders a cell containing a title, description, selection abstract (optional) and
 * chevron (>). Clicking the setting invokes an OnClickHandler allowing for custom behavior.
 */
public class OnClickActionSetting extends AbstractSetting implements Setting {

    private View.OnClickListener listener;
    private ImageView chevron;
    private boolean hideChevron;

    public OnClickActionSetting(String title, String description, View.OnClickListener listener) {
        super(title, description, R.layout.setting_parent);
        this.listener = listener;
    }

    public OnClickActionSetting(String title, String description, String initialSelectionAbstract, View.OnClickListener listener) {
        super(title, description, initialSelectionAbstract, R.layout.setting_parent);
        this.listener = listener;
    }

    public OnClickActionSetting(String title, String description, String initialSelectionAbstract, boolean hideChevron, View.OnClickListener listener){
        super(title, description, initialSelectionAbstract, R.layout.setting_parent);
        this.listener = listener;
        this.hideChevron = hideChevron;
    }

    public OnClickActionSetting(String title, String description, String initialSelectionAbstract){
        super(title, description, initialSelectionAbstract, R.layout.setting_parent);
    }

    public OnClickActionSetting(String title, String description, Integer initialSelectionAbstractIcon) {
        super(title, description, initialSelectionAbstractIcon, R.layout.setting_parent);
    }

    public void setOnClickListener (View.OnClickListener listener) {
        this.listener = listener;
    }

    @Override
    public View getView(Context context, ViewGroup root) {

        View settingView = LayoutInflater.from(context).inflate(getLayoutId(), root, false);

        TextView title = (TextView) settingView.findViewById(R.id.setting_title);
        TextView description = (TextView) settingView.findViewById(R.id.setting_description);
        final TextView selectionAbstract = (TextView) settingView.findViewById(R.id.selection_abstract);
        ImageView selectionAbstractImage = (ImageView) settingView.findViewById(R.id.selection_abstract_image);
        View divider = settingView.findViewById(R.id.divider);
        chevron = (ImageView) settingView.findViewById(R.id.setting_parent_chevron);

        title.setTextColor(isUseLightColorScheme() ? Color.WHITE : Color.BLACK);
        description.setTextColor(isUseLightColorScheme() ? context.getResources().getColor(R.color.white_with_35) : context.getResources().getColor(R.color.black_with_60));
        selectionAbstract.setTextColor(isUseLightColorScheme() ? context.getResources().getColor(R.color.white_with_35) : context.getResources().getColor(R.color.black_with_60));
        chevron.setImageResource(isUseLightColorScheme() ? R.drawable.chevron_white : R.drawable.chevron);
        divider.setBackgroundColor(isUseLightColorScheme() ? context.getResources().getColor(R.color.white_with_10) : context.getResources().getColor(R.color.black_with_10));

        if(hideChevron){
            chevron.setVisibility(View.GONE);
        }
        title.setText(getTitle());

        // TODO: A bug in some versions of Android results in the last line of dynamically-set text being cutoff; the newline is a suggested workaround.
        if (getDescription() != null) {
            description.setText(getDescription() + "\n");
        } else {
            description.setVisibility(View.GONE);
        }


        if (hasSelectionIconResource()) {
            selectionAbstract.setVisibility(View.GONE);
            selectionAbstractImage.setVisibility(View.VISIBLE);
            Invert inversion = isUseLightColorScheme() ? Invert.BLACK_TO_WHITE : Invert.WHITE_TO_BLACK;
            ImageManager.with(context)
                  .putDrawableResource(getSelectionAbstractIconResource())
                  .withTransform(new BlackWhiteInvertTransformation(inversion))
                  .into(selectionAbstractImage)
                  .execute();
        }
        else if (hasSelectionAbstract()) {
            selectionAbstract.setVisibility(View.VISIBLE);
            selectionAbstractImage.setVisibility(View.GONE);
            selectionAbstract.setText(getSelectionAbstract());
        }
        else {
            selectionAbstract.setVisibility(View.GONE);
            selectionAbstractImage.setVisibility(View.GONE);
        }

        settingView.setOnClickListener(listener);
        this.addListener(new SettingAbstractChangedListener() {
            @Override
            public void onSettingAbstractChanged() {
                selectionAbstract.setText(getSelectionAbstract());
            }
        });

        return settingView;
    }

}
