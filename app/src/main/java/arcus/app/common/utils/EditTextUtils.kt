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
@file:JvmName("EditTextUtils")
package arcus.app.common.utils

import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

/**
 * Gets the text, as a string, from an [EditText] or an empty string.
 */
fun EditText.textOrEmpty(): String = text?.toString() ?: ""

/**
 * Sets up a Focus Change Listener on the [EditText] that clears the error from the
 * [TextInputLayout] when the view is said to receive focus.
 */
infix fun TextInputLayout.clearErrorsOnFocusChangedTo(editText: EditText) {
    clearErrorsOnFocusChangedTo(editText) {}
}

/**
 * Sets up a Focus Change Listener on the [EditText] that clears the error from the
 * [TextInputLayout] when the view is said to receive focus and invokes [additionalAction] with
 * the current focus state whenever it changes.
 */
fun TextInputLayout.clearErrorsOnFocusChangedTo(
        editText: EditText,
        additionalAction: (hasFocus: Boolean) -> Unit = {}
) {
    editText.setOnFocusChangeListener { _, hasFocus ->
        if (hasFocus) {
            this@clearErrorsOnFocusChangedTo.error = null
        }

        additionalAction(hasFocus)
    }
}
