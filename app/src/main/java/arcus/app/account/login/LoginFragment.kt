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
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import arcus.app.R
import arcus.app.activities.DashboardActivity
import arcus.app.common.error.ErrorManager
import arcus.app.common.utils.LoginUtils
import arcus.app.common.validation.EmailValidator
import arcus.app.common.validation.NotEmptyValidator
import arcus.app.common.validation.UrlValidator
import arcus.app.common.view.ScleraEditText
import arcus.app.createaccount.CreateAccountActivity

class LoginFragment : Fragment(), LoginPresenterContract.LoginView {

    private lateinit var emailField: ScleraEditText
    private lateinit var passwordField: ScleraEditText
    private lateinit var errorBanner: LinearLayout
    private lateinit var genericErrorBanner: LinearLayout
    private lateinit var errorBannerText: TextView
    private lateinit var forgotPasswordLink: TextView
    private lateinit var useInviteCodeLink: TextView
    private lateinit var createAccountLink: TextView
    private lateinit var loginButton: Button
    private lateinit var indeterminateProgress: RelativeLayout
    private lateinit var platformUrlEntry: ScleraEditText
    private val presenter = LoginPresenter()

    private val isEmailValid: Boolean
        get() = EmailValidator(emailField).isValid

    private val placeId: String
        get() {
            return arguments?.getString(PLACE_ID, null).orEmpty()
        }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        errorBanner = view.findViewById<View>(R.id.error_banner) as LinearLayout
        genericErrorBanner = view.findViewById<View>(R.id.generic_error_banner) as LinearLayout
        errorBannerText = view.findViewById(R.id.error_banner_text)
        emailField = view.findViewById<View>(R.id.email) as ScleraEditText
        passwordField = view.findViewById<View>(R.id.password) as ScleraEditText
        forgotPasswordLink = view.findViewById(R.id.forgot_password)
        useInviteCodeLink = view.findViewById(R.id.use_invitation_code)
        createAccountLink = view.findViewById(R.id.create_account)
        loginButton = view.findViewById(R.id.login)
        indeterminateProgress = view.findViewById<View>(R.id.indeterminate_progress) as RelativeLayout
        platformUrlEntry = view.findViewById(R.id.platformUrl)
    }

    override fun onResume() {
        super.onResume()
        hideProgressBar()
        setupListeners()
        presenter.startPresenting(this)
    }

    override fun onPause() {
        super.onPause()
        presenter.stopPresenting()
    }

    private fun attemptLogin() {
        setCredentialErrorBannerVisible(false)

        // Both email/password validation should fire together, do not short-circuit in if-statement
        context?.run {
            // Why isn't the presenter doing all of this?
            val validEmail = LoginUtils.isMagicEmail(emailField.text.toString()) || isEmailValid
            val validPassword = NotEmptyValidator(this, passwordField, R.string.account_registration_verify_password_blank_error_msg).isValid
            val platformUrlValid = UrlValidator(platformUrlEntry).isValid

            if (validEmail && validPassword && platformUrlValid) {
                showProgressBar()
                presenter.login(
                        placeId,
                        emailField.text.toString(),
                        passwordField.text.toString().toCharArray(),
                        platformUrlEntry.text
                )
            }
        }
    }

    override fun showPlatformUrlEntry(value: String?) = platformUrlEntry.setText(value)

    override fun onLoginSucceeded() {
        context?.run {
            val intent = Intent(this, DashboardActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    override fun onLoginFailed(badCredentialsCause: Boolean) {
        hideProgressBar()
        setCredentialErrorBannerVisible(badCredentialsCause)
        setGenericErrorBannerVisible(!badCredentialsCause)
    }

    override fun onAccountAlmostFinished(personName: String, personEmail: String) {
        context?.run {
            startActivity(CreateAccountActivity.forAlmostFinishedLandingPage(
                    this,
                    personName,
                    personEmail
            ))
        }
    }

    override fun onAccountCheckEmail(personAddress: String) {
        context?.run {
            startActivity(CreateAccountActivity.forEmailSentLandingPage(
                this,
                personAddress
            ))
        }
    }

    override fun onPending(progressPercent: Int?) {
        setCredentialErrorBannerVisible(false)
        showProgressBar()
    }

    override fun onError(throwable: Throwable) {
        ErrorManager.`in`(activity).showGenericBecauseOf(throwable)
    }

    override fun updateView(model: Any) {
        hideProgressBar()
    }

    fun showProgressBar() {
        indeterminateProgress.visibility = View.VISIBLE
    }

    fun hideProgressBar() {
        indeterminateProgress.visibility = View.GONE
    }

    private fun setGenericErrorBannerVisible(visible: Boolean) {
        genericErrorBanner.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) {
            errorBannerText.linksClickable = true
        }
    }

    private fun setCredentialErrorBannerVisible(visible: Boolean) {
        errorBanner.visibility = if (visible) View.VISIBLE else View.GONE
        if (visible) {
            emailField.error = ""
            passwordField.error = ""
        }
    }

    private fun setupListeners() {
        loginButton.setOnClickListener { attemptLogin() }
        forgotPasswordLink.setOnClickListener {
            activity?.let {
                presenter.forgotPassword(it)
            }
        }
        useInviteCodeLink.setOnClickListener {
            activity?.let {
                presenter.useInvitationCode(it)
            }
        }

        // Validate email when user focuses other component
        emailField.setLostFocusListener { isEmailValid }

        // Validate password when user focuses other component
        passwordField.setLostFocusListener {
            context?.run {
                NotEmptyValidator(this, passwordField, R.string.account_registration_verify_password_blank_error_msg).isValid
            }
        }

        // Hide error banner when user modifies email
        emailField.setTextChangeListener { _, _ -> setCredentialErrorBannerVisible(false) }

        // Hide error banner when user modifies password
        passwordField.setTextChangeListener { _, _ -> setCredentialErrorBannerVisible(false) }

        val actionListener = TextView.OnEditorActionListener { v, _, _ ->
            attemptLogin()
            val imm = activity?.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(v.windowToken, 0)

            true
        }

        passwordField.setOnEditorActionListener(actionListener)
        platformUrlEntry.setOnEditorActionListener(actionListener)

        createAccountLink.setOnClickListener { _ ->
            activity?.run {
                startActivity(Intent(activity, CreateAccountActivity::class.java))
                overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.fade_out)
            }
        }
    }

    companion object {

        private val PLACE_ID = "PLACE_ID"

        @JvmStatic
        fun newInstance(placeId: String?): LoginFragment {
            val instance = LoginFragment()
            val arguments = Bundle()
            arguments.putString(PLACE_ID, placeId)
            instance.arguments = arguments
            return instance
        }
    }
}
