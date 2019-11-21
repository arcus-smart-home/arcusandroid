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
import androidx.annotation.CallSuper
import androidx.annotation.LayoutRes
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.fragment.app.FragmentManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.slf4j.LoggerFactory

abstract class ModalBottomSheet : BottomSheetDialogFragment() {
    private lateinit var behavior : BottomSheetBehavior<View>

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
        val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        if (bottomSheet != null) {
            behavior = BottomSheetBehavior.from(bottomSheet)
            behavior.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(bottomSheet: View, slideOffset: Float) {
                    // No-Op
                }

                override fun onStateChanged(bottomSheet: View, newState: Int) {
                    if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        behavior.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                }
            })
        } else {
            logger.warn("Could not find bottom sheet - unable to block dragging.")
        }
    }

    /**
     * Common hook to clean up any references, or resources, this view may hold to help prevent leaks.
     */
    open fun cleanUp() {
        // No-Op
    }

    fun show(fragmentManager: FragmentManager?) = apply {
        if (fragmentManager?.isStateSaved == false) {
            show(fragmentManager, this::class.java.name)
        }
    }

    companion object {
        @JvmStatic
        private val logger = LoggerFactory.getLogger(ModalBottomSheet::class.java)
    }
}
