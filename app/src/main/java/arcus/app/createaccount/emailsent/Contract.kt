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
package arcus.app.createaccount.emailsent

import arcus.app.createaccount.BasePresenterContract

interface EmailSentView {
    /**
     * Called when the users email is loaded.
     *
     * @param email The users email
     */
    fun onEmailLoaded(email: String)

    /**
     * Called when the verification email is sent successfully
     */
    fun onEmailSent()

    /**
     * Called when the logout process is complete.
     */
    fun onLoggedOut()

    /**
     * Called when an error happened we don't know how to handle yet.
     */
    fun onUnhandledError()
}

interface EmailSentPresenter : BasePresenterContract<EmailSentView> {
    /**
     * Loads the person from the provided address.
     *
     * @param personAddress Persons address in "SERV:person:***" format.
     */
    fun loadPersonFromAddress(personAddress: String)

    /**
     * Logs the user out
     */
    fun logout()

    /**
     * Attempts to resend the verification email
     */
    fun resendEmail()
}