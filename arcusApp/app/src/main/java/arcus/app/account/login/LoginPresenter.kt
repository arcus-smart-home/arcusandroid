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
package arcus.app.account.login

import android.content.IntentSender
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.CredentialRequest
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import arcus.cornea.SessionController
import arcus.cornea.common.BasePresenter
import com.iris.client.exception.UnauthorizedException
import com.iris.client.model.AccountModel
import com.iris.client.model.PersonModel
import com.iris.client.model.PlaceModel
import com.iris.client.session.Credentials
import com.iris.client.session.UsernameAndPasswordCredentials
import arcus.app.ArcusApplication
import arcus.app.activities.BaseActivity
import arcus.app.activities.GenericConnectedFragmentActivity
import arcus.app.activities.InvitationActivity
import arcus.app.common.image.IntentRequestCode
import arcus.app.common.utils.LoginUtils
import arcus.app.createaccount.COMPLETE
import arcus.app.createaccount.SIGNUP_1
import arcus.app.launch.AccountLoginForgotFragment
import arcus.app.launch.CredentialResolutionResultHandler
import org.slf4j.LoggerFactory

class LoginPresenter : BasePresenter<LoginPresenterContract.LoginView>(), LoginPresenterContract.LoginPresenter, SessionController.LoginCallback {

    override fun promptForSavedCredentials() {
        val client = (ArcusApplication.getArcusApplication().foregroundActivity as BaseActivity).googleApiClient

        val credentialRequest = CredentialRequest.Builder()
                .setPasswordLoginSupported(true)
                .build()

        Auth.CredentialsApi.request(client, credentialRequest).setResultCallback { credentialRequestResult ->
            logger.debug("Completed request for credential.")
            if (credentialRequestResult.status.isSuccess) {
                logger.debug("Credential request succeeded.")
                onCredentialRetrieved(credentialRequestResult.credential)
            } else {
                logger.debug("Credential resolution required.")
                resolveResult(credentialRequestResult.status)
            }
        }
    }

    override fun login(placeId: String, username: String, password: CharArray) {
        addListener(SessionController::class.java.simpleName, SessionController.instance().setCallback(this))

        val credentials = getUsernamePasswordCredentials(username, password)

        logger.debug("Signing into requested place {}", LoginUtils.getContextualPlaceIdOrLastUsed(placeId))
        SessionController.instance().login(credentials, LoginUtils.getContextualPlaceIdOrLastUsed(placeId))
    }

    override fun useInvitationCode() {
        presentedView.onPending(null)
        val thisActivity = ArcusApplication.getArcusApplication().foregroundActivity
        if (thisActivity != null) {
            InvitationActivity.start(thisActivity)
            thisActivity.finish()
        } else if (isPresenting) {
            presentedView.onError(IllegalStateException("No foreground activity!"))
        }
    }

    override fun forgotPassword() {
        presentedView.onPending(null)

        val thisActivity = ArcusApplication.getArcusApplication().foregroundActivity
        if (thisActivity != null) {
            thisActivity.startActivity(
                GenericConnectedFragmentActivity.getLaunchIntent(
                    thisActivity,
                    AccountLoginForgotFragment::class.java,
                        allowBackPress = false
                )
            )
            thisActivity.finish()
        } else if (isPresenting) {
            presentedView.onError(IllegalStateException("No foreground activity!"))
        }
    }

    private fun getUsernamePasswordCredentials(username: String, password: CharArray): Credentials {
        val credentials = UsernameAndPasswordCredentials()
        credentials.connectionURL = LoginUtils.getPlatformUrl(username)
        credentials.setPassword(password)
        credentials.username = LoginUtils.getUsername(username)
        return credentials
    }

    override fun loginSuccess(placeModel: PlaceModel?, personModel: PersonModel?, accountModel: AccountModel?) {
        LoginUtils.completeLogin()

        if (accountModel != null) {
            if (COMPLETE == accountModel.state) {
                // Normal Login since the account state == COMPLETE
                presentedView.onLoginSucceeded()
            } else if (SIGNUP_1 == accountModel.state) {
                // Show the "Check your Email" screen so they can resend email if needed
                // since they haven't started any of the web setup
                if (personModel != null) {
                    presentedView.onAccountCheckEmail(personModel.address)
                } else {
                    presentedView.onAccountCheckEmail("")
                }
            } else {
                // Show the "Almost Finished" Screen
                // Completed some, but not all of the web setup
                if (personModel != null) {
                    presentedView.onAccountAlmostFinished(personModel.firstName, personModel.email)
                } else {
                    presentedView.onAccountAlmostFinished(null, null)
                }
            }
        }
    }

    override fun onError(throwable: Throwable) {
        if (isPresenting) {
            presentedView.onLoginFailed(throwable is UnauthorizedException)
        }
    }

    private fun resolveResult(status: Status) {
        if (status.statusCode == CommonStatusCodes.RESOLUTION_REQUIRED) {
            try {
                // Prompt the user to choose a saved credential; do not show the hint selector.
                CredentialResolutionResultHandler.getInstance().setResolvedCredentialCallback { credential -> onCredentialRetrieved(credential) }
                status.startResolutionForResult(ArcusApplication.getArcusApplication().foregroundActivity, IntentRequestCode.CREDENTIAL_RETRIEVED.requestCode)
            } catch (e: IntentSender.SendIntentException) {
                logger.error("Failed to start credential resolution.", e)
            }

        } else {
            logger.debug("No shared credentials available, or error occurred. Status: " + status.statusCode + " - " + status.statusMessage)
        }
    }

    private fun onCredentialRetrieved(credential: Credential?) {
        if (credential != null) {
            val accountType = credential.accountType
            if (accountType == null) {
                presentedView.onRetrievedSharedCredential(credential.id, credential.password)
            }
        } else {
            logger.debug("Credential request complete; user denied access.")
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(LoginPresenter::class.java)
    }
}