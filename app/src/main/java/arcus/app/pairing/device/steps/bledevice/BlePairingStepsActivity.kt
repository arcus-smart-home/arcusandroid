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
package arcus.app.pairing.device.steps.bledevice

import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import android.view.View
import arcus.app.R
import arcus.app.common.fragments.ModalBottomSheet
import arcus.app.common.view.DisableSwipeViewPager
import arcus.app.common.fragment.TitledFragment
import arcus.app.common.utils.setColorSchemePurple
import arcus.app.common.utils.setColorSchemePurpleOutline
import arcus.app.device.settings.wifi.BleWiFiReconfigureStepFragment
import arcus.app.pairing.device.steps.bledevice.connect.BleConnectFragment
import arcus.app.pairing.device.steps.PairingStepsActivity
import arcus.app.pairing.device.steps.ViewPagerSelectedFragment
import arcus.app.pairing.device.steps.bledevice.bleonoff.BleOnOffFragment
import arcus.presentation.ble.BleWiFiNetwork
import arcus.presentation.ble.AndroidBleConnector
import arcus.presentation.ble.BleConnector
import arcus.presentation.pairing.V03_HUB_PRODUCT_ADDRESS
import kotlin.properties.Delegates

interface BleStepsNavigationDelegate {
    /**
     * Callback indicating that any "forward" navigation should be enabled
     */
    fun enableContinue()

    /**
     * Callback indicating that any "forward" navigation should be disabled
     */
    fun disableContinue()

    fun setSelectedNetwork(selectedNetwork: BleWiFiNetwork)

    fun rewindToEnterBleModeInstructionsPage()
}

class BlePairingStepsActivity : PairingStepsActivity(), BleStepsNavigationDelegate {
    private var viewPagerFragments by Delegates.notNull<List<Fragment>>()
    private lateinit var bluetoothConnector : BleConnector<Context>
    private lateinit var handlerThread : HandlerThread
    private lateinit var handler : Handler
    private var onPauseCalled = false
    private var bleOnOffPosition = 1
    private var bleEnablePairingPosition = 1
    private var popupShowing : ModalBottomSheet? = null
    private var backEnabled: Boolean = true
        set(value) {
            field = value
            supportActionBar?.setDisplayHomeAsUpEnabled(value)
        }
        get() {
            return field || viewPager.currentItem != 0
        }

    private val pageChangedListener = object : ViewPager.OnPageChangeListener {
        override fun onPageScrollStateChanged(state: Int) {
            // No-Op
        }

        override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            // No-Op
        }

        override fun onPageSelected(position: Int) {
            if (position < 0) {
                return
            }

            circlePageIndicator.setCurrentItem(position)
            if (snackBar?.isShownOrQueued() == true) {
                snackBar?.dismiss()
            }

            val currentFragment = viewPagerFragments[position]
            when {
                currentFragment is BleWiFiReconfigureStepFragment && position == 0 -> {
                    backEnabled = false
                    // Setup becomes next, next becomes cancel
                    setupButton.setColorSchemePurple()
                    setupButton.text = getString(R.string.pairing_next)
                    setupButton.visibility = View.VISIBLE
                    setupButton.setOnClickListener {
                        viewPager.setCurrentItem(viewPager.currentItem + 1, true)
                    }
                    nextButton.isEnabled = true
                    nextButton.setColorSchemePurpleOutline()
                    nextButton.text = getString(R.string.cancel_text)
                    nextButton.setOnClickListener {
                        finish()
                    }
                }
                currentFragment is BleConnectFragment -> {
                    updateBleConnectButtons(currentFragment)
                }
                else -> {
                    setupButton.visibility = View.GONE
                    backEnabled = true
                    nextButton.setColorSchemePurple()
                    updateNextButtonText(position)
                    enableContinue()
                }
            }

            if (currentFragment is BleConnected) {
                currentFragment.setBleConnector(bluetoothConnector)
            }

            // Notify the previous one it is not selected anymore.
            val previousIndex = position - 1
            if (previousIndex >= 0 && viewPagerFragments[previousIndex] is ViewPagerSelectedFragment) {
                (viewPagerFragments[previousIndex] as ViewPagerSelectedFragment).onNotSelected()
            }

            // Notify the current one it is selected.
            if (currentFragment is ViewPagerSelectedFragment) {
                currentFragment.onPageSelected()
            }

            // Notify the next one it is not selected anymore.
            val nextIndex = position + 1
            if (nextIndex <= viewPagerFragments.lastIndex && viewPagerFragments[nextIndex] is ViewPagerSelectedFragment) {
                (viewPagerFragments[nextIndex] as ViewPagerSelectedFragment).onNotSelected()
            }

            if (currentFragment is TitledFragment) {
                title = currentFragment.getTitle()
            }
        }
    }
    private val bluetoothStateChangeBR = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val nnIntent = intent ?: return
            if (viewPager.currentItem >= bleOnOffPosition && nnIntent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (nnIntent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF,
                    BluetoothAdapter.STATE_TURNING_OFF -> {
                        if (viewPager.currentItem == bleOnOffPosition) {
                            pageChangedListener.onPageSelected(bleOnOffPosition)
                        } else {
                            val popup = BleTurnedOffPopup()
                            popup.isCancelable = false
                            popup.tryAgainAction = {
                                viewPager.currentItem = bleOnOffPosition
                                pageChangedListener.onPageSelected(bleOnOffPosition)
                                popupShowing = null
                            }

                            if (popupShowing == null) {
                                popupShowing = popup
                                popup.show(supportFragmentManager)
                            }
                        }
                    }
                    else -> {
                        if (viewPager.currentItem == bleOnOffPosition) {
                            pageChangedListener.onPageSelected(bleOnOffPosition)
                        }
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        handlerThread = HandlerThread("BTLERW")
        handlerThread.start()
        handler = Handler(handlerThread.looper)
        bluetoothConnector = AndroidBleConnector(handler)
        viewPager.offscreenPageLimit = 2
        backEnabled = !isForReconnect
        registerReceiver(bluetoothStateChangeBR, IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
    }

    override fun onResume() {
        super.onResume()
        if (onPauseCalled) {
            onPauseCalled = false
            pageChangedListener.onPageSelected(viewPager.currentItem)
        }

        if (isForReconnect) {
            circlePageIndicator.visibility = View.GONE
            (viewPager as? DisableSwipeViewPager)?.disableSwipe = DisableSwipeViewPager.DisableSwipe.BOTH
        }
    }

    override fun onBackPressed() {
        if (backEnabled) {
            super.onBackPressed()
        }
    }

    override fun onPause() {
        super.onPause()
        onPauseCalled = true
    }

    override fun onDestroy() {
        super.onDestroy()
        bluetoothConnector.disconnectAndClose()
        handlerThread.quitSafely()
        unregisterReceiver(bluetoothStateChangeBR)
    }

    override fun finish() {
        super.finish()
        if (isForReconnect) {
            overridePendingTransition(R.anim.fade_in, R.anim.slide_out_through_bottom)
        }
    }

    override fun getPageChangedListener(fragments: List<Fragment>): ViewPager.OnPageChangeListener {
        viewPagerFragments = fragments
        bleOnOffPosition = Math.abs(fragments.indexOfFirst { it is BleOnOffFragment })
        bleEnablePairingPosition = Math.abs(fragments.indexOfFirst { it is EnableBlePairingStep || it is BleWiFiReconfigureStepFragment })
        viewPager.postDelayed({
            pageChangedListener.onPageSelected(viewPager.currentItem)
        }, 500)
        return pageChangedListener
    }

    override fun updateNextButtonText(selectedPosition: Int) {
        super.updateNextButtonText(selectedPosition)

        // On the first page
        if (viewPager.currentItem == 0 && isForReconnect) {
            backEnabled = false
            // Setup becomes next, next becomes cancel
            setupButton.setColorSchemePurple()
            setupButton.text = getString(R.string.pairing_next)
            setupButton.visibility = View.VISIBLE
            setupButton.setOnClickListener {
                viewPager.setCurrentItem(viewPager.currentItem + 1, true)
            }
            nextButton.isEnabled = true
            nextButton.setColorSchemePurpleOutline()
            nextButton.text = getString(R.string.cancel_text)
            nextButton.setOnClickListener {
                finish()
            }
        }
    }

    fun updateBleConnectButtons(fragment: BleConnectFragment) {
        nextButton.text = getString(R.string.connect)
        setupButton.visibility = View.GONE

        nextButton.setOnClickListener {
            fragment.connectBleCamera()
        }
    }

    override fun enableContinue() {
        super.enableContinue()
        if (!isForReconnect) {
            (viewPager as? DisableSwipeViewPager)?.disableSwipe = DisableSwipeViewPager.DisableSwipe.NONE
        }
    }

    override fun disableContinue() {
        super.disableContinue()
        if (!isForReconnect) {
            (viewPager as? DisableSwipeViewPager)?.disableSwipe = DisableSwipeViewPager.DisableSwipe.FORWARD
        }
    }

    override fun rewindToEnterBleModeInstructionsPage() {
        viewPager.currentItem = bleEnablePairingPosition
        pageChangedListener.onPageSelected(bleEnablePairingPosition)
    }

    override fun setSelectedNetwork(selectedNetwork: BleWiFiNetwork) {
        viewPagerFragments
            .first {
                it is BleConnectFragment
            }
            .let {
                (it as BleConnectFragment).selectedNetwork = selectedNetwork
            }
    }

    companion object {
        @JvmStatic
        fun createIntent(context: Context, productAddress: String) = Intent(
            context,
            BlePairingStepsActivity::class.java
        ).also {
            it.putExtra(ARG_PRODUCT_ADDRESS, productAddress)
        }

        @JvmStatic
        fun createIntentForV3Hub(
            context: Context
        ) = createIntent(context, V03_HUB_PRODUCT_ADDRESS)

        @JvmStatic
        fun createIntentForReconnect(
            context: Context,
            productAddress: String
        ) = createIntent(context, productAddress).also {
            it.putExtra(ARG_IS_RECONNECT, true)
        }

        @JvmStatic
        fun createIntentForV3HubReconnect(
            context: Context
        ) = createIntent(context, V03_HUB_PRODUCT_ADDRESS).also {
            it.putExtra(ARG_IS_RECONNECT, true)
        }
    }
}
