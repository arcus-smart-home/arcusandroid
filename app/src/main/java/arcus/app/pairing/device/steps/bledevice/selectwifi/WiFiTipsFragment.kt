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
package arcus.app.pairing.device.steps.bledevice.selectwifi

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.R
import arcus.app.common.utils.ActivityUtils
import android.widget.Button


class WiFiTipsFragment : Fragment() {
    private lateinit var wifiNotFoundUri: Uri

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wifi_tips, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<Button>(R.id.ok_button).setOnClickListener {
            activity?.finish()
        }

        wifiNotFoundUri = arguments?.getParcelable(ARG_WIFI_NOT_FOUND_URI) ?: return
        view.findViewById<View>(R.id.step_2).setOnClickListener {
            ActivityUtils.launchUrl(wifiNotFoundUri)
            activity?.finish()
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.wifi_network_tips)
    }

    companion object {
        private const val ARG_WIFI_NOT_FOUND_URI = "ARG_WIFI_NOT_FOUND_URI"

        @JvmStatic
        fun createBundle(wifiNotFoundUri: Uri) = Bundle().also {
            it.putParcelable(ARG_WIFI_NOT_FOUND_URI, wifiNotFoundUri)
        }
    }
}
