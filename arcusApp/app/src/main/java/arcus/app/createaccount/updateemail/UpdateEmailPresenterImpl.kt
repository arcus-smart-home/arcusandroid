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

import arcus.cornea.provider.PersonModelProvider
import arcus.cornea.utils.Listeners
import com.iris.client.capability.Person
import com.iris.client.event.Futures
import com.iris.client.exception.ErrorResponseException
import arcus.app.createaccount.AbstractPresenter
import arcus.app.createaccount.EMAIL_IN_USE_UPDATE


class UpdateEmailPresenterImpl : AbstractPresenter<UpdateEmailView>(), UpdateEmailPresenter {
    private var personAddress : String? = null

    override fun loadPersonFrom(address: String) {
        personAddress = address

        PersonModelProvider
            .instance()
            .getModel(address)
            .load()
            .onSuccess(Listeners.runOnUiThread { model ->
                onlyIfView { view ->
                    view.onEmailLoaded(model.email ?: "")
                }
            })
            .onFailure(Listeners.runOnUiThread { _ ->
                onlyIfView { view ->
                    view.onUnhandledError()
                }
            })
    }

    override fun updateEmailAndSendVerification(email: String) {
        val address = personAddress ?: return
        PersonModelProvider
            .instance()
            .getModel(address)
            .load()
            .chain { model ->
                model?.let { person ->
                    person[Person.ATTR_EMAIL] = email
                    person.commit()
                } ?: Futures.failedFuture(RuntimeException("Cannot commit model to null address."))
            }
            .chain {
                PersonModelProvider
                    .instance()
                    .getModel(address)
                    .load()
                    .chain { model ->
                        model
                            ?.sendVerificationEmail(Person.SendVerificationEmailRequest.SOURCE_ANDROID)
                                ?: Futures.failedFuture(RuntimeException("Cannot commit model to null address."))
                    }
            }
            .onSuccess(Listeners.runOnUiThread { _ ->
                onlyIfView { view ->
                    view.onEmailSent()
                }
            })
            .onFailure { error ->
                PersonModelProvider
                    .instance()
                    .getModel(address)
                    .load()
                    .onSuccess {
                        it.clearChanges()
                    }
                    .onCompletion(Listeners.runOnUiThread { // Now render the error we received from the update.
                        onlyIfView { view ->
                            if (error is ErrorResponseException && error.code == EMAIL_IN_USE_UPDATE) {
                                view.onDuplicateEmail()
                            } else {
                                view.onUnhandledError()
                            }
                        }
                    })
            }
    }
}