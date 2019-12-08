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
package arcus.app.pairing.device.steps.bledevice.connect

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.constraintlayout.widget.Group
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import android.text.InputFilter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import arcus.app.R
import arcus.app.activities.DashboardActivity
import arcus.app.activities.GenericFragmentActivity
import arcus.app.common.fragment.TitledFragment
import arcus.app.common.fragments.ModalBottomSheet
import arcus.app.common.popups.ScleraPopup
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.utils.Errors
import arcus.app.common.utils.GlobalSetting
import arcus.app.common.utils.sealedWhen
import arcus.app.common.utils.inflate
import arcus.app.common.wifi.BleWiFiSecuritySelectionPopup
import arcus.app.pairing.device.searching.DeviceSearchingActivity
import arcus.app.pairing.device.steps.ViewPagerSelectedFragment
import arcus.app.pairing.device.steps.bledevice.BleConnected
import arcus.app.pairing.device.steps.bledevice.BleDisconnectedPopup
import arcus.app.pairing.device.steps.bledevice.BleStepsNavigationDelegate
import arcus.app.pairing.hub.HubProgressFragment
import arcus.app.pairing.hub.popups.Generic1ButtonErrorPopup
import arcus.presentation.ble.BleConnector
import arcus.presentation.ble.BleWiFiNetwork
import arcus.presentation.common.wifi.DeviceWiFiNetworkSecurity
import arcus.presentation.pairing.MULTI_SPACES_FROM_START
import arcus.presentation.pairing.device.steps.BleGenericPairingStep
import arcus.presentation.pairing.device.steps.PairingStepsPresenterImpl.Companion.HUB_NAME_PREFIX_FILTER
import arcus.presentation.pairing.device.steps.bledevice.BleConnectionStatus
import arcus.presentation.pairing.device.steps.bledevice.IpcdAlreadyAdded
import arcus.presentation.pairing.device.steps.bledevice.IpcdAlreadyClaimed
import arcus.presentation.pairing.device.steps.bledevice.IpcdConnected
import arcus.presentation.pairing.device.steps.bledevice.IpcdConnectionStatus
import arcus.presentation.pairing.device.steps.bledevice.IpcdNotFound
import arcus.presentation.pairing.device.steps.bledevice.IpcdTimedOut
import arcus.presentation.pairing.device.steps.bledevice.WiFiConnectionStatus
import arcus.presentation.pairing.device.steps.bledevice.bleconnect.AndroidBleConnectPresenter
import arcus.presentation.pairing.device.steps.bledevice.bleconnect.BleConnectView
import com.google.android.material.textfield.TextInputLayout
import kotlin.properties.Delegates

class BleConnectFragment : Fragment(), TitledFragment, BleConnected, BleConnectView,
    ViewPagerSelectedFragment {
    private enum class SelectionType {
        UNSECURED,
        SECURED,
        CUSTOM
    }

    private lateinit var namedNetworkContainer : View
    private lateinit var networkNameEntry : EditText
    private lateinit var networkNameEntryContainer : TextInputLayout
    private lateinit var networkPassEntry : EditText
    private lateinit var networkPassEntryContainer : TextInputLayout
    private lateinit var securitySelectionGroup: Group
    private lateinit var securitySelectionRow: TextView

    private lateinit var unsecuredNetworkContainer : View
    private lateinit var unsecuredNetworkTitle : TextView
    private var disabledColor : Int = -1
    private var enabledColor : Int = -1
    private var popupShowing : ModalBottomSheet? = null

    private var selectionType : SelectionType = SelectionType.CUSTOM
    private var bleStepsNavigationDelegate by Delegates.notNull<BleStepsNavigationDelegate>()
    private val presenter = AndroidBleConnectPresenter()
    private var pairingStep : BleGenericPairingStep? = null
    private var factoryResetAkaLink : Uri = Uri.EMPTY

    private lateinit var prefix : String
    private val isForReconnect by lazy {
        arguments?.getBoolean(ARG_IS_FOR_RECONNECT) ?: false
    }

    var selectedNetwork: BleWiFiNetwork? = null
        set(value) {
            field = value
            value ?: return

            when {
                value.isOtherNetwork -> customNetworkSelected()
                value.isSecure() -> securedNetworkSelected(value.name)
                else-> unsecuredNetworkSelected(value.name)
            }
        }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        bleStepsNavigationDelegate = context as BleStepsNavigationDelegate
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { args->
            prefix = args.getString(ARG_DEVICE_PREFIX)!!
            pairingStep = args.getParcelable(ARG_INPUT_STEP)
            factoryResetAkaLink = args.getParcelable(ARG_AKA_LINK)!!
        }
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? = container?.inflate(R.layout.fragment_ble_connect)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disabledColor = ContextCompat.getColor(view.context, R.color.sclera_tab_unselected_grey)
        enabledColor = ContextCompat.getColor(view.context, R.color.sclera_text_color_dark)

        securitySelectionGroup = view.findViewById(R.id.security_selection_group)
        securitySelectionRow = view.findViewById(R.id.security_selection_choice)
        securitySelectionRow.text = DeviceWiFiNetworkSecurity.WPA2_PSK.friendlyName
        securitySelectionRow.setOnClickListener {
            popupShowing?.dismiss()
            popupShowing = BleWiFiSecuritySelectionPopup
                .newInstance(getSecuritySelectionFromRow())
                .setTopButtonCallback { selected ->
                    securitySelectionRow.text = selected.friendlyName
                    if (selected == DeviceWiFiNetworkSecurity.NONE) {
                        networkPassEntry.text = null
                        networkPassEntry.visibility = View.GONE
                    } else {
                        networkPassEntry.visibility = View.VISIBLE
                    }
                }
                .show(fragmentManager)
        }

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

    private fun getSecuritySelectionFromRow() = if (selectedNetwork?.isOtherNetwork == true) {
        DeviceWiFiNetworkSecurity.fromStringRepresentation(securitySelectionRow.text.toString()).blePlatformName
    } else {
        selectedNetwork?.security ?: DeviceWiFiNetworkSecurity.NONE.blePlatformName
    }

    override fun onResume() {
        super.onResume()
        presenter.setView(this)
        presenter.setDevicePrefix(prefix)
    }

    override fun onPause() {
        super.onPause()

        if (prefix != HUB_NAME_PREFIX_FILTER) {
            presenter.cancelIPCDRegistration()
            popupShowing?.dismiss()
        }
        presenter.clearView()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.cancelHubRegistration()
    }

    override fun setBleConnector(bleConnector: BleConnector<Context>) {
        presenter.setBleConnector(bleConnector)
    }

    private fun customNetworkSelected() {
        selectionType = SelectionType.CUSTOM
        updateFields("", selectionType)
        securitySelectionGroup.visibility = View.VISIBLE
    }

    private fun securedNetworkSelected(name: String) {
        selectionType = SelectionType.SECURED
        updateFields(name, selectionType)
        securitySelectionGroup.visibility = View.GONE
    }

    private fun unsecuredNetworkSelected(name: String) {
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

    fun connectBleCamera() {
        if (fieldsAreValid()) {
            selectedNetwork?.let {
                val networkName = when {
                    it.isOtherNetwork -> networkNameEntry.text.toString()
                    else -> it.name
                }
                val securityChoice = when {
                    it.isOtherNetwork -> DeviceWiFiNetworkSecurity
                        .fromStringRepresentation(securitySelectionRow.text.toString())
                        .blePlatformName
                    else -> it.security
                }
                popupShowing = if (isForReconnect) {
                    ConnectingPopup.newInstance(
                        getString(R.string.connecting),
                        getString(R.string.setting_up_wifi_may_take_few_minutes)
                    ).show(fragmentManager)
                } else if(prefix == HUB_NAME_PREFIX_FILTER){
                    ConnectingPopup.newInstance(
                            getString(R.string.connecting),
                            getString(R.string.setting_up_wifi)
                    ).show(fragmentManager)
                } else {
                    ConnectingPopup.newInstance(
                            getString(R.string.connecting),
                            getString(R.string.wifi_smart_switch_patient)
                    ).show(fragmentManager)
                }

                presenter.updateWiFiCredentials(
                        networkPassEntry.text?.toString() ?: "",
                        networkName,
                        securityChoice,
                        isForReconnect
                )
            }
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

    override fun onWiFiStatusChange(status: WiFiConnectionStatus) {
        when (status) {
            WiFiConnectionStatus.WIFI_CONNECTED -> {
                if(prefix == HUB_NAME_PREFIX_FILTER){
                    val popup = popupShowing as ConnectingPopup
                    popup.updateText(getString(R.string.connecting_to_arcus_platform))
                    presenter.registerHub()
                } else {
                    pairingStep?.inputs?.let { presenter.registerIPCD(it) }
                }
            }

            WiFiConnectionStatus.WIFI_ERROR_IN_WRITING -> {
                popupShowing?.dismiss()
                val popup = FailedToWriteWiFiCredsPopup.newInstance(
                    getString(R.string.ble_failed_write_wifi_error_title),
                    getString(R.string.ble_failed_write_wifi_error, pairingStep?.productShortName ?: "Device"),
                    getString(R.string.factory_reset_steps),
                    factoryResetAkaLink
                )
                popup.tryAgainAction = {
                    connectBleCamera()
                }

                popupShowing = popup.show(fragmentManager)
            }

            WiFiConnectionStatus.WIFI_FAILED_TO_CONNECT -> {
                popupShowing?.dismiss()
                popupShowing = InvalidCredentialsErrorPopup().show(fragmentManager)
            }
        }
    }

    override fun onIpcdStatusChange(status: IpcdConnectionStatus) {
        popupShowing?.dismiss()

        when (status) {
            IpcdConnected -> {
                activity?.let {
                    it.finish()
                    it.startActivity(
                            DeviceSearchingActivity.createIntent(it, false, true)
                    )
                }
            }

            IpcdNotFound,
            IpcdTimedOut -> {
                val popup = NoDevicesFoundPopup
                    .newInstance(
                        pairingStep?.productShortName ?: "Device",
                        factoryResetAkaLink
                    )

                popupShowing = popup.show(fragmentManager)
            }

            IpcdAlreadyClaimed -> {
                popupShowing = DeviceTakenErrorPopup().show(fragmentManager)
            }

            is IpcdAlreadyAdded -> {
                popupShowing = ScleraPopup
                    .newInstance(
                        R.string.device_reconnected_title,
                        -1,
                        bottomButtonRes = R.string.go_to_dashboard,
                        hideTopButton = true
                    )
                    .overrideDescriptionText(
                        getString(R.string.device_reconnected_desc, status.deviceName)
                    )
                    .setBottomButtonAction {
                        activity?.run {
                            startActivity(DashboardActivity.getHomeFragmentIntent(this))
                            finish()
                        }
                    }
                    .ignoreTouchOnOutside()
                    .show(fragmentManager)
            }
        }.sealedWhen
    }

    override fun onWiFiSSIDNotUpdatedError() {
        activity?.run {
            setResult(Activity.RESULT_FIRST_USER)
            finish()
        }
    }

    override fun onWiFiSSIDNotUpdatedSuccess(updatedTo: String) {
        activity?.run {
            setResult(Activity.RESULT_OK, Intent().also { intent ->
                intent.putExtra(Intent.EXTRA_TEXT, updatedTo)
                intent.putExtra(Intent.EXTRA_RETURN_RESULT /* show ethernet text */, false)
            })
            finish()
        }
    }

    override fun onHubPairEvent(hubId : String) {
        // Go to the hub progress fragment
        val args = Bundle()
        args.putString(HubProgressFragment.ARG_HUB_ID, hubId)

        activity?.let {
            startActivity(
                GenericFragmentActivity.getLaunchIntent(
                    it,
                    HubProgressFragment::class.java,
                    args,
                    allowBackPress = false
                )
            )
            it.overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.fade_out)
            it.finish()
        }
    }

    override fun onHubPairTimeout() {
        // Show factory reset steps
        popupShowing?.dismiss()
        val popup = Generic1ButtonErrorPopup.newInstance(
                getString(R.string.no_shortNames_found, getString(R.string.smart_hub)),
                getString(R.string.ensure_plugged_in_powered),
                getString(R.string.factory_reset_steps),
                buttonText = getString(R.string.close_text)
        )
        popup.isCancelable = false
        popup.setTopLinkListener {
            ActivityUtils.launchUrl(Uri.parse(GlobalSetting.HUB_FACTORY_RESET_STEPS_URL))
        }
        popup.setButtonListener {
            bleStepsNavigationDelegate.rewindToEnterBleModeInstructionsPage()
        }
        popupShowing = popup.show(fragmentManager)
    }

    override fun onHubPairError(error: String, hubId: String) {
        activity?.title = getString(R.string.hub_error)

        when (error){
            Errors.Hub.ALREADY_REGISTERED -> {      // E01
                popupShowing?.dismiss()
                val popup = Generic1ButtonErrorPopup.newInstance(
                        getString(R.string.hub_error_code_e01),
                        getString(R.string.v3_hub_error_e01, hubId),
                        getString(R.string.support_number),
                        buttonText = getString(R.string.close_text)
                )
                popup.isCancelable = false
                popup.setTopLinkListener {
                    ActivityUtils.callSupport()
                }
                popup.setButtonListener {
                    bleStepsNavigationDelegate.rewindToEnterBleModeInstructionsPage()
                }
                popupShowing = popup.show(fragmentManager)
            }
            Errors.Hub.ORPHANED_HUB -> {            // E02
                popupShowing?.dismiss()
                val popup = Generic1ButtonErrorPopup.newInstance(
                        getString(R.string.hub_error_code_e02),
                        getString(R.string.v3_hub_error_e02, hubId),
                        getString(R.string.factory_reset_steps),
                        getString(R.string.support_number),
                        getString(R.string.close_text)
                )
                popup.isCancelable = false
                popup.setTopLinkListener {
                    ActivityUtils.launchUrl(Uri.parse(GlobalSetting.HUB_FACTORY_RESET_STEPS_URL))
                }
                popup.setBottomLinkListener {
                    ActivityUtils.callSupport()
                }
                popup.setButtonListener {
                    bleStepsNavigationDelegate.rewindToEnterBleModeInstructionsPage()
                }
                popupShowing = popup.show(fragmentManager)
            }
            Errors.Process.CANCELLED -> {
                popupShowing?.dismiss()
                // TODO - how do we handle this error case?
            }
            else -> {
                popupShowing?.dismiss()
                // TODO: what do here? For now, recommend factory reset
            }
        }
    }

    override fun onBleStatusChange(status: BleConnectionStatus) {
        if (popupShowing !is Generic1ButtonErrorPopup) {
            popupShowing?.dismiss()

            when (status) {
                BleConnectionStatus.BLE_CONNECTED -> {
                    // No - Op
                }

                BleConnectionStatus.BLE_CONNECT_FAILURE,
                BleConnectionStatus.BLE_DISCONNECTED -> {
                    val popup = BleDisconnectedPopup()
                    popup.okButtonClickAction = {
                        bleStepsNavigationDelegate.rewindToEnterBleModeInstructionsPage()
                    }
                    popup.show(fragmentManager)
                }
            }
        }
    }

    override fun getTitle() : String = if (prefix == HUB_NAME_PREFIX_FILTER) {
        getString(R.string.smart_hub)
    } else {
        when (selectionType) {
            SelectionType.UNSECURED -> getString(R.string.unsecured_network_title)
            SelectionType.SECURED -> getString(R.string.wifi_network_info_title)
            SelectionType.CUSTOM -> getString(R.string.wifi_custom_network_title)
        }
    }

    override fun onPageSelected() {
        presenter.initializeBleInteractionCallback()
    }

    override fun onNotSelected() {
        presenter.cancelIPCDRegistration()
    }

    companion object {
        private const val ARG_INPUT_STEP = "ARG_INPUT_STEP"
        private const val ARG_DEVICE_PREFIX= "ARG_DEVICE_PREFIX"
        private const val ARG_AKA_LINK= "ARG_AKA_LINK"
        private const val ARG_IS_FOR_RECONNECT= "ARG_IS_FOR_RECONNECT"

        @JvmStatic
        fun newInstance(
            step: BleGenericPairingStep,
            prefix: String,
            factoryResetAkaUri: Uri,
            isForReconnect: Boolean = false
        ) = BleConnectFragment().also {
            with(Bundle()) {
                putParcelable(ARG_INPUT_STEP, step)
                putString(ARG_DEVICE_PREFIX, prefix)
                putParcelable(ARG_AKA_LINK, factoryResetAkaUri)
                putBoolean(ARG_IS_FOR_RECONNECT, isForReconnect)
                it.arguments = this
            }
        }
    }
}
