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
package arcus.app.createaccount.emailandpassword

import arcus.cornea.CorneaClientFactory
import arcus.cornea.provider.PersonModelProvider
import arcus.cornea.utils.Listeners
import com.iris.capability.util.Addresses
import com.iris.client.IrisClient
import com.iris.client.capability.Person
import com.iris.client.connection.ConnectionState
import com.iris.client.event.Futures
import com.iris.client.exception.ErrorResponseException
import com.iris.client.service.AccountService
import com.iris.client.session.UsernameAndPasswordCredentials
import arcus.app.common.image.ImageCategory
import arcus.app.createaccount.AbstractPresenter
import arcus.app.createaccount.EMAIL_IN_USE
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

class EmailPasswordEntryPresenterImpl(
    private val platformURL : String,
    private val client : IrisClient = CorneaClientFactory.getClient(),
    private val accountService : AccountService = CorneaClientFactory.getService(AccountService::class.java)
) : AbstractPresenter<EmailPasswordEntryView>(), EmailPasswordEntryPresenter {
    private val isLoading = AtomicBoolean(false)
    private val accountCreatedFor = AtomicReference<String>(null)

    override fun setView(view: EmailPasswordEntryView) {
        super.setView(view)
        if (!isLoading.get()) {
            view.onLoadingComplete()
        }
        accountCreatedFor.get()?.let {
            view.onAccountCreatedFor(it)
        }
    }

    override fun signupUsing(newAccountInformation: NewAccountInformation) {
        val personAddress = accountCreatedFor.get()
        when {
            isLoading.getAndSet(true) -> {
                // We're already loading, somehow we submitted again.
                onlyIfView { view ->
                    view.onLoading()
                }
            }
            personAddress != null -> {
                // We've signed up, maybe we didn't get the notice to the view?
                onlyIfView { view ->
                    view.onLoadingComplete()
                    view.onAccountCreatedFor(personAddress)
                }
            }
            else -> doSignUp(newAccountInformation) // Try to register a new account
        }
    }

    private fun doSignUp(newAccountInformation: NewAccountInformation) {
        when (client.connectionState) {
            ConnectionState.DISCONNECTED, ConnectionState.CLOSED -> client.connectionURL = platformURL
            else -> {
                logger.warn("Cannot change url while connected. Using existing: [${client.connectionURL}]")
            }
        }

        onlyIfView { view ->
            view.onLoading()
        }

        accountService
            .createAccount( // Create the account if we can.
                newAccountInformation.email,
                newAccountInformation.pass,
                newAccountInformation.optIn.toString(),
                null,
                newAccountInformation.getPersonAttributes(), // person
                emptyMap() // Place
            )
            .chain { // Login now that we have an account
                if (it == null) {
                    Futures.failedFuture(RuntimeException("Received null response. Cannot continue."))
                } else {
                    val uap = UsernameAndPasswordCredentials()
                    uap.setPassword(newAccountInformation.pass.toCharArray())
                    uap.username = newAccountInformation.email
                    uap.connectionURL = platformURL

                    client.login(uap)
                }
            }
            .chain { sessionInfo -> // Get the person address && place out of the session
                if (sessionInfo == null || sessionInfo.personId.isNullOrEmpty()) {
                    Futures.failedFuture(RuntimeException("Received null session info. Cannot continue."))
                } else {
                    moveCustomPhotoIfExists(newAccountInformation, sessionInfo.personId)
                    Futures.succeededFuture(Pair(
                        Addresses.toObjectAddress(Person.NAMESPACE, sessionInfo.personId),
                        sessionInfo.places?.get(0)?.placeId
                    ))
                }
            }
            .chain { personPlacePair -> // Set the active place
                if (personPlacePair == null) {
                    Futures.failedFuture(RuntimeException("Received null person / place pair. Cannot continue."))
                } else {
                    client
                        .setActivePlace(personPlacePair.second)
                        .chain { // Silently try to send the verification email && get the person address
                            sendVerificationEmail(personPlacePair.first)
                            Futures.succeededFuture(personPlacePair.first)
                        }
                }
            }
            .onSuccess(Listeners.runOnUiThread { personId ->
                isLoading.set(false)
                accountCreatedFor.set(personId)

                onlyIfView { view ->
                    view.onLoadingComplete()
                    view.onAccountCreatedFor(personId)
                }
            })
            .onFailure(Listeners.runOnUiThread { error ->
                isLoading.set(false)
                logger.error("Bad times in boom town. Going to try and tell the UI about it.", error)

                onlyIfView { view ->
                    view.onLoadingComplete()

                    if (error is ErrorResponseException && error.code == EMAIL_IN_USE) {
                        view.onDuplicateEmail()
                    } else {
                        view.onUnhandledError()
                    }
                }
            })
    }

    private fun moveCustomPhotoIfExists(info: NewAccountInformation, personId: String) {
        info.getCustomImageLocation()?.replace("file://", "")?.let {
            try {
                val original = File(it)
                val copy = File("${it.substringBeforeLast("/")}${File.separator}${ImageCategory.PERSON.name}-$personId.png")
                original.copyTo(copy, true)

                logger.debug("Copied file from: ${original.absolutePath} to ${copy.absolutePath}? ${copy.exists()}")
                original.delete()
            } catch (ex: Exception) {
                logger.error("Failed Copying custom photo.", ex)
            }
        }
    }

    private fun sendVerificationEmail(personAddress: String) {
        PersonModelProvider
            .instance()
            .getModel(personAddress)
            .load()
            .onFailure { logger.error("Could not load person model.", it) }
            .chain { personModel ->
                if (personModel == null || personModel.id.isNullOrBlank()) {
                    Futures.failedFuture(RuntimeException("Received null person model. Cannot continue."))
                } else {
                    personModel
                        .sendVerificationEmail(Person.SendVerificationEmailRequest.SOURCE_ANDROID)
                        .onFailure { logger.error("Could not send verification email.", it) }
                }
            }

    }

    companion object {
        private val logger = LoggerFactory.getLogger(EmailPasswordEntryPresenterImpl::class.java)
    }
}