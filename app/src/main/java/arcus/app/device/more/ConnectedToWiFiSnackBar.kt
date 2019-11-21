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
package arcus.app.device.more

import com.google.android.material.snackbar.BaseTransientBottomBar
import androidx.core.content.ContextCompat
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import arcus.app.R
import arcus.app.common.utils.FontUtils
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraTextView


class ConnectedToWiFiSnackBar private constructor(
        parent: ViewGroup,
        content: View,
        contentViewCallback: ContentViewCallback
) : BaseTransientBottomBar<ConnectedToWiFiSnackBar>(parent, content, contentViewCallback) {
    private val snackBarOriginalView : View
    init {
        val color = ContextCompat.getColor(context, R.color.sclera_snackbar_green)
        snackBarOriginalView = view
        view.setBackgroundColor(color)

        // Our custom view is actually being added to the snackbars view - which has some
        // padding on left and right If we don't set the bg color here, that will show-through
    }

    companion object {
        @JvmStatic
        @JvmOverloads
        fun make(parent: ViewGroup, @Duration durationMilliSeconds: Int = 7_000) : ConnectedToWiFiSnackBar {
            val view = parent.inflate(R.layout.snackbar_connected_to_wifi)
            val cb   = ContentViewCallback()
            val snackBar = ConnectedToWiFiSnackBar(parent, view, cb)
            snackBar.duration = durationMilliSeconds

            return snackBar
        }
    }

    fun setNetworkName(networkName: String, showEthernetText: Boolean = true) = apply {
        val connectedText = if (showEthernetText) {
            context.resources.getString(R.string.snackbar_connected_to_network, networkName)
        } else {
            context.resources.getString(R.string.snackbar_connected_to_network_no_ethernet, networkName)
        }
        view.findViewById<ScleraTextView>(R.id.description_text).text = connectedText
    }

    fun setAction(withText: String, listener : () -> Unit) = apply {
        setActionInternal(withText, listener)
    }

    fun setAction(listener : () -> Unit) = apply {
        setActionInternal(listener = listener)
    }

    private fun setActionInternal(text: String? = null, listener: () -> Unit) {
        val button = view.findViewById<Button>(R.id.action_text)
        button.visibility = View.VISIBLE
        button.text = text
        button.setOnClickListener {
            listener()
            dismiss()
        }
        button.typeface = FontUtils.getNormal()
    }

    private class ContentViewCallback : BaseTransientBottomBar.ContentViewCallback {
        override fun animateContentIn(delay: Int, duration: Int) {
            // No-Op
        }

        override fun animateContentOut(delay: Int, duration: Int) {
            // No-Op
        }
    }
}
