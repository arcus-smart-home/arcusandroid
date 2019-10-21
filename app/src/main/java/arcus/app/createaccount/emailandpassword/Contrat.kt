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
package arcus.app.createaccount.emailandpassword

import arcus.app.createaccount.BasePresenterContract
import arcus.app.createaccount.nameandphone.NamePhoneAndImageLocation

data class NewAccountInformation(
    val email : String,
    val pass : String,
    val optIn : Boolean,
    private val initialInfo : NamePhoneAndImageLocation
) {
    fun getPersonAttributes() = initialInfo.getPersonAttributes()

    fun getCustomImageLocation() = initialInfo.fileSaveLocation
}

interface EmailPasswordEntryView {
    /**
     * Called when a duplicate email is found.
     */
    fun onDuplicateEmail()

    /**
     * Called when the account is successfully created,
     * the account is logged in to, the active place is set
     *
     * Note: This tries to send, but does not guarantee, the initial verification email.
     *
     * @param personAddress Fully Qualified Address for the person that was just created
     */
    fun onAccountCreatedFor(personAddress: String)

    /**
     * Called when a general error happens.
     */
    fun onUnhandledError()

    /**
     * Called when there is a request in flight.
     */
    fun onLoading()

    /**
     * Called when there is no longer a request in flight.
     */
    fun onLoadingComplete()
}

interface EmailPasswordEntryPresenter : BasePresenterContract<EmailPasswordEntryView> {
    /**
     * Call to try and sign up for service with the [newAccountInformation] provided.
     *
     * This will try to create the account, login, set the active place, and send the initial
     * verification email.
     *
     * Note: This will try to send, but does not guarantee the sending of, the initial verification email.
     */
    fun signupUsing(newAccountInformation: NewAccountInformation)

    /**
     * Checks to see if the password:
     * Is Not Blank
     * Has No Spaces
     * Meets the minimum length
     * Has at least one digit
     * has at least one character
     */
    fun passwordIsValid(password: CharSequence) : Boolean {
        return isNotBlank(password) &&
                hasNoSpaces(password) &&
                meetsMinimumLength(password) &&
                hasAtLeastOneDigit(password) &&
                hasAtLeastOneCharacter(password)
    }

    /**
     * Checks both passwords for equality.
     */
    fun passwordsMatch(first: CharSequence, second: CharSequence) = first.toString() == second.toString()

    private fun isNotBlank(firstPass: CharSequence): Boolean {
        return !firstPass.isBlank()
    }

    private fun hasNoSpaces(firstPass: CharSequence): Boolean {
        return !firstPass.contains(" ")
    }

    private fun meetsMinimumLength(firstPass: CharSequence): Boolean {
        return firstPass.length >= PASSWORD_MIN_LENGTH
    }

    private fun hasAtLeastOneDigit(firstPass: CharSequence): Boolean {
        return firstPass.contains(".*\\d+.*".toRegex())
    }

    private fun hasAtLeastOneCharacter(firstPass: CharSequence): Boolean {
        return firstPass.contains(".*[a-zA-Z].+".toRegex())
    }

    companion object {
        private const val PASSWORD_MIN_LENGTH = 8
    }
}
