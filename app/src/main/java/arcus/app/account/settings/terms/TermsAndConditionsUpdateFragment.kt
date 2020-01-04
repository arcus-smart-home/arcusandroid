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
package arcus.app.account.settings.terms

import android.os.Bundle
import android.text.Html
import android.text.Spannable
import android.text.method.LinkMovementMethod
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.core.view.isVisible
import arcus.app.R
import arcus.app.common.backstack.BackstackManager
import arcus.app.common.error.ErrorManager.`in` as errorIn
import arcus.app.common.fragments.NoViewModelFragment
import arcus.app.common.utils.GlobalSetting
import arcus.app.common.utils.ViewUtils
import arcus.app.dashboard.HomeFragment
import arcus.cornea.account.TermsAndConditionsContract
import arcus.cornea.account.TermsAndConditionsPresenter

class TermsAndConditionsUpdateFragment : NoViewModelFragment(), TermsAndConditionsContract.View {
    private lateinit var presenter: TermsAndConditionsContract.Presenter
    private lateinit var acceptButton: Button

    override val title: String = ""
    override val layoutId: Int = R.layout.terms_and_conditions

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        presenter = TermsAndConditionsPresenter(this)

        val privacyString = getLinkString(GlobalSetting.PRIVACY_LINK, getString(R.string.privacy_statement))
        val termsString = getLinkString(GlobalSetting.T_AND_C_LINK, getString(R.string.terms_of_service))
        val madeChangesCopy = Html.fromHtml(getString(
                R.string.made_some_changes_with_placeholders,
                termsString,
                privacyString
            )) as Spannable
        val clickAcceptCopy = Html.fromHtml(getString(R.string.clicking_accept_agree, termsString, privacyString)) as Spannable
        ViewUtils.removeUnderlines(madeChangesCopy, clickAcceptCopy)
        val madeChanges = view.findViewById<TextView>(R.id.made_changes_copy)
        val clickAccept = view.findViewById<TextView>(R.id.click_accept_copy)
        madeChanges.text = madeChangesCopy
        madeChanges.movementMethod = LinkMovementMethod.getInstance()
        clickAccept.text = clickAcceptCopy
        clickAccept.movementMethod = LinkMovementMethod.getInstance()
        acceptButton = view.findViewById(R.id.terms_accept_button)
        acceptButton.setOnClickListener {
            progressContainer.isVisible = true
            acceptButton.isEnabled = false
            presenter.acceptTermsAndConditions()
        }
    }

    override fun onResume() {
        super.onResume()
        hideActionBar()

        // If user clicks accept but then gets a call, when we resume we want to check to see if we still need to accept
        presenter.recheckNeedToAccept()
    }

    override fun onBackPressed(): Boolean = true // Consume this event.

    override fun onPause() {
        super.onPause()
        presenter.clearReferences()
    }

    override fun onDestroy() {
        super.onDestroy()
        progressContainer.isVisible = false
        showActionBar()
    }

    override fun acceptRequired() {
        progressContainer.isVisible = false
        acceptButton.isEnabled = true
    }

    override fun onAcceptSuccess() {
        BackstackManager.getInstance().rewindToFragment(HomeFragment.newInstance())
    }

    override fun onError(throwable: Throwable) {
        acceptRequired()
        errorIn(activity).showGenericBecauseOf(throwable)
    }

    private fun getLinkString(httpUrl: String, displayText: String): String = "<a href=\"$httpUrl\">$displayText</a>"

    companion object {
        @JvmStatic
        fun newInstance(): TermsAndConditionsUpdateFragment = TermsAndConditionsUpdateFragment()
    }
}
