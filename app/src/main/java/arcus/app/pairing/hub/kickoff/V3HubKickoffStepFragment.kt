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
package arcus.app.pairing.hub.kickoff

import android.net.Uri
import android.os.Bundle
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import arcus.app.R
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.view.ScleraLinkView
import arcus.app.common.view.ScleraTextView

class V3HubKickoffStepFragment : Fragment() {
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_v3_hub_kickoff_step, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.run {
            view
                    .findViewById<ImageView>(R.id.pairing_step_image)
                    .setImageResource(getInt(ARG_IMAGE_RESOURCE))
            view
                    .findViewById<TextView>(R.id.pairing_step_text)
                    .text = getString(getInt(ARG_STEP_TEXT))

            val header: ScleraTextView = view.findViewById(R.id.pairing_step_header)
            header.text = if (getInt(ARG_HEADER_TEXT) != 0) getString(getInt(ARG_HEADER_TEXT)) else ""
            header.visibility = if (getInt(ARG_HEADER_TEXT) != 0) View.VISIBLE else View.GONE


            val link: ScleraLinkView = view.findViewById(R.id.pairing_step_link)
            link.text = if (getInt(ARG_LINK_TEXT) != 0) getString(getInt(ARG_LINK_TEXT)) else ""
            link.visibility = if (getInt(ARG_LINK_TEXT) != 0) View.VISIBLE else View.GONE
            link.setOnClickListener {
                ActivityUtils.launchUrl(Uri.parse(getString(ARG_LINK_URI)))
            }
        }
    }

    companion object {
        private const val ARG_IMAGE_RESOURCE = "ARG_IMAGE_RESOURCE"
        private const val ARG_HEADER_TEXT = "ARG_HEADER_TEXT"
        private const val ARG_STEP_TEXT = "ARG_STEP_TEXT"
        private const val ARG_LINK_TEXT = "ARG_LINK_TEXT"
        private const val ARG_LINK_URI = "ARG_LINK_URI"

        @JvmStatic
        fun newInstance(
                @DrawableRes imageResource: Int,
                @StringRes stepTextRes: Int,
                @StringRes headerTextRes: Int = 0,
                @StringRes linkTextRes: Int = 0,
                @StringRes linkUri: String = ""
        ) = V3HubKickoffStepFragment().also {
            with (Bundle()) {
                putInt(ARG_IMAGE_RESOURCE, imageResource)
                putInt(ARG_STEP_TEXT, stepTextRes)
                putInt(ARG_HEADER_TEXT, headerTextRes)
                putInt(ARG_LINK_TEXT, linkTextRes)
                putString(ARG_LINK_URI, linkUri)
                it.arguments = this
            }
        }
    }
}
