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
package arcus.app.launch

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import arcus.cornea.CorneaService
import arcus.cornea.utils.Listeners
import arcus.cornea.utils.LooperExecutor
import com.iris.client.service.PersonService
import arcus.app.ArcusApplication
import arcus.app.R
import arcus.app.activities.LaunchActivity
import arcus.app.common.error.ErrorManager.`in` as errorIn
import arcus.app.common.fragment.FragmentContainerHolder
import arcus.app.common.utils.PreferenceUtils
import arcus.app.common.utils.enableViews
import arcus.app.common.utils.inflate
import arcus.app.common.validation.CustomEmailValidator
import arcus.app.common.view.ButtonWithProgress
import arcus.app.common.view.ScleraLinkView
import com.google.android.material.textfield.TextInputLayout
import org.slf4j.LoggerFactory
import kotlin.concurrent.thread

// TODO: This should have it's logic put into a presenter so we're not calling Cornea directly from the app. We'll need to come back to this at some point if we ever hope to decouple the app from Cornea...
class AccountLoginForgotFragment : Fragment() {

    private lateinit var email: EditText
    private lateinit var emailContainer: TextInputLayout
    private lateinit var bypassLink: ScleraLinkView
    private lateinit var submitBtn: ButtonWithProgress
    private lateinit var cancelBtn: Button
    private lateinit var fieldEnableGroups: List<View>

    private var fragmentContainerHolder: FragmentContainerHolder? = null

    val corneaService: CorneaService
        get() = ArcusApplication.getArcusApplication().corneaService

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_account_login_forgot)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = getString(R.string.account_registration_forgot_password)

        submitBtn = view.findViewById(R.id.btnSubmitForgot)
        cancelBtn = view.findViewById(R.id.btnCancel)
        bypassLink = view.findViewById(R.id.email_entry_bypass_link)
        email = view.findViewById(R.id.email)
        emailContainer = view.findViewById(R.id.email_container)
        fieldEnableGroups = listOf(submitBtn, cancelBtn, bypassLink, email)

        email.requestFocus()

        bypassLink.setOnClickListener { _ ->
            fragmentContainerHolder?.replaceFragmentContainerWith(AccountLoginEmailSentFragment.newInstance())
        }

        submitBtn.setOnClickListener { _ ->
            val validator = CustomEmailValidator(
                emailContainer,
                email,
                R.string.account_registration_missing_email_error_msg_v2,
                R.string.account_registration_email_well_formed_error_msg_v2
            )
            if (validator.isValid) {
                emailContainer.error = null
                fieldEnableGroups.enableViews(false)

                // Because we're using a hostname here, Netty (during bootstrap.connect) is doing an InetSockAddress creation...
                // threading this prevents weird NPE errors - which I belive to actually be an android NMT exception sice InetSockAddress
                // does a name lookup which requires the entry to be cached already or using DNS (network).
                // TODO: This used to do this... But that may have been before we switched to NettyClient2 ... Would be worthwhile to re-validate this behavior.
                thread {
                    try {
                        corneaService.setConnectionURL(PreferenceUtils.getPlatformUrl())
                        corneaService
                            .getService(PersonService::class.java)
                            .sendPasswordReset(
                                getEmailAddress(),
                                PersonService.SendPasswordResetRequest.METHOD_EMAIL
                            )
                            .onCompletion(Listeners.runOnUiThread { result ->
                                onlyIfAddedAndNotDetached {
                                    fieldEnableGroups.enableViews(true)

                                    if (result.isError) {
                                        logger.debug("Received Exception response from sendPasswordReset.", result.error)
                                        errorIn(activity).showGenericBecauseOf(result.error)
                                    } else {
                                        fragmentContainerHolder?.replaceFragmentContainerWith(
                                            AccountLoginEmailSentFragment.newInstance(getEmailAddress())
                                        )
                                    }
                                }
                            })
                    } catch (ex: Exception) {
                        LooperExecutor.getMainExecutor().execute {
                            onlyIfAddedAndNotDetached {
                                fieldEnableGroups.enableViews(true)
                                errorIn(activity).showGenericBecauseOf(ex)
                            }
                        }
                    }
                }
            }
        }

        cancelBtn.setOnClickListener { _ -> LaunchActivity.startLoginScreen(activity) }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContainerHolder = context as? FragmentContainerHolder?
    }

    override fun onResume() {
        super.onResume()
        fragmentContainerHolder?.showBackButtonOnToolbar(false)
    }

    private fun getEmailAddress() = email.text.toString().trim { char -> char < ' ' }

    private inline fun onlyIfAddedAndNotDetached(block: () -> Unit) {
        if (isAdded && !isDetached) {
            block()
        }
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AccountLoginForgotFragment::class.java)
    }
}
