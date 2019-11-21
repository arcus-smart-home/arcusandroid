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
package arcus.app.common.popups.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import arcus.app.R;
import arcus.app.common.view.Version1Button;

import java.util.List;

public class PopupButtonAdapter extends ArrayAdapter<String> {

    public PopupButtonAdapter(Context context, @NonNull List<String> buttonTextList) {
        super(context, 0, buttonTextList);
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.button_action_list, parent, false);
        }

        Version1Button button = (Version1Button) convertView.findViewById(R.id.action_button);
        button.setText(getItem(position));
        button.setClickable(false);
        button.setFocusable(false);

        return convertView;
    }
}
