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
package arcus.app.seasonal.christmas.cards.views;

import android.content.Context;
import android.support.annotation.NonNull;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.text.Html;
import android.view.View;
import android.widget.ImageView;

import arcus.app.R;
import arcus.app.common.view.DashboardFlipViewHolder;
import arcus.app.common.view.ScleraTextView;
import arcus.app.seasonal.christmas.cards.SantaCard;
import arcus.app.seasonal.christmas.model.ChristmasModel;
import arcus.app.seasonal.christmas.receiver.ChristmasNotificationReceiver;
import arcus.app.seasonal.christmas.util.ChristmasModelUtils;
import arcus.app.common.view.Version1TextView;
import arcus.app.seasonal.christmas.util.SantaEventTiming;

import java.util.GregorianCalendar;

public class SantaCardItemView extends DashboardFlipViewHolder {

    private ImageView serviceImage;
    private Version1TextView serviceName;
    private Context context;
    private Version1TextView titleView;
    private ScleraTextView textView;

    public SantaCardItemView(View view) {
        super(view);
        serviceImage = (ImageView) view.findViewById(R.id.service_image);
        serviceName = (Version1TextView) view.findViewById(R.id.service_name);
        titleView = (Version1TextView) view.findViewById(R.id.title);
        textView = (ScleraTextView) view.findViewById(R.id.service_subtext);
        context = view.getContext();
    }

    public void build(@NonNull SantaCard card) {
        serviceName.setText(context.getString(R.string.santa_tracker_title));
        serviceImage.setImageResource(R.drawable.dashboard_santatracker);

        if (titleView != null) {
            titleView.setText(Html.fromHtml(context.getResources().getString(R.string.santa_tracker_title)));
        }

        if (textView != null) {
            textView.setText(card.getCardDescription());
            textView.setVisibility(View.VISIBLE);
        }

        // If the device is rebooted, we have to reschedule these; otherwise we're updating the one already scheduled.
        ChristmasModel model = ChristmasModelUtils.getModelCacheFromDisk();
        GregorianCalendar notConfiguredReminderTime = SantaEventTiming.instance().getNotConfiguredNotificationTime();
        if (!model.isSetupComplete() && (System.currentTimeMillis() <= notConfiguredReminderTime.getTimeInMillis())) {
            addNotConfiguredNotification(notConfiguredReminderTime);
        }

        GregorianCalendar visitorArrivedTime = SantaEventTiming.instance().getSantaArrivedNotificationTime();
        if (model.isSetupComplete() && (System.currentTimeMillis() <= visitorArrivedTime.getTimeInMillis())) {
            addDayOfNotification(visitorArrivedTime);
        }
    }

    private void addNotConfiguredNotification(GregorianCalendar notifyOnDate) {
        Intent intent = new Intent(context, ChristmasNotificationReceiver.class);
        intent.putExtra(ChristmasNotificationReceiver.MESSAGE_TYPE, ChristmasNotificationReceiver.NOT_CONFIGURED_NOTIFICATION);
        setupNotificationSchedule(intent, notifyOnDate);
    }

    private void addDayOfNotification(GregorianCalendar notifyOnDate) {
        Intent intent = new Intent(context, ChristmasNotificationReceiver.class);
        intent.putExtra(ChristmasNotificationReceiver.MESSAGE_TYPE, ChristmasNotificationReceiver.SANTA_VISITED_NOTIFICATION);
        setupNotificationSchedule(intent, notifyOnDate);
    }

    private void setupNotificationSchedule(Intent intent, GregorianCalendar notifyOnDate) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);

        if (Build.VERSION.SDK_INT >= 19) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, notifyOnDate.getTimeInMillis(), pendingIntent);
        }
        else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, notifyOnDate.getTimeInMillis(), pendingIntent);
        }
    }
}
