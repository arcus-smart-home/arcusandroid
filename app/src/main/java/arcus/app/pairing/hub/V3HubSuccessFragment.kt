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
package arcus.app.pairing.hub

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

import arcus.app.R
import arcus.app.activities.DashboardActivity
import arcus.app.activities.GenericConnectedFragmentActivity
import arcus.app.common.image.IntentRequestCode
import arcus.app.common.utils.inflate
import arcus.app.common.view.ScleraLinkView
import arcus.app.hub.wifi.scan.HubWiFiScanFragment
import arcus.app.pairing.device.productcatalog.ProductCatalogActivity
import arcus.app.pairing.hub.activation.KitActivationGridFragment
import arcus.presentation.pairing.hub.paired.ConnectionType
import arcus.presentation.pairing.hub.paired.HubConnectionInfo
import arcus.presentation.pairing.hub.paired.HubPairedSuccessPresenter
import arcus.presentation.pairing.hub.paired.HubPairedSuccessView
import arcus.presentation.pairing.hub.paired.HubPairedSuccessPresenterImpl
import kotlin.properties.Delegates

class V3HubSuccessFragment : Fragment(), HubPairedSuccessView {
    private var hasKitItems : Boolean = false
    private val hubPairedSuccessPresenter : HubPairedSuccessPresenter = HubPairedSuccessPresenterImpl()
    private lateinit var titleText: TextView
    private lateinit var subTitleText: TextView
    private lateinit var successImage: ImageView
    private lateinit var checkMarkImage: ImageView

    private lateinit var nextButton: Button
    private lateinit var connectToWiFiButton: Button
    private lateinit var goToDashboardLink: ScleraLinkView
    private var newWiFiNetworkName: String? = null
    override var hubConnectionInfo by Delegates.observable(HubConnectionInfo.EMPTY) { _, _, newValue ->
        if (isAdded && !isDetached) {
            when (newValue.connectionType) {
                ConnectionType.UNKNOWN,
                ConnectionType.CELLULAR,
                ConnectionType.WIFI -> {
                    titleText.text = getString(R.string.hub_on_wifi_title, hubPairedSuccessPresenter.getHubWiFiNetworkName())
                    connectToWiFiButton.visibility = View.GONE
                    successImage.setImageResource(R.drawable.wifi_90x90)
                    checkMarkImage.visibility = View.VISIBLE

                    if (hasKitItems) {
                        onWiFiWithKitItems()
                    } else {
                        onWiFiWithoutKitItems()
                    }
                }

                ConnectionType.ETHERNET -> {
                    successImage.setImageResource(R.drawable.check_success_90x90)
                    checkMarkImage.visibility = View.GONE

                    titleText.text = getString(R.string.hub_horray_on_ethernet)

                    connectToWiFiButton.text = getString(R.string.connect_hub_wifi)
                    connectToWiFiButton.visibility = View.GONE
                    connectToWiFiButton.setOnClickListener { view ->
                        startActivityForResult(
                            GenericConnectedFragmentActivity.getLaunchIntent(
                                view.context,
                                HubWiFiScanFragment::class.java
                            ),
                            IntentRequestCode.HUB_WIFI_PAIRING_REQUEST.requestCode
                        )
                    }

                    val wifiNetwork = newWiFiNetworkName.orEmpty()
                    val justSetupWiFi = wifiNetwork.isNotEmpty()
                    if (justSetupWiFi) {
                        titleText.text = getString(R.string.horray_hub_has_kit_items_just_setup_wifi, wifiNetwork)
                        connectToWiFiButton.visibility = View.GONE
                    }

                    if (hasKitItems) {
                        onEthernetWithKitItems(justSetupWiFi)
                    } else {
                        onEthernetWithoutKitItems(justSetupWiFi)
                    }
                }
            }
        }
    }

    private fun onWiFiWithKitItems() {
        subTitleText.text = getString(R.string.hub_on_wifi_description_with_kit)

        // Buttons
        nextButton.text = getString(R.string.continue_kit_setup)
        nextButton.setOnClickListener { _ ->
            activity?.run {
                startActivity(GenericConnectedFragmentActivity.getLaunchIntent(this, KitActivationGridFragment::class.java))
                finish()
            }
        }

        goToDashboardLink.visibility   = View.GONE
    }

    private fun onWiFiWithoutKitItems() {
        subTitleText.text = getString(R.string.hub_on_wifi_description_without_kit)

        // Buttons
        nextButton.text = getString(R.string.pair_another_device)
        nextButton.setOnClickListener { _ ->
            activity?.run {
                startActivity(ProductCatalogActivity.createIntentClearTop(this, DashboardActivity.getHomeFragmentIntent(this)))
                finish()
            }
        }

        goToDashboardLink.visibility = View.VISIBLE
    }

    private fun onEthernetWithKitItems(justSetupWiFi: Boolean) {
        if (justSetupWiFi) {
            subTitleText.text = getString(R.string.horray_hub_has_kit_items_just_setup_wifi_subtext_with_kit_items)
        } else {
            subTitleText.text = getString(R.string.hub_horray_on_ethernet_subtext)
        }

        // Buttons
        nextButton.text   = getString(R.string.continue_kit_setup)
        nextButton.setOnClickListener { _ ->
            activity?.run {
                startActivity(GenericConnectedFragmentActivity.getLaunchIntent(this, KitActivationGridFragment::class.java))
                finish()
            }
        }

        goToDashboardLink.visibility = View.GONE
    }

    private fun onEthernetWithoutKitItems(justSetupWiFi: Boolean) {
        if (justSetupWiFi) {
            subTitleText.text = getString(R.string.horray_hub_has_kit_items_just_setup_wifi_subtext_without_kit_items)
        } else {
            subTitleText.text = getString(R.string.hub_horray_on_ethernet_subtext)
        }

        // Buttons
        nextButton.text   = getString(R.string.pair_another_device)
        nextButton.setOnClickListener { _ ->
            activity?.run {
                startActivity(ProductCatalogActivity.createIntentClearTop(this, DashboardActivity.getHomeFragmentIntent(this)))
                finish()
            }
        }

        goToDashboardLink.visibility = View.VISIBLE
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_v3_hub_success_generic)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hasKitItems = arguments?.getBoolean(ARG_HAS_KIT_ITEMS, false) ?: false

        successImage = view.findViewById(R.id.image)
        checkMarkImage = view.findViewById(R.id.checkmark_image)
        titleText = view.findViewById(R.id.title_text)
        subTitleText = view.findViewById(R.id.sub_title_text)

        nextButton = view.findViewById(R.id.next_button)
        connectToWiFiButton = view.findViewById(R.id.connect_to_wifi_button)
        goToDashboardLink = view.findViewById(R.id.go_to_dashboard_link)
        goToDashboardLink.setOnClickListener {
            activity?.run {
                startActivity(DashboardActivity.getHomeFragmentIntent(this))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        activity?.title = getString(R.string.success_text)
        hubPairedSuccessPresenter.setView(this)
        hubPairedSuccessPresenter.getHubKitInfo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hubPairedSuccessPresenter.clearView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val requestCodeMatches = requestCode == IntentRequestCode.HUB_WIFI_PAIRING_REQUEST.requestCode
        val successfulResult = resultCode == Activity.RESULT_OK

        if (requestCodeMatches && successfulResult) {
            newWiFiNetworkName = data?.getStringExtra(Intent.EXTRA_TEXT)
        }
    }

    companion object {
        private const val ARG_HAS_KIT_ITEMS = "ARG_HAS_KIT_ITEMS"

        @JvmStatic
        fun newInstance(hasKitItems: Boolean) = V3HubSuccessFragment().also {
            with (Bundle()) {
                putBoolean(ARG_HAS_KIT_ITEMS, hasKitItems)
                it.arguments = this
            }
        }
    }
}
