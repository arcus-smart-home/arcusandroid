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
package arcus.app.hub.wifi.connect

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import arcus.app.R
import arcus.app.common.fragment.FragmentContainerHolder
import arcus.app.common.fragments.ModalBottomSheet
import arcus.app.common.popups.ScleraPopup
import arcus.app.common.utils.enableViews
import arcus.app.common.utils.inflate
import arcus.app.common.wifi.WiFiSecuritySelectionPopup
import arcus.presentation.common.wifi.DeviceWiFiNetworkSecurity
import arcus.presentation.hub.wifi.connect.HubWiFiConnectPresenter
import arcus.presentation.hub.wifi.connect.HubWiFiConnectPresenterImpl
import arcus.presentation.hub.wifi.connect.HubWiFiConnectState
import arcus.presentation.hub.wifi.connect.HubWiFiConnectView
import arcus.presentation.hub.wifi.connect.HubWiFiCredentials
import arcus.presentation.hub.wifi.scan.HubAvailableWiFiNetwork
import arcus.presentation.pairing.MULTI_SPACES_FROM_START
import com.google.android.material.textfield.TextInputLayout
import kotlin.properties.Delegates

class HubWiFiConnectFragment : Fragment(), HubWiFiConnectView {
    private lateinit var namedNetworkContainer: View
    private lateinit var networkNameEntry: EditText
    private lateinit var networkNameEntryContainer: TextInputLayout
    private lateinit var networkPassEntry: EditText
    private lateinit var networkPassEntryContainer: TextInputLayout
    private lateinit var unsecuredNetworkContainer: View
    private lateinit var unsecuredNetworkTitle: TextView
    private lateinit var securitySelectionGroup: Group
    private lateinit var securitySelectionRow: TextView
    private lateinit var connectWiFiButton: Button
    private lateinit var unsecuredConnectWiFiButton: Button
    private lateinit var alertBanner: View
    private val enableDisableFields = mutableListOf<View>()

    private var disabledColor : Int = -1
    private var enabledColor : Int = -1
    private var popupShowing : ModalBottomSheet? = null

    private lateinit var hubAvailableWiFiNetwork: HubAvailableWiFiNetwork
    private var fragmentContainerHolder: FragmentContainerHolder? = null
    private val hubWiFiConnectPresenter: HubWiFiConnectPresenter = HubWiFiConnectPresenterImpl()

    override var hubWiFiConnectState by Delegates.observable(HubWiFiConnectState.INITIAL) { _, _, newValue ->
        when (newValue) {
            HubWiFiConnectState.CONNECTING -> {
                enableDisableFields.enableViews(false)

                popupShowing?.dismiss()
                popupShowing = ConnectingPopup.newInstance().show(fragmentManager)
            }
            HubWiFiConnectState.CONNECTION_FAILED -> {
                // Enable entry fields (only name if it's an other network though)
                networkNameEntry.isEnabled = hubAvailableWiFiNetwork.isOtherNetwork
                networkPassEntry.isEnabled = true

                // Make sure user can click submit again
                unsecuredConnectWiFiButton.isEnabled = true
                connectWiFiButton.isEnabled = true

                popupShowing?.dismiss()
                popupShowing = ScleraPopup
                    .newInstance(
                        R.string.wifi_connection_error_text,
                        R.string.hub_wifi_connect_after_paired_failed_to_connect,
                        R.string.try_again,
                        R.string.stay_on_ethernet,
                        true
                    )
                    .ignoreTouchOnOutside()
                    .setTopButtonAction {
                        // Dismiss to let them double check info and try again
                        popupShowing?.dismiss()
                    }
                    .setBottomButtonAction {
                        popupShowing?.dismiss()
                        activity?.finish()
                    }
                    .show(fragmentManager)
            }
            HubWiFiConnectState.CONNECTED -> {
                activity?.run {
                    setResult(Activity.RESULT_OK, Intent().also { intent ->
                        intent.putExtra(Intent.EXTRA_TEXT, networkNameEntry.text.toString())
                    })
                    finish()
                }
            }
            HubWiFiConnectState.MISSING_OR_INVALID_SSID -> {
                networkNameEntryContainer.error = getString(R.string.wifi_ssid_missing_error)
            }
            HubWiFiConnectState.MISSING_OR_INVALID_PASS -> {
                networkPassEntryContainer.error = getString(R.string.wifi_password_missing_error)
            }
            HubWiFiConnectState.MISSING_OR_INVALID_SSID_AND_PASS -> {
                networkNameEntryContainer.error = getString(R.string.wifi_ssid_missing_error)
                networkPassEntryContainer.error = getString(R.string.wifi_password_missing_error)
            }

            HubWiFiConnectState.INITIAL -> {
                popupShowing?.dismiss()
                alertBanner.visibility = View.GONE
                // Enable all fields again
                enableDisableFields.enableViews(true)
                // But only the name field if it's an "other network"
                networkNameEntry.isEnabled = hubAvailableWiFiNetwork.isOtherNetwork
            }
            HubWiFiConnectState.HUB_OFFLINE -> {
                popupShowing?.dismiss()
                alertBanner.visibility = View.VISIBLE
                enableDisableFields.enableViews(false)
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragmentContainerHolder = context as? FragmentContainerHolder?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args->
            hubAvailableWiFiNetwork = args.getParcelable(ARG_HUB_WIFI_NETWORK)!!
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_hub_wifi_connect)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        namedNetworkContainer = view.findViewById(R.id.named_network_container)
        networkNameEntry = view.findViewById(R.id.network_name)
        networkNameEntryContainer = view.findViewById(R.id.network_name_container)
        networkPassEntry = view.findViewById(R.id.network_password)
        networkPassEntryContainer = view.findViewById(R.id.network_password_container)
        unsecuredNetworkContainer = view.findViewById(R.id.unsecured_network_container)
        unsecuredNetworkTitle = view.findViewById(R.id.unsecured_network_title)
        securitySelectionGroup = view.findViewById(R.id.security_selection_group)
        securitySelectionRow = view.findViewById(R.id.security_selection_choice)
        connectWiFiButton = view.findViewById(R.id.connect_to_wifi_button)
        unsecuredConnectWiFiButton = view.findViewById(R.id.unsecured_connect_to_wifi_button)
        alertBanner = view.findViewById(R.id.alert_banner)

        enableDisableFields.add(networkNameEntry)
        enableDisableFields.add(networkPassEntry)
        enableDisableFields.add(unsecuredConnectWiFiButton)
        enableDisableFields.add(connectWiFiButton)

        unsecuredConnectWiFiButton.setOnClickListener {
            hubWiFiConnectPresenter.connectToWiFi(
                HubWiFiCredentials(
                    hubAvailableWiFiNetwork.ssid,
                    "",
                    hubAvailableWiFiNetwork.security
                )
            )
        }

        securitySelectionRow.text = DeviceWiFiNetworkSecurity.WPA2_PSK.friendlyName
        securitySelectionRow.setOnClickListener {
            popupShowing?.dismiss()
            popupShowing = WiFiSecuritySelectionPopup
                .newInstance(getSecuritySelectionFromRow().name)
                .setTopButtonCallback { selected ->
                    securitySelectionRow.text = selected.friendlyName
                    if (selected == DeviceWiFiNetworkSecurity.NONE) {
                        networkPassEntry.text = null
                        networkPassEntryContainer.visibility = View.GONE
                    } else {
                        networkPassEntryContainer.visibility = View.VISIBLE
                    }
                }
                .show(fragmentManager)
        }

        connectWiFiButton.setOnClickListener {
            hubWiFiConnectPresenter.connectToWiFi(
                HubWiFiCredentials(
                    networkNameEntry.text.toString(),
                    networkPassEntry.text.toString(),
                    getSecuritySelectionFromRow()
                )
            )
        }

        disabledColor = ContextCompat.getColor(view.context, R.color.sclera_tab_unselected_grey)
        enabledColor = ContextCompat.getColor(view.context, R.color.sclera_text_color_dark)

        networkNameEntry.filters = arrayOf(InputFilter { source, _, _, dest, _, _ ->
            if (dest.isEmpty() && source.matches(MULTI_SPACES_FROM_START)) {
                source.replace(MULTI_SPACES_FROM_START, "")
            } else {
                source
            }
        })

        networkNameEntryContainer.error = null
        networkPassEntry.run {
            error = null
            text.clear()
        }

        if (hubAvailableWiFiNetwork.isOtherNetwork) {
            networkNameEntry.run {
                text.clear()
                isEnabled = true
            }
        } else {
            networkNameEntry.run {
                setText(hubAvailableWiFiNetwork.ssid, TextView.BufferType.EDITABLE)
                isEnabled = false
            }
        }

        when (hubAvailableWiFiNetwork.security) {
            DeviceWiFiNetworkSecurity.NONE -> { // Unsecured network
                unsecuredNetworkTitle.text = getString(R.string.wifi_is_unsecured, hubAvailableWiFiNetwork.ssid)
                namedNetworkContainer.visibility = View.GONE
                unsecuredNetworkContainer.visibility = View.VISIBLE
                securitySelectionGroup.visibility = View.GONE
            }
            DeviceWiFiNetworkSecurity.UNKNOWN -> { // Custom Network
                unsecuredNetworkTitle.text = null
                namedNetworkContainer.visibility = View.VISIBLE
                unsecuredNetworkContainer.visibility = View.GONE
                securitySelectionGroup.visibility = View.VISIBLE
            }
            else -> {
                unsecuredNetworkTitle.text = null
                namedNetworkContainer.visibility = View.VISIBLE
                unsecuredNetworkContainer.visibility = View.GONE
                securitySelectionGroup.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hubWiFiConnectPresenter.setView(this)
        fragmentContainerHolder?.showBackButtonOnToolbar(true)
        val title = when {
            hubAvailableWiFiNetwork.isSecure() -> getString(R.string.wifi_network_info_title)
            hubAvailableWiFiNetwork.isOtherNetwork -> getString(R.string.wifi_custom_network_title)
            else -> getString(R.string.unsecured_network_title)
        }
        fragmentContainerHolder?.setTitle(title)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        hubWiFiConnectPresenter.clearView()
    }

    private fun getSecuritySelectionFromRow() : DeviceWiFiNetworkSecurity = if (hubAvailableWiFiNetwork.isOtherNetwork) {
        DeviceWiFiNetworkSecurity.fromStringRepresentation(securitySelectionRow.text.toString())
    } else {
        hubAvailableWiFiNetwork.security
    }

    companion object {
        private const val ARG_HUB_WIFI_NETWORK = "ARG_HUB_WIFI_NETWORK"

        @JvmStatic
        fun newInstance(
            hubAvailableWiFiNetwork: HubAvailableWiFiNetwork
        ) = HubWiFiConnectFragment().also {
            with(Bundle()) {
                putParcelable(ARG_HUB_WIFI_NETWORK, hubAvailableWiFiNetwork)
                it.arguments = this
            }
        }
    }
}
