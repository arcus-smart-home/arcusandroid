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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;

import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.AlarmDashedCircleView;
import arcus.app.common.view.DashedCircleView;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.alarm.promonitoring.models.SafetyAlarmStatusModel;


public class ProMonitoringAlarmSafetyItemView extends RecyclerView.ViewHolder {

    private View cardView;
    private ImageView alarmTypeImage;
    private Version1TextView alarmTypeTitle;
    private ImageView proMonImage;
    private Version1TextView subText;

    private ImageView alarmIcon;
    private Version1TextView alarmStatusText;

    private AlarmDashedCircleView dashedCircleView;
    private SafetyAlarmStatusModel model;

    public ProMonitoringAlarmSafetyItemView(View view) {
        super(view);
        cardView = view;
        alarmTypeImage = (ImageView) view.findViewById(R.id.alarm_type_image);
        alarmTypeTitle = (Version1TextView) view.findViewById(R.id.alarm_type_title);
        proMonImage = (ImageView) view.findViewById(R.id.promon_image);
        subText = (Version1TextView) view.findViewById(R.id.subtext);
        dashedCircleView = (AlarmDashedCircleView) view.findViewById(R.id.dashed_circle);
        alarmStatusText = (Version1TextView) view.findViewById(R.id.alarm_status_text);
        alarmIcon = (ImageView) view.findViewById(R.id.alarm_icon);
    }

    public void build(@NonNull SafetyAlarmStatusModel model) {
        this.model = model;
        cardView.setBackgroundResource(android.R.color.transparent);
        alarmTypeTitle.setText(this.model.getAlarmTypeString().toUpperCase());
        alarmTypeImage.setImageResource(this.model.getIconResourceId());
        proMonImage.setVisibility(model.isProMonitored() ? View.VISIBLE : View.GONE);

        if(TextUtils.isEmpty(this.model.getSubtext())) {
            subText.setVisibility(View.GONE);
        } else {
            subText.setVisibility(View.VISIBLE);
            subText.setText(this.model.getSubtext());
        }

        if(this.model.getTotalDevicesCount() > 0) {
            dashedCircleView.setDevicesCount(this.model.getOfflineDevicesCount(), this.model.getTiggeredDevicesCount(), this.model.getActiveDevicesCount());
            dashedCircleView.setAlarmState(DashedCircleView.AlarmState.ON);

            String activeDevicesCount = String.valueOf(model.getActiveDevicesCount());
            String devicesDivisor = ArcusApplication.getContext().getString(R.string.security_devices_divisor, model.getParticipatingDevicesCount());

            dashedCircleView.setAlarmState(DashedCircleView.AlarmState.ON);
            alarmStatusText.setVisibility(View.VISIBLE);
            alarmStatusText.setText(StringUtils.getSuperscriptSpan(activeDevicesCount, devicesDivisor));
        }
        else {
            moreDevicesNeeded();
        }
    }

    private void moreDevicesNeeded() {
        resetVisibility();
        dashedCircleView.setVisibility(View.GONE);
    }

    public int getTotalDeviceCount() {
        return model.getTotalDevicesCount();
    }

    private void resetVisibility() {
        alarmStatusText.setVisibility(View.VISIBLE);
        alarmIcon.setVisibility(View.GONE);
        alarmStatusText.setVisibility(View.GONE);
        dashedCircleView.setVisibility(View.VISIBLE);
    }
}
