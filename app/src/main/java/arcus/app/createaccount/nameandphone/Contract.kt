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
@file:JvmMultifileClass
package arcus.app.createaccount.nameandphone

import android.os.Parcelable
import com.iris.client.capability.Person
import arcus.app.createaccount.BasePresenterContract
import kotlinx.android.parcel.Parcelize

@Parcelize
data class NamePhoneAndImageLocation(
    val firstName: String,
    val lastName: String,
    val phoneNumber: String,
    val fileSaveLocation: String?
) : Parcelable {
    fun getPersonAttributes() = mapOf(
        Person.ATTR_FIRSTNAME to firstName,
        Person.ATTR_LASTNAME to lastName,
        Person.ATTR_MOBILENUMBER to phoneNumber
    )

    companion object {
        @JvmField
        val EMPTY =
            NamePhoneAndImageLocation(
                "",
                "",
                "",
                null
            )
    }
}

// Marker interface
interface NameAndPhoneEntryView

interface NameAndPhoneEntryPresenter : BasePresenterContract<NameAndPhoneEntryView> {
    /**
     * Checks to see if the phone number is valid
     *
     * @return true if valid, false if not
     */
    fun phoneNumberValid(text: CharSequence?) : Boolean

    /**
     * Gets the random UUID generated for this person
     */
    fun getGeneratedPersonId() : String
}

