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

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.filters.SmallTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test


@SmallTest
class NotificationChannelsSetupTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    private val notificationChannelsSetup = NotificationChannelsSetup()
    private lateinit var notificationManager: NotificationManager

    @Before
    fun doBefore() {
        notificationManager = context.getSystemService(NotificationManager::class.java) as NotificationManager
        notificationManager
            .notificationChannels
            .forEach { channel ->
                notificationManager.deleteNotificationChannel(channel.id)
            }
    }

    @Test
    fun setsUpTheCorrectChannels() {
        notificationChannelsSetup.setupChannels(context)

        val currentChannels = notificationManager.notificationChannels
        val expecetedChannels = ArcusNotificationChannels.getChannels(context)

        assertEquals(2, currentChannels.size)
        assertTrue(currentChannels.containsAll(expecetedChannels))
    }

    @Test
    fun deletesChannelsWeNoLongerKnowAbout() {
        // Add a channel to be deleted
        notificationManager.createNotificationChannel(NotificationChannel("DELETE_ME", "Channel to be deleted.", NotificationManager.IMPORTANCE_HIGH))

        assertEquals(1, notificationManager.notificationChannels.size)

        notificationChannelsSetup.setupChannels(context)

        val setupChannels = notificationManager.notificationChannels
        assertEquals(2, setupChannels.size)
        assertNull(setupChannels.firstOrNull { it.id == "DELETE_ME" })
    }
}
