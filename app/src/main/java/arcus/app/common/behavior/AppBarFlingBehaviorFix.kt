/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package arcus.app.common.behavior

import android.content.Context
import com.google.android.material.appbar.AppBarLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.ViewCompat
import android.util.AttributeSet
import android.view.View

/**
 * Workaround AppBarLayout.Behavior for https://issuetracker.google.com/66996774
 */
class AppBarFlingBehaviorFix : AppBarLayout.Behavior {
    constructor() : super()

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onNestedScroll(
            coordinatorLayout: CoordinatorLayout, child: AppBarLayout, target: View,
            dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int
    ) {
        super.onNestedScroll(
                coordinatorLayout, child, target, dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, type
        )
        stopNestedScrollIfNeeded(dyUnconsumed, child, target, type)
    }

    override fun onNestedPreScroll(
            coordinatorLayout: CoordinatorLayout, child: AppBarLayout,
            target: View, dx: Int, dy: Int, consumed: IntArray, type: Int
    ) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type)
        stopNestedScrollIfNeeded(dy, child, target, type)
    }

    private fun stopNestedScrollIfNeeded(dy: Int, child: AppBarLayout, target: View, type: Int) {
        if (type == ViewCompat.TYPE_NON_TOUCH) {
            val currOffset = topAndBottomOffset
            if (dy < 0 && currOffset == 0 || dy > 0 && currOffset == -child.totalScrollRange) {
                ViewCompat.stopNestedScroll(target, ViewCompat.TYPE_NON_TOUCH)
            }
        }
    }
}
