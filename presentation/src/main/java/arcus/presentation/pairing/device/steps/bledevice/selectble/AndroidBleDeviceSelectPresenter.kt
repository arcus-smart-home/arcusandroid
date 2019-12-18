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
package arcus.presentation.pairing.device.steps.bledevice.selectble

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.HandlerThread
import android.os.Looper
import androidx.annotation.WorkerThread
import arcus.cornea.presenter.KBasePresenter
import arcus.cornea.utils.AndroidExecutor
import arcus.cornea.utils.ScheduledExecutor
import arcus.presentation.ble.BleConnector
import arcus.presentation.ble.BleDevice
import arcus.presentation.ble.BluetoothInteractionCallbacks
import arcus.presentation.pairing.device.steps.bledevice.BleConnectionStatus
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.properties.Delegates
import org.slf4j.LoggerFactory

class AndroidBleDeviceSelectPresenter
private constructor(
    private val namePrefix: String,
    private var scheduledExecutor: ScheduledExecutor = AndroidExecutor(Looper.myLooper()!!)
) : BleDeviceSelectPresenter<Context>, KBasePresenter<BleDeviceSelectView>() {
    private val receiverRegistered = AtomicBoolean(false)
    private val bleAdapterRef = AtomicReference<BluetoothAdapter?>(null)
    private val classicScanBroadcastReceiver = object : BroadcastReceiver() {
        val devicesFound = mutableMapOf<String, BluetoothDevice>()

        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return

            if (BluetoothDevice.ACTION_FOUND == intent.action) {
                val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                // Must not be classic only - must be LE or LE+Classic
                if (device != null &&
                    device.type != BluetoothDevice.DEVICE_TYPE_CLASSIC &&
                    device.name?.startsWith(namePrefix) == true
                ) {
                    // Always keep the latest device
                    devicesFound[device.address] = device
                }

                foundDevices(devicesFound.values.toList())
            }
        }
    }

    private var bluetoothConnector by Delegates.notNull<BleConnector<Context>>()
    private var handlerThread: HandlerThread? = null

    override fun setBleConnector(connector: BleConnector<Context>) {
        bluetoothConnector = connector
    }

    override fun reconnectBle(with: Context?) {
        if (!bluetoothConnector.reconnect(with)) {
            onlyIfView {
                it.onBleStatusChange(BleConnectionStatus.BLE_CONNECT_FAILURE)
            }
        }
    }

    override fun startScanning(with: Context?) {
        with?.applicationContext?.let { appContext ->
            if (!receiverRegistered.getAndSet(true)) {
                appContext.registerReceiver(
                    classicScanBroadcastReceiver,
                    IntentFilter(BluetoothDevice.ACTION_FOUND)
                )
            }

            updateBleAdapter(with)?.let { adapter ->
                classicScanBroadcastReceiver.devicesFound.clear()
                doStartScanning(adapter)
            }
        }
    }

    override fun stopScanning(with: Context?) {
        if (with != null) {
            with.applicationContext?.let { appContext ->
                if (receiverRegistered.getAndSet(false)) {
                    appContext.unregisterReceiver(classicScanBroadcastReceiver)
                }

                updateBleAdapter(with)?.let {
                    doStopScanning(it)
                }
            }
        } else {
            // We can't unregister the receiver but we can at least try to stop the scan.
            // Failsafe for what should't ever happen.
            val bleAdapter = BluetoothAdapter.getDefaultAdapter()
            bleAdapter?.let {
                doStopScanning(it)
            }
        }
    }

    override fun connectToDevice(with: Context?, device: BleDevice, autoReconnect: Boolean) {
        stopScanning(with)
        disconnect()
        logger.debug("Requested connection to ${device.name} which is device type ${device.type} (Need type 2)")

        scheduledExecutor.executeDelayed(CONNECTION_TIMEOUT_DELAY) {
            scheduledExecutor.clearExecutor()
            disconnect()
            onMainWithView {
                onBleStatusChange(BleConnectionStatus.BLE_CONNECT_FAILURE)
            }
        }
        bluetoothConnector.connect(with, device.bluetoothDevice)
    }

    override fun disconnect() = bluetoothConnector.disconnectAndClose()

    override fun isConnected(): Boolean = bluetoothConnector.isConnected()

    override fun setView(view: BleDeviceSelectView) {
        super.setView(view)

        handlerThread?.quitSafely()
        handlerThread = HandlerThread("CB_HT")
        handlerThread?.let {
            it.start()
            scheduledExecutor = AndroidExecutor(it.looper)
        }
    }

    override fun initializeBleInteractionCallback() {
        bluetoothConnector.interactionCallback = object : BluetoothInteractionCallbacks() {
            override fun onConnected() {
                logger.debug("Device Connected. Notifying View")
                scheduledExecutor.clearExecutor()
                onMainWithView {
                    onBleStatusChange(BleConnectionStatus.BLE_CONNECTED)
                }
            }

            override fun onDisconnected(previouslyConnected: Boolean) {
                logger.debug("Device Disconnected. Was ${if (previouslyConnected) "" else "NOT "}previously connected.")
                scheduledExecutor.clearExecutor() // Clear the timeout from when we try to connect to the device
                onMainWithView {
                    when (previouslyConnected) {
                        true -> {
                            onBleStatusChange(BleConnectionStatus.BLE_DISCONNECTED)
                        }
                        false -> {
                            onBleStatusChange(BleConnectionStatus.BLE_CONNECT_FAILURE)
                        }
                    }
                }

                bleAdapterRef.get()?.let { adapter ->
                    doStartScanning(adapter)
                }
            }
        }
    }

    override fun clearView() {
        super.clearView()
        scheduledExecutor.clearExecutor() // Flush the queue.
        bleAdapterRef.getAndSet(null)?.cancelDiscovery()

        handlerThread?.quitSafely()
    }

    private fun updateBleAdapter(with: Context?) = with?.getBleAdapter()?.let {
        bleAdapterRef.set(it)
        it
    }

    private fun doStartScanning(adapter: BluetoothAdapter) {
        // Setup a fallback in the event we don't get results after "X" delay
        doStopScanning(adapter)
        resetAndAddTimeoutRunnable()

        onMainWithView {
            onSearching()
        }

        adapter.startDiscovery()
    }

    private fun resetAndAddTimeoutRunnable() {
        scheduledExecutor.clearExecutor()
        scheduledExecutor.executeDelayed(TIMEOUT_DELAY) {
            bleAdapterRef.get()?.let { adapter ->
                doStopScanning(adapter)
                scheduledExecutor.executeDelayed(RESCAN_DELAY) {
                    doStartScanning(adapter)
                }
            }

            logger.debug("Hit searching timeout. Stopping and starting a new search after [$RESCAN_DELAY ms] delay.")
        }
    }

    private fun doStopScanning(adapter: BluetoothAdapter) {
        scheduledExecutor.clearExecutor() // Flush the queue.

        adapter.cancelDiscovery()

        onMainWithView {
            onSearchingStopped()
        }
    }

    @WorkerThread
    internal fun foundDevices(devices: List<BluetoothDevice>) {
        val device = bluetoothConnector.getConnectedDevice()
        val bleDevices = devices
            .map {
                BleDevice(it, device?.address == it.address)
            }

        if (bleDevices.isNotEmpty()) { // Tell UI About our devices (or lack thereof)
            onMainWithView {
                onBleDevicesFound(bleDevices)

                if (namePrefix.contains("hub", true)) {
                    onShouldConnectTo(bleDevices.first())
                }
            }
        }
    }

    private fun Context.getBleManager(): BluetoothManager? =
        getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager?
    private fun Context.getBleAdapter(): BluetoothAdapter? = getBleManager()?.adapter

    companion object {
        private val logger = LoggerFactory.getLogger(AndroidBleDeviceSelectPresenter::class.java)
        /**
         * Maximum duration we'll wait to hear we've connected to a device.
         */
        private val CONNECTION_TIMEOUT_DELAY = TimeUnit.SECONDS.toMillis(25)

        /**
         * Maximum duration of a scan w/o getting any scan results back.
         */
        private val TIMEOUT_DELAY = TimeUnit.SECONDS.toMillis(12)

        /**
         * Time in between successful scans to wait before starting another scan.
         */
        private val RESCAN_DELAY = TimeUnit.SECONDS.toMillis(2)

        @JvmStatic
        fun forPrefix(prefix: String) = AndroidBleDeviceSelectPresenter(prefix)
    }
}
