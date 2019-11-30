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
package arcus.app.common.fragments

import android.graphics.Color
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import android.widget.FrameLayout
import arcus.app.R


abstract class FullScreenModalBottomSheet : ModalBottomSheet() {
    override fun onStart() {
        super.onStart()

        dialog
                ?.findViewById<FrameLayout>(R.id.design_bottom_sheet)
                ?.layoutParams
                ?.height = ViewGroup.LayoutParams.MATCH_PARENT

        view?.let {
            val parent = it.parent as View
            val params = parent.layoutParams as CoordinatorLayout.LayoutParams
            val behavior = params.behavior
            val bottomSheetBehavior = behavior as BottomSheetBehavior<*>?
            bottomSheetBehavior?.peekHeight = it.measuredHeight

            parent.setBackgroundColor(Color.TRANSPARENT)
        }
    }
}
