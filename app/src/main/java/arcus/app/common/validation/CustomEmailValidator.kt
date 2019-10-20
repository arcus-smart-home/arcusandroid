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
package arcus.app.common.validation

import android.support.annotation.StringRes
import android.widget.EditText

import arcus.app.ArcusApplication

import org.apache.commons.lang3.StringUtils

class CustomEmailValidator(
    private val emailTB: EditText,
    @param:StringRes private val missingEmailError: Int,
    @param:StringRes private val invalidEmailError: Int
) : InputValidator {
    private val emailAddress: String?

    init {
        val chars = emailTB.text
        if (chars != null) {
            this.emailAddress = chars.toString()
        } else {
            this.emailAddress = null
        }
    }

    override fun isValid(): Boolean {
        if (StringUtils.isEmpty(emailAddress)) {
            emailTB.error = ArcusApplication.getContext().getString(missingEmailError)
            return false
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailAddress!!).matches()) {
            emailTB.error = ArcusApplication.getContext().resources.getString(invalidEmailError)
            return false
        }

        return true
    }
}
