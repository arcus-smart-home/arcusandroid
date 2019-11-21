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
package arcus.app.subsystems.alarm.security.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import arcus.cornea.subsystem.security.model.ConfigDeviceModel;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.backstack.BackstackManager;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.SessionModelManager;
import arcus.app.subsystems.alarm.security.DeviceConfigFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

public class DevicesConfigListAdapter extends BaseAdapter {

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SEPARATOR = 1;

    @NonNull
    private ArrayList mData = new ArrayList();
    @NonNull
    private TreeSet<Integer> sectionHeader = new TreeSet<>();

    private LayoutInflater mInflater;

    private Context mContext;

    public DevicesConfigListAdapter(@NonNull Context context) {
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = context;
    }

    public void addItemsForSection(@Nullable final String title, @Nullable final List<ConfigDeviceModel> items) {
        if (title == null || items == null) return;

        mData.add(title);
        sectionHeader.add(mData.size() - 1);

        for (ConfigDeviceModel item : items) {
            mData.add(item);
        }

        notifyDataSetChanged();
    }

    public void addItem(final ConfigDeviceModel item) {
        mData.add(item);
        notifyDataSetChanged();
    }

    public void addItems(@NonNull final List<ConfigDeviceModel> items) {
        mData.addAll(items);
        notifyDataSetChanged();
    }

    public void addSectionHeaderItem(final String item) {
        mData.add(item);
        sectionHeader.add(mData.size() - 1);
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
        sectionHeader.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return sectionHeader.contains(position) ? TYPE_SEPARATOR : TYPE_ITEM;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Nullable
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        ViewHolder holder;
        int rowType = getItemViewType(position);

        if (convertView == null) {
            holder = new ViewHolder();
            switch (rowType) {
                case TYPE_ITEM:
                    convertView = mInflater.inflate(R.layout.cell_alarm_devices_config, null);
                    holder.titleTextView = (TextView) convertView.findViewById(R.id.title_text);
                    holder.descriptionTextView = (TextView) convertView.findViewById(R.id.description_text);
                    holder.activityTextView = (TextView) convertView.findViewById(R.id.activity_text);
                    holder.imageView = (ImageView) convertView.findViewById(R.id.image);
                    holder.chevron = (ImageView) convertView.findViewById(R.id.chevron);
                    break;
                case TYPE_SEPARATOR:
                    convertView = mInflater.inflate(R.layout.cell_alarm_devices_section, null);
                    holder.titleTextView = (TextView) convertView.findViewById(R.id.section_title);
                    break;
            }
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Object item = mData.get(position);

        if (item instanceof String) {
            holder.titleTextView.setText((String)item);
        } else if (item instanceof ConfigDeviceModel) {
            final String deviceName = ((ConfigDeviceModel)item).getName();
            final String deviceDescription = ((ConfigDeviceModel)item).getDescription();
            final String deviceId = ((ConfigDeviceModel)item).getId();

            holder.titleTextView.setText(deviceName);
            holder.descriptionTextView.setText(deviceDescription);
            holder.chevron.setVisibility(View.GONE);

            String linkText = ((ConfigDeviceModel)item).getLinkText();
            if (linkText != null || !linkText.equals("")) {
                holder.activityTextView.setText(((ConfigDeviceModel)item).getLinkText());
                holder.chevron.setVisibility(View.VISIBLE);
            }


            if (deviceId != null) {
                final DeviceModel model = SessionModelManager.instance().getDeviceWithId(deviceId, false);

                if (model != null) {
                    ImageManager.with(mContext)
                            .putSmallDeviceImage(model)
                            .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                            .withPlaceholder(R.drawable.device_list_placeholder)
                            .withError(R.drawable.device_list_placeholder)
                            .noUserGeneratedImagery()
                            .into(holder.imageView)
                            .execute();

                    convertView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            BackstackManager.getInstance()
                                    .navigateToFragment(DeviceConfigFragment.newInstance(deviceName, deviceId), true);
                        }
                    });
                }
            }
        }

        return convertView;
    }

    public static class ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;
        public TextView activityTextView;
        public ImageView imageView;
        public ImageView chevron;
    }

}
