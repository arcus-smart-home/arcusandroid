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
@file:JvmName("AccountCreationConstantsKt")
package arcus.app.createaccount

// Error Field found when a duplicate email is found
const val EMAIL_IN_USE = "error.signup.emailinuse"
const val EMAIL_IN_USE_UPDATE = "EmailInUseException"

// Initial account signup state
const val SIGNUP_1 = "signUp1"

// Account state that has completed the sign up process
const val COMPLETE = "COMPLETE"
