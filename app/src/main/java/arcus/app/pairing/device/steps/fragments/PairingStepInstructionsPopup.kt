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
import android.annotation.TargetApi
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import arcus.app.R
import arcus.app.common.error.ErrorManager


class PairingStepInstructionsPopup: androidx.fragment.app.Fragment() {
    private lateinit var webView: WebView
    private lateinit var instructionsUrl: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { bundle ->
            instructionsUrl = bundle.getString(INSTRUCTIONS_URL)!!
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.pairing_steps_instructions, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<ImageView>(R.id.pairing_step_instructions_close_btn).setOnClickListener {
            activity?.finish()
        }

        this.webView = view.findViewById(R.id.pairing_step_instructions_webview)
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = InternalWebViewClient()
        webView.loadUrl(instructionsUrl)

        webView.setDownloadListener { url, _, _, mimeType, _ ->
            if (PDF_MIME_TYPE == mimeType) {
                webView.loadUrl(GOOGLE_DOCS_PDF_URL + url)
            }
        }

        webView.setOnKeyListener {_, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                when (keyCode) {
                    KeyEvent.KEYCODE_BACK -> {
                        activity?.finish()
                    }
                }
            }
            false
        }
    }

    inner class InternalWebViewClient: WebViewClient() {

        @Suppress("OverridingDeprecatedMember", "DEPRECATION")
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }

        // API level 23 and above
        @TargetApi(23)
        override fun onReceivedError(view: WebView, request: WebResourceRequest, error: WebResourceError) {
            ErrorManager.`in`(activity).showGenericBecauseOf(IllegalStateException(error.description.toString()))
            super.onReceivedError(view, request, error)
            activity?.finish()
        }

        @Suppress("OverridingDeprecatedMember", "DEPRECATION")
        override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
            ErrorManager.`in`(activity).showGenericBecauseOf(IllegalStateException(description))
            super.onReceivedError(view, errorCode, description, failingUrl)
            activity?.finish()
        }
    }

    companion object {
        const val INSTRUCTIONS_URL = "INSTRUCTIONS_URL"
        const val PDF_MIME_TYPE: String = "application/pdf"
        const val GOOGLE_DOCS_PDF_URL: String = "http://drive.google.com/viewerng/viewer?embedded=true&url="

        @JvmStatic
        fun newInstance(
                instructionsURL: String
        ): PairingStepInstructionsPopup {
            val fragment = PairingStepInstructionsPopup()
            with (fragment) {
                val args = Bundle()
                args.putString(INSTRUCTIONS_URL, instructionsURL)
                arguments = args
            }
            return fragment
        }
    }

}
