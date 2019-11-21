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
package arcus.app.subsystems.alarm.promonitoring.views;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.alarm.promonitoring.models.InactiveAlarmStatusModel;


public class ProMonitoringAlarmInactiveItemView extends RecyclerView.ViewHolder {

    private Context context;
    private View cardView;
    private ImageView alarmTypeImage;
    private Version1TextView alarmTypeTitle;
    private ImageView proMonImage;
    private Version1TextView subText;

    private InactiveAlarmStatusModel model;

    public ProMonitoringAlarmInactiveItemView(View view) {
        super(view);
        context = view.getContext();
        cardView = view;
        alarmTypeImage = (ImageView) view.findViewById(R.id.alarm_type_image);
        alarmTypeTitle = (Version1TextView) view.findViewById(R.id.alarm_type_title);
        proMonImage = (ImageView) view.findViewById(R.id.promon_image);
        subText = (Version1TextView) view.findViewById(R.id.subtext);
    }

    public void build(@NonNull InactiveAlarmStatusModel model) {
        this.model = model;
        cardView.setBackgroundResource(android.R.color.transparent);
        alarmTypeTitle.setText(this.model.getAlarmTypeString().toUpperCase());
        alarmTypeImage.setImageResource(model.getIconResourceId());
        proMonImage.setVisibility(model.isProMonitored() ? View.VISIBLE : View.GONE);

        if(TextUtils.isEmpty(model.getNotAvailableCopy())) {
            subText.setVisibility(View.GONE);
        } else {
            subText.setVisibility(View.VISIBLE);
            subText.setText(model.getNotAvailableCopy());
        }
    }
}
