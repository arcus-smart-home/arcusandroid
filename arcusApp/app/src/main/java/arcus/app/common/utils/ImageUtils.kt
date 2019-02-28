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
package arcus.app.common.utils

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup

import arcus.app.ArcusApplication


object ImageUtils {

    // https://developer.android.com/training/multiscreen/screendensities.html
    @JvmStatic
    val screenDensity: String
        get() {
            try {
                val densityDPI = ArcusApplication.getArcusApplication().resources.displayMetrics.density

                when (densityDPI) {
                    in 0.0..0.75 -> return "ldpi"
                    in 0.751..1.0 -> return "mdpi"
                    in 1.01..1.50 -> return "hdpi"
                    in 1.51..2.0 -> return "xhdpi"
                    in 2.01..3.0 -> return "xxhdpi"
                    in 3.01..4.0 -> return "xxhdpi"
                }
            } catch (ex: Exception) { /* No-OP */ }

           return "hdpi"
        }

    @JvmStatic
    fun dpToPx(dp: Int): Int {
        return if (ArcusApplication.getContext() == null || ArcusApplication.getContext().resources == null) {
            dp
        } else dpToPx(ArcusApplication.getContext().resources.displayMetrics, dp)

    }

    @JvmStatic
    fun dpToPx(context: Context?, dp: Int): Int {
        return if (context == null) {
            dp
        } else dpToPx(context.resources.displayMetrics, dp)

    }

    @JvmStatic
    fun dpToPx(metrics: DisplayMetrics, dp: Int): Int {
        return Math.round(dp * (metrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT))
    }

    /**
     * Determines if given points are to the right of a view
     * @param x - x coordinate of point
     * @param view - view object to compare
     * @param margin - Margin (in dp) to the left of the given view that counts
     * @return true if the points are within view bounds, false otherwise
     */
    @JvmStatic
    fun isRightOfView(x: Int, view: View, margin: Int): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return x > location[0] - ImageUtils.dpToPx(ArcusApplication.getContext(), margin)
    }

    @JvmStatic
    fun isLeftOfView(x: Int, view: View, margin: Int): Boolean {
        val location = IntArray(2)
        view.getLocationOnScreen(location)
        return x < location[0] - ImageUtils.dpToPx(ArcusApplication.getContext(), margin)
    }

    @JvmStatic
    fun areAllViewsVisibleVertically(views: List<ViewGroup>, activity: Activity?): Boolean {
        if (activity == null) {
            return false
        }
        val display = activity.windowManager.defaultDisplay
        val size = Point()
        display.getSize(size)
        val height = size.y

        var totalViewHeight = 0
        for (view in views) {
            val viewPosition = IntArray(2)
            view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
            view.getLocationOnScreen(viewPosition)
            val viewBounds = Rect(viewPosition[0], viewPosition[1], viewPosition[0] + view.measuredWidth, viewPosition[1] + view.measuredHeight)
            totalViewHeight += viewBounds.height()
        }

        return totalViewHeight >= height

    }

}
