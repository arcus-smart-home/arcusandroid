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
package arcus.presentation.pairing.device.steps

import android.os.Parcel
import android.os.Parcelable
import arcus.cornea.presenter.BasePresenterContract
import arcus.presentation.pairing.WIFI_SMART_SWITCH_PRODUCT_ID

interface PairingStepsView {
    /**
     * Show pairing steps for a device without a pairing step video URL
     *
     * @param pageTitle - Title of the page
     * @param steps - The devices pairing steps
     */
    fun updateView(pageTitle: String, steps: List<ParsedPairingStep>)

    /**
     * Show pairing steps for a device WITH a pairing step video URL
     *
     * @param pageTitle - Title of the page
     * @param steps - The devices pairing steps
     * @param videoUrl - The videos URL
     */
    fun updateView(pageTitle: String, steps: List<ParsedPairingStep>, videoUrl: String)

    /**
     * Called when devices have paired to the Pairing Subsystem to show the appropriate banner(s)
     *
     * @param count - The number of devices paired currently
     */
    fun devicesPaired(count: Int)

    /**
     * Some type of error has been received.
     *
     * @param throwable - The error
     */
    fun errorReceived(throwable: Throwable)

    /**
     * Called when pairing times-out on the subsystem / place altogether while viewing the
     * pairing steps for a device.
     *
     * @param hasDevicesPaired true if there are devices that have paired and need attention
     */
    fun pairingTimedOut(hasDevicesPaired: Boolean)
}

interface PairingStepsPresenter : BasePresenterContract<PairingStepsView> {
    /**
     * Starts pairing for [productAddress]
     *
     * @param productAddress - The FULL product address; EX: SERV:product:60e426
     */
    fun startPairing(productAddress: String, isForReconnect: Boolean = false)

    /**
     * Stops pairing
     */
    fun stopPairing()
}

/**
 * Describes a Pairing Step Input Type
 */
enum class PairingStepInputType {
    TEXT,
    HIDDEN,
}

/**
 * Describes an single user/hidden input
 *
 * Parcelable so that it can be passed to a new Activity / Fragment if needed
 *
 * @param inputType - The type of input
 * @param keyName - Name of the key for sending back to platform in a Map<*, *>
 * @param minLength - Minimum length requirement of the field
 * @param maxLength - Maximum length requirement of the field
 * @param label - Label to display to the user when rendering the form.
 * @param value - opt - If specified this is the default value for the form, or in the case of
 *                      HIDDEN this value should be submitted as the value to the Search command
 */
data class PairingStepInput(
    val inputType: PairingStepInputType,
    val keyName: String,
    val minLength: Int,
    val maxLength: Int,
    val label: String,
    val value: String? = null
) : Parcelable {
    constructor(source: Parcel) : this(
        PairingStepInputType.values()[source.readInt()],
        source.readString(),
        source.readInt(),
        source.readInt(),
        source.readString(),
        source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(inputType.ordinal)
        writeString(keyName)
        writeInt(minLength)
        writeInt(maxLength)
        writeString(label)
        writeString(value)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PairingStepInput> =
            object :
                Parcelable.Creator<PairingStepInput> {
                override fun createFromParcel(source: Parcel): PairingStepInput =
                    PairingStepInput(source)

                override fun newArray(size: Int): Array<PairingStepInput?> = arrayOfNulls(size)
            }
    }
}

/**
 * Holds data pertaining to a web (internet) link
 *
 * @param text - The text to use for the link
 * @param url  - The url to navigate to when this link is activated
 */
data class WebLink(
        val text: String,
        val url: String
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(text)
        writeString(url)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<WebLink> = object :
            Parcelable.Creator<WebLink> {
            override fun createFromParcel(source: Parcel): WebLink =
                WebLink(source)
            override fun newArray(size: Int): Array<WebLink?> = arrayOfNulls(size)
        }
    }
}

/**
 * Describes the type of pairing mode
 */
enum class PairingModeType {
    HUB,
    CLOUD,
    OAUTH,
}

/**
 * Holds data describing additional OAuth information
 *
 * @param oAuthUrl - The url to start OAuth authentication
 * @param oAuthStyle - The style of oAuth integration
 */
data class OAuthDetails(
        val oAuthUrl: String?,
        val oAuthStyle: String?
) : Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(oAuthUrl)
        writeString(oAuthStyle)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<OAuthDetails> = object :
            Parcelable.Creator<OAuthDetails> {
            override fun createFromParcel(source: Parcel): OAuthDetails =
                OAuthDetails(source)
            override fun newArray(size: Int): Array<OAuthDetails?> = arrayOfNulls(size)
        }
    }
}

/**
 * Marker class for a type of Pairing Step
 */
sealed class ParsedPairingStep(stepNumber: Int) : OrderedPairingStep(stepNumber)

/**
 * Ordered step
 */
open class OrderedPairingStep(val stepNumber: Int) : Parcelable {
    constructor(source: Parcel) : this(
            source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(stepNumber)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<OrderedPairingStep> = object :
            Parcelable.Creator<OrderedPairingStep> {
            override fun createFromParcel(source: Parcel): OrderedPairingStep =
                OrderedPairingStep(source)
            override fun newArray(size: Int): Array<OrderedPairingStep?> = arrayOfNulls(size)
        }
    }
}

/**
 * Describes, in full, a particular step in the pairing flow.
 * This should contain ALL information required to display the step it is describing - including
 * Form inputs, Oauth information, etc.
 */
data class InputPairingStep(
    val productId: String,
    val instructions: List<String>,
    val inputs: List<PairingStepInput>,
    val pairingModeType: PairingModeType,
    val title: String? = null,
    val info: String? = null,
    val link: WebLink? = null,
    private val order: Int,
    val id: String
) : ParsedPairingStep(order),
    Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            source.createStringArrayList(),
            source.createTypedArrayList(PairingStepInput.CREATOR),
            PairingModeType.values()[source.readInt()],
            source.readString(),
            source.readString(),
            source.readParcelable<WebLink>(WebLink::class.java.classLoader),
            source.readInt(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(productId)
        writeStringList(instructions)
        writeTypedList(inputs)
        writeInt(pairingModeType.ordinal)
        writeString(title)
        writeString(info)
        writeParcelable(link, 0)
        writeInt(order)
        writeString(id)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<InputPairingStep> = object :
            Parcelable.Creator<InputPairingStep> {
            override fun createFromParcel(source: Parcel): InputPairingStep =
                InputPairingStep(source)
            override fun newArray(size: Int): Array<InputPairingStep?> = arrayOfNulls(size)
        }
    }
}

/**
 * Describes, in full, a particular step in the pairing flow.
 * This should contain ALL information required to display the step it is describing - including
 * Form inputs, Oauth information, etc.
 */
data class SimplePairingStep(
    val productId: String,
    val instructions: List<String>,
    val pairingModeType: PairingModeType,
    val title: String? = null,
    val info: String? = null,
    val link: WebLink? = null,
    private val order: Int,
    val id: String,
    val oAuthDetails: OAuthDetails? = null
) : ParsedPairingStep(order),
    Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.createStringArrayList(),
        PairingModeType.values()[source.readInt()],
        source.readString(),
        source.readString(),
        source.readParcelable<WebLink>(WebLink::class.java.classLoader),
        source.readInt(),
        source.readString(),
        source.readParcelable<OAuthDetails>(OAuthDetails::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(productId)
        writeStringList(instructions)
        writeInt(pairingModeType.ordinal)
        writeString(title)
        writeString(info)
        writeParcelable(link, 0)
        writeInt(order)
        writeString(id)
        writeParcelable(oAuthDetails, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<SimplePairingStep> =
            object :
                Parcelable.Creator<SimplePairingStep> {
                override fun createFromParcel(source: Parcel): SimplePairingStep =
                    SimplePairingStep(source)

                override fun newArray(size: Int): Array<SimplePairingStep?> = arrayOfNulls(size)
            }
    }
}

/**
 * Describes, in full, a particular step in the pairing flow.
 * This should contain ALL information required to display the step it is describing - including
 * Form inputs, Oauth information, etc.
 */
data class AssistantPairingStep(
    val productId: String,
    val pairingModeType: PairingModeType,
    val manufacturer: String? = null,
    val name: String? = null,
    private val order: Int,
    val instructions : String? = null,
    val appUrl: String? = null
) : ParsedPairingStep(order),
    Parcelable {
    constructor(source: Parcel) : this(
            source.readString(),
            PairingModeType.values()[source.readInt()],
            source.readString(),
            source.readString(),
            source.readInt(),
            source.readString(),
            source.readString()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(productId)
        writeInt(pairingModeType.ordinal)
        writeString(manufacturer)
        writeString(name)
        writeInt(order)
        writeString(instructions)
        writeString(appUrl)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<AssistantPairingStep> = object :
            Parcelable.Creator<AssistantPairingStep> {
            override fun createFromParcel(source: Parcel): AssistantPairingStep =
                AssistantPairingStep(source)
            override fun newArray(size: Int): Array<AssistantPairingStep?> = arrayOfNulls(size)
        }
    }
}

data class WiFiSmartSwitchPairingStep(
    private val order: Int,
    val inputs: List<PairingStepInput> = emptyList()
) : ParsedPairingStep(order),
    Parcelable {
    fun getProductId() = WIFI_SMART_SWITCH_PRODUCT_ID

    constructor(source: Parcel) : this(
        source.readInt(),
        source.createTypedArrayList(PairingStepInput.CREATOR)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(order)
        writeTypedList(inputs)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<WiFiSmartSwitchPairingStep> =
            object :
                Parcelable.Creator<WiFiSmartSwitchPairingStep> {
                override fun createFromParcel(source: Parcel): WiFiSmartSwitchPairingStep =
                    WiFiSmartSwitchPairingStep(
                        source
                    )

                override fun newArray(size: Int): Array<WiFiSmartSwitchPairingStep?> =
                    arrayOfNulls(size)
            }
    }
}

data class BleGenericPairingStep(
    private val order: Int,
    val productId: String,
    val productShortName: String,
    val bleNamePrefix: String,
    val inputs: List<PairingStepInput> = emptyList(),
    val isForReconnect: Boolean = false
) : ParsedPairingStep(order), Parcelable {
    constructor(source: Parcel) : this(
        source.readInt(),
        source.readString(),
        source.readString(),
        source.readString(),
        source.createTypedArrayList(PairingStepInput.CREATOR),
        1 == source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeInt(order)
        writeString(productId)
        writeString(productShortName)
        writeString(bleNamePrefix)
        writeTypedList(inputs)
        writeInt((if (isForReconnect) 1 else 0))
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BleGenericPairingStep> =
            object : Parcelable.Creator<BleGenericPairingStep> {
                override fun createFromParcel(source: Parcel): BleGenericPairingStep = BleGenericPairingStep(source)
                override fun newArray(size: Int): Array<BleGenericPairingStep?> = arrayOfNulls(size)
            }
    }
}

data class BleWiFiReconfigureStep(
    val productId: String,
    val instructions: List<String>,
    val title: String? = null,
    val info: String? = null,
    val link: WebLink? = null,
    private val order: Int
) : ParsedPairingStep(order), Parcelable {
    constructor(source: Parcel) : this(
        source.readString(),
        source.createStringArrayList(),
        source.readString(),
        source.readString(),
        source.readParcelable<WebLink>(WebLink::class.java.classLoader),
        source.readInt()
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeString(productId)
        writeStringList(instructions)
        writeString(title)
        writeString(info)
        writeParcelable(link, 0)
        writeInt(order)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<BleWiFiReconfigureStep> =
            object : Parcelable.Creator<BleWiFiReconfigureStep> {
                override fun createFromParcel(source: Parcel): BleWiFiReconfigureStep = BleWiFiReconfigureStep(source)
                override fun newArray(size: Int): Array<BleWiFiReconfigureStep?> = arrayOfNulls(size)
            }
    }
}
