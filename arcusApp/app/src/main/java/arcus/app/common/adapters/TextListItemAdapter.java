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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import arcus.app.R;

import java.util.List;


public class TextListItemAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> items;

    public TextListItemAdapter(Context context) {
        super(context, R.layout.list_item_network_device);
        this.context = context;
    }

    public TextListItemAdapter(Context context, List<String> items) {
        super(context, R.layout.list_item_network_device);
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;

        if(v == null) {
            v = LayoutInflater.from(context).inflate(R.layout.list_item_text_adapter, null);
        }

        TextView text = (TextView) v.findViewById(R.id.textview1);
        text.setText(items.get(position));

        return v;
    }

}
