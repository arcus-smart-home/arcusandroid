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

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.NumberPicker
import android.widget.TextView
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet


abstract class ScleraSelectionPopup<T> : ModalBottomSheet() {
    private var topButtonCallback: ((T) -> Unit)? = null
    private var bottomButtonCallback: (() -> Unit)? = null

    abstract fun loadChoices() : Array<T>

    abstract fun getDisplayName(itemIndex: Int, item: T): String

    abstract fun getTitle(): String

    open fun getInitialSelectionValue(): Int = 0

    open fun getTopButtonText(): String = getString(R.string.save_text)

    open fun getBottomButtonText(): String = getString(R.string.cancel_text)


    override fun allowDragging() = false
    override fun getLayoutResourceId() = R.layout.popup_generic_selection

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.title).text = getTitle()

        val picker = view.findViewById<NumberPicker>(R.id.wifi_security_choices)
        val choices = loadChoices()
        picker.minValue = 0
        picker.maxValue = choices.size - 1
        picker.value = getInitialSelectionValue().coerceAtLeast(0)
        picker.displayedValues = choices.mapIndexed { index, item -> getDisplayName(index, item) }.toTypedArray()

        val topButton = view.findViewById<Button>(R.id.save_button)
        topButton.text = getTopButtonText()
        topButton.setOnClickListener { _ ->
            topButtonCallback?.invoke(choices[picker.value])
            dismiss()
        }

        val bottomButton = view.findViewById<Button>(R.id.cancel_button)
        bottomButton.text = getBottomButtonText()
        bottomButton.setOnClickListener { _ ->
            bottomButtonCallback?.invoke()
            dismiss()
        }
    }

    fun setTopButtonCallback(callback: ((T) -> Unit)?) = apply {
        topButtonCallback = callback
    }

    fun setBottomButtonCallback(callback: (() -> Unit)?) = apply {
        bottomButtonCallback = callback
    }
}
