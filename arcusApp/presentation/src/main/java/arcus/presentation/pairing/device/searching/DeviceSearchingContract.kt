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
package arcus.presentation.pairing.device.searching

import android.os.Parcel
import android.os.Parcelable
import arcus.cornea.presenter.BasePresenterContract
import arcus.presentation.pairing.device.steps.WebLink

interface DeviceSearchingView {
    /**
     * Invoked when the search for a device times out and we have help steps
     *
     * @param steps list of help steps
     */
    fun searchTimedOut(steps: List<HelpStep>)

    /**
     * Invoked when a new device is paired - This wil be the full list of devices each invocation
     *
     * @param deviceList the list of devices
     * @param inSearchingMode if the system is in pairing mode currently
     */
    fun showPairedDevices(deviceList: List<DevicePairingData>, inSearchingMode: Boolean)

    /**
     * Invoked when the subsystem leaves pairing mode eg: goes back to IDLE and does not have any
     * devices paired.
     */
    fun pairingTimedOutWithoutDevices()

    /**
     * Invoked when the subsystem leaves pairing mode eg: goes back to IDLE and has devices paired
     *
     * @param anyInError true if any devices are in an error state
     */
    fun pairingTimedOutWithDevices(anyInError: Boolean)

    /**
     * Navigate to the ZWave rebuild activity when we dismiss the pairing cart
     */
    fun dismissWithZwaveRebuild()

    /**
     * dismiss the contents of the pairing cart normally
     */
    fun dismissNormally()

    /**
     * Invoked when an error has occurred
     */
    fun onError(throwable: Throwable)
}

interface DeviceSearchingPresenter : BasePresenterContract<DeviceSearchingView> {
    /**
     * Stops pairing mode on the subsystem - same as when we exit back to the "+ Menu"
     */
    fun dismissAll()

    /**
     * Starts searching timer for the current product / any product
     *
     * @param formInputs optional (null if not present) map of inputs from previous form
     * entries that need to be passed to the platform
     */
    fun startSearching(formInputs: Map<String, String>? = null)

    /**
     * Gets the currently paired devices.
     */
    fun updatePairedDeviceList()

    /**
     * Restarts searching timer for the current product / any product
     *
     * @param formInputs optional (null if not present) map of inputs from previous form
     * entries that need to be passed to the platform
     */
    fun restartSearching(formInputs: Map<String, String>? = null)

    /**
     * Attempts to instruct the pairing subsystem to stop searching
     */
    fun stopSearching()

    /**
     * Checks for mispaired or misfigured devices during pairing
     */
    fun getMispairedOrMisconfigured(): Boolean

    /**
     * Checks to see if all devices are configured.
     */
    fun allDevicesConfigured() : Boolean

    /**
     * Checks for paired devices during pairing
     */
    fun hasPairedDevices(): Boolean

    /**
     * Gets the number of paired devices in the subsystem.
     */
    fun getPairedDevicesCount(): Int

    /**
     * Checks to see if we ARE IDLE.
     */
    fun isIdle(): Boolean
}

enum class HelpStepType {
    INFO,
    PAIRING_STEPS,
    LINK,
    FORM,
    FACTORY_RESET,
}

data class HelpStep(
    val id: String,
    val order: Int,
    val action: HelpStepType,
    val message: String,
    val link: WebLink?
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readInt(),
            HelpStepType.values()[source.readInt()],
            source.readString(),
            source.readParcelable<WebLink>(WebLink::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeInt(order)
        writeInt(action.ordinal)
        writeString(message)
        writeParcelable(link, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<HelpStep> = object :
            Parcelable.Creator<HelpStep> {
            override fun createFromParcel(source: Parcel): HelpStep =
                HelpStep(source)
            override fun newArray(size: Int): Array<HelpStep?> = arrayOfNulls(size)
        }
    }
}

enum class DevicePairingPhase(val canonicalName: String) {
    JOIN ("Found New Device"),
    CONNECT ("Connecting to Device"),
    IDENTIFY ("Discovering Device Features"),
    PREPARE ("Preparing Device for Use"),
    CONFIGURE ("Writing Initial Settings"),
    FAILED ("Failed"),
    PAIRED ("Paired"),
}

enum class DevicePairingState(val canonicalName: String) {
    PAIRING ("Pairing"),
    MISPAIRED ("Improperly Paired"),
    MISCONFIGURED ("Misconfigured"),
    PAIRED ("Paired"),
}

data class DevicePairingData(
    val id: String,
    val productId: String,
    val productScreen: String,
    val pairingState: DevicePairingState,
    val description: String,
    val pairingDeviceAddress: String,
    val name: String = "Device Detected",
    val customized: Boolean = false,
    val errorState: Boolean = false
) : Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.readString(),
        source.readString(),
        DevicePairingState.values()[source.readInt()],
        source.readString(),
        source.readString(),
        source.readString(),
        1 == source.readInt(),
        1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(id)
        writeString(productId)
        writeString(productScreen)
        writeInt(pairingState.ordinal)
        writeString(description)
        writeString(pairingDeviceAddress)
        writeString(name)
        writeInt((if (customized) 1 else 0))
        writeInt((if (errorState) 1 else 0))
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<DevicePairingData> =
            object :
                Parcelable.Creator<DevicePairingData> {
                override fun createFromParcel(source: Parcel): DevicePairingData =
                    DevicePairingData(source)

                override fun newArray(size: Int): Array<DevicePairingData?> = arrayOfNulls(size)
            }
    }
}

data class PairedDeviceModel(
    val id: String,
    val iconUrl: String,
    val fallbackIconUrl: String,
    val name: String,
    val pairingDeviceAddress: String,
    val description: String,
    val customized: Boolean,
    val errorState: Boolean,
    val pairingState: DevicePairingState
) {
    fun getViewType(): Int {
        return if(errorState) {    // If the pairing phase is FAILED
            if (pairingState == DevicePairingState.MISCONFIGURED) {
                Type.MISCONFIGURED_DEVICE    // Resolve chevron
            } else {
                Type.MISPAIRED_DEVICE        // Remove chevron
            }
        } else if(pairingState != DevicePairingState.PAIRED) {
            Type.PARTIALLY_PAIRED_DEVICE     // spinner
        } else {
            if (customized) {
                Type.FULLY_PAIRED_DEVICE                // checkmark
            } else {
                Type.PAIRED_NEEDS_CUSTOMIZATION_DEVICE   // customize chevron
            }
        }
    }

    object Type {
        const val FULLY_PAIRED_DEVICE = 0
        const val PARTIALLY_PAIRED_DEVICE = 1
        const val PAIRED_NEEDS_CUSTOMIZATION_DEVICE = 2
        const val MISPAIRED_DEVICE = 3
        const val MISCONFIGURED_DEVICE = 4
    }
}