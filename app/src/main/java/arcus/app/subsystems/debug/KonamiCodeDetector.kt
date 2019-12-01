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
package arcus.app.subsystems.debug

import android.content.Intent
import android.os.Handler
import android.view.ScaleGestureDetector
import arcus.app.BuildConfig

import arcus.app.ArcusApplication
import arcus.app.activities.GenericFragmentActivity

import org.slf4j.LoggerFactory

class KonamiCodeDetector : ScaleGestureDetector.OnScaleGestureListener {
    private val asyncHandler = Handler()
    private var pinchesDetected = 0
    private var lastSpanX: Float = 0f

    override fun onScale(detector: ScaleGestureDetector) = true

    override fun onScaleBegin(detector: ScaleGestureDetector) : Boolean {
        this.lastSpanX = detector.currentSpanX
        return true
    }

    override fun onScaleEnd(detector: ScaleGestureDetector) {
        if (BuildConfig.DEBUG) {
            handlePinch(detector.currentSpanX / lastSpanX)
        }
    }

    private fun handlePinch(pinchFactorX: Float) {
        asyncHandler.removeCallbacksAndMessages(null)

        // In order for pinch to count, they need to pinch open a substantial amount; this prevents
        // spurious shakes and accidental gestures from activating the Konami code.
        if (pinchFactorX > PINCH_THRESHOLD && ++pinchesDetected >= PINCHES_REQUIRED) {
            val context = ArcusApplication.getArcusApplication()
            val intent = GenericFragmentActivity.getLaunchIntent(context, DebugMenuFragment::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(intent)
            pinchesDetected = 0
        } else {
            logger.debug("Pinch count [$pinchesDetected]; Factor: [$pinchFactorX]")
            asyncHandler.postDelayed({
                pinchesDetected = 0
                logger.debug("Pinch timeout expired; resetting pinches to [$pinchesDetected]")
            }, 4000)
        }
    }

    companion object {
        private const val PINCH_THRESHOLD = 1.25f
        private const val PINCHES_REQUIRED = 3

        private val logger = LoggerFactory.getLogger(KonamiCodeDetector::class.java)
    }
}
