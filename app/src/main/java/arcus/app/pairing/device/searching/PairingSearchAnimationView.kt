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
package arcus.app.pairing.device.searching

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.OvalShape
import androidx.core.content.ContextCompat
import androidx.appcompat.widget.AppCompatImageView
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import arcus.app.R
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class PairingSearchAnimationView : RelativeLayout {
    private val animators = mutableListOf<Animator>()
    var shouldAnimate = true
        set (value) {
            field = value

            for (index in 0 until (childCount - 1)) {
                getChildAt(index).visibility = if (!value) View.INVISIBLE else View.VISIBLE
            }
        }
    private val animationsWithDelayStartIndex : Long = 2
    private var startAnimationDelay by Delegates.notNull<Long>()
    private var delayIncrement      by Delegates.notNull<Long>()
    private var animationDuration   by Delegates.notNull<Long>()
    private var startScale          by Delegates.notNull<Float>()
    private var endScale            by Delegates.notNull<Float>()
    private var circleColor         by Delegates.notNull<Int>()
    private var mainImage : Drawable? = null

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        val array = context.obtainStyledAttributes(attrs, R.styleable.PairingSearchAnimationView, defStyleAttr, 0)

        startAnimationDelay = array.getInt(R.styleable.PairingSearchAnimationView_startAnimationDelay, START_ANIMATION_DELAY).toLong()
        delayIncrement = array.getInt(R.styleable.PairingSearchAnimationView_delayIncrement, DELAY_INCREMENT).toLong()
        animationDuration = array.getInt(R.styleable.PairingSearchAnimationView_animationDuration, ANIMATION_DURATION).toLong()

        val requestedStartScale = array.getFloat(R.styleable.PairingSearchAnimationView_startScale, UNSPECIFIED_START_SCALE)
        startScale = if (requestedStartScale < 1) {
            1f
        } else {
            requestedStartScale
        }

        val requestedEndScale = array.getFloat(R.styleable.PairingSearchAnimationView_endScale, UNSPECIFIED_MAX_SCALE)
        endScale = if (requestedEndScale < requestedStartScale) {
            shouldAnimate = false
            requestedStartScale
        } else {
            requestedEndScale
        }

        circleColor = array.getInt(R.styleable.PairingSearchAnimationView_circleRadiateColor, UNSPECIFIED_COLOR)
        mainImage   = array.getDrawable(R.styleable.PairingSearchAnimationView_mainImage)

        array.recycle()

        initView()
    }

    private fun initView() {
        inflate(context, R.layout.searching_image, this)
        setupMainImageView()

        if (shouldAnimate) {
            setupAnimations()
        }
    }

    private fun setupAnimations() {
        val circleShape = ShapeDrawable(OvalShape())
        circleShape.paint.color = ContextCompat.getColor(context, circleColor)

        for (index in 0 until (childCount - 1)) {
            val image = getChildAt(index) as AppCompatImageView
            image.background = circleShape
            animators.add(getAnimation(index + 1, image))
        }
    }

    private fun setupMainImageView() {
        val metrics = resources.displayMetrics
        val dip = TypedValue.COMPLEX_UNIT_DIP

        // We should have at least 12dp padding - minimum
        val basePadding = TypedValue.applyDimension(dip, 12f, metrics)

        // For every scale factor above our base (2), we need 20 additional dp of padding so the
        // animations edges don't get cut off
        val incrementalPadding = TypedValue.applyDimension(dip, 20f, metrics)

        // Since our base is maxScale = 2 and uses 12dp of padding
        // we remove that from the modifiedMaxScale since we've already accounted for that
        // and we don't add in un-needed padding
        val modifiedMaxScale = Math.max(0f, endScale - 2)

        // Now take the base of 12 + any incremental padding we need for every scale factor above
        // our base and round that to an int
        val padding = ((incrementalPadding * modifiedMaxScale) + basePadding).roundToInt()

        // Get the image view with the house icon
        val view = getChildAt(childCount - 1) as AppCompatImageView

        // Now apply the padding to the view
        view.setPadding(padding, padding, padding, padding)
        val image = mainImage
        if (image != null) {
            view.setImageDrawable(image)
        } else {
            view.setImageResource(UNSPECIFIED_IMAGE)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        stopAnimations()
        startAnimations()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        stopAnimations()
    }

    private fun startAnimations() {
        // isStarted takes into account startDelay - isRunning does not.
        if (shouldAnimate) {
            animators
                .filter { !it.isStarted }
                .forEach { it.start() }
        }
    }

    private fun stopAnimations() {
        animators.filter { it.isStarted }.forEach { it.cancel() }
    }

    private fun getAnimation(order: Int, view: ImageView) = with (ValueAnimator.ofFloat(startScale, endScale)) {
        duration = animationDuration
        repeatMode = ValueAnimator.RESTART
        repeatCount = ValueAnimator.INFINITE

        if (order >= animationsWithDelayStartIndex) {
            val multiplier = Math.max(0, order - animationsWithDelayStartIndex)
            startDelay = startAnimationDelay + (multiplier * delayIncrement)
        }

        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            val currentScale = it.animatedValue as Float
            val alpha = currentScale / endScale
            view.scaleX = currentScale
            view.scaleY = currentScale
            view.alpha  = 1 - alpha
        }

        this
    }

    companion object {
        private const val START_ANIMATION_DELAY = 500
        private const val DELAY_INCREMENT = 500
        private const val ANIMATION_DURATION = 4_000
        private const val UNSPECIFIED_MAX_SCALE   = 4f
        private const val UNSPECIFIED_START_SCALE = 1f
        private const val UNSPECIFIED_COLOR = R.color.sclera_purple
        private const val UNSPECIFIED_IMAGE = R.drawable.pair_60x60
    }
}
