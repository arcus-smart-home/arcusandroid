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

import arcus.cornea.CorneaClientFactory
import arcus.cornea.provider.PersonModelProvider
import arcus.cornea.utils.Listeners
import com.iris.client.IrisClient
import com.iris.client.capability.Person
import com.iris.client.event.Futures
import com.iris.client.model.ModelCache
import arcus.app.createaccount.AbstractPresenter
import org.slf4j.LoggerFactory

class EmailSentPresenterImpl(
    private val client : IrisClient = CorneaClientFactory.getClient(),
    private val cache  : ModelCache = CorneaClientFactory.getModelCache()
) : AbstractPresenter<EmailSentView>(), EmailSentPresenter {
    private var personAddressLocal : String? = null
    private val failureListener = Listeners.runOnUiThread<Throwable> {
        logger.error("Action failed.", it)
        onlyIfView { view ->
            view.onUnhandledError()
        }
    }

    override fun loadPersonFromAddress(personAddress: String) {
        PersonModelProvider
            .instance()
            .getModel(personAddress)
            .load()
            .chain {
                if (it == null) {
                    Futures.failedFuture(RuntimeException("Person model was null."))
                } else {
                    personAddressLocal = it.address
                    Futures.succeededFuture(it.email ?: "")
                }
            }
            .onSuccess(Listeners.runOnUiThread {
                onlyIfView { view ->
                    view.onEmailLoaded(it)
                }
            })
            .onFailure(failureListener)
    }

    override fun logout() {
        client
            .logout()
            .chain {
                cache.clearCache()
                Futures.succeededFuture(true)
            }
            .onSuccess(Listeners.runOnUiThread {
                onlyIfView { view ->
                    view.onLoggedOut()
                }
            })
    }

    override fun resendEmail() {
        val address = personAddressLocal ?: return
        PersonModelProvider
            .instance()
            .getModel(address)
            .load()
            .chain { model ->
                if (model == null) {
                    Futures.failedFuture(RuntimeException("Person model was null."))
                } else {
                    model.sendVerificationEmail(Person.SendVerificationEmailRequest.SOURCE_ANDROID)
                }
            }
            .onSuccess {
                onlyIfView { view ->
                    view.onEmailSent()
                }
            }
            .onFailure(failureListener)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(EmailSentPresenterImpl::class.java)
    }
}