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
package arcus.app.seasonal.christmas.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import arcus.app.ArcusNotificationChannels;
import arcus.app.PlatformNotificationImportance;
import arcus.app.R;
import arcus.app.activities.LaunchActivity;
import arcus.app.seasonal.christmas.util.ChristmasModelUtils;

public class ChristmasNotificationReceiver extends BroadcastReceiver {
    public static final String MESSAGE_TYPE = "MESSAGE_TYPE";
    public static final int NOT_CONFIGURED_NOTIFICATION = 1;
    public static final int SANTA_VISITED_NOTIFICATION = 2;


    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) {
            return;
        }

        int messageType = bundle.getInt(MESSAGE_TYPE, 0);

        // Only show setup reminder if user hasn't already configured Santa Tracker
        if (messageType == 1 && !ChristmasModelUtils.modelCacheExists()) {
            doNotification(context, context.getString(R.string.first_app_alert));
        }

        // Only show "Santa stopped by" if user has configured Santa Tracker
        else if (messageType == 2 && ChristmasModelUtils.modelCacheExists()) {
            doNotification(context, context.getString(R.string.second_app_alert));
        }
    }

    private void doNotification(Context context, String message) {
        String title = context.getString(R.string.app_name);

        Intent newIntent = new Intent(context, LaunchActivity.class);
        newIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, newIntent, 0);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        String normalImportance = PlatformNotificationImportance.DEFAULT.getRepresentation();
        String channel = ArcusNotificationChannels.getChannelForImportance(normalImportance);
        Notification notification = new NotificationCompat.Builder(context, channel)
              .setSmallIcon(R.drawable.device_purple_45x45)
              .setContentTitle(title)
              .setContentText(message)
              .setSound(defaultSoundUri)
              .setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setBigContentTitle(title))
              .setContentIntent(pendingIntent)
              .setAutoCancel(true)
              .build();

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(0, notification);
        }
    }

}
