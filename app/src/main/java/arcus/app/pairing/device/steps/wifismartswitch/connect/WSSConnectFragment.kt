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
package arcus.app.pairing.device.steps.wifismartswitch.connect

import android.content.Context
import android.net.wifi.ScanResult
import android.net.wifi.WifiInfo
import android.os.Bundle
import androidx.core.content.ContextCompat
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import arcus.app.common.fragment.TitledFragment
import arcus.app.pairing.device.searching.DeviceSearchingActivity
import arcus.presentation.pairing.device.steps.WiFiSmartSwitchPairingStep
import arcus.app.pairing.device.steps.wifismartswitch.WiFiNetworkBaseFragment
import arcus.app.pairing.device.steps.wifismartswitch.WSSStepsNavigationDelegate
import arcus.presentation.pairing.device.steps.wifismartswitch.connect.WSSConnectPresenter
import arcus.presentation.pairing.device.steps.wifismartswitch.connect.WSSConnectView
import arcus.presentation.pairing.device.steps.wifismartswitch.connect.WiFiConnectInformation
import com.google.android.material.textfield.TextInputLayout
import kotlin.properties.Delegates

class WSSConnectFragment : WiFiNetworkBaseFragment(),
    TitledFragment,
    WSSConnectView {
    private enum class SelectionType {
        UNSECURED,
        SECURED,
        CUSTOM
    }

    override var continueSearchingInLoop = false
    private lateinit var namedNetworkContainer : View
    private lateinit var networkNameEntry : EditText
    private lateinit var networkNameEntryContainer : TextInputLayout
    private lateinit var networkPassEntry : EditText
    private lateinit var networkPassEntryContainer : TextInputLayout

    private lateinit var unsecuredNetworkContainer : View
    private lateinit var unsecuredNetworkTitle : TextView
    private var disabledColor : Int = -1
    private var enabledColor : Int = -1
    private var popupShowing : ModalBottomSheet? = null

    private var selectionType : SelectionType = SelectionType.CUSTOM
    private var wssStepsNavigationDelegate by Delegates.notNull<WSSStepsNavigationDelegate>()

    private val connectionInformation : WiFiConnectInformation
        get() {
            val inputs = arguments
                ?.getParcelable<WiFiSmartSwitchPairingStep>(ARG_INPUT_STEP)
                ?.inputs
                    ?: emptyList()
            val isSecure = selectionType == SelectionType.SECURED

            return WiFiConnectInformation(
                networkNameEntry.text.toString(),
                networkPassEntry.text.toString(),
                isSecure,
                inputs
            )
        }
    private val presenter : WSSConnectPresenter<ScanResult> = AndroidWSSConnectPresenterImpl()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        wssStepsNavigationDelegate = context as WSSStepsNavigationDelegate
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_wss_connect, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disabledColor = ContextCompat.getColor(view.context, R.color.sclera_tab_unselected_grey)
        enabledColor = ContextCompat.getColor(view.context, R.color.sclera_text_color_dark)

        namedNetworkContainer = view.findViewById(R.id.named_network_container)
        networkNameEntry = view.findViewById(R.id.network_name)
        networkNameEntryContainer = view.findViewById(R.id.network_name_container)
        networkNameEntry.filters = arrayOf(InputFilter { source, _, _, dest, _, _ ->
            if (dest?.length == 0 && source?.matches(MULTI_SPACES_FROM_START) == true) {
                source.replace(MULTI_SPACES_FROM_START, "")
            } else {
                source
            }
        })
        networkPassEntry = view.findViewById(R.id.network_password)
        networkPassEntryContainer = view.findViewById(R.id.network_password_container)

        unsecuredNetworkContainer = view.findViewById(R.id.unsecured_network_container)
        unsecuredNetworkTitle = view.findViewById(R.id.unsecured_network_title)
    }

    override fun onResume() {
        super.onResume()
        presenter.setView(this)
    }

    override fun onPause() {
        super.onPause()
        presenter.clearView()
    }

    fun customNetworkSelected() {
        selectionType = SelectionType.CUSTOM
        updateFields("", selectionType)
    }

    fun securedNetworkSelected(name: String) {
        selectionType = SelectionType.SECURED
        updateFields(name, selectionType)
    }

    fun unsecuredNetworkSelected(name: String) {
        selectionType = SelectionType.UNSECURED
        updateFields(name, selectionType)
    }

    private fun updateFields(name: String, type: SelectionType) {
        networkNameEntryContainer.error = null
        networkPassEntryContainer.error = null

        if (name.isEmpty()) {
            networkNameEntry.text.clear()
            networkNameEntry.isEnabled = true
        } else {
            networkNameEntry.setText(name, TextView.BufferType.EDITABLE)
            networkNameEntry.isEnabled = false
        }
        networkPassEntry.text.clear()

        when (type) {
            SelectionType.UNSECURED -> {
                unsecuredNetworkTitle.text = getString(R.string.wifi_is_unsecured, name)
                namedNetworkContainer.visibility = View.GONE
                unsecuredNetworkContainer.visibility = View.VISIBLE
            }
            else -> {
                unsecuredNetworkTitle.text = null
                namedNetworkContainer.visibility = View.VISIBLE
                unsecuredNetworkContainer.visibility = View.GONE
            }
        }
    }

    fun connectSmartPlug() {
        if (fieldsAreValid()) {
            startSearchingForNetworksWithPopup()
        }
    }

    private fun fieldsAreValid() : Boolean {
        return when (selectionType) {
            SelectionType.CUSTOM -> {
                if (networkNameEntry.text.isNullOrEmpty()) {
                    networkNameEntryContainer.error = getString(R.string.wifi_ssid_missing_error)
                    false
                } else {
                    true
                }
            }
            SelectionType.SECURED -> {
                if (networkPassEntry.text.isNullOrEmpty()) {
                    networkPassEntryContainer.error = getString(R.string.wifi_password_missing_error)
                    false
                } else {
                    true
                }
            }
            SelectionType.UNSECURED -> true // No validation needs to be done.
        }
    }

    override fun searchingForNetworks() { /* No - Op */ }

    override fun onResultsReceived(scanResults: List<ScanResult>, currentNetwork: WifiInfo?) {
        stopNetworkSearch()

        val swannAPName = presenter
            .parseScanResultsForSwannAPs(scanResults)
            .firstOrNull()
        if (swannAPName == null) {
            popupShowing?.dismiss()

            val popup = NoDevicesFoundPopup()
            popup.isCancelable = false
            popup.tryAgainAction = { startSearchingForNetworksWithPopup() }
            popup.show(fragmentManager)
        } else {
            presenter.startPairing(swannAPName, connectionInformation)
        }
    }

    private fun startSearchingForNetworksWithPopup() {
        val popup = ConnectingPopup()
        popup.isCancelable = false
        popupShowing = popup
        popup.show(fragmentManager)

        startNetworkSearch()
    }

    override fun onDeviceTakenError() {
        popupShowing?.dismiss()

        val popup = DeviceTakenWSSErrorPopup()
        popupShowing = popup
        popup.show(fragmentManager)
    }

    override fun onResetDeviceError() {
        popupShowing?.dismiss()

        val popup = ResetWSSErrorPopup()
        popupShowing = popup
        popup.show(fragmentManager)
    }

    override fun onInvalidCredentialsError() {
        popupShowing?.dismiss()

        val popup = InvalidCredentialsErrorPopup()
        popupShowing = popup
        popup.show(fragmentManager)
    }

    override fun onNoApsFoundError() {
        popupShowing?.dismiss()
    }

    override fun onSuccess() {
        context?.let {
            it.startActivity(
                DeviceSearchingActivity.createIntent(
                context = it,
                startSearching = false,
                disableBackPress = true
            ))
        }
    }

    override fun getTitle() : String = when (selectionType) {
        SelectionType.UNSECURED -> getString(R.string.unsecured_network_title)
        SelectionType.SECURED -> getString(R.string.wifi_network_info_title)
        SelectionType.CUSTOM -> getString(R.string.wifi_custom_network_title)
    }

    companion object {
        private const val ARG_INPUT_STEP = "ARG_INPUT_STEP"

        @JvmStatic
        fun newInstance(step: WiFiSmartSwitchPairingStep) = WSSConnectFragment().also {
            it.arguments = Bundle().also { it.putParcelable(ARG_INPUT_STEP, step) }
        }

        @JvmField
        val MULTI_SPACES_FROM_START = "^\\s+".toRegex()
    }
}
