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
package arcus.app.createaccount.updateemail

import arcus.app.createaccount.BasePresenterContract


interface UpdateEmailView {
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
     * Called when an error happened we don't know how to handle yet.
     */
    fun onUnhandledError()

    /**
     * Called when a duplicate email is found.
     */
    fun onDuplicateEmail()
}

interface UpdateEmailPresenter : BasePresenterContract<UpdateEmailView> {
    /**
     * Loads the person from the specified address.
     *
     * @param address The person's address "SERV:person:***" format
     */
    fun loadPersonFrom(address: String)

    /**
     * Update the persons email and sends a new verification email.
     *
     * @param email The persons new email address
     */
    fun updateEmailAndSendVerification(email: String)
}
