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
package arcus.app.pairing.device.customization.orbit.edit

import android.os.Bundle
import android.view.View
import android.widget.NumberPicker
import arcus.cornea.subsystem.lawnandgarden.utils.LNGDefaults
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import android.widget.Button

class WateringDurationPopup : ModalBottomSheet() {
    private lateinit var zoneDurationPicker : NumberPicker
    private var wateringTimes = LNGDefaults.getWateringTimes()
    private var callback : ((Int) -> Unit)? = null

    override fun allowDragging() = false
    override fun getLayoutResourceId() = R.layout.popup_water_duration

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        zoneDurationPicker = view.findViewById(R.id.zone_duration_picker)
        zoneDurationPicker.wrapSelectorWheel = true
        zoneDurationPicker.minValue = 0
        zoneDurationPicker.maxValue = wateringTimes.lastIndex

        zoneDurationPicker.displayedValues = wateringTimes.map {
            if (it < 60) {
                resources.getQuantityString(R.plurals.minutes, it, it)
            } else {
                resources.getQuantityString(R.plurals.hours, it / 60, it / 60)
            }
        }.toTypedArray()

        val currentSelection = arguments?.getInt(ARG_CURRENT_VALUE, 1) ?: 1
        zoneDurationPicker.value = wateringTimes.indexOf(currentSelection)

        view.findViewById<Button>(R.id.cancel_button).setOnClickListener { dismiss() }
        view.findViewById<Button>(R.id.save_button).setOnClickListener { _ ->
            callback?.let {
                it(wateringTimes[zoneDurationPicker.value])
                dismiss()
            }
        }
    }

    override fun cleanUp() {
        super.cleanUp()
        callback = null
    }

    fun setSelectionCallback(callback: (Int) -> Unit) = apply {
        this.callback = callback
    }

    companion object {
        private const val ARG_CURRENT_VALUE = "ARG_CURRENT_VALUE"

        @JvmStatic
        fun newInstance(currentInMinutes: Int = 1) = WateringDurationPopup().also {
            with (Bundle()) {
                putInt(ARG_CURRENT_VALUE, currentInMinutes)
                it.arguments = this
            }
        }
    }
}
