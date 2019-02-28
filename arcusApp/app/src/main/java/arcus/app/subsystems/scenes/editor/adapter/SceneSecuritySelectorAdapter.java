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
package arcus.app.subsystems.scenes.editor.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.base.Strings;
import arcus.app.R;
import arcus.app.subsystems.scenes.editor.model.SceneListItemModel;

import java.util.List;

public class SceneSecuritySelectorAdapter extends ArrayAdapter<SceneListItemModel> {
    private int lastPositionSelected = -1;
    private boolean isLightScheme = false;

    public SceneSecuritySelectorAdapter(Context context, List<SceneListItemModel> objects) {
        this(context, objects, false);
    }

    public SceneSecuritySelectorAdapter(Context context, List<SceneListItemModel> objects, boolean isLightColorScheme) {
        super(context, 0, objects);
        this.isLightScheme = isLightColorScheme;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.scene_security_selector_item, parent, false);
        }

        SceneListItemModel item = getItem(position);

        int checked = isLightScheme ? R.drawable.circle_check_white_filled : R.drawable.circle_check_black_filled;
        int unchecked = isLightScheme ? R.drawable.circle_hollow_white : R.drawable.circle_hollow_black;

        ImageView toggleButton = (ImageView) convertView.findViewById(R.id.alarm_item_checkbox);
        if (item.isChecked()) {
            toggleButton.setImageResource(checked);
            lastPositionSelected = position;
        }
        else {
            toggleButton.setImageResource(unchecked);
        }

        TextView topTextView = (TextView) convertView.findViewById(R.id.tvText);
        topTextView.setText(item.getTitle());
        if (isLightScheme) {
            topTextView.setTextColor(Color.WHITE);
        }

        TextView bottomTextView = (TextView) convertView.findViewById(R.id.tvSubText);
        if (!Strings.isNullOrEmpty(item.getSubText())) {
            bottomTextView.setVisibility(View.VISIBLE);
            bottomTextView.setText(item.getSubText());
            if (isLightScheme) {
                bottomTextView.setTextColor(getContext().getResources().getColor(R.color.overlay_white_with_60));
            }
        }
        else {
            bottomTextView.setVisibility(View.GONE);
        }

        convertView.setEnabled(true);
        return convertView;
    }

    public void toggleItem(int position) {
        if (lastPositionSelected != -1) {
            getItem(lastPositionSelected).setIsChecked(false);
        }

        getItem(position).setIsChecked(true);
        lastPositionSelected = position;
        notifyDataSetChanged();
    }
}
