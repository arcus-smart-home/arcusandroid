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
package arcus.app

import android.annotation.TargetApi
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

enum class PlatformNotificationImportance(val representation: String) {
    DEFAULT("IMPORTANCE_DEFAULT"),
    HIGH("IMPORTANCE_HIGH")
}

@TargetApi(Build.VERSION_CODES.O)
enum class ArcusNotificationChannels(
    val id: String,
    val importance: Int,
    val platformImportance: PlatformNotificationImportance
) {
    IMPORTANT_NOTIFICATIONS(
        "IMPORTANT_NOTIFICATIONS_ID",
        NotificationManager.IMPORTANCE_DEFAULT,
        PlatformNotificationImportance.DEFAULT
    ) {
        override fun getChannel(context: Context): NotificationChannel = NotificationChannel(
            id,
            context.getString(R.string.important_notification_title),
            importance
        ).also {
            it.description = context.getString(R.string.important_notification_description)
        }
    },

    CRITICAL_NOTIFICATIONS(
        "CRTICAL_NOTIFICATIONS_ID",
        NotificationManager.IMPORTANCE_HIGH,
        PlatformNotificationImportance.HIGH
    ) {
        override fun getChannel(context: Context): NotificationChannel = NotificationChannel(
            id,
            context.getString(R.string.critical_notification_title),
            importance
        ).also {
            it.description = context.getString(R.string.critical_notification_description)
        }
    }
    ;

    abstract fun getChannel(context: Context): NotificationChannel

    companion object {
        @TargetApi(Build.VERSION_CODES.BASE)
        @JvmStatic
        fun buildMeetsMinVersion(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

        @TargetApi(Build.VERSION_CODES.O)
        @JvmStatic
        fun getChannels(context: Context) = values().map { it.getChannel(context) }

        @JvmStatic
        fun getChannelForImportance(
            importanceLevel: String
        ) : String {
            val matching = values().firstOrNull { it.platformImportance.representation.equals(importanceLevel, true) }

            return matching?.id ?: IMPORTANT_NOTIFICATIONS.id
        }
    }
}

class NotificationChannelsSetup {
    fun setupChannels(context: Context) {
        if (ArcusNotificationChannels.buildMeetsMinVersion()) {

            val appContext = context.applicationContext
            val notificationManager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            notificationManager?.run {
                val arcusNotificationChannels =
                    ArcusNotificationChannels.getChannels(appContext)
                notificationChannels
                    .filterNot { existing -> // If we don't have it in the list of things that should be setup
                        arcusNotificationChannels.any { toSetup -> existing.id == toSetup.id }
                    }
                    .forEach { toDelete -> // then delete it
                        deleteNotificationChannel(toDelete.id)
                    }

                createNotificationChannels(arcusNotificationChannels)
            }

        }
    }
}