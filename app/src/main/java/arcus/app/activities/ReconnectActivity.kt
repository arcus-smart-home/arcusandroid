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
package arcus.app.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.StringRes

import arcus.cornea.SessionController
import arcus.cornea.utils.Listeners
import com.iris.client.event.ListenerRegistration
import com.iris.client.model.AccountModel
import com.iris.client.model.PersonModel
import com.iris.client.model.PlaceModel
import com.iris.client.session.SessionTokenCredentials
import arcus.app.R
import arcus.app.account.fingerprint.FingerprintPopup
import arcus.app.common.utils.BiometricLoginUtils
import arcus.app.common.utils.LoginUtils
import arcus.app.common.utils.PreferenceUtils

import org.slf4j.LoggerFactory

/*
 * This activity is used to reconnect to the platform automatically.
 *
 * If a previous token is not found, this will kill all activities and launch the LaunchActivity.
 * If a previous token IS found, this will try to login to the platform again using that token.  If that fails,
 * the LaunchActivity will be shown; if it succeeds, this activity will simply be dismissed unless a new Intent
 * is passed to it, then that intent will be launched.
 */
open class ReConnectActivity : PermissionsActivity() {
    private var listenerReg: ListenerRegistration? = null
    private var callback: LoginCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.launch_progress)
    }

    override fun onResume() {
        super.onResume()

        val cachedLoginToken = PreferenceUtils.getLoginToken()
        if (cachedLoginToken == null) {
            relaunchLoginActivity()
        } else {
            // If for some reason fingerprint deleted or screen lock disabled while the app was backgrounded
            if (!BiometricLoginUtils.fingerprintUnavailable(this).isNullOrEmpty()) {
                PreferenceUtils.setUseFingerPrint(false)
            }

            // Use fingerprint, but don't display the prompt if login is already in progress
            if (PreferenceUtils.getUsesFingerPrint() && !SessionController.instance().isLoginPending) {
                val credentials = LoginUtils.getSessionTokenCredentials(cachedLoginToken)
                initializeFingerprintPrompt(
                    credentials,
                    R.string.fingerprint_sign_in,
                    R.string.fingerprint_instructions
                )
            } else { // If not using fingerprint, log in normally
                loginWithCredentials(LoginUtils.getSessionTokenCredentials(cachedLoginToken))
            }
        }
    }

    override fun onBackPressed() {
        // No-Op --> Do nothing on back press.
    }

    /**
     * Create the fingerprint dialog
     */
    private fun initializeFingerprintPrompt(
        credentials: SessionTokenCredentials,
        @StringRes dialogTitle: Int,
        @StringRes dialogMessage: Int) {
        val callback = object : FingerprintPopup.FingerprintPopupCallback {
            override fun successfullyAuthenticated() {
                // Process login now
                loginWithCredentials(credentials)
            }

            override fun failedAuthentication() {
                relaunchLoginActivity()
            }

            override fun onCanceled() {
                relaunchLoginActivity()
            }
        }

        FingerprintPopup
            .newInstance(getString(dialogTitle), getString(dialogMessage), callback)
            .show(fragmentManager, "Fingerprint Login Popup")
    }

    /**
     * Login to the platform using the given credentials. Navigate to the dashboard upon success, or
     * display the login screen on failure.
     *
     * @param credentials Credentials to login with.
     */
    private fun loginWithCredentials(credentials: SessionTokenCredentials) {
        try { // Try to login using an existing token
            val lastPlace = LoginUtils.getContextualPlaceIdOrLastUsed(intent.getStringExtra(EXTRA_PLACE_ID))
            logger.debug("Logging in with requested place {}", lastPlace)

            callback = LoginCallback(credentials.token)
            listenerReg = SessionController.instance().setCallback(callback)
            SessionController.instance().login(credentials, lastPlace)
        } catch (e: Exception) {
            logger.error("Can't get client from ClientFactory: {}", e)

            // Should only be used when we want to reset the singletons in the App - not being able to get the Cornea
            // client would be a good example.
            val intent = Intent(this, LaunchActivity::class.java)
            startActivity(intent)
            finish()

            Runtime.getRuntime().exit(0)
        }
    }

    // Relaunch the login activity making it the root of the task stack
    private fun relaunchLoginActivity() {
        LoginUtils.completeLogout()

        val launchIntent = Intent(this, LaunchActivity::class.java)
        val actualIntent = Intent.makeRestartActivityTask(launchIntent.component)
        startActivity(actualIntent)

        finishAffinity() // Close this and (parent) activities of the same affinity
    }

    /**
     * Callback for auto-login results.
     */
    private inner class LoginCallback internal constructor(private val token: String) : SessionController.LoginCallback {
        override fun loginSuccess(
            placeModel: PlaceModel?,
            personModel: PersonModel?,
            accountModel: AccountModel?) {
            LoginUtils.completeLogin()

            Listeners.clear(listenerReg)

            intent.getParcelableExtra<Intent>(EXTRA_NEXT_INTENT)?.let {
                startActivity(it)
            }

            finish()
        }

        override fun onError(throwable: Throwable) {
            logger.error("Login failed; displaying login screen.")

            Listeners.clear(listenerReg)
            LoginUtils.logLoginFailure(throwable, token)
            relaunchLoginActivity()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(ReConnectActivity::class.java)
        private const val EXTRA_NEXT_INTENT = "EXTRA_NEXT_INTENT"
        private const val EXTRA_PLACE_ID = "EXTRA_PLACE_ID"

        @JvmOverloads
        @JvmStatic
        fun getStartIntent(
            context: Context,
            nextIntent: Intent? = null,
            placeId: String? = null
        ) = Intent(context, ReConnectActivity::class.java).also { startIntent ->
            nextIntent?.let { nnNextIntent ->
                startIntent.putExtra(EXTRA_NEXT_INTENT, nnNextIntent)
            }

            placeId?.let { nnPlaceId ->
                startIntent.putExtra(EXTRA_PLACE_ID, nnPlaceId)
            }
        }
    }
}
