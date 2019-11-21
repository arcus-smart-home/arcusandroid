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
package arcus.app.common.cards.view;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import com.iris.client.model.DeviceModel;
import arcus.app.R;
import arcus.app.common.cards.AlertCard;
import arcus.app.common.image.ImageManager;
import arcus.app.common.image.picasso.transformation.BlackWhiteInvertTransformation;
import arcus.app.common.image.picasso.transformation.Invert;
import arcus.app.common.models.SessionModelManager;
import arcus.app.common.view.Version1TextView;


public class AlertCardItemView extends RecyclerView.ViewHolder {

    private AlertCard.ALARM_SYSTEM alarm_system;
    ImageView alarmIcon;
    ImageView deviceImage;
    Version1TextView alertName;
    Version1TextView triggeredName;
    Version1TextView triggeredBy;
    Context context;

    public AlertCardItemView(View view) {
        super(view);
        alarmIcon = (ImageView) view.findViewById(R.id.card_alert_icon);
        deviceImage = (ImageView) view.findViewById(R.id.card_alert_device_image);
        alertName = (Version1TextView) view.findViewById(R.id.card_alert_name);
        triggeredName = (Version1TextView) view.findViewById(R.id.card_alert_triggered_name);
        triggeredBy = (Version1TextView) view.findViewById(R.id.card_alert_triggered_by);
        this.context = view.getContext();
    }

    public void build(@NonNull AlertCard card) {

        final DeviceModel model = SessionModelManager.instance().getDeviceWithId(card.getDeviceId(), false);

        alarm_system = card.getAlarm_system();

        if(alarm_system == AlertCard.ALARM_SYSTEM.SAFETY){
            alarmIcon.setImageResource(R.drawable.icon_alarmwaves);
            alertName.setText(context.getString(R.string.safety_alarm_title));
            triggeredName.setText(card.getName());
            triggeredBy.setText(card.getTriggeredBy());
        }else{
            alarmIcon.setImageResource(R.drawable.icon_lockwaves);
            alertName.setText(context.getString(R.string.card_security_alarm_title));
            if(model!=null){
                triggeredName.setText("Intruder Alert");
            } else {
                triggeredName.setText("Panic Alert");
            }
            triggeredBy.setText("Triggered by " + card.getName());
        }



        if (model != null) {
            ImageManager.with(context)
                    .putSmallDeviceImage(model)
                    .withTransformForStockImages(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                    .withPlaceholder(R.drawable.device_list_placeholder)
                    .withError(R.drawable.device_list_placeholder)
                    .noUserGeneratedImagery()
                    .into(deviceImage)
                    .execute();
        } else {
            ImageManager.with(context)
                    .putDrawableResource(R.drawable.icon_cat_securityalarm)
                    .withTransform(new BlackWhiteInvertTransformation(Invert.BLACK_TO_WHITE))
                    .into(deviceImage)
                    .execute();
        }

    }

    public AlertCard.ALARM_SYSTEM getAlarmSystem(){
        return alarm_system;
    }
}
