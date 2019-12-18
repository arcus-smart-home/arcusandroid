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

import android.os.Parcelable
import arcus.cornea.presenter.BasePresenterContract
import arcus.presentation.pairing.WIFI_SMART_SWITCH_PRODUCT_ID
import kotlinx.android.parcel.Parcelize

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
@Parcelize
data class PairingStepInput(
    val inputType: PairingStepInputType,
    val keyName: String,
    val minLength: Int,
    val maxLength: Int,
    val label: String,
    val value: String? = null
) : Parcelable

/**
 * Holds data pertaining to a web (internet) link
 *
 * @param text - The text to use for the link
 * @param url - The url to navigate to when this link is activated
 */
@Parcelize
data class WebLink(
    val text: String,
    val url: String
) : Parcelable

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
@Parcelize
data class OAuthDetails(
    val oAuthUrl: String?,
    val oAuthStyle: String?
) : Parcelable

/**
 * Marker class for a type of Pairing Step
 */
sealed class ParsedPairingStep(stepNumber: Int) : OrderedPairingStep(stepNumber)

/**
 * Ordered step
 */
@Parcelize
open class OrderedPairingStep(val stepNumber: Int) : Parcelable

/**
 * Describes, in full, a particular step in the pairing flow.
 * This should contain ALL information required to display the step it is describing - including
 * Form inputs, Oauth information, etc.
 */
@Parcelize
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
) : ParsedPairingStep(order), Parcelable

/**
 * Describes, in full, a particular step in the pairing flow.
 * This should contain ALL information required to display the step it is describing - including
 * Form inputs, Oauth information, etc.
 */
@Parcelize
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
) : ParsedPairingStep(order), Parcelable

/**
 * Describes, in full, a particular step in the pairing flow.
 * This should contain ALL information required to display the step it is describing - including
 * Form inputs, Oauth information, etc.
 */
@Parcelize
data class AssistantPairingStep(
    val productId: String,
    val pairingModeType: PairingModeType,
    val manufacturer: String? = null,
    val name: String? = null,
    private val order: Int,
    val instructions: String? = null,
    val appUrl: String? = null
) : ParsedPairingStep(order), Parcelable

@Parcelize
data class WiFiSmartSwitchPairingStep(
    private val order: Int,
    val inputs: List<PairingStepInput> = emptyList()
) : ParsedPairingStep(order), Parcelable {
    fun getProductId() = WIFI_SMART_SWITCH_PRODUCT_ID
}

@Parcelize
data class BleGenericPairingStep(
    private val order: Int,
    val productId: String,
    val productShortName: String,
    val bleNamePrefix: String,
    val inputs: List<PairingStepInput> = emptyList(),
    val isForReconnect: Boolean = false
) : ParsedPairingStep(order), Parcelable

@Parcelize
data class BleWiFiReconfigureStep(
    val productId: String,
    val instructions: List<String>,
    val title: String? = null,
    val info: String? = null,
    val link: WebLink? = null,
    private val order: Int
) : ParsedPairingStep(order), Parcelable
