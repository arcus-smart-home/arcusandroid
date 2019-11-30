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

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

abstract class ModalBottomSheet : BottomSheetDialogFragment() {
    private var bottomSheetBehavior: BottomSheetBehavior<View>? = null
    private var bottomSheetCallback: BottomSheetBehavior.BottomSheetCallback? = null

    /**
     * Specifies if the modal bottom sheet should be draggable by the user
     */
    abstract fun allowDragging() : Boolean

    /**
     * Specifies the layout resource to use when calling [onCreateView]
     */
    @LayoutRes
    abstract fun getLayoutResourceId() : Int

    /**
     * Specifies if we should inflate the view and attach it to the parent ViewGroup as well.
     *
     * Default value is to return false.
     */
    open fun shouldAttachViewToRoot() : Boolean = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(getLayoutResourceId(), container, shouldAttachViewToRoot())
    }

    @CallSuper
    override fun onResume() {
        super.onResume()

        if (!allowDragging()) {
            blockDragging()
        }
    }

    private fun blockDragging() {
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        if (bottomSheet != null) {
            val behavior = BottomSheetBehavior.from(bottomSheet)
            val callback = object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // No-Op
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            }
            behavior.addBottomSheetCallback(callback)

            bottomSheetBehavior = behavior
            bottomSheetCallback = callback
        }
    }

    fun show(fragmentManager: FragmentManager?) = apply {
        fragmentManager?.let {
            show(it, this::class.java.name)
        }
    }

    /**
     * Common hook to clean up any references, or resources, this view may hold to help prevent leaks.
     */
    @CallSuper
    open fun cleanUp() {
        val behavior = bottomSheetBehavior ?: return
        val callback = bottomSheetCallback ?: return
        behavior.removeBottomSheetCallback(callback)
    }
}
