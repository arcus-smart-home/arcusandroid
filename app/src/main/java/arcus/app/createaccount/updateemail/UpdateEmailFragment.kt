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

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import arcus.app.R
import android.widget.Button
import android.widget.EditText
import arcus.app.common.utils.clearErrorsOnFocusChangedTo
import arcus.app.common.validation.EmailValidator
import com.google.android.material.textfield.TextInputLayout
import kotlin.properties.Delegates

class UpdateEmailFragment : Fragment(), UpdateEmailView {
    private lateinit var emailView    : EditText
    private lateinit var emailViewContainer : TextInputLayout
    private lateinit var alertBanner  : TextView
    private lateinit var focusHog     : View
    private var personAddress by Delegates.notNull<String>()
    private val presenter : UpdateEmailPresenter = UpdateEmailPresenterImpl()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_update_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        personAddress = arguments?.getString(ARG_PERSON_ADDRESS) ?: ""

        focusHog = view.findViewById(R.id.focus_hog)
        alertBanner = view.findViewById(R.id.alert_banner)

        emailViewContainer = view.findViewById(R.id.email_container)
        emailView = view.findViewById(R.id.email)
        emailViewContainer clearErrorsOnFocusChangedTo emailView
        emailView.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
            if (hasFocus) emailView.error = null
        }

        view.findViewById<Button>(R.id.update_and_resend_button).setOnClickListener {
            focusHog.requestFocusFromTouch()
            if (EmailValidator(emailViewContainer, emailView).isValid) {
                presenter.updateEmailAndSendVerification(emailView.text.toString())
            }
        }
        view.findViewById<Button>(R.id.cancel_button).setOnClickListener {
            activity?.finish()
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(R.string.update_email_address)
        presenter.setView(this)
        presenter.loadPersonFrom(personAddress)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.cleanUp()
    }

    override fun onEmailLoaded(email: String) {
        emailView.setText(email, TextView.BufferType.EDITABLE)
    }

    override fun onEmailSent() {
        alertBanner.visibility = View.GONE
        activity?.run {
            setResult(Activity.RESULT_OK)
            finish()
        }
    }

    override fun onUnhandledError() {
        alertBanner.visibility = View.VISIBLE
        emailView.error = getString(R.string.error_occurred)
    }

    override fun onDuplicateEmail() {
        alertBanner.visibility = View.VISIBLE
        emailView.error = getString(R.string.email_already_registered)
    }

    companion object {
        const val ARG_PERSON_ADDRESS = "ARG_PERSON_ADDRESS"

        @JvmStatic
        fun newInstance(personAddress: String) : UpdateEmailFragment {
            val fragment = UpdateEmailFragment()
            with (Bundle()) {
                putString(ARG_PERSON_ADDRESS, personAddress)
                fragment.arguments = this
            }
            return fragment
        }
    }
}
