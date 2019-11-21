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
package arcus.app.pairing.hub

import androidx.annotation.StringRes
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import arcus.app.R


class HubIdInputTextWatcher : TextWatcher {
    private var startingLength = 0
    var isValid = false
        private set

    @StringRes
    var errorRes =  R.string.v3_hub_step_3_error_text
        private set

    override fun afterTextChanged(s: Editable?) {
        s?.let {
            if (it.length == 3 && startingLength < it.length) {
                it.append("-")
            }
        }

        isValid = s?.matches(HUB_REGEX) == true
        if (!isValid) {
            errorRes = if (s.isNullOrBlank()) {
                R.string.v3_hub_step_3_error_text
            } else {
                R.string.hub_id_wrong_format
            }
        }
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        s?.let {
            startingLength = it.length
        }
    }
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { /* Nop Nop Nop */ }

    companion object {
        private val HUB_REGEX = "^[a-zA-Z]{3,}\\-\\d{4,}".toRegex()
    }
}

val hubIdInputFilers = arrayOf(
    InputFilter.LengthFilter(8),
    InputFilter.AllCaps()
)
