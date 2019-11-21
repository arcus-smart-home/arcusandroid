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
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.app.R;
import arcus.app.integrations.Address;

import java.util.ArrayList;


public class CheckableRecyclewViewAdapter extends RecyclerView.Adapter<CheckableRecyclewViewAdapter.CustomViewHolder> {

    private ArrayList<Address> suggestions = new ArrayList<>();
    private Context context;
    private int selection = 0;
    private CheckableRecyclerViewAdapterCallback callback;
    private boolean isLightMode = false;

    public interface CheckableRecyclerViewAdapterCallback {
        void itemChecked();
    }

    public CheckableRecyclewViewAdapter(Context context, ArrayList<Address> suggestions, CheckableRecyclerViewAdapterCallback callback, boolean isLightMode) {
        this.suggestions = suggestions;
        this.context = context;
        this.callback = callback;
        this.isLightMode = isLightMode;
    }

    public int getSelection() {
        return selection;
    }

    public void setSelection(int selection) {
        this.selection = selection;
    }

    public Address getSelectedItem() {
        if(suggestions.size() > selection) {
            return suggestions.get(selection);
        }
        return null;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.checkable_address_item, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder customViewHolder, final int position) {
        Address suggestion = suggestions.get(position);
        customViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selection = position;
                if(callback != null) {
                    callback.itemChecked();
                }
                customViewHolder.checkbox.setImageResource(R.drawable.circle_check_white_filled);
                notifyDataSetChanged();
            }
        });
        if(position == selection) {
            if(isLightMode) {
                customViewHolder.checkbox.setImageResource(R.drawable.circle_check_black_filled);
            } else {
                customViewHolder.checkbox.setImageResource(R.drawable.circle_check_white_filled);
            }

        } else {
            if(isLightMode) {
                customViewHolder.checkbox.setImageResource(R.drawable.circle_hollow_black);
            } else {
                customViewHolder.checkbox.setImageResource(R.drawable.circle_hollow_white);
            }
        }

        if(isLightMode) {
            customViewHolder.street1.setTextColor(ContextCompat.getColor(context, R.color.black));
            customViewHolder.street2.setTextColor(ContextCompat.getColor(context, R.color.black));
            customViewHolder.cityStateZip.setTextColor(ContextCompat.getColor(context, R.color.black));
            customViewHolder.divider.setBackgroundColor(ContextCompat.getColor(context, R.color.black_with_60));
        }

        customViewHolder.street1.setText(suggestion.getStreet());
        if(!TextUtils.isEmpty(suggestion.getStreet2())) {
            customViewHolder.street2.setText(suggestion.getStreet2());
        } else {
            customViewHolder.street2.setVisibility(View.GONE);
        }
        customViewHolder.cityStateZip.setText(context.getResources().getString(R.string.address_verification_display, suggestion.getCity(), suggestion.getState(), suggestion.getZipCode()));
    }

    @Override
    public int getItemCount() {
        return (null != suggestions ? suggestions.size() : 0);
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected ImageView checkbox;
        protected TextView street1;
        protected TextView street2;
        protected TextView cityStateZip;
        protected View divider;

        public CustomViewHolder(View view) {
            super(view);
            this.checkbox = (ImageView) view.findViewById(R.id.checkbox);
            this.street1 = (TextView) view.findViewById(R.id.street1);
            this.street2 = (TextView) view.findViewById(R.id.street2);
            this.cityStateZip = (TextView) view.findViewById(R.id.city_state_zip);
            this.divider = view.findViewById(R.id.item_divider);
        }
    }
}
