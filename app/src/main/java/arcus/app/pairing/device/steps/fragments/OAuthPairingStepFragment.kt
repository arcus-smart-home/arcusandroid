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
package arcus.app.pairing.device.steps.fragments

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.annotation.NonNull
import androidx.fragment.app.Fragment
import android.view.*
import android.webkit.CookieManager
import android.webkit.WebView
import arcus.cornea.SessionController

import arcus.app.R
import arcus.app.activities.DashboardActivity
import arcus.app.common.error.ErrorManager
import arcus.app.pairing.device.searching.DeviceSearchingActivity
import arcus.presentation.pairing.device.steps.OAuthDetails
import arcus.app.pairing.device.steps.oauth.client.OAuthPairingCallback
import arcus.app.pairing.device.steps.oauth.client.OAuthPairingClient

import org.slf4j.LoggerFactory
import kotlin.properties.Delegates


class OAuthPairingStepFragment : Fragment(), OAuthPairingCallback {
    private val logger = LoggerFactory.getLogger(OAuthPairingStepFragment::class.java)
    private var oAuthDetails by Delegates.notNull<OAuthDetails>()
    private var showCancelButton = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) : View? {
        setHasOptionsMenu(true)

        arguments?.run {
            oAuthDetails = getParcelable(OAUTH_DETAILS)!!
        }

        return inflater.inflate(R.layout.fragment_honeywell_webview, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onResume() {
        super.onResume()
        val view = view ?: return

        // This was in the original Lutron stuff, not in the nest though...
        if (oAuthDetails?.oAuthStyle == LUTRON_OAUTH_STYLE) {
            val lutronCookie = SessionController.instance().geLutronCookieValue()
            CookieManager.getInstance().setCookie(
                lutronCookie.first,
                lutronCookie.second
            )
        }

        val webView = view.findViewById<WebView>(R.id.webview)
        webView.webViewClient = OAuthPairingClient(this)
        webView.settings.javaScriptEnabled = true
        webView.settings.allowUniversalAccessFromFileURLs = true
        webView.loadUrl(oAuthDetails.oAuthUrl)

        activity?.run {
            title = getString(R.string.link_account_title_case)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        if (showCancelButton) {
            inflater.inflate(R.menu.menu_cancel, menu)
        } else {
            menu.removeItem(R.id.menu_cancel)
        }
    }

    override fun onOptionsItemSelected(@NonNull item: MenuItem) : Boolean {
        activity?.finish()
        return true
    }

    override fun onShowCancelButton(visible: Boolean) {
        logger.debug("Showing cancel button: $visible")
        showCancelButton = visible
        activity?.invalidateOptionsMenu()
    }

    override fun onAbortToDashboard() {
        logger.debug("Aborting account linking; returning user to dashboard.")

        val intent = Intent(context, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP

        startActivity(intent)
    }

    override fun onAccountLinkSuccess() {
        logger.debug("Successfully linked account.")
        activity?.let {
            startActivity(Intent(it, DeviceSearchingActivity::class.java))
            it.finish()
        }
    }

    override fun onWebError() {
        ErrorManager.`in`(activity).showGenericBecauseOf(IllegalStateException("An error occurred loading account linking pages."))
        onAbortToDashboard()
    }

    companion object {
        const val OAUTH_DETAILS = "OAUTH_DETAILS"
        const val LUTRON_OAUTH_STYLE = "LUTRON"

        @JvmStatic
        fun newInstance(oAuthDetails: OAuthDetails): OAuthPairingStepFragment {
            val instance = OAuthPairingStepFragment()
            val arguments = Bundle()
            arguments.putParcelable(OAUTH_DETAILS, oAuthDetails)
            instance.arguments = arguments
            return instance
        }
    }
}
