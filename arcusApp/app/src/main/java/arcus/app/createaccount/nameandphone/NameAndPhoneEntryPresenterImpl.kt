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
package arcus.app.createaccount.nameandphone

import arcus.app.createaccount.AbstractPresenter
import java.util.UUID


class NameAndPhoneEntryPresenterImpl
    : AbstractPresenter<NameAndPhoneEntryView>(), NameAndPhoneEntryPresenter {
    private val phoneRegex = "^((\\d(\\s|-?))?\\d{3}(\\s|-)?(\\d{3})(-)?(\\d{4})|\\(\\d{3}\\)(\\s)?(\\d{3})(-)?(\\d{4}))$".toRegex()
    private var generatedID : String? = null

    override fun phoneNumberValid(text: CharSequence?) = text?.matches(phoneRegex) == true

    override fun getGeneratedPersonId() : String {
        return generatedID?.let { it } ?: run {
            val id = UUID.randomUUID().toString()
            generatedID = id
            id
        }
    }
}