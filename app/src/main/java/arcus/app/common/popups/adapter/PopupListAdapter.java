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
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.iris.client.model.Model;
import arcus.app.R;

import java.util.List;

public abstract class PopupListAdapter<T extends Model> extends ArrayAdapter<T> implements AdapterView.OnItemClickListener {
    private boolean hideChecks;
    private String checkedItem;
    private T currentModel;

    /**
     *
     * Implicitly calls hideChecks = false;
     *
     * @param context
     * @param modelList
     * @param checkedItem
     */
    public PopupListAdapter(@NonNull Context context, @NonNull List<T> modelList, @NonNull String checkedItem) {
        super(context, 0, modelList);

        this.checkedItem = checkedItem;
        this.hideChecks = false;
    }

    /**
     *
     * Implicitly calls hideChecks = true;
     *
     * @param context
     * @param modelList
     */
    public PopupListAdapter(@NonNull Context context, @NonNull List<T> modelList) {
        this(context, modelList, "");
        hideChecks = true;
    }

    public void setHideChecks(boolean hideChecks) {
        this.hideChecks = hideChecks;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(getLayout(), parent, false);
        }

        currentModel = getItem(position);
        ImageView checkBox = (ImageView) convertView.findViewById(R.id.action_checkbox);
        if (hideChecks) {
            checkBox.setVisibility(View.GONE);
        }
        else {
            checkBox.setVisibility(View.VISIBLE);
            if (currentModel.getId().equals(checkedItem) || currentModel.getAddress().equalsIgnoreCase(checkedItem)) {
                checkBox.setImageResource(R.drawable.circle_check_black_filled);
            }
            else {
                checkBox.setImageResource(R.drawable.circle_hollow_black);
            }
        }

        TextView name = (TextView) convertView.findViewById(R.id.list_item_name);
        TextView device = (TextView) convertView.findViewById(R.id.list_item_description);
        ImageView deviceImage = (ImageView) convertView.findViewById(R.id.device_image);

        setTextName(name);
        setTextUnderName(device);
        setImage(deviceImage);

        return(convertView);
    }

    /**
     *
     * For verfication in extending classes. Changing this breaks other areas though (reason for final)
     *
     * @return int of layout
     */
    @LayoutRes
    public final int getLayout() {
        return R.layout.floating_device_picker_item;
    }

    protected T getCurrentModel() {
        return currentModel;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        checkedItem = getItem(position).getId();
        notifyDataSetChanged();
    }

    protected void showChecksAndNotifyAdapter(boolean hideChecks) {
        this.hideChecks = hideChecks;
        notifyDataSetChanged();
    }

    protected abstract void setImage(final ImageView deviceImage);
    protected abstract void setTextName(TextView textView);
    protected abstract void setTextUnderName(TextView textView);
}
