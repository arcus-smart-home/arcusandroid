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

import android.app.Activity
import arcus.cornea.SessionController
import arcus.cornea.common.BasePresenter
import com.iris.client.exception.UnauthorizedException
import com.iris.client.model.AccountModel
import com.iris.client.model.PersonModel
import com.iris.client.model.PlaceModel
import com.iris.client.session.Credentials
import com.iris.client.session.UsernameAndPasswordCredentials
import arcus.app.activities.GenericConnectedFragmentActivity
import arcus.app.activities.InvitationActivity
import arcus.app.common.utils.LoginUtils
import arcus.app.common.utils.PreferenceUtils
import arcus.app.createaccount.COMPLETE
import arcus.app.createaccount.SIGNUP_1
import arcus.app.launch.AccountLoginForgotFragment
import org.slf4j.LoggerFactory

class LoginPresenter : BasePresenter<LoginPresenterContract.LoginView>(), LoginPresenterContract.LoginPresenter, SessionController.LoginCallback {

    override fun startPresenting(view: LoginPresenterContract.LoginView?) {
        super.startPresenting(view)

        val platformUrl = PreferenceUtils.getPlatformUrl()
        if (platformUrl.isBlank()) {
            view?.showPlatformUrlEntry(null)
        } else {
            view?.showPlatformUrlEntry(platformUrl)
        }
    }

    override fun login(
            placeId: String,
            username: String,
            password: CharArray,
            platformUrl: CharSequence
    ) {
        addListener(SessionController::class.java.simpleName, SessionController.instance().setCallback(this))

        val credentials = getUsernamePasswordCredentials(username, password, platformUrl)

        logger.debug("Signing into requested place {}", LoginUtils.getContextualPlaceIdOrLastUsed(placeId))
        SessionController.instance().login(credentials, LoginUtils.getContextualPlaceIdOrLastUsed(placeId))
    }

    override fun useInvitationCode(activity: Activity) {
        presentedView.onPending(null)
        InvitationActivity.start(activity)
        activity.finish()
    }

    override fun forgotPassword(activity: Activity) {
        presentedView.onPending(null)
        activity.startActivity(
                GenericConnectedFragmentActivity.getLaunchIntent(
                        activity,
                        AccountLoginForgotFragment::class.java,
                        allowBackPress = false
                )
        )
        activity.finish()
    }

    private fun getUsernamePasswordCredentials(
            username: String,
            password: CharArray,
            platformUrl: CharSequence
    ): Credentials {
        PreferenceUtils.putPlatformUrl(platformUrl.trim().toString())
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

    companion object {
        private val logger = LoggerFactory.getLogger(LoginPresenter::class.java)
    }
}
