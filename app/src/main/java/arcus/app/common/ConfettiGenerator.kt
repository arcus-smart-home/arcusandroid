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
package arcus.app.common

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.content.ContextCompat
import android.util.TypedValue
import android.view.ViewGroup
import com.github.jinatonic.confetti.ConfettoGenerator
import com.github.jinatonic.confetti.confetto.Confetto
import arcus.app.R
import java.util.Random
import com.github.jinatonic.confetti.ConfettiSource
import com.github.jinatonic.confetti.ConfettiManager
import com.github.jinatonic.confetti.Utils
import com.github.jinatonic.confetti.confetto.BitmapConfetto
import kotlin.math.roundToInt


class ConfettiGenerator(
    context: Context,
    confettiContainer: ViewGroup,
    confettoSize : Float = 18F
) : ConfettoGenerator {
    private val bitmaps : List<Bitmap>
    private val confettiManager : ConfettiManager
    init {
        val size = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            confettoSize,
            context.resources.displayMetrics
        ).roundToInt()

        bitmaps = Utils.generateConfettiBitmaps(intArrayOf(
            ContextCompat.getColor(context, R.color.pink_confetto),
            ContextCompat.getColor(context, R.color.purple_confetto),
            ContextCompat.getColor(context, R.color.yellow_confetto),
            ContextCompat.getColor(context, R.color.blue_confetto),
            ContextCompat.getColor(context, R.color.turqoise_confetto)),
            size
        )

        confettiManager = ConfettiManager(
                context,
                this,
                ConfettiSource(
                    0,
                    -size,
                    confettiContainer.width,
                    -size
                ),
                confettiContainer
            )
            .setVelocityX(0F, 50F)
            .setVelocityY(100F, 50F)
            .setInitialRotation(180, 180)
            .setRotationalAcceleration(360F, 180F)
            .setTargetRotationalVelocity(360F)
            .setNumInitialCount(0)
            .setEmissionDuration(ConfettiManager.INFINITE_DURATION)
            .setEmissionRate(15F)
    }

    fun startConfetti() = apply {
        confettiManager.animate()
    }

    /**
     * While the [ConfettiManager] seems to attach a listener to the container and stop the
     * animation when the window is detached, it's best not to rely on that
     */
    fun stopConfetti() = apply {
        confettiManager.terminate()
    }

    override fun generateConfetto(random: Random): Confetto = BitmapConfetto(bitmaps[random.nextInt(bitmaps.size)])
}
