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
package arcus.app.common.view

import android.annotation.SuppressLint
import android.content.Context
import androidx.core.view.GestureDetectorCompat
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import kotlin.properties.Delegates

class DisableSwipeViewPager : ViewPager {
    enum class DisableSwipe {
        BACKWARD,
        FORWARD,
        BOTH,
        NONE,
    }

    @Suppress("MemberVisibilityCanBePrivate") // API
    var disableSwipe: DisableSwipe = DisableSwipe.NONE

    private var initialX: Float = 0.0f
    private var velocityTracker by Delegates.notNull<VelocityTracker>()

    private val gestureDetectorCompat =
        GestureDetectorCompat(context, object : GestureDetector.SimpleOnGestureListener() {
            override fun onSingleTapConfirmed(e: MotionEvent?) = true
            override fun onSingleTapUp(e: MotionEvent?) = true
        })
    private val scaledTouchSlop : Int

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet) {
        scaledTouchSlop = ViewConfiguration.get(context).scaledTouchSlop
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        velocityTracker = VelocityTracker.obtain()
    }

    @SuppressLint("ClickableViewAccessibility") // We call super if we get a single tap / single tap confirmed
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return when {
            gestureDetectorCompat.onTouchEvent(event) -> {
                super.onTouchEvent(event)
            }
            isSwipeAllowed(event) -> super.onTouchEvent(event)
            else -> false
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return when {
            gestureDetectorCompat.onTouchEvent(event) -> {
                super.onInterceptTouchEvent(event)
            }
            isSwipeAllowed(event) -> super.onInterceptTouchEvent(event)
            else -> false
        }
    }

    private fun isSwipeAllowed(event: MotionEvent): Boolean {
        return when {
            disableSwipe == DisableSwipe.NONE -> true
            disableSwipe == DisableSwipe.BOTH -> false
            event.pointerCount > 1 -> {
                event.action = MotionEvent.ACTION_CANCEL
                velocityTracker.clear()
                true
            }
            else -> handleMotionEvent(event, disableSwipe == DisableSwipe.FORWARD)
        }
    }

    private fun handleMotionEvent(event: MotionEvent, disableForward: Boolean) : Boolean {
        return when (event.actionMasked) {
            MotionEvent.ACTION_CANCEL -> true
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                event.action = MotionEvent.ACTION_DOWN
                initialX = event.x
                velocityTracker.run {
                    clear()
                    addMovement(event)
                }
                true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                event.action = MotionEvent.ACTION_UP
                initialX = 0.0f
                velocityTracker.clear()
                true
            }
            MotionEvent.ACTION_MOVE -> {
                val deltaX = event.x - initialX
                if (Math.abs(deltaX) > scaledTouchSlop) {
                    // Velocity tracking is an attempt to block quick swipes one way,
                    // then back the other way to get the view pager to go the direction
                    // it's not supposed to
                    velocityTracker.addMovement(event)
                    velocityTracker.computeCurrentVelocity(1)
                    val xVelocity = velocityTracker.xVelocity

                    if (disableForward) {
                        xVelocity >= 0 && deltaX > 0
                    } else {
                        xVelocity <= 0 && deltaX < 0
                    }
                } else {
                    false
                }
            }
            else -> {
                false
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        velocityTracker.recycle()
    }
}
