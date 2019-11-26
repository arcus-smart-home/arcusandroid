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
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import arcus.app.R;

import java.util.Arrays;
import java.util.List;

public class SpinnerAdapter extends ArrayAdapter<String> {
    private int resource = -1;
    private Context context;
    private boolean isLightColorScheme;
    private List<Integer> disabledItems;
    boolean wrapText = false;       // When true, long text entries will wrapped on multiple lines

    public SpinnerAdapter(Context context, int resource, String[] items) {
        super(context, resource);
        this.resource = resource;
        this.context = context;
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addAll(items);
    }

    public SpinnerAdapter(Context context, int resource, String[] items, boolean isLightColorScheme) {
        super(context, resource);
        this.resource = resource;
        this.context = context;
        this.isLightColorScheme = isLightColorScheme;
        setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        addAll(items);
    }

    public void setDisabledItems(Integer... disabledItems) {
        this.disabledItems = Arrays.asList(disabledItems);
    }

    @Override
    public boolean areAllItemsEnabled () {
        return disabledItems == null || disabledItems.size() == 0;
    }

    @Override
    public boolean isEnabled(int position) {
        return areAllItemsEnabled() || !disabledItems.contains(position);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View mView = super.getDropDownView(position, convertView, parent);
        TextView mTextView = (TextView) mView;

        // Don't change single line setting until view is rendered, otherwise this has no effect :(
        final TextView finalTextView = mTextView;
        mTextView.post(new Runnable() {
            @Override
            public void run() {
                finalTextView.setSingleLine(!wrapText);
            }
        });

        if (!isEnabled(position)) {
            mTextView.setTextColor(Color.GRAY);
        } else {
            mTextView.setTextColor(Color.BLACK);
        }
        return mView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = super.getView(position, convertView, parent);
        if (resource != -1) {
            TextView text = (TextView) v.findViewById(R.id.spinner_item);

            if (position == 0) {
                if (isLightColorScheme) {
                    text.setTextColor(context.getResources().getColor(R.color.overlay_white_with_60));
                }
                else {
                    text.setTextColor(context.getResources().getColor(R.color.black_with_35));
                }
            } else {
                if(isLightColorScheme){
                    text.setTextColor(Color.WHITE);
                }else {
                    text.setTextColor(Color.BLACK);
                }
            }
        } else {
            if (position == getCount()) {
                ((TextView) v.findViewById(android.R.id.text1)).setText("");
                ((TextView) v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                ((TextView) v.findViewById(android.R.id.text1)).setTextColor(getContext().getResources().getColor(R.color
                        .arcus_gray));
            }
        }
        return v;
    }

}
