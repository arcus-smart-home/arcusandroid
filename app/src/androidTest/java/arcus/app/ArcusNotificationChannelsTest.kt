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

import android.app.NotificationManager
import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.filters.SmallTest
import org.junit.Assert.*
import org.junit.Test


@SmallTest
class ArcusNotificationChannelsTest {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun hasCorrectNumberOfChannels() {
        assertEquals(2, ArcusNotificationChannels.getChannels(context).size)
    }

    @Test
    fun channelsHaveCorrectImportance() {
        val channels = ArcusNotificationChannels.values()
        channels.forEach { channel ->
            if (channel.importance == NotificationManager.IMPORTANCE_HIGH) {
                assertEquals(channel.platformImportance, PlatformNotificationImportance.HIGH)
            } else {
                assertEquals(channel.platformImportance, PlatformNotificationImportance.DEFAULT)
            }
        }
    }

    @Test
    fun getsDefaultChannelFrom_IMPORTANCE_DEFAULT() {
        val defaultChannelId = ArcusNotificationChannels.IMPORTANT_NOTIFICATIONS.getChannel(context).id
        // Variations of the correct text
        assertEquals(defaultChannelId, ArcusNotificationChannels.getChannelForImportance("IMPORTANCE_DEFAULT"))
        assertEquals(defaultChannelId, ArcusNotificationChannels.getChannelForImportance("importance_default"))
        assertEquals(defaultChannelId, ArcusNotificationChannels.getChannelForImportance("importance_DEFAULT"))
        assertEquals(defaultChannelId, ArcusNotificationChannels.getChannelForImportance("IMPORTANCE_default"))
    }

    @Test
    fun getsDefaultChannelForAnyUnknownValues() {
        val defaultChannelId = ArcusNotificationChannels.IMPORTANT_NOTIFICATIONS.getChannel(context).id
        assertEquals(defaultChannelId, ArcusNotificationChannels.getChannelForImportance("MEH, It's Monday"))
        assertEquals(defaultChannelId, ArcusNotificationChannels.getChannelForImportance("Erm, I mean Tuesday !@#\$%^&*()~"))
    }

    @Test
    fun getsHighChannelFrom_IMPORTANCE_HIGH() {
        val defaultChannelId = ArcusNotificationChannels.CRITICAL_NOTIFICATIONS.getChannel(context).id
        assertEquals(defaultChannelId, ArcusNotificationChannels.getChannelForImportance("IMPORTANCE_HIGH"))
        assertEquals(defaultChannelId, ArcusNotificationChannels.getChannelForImportance("importance_high"))
    }
}
