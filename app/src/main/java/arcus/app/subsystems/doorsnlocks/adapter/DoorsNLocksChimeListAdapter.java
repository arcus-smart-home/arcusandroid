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
package arcus.app.subsystems.doorsnlocks.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import arcus.cornea.subsystem.doorsnlocks.DoorsNLocksMoreController;
import arcus.cornea.subsystem.doorsnlocks.model.ChimeConfig;
import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.SessionModelManager;

import java.util.ArrayList;
import java.util.List;


public class DoorsNLocksChimeListAdapter extends BaseAdapter {

    private List<ChimeConfig> chimeConfigList;
    private LayoutInflater mInflater;
    private Context context;
    private DoorsNLocksMoreController doorsNLocksMoreController;

    public DoorsNLocksChimeListAdapter(@NonNull Context context){
        this.context = context;
        mInflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        chimeConfigList = new ArrayList<>();
    }

    public void setController(DoorsNLocksMoreController doorsNLocksMoreController){
        this.doorsNLocksMoreController = doorsNLocksMoreController;
    }

    public void showChimeList(List<ChimeConfig> chimeConfigList){
        this.chimeConfigList = chimeConfigList;
        notifyDataSetChanged();
    }

    public void updateChimeList(@NonNull ChimeConfig chimeConfig){
        if(chimeConfigList !=null){
            if(containConfig(chimeConfig)){
                showChimeList(chimeConfigList);
            }
        }
    }

    private boolean containConfig(@NonNull final ChimeConfig chimeConfig){
        for(ChimeConfig config : chimeConfigList){
            if(config.getDeviceId().equals(chimeConfig.getDeviceId())){
                chimeConfigList.set(chimeConfigList.indexOf(config),chimeConfig);
                return true;
            }
        }
        return false;
    }

    public void clear(){
        chimeConfigList.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return chimeConfigList.size();
    }

    @Override
    public Object getItem(int position) {
        return chimeConfigList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.doors_and_locks_chime_list_item,parent,false);
            holder.deviceImage = (ImageView) convertView.findViewById(R.id.chime_item_device_image);
            holder.deviceName = (TextView) convertView.findViewById(R.id.chime_item_device_name);
            holder.checkBox = (ToggleButton) convertView.findViewById(R.id.chime_item_toggle);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        final ChimeConfig chimeConfig = (ChimeConfig) getItem(position);

        String deviceId = chimeConfig.getDeviceId();
        if (deviceId != null) {
            final DeviceModel model = SessionModelManager.instance().getDeviceWithId(deviceId, false);

            if (model != null) {
                ImageManager.with(context)
                        .putSmallDeviceImage(model)
                        .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                        .withPlaceholder(R.drawable.device_list_placeholder)
                        .withError(R.drawable.device_list_placeholder)
                        .noUserGeneratedImagery()
                        .into(holder.deviceImage)
                        .execute();
            }
        }

        holder.deviceName.setText(chimeConfig.getName());

        holder.checkBox.setChecked(chimeConfig.isEnabled());

        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chimeConfig.setEnabled(!chimeConfig.isEnabled());
                doorsNLocksMoreController.setConfig(chimeConfig);
            }
        });

        return convertView;
    }

    public static class ViewHolder {
        public ImageView deviceImage;
        public TextView deviceName;
        public ToggleButton checkBox;
    }
}
