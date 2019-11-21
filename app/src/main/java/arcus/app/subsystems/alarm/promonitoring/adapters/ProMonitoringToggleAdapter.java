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
package arcus.app.subsystems.alarm.promonitoring.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ToggleButton;

import arcus.app.R;
import arcus.app.common.models.ToggleSettingModel;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class ProMonitoringToggleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    List<ToggleSettingModel> items;

    Reference<Callback> callbackRef = new WeakReference<>(null);

    public interface Callback {
        void updateSelected(ToggleSettingModel selectedItem);
    }

    public ProMonitoringToggleAdapter(
          @NonNull List<ToggleSettingModel> items,
          @NonNull Context context
    ) {

        this.items = new ArrayList<>(items);
        setHasStableIds(true);
    }

    public void setCallback(Callback callback) {
        callbackRef = new WeakReference<>(callback);
    }

    @Override
    public long getItemId(int position) {
        return items.get(position).hashCode();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        ((ViewHolder)holder).bind(items.get(position));
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_toggle_title, parent, false);
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView title;
        private ToggleButton toggleButton;

        public ViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            toggleButton = (ToggleButton) view.findViewById(R.id.toggle_button);
        }

        public void bind(final ToggleSettingModel model) {
            title.setText(model.getTitle().toUpperCase());

            toggleButton.setChecked(model.isOn());
            toggleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean checked = ((ToggleButton)v).isChecked();
                    toggleButton.setChecked(!checked);
                    model.setOn(toggleButton.isChecked());
                    Callback callback = callbackRef.get();
                    if (callback != null) {
                        callback.updateSelected(model);
                    }
                }
            });
        }

        public TextView getTitle() {
            return title;
        }

        public void setTitle(TextView title) {
            this.title = title;
        }

        public ToggleButton getToggleButton() {
            return toggleButton;
        }

        public void setToggleButton(ToggleButton toggleButton) {
            this.toggleButton = toggleButton;
        }
    }
}
