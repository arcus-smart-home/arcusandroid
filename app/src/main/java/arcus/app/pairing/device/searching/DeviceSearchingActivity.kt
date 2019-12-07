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
package arcus.app.pairing.device.searching

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.view.ViewCompat
import androidx.appcompat.widget.AppCompatImageView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.View
import arcus.app.R
import arcus.app.activities.ConnectedActivity
import arcus.app.activities.DashboardActivity
import arcus.app.common.fragments.ModalBottomSheet
import android.widget.Button
import arcus.app.common.view.ScleraTextView
import arcus.app.pairing.device.customization.PairingCustomizationActivity
import arcus.app.pairing.device.post.zwaveheal.ZWaveRebuildActivity
import arcus.app.pairing.device.productcatalog.ProductCatalogActivity
import arcus.app.pairing.device.remove.RemoveDeviceActivity
import arcus.app.pairing.device.searching.timeout.ConfirmExitPairingPopupNoDevices
import arcus.app.pairing.device.searching.timeout.ConfirmExitPairingPopupWithDevices
import arcus.app.pairing.device.searching.timeout.NoDevicesSearchingTimeout
import arcus.app.pairing.device.searching.timeout.WithDevicesSearchingTimeout
import arcus.presentation.pairing.device.searching.DevicePairingData
import arcus.presentation.pairing.device.searching.DeviceSearchingView
import arcus.presentation.pairing.device.searching.DeviceSearchingPresenter
import arcus.presentation.pairing.device.searching.DeviceSearchingPresenterImpl
import arcus.presentation.pairing.device.searching.HelpStep
import org.slf4j.LoggerFactory
import kotlin.properties.Delegates

/**
 * States:
 * Searching, No Devices
 * Searching, No Devices, Troubleshooting Tips
 * Searching, With Devices
 * Not Searching, No Devices
 * Not Searching, With Devices
 */
class DeviceSearchingActivity
    : ConnectedActivity(),
        DeviceSearchingView,
        PairedDeviceClickHandler {
    private var pairingInputs by Delegates.notNull<HashMap<String, String>>()
    private var popupShowing : ModalBottomSheet? = null
    private var startSearching = START_SEARCHING_DEFAULT
    private var disableBackPress = DISABLE_BACK_PRESS_DEFAULT
    private val presenter : DeviceSearchingPresenter = DeviceSearchingPresenterImpl()

    private lateinit var imageContainer : View
    private lateinit var noDevicesImage : AppCompatImageView
    private lateinit var searchingAnimation : PairingSearchAnimationView
    private lateinit var title : ScleraTextView
    private lateinit var description : ScleraTextView
    private lateinit var centerContentContainer : View // Houses the Troubleshooting / Paired Devices list
    private lateinit var deviceSearchingTroubleshootingTitle : ScleraTextView
    private lateinit var recyclerView : RecyclerView
    private lateinit var pairAnotherDeviceButton : Button
    private lateinit var goToDashboardButton : Button

    private val restartSearchingCallback = {
        presenter.restartSearching(pairingInputs)
    }
    private val goToDashboardListener = {
        presenter.dismissAll()
    }

    private val backButtonEnabled : (Boolean) -> Unit = {
        supportActionBar?.setDisplayHomeAsUpEnabled(it)
        disableBackPress = !it
        startSearching = it
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_searching)

        @Suppress("UNCHECKED_CAST")
        pairingInputs = when {
            savedInstanceState?.containsKey(ARG_USER_INPUT_STEPS) == true -> {
                savedInstanceState.getSerializable(ARG_USER_INPUT_STEPS) as HashMap<String, String>
            }

            intent.hasExtra(ARG_USER_INPUT_STEPS) -> {
                intent.getSerializableExtra(ARG_USER_INPUT_STEPS) as HashMap<String, String>
            }

            else -> HashMap()
        }

        startSearching = when {
            savedInstanceState?.containsKey(ARG_START_SEARCHING_BOOL) == true -> {
                savedInstanceState.getBoolean(ARG_START_SEARCHING_BOOL)
            }
            else -> {
                intent.getBooleanExtra(ARG_START_SEARCHING_BOOL, START_SEARCHING_DEFAULT)
            }
        }

        setSupportActionBar(findViewById(R.id.toolbar))
        disableBackPress = intent.getBooleanExtra(ARG_DISABLE_BACK_PRESS_BOOL, DISABLE_BACK_PRESS_DEFAULT)

        imageContainer = findViewById(R.id.image_container)
        noDevicesImage = findViewById(R.id.no_devices_image)
        searchingAnimation = findViewById(R.id.searching_animation)
        title = findViewById(R.id.device_searching_title)
        description = findViewById(R.id.device_searching_description)
        centerContentContainer = findViewById(R.id.center_content_container)
        deviceSearchingTroubleshootingTitle = findViewById(R.id.device_searching_troubleshooting_title)
        pairAnotherDeviceButton = findViewById(R.id.pair_device_button)
        goToDashboardButton = findViewById(R.id.go_to_dashboard_button)

        recyclerView = findViewById(R.id.recycler_view_content)
        recyclerView.layoutManager = LinearLayoutManager(this)
        ViewCompat.setNestedScrollingEnabled(recyclerView, false)

        goToDashboardButton.setOnClickListener { handleDashboardButtonClick() }
        pairAnotherDeviceButton.setOnClickListener {
            val intent = Intent(this, ProductCatalogActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putSerializable(ARG_USER_INPUT_STEPS, pairingInputs)
        outState.putBoolean(ARG_START_SEARCHING_BOOL, startSearching)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)
        pairingInputs = when {
            savedInstanceState?.containsKey(ARG_USER_INPUT_STEPS) == true -> {
                savedInstanceState.getSerializable(ARG_USER_INPUT_STEPS) as HashMap<String, String>
            }
            else -> HashMap()
        }

        startSearching = when {
            savedInstanceState?.containsKey(ARG_START_SEARCHING_BOOL) == true -> {
                savedInstanceState.getBoolean(ARG_START_SEARCHING_BOOL)
            }
            else -> START_SEARCHING_DEFAULT
        }
    }

    override fun onResume() {
        super.onResume()
        disableBackPress = intent.getBooleanExtra(ARG_DISABLE_BACK_PRESS_BOOL, DISABLE_BACK_PRESS_DEFAULT)
        presenter.setView(this)

        if (startSearching) {
            backButtonEnabled(true)
            presenter.startSearching(pairingInputs)
        } else if (presenter.isIdle()) {
            backButtonEnabled(false)
            presenter.updatePairedDeviceList()
        }
    }

    private fun handleDashboardButtonClick() {
        dismissAndClearPopupShowing()
        when {
            presenter.getMispairedOrMisconfigured() -> {
                // show mispaired popup
                val fragment = ConfirmExitPairingPopupWithDevices.newInstance(
                    false,
                    getString(R.string.confirm_exit_pairing_misconfirgured_or_mispaired),
                    getString(R.string.return_to_devices)
                )
                fragment.setGoToDashboardButtonListener(goToDashboardListener)
                popupShowing = fragment
                fragment.show(
                    supportFragmentManager,
                    ConfirmExitPairingPopupWithDevices::class.java.name
                )
            }
            presenter.hasPairedDevices() -> {
                // show customize popup
                val count = presenter.getPairedDevicesCount()
                val countString = resources.getQuantityString(R.plurals.customize_my_device, count, count)
                val fragment = ConfirmExitPairingPopupWithDevices.newInstance(
                    true,
                    getString(R.string.confirm_exit_pairing_default_settings),
                    countString)
                fragment.setGoToDashboardButtonListener(goToDashboardListener)
                popupShowing = fragment
                fragment.show(supportFragmentManager, ConfirmExitPairingPopupWithDevices::class.java.name)
            }
            presenter.allDevicesConfigured() -> goToDashboardListener()
            else -> {
                // Otherwise, show plain old popup
                val fragment = ConfirmExitPairingPopupNoDevices()

                fragment.isCancelable = true
                fragment.setKeepSearchingListener(restartSearchingCallback)
                fragment.setGoToDashboardListener(goToDashboardListener)
                popupShowing = fragment
                fragment.show(supportFragmentManager, NoDevicesSearchingTimeout::class.java.name)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.clearView()
    }

    override fun onPause() {
        super.onPause()
        presenter.clearView()
    }

    override fun navigateUpTo(upIntent: Intent?): Boolean {
        onBackPressed() // Simulate a hardware back press
        return false // We didn't redeliver the intent
    }

    override fun onBackPressed() {
        if (!disableBackPress) {
            finish()
        }
    }

    override fun pairingTimedOutWithoutDevices() {
        dismissAndClearPopupShowing()
        val fragment = NoDevicesSearchingTimeout()
        fragment.isCancelable = false
        fragment.setKeepSearchingListener(restartSearchingCallback)
        popupShowing = fragment
        fragment.show(supportFragmentManager, NoDevicesSearchingTimeout::class.java.name)
    }

    override fun pairingTimedOutWithDevices(anyInError: Boolean) {
        dismissAndClearPopupShowing()
        val fragment = WithDevicesSearchingTimeout()
        fragment.isCancelable = false
        fragment.setKeepSearchingListener(restartSearchingCallback)
        fragment.setNoViewMyDevicesListener {
            presenter.setView(this@DeviceSearchingActivity)
        }
        popupShowing = fragment
        fragment.show(supportFragmentManager, WithDevicesSearchingTimeout::class.java.name)
    }

    private fun dismissAndClearPopupShowing() {
        popupShowing?.cleanUp()
        popupShowing?.dismiss()
        popupShowing = null
    }

    override fun searchTimedOut(steps: List<HelpStep>) {
        val adapter = recyclerView.adapter
        if (adapter == null || adapter.itemCount == 0) {
            title.text = getString(R.string.device_searching_title)
            description.text = getString(R.string.device_searching_timeout_desc)
            deviceSearchingTroubleshootingTitle.visibility = View.VISIBLE

            centerContentContainer.visibility = View.VISIBLE
            recyclerView.adapter = TroubleshootingStepsAdapter(this, steps)
        }
    }

    /**
     * Shows the paired devices based on searching or not.
     *
     * Expected elements during each state:
     *
     * Not Searching without devices
     * - Animation hidden
     * - NoDevices Image showing
     * - Text says nothing found, click pair again to pair a device
     * - Troubleshooting title hidden
     * - RecyclerView container hidden
     * - Pair device button showing
     */
    override fun showPairedDevices(deviceList: List<DevicePairingData>, inSearchingMode: Boolean) {
        backButtonEnabled(inSearchingMode)

        when {
            inSearchingMode && deviceList.isNotEmpty() -> {
                searchingWithDevicesPaired(deviceList)
            }
            inSearchingMode && deviceList.isEmpty() -> {
                searchingWithoutDevicesPaired()
            }
            !inSearchingMode && deviceList.isNotEmpty() -> {
                notSearchingWithDevices(deviceList)
            }
            !inSearchingMode && deviceList.isEmpty() -> {
                notSearchingWithoutDevices()
            }
            else -> {
                logger.warn("Received unknown state for showPairedDevices")
            }
        }
    }

    /**
     * Expected:
     *
     * Searching mode with devices
     * - Animation Showing
     * - NoDevices Image hidden
     * - Text says 'found x devices' tap below to customize
     * - Troubleshooting title hidden
     * - RecyclerView container showing devices
     * - Pair device button hidden
     */
    private fun searchingWithDevicesPaired(deviceList: List<DevicePairingData>) {
        searchingAnimation.visibility = View.VISIBLE
        noDevicesImage.visibility = View.GONE
        imageContainer.visibility = View.VISIBLE

        title.text = resources.getQuantityString(R.plurals.devices_found_plural_exclaim, deviceList.size, deviceList.size)
        description.text = getString(R.string.tap_below_to_customize)

        deviceSearchingTroubleshootingTitle.visibility = View.GONE

        centerContentContainer.visibility = View.VISIBLE
        val adapter = recyclerView.adapter
        val newAdapter = DeviceSearchingAdapter(this, deviceList)

        if (adapter != null) {
            if (adapter is TroubleshootingStepsAdapter) {
                recyclerView.adapter = newAdapter
            } else {
                recyclerView.swapAdapter(newAdapter, false)
            }
        } else {
            recyclerView.adapter = newAdapter
        }

        pairAnotherDeviceButton.visibility = View.GONE
    }

    /**
     * Expected:
     *
     * Searching mode without devices
     * - Animation Showing
     * - NoDevices Image hidden
     * - Text says we're searching may take a few
     * - IF NOT showing troubleshooting steps "Troubleshooting title" hidden, else visible
     * - IF NOT showing troubleshooting steps RecyclerView container hidden, else visible
     * - Pair device button hidden
     */
    private fun searchingWithoutDevicesPaired() {
        searchingAnimation.visibility = View.VISIBLE
        noDevicesImage.visibility = View.GONE
        imageContainer.visibility = View.VISIBLE

        title.text = getString(R.string.device_searching_title)
        description.text = getString(R.string.device_searching_desc)

        val currentAdapter = recyclerView.adapter
        if (currentAdapter == null || currentAdapter !is TroubleshootingStepsAdapter) {
            centerContentContainer.visibility = View.GONE
            deviceSearchingTroubleshootingTitle.visibility = View.GONE
        } else {
            deviceSearchingTroubleshootingTitle.visibility = View.VISIBLE
            centerContentContainer.visibility = View.VISIBLE
        }

        pairAnotherDeviceButton.visibility = View.GONE
    }

    /**
     * Expected:
     *
     * Not Searching with devices
     * - Animation hidden
     * - NoDevices Image hidden
     * - Text says 'found x devices' tap below to customize
     * - Troubleshooting title hidden
     * - RecyclerView container showing devices
     * - Pair device button shown but uses text "Pair Another Device"
     */
    private fun notSearchingWithDevices(deviceList: List<DevicePairingData>) {
        searchingAnimation.visibility = View.GONE
        noDevicesImage.visibility = View.GONE
        imageContainer.visibility = View.GONE

        title.text = resources.getQuantityString(R.plurals.devices_found_plural_exclaim, deviceList.size, deviceList.size)

        if (presenter.allDevicesConfigured()) {
            description.text = getString(R.string.all_devices_customized)
        }
        else {
            description.text = getString(R.string.tap_below_to_customize)
        }

        deviceSearchingTroubleshootingTitle.visibility = View.GONE

        centerContentContainer.visibility = View.VISIBLE
        val adapter = recyclerView.adapter
        val newAdapter = DeviceSearchingAdapter(this, deviceList)

        if (adapter != null) {
            if (adapter is TroubleshootingStepsAdapter) {
                recyclerView.adapter = newAdapter
            } else {
                recyclerView.swapAdapter(newAdapter, false)
            }
        } else {
            recyclerView.adapter = newAdapter
        }

        pairAnotherDeviceButton.text = getString(R.string.device_searching_pair_another_device_button_text)
        pairAnotherDeviceButton.visibility = View.VISIBLE
    }

    /**
     * Expected:
     *
     * Not Searching without devices
     * - Animation hidden
     * - NoDevices Image showing
     * - Text says nothing found, click pair again to pair a device
     * - Troubleshooting title hidden
     * - RecyclerView container hidden
     * - Pair device button showing
     */
    private fun notSearchingWithoutDevices() {
        searchingAnimation.visibility = View.GONE
        noDevicesImage.visibility = View.VISIBLE
        imageContainer.visibility = View.VISIBLE

        title.text = getString(R.string.device_searching_pair_device_title)
        description.text = getString(R.string.device_searching_pair_device_desc)

        deviceSearchingTroubleshootingTitle.visibility = View.GONE
        centerContentContainer.visibility = View.GONE
        recyclerView.adapter = null

        pairAnotherDeviceButton.text = getString(R.string.device_searching_pair_device_button_text)
        pairAnotherDeviceButton.visibility = View.VISIBLE
    }

    override fun onMisconfiguredDeviceClicked(pairingDeviceAddress: String) {
        // TODO: Take to resolve when this is ready.
        startActivity(RemoveDeviceActivity.newIntent(this, pairingDeviceAddress))

        backButtonEnabled(false)
        presenter.stopSearching()
    }

    override fun onMispairedDeviceClicked(pairingDeviceAddress: String) {
        startActivity(RemoveDeviceActivity.newIntent(this, pairingDeviceAddress))

        backButtonEnabled(false)
        presenter.stopSearching()
    }

    override fun onCustomizeDeviceClicked(pairingDeviceAddress: String) {
        startActivity(PairingCustomizationActivity.newIntent(this, pairingDeviceAddress))

        backButtonEnabled(false)
        presenter.stopSearching()
    }

    override fun onError(throwable: Throwable) {
        Log.d(TAG, "Error received", throwable)
    }


    override fun dismissWithZwaveRebuild() {
        val intent = Intent(this, ZWaveRebuildActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
    }

    override fun dismissNormally() {
        startActivity(DashboardActivity.getHomeFragmentIntent(this))
    }

    companion object {
        private const val START_SEARCHING_DEFAULT = true
        private const val DISABLE_BACK_PRESS_DEFAULT = false
        const val ARG_START_SEARCHING_BOOL = "ARG_START_SEARCHING_BOOL"
        const val ARG_DISABLE_BACK_PRESS_BOOL = "ARG_DISABLE_BACK_PRESS_BOOL"
        const val ARG_USER_INPUT_STEPS = "ARG_USER_INPUT_STEPS"
        const val TAG = "DeviceSearching.View"

        private val logger = LoggerFactory.getLogger(DeviceSearchingActivity::class.java)

        @JvmStatic
        fun createIntent(
            context: Context,
            startSearching: Boolean = true,
            disableBackPress: Boolean = false,
            userInputStep: Map<String, String> = emptyMap()) = Intent(
            context,
            DeviceSearchingActivity::class.java
        ).also {
            it.putExtra(ARG_START_SEARCHING_BOOL, startSearching)
            it.putExtra(ARG_DISABLE_BACK_PRESS_BOOL, disableBackPress)
            it.putExtra(ARG_USER_INPUT_STEPS, HashMap(userInputStep))
        }
    }
}
