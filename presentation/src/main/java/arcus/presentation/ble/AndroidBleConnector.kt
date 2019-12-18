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
package arcus.presentation.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Build
import android.os.Handler
import java.util.UUID
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import org.slf4j.LoggerFactory

class AndroidBleConnector(
    private val handler: Handler
) : BleConnector<Context> {
    @Volatile // Always set on main, but can be read from a Binder thread.
    override var interactionCallback: BluetoothInteractionCallbacks? = null

    override var serialNumber: String = ""

    /**
     * The mac address identified during the scanning process.
     *
     * Devices use the last 12 characters to put the mac address in the name.
     * Example: Cam_123456789012
     */
    private var deviceMac: String = ""

    private var delayBetweenNetworkStatusReads: Long = 0L
    private val isMonitoringNetworkStatus = AtomicBoolean(false)

    private val currentGattConnection = AtomicReference<BluetoothGatt?>(null)
    private val previousConnecteDevice = AtomicReference<BluetoothDevice?>(null)

    // Device seems to handle serial I/O best.
    private val readWriteLock = Semaphore(1)

    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            ch: BluetoothGattCharacteristic?,
            status: Int
        ) {
            try {
                if (ch != null) {
                    // we got response regarding our request to fetch characteristic value
                    if (BluetoothGatt.GATT_SUCCESS == status) {
                        when (ch.uuid) {
                            GattCharacteristic.SERIAL_NUMBER.uuid -> {
                                getStringValueOfAndLog(ch).let {
                                    serialNumber = it
                                    interactionCallback?.onConnected()
                                }
                            }
                            GattCharacteristic.STATUS.uuid -> {
                                val results = getStringValueOfAndLog(ch)
                                if (isMonitoringNetworkStatus.get()) {
                                    currentGattConnection.get()?.let {
                                        requestReadCharacteristic(it, ch, delayBetweenNetworkStatusReads)
                                    }

                                    interactionCallback?.onReadSuccess(ch.uuid, results)
                                }
                            }
                            else -> {
                                val results = getStringValueOfAndLog(ch)
                                interactionCallback?.onReadSuccess(ch.uuid, results)
                            }
                        }
                    } else {
                        interactionCallback?.onReadFailure(ch.uuid)
                    }
                }
            } finally {
                logger.debug(
                    "Releasing lock (reading) for: " +
                            "${GattCharacteristic.fromUuid(ch?.uuid).canonicalName} Status: $status"
                )
                readWriteLock.release()
            }
        }

        override fun onCharacteristicWrite(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            try {
                characteristic?.let {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        interactionCallback?.onWriteSuccess(it.uuid, it.getStringValue(0) ?: "")
                    } else {
                        interactionCallback?.onWriteFailure(it.uuid)
                    }
                }
            } finally {
                logger.debug(
                    "Releasing lock (writing) for: " +
                            "${GattCharacteristic.fromUuid(characteristic?.uuid).canonicalName}; " +
                            "Status: $status; Updated Value: ${characteristic?.getStringValue(0)};"
                )
                readWriteLock.release()
            }
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?
        ) {
            // Only called if we enable notifications for characteristics. Though this isn't always 100% it seems...
            logger.debug(
                "onCharacteristicChanged: " +
                        "${GattCharacteristic.fromUuid(characteristic?.uuid).canonicalName}; " +
                        "String Value: ${characteristic?.getStringValue(0)};"
            )
        }

        override fun onConnectionStateChange(
            gatt: BluetoothGatt?,
            status: Int,
            newState: Int
        ) {
            logger.debug("Connection state changed: Gatt: $gatt, Status: $status, New State: $newState}")
            gatt?.let {
                when (newState) {
                    BluetoothProfile.STATE_CONNECTED -> {
                        currentGattConnection.set(it)
                        val name = it.device.name ?: "ABC123-123456789012"
                        deviceMac = name.substring(name.length - 12, name.length)
                        it.discoverServices()
                        previousConnecteDevice.set(null) // Clear this out - only set when we disconnect again
                        // We don't call connected until we've read the serial number.
                    }

                    BluetoothProfile.STATE_DISCONNECTING,
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        val previous = currentGattConnection.getAndSet(null)
                        previous?.close() // No Leaky Leaky the resource mmm k?
                        previousConnecteDevice.set(previous?.device)
                        interactionCallback?.onDisconnected(previous != null)
                    }
                    else -> { /* No Op - Ignoring: BluetoothProfile.STATE_CONNECTING */
                    }
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            logger.debug("onServicesDiscovered Gatt: $gatt, Status: $status")

            if (status == BluetoothGatt.GATT_SUCCESS) {
                gatt?.let {
                    val services = it.services ?: emptyList()
                    handleServicesDiscovered(it, services)
                }
            }
        }

        private fun handleServicesDiscovered(gatt: BluetoothGatt, services: List<BluetoothGattService>) {
            services.forEach {
                logger.debug("Found [${GattService.fromUuid(it.uuid).canonicalName}]. " +
                        "[${it.uuid}] Parsing Characteristics.")
                (it.characteristics ?: emptyList()).forEach {
                    when (it.uuid) {
                        // Add more as needed... Ex:
//                        GattCharacteristic.WIFI_CFG_FREQ.uuid,
//                        GattCharacteristic.WIFI_CFG_SUP_MODES.uuid,
                        GattCharacteristic.SERIAL_NUMBER.uuid,
                        GattCharacteristic.DEVICE_NAME.uuid -> {
                            requestReadCharacteristic(gatt, it)
                        }
                    }
                }
            }
        }

        override fun onDescriptorWrite(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            logger.debug("Releasing lock; onDescriptorWrite: " +
                    "UUID: ${GattDescriptor.fromUuid(descriptor?.uuid).canonicalName}, " +
                    "Status: $status")
            readWriteLock.release()
        }

        private fun getStringValueOfAndLog(
            characteristic: BluetoothGattCharacteristic,
            andLog: Boolean = true,
            onlyPrintable: Boolean = true
        ): String {
            val bytes = characteristic.value
            return if (bytes != null && bytes.isNotEmpty()) {
                val sb = StringBuffer(bytes.size)
                if (onlyPrintable) {
                    bytes
                        .filter { it in 32..126 }
                        .forEach { sb.append("%c".format(it)) }
                } else {
                    bytes.forEach { sb.append("%c".format(it)) }
                }

                if (andLog) {
                    logger.debug(
                        "Read new value for " +
                                "${GattCharacteristic.fromUuid(characteristic.uuid).canonicalName} of [$sb]"
                    )
                }
                sb.toString()
            } else {
                ""
            }
        }

        // Can open some of these up if we need to. Leaving final for now until necessecary
        override fun onReadRemoteRssi(gatt: BluetoothGatt?, rssi: Int, status: Int) {
            logger.debug("Read remote RSSI: Gatt: $gatt, Rssi: $rssi, Status: $status")
        }

        override fun onPhyUpdate(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            logger.debug("onPhyUpdate: Gatt: $gatt, tyPhy: $txPhy, rxPhy: $rxPhy, Status: $status")
        }

        override fun onMtuChanged(gatt: BluetoothGatt?, mtu: Int, status: Int) {
            logger.debug("onMtuChanged: Gatt: $gatt, Mtu: $mtu, Status: $status")
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt?, status: Int) {
            logger.debug("onReliableWriteCompleted: Gatt: $gatt, Status: $status")
        }

        override fun onDescriptorRead(
            gatt: BluetoothGatt?,
            descriptor: BluetoothGattDescriptor?,
            status: Int
        ) {
            logger.debug("onDescriptorRead: UUID: ${descriptor?.uuid}, Status: $status")
        }

        override fun onPhyRead(gatt: BluetoothGatt?, txPhy: Int, rxPhy: Int, status: Int) {
            logger.debug("onPhyRead: ")
        }
    }

    override fun connect(with: Context?, device: BluetoothDevice, autoConnect: Boolean) {
        if (with == null) {
            logger.error("Cannot connect to device. Context was null.")
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            device.connectGatt(with, autoConnect, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE)
        } else {
            device.connectGatt(with, autoConnect, bluetoothGattCallback)
        }
    }

    override fun reconnect(with: Context?, autoConnect: Boolean): Boolean {
        val device = previousConnecteDevice.get()
        return if (device == null) {
            false
        } else {
            connect(with, device, autoConnect)
            true
        }
    }

    override fun disconnectAndClose() {
        currentGattConnection.getAndSet(null)?.run {
            previousConnecteDevice.set(null)
            disconnect()
            close()
        }
    }

    override fun isConnected(): Boolean = currentGattConnection.get() != null

    override fun getConnectedDevice(): BluetoothDevice? = currentGattConnection.get()?.device

    override fun scanForWiFiNetworks(): Boolean {
        GattCharacteristic.SCAN_RESULTS.uuid // Reading this causes networks to be scanned on the camera.
        return currentGattConnection.get()?.let {
            requestWiFiServiceRead(it, GattCharacteristic.SCAN_RESULTS.uuid)
            true
        } ?: false
    }

    private fun requestWiFiServiceRead(gatt: BluetoothGatt, characteristicUUID: UUID, delay: Long = 0L) {
        val wifiConfigService = gatt.getService(GattService.WIFI_CONFIG.uuid) ?: return
        val characteristic = wifiConfigService.getCharacteristic(characteristicUUID) ?: return

        logger.debug("Requesting WiFi Service Read.")
        requestReadCharacteristic(gatt, characteristic, delay)
    }

    private fun requestReadCharacteristic(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        delay: Long = 0L
    ) {
        val runnable = Runnable {
            if (characteristic.uuid != GattCharacteristic.STATUS.uuid || isMonitoringNetworkStatus.get()) {
                readWriteLock.acquireUninterruptibly()
                logger.debug(
                    "Acquired lock (read) for: " +
                            GattCharacteristic.fromUuid(characteristic.uuid).canonicalName
                )
                gatt.readCharacteristic(characteristic)
            }
        }

        if (delay > 0) {
            handler.postDelayed(runnable, delay)
        } else {
            handler.post(runnable)
        }
    }

    override fun writeWiFiConfiguration(pass: String, network: String, securityType: String): Boolean {
        return try {
            val cryptor = AESCrypt.forArcusBleDevice(deviceMac)
            val encPass = cryptor.encrypt(pass)

            logger.debug("Encrypted WiFi Status: [${AESCrypt.convertToHexStr(encPass)}] Mac: [$deviceMac]")

            currentGattConnection.get()?.let { nnGatt ->
                requestWiFiServiceWrite(nnGatt, GattCharacteristic.SSID.uuid, network.toByteArray())
                requestWiFiServiceWrite(nnGatt, GattCharacteristic.AUTH.uuid, securityType.toByteArray())
                requestWiFiServiceWrite(nnGatt, GattCharacteristic.PASS.uuid, encPass)
                true
            } ?: false
        } catch (ex: Exception) {
            logger.debug("Could not encrypt/write data.", ex)
            false
        }
    }

    private fun requestWiFiServiceWrite(
        gatt: BluetoothGatt,
        characteristicUUID: UUID,
        value: ByteArray
    ) {

        val wifiConfigService = gatt.getService(GattService.WIFI_CONFIG.uuid) ?: return
        val characteristic = wifiConfigService.getCharacteristic(characteristicUUID) ?: return

        logger.debug("Requesting WiFi Service Write.")
        requestWriteCharacteristic(gatt, characteristic, value)
    }

    private fun requestWriteCharacteristic(
        gatt: BluetoothGatt,
        characteristic: BluetoothGattCharacteristic,
        value: ByteArray
    ) {

        handler.post {
            readWriteLock.acquireUninterruptibly()
            logger.debug(
                "Acquired lock (write) for: " +
                        GattCharacteristic.fromUuid(characteristic.uuid).canonicalName +
                        " to write value: [${String(value, charset("UTF-8"))}]"
            )
            characteristic.value = value
            gatt.writeCharacteristic(characteristic)
        }
    }

    override fun startMonitoringNetworkStatus(delayBetweenReads: Long, unit: TimeUnit): Boolean {
        return currentGattConnection.get()?.let { nnGatt ->
            // Looks like it goes to NO_INTERNET if it fails to connect but by then (even when I did this as quick as I could)
            // the device times out and we have to put it back into BLE mode and reconnect to it...
            isMonitoringNetworkStatus.set(true)
            delayBetweenNetworkStatusReads = unit.toMillis(delayBetweenReads)
            requestWiFiServiceRead(nnGatt, GattCharacteristic.STATUS.uuid, delayBetweenNetworkStatusReads)
            true
        } ?: false
    }

    override fun stopMonitoringNetworkStatus() {
        isMonitoringNetworkStatus.set(false)
    }

    companion object {
        private val logger = LoggerFactory.getLogger(AndroidBleConnector::class.java)
    }
}
