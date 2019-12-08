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
import android.widget.EditText
import android.widget.TextView
import arcus.cornea.SessionController
import arcus.cornea.utils.Listeners
import com.iris.client.exception.ErrorResponseException
import com.iris.client.model.AccountModel
import com.iris.client.model.PersonModel
import com.iris.client.model.PlaceModel
import com.iris.client.session.ResetPasswordCredentials
import arcus.app.R
import arcus.app.activities.BaseActivity.logger
import arcus.app.activities.DashboardActivity
import arcus.app.common.fragment.FragmentContainerHolder
import arcus.app.common.utils.LoginUtils
import arcus.app.common.utils.PreferenceUtils
import arcus.app.common.utils.clearErrorsOnFocusChangedTo
import arcus.app.common.utils.enableViews
import arcus.app.common.utils.inflate
import arcus.app.common.validation.CustomEmailValidator
import arcus.app.common.validation.NotEmptyValidator
import arcus.app.common.validation.PasswordValidator
import arcus.app.common.view.ButtonWithProgress
import com.google.android.material.textfield.TextInputLayout

// TODO: This should have it's logic put into a presenter so we're not calling Cornea directly from the app. We'll need to come back to this at some point if we ever hope to decouple the app from Cornea...
class AccountLoginEmailSentFragment : Fragment() {
    private var emailAddress: String? = null
    private var fromCustomerSupport: Boolean = false

    private lateinit var headerText: TextView
    private lateinit var subHeaderText: TextView
    private lateinit var email: EditText
    private lateinit var emailContainer: TextInputLayout
    private lateinit var code: EditText
    private lateinit var codeContainer: TextInputLayout
    private lateinit var newPassword: EditText
    private lateinit var newPasswordContainer: TextInputLayout
    private lateinit var confirmPassword: EditText
    private lateinit var confirmPasswordContainer: TextInputLayout
    private lateinit var banner: EditText
    private lateinit var fieldEnableGroups: List<View>
    private lateinit var submitButtonWithProgress: ButtonWithProgress
    private var fragmentContainerHolder: FragmentContainerHolder? = null

    private var listenerRegistration = Listeners.empty()
    private val loginCallback = object : SessionController.LoginCallback {
        override fun loginSuccess(placeModel: PlaceModel?, personModel: PersonModel?, accountModel: AccountModel?) {
            enableFieldGroup(true)
            LoginUtils.completeLogin()
            Listeners.clear(listenerRegistration)

            activity?.let { nnActivity ->
                nnActivity.runOnUiThread {
                    DashboardActivity.startActivity(nnActivity)
                    nnActivity.finish()
                }
            }
        }

        override fun onError(throwable: Throwable) {
            Listeners.clear(listenerRegistration)
            activity?.runOnUiThread {
                enableFieldGroup(true)

                if (throwable is ErrorResponseException && throwable.code == ERROR_TOKEN_CODE) {
                    banner.setText(getString(R.string.error_fix_highlight))
                    codeContainer.error = getString(R.string.invalid_code)
                } else {
                    banner.setText(getString(R.string.error_occurred))
                }

                banner.visibility = View.VISIBLE
            }

            logger.error("Error logging in. ", throwable)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_account_login_email_sent)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activity?.title = getString(R.string.forgot_password_check_inbox)

        headerText = view.findViewById(R.id.email_sent_text)
        subHeaderText = view.findViewById(R.id.forgot_password_text_3)
        email = view.findViewById(R.id.email)
        emailContainer = view.findViewById(R.id.email_container)
        code = view.findViewById(R.id.etCode)
        codeContainer = view.findViewById(R.id.etCode_container)
        newPassword = view.findViewById(R.id.etNewPassword)
        newPasswordContainer = view.findViewById(R.id.etNewPassword_container)
        confirmPassword = view.findViewById(R.id.etConfirmPassword)
        confirmPasswordContainer = view.findViewById(R.id.etConfirmPassword_container)
        submitButtonWithProgress = view.findViewById(R.id.fragment_email_sent_btn)
        banner = view.findViewById(R.id.alert_banner)
        fieldEnableGroups = listOf(email, code, newPassword, confirmPassword, submitButtonWithProgress)

        newPasswordContainer clearErrorsOnFocusChangedTo newPassword
        confirmPasswordContainer clearErrorsOnFocusChangedTo confirmPassword

        arguments?.let {
            emailAddress = it.getString(EMAIL_ADDRESS)
            fromCustomerSupport = it.getBoolean(FROM_CUSTOMER_SUPPORT)
        }

        if (fromCustomerSupport) {
            headerText.text = getString(R.string.support_sent_code)
            subHeaderText.text = getString(R.string.contact_support_no_email)
            email.visibility = View.VISIBLE
        } else {
            headerText.text = String.format(getString(R.string.forgot_password_email_sent), emailAddress)
            subHeaderText.text = getString(R.string.contact_support_forgot_password)
            email.visibility = View.GONE
            email.setText(emailAddress, TextView.BufferType.EDITABLE)
        }

        submitButtonWithProgress.setOnClickListener {
            banner.visibility = View.GONE
            emailAddress = email.text.toString()

            val emailValidator = CustomEmailValidator(
                emailContainer,
                email,
                R.string.account_registration_missing_email_error_msg_v2,
                R.string.account_registration_email_well_formed_error_msg_v2
            )
            val passwordValidator = PasswordValidator(activity, newPasswordContainer, newPassword, confirmPasswordContainer, confirmPassword, emailAddress)
            val codeValidator = NotEmptyValidator(code.context, code, R.string.missing_code)

            if (codeValidator.isValid && emailValidator.isValid && passwordValidator.isValid) {
                enableFieldGroup(false)
                val credentials = ResetPasswordCredentials(emailAddress, code.text.toString(), newPassword.text.toString())
                credentials.connectionURL = PreferenceUtils.getPlatformUrl()
                listenerRegistration = SessionController.instance().setCallback(loginCallback)
                SessionController.instance().login(credentials, LoginUtils.getContextualPlaceIdOrLastUsed(null))
            }
        }
    }

    private fun enableFieldGroup(enable: Boolean) {
        fieldEnableGroups.enableViews(enable)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContainerHolder = context as? FragmentContainerHolder?
    }

    override fun onResume() {
        super.onResume()
        fragmentContainerHolder?.showBackButtonOnToolbar(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        Listeners.clear(listenerRegistration)
    }

    companion object {
        private const val EMAIL_ADDRESS = "EMAIL_ADDRESS"
        private const val FROM_CUSTOMER_SUPPORT = "FROM_CUSTOMER_SUPPORT"
        private const val ERROR_TOKEN_CODE = "person.reset.token_failed"

        @JvmStatic
        @JvmOverloads
        fun newInstance(
            emailAddress: String = ""
        ) = AccountLoginEmailSentFragment().also { fragment ->
            with (Bundle(2)) {
                putString(EMAIL_ADDRESS, emailAddress)
                putBoolean(FROM_CUSTOMER_SUPPORT, emailAddress.isBlank())
                fragment.arguments = this
            }
        }
    }
}
