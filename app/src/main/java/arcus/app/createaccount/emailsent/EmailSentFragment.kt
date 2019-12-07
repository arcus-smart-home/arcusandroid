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
package arcus.app.createaccount.emailsent

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.activities.GenericFragmentActivity
import arcus.app.activities.LaunchActivity
import arcus.app.common.image.IntentRequestCode
import android.widget.Button
import arcus.app.common.view.ScleraLinkView
import arcus.app.common.view.ScleraTextView
import arcus.app.createaccount.CreateAccountFlow
import arcus.app.createaccount.SuccessSnackbar
import arcus.app.createaccount.updateemail.UpdateEmailFragment
import kotlin.properties.Delegates

class EmailSentFragment : Fragment(), EmailSentView {
    private var callback : CreateAccountFlow? = null
    private lateinit var alertBanner  : View
    private lateinit var emailField   : ScleraTextView

    var personAddress by Delegates.notNull<String>()
        private set

    private val presenter : EmailSentPresenter = EmailSentPresenterImpl()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        personAddress = arguments?.getString(ARG_PERSON_ADDRESS) ?: ""
        return inflater.inflate(R.layout.fragment_email_sent, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        alertBanner = view.findViewById(R.id.alert_banner)

        view.findViewById<Button>(R.id.resend_email_button).setOnClickListener {
            presenter.resendEmail()
        }

        view.findViewById<Button>(R.id.logout_button).setOnClickListener {
            presenter.logout()
        }

        emailField = view.findViewById(R.id.email_address)

        view.findViewById<ScleraLinkView>(R.id.wrong_email_link).setOnClickListener {
            val args = Bundle()
            args.putString(UpdateEmailFragment.ARG_PERSON_ADDRESS, personAddress)

            activity?.let {
                startActivityForResult(
                    GenericFragmentActivity.getLaunchIntent(
                        it,
                        UpdateEmailFragment::class.java,
                        args
                    ), IntentRequestCode.EMAIL_SENT_SUCCESS.requestCode)
                it.overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.fade_out)
            }
        }
    }

    private fun showEmailSentSnackbar() {
        view?.run {
            SuccessSnackbar
                .make(findViewById(R.id.check_email_main_layout))
                .setText(R.string.email_sent)
                .show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            IntentRequestCode.EMAIL_SENT_SUCCESS.requestCode -> {
                if (resultCode == Activity.RESULT_OK) {
                    showEmailSentSnackbar()
                }
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is CreateAccountFlow) {
            callback = context
        }
    }

    override fun onStart() {
        super.onStart()
        activity?.setTitle(R.string.check_your_email)
        callback?.hideBackButton()

        presenter.setView(this)
        presenter.loadPersonFromAddress(personAddress)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.cleanUp()
    }

    override fun onEmailLoaded(email: String) {
        alertBanner.visibility = View.GONE
        emailField.text = email
    }

    override fun onEmailSent() {
        alertBanner.visibility = View.GONE
        showEmailSentSnackbar()
    }

    override fun onLoggedOut() {
        activity?.let {
            LaunchActivity.startLoginScreen(it)
        }
    }

    override fun onUnhandledError() {
        alertBanner.visibility = View.VISIBLE
    }

    companion object {
        const val ARG_PERSON_ADDRESS = "ARG_PERSON_ADDRESS"

        @JvmStatic
        fun newInstance(personAddress: String) : EmailSentFragment {
            val fragment = EmailSentFragment()
            with (Bundle()) {
                putString(ARG_PERSON_ADDRESS, personAddress)
                fragment.arguments = this
            }
            return fragment
        }
    }
}
