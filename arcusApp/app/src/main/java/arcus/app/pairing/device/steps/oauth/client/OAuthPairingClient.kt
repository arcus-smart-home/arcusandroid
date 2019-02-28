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
@file:JvmMultifileClass
package arcus.app.pairing.device.steps.oauth.client

import android.net.http.SslError
import android.webkit.*
import arcus.cornea.SessionController

import org.slf4j.LoggerFactory

import java.lang.ref.WeakReference


interface OAuthPairingCallback {
    /**
     * Called to indicate whether or not the action bar "CANCEL" button should be displayed.
     * @param visible True to display cancel button; false to hide it.
     */
    fun onShowCancelButton(visible: Boolean)

    /**
     * Called to indicate pairing process has been aborted and user should be taken to dashboard.
     */
    fun onAbortToDashboard()

    /**
     * Called to indicate pairing process completed successfully, device models have been added, and
     * the user should be taken to the multi-pairing sequence to name and setup their new devices.
     */
    fun onAccountLinkSuccess()

    /**
     * Called to indicate a general web/connection error occured.
     */
    fun onWebError()
}


class OAuthPairingClient(listener: OAuthPairingCallback) : WebViewClient() {
    private val listenerRef = WeakReference(listener)

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        if (CANCELABLE_ERRORS.any { it == error.primaryError }) {
            handler.cancel()
        } else {
            handler.proceed()
        }
    }

    @Suppress("OverridingDeprecatedMember")
    override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
        logger.debug("Loading $url")
        listenerRef.get()?.onShowCancelButton(!url.contains(START_LOADING_URL_SHOW_CANCEL))
        return false
    }

    @Suppress("OverridingDeprecatedMember")
    override fun onReceivedError(
        view: WebView,
        errorCode: Int,
        description: String,
        failingUrl: String
    ) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        logger.error("Received WebView error $errorCode : $description when loading $failingUrl.")
        listenerRef.get()?.onWebError()
    }

    override fun onPageFinished(view: WebView, url: String) {
        logger.debug("Finished loading $url")

        listenerRef.get()?.let { listener ->
            when {
                REDIRECT_FLAGS.any { url.contains(it) } -> listener.onAccountLinkSuccess()
                ABORT_FLAGS.any { url.contains(it)} -> listener.onAbortToDashboard()
                else -> listener.onShowCancelButton(SHOW_CANCEL_URLS.any { url.contains(it) } || url.matches(HONEYWELL_SHOW_CANCEL))
            }
        }
    }


    companion object {
        private val REDIRECT_FLAGS = arrayOf(
            SessionController.instance().honeywellRedirectURI, // Honeywell
            "state=success", // Nest
            "pair/success"  // Lutron
        )

        private val ABORT_FLAGS = arrayOf(
            "state=error", // Nest
            "/pair/cancel" // Lutron
        )

        private const val START_LOADING_URL_SHOW_CANCEL = "example.com"
        private val HONEYWELL_SHOW_CANCEL = ".*honeywell.*Login.*".toRegex()

        private val SHOW_CANCEL_URLS = arrayOf(
            "home.nest.com",
            "www.example.com",
            "device-login.lutron.com",
            "support.example.com"
        )

        private val CANCELABLE_ERRORS = arrayOf(
            SslError.SSL_DATE_INVALID,
            SslError.SSL_IDMISMATCH,
            SslError.SSL_INVALID,
            SslError.SSL_UNTRUSTED
        )

        private val logger = LoggerFactory.getLogger(OAuthPairingClient::class.java)
    }
}