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

import arcus.app.ArcusApplication;
import arcus.app.R;
import arcus.app.common.utils.StringUtils;
import arcus.app.common.view.AlarmDashedCircleView;
import arcus.app.common.view.DashedCircleView;
import arcus.app.common.view.Version1Button;
import arcus.app.common.view.Version1TextView;
import arcus.app.subsystems.alarm.promonitoring.models.SecurityAlarmStatusModel;


public class ProMonitoringAlarmSecurityItemView extends RecyclerView.ViewHolder implements View.OnClickListener {
    private Context context;
    private View cardView;
    private View titleArea;
    private ImageView alarmTypeImage;
    private Version1TextView alarmTypeTitle;
    private ImageView proMonImage;
    private Version1TextView subText;

    private ImageView alarmIcon;
    private Version1TextView alarmStatusText;

    private AlarmDashedCircleView dashedCircleView;
    private SecurityAlarmStatusModel model;
    private View securityButtons;

    private Version1Button onButton;
    private Version1Button partialButton;
    private Version1Button offButton;
    private Version1Button cancelButton;

    private SecurityCallback callback;

    public interface SecurityCallback {
        void arm();
        void partial();
        void disarm();
    }

    public void setSecurityListener(SecurityCallback callback) {
        this.callback = callback;
    }

    public ProMonitoringAlarmSecurityItemView(View view) {
        super(view);
        context = view.getContext();
        cardView = view;
        titleArea = view.findViewById(R.id.title_area);
        alarmTypeImage = (ImageView) view.findViewById(R.id.alarm_type_image);
        alarmTypeTitle = (Version1TextView) view.findViewById(R.id.alarm_type_title);
        proMonImage = (ImageView) view.findViewById(R.id.promon_image);
        subText = (Version1TextView) view.findViewById(R.id.subtext);
        dashedCircleView = (AlarmDashedCircleView) view.findViewById(R.id.dashed_circle);
        alarmStatusText = (Version1TextView) view.findViewById(R.id.alarm_status_text);
        alarmIcon = (ImageView) view.findViewById(R.id.alarm_icon);
        securityButtons = view.findViewById(R.id.buttons_container);

        onButton = (Version1Button) view.findViewById(R.id.on_button);
        partialButton = (Version1Button) view.findViewById(R.id.partial_button);
        offButton = (Version1Button) view.findViewById(R.id.off_button);
        cancelButton = (Version1Button) view.findViewById(R.id.cancel_button);
    }

    public void build(@NonNull SecurityAlarmStatusModel model) {
        this.model = model;
        cardView.setBackgroundResource(android.R.color.transparent);
        alarmTypeTitle.setText(this.model.getAlarmTypeString().toUpperCase());
        alarmTypeImage.setImageResource(model.getIconResourceId());
        proMonImage.setVisibility(model.isProMonitored() ? View.VISIBLE : View.GONE);

        if(TextUtils.isEmpty(model.getSubtext())) {
            subText.setVisibility(View.GONE);
        } else {
            subText.setVisibility(View.VISIBLE);
            subText.setText(model.getSubtext());
        }

        if(model.getAlarmState() == SecurityAlarmStatusModel.SecurityAlarmArmingState.INACTIVE) {
            moreDevicesNeeded();
        }
        else {
            handleAlarmState(model);
        }

        onButton.setOnClickListener(this);
        partialButton.setOnClickListener(this);
        offButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
    }

    private void moreDevicesNeeded() {
        resetVisibility();
        dashedCircleView.setVisibility(View.GONE);
    }

    public int getTotalDeviceCount() {
        return model.getTotalDevicesCount();
    }

    private void handleAlarmState(@NonNull SecurityAlarmStatusModel model) {
        resetVisibility();
        dashedCircleView.setDevicesCount(this.model.getOfflineDevicesCount(), this.model.getTiggeredDevicesCount(), this.model.getActiveDevicesCount());
        switch (model.getAlarmState()) {
            case DISARMED:
            case OFF:
                cancelButton.setVisibility(View.GONE);
                securityButtons.setVisibility(View.VISIBLE);
                setStateOff(model);
                break;
            case ON:
                cancelButton.setVisibility(View.GONE);
                securityButtons.setVisibility(View.VISIBLE);
                setStateOn(model);
                break;
            case ARMING:
                cancelButton.setVisibility(View.VISIBLE);
                securityButtons.setVisibility(View.GONE);
                setStateArming(model);
                break;
            case ALERT:
                cancelButton.setVisibility(View.GONE);
                securityButtons.setVisibility(View.GONE);
                setStateAlert(model);
                break;
            default:
                break;
        }
    }

    private void setStateOff(@NonNull SecurityAlarmStatusModel card) {
        dashedCircleView.setAlarmState(DashedCircleView.AlarmState.OFF);
        alarmStatusText.setText(context.getString(R.string.off_first_capital));
        alarmStatusText.setVisibility(View.VISIBLE);

        onButton.setEnabled(true);
        partialButton.setEnabled(true);
        offButton.setEnabled(false);
    }

    private void setStateOn(@NonNull SecurityAlarmStatusModel model) {
        String activeDevicesCount = String.valueOf(model.getActiveDevicesCount());
        String devicesDivisor = ArcusApplication.getContext().getString(R.string.security_devices_divisor, model.getParticipatingDevicesCount());

        String secondsSuffix = ArcusApplication.getContext().getString(R.string.security_seconds_suffix);

        dashedCircleView.setAlarmState(DashedCircleView.AlarmState.ON);
        alarmStatusText.setVisibility(View.VISIBLE);

        if (model.getArmingSecondsRemaining() > 0) {
            alarmStatusText.setText(StringUtils.getSuperscriptSpan(String.valueOf(model.getArmingSecondsRemaining()), secondsSuffix));
        } else if (model.getPrealertSecondsRemaining() > 0) {
            alarmStatusText.setText(StringUtils.getSuperscriptSpan(String.valueOf(model.getPrealertSecondsRemaining()), secondsSuffix));
            subText.setText(R.string.incident_grace_countdown);
        } else {
            alarmStatusText.setText(StringUtils.getSuperscriptSpan(activeDevicesCount, devicesDivisor));
        }

        onButton.setEnabled(false);
        partialButton.setEnabled(false);
        offButton.setEnabled(true);
    }

    private void setStateArming(@NonNull SecurityAlarmStatusModel model) {
        String secondsSuffix = ArcusApplication.getContext().getString(R.string.security_seconds_suffix);

        dashedCircleView.setAlarmState(DashedCircleView.AlarmState.ARMING);
        alarmStatusText.setVisibility(View.VISIBLE);
        alarmStatusText.setText(StringUtils.getSuperscriptSpan(String.valueOf(model.getArmingSecondsRemaining()), secondsSuffix));

        onButton.setEnabled(false);
        partialButton.setEnabled(false);
        offButton.setEnabled(true);
    }

    private void setStateAlert(@NonNull SecurityAlarmStatusModel model) {
        dashedCircleView.setAlarmState(DashedCircleView.AlarmState.ALERT);
        alarmIcon.setVisibility(View.VISIBLE);
        alarmStatusText.setVisibility(View.GONE);
    }

    private void resetVisibility() {
        alarmStatusText.setVisibility(View.VISIBLE);
        alarmIcon.setVisibility(View.GONE);
        alarmStatusText.setVisibility(View.GONE);
        dashedCircleView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.on_button) {
            if(callback != null) {
                onButton.setEnabled(false);
                partialButton.setEnabled(false);
                callback.arm();
            }
        } else if(view.getId() == R.id.partial_button) {
            if(callback != null) {
                onButton.setEnabled(false);
                partialButton.setEnabled(false);
                callback.partial();
            }
        } else if(view.getId() == R.id.off_button) {
            if(callback != null) {
                offButton.setEnabled(false);
                callback.disarm();
            }
        } else if(view.getId() == R.id.cancel_button) {
            if(callback != null) {
                callback.disarm();
            }
        }
    }

}
