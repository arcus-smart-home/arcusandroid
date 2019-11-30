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
package arcus.app.pairing.device.steps.bledevice.selectble

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import arcus.app.pairing.device.steps.bledevice.BleStepsNavigationDelegate
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import arcus.app.common.fragment.TitledFragment
import arcus.app.common.utils.ActivityUtils
import arcus.app.common.view.ScleraLinkView
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.steps.ViewPagerSelectedFragment
import arcus.app.pairing.device.steps.bledevice.BleConnected
import arcus.presentation.ble.BleDevice
import arcus.presentation.ble.BleConnector
import arcus.presentation.pairing.device.steps.bledevice.BleConnectionStatus
import arcus.presentation.pairing.device.steps.bledevice.selectble.AndroidBleDeviceSelectPresenter
import arcus.presentation.pairing.device.steps.bledevice.selectble.BleDeviceSelectView
import java.util.concurrent.TimeUnit
import kotlin.properties.Delegates

class BleDeviceSelectFragment
    : Fragment(), TitledFragment, ViewPagerSelectedFragment, BleConnected,
    BleDeviceSelectView {
    private lateinit var searchingContainer : View
    private lateinit var scanRecyclerView : RecyclerView

    private lateinit var pairingDevice : String

    private var popup : ModalBottomSheet? = null
    private var selectBleAdapter : SelectBleAdapter? = null
    private var connectedDevice: BleDevice? = null

    private lateinit var presenter : AndroidBleDeviceSelectPresenter
    private var stepsDelegate by Delegates.notNull<BleStepsNavigationDelegate>()
    private lateinit var needHelpUri: Uri
    private lateinit var needHelpLink: ScleraLinkView
    private val needHelpVisibiltyToggle = {
        if (isAdded && !isDetached) {
            needHelpLink.visibility = View.VISIBLE
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        stepsDelegate = context as BleStepsNavigationDelegate
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_ble_device_select, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let { bundle ->
            val prefix = bundle.getString(ARG_DEVICE_PREFIX)!!
            presenter = AndroidBleDeviceSelectPresenter.forPrefix(prefix)
            pairingDevice = bundle.getString(ARG_DEVICE_NAME)!!
            needHelpUri = bundle.getParcelable<Uri>(ARG_NEED_HELP_URI) as Uri
        }

        needHelpLink = view.findViewById(R.id.ble_need_help_link)
        needHelpLink.setLinkTextAndTarget(
            getString(R.string.don_t_see_your_ble_device),
            needHelpUri
        )

        searchingContainer = view.findViewById(R.id.searching_container)
        scanRecyclerView = view.findViewById(R.id.scan_results)
        view.findViewById<ScleraTextView>(R.id.title).text = getString(
                R.string.ble_camera_choose_device_to_pair,
                pairingDevice
        )
        scanRecyclerView.layoutManager = LinearLayoutManager(context)
        ViewCompat.setNestedScrollingEnabled(scanRecyclerView, false)

        presenter.setView(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.stopScanning(context)
        presenter.clearView()
    }

    override fun getTitle() : String = pairingDevice

    override fun onPageSelected() {
        if (connectedDevice != null) {
            needHelpLink.visibility = View.INVISIBLE
        }

        presenter.initializeBleInteractionCallback()

        if (presenter.isConnected()) {
            stepsDelegate.enableContinue()
            val currentAdapter = scanRecyclerView.adapter
            if (currentAdapter == null) {
                scanRecyclerView.adapter = selectBleAdapter
            }
        } else {
            unsetConnectedDevice()
            presenter.startScanning(context)
            selectBleAdapter = null
            scanRecyclerView.adapter = selectBleAdapter
        }

        if (needHelpLink.visibility != View.VISIBLE) {
            needHelpLink.removeCallbacks(needHelpVisibiltyToggle)
            needHelpLink.postDelayed(needHelpVisibiltyToggle, NEED_HELP_VISIBILITY_DELAY)
        }
    }

    override fun onNotSelected() {
        presenter.stopScanning(context)
    }

    override fun onPause() {
        super.onPause()
        presenter.stopScanning(context)
    }

    override fun setBleConnector(bleConnector: BleConnector<Context>) {
        presenter.setBleConnector(bleConnector)
    }

    override fun onBleDevicesFound(devices: List<BleDevice>) {
        val nnActivity = activity ?: return

        selectBleAdapter = SelectBleAdapter(devices, nnActivity) { bleDevice ->
            onShouldConnectTo(bleDevice)
        }
        scanRecyclerView.adapter = selectBleAdapter
    }

    override fun onShouldConnectTo(device: BleDevice) {
        if (!device.isConnected) {
            context?.let { nnContext ->
                connectedDevice = device
                presenter.stopScanning(nnContext)
                connectToDevice()
            }
        }
    }

    override fun onUnhandledError() {
        /* No-Op for now */
    }

    override fun onBleStatusChange(status: BleConnectionStatus) {
        popup?.dismiss()

        when (status) {
            BleConnectionStatus.BLE_CONNECTED -> {
                stepsDelegate.enableContinue()
                connectedDevice?.let {
                    (scanRecyclerView.adapter as SelectBleAdapter?)?.showDeviceConnected(it)
                }
            }
            BleConnectionStatus.BLE_DISCONNECTED -> {
                unsetConnectedDevice()
                scanRecyclerView.adapter = null
            }
            BleConnectionStatus.BLE_CONNECT_FAILURE -> {
                showConnectionFailure()
            }
        }
    }

    private fun showConnectionFailure() {
        val cancelPopup = ConnectingFailedPopup.newInstance()
        cancelPopup.cancelButtonClickHandler = {
            unsetConnectedDevice()
            presenter.startScanning(context)
        }
        cancelPopup.needHelpClickHandler = {
            ActivityUtils.launchUrl(needHelpUri)
        }
        cancelPopup.tryAgainButtonClickHandler = {
            connectToDevice()
        }

        popup = cancelPopup.show(fragmentManager)
    }

    private fun connectToDevice() {
        connectedDevice?.let { bleDevice ->
            context?.let { nnContext ->
                presenter.connectToDevice(nnContext, bleDevice)
                popup = ConnectingPopup.newInstance().show(fragmentManager)
            } ?: showConnectionFailure()
        } ?: showConnectionFailure()
    }

    private fun unsetConnectedDevice() {
        connectedDevice = null
        stepsDelegate.disableContinue()
    }

    override fun onSearching() {
        searchingContainer.visibility = View.VISIBLE
    }

    override fun onSearchingStopped() {
        searchingContainer.visibility = View.INVISIBLE
    }

    companion object {
        private const val ARG_DEVICE_PREFIX= "ARG_DEVICE_PREFIX"
        private const val ARG_DEVICE_NAME= "ARG_DEVICE_NAME"
        private const val ARG_NEED_HELP_URI = "ARG_NEED_HELP_URI"
        private val NEED_HELP_VISIBILITY_DELAY = TimeUnit.SECONDS.toMillis(30)

        @JvmStatic
        fun newInstance(
            shortName: String,
            prefix: String,
            needHelpUri: Uri
        ) = BleDeviceSelectFragment().also {
            with (Bundle()) {
                putString(ARG_DEVICE_NAME, shortName)
                putString(ARG_DEVICE_PREFIX, prefix)
                putParcelable(ARG_NEED_HELP_URI, needHelpUri)
                it.arguments = this
            }
        }
    }
}

