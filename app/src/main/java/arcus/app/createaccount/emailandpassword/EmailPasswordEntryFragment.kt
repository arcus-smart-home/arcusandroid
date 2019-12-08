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

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.text.Html
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import arcus.app.R
import arcus.app.common.utils.GlobalSetting
import arcus.app.common.utils.PreferenceUtils
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import arcus.app.common.utils.clearErrorsOnFocusChangedTo
import arcus.app.common.utils.textOrEmpty
import arcus.app.common.validation.EmailValidator
import arcus.app.createaccount.CreateAccountFlow
import arcus.app.createaccount.nameandphone.NamePhoneAndImageLocation
import com.google.android.material.textfield.TextInputLayout
import kotlin.properties.Delegates

class EmailPasswordEntryFragment : Fragment(), EmailPasswordEntryView {
    private lateinit var callback     : CreateAccountFlow
    private lateinit var userInfo     : NamePhoneAndImageLocation

    private lateinit var nextButton   : Button
    private lateinit var email        : EditText
    private lateinit var emailContainer : TextInputLayout
    private lateinit var password     : EditText
    private lateinit var passwordContainer : TextInputLayout
    private lateinit var confirmPass  : EditText
    private lateinit var confirmPassContainer : TextInputLayout
    private lateinit var alertBanner  : TextView
    private lateinit var offersNPromo : CheckBox
    private lateinit var focusHog     : View

    private val presenter : EmailPasswordEntryPresenter = EmailPasswordEntryPresenterImpl(
            PreferenceUtils.getPlatformUrl()
    )

    var personAddress by Delegates.notNull<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userInfo = arguments?.getParcelable(ARG_USER_INFO) ?: NamePhoneAndImageLocation.EMPTY
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_email_password_entry, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alertBanner = view.findViewById(R.id.alert_banner)
        offersNPromo = view.findViewById(R.id.offers_and_promos)
        focusHog = view.findViewById(R.id.focus_hog)

        email = view.findViewById(R.id.email)
        emailContainer = view.findViewById(R.id.email_container)
        emailContainer clearErrorsOnFocusChangedTo email

        password = view.findViewById(R.id.password)
        passwordContainer = view.findViewById(R.id.password_container)
        passwordContainer.clearErrorsOnFocusChangedTo(password) { hasFocus ->
            if (hasFocus) {
                setAlertBannerVisibility(View.GONE)
                confirmPassContainer.error = null
            }
        }

        confirmPass = view.findViewById(R.id.confirm_password)
        confirmPassContainer = view.findViewById(R.id.confirm_password_container)
        confirmPassContainer.clearErrorsOnFocusChangedTo(confirmPass) { hasFocus ->
            if (hasFocus) {
                setAlertBannerVisibility(View.GONE)
                passwordContainer.error = null
            }
        }

        nextButton = view.findViewById(R.id.next_button)
        nextButton.setOnClickListener { trySignUp() }

        val privacyString = getLinkString(GlobalSetting.PRIVACY_LINK, getString(R.string.privacy_statement))
        val termsString = getLinkString(GlobalSetting.T_AND_C_LINK, getString(R.string.arcus_terms_of_service))
        val clickAcceptCopy = Html.fromHtml(getString(
                R.string.clicking_accept_agree_2,
                termsString,
                privacyString
            )) as Spannable
        val acceptText = view.findViewById<TextView>(R.id.terms_and_conditions_text)
        acceptText.text = clickAcceptCopy
        acceptText.movementMethod = LinkMovementMethod.getInstance()
    }

    private fun trySignUp() {
        focusHog.requestFocusFromTouch()
        val emailOk = EmailValidator(emailContainer, email).isValid
        val passwordOk = presenter.passwordIsValid(password.textOrEmpty()).apply {
            if (!this) {
                passwordContainer.error = getString(R.string.invalid_password)
            }
        }

        val confirmPassOk = passwordOk && presenter.passwordsMatch(
                password.textOrEmpty(),
                confirmPass.textOrEmpty()
        ).apply {
            if (!this) {
                passwordContainer.error = null
                confirmPassContainer.error = getString(R.string.passwords_do_not_match)
            }
        }
        if (emailOk && passwordOk && confirmPassOk) {
            presenter
                .signupUsing(
                    NewAccountInformation(
                        email.text.toString(),
                        password.text.toString(),
                        offersNPromo.isChecked,
                        userInfo
                    )
                )
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        check(context is CreateAccountFlow) { "Dev: Make sure the host implements CreateAccountFlow." }
        callback = context
    }

    override fun onStart() {
        super.onStart()
        presenter.setView(this)
        callback.showBackButton()
        activity?.setTitle(R.string.account_registration_create_account)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.cleanUp()
    }

    private fun getLinkString(httpUrl: String, displayText: String) = "<a href=\"$httpUrl\">$displayText</a>"

    private fun setAlertBannerVisibility(to: Int) {
        if (alertBanner.visibility != to) {
            alertBanner.visibility = to
        }
    }

    override fun onAccountCreatedFor(personAddress: String) {
        this.personAddress = personAddress
        callback.nextFrom(this)
    }

    override fun onDuplicateEmail() {
        email.error = getString(R.string.email_already_registered)
        alertBanner.text = getString(R.string.error_fix_highlight)
        setAlertBannerVisibility(View.VISIBLE)
    }

    override fun onUnhandledError() {
        alertBanner.text = getString(R.string.error_occurred)
        setAlertBannerVisibility(View.VISIBLE)
    }

    override fun onLoading() {
        callback.loading(true)
    }

    override fun onLoadingComplete() {
        callback.loading(false)
    }

    companion object {
        private const val ARG_USER_INFO = "ARG_USER_INFO"

        @JvmStatic
        fun newInstance(userInfo: NamePhoneAndImageLocation) : EmailPasswordEntryFragment {
            val fragment =
                EmailPasswordEntryFragment()
            with (Bundle()) {
                putParcelable(ARG_USER_INFO, userInfo)
                fragment.arguments = this
            }
            return fragment
        }
    }
}
