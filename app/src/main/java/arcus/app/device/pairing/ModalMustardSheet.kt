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
package arcus.app.device.pairing

import android.os.Bundle
import android.support.annotation.LayoutRes
import android.support.design.widget.BottomSheetBehavior
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import arcus.app.R
import arcus.app.common.fragments.FullScreenModalBottomSheet
import arcus.app.common.view.ScleraButton


class ModalMustardSheet : FullScreenModalBottomSheet() {

    private var kitDevices = 0
    private var hubDevices = 0
    private var kitDevicesListener : (() -> Unit)? = null
    private var hubDevicesListener : (() -> Unit)? = null

    override fun allowDragging() = false

    @LayoutRes
    override fun getLayoutResourceId() = R.layout.modal_mustard_sheet

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            kitDevices = it.getInt(ModalMustardSheet.ARG_KIT_DEVICE)
            hubDevices = it.getInt(ModalMustardSheet.ARG_HUB_DEVICE)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.modal_mustard_sheet, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<ScleraButton>(R.id.kit_devices_button).setOnClickListener {
            kitDevicesListener?.invoke()
        }

        view.findViewById<ScleraButton>(R.id.hub_devices_button).setOnClickListener {
            hubDevicesListener?.invoke()
            dialog.dismiss()
        }

        view.findViewById<ScleraButton>(R.id.modal_dismiss_button).setOnClickListener {
            dialog.dismiss()
        }

        val kitSection = view.findViewById<LinearLayout>(R.id.kit_section)
        val hubSection = view.findViewById<LinearLayout>(R.id.hub_section)

        if(kitDevices == 0){
            kitSection.visibility = View.GONE
        }
        if(hubDevices == 0){
            hubSection.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()

        val bottomSheet = dialog.findViewById<FrameLayout>(android.support.design.R.id.design_bottom_sheet)
        BottomSheetBehavior.from(bottomSheet).state = BottomSheetBehavior.STATE_EXPANDED
    }

    /**
     * Sets the action to perform; then the dialog is dismissed
     */
    fun setKitButtonAction(action: () -> Unit) {
        kitDevicesListener = action
    }

    /**
     * Sets the action to perform; then the dialog is dismissed
     */
    fun setHubButtonAction(action: () -> Unit) {
        hubDevicesListener = action
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanUp()
    }


    override fun cleanUp() {
        kitDevicesListener = null
        hubDevicesListener = null
    }

    companion object {
        private const val ARG_KIT_DEVICE = "ARG_KIT_DEVICE"
        private const val ARG_HUB_DEVICE = "ARG_HUB_DEVICE"

        @JvmStatic
        fun newInstance(kitDevices : Int, hubDevices :Int) : ModalMustardSheet {
            val args = Bundle()
            args.putInt(ARG_KIT_DEVICE, kitDevices)
            args.putInt(ARG_HUB_DEVICE, hubDevices)
            val fragment = ModalMustardSheet()
            fragment.arguments = args
            return fragment
        }
    }
}