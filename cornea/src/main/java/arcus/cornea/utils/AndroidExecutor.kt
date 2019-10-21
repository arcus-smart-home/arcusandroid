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
package arcus.cornea.utils

import android.os.Handler
import android.os.Looper

import java.util.concurrent.TimeUnit

class AndroidExecutor(looper: Looper) : ScheduledExecutor {
    private val handler: Handler = Handler(looper)

    override fun execute(command: Runnable) {
        if (Thread.currentThread() === handler.looper.thread) {
            command.run()
        } else {
            handler.post(command)
        }
    }

    override fun executeDelayed(delay: Long, unit: TimeUnit, command: () -> Unit) {
        handler.postDelayed(command, unit.toMillis(delay))
    }

    override fun clearExecutor() {
        handler.removeCallbacksAndMessages(null)
    }

    override fun clearCommand(command: () -> Unit) {
        handler.removeCallbacks(command)
    }

    companion object {
        val mainExecutor by lazy {
            AndroidExecutor(Looper.getMainLooper())
        }
    }
}
