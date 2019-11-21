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
package arcus.app.common.adapters;

import android.content.Context;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.common.models.FullScreenErrorModel;

import java.util.ArrayList;

public class ErrorListDataAdapter extends ArrayAdapter<FullScreenErrorModel> {

    public ErrorListDataAdapter(Context context) {
        super(context, 0);
    }

    public ErrorListDataAdapter(Context context, ArrayList<FullScreenErrorModel> data) {
        super(context, 0);
        super.addAll(data);
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.error_listdata_item, parent, false);
        }

        FullScreenErrorModel mListData = getItem(position);

        TextView topText = (TextView) convertView.findViewById(R.id.tvTopText);
        TextView bottomText = (TextView) convertView.findViewById(R.id.tvBottomText);
        topText.setText(mListData.getText());
        bottomText.setVisibility(View.GONE);

        return (convertView);
    }
}
