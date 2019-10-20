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
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import arcus.app.R
import arcus.app.common.view.ScleraButton
import arcus.app.common.view.ScleraEditText
import arcus.app.common.view.ScleraTextView
import com.rengwuxian.materialedittext.validation.METValidator
import kotlin.properties.Delegates

class UpdateEmailFragment : Fragment(), UpdateEmailView {
    private lateinit var emailView    : ScleraEditText
    private lateinit var alertBanner  : ScleraTextView
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

        emailView = view.findViewById(R.id.email)
        emailView.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            emailView.onFocusChange(v, hasFocus)
            if (hasFocus) {
                emailView.error = null
            }
        }
        emailView.addValidator(object : METValidator(getString(R.string.invalid_email)) {
            override fun isValid(text: CharSequence, isEmpty: Boolean): Boolean {
                return android.util.Patterns.EMAIL_ADDRESS.matcher(text).matches()
            }
        })

        view.findViewById<ScleraButton>(R.id.update_and_resend_button).setOnClickListener {
            focusHog.requestFocusFromTouch()
            if (emailView.validate()) {
                presenter.updateEmailAndSendVerification(emailView.text.toString())
            }
        }
        view.findViewById<ScleraButton>(R.id.cancel_button).setOnClickListener {
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